package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.util.ArrayList;
import java.util.Arrays;
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
import cern.jet.math.tdouble.DoubleFunctions;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Class providing the methods for the computation of weighted current flow centrality measures.
 * These are betweenness, and closeness centrality.
 * 
 * @author Nadezhda Doncheva
 */
public class CurrentFlowCentrality extends Centrality {

	/**
	 * Initializes a new instance of the <code>CurrentFlowCentrality</code>.
	 * 
	 * @param aNetwork
	 *          Network to be analyzed.
	 * @param aCyNode2Index
	 *          Mapping of nodes and node indices.
	 * @param aSetSize
	 *          Number of selected nodes.
	 * @param aInvLapl
	 *          Inverse Laplacian.
	 */
	public CurrentFlowCentrality(Map<CyNode, Integer> aCyNode2Index, Set<CyNode> aSet,
			CyNetwork aNetwork, DoubleMatrix2D aInvLapl) {
		super(aCyNode2Index, aSet, aNetwork, aInvLapl);
	}

	/**
	 * Implements the current flow betweenness centrality of M.Newman, A measure of betweenness
	 * centrality based on random walks, Social Networks 27:39-54. The centrality value of a node is
	 * determined by the amount of current passing through the node when one unit of electrical
	 * current is shipped from s to t.
	 * 
	 * We introduce following generalizations to the original measure: 1.) The graph can be weighted.
	 * The edge weights correspond to conductances of the appropriate electrical network. 2.) Not all
	 * pairs are considered, but only those specified in <code>pairs</code>.
	 * 
	 * @param selectedSet
	 *          Set of selected nodes.
	 * @param pairs
	 *          Pairs of nodes for betweenness computation.
	 * @param weightMatrix
	 *          Adjacency matrix with modified edge weights, node indices are stored in a
	 *          <code>node2index</code> map.
	 * @return Map containing the centrality value of each node.
	 */
	public Map<CyNode, Double> betweennessCentrality(Set<IntPair> pairs, DoubleMatrix2D weightMatrix) {
		final DoubleMatrix1D cent = new DenseDoubleMatrix1D(nodeCount);
		final Map<CyNode, Double> betweenness = new HashMap<CyNode, Double>(nodeCount);
		final CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(Messages.CENT_CFB) == null) {
			nodeTable.createColumn(Messages.CENT_CFB, Double.class, false);
		}
		final int[] nodeIndices = getIndices(node2index.keySet());
		final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
		final int p = pairs != null ? pairs.size() : nodeCount * (nodeCount - 1);
		final int progressAccu = nodeCount > p ? 1 : p / nodeCount;
		while (it.hasNext()) {
			CentralityAnalyzer.progress += progressAccu;
			if (CentralityAnalyzer.cancelled) {
				return null;
			}
			final Map.Entry<CyNode, Integer> entry = it.next();
			final int nodeIndex = entry.getValue().intValue();
			// handle neighbors of node
			// final Set<CyNode> neighbors = getWeightedNeighbors(entry.getKey(), weightMatrix
			// .viewRow(nodeIndex));
			// final Map<CyNode, Integer> neighbor2index = new HashMap<CyNode, Integer>(neighbors.size());
			// int counter = 0;
			// for (final CyNode neighborCyNode : neighbors) {
			// final int neighborIndex = node2index.get(neighborCyNode).intValue();
			// neighborIndices[counter] = neighborIndex;
			// neighbor2index.put(neighborCyNode, new Integer(counter));
			// counter++;
			// }
			List<Integer> neighbors = new ArrayList<Integer>();
			for (int i = 0; i < nodeCount; i++) {
				final double weight = weightMatrix.get(nodeIndex, i);
				if (weight != 0.0) {
					neighbors.add(new Integer(i));
				}
			}
			final int[] neighborIndices = new int[neighbors.size()];
			for (int i = 0; i < neighbors.size(); i++) {
				neighborIndices[i] = neighbors.get(i).intValue();
			}

			// form B_hat, such that dim(B_hat) = (|N_i|, n) and B_hat[jr] = B[ir] - B[jr]
			final DoubleMatrix2D bHat = invLaplacian.viewSelection(neighborIndices, nodeIndices).copy();
			for (int bHatNbIdx = 0; bHatNbIdx < neighborIndices.length; bHatNbIdx++) {
				final int neighborIndex = neighborIndices[bHatNbIdx];
				// B_hat[j,:] = B[i,:] - B_hat[j,:]
				bHat.viewRow(bHatNbIdx).assign(invLaplacian.viewRow(nodeIndex),
						DoubleFunctions.swapArgs(DoubleFunctions.minus));
				// B_hat = B_hat * neighbor_weight[i] outside neighbor loop =>
				// B_hat[:, j] = B_hat[:, j] * neighbor_weight[i, j]
				bHat.viewColumn(neighborIndex).assign(
						DoubleFunctions.mult(weightMatrix.get(nodeIndex, neighborIndex)));
			}
			// compute centrality
			if (pairs != null) {
				for (final IntPair pair : pairs) {
					cent.set(
							nodeIndex,
							cent.get(nodeIndex)
									+ computeBetwForPair(nodeIndex, pair.first(), pair.second(),
											bHat.viewColumn(pair.first()).copy(), bHat.viewColumn(pair.second())));
				}
			} else {
				for (final CyNode source : selectedSet) {
					for (final CyNode target : selectedSet) {
						if (!source.equals(target)) {
							cent.set(
									nodeIndex,
									cent.get(nodeIndex)
											+ computeBetwForPair(nodeIndex, node2index.get(source).intValue(), node2index
													.get(target).intValue(),
													bHat.viewColumn(node2index.get(source).intValue()).copy(), bHat
															.viewColumn(node2index.get(target).intValue())));
						}
					}
				}
			}
			double centValue = p > 0 ? cent.get(nodeIndex) / p : cent.get(nodeIndex);
			betweenness.put(entry.getKey(), new Double(centValue));
			nodeTable.getRow(entry.getKey().getSUID()).set(Messages.CENT_CFB, new Double(centValue));
			// nodeAttr.setAttribute(node.getIdentifier(), Messages.CENT_CFB+"_",
			// UtilsUI.getThreeDigits(centValue));
		}
		return betweenness;
	}

	/**
	 * Accumulate the betweenness of the node <code>n</code> depending on the pair (s,t).
	 * 
	 * @param n
	 *          Index of node n.
	 * @param s
	 *          Index of the first/source node in the pair.
	 * @param t
	 *          Index of the second/target node in the pair.
	 * @param bHat
	 *          Vector needed for the computation of the betweenness of n.
	 * @param cent
	 *          Centrality values of all nodes so far.
	 */
	private double computeBetwForPair(int n, int s, int t, DoubleMatrix1D sColumn,
			DoubleMatrix1D tColumn) {
		// TODO: [Improve][Analysis] Consider end points or not?
		// if (sourceIndex != targetIndex && nodeIndex != sourceIndex && nodeIndex !=
		// targetIndex)
		// {
		final double sum = sColumn.assign(tColumn,
				DoubleFunctions.chain(DoubleFunctions.abs, DoubleFunctions.minus)).zSum();
		if (n != s && n != t) {
			// c[i] += 0.5*numpy.sum(numpy.abs(B_hat[:,s] - B_hat[:,t]))
			return 0.5d * sum;
		}
		return 1.0d * sum;
	}

	/**
	 * Return a sorted array of the internal node indices of the nodes in <code>aCyNodeSet</code>.
	 * 
	 * @param aCyNodeSet
	 *          Set with nodes
	 * @return Sorted array with node indices of the nodes in <code>aCyNodeSet</code>.
	 */
	private int[] getIndices(Set<CyNode> aCyNodeSet) {
		final int[] indices = new int[aCyNodeSet.size()];
		int counter = 0;
		for (final CyNode node : aCyNodeSet) {
			indices[counter] = node2index.get(node).intValue();
			counter++;
		}
		Arrays.sort(indices);
		return indices;
	}

	/**
	 * Get all nodes that have an edge with <code>aCyNode</code>, whose weight is bigger than 0.
	 * 
	 * @param aNetwork
	 *          Target network.
	 * @param aCyNode
	 *          Source node.
	 * @param weight
	 *          Weights of the edges of <code>aCyNode</code> with all other nodes in the network.
	 * @return Set of the neighbors of <code>aCyNode</code>.
	 */
	// private Set<CyNode> getWeightedNeighbors(CyNetwork aNetwork, CyNode aCyNode, DoubleMatrix1D
	// weight) {
	// final int nodeIndex = aCyNode.getRootGraphIndex();
	// final int[] adjacentEdgesIndices = aNetwork.getAdjacentEdgeIndicesArray(nodeIndex, true,
	// true, true);
	// final Set<CyNode> neighbors = new HashSet<CyNode>(adjacentEdgesIndices.length);
	// for (final int edgeIndex : adjacentEdgesIndices) {
	// final Edge e = aNetwork.getEdge(edgeIndex);
	// final CyNode source = e.getSource();
	// if (source != aCyNode) {
	// if (weight.get(node2index.get(source).intValue()) > 0.0d) {
	// neighbors.add(source);
	// }
	// } else {
	// final CyNode target = e.getTarget();
	// if (target != aCyNode) {
	// if (weight.get(node2index.get(target).intValue()) > 0.0d) {
	// neighbors.add(target);
	// }
	// }
	// }
	// }
	// return neighbors;
	// }

	/**
	 * Implements the current flow closeness centrality. The centrality value of the node is
	 * determined by how close it is to nodes given by selectedSet. If selectedSet is empty then all
	 * nodes of the network are considered. The distance function d(i,j) is effective resistance
	 * between nodes i and j.
	 * 
	 * @param selectedSet
	 *          Set of selected nodes.
	 * @return Map containing the centrality value of each node.
	 */
	public Map<CyNode, Double> closenessCentrality() {
		final Map<CyNode, Double> closeness = new HashMap<CyNode, Double>(nodeCount);
		final DoubleMatrix1D cent = new DenseDoubleMatrix1D(nodeCount);
		for (final CyNode source : selectedSet) {
			CentralityAnalyzer.progress++;
			if (CentralityAnalyzer.cancelled) {
				return null;
			}
			final int sourceIndex = node2index.get(source).intValue();
			final double sourceDegree = invLaplacian.get(sourceIndex, sourceIndex);
			for (int n = 0; n < cent.size(); n++) {
				double value = cent.get(n) + sourceDegree - 2 * invLaplacian.get(sourceIndex, n)
						+ invLaplacian.get(n, n);
				cent.set(n, value);
			}
		}
		// normalize and save
		final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
		final CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(Messages.CENT_CFC) == null) {
			nodeTable.createColumn(Messages.CENT_CFC, Double.class, false);
		}
		while (it.hasNext()) {
			final Map.Entry<CyNode, Integer> entry = it.next();
			final int nodeIndex = entry.getValue().intValue();
			double centValue = cent.get(nodeIndex);
			if (centValue != 0.0d) {
				centValue = setSize / centValue;
			}
			closeness.put(entry.getKey(), new Double(centValue));
			nodeTable.getRow(entry.getKey().getSUID()).set(Messages.CENT_CFC, new Double(centValue));
			// nodeAttributes.setAttribute(node.getIdentifier(), Messages.CENT_CFC+"_",
			// UtilsUI.getThreeDigits(centValue));
		}
		return closeness;
	}
}
