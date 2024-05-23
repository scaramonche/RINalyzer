package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Class providing the methods for the computation of weighted shortest path centrality measures.
 * These are betweenness, closeness, and degree centrality.
 * 
 * @author Nadezhda Doncheva
 */

public class ShortestPathCentrality extends Centrality {

	/**
	 * Initializes a new instance of the <code>ShortestPathCentrality</code>.
	 * 
	 * @param aNetwork
	 *          Network to be analyzed.
	 * @param aCyNode2Index
	 *          Mapping of nodes and node indices.
	 * @param aSetSize
	 *          Number of selected nodes.
	 */
	public ShortestPathCentrality(Map<CyNode, Integer> aCyNode2Index, Set<CyNode> aSet,
			CyNetwork aNetwork) {
		super(aCyNode2Index, aSet, aNetwork);
		distance = null;
		spCount = null;
	}

	/**
	 * Compute and return the betweenness centrality of all pairs in <code>pairs</code> using the
	 * weights form <code>weights</code>. The betweenness centrality is computed considering a set of
	 * selected node pairs, i.e. the values of each node is not global, considering all pairs, but
	 * relative to the selected set of node pairs. Betweenness centrality is normalized, so it's
	 * always between 0 and 1. This method also saves each computed value in the node attributes.
	 * 
	 * @param selectedSet
	 *          Set of selected nodes.
	 * @param pairs
	 *          Pairs of nodes for betweenness computation.
	 * @param weightMatrix
	 *          Adjacency matrix with modified edge weights, node indices are stored in a
	 *          <code>node2index</code> map.
	 * @return Map of nodes and their computed betweenness centrality values.
	 */
	public Map<CyNode, Double> betweennessCentrality(Set<IntPair> pairs, DoubleMatrix2D weightMatrix) {
		// initialize
		// getAllDistancesAndSpCount(weightMatrix);
		final DenseDoubleMatrix1D cent = new DenseDoubleMatrix1D(nodeCount);
		int betwNorm = 0;
		// compute betweenness with respect to a set of selected nodes
		if (pairs != null) {
			betwNorm = pairs.size();
			for (final IntPair pair : pairs) {
				if (CentralityAnalyzer.cancelled) {
					return null;
				}
				computeBetwForPair(pair.first(), pair.second(), cent);
			}
		} else {
			betwNorm = nodeCount * (nodeCount - 1);
			for (final CyNode source : selectedSet) {
				for (final CyNode target : selectedSet) {
					if (!source.equals(target)) {
						if (CentralityAnalyzer.cancelled) {
							return null;
						}
						computeBetwForPair(node2index.get(source).intValue(),
								node2index.get(target).intValue(), cent);
					}
				}
			}
		}
		final Map<CyNode, Double> betweenness = new HashMap<CyNode, Double>(nodeCount);
		if (betwNorm > 0) {
			final CyTable nodeTable = network.getDefaultNodeTable();
			if (nodeTable.getColumn(Messages.CENT_SPB) == null) {
				nodeTable.createColumn(Messages.CENT_SPB, Double.class, false);
			}
			// normalize and save centrality values
			final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
			while (it.hasNext()) {
				final Map.Entry<CyNode, Integer> entry = it.next();
				double centValue = cent.get(entry.getValue().intValue()) / betwNorm;
				betweenness.put(entry.getKey(), new Double(centValue));
				nodeTable.getRow(entry.getKey().getSUID()).set(Messages.CENT_SPB, new Double(centValue));
				// nodeAttributes.setAttribute(node.getIdentifier(), Messages.CENT_SPB + "_", UtilsUI
				// .getThreeDigits(centValue));
			}
		}
		return betweenness;
	}

	/**
	 * Accumulate the betweenness of all nodes depending on the pair (s,t)
	 * 
	 * @param s
	 *          Index of the first/source node in the pair.
	 * @param t
	 *          Index of the second/target node in the pair.
	 * @param cent
	 *          Centrality values of all nodes so far.
	 */
	private void computeBetwForPair(int s, int t, DoubleMatrix1D cent) {
		CentralityAnalyzer.progress++;
		final Iterator<Integer> it = node2index.values().iterator();
		while (it.hasNext()) {
			final int n = it.next().intValue();
			// TODO: [Improve][Analysis] Consider end points or not?
			// if (nodeIndex != firstIndex && nodeIndex != secondIndex) {
			if (distance.get(s, t) != 0.0d
					&& distance.get(s, t) == (distance.get(s, n) + distance.get(n, t))) {
				double betweennessValue = spCount.get(s, n) * spCount.get(n, t) / spCount.get(s, t)
						+ cent.get(n);
				cent.set(n, betweennessValue);
			}
		}
	}

	/**
	 * Compute and return the closeness centrality of all nodes using the weights from
	 * <code>weights</code> and <code>selectedSet</code> as reference nodes. The closeness centrality
	 * is computed considering a set of selected nodes, i.e. during the computation not all shortest
	 * paths are considered, but only those leading to node in the specified set.
	 * 
	 * The closeness centrality is normalized, so it's always between 0 and 1. This method also saves
	 * each computed value in the node attributes.
	 * 
	 * Note: This method should only only be called after betweennessCentrality!
	 * 
	 * @param selectedSet
	 *          Set of selected nodes.
	 * @return Map of nodes and their computed closeness centrality values.
	 */
	public Map<CyNode, Double> closenessCentrality() {
		// initialize
		int[] closNorm = new int[nodeCount];
		final DenseDoubleMatrix1D cent = new DenseDoubleMatrix1D(nodeCount);
		// compute closeness with respect to a set of selected nodes
		for (final CyNode source : selectedSet) {
			CentralityAnalyzer.progress++;
			if (CentralityAnalyzer.cancelled) {
				return null;
			}
			final DoubleMatrix1D distances = getDistances(source);
			Iterator<Integer> it = node2index.values().iterator();
			while (it.hasNext()) {
				final int n = it.next().intValue();
				double nodeDistance = distances.get(n);
				if (nodeDistance != 0.0d) {
					closNorm[n]++;
					nodeDistance += cent.get(n);
					cent.set(n, nodeDistance);
				}
			}
		}
		final Map<CyNode, Double> closeness = new HashMap<CyNode, Double>(nodeCount);
		final CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(Messages.CENT_SPC) == null) {
			nodeTable.createColumn(Messages.CENT_SPC, Double.class, false);
		}
		// normalize and save centrality values
		Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<CyNode, Integer> entry = it.next();
			final int n = entry.getValue().intValue();
			double centValue = cent.get(n);
			if (closNorm[n] > 0 && centValue != 0.0d) {
				centValue = closNorm[n] / centValue;
			}
			closeness.put(entry.getKey(), new Double(centValue));
			nodeTable.getRow(entry.getKey().getSUID()).set(Messages.CENT_SPC, new Double(centValue));
			// nodeAttributes.setAttribute(node.getIdentifier(), Messages.CENT_SPC + "_", UtilsUI
			// .getThreeDigits(centValue));
		}
		return closeness;
	}

	/**	
	 * Compute and return the degree centrality of all nodes using the weights form
	 * <code>weights</code>, <code>selectedSet</code> as reference nodes, and a <code>aCutoff</code>.
	 * 
	 * The degree centrality is computed considering a set of selected nodes, i.e. during the
	 * computation not all neighbors are considered, but only those from the specified set. The cutoff
	 * decides how much the distance between a node and its neighbor should be, in order to still call
	 * it neighbor, i.e. for each node in the network its distance to <code>source</code> is computed
	 * and the node is counted as a neighbor, i.e. the degree is increased, only if the distance is
	 * smaller than <code>aCutoff</code>.
	 * 
	 * The degree centrality is normalized, so it's always between 0 and 1. This method also saves
	 * each computed value in the node attributes.
	 * 
	 * @param selectedSet
	 *          Set of selected nodes.
	 * @param weights
	 *          Adjacency matrix with modified edge weights, node indices are stored in a
	 *          <code>node2index</code> map.
	 * @param aCutoff
	 *          Cutoff for the weighted degree.
	 * @return Map of nodes and their computed closeness centrality values.
	 */
	public Map<CyNode, Double> degreeCentrality(DoubleMatrix2D weights, double aCutoff) {
		// initialize
		getAllDistancesAndSpCount(weights);
		final DenseDoubleMatrix1D cent = new DenseDoubleMatrix1D(nodeCount);
		// final Dijkstra dijkstra = new Dijkstra(node2index);
		// compute degree with respect to a set of selected nodes
		for (final CyNode source : selectedSet) {
			CentralityAnalyzer.progress++;
			if (CentralityAnalyzer.cancelled) {
				return null;
			}
			// final DoubleMatrix1D distances = dijkstra.computeDistances(source, weights, null);
			final DoubleMatrix1D distances = getDistances(source);
			final Iterator<Integer> it = node2index.values().iterator();
			while (it.hasNext()) {
				final int n = it.next().intValue();
				if (n != node2index.get(source).intValue() && distances.get(n) != 0.0d
						&& distances.get(n) <= aCutoff) {
					cent.set(n, 1.0d + cent.get(n));
				}
			}
		}
		final Map<CyNode, Double> degree = new HashMap<CyNode, Double>(nodeCount);
		final CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(Messages.CENT_SPD) == null) {
			nodeTable.createColumn(Messages.CENT_SPD, Double.class, false);
		}
		final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<CyNode, Integer> entry = it.next();
			final int n = entry.getValue().intValue();
			double centValue = cent.get(n);
			if (!selectedSet.contains(entry.getKey())) {
				centValue /= setSize;
			} else {
				centValue /= setSize - 1;
			}
			degree.put(entry.getKey(), new Double(centValue));
			nodeTable.getRow(entry.getKey().getSUID()).set(Messages.CENT_SPD, new Double(centValue));
			// nodeAttributes.setAttribute(node.getIdentifier(), Messages.CENT_SPD + "_", UtilsUI
			// .getThreeDigits(centValue));
		}
		return degree;
	}

	/**
	 * Get the map of all distances between <code>aSource</code> and any other node in the network.
	 * 
	 * @param aSource
	 *          Source node to compute shortest paths from it.
	 * @return Map of all distances between <code>aSource</code> and any other node in the network.
	 */
	private DoubleMatrix1D getDistances(CyNode aSource) {
		return distance.viewRow(node2index.get(aSource).intValue()).copy();
	}

	/**
	 * Compute all shortest paths and count the shortest paths for each node. Computation uses the
	 * Dijkstra algorithm.
	 * 
	 * @param network
	 *          Network to be analyzed.
	 * @param weightMatrix
	 *          Adjacency matrix with modified edge weights, node indices are stored in a
	 *          <code>node2index</code> map.
	 */
	private void getAllDistancesAndSpCount(DoubleMatrix2D weightMatrix) {
		distance = new SparseDoubleMatrix2D(nodeCount, nodeCount);
		spCount = new SparseDoubleMatrix2D(nodeCount, nodeCount);
		final Dijkstra dijkstra = new Dijkstra(node2index);
		final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
		while (it.hasNext()) {
			CentralityAnalyzer.progress++;
			final Map.Entry<CyNode, Integer> entry = it.next();
			final Map<CyNode, List<CyNode>> predMap = new HashMap<CyNode, List<CyNode>>(nodeCount);
			final DoubleMatrix1D distances = dijkstra.computeDistances(entry.getKey(), weightMatrix,
					predMap);
			distance.viewRow(entry.getValue().intValue()).assign(distances);
			spCount.viewRow(entry.getValue().intValue()).assign(
					dijkstra.computeSPCount(entry.getKey(), distances, predMap));
		}
	}

	/**
	 * A n x n matrix of the distances between all nodes in the network, where n is the number of
	 * nodes and each node corresponds to an index as stored in the <code>nodeToIndex</code> map.
	 */
	private DoubleMatrix2D distance;

	/**
	 * A n x n matrix of the number of shortest paths between all nodes in the network, where n is the
	 * number of nodes and each node corresponds to an index as stored in the <code>nodeToIndex</code>
	 * map.
	 */
	private DoubleMatrix2D spCount;

}
