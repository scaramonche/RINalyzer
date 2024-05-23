package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.util.HashMap;
import java.util.Iterator;
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
 * Class providing the methods for the computation of weighted random walk
 * centrality measures. These are betweenness, and receiver and transmitter
 * closeness centrality.
 * 
 * @author Nadezhda Doncheva
 */
public class RandomWalkCentrality extends Centrality {

	/**
	 * Initializes a new instance of the <code>RandomWalkCentrality</code>.
	 * 
	 * @param aNetwork
	 *            Network to be analyzed.
	 * @param aCyNode2Index
	 *            Mapping of nodes and node indices.
	 * @param aWeights
	 *            Adjacency matrix with modified edge weights, node indices are
	 *            stored in a <code>node2index</code> map.
	 * @param aSetSize
	 *            Number of selected nodes.
	 * @param aInvLapl
	 *            Inverse Laplacian
	 */
	public RandomWalkCentrality(Map<CyNode, Integer> aCyNode2Index, DoubleMatrix2D aWeights,
			Set<CyNode> aSet, CyNetwork aNetwork, DoubleMatrix2D aInvLapl) {
		super(aCyNode2Index, aSet, aNetwork, aInvLapl);
		precomputeDegrees(aWeights);
		precomputeZ();
	}

	/**
	 * Implements the random walk betweenness centrality measure, where the
	 * centrality value of a node is equal to the expected number of times the
	 * particle leaves the node on its random round-trip walk from s to t,
	 * averaged over the pairs of vertices given as input.
	 * 
	 * Important note: Since the basic quantity used in the definition is not
	 * symmetric, the contribution to centrality values of a pair (s,t) is not
	 * equal to the contribution of a pair (t,s) i.e., the order of nodes in the
	 * pair is important. However, if both pairs, (s,t) and (t,s), are given
	 * with an equal weight, then the contribution to a node is
	 * pairs(s,t)*d(x)*R(s,t). Thus this measure is redundant to degree
	 * centrality when for every pair both directions are considered with the
	 * same weight.
	 * 
	 * @param selectedSet
	 *            Set of selected nodes.
	 * @param pairs
	 *            Pairs of nodes for betweenness computation.
	 * @return Map of nodes and their respective betweenness centrality values.
	 */
	public Map<CyNode, Double> betweennessCentrality(Set<IntPair> pairs) {
		int betwNorm = 0;
		final DenseDoubleMatrix1D cent = new DenseDoubleMatrix1D(nodeCount);
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
						computeBetwForPair(node2index.get(source).intValue(), node2index
								.get(target).intValue(), cent);
					}
				}
			}
		}
		// normalize and save centrality values
		final Map<CyNode, Double> betweenness = new HashMap<CyNode, Double>(nodeCount);
		if (betwNorm > 0) {
			final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
			final CyTable nodeTable = network.getDefaultNodeTable();
			if (nodeTable.getColumn(Messages.CENT_RWB) == null) {
				nodeTable.createColumn(Messages.CENT_RWB, Double.class, false);
			}
			while (it.hasNext()) {
				final Map.Entry<CyNode, Integer> entry = it.next();
				double centValue = cent.get(entry.getValue().intValue()) / betwNorm;
				betweenness.put(entry.getKey(), new Double(centValue));
				nodeTable.getRow(entry.getKey().getSUID()).set(Messages.CENT_RWB,
						new Double(centValue));
				// nodeAttr.setAttribute(node.getIdentifier(), Messages.CENT_RWB
				// + "_", UtilsUI
				// .getThreeDigits(centValue));
			}
		}
		return betweenness;
	}

	/**
	 * Accumulate the betweenness of all nodes depending on the pair (s,t)
	 * 
	 * @param s
	 *            Index of the first/source node in the pair.
	 * @param t
	 *            Index of the second/target node in the pair.
	 * @param cent
	 *            Centrality values of all nodes so far.
	 */
	private void computeBetwForPair(int s, int t, DoubleMatrix1D cent) {
		CentralityAnalyzer.progress++;
		// h_st = m*(B[s,s] - 2*B[s,t] + B[t,t]) + z[t] - z[s]
		final double hitTime = m
				* (invLaplacian.get(s, s) - 2 * invLaplacian.get(s, t) + invLaplacian.get(t, t))
				+ z.get(t) - z.get(s);
		// u_st = B[:,s] - B[:,t] - (B[t,s] - B[t,t]) # adjust voltages so that
		// v_{st}(t) = 0.0
		final DoubleMatrix1D expVisits = invLaplacian.viewColumn(s).copy();
		expVisits.assign(invLaplacian.viewColumn(t), DoubleFunctions.minus);
		expVisits.assign(DoubleFunctions.minus(invLaplacian.get(t, s) - invLaplacian.get(t, t)));

		final Iterator<Integer> it = node2index.values().iterator();
		while (it.hasNext()) {
			// CentralityAnalyzer.progress++;
			// TODO: [Improve][Analysis] Consider end points or not?
			// if (sourceIndex != targetIndex && nodeIndex != sourceIndex &&
			// nodeIndex !=
			// targetIndex)
			final int idx = it.next().intValue();
			double value = cent.get(idx);
			value += (degrees.get(idx) * expVisits.get(idx)) / hitTime;
			cent.set(idx, value);
		}
	}

	/**
	 * Get the "receiver" closeness centrality, i.e. the inversion of the sum of
	 * first passage times H(s,v).
	 * 
	 * @param selectedSet
	 *            Set of selected nodes.
	 * @return Map of nodes and their respective "receiver" closeness
	 *         centrality.
	 */
	public Map<CyNode, Double> receiverClosenessCentrality() {
		if (receiverCloseness == null) {
			computeCloseness();
		}
		if (receiverCloseness != null) {
			final Iterator<Map.Entry<CyNode, Double>> it = receiverCloseness.entrySet().iterator();
			final CyTable nodeTable = network.getDefaultNodeTable();
			if (nodeTable.getColumn(Messages.CENT_RWRC) == null) {
				nodeTable.createColumn(Messages.CENT_RWRC, Double.class, false);
			}
			while (it.hasNext()) {
				final Map.Entry<CyNode, Double> entry = it.next();
				nodeTable.getRow(entry.getKey().getSUID())
						.set(Messages.CENT_RWRC, entry.getValue());
				// nodeAttr.setAttribute(node.getIdentifier(), Messages.CENT_RWB
				// + "_", UtilsUI
				// .getThreeDigits(centValue));
			}

		}
		return receiverCloseness;
	}

	/**
	 * Get the "transmitter" closeness centrality, i.e. the inversion of the sum
	 * of first passage times H(v,s).
	 * 
	 * @param selectedSet
	 *            Set of selected nodes.
	 * @return Map of nodes and their respective "transmitter" closeness
	 *         centrality.
	 */
	public Map<CyNode, Double> transmitterClosenessCentrality() {
		if (transmitterCloseness == null) {
			computeCloseness();
		}
		if (transmitterCloseness != null) {
			final Iterator<Map.Entry<CyNode, Double>> it = transmitterCloseness.entrySet()
					.iterator();
			final CyTable nodeTable = network.getDefaultNodeTable();
			if (nodeTable.getColumn(Messages.CENT_RWTC) == null) {
				nodeTable.createColumn(Messages.CENT_RWTC, Double.class, false);
			}
			while (it.hasNext()) {
				final Map.Entry<CyNode, Double> entry = it.next();
				nodeTable.getRow(entry.getKey().getSUID())
						.set(Messages.CENT_RWTC, entry.getValue());
			}

		}
		return transmitterCloseness;
	}

	/**
	 * Implements two closeness centrality measures based on random walks. In
	 * particular, we use either first passage times H(v,s) to measure how
	 * quickly a node v can reach the selected nodes, "transmitter efficiency"
	 * or first passage times H(s, v) to measure how quickly the selected nodes
	 * can reach v, "receiver efficiency".
	 * 
	 * @param selectedSet
	 *            Set of selected nodes.
	 */
	private void computeCloseness() {
		receiverCloseness = new HashMap<CyNode, Double>(nodeCount);
		// transmitterCloseness = new HashMap<CyNode, Double>(nodeCount);
		final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
		while (it.hasNext()) {
			CentralityAnalyzer.progress++;
			if (CentralityAnalyzer.cancelled) {
				return;
			}
			final Map.Entry<CyNode, Integer> entry = it.next();
			final int n = entry.getValue().intValue();
			final DoubleMatrix1D hitRec = new DenseDoubleMatrix1D(setSize);
			// final DoubleMatrix1D hitTrans = new DenseDoubleMatrix1D(setSize);
			int counter = 0;
			for (final CyNode setCyNode : selectedSet) {
				// CentralityAnalyzer.progress++;
				final int s = node2index.get(setCyNode).intValue();
				final double resValue = m
						* (invLaplacian.get(n, n) - 2 * invLaplacian.get(n, s) + invLaplacian.get(
								s, s));
				final double zValue = z.get(s) - z.get(n);
				hitRec.set(counter, resValue - zValue);
				// hitTrans.set(counter, resValue + zValue);
				counter++;
			}
			// save and normalize closeness values
			double hitRecSum = hitRec.zSum();
			if (hitRecSum > 0.0) {
				hitRecSum = setSize / hitRecSum;
			}
			receiverCloseness.put(entry.getKey(), new Double(hitRecSum));
			// double hitTransSum = hitTrans.zSum();
			// if (hitTransSum > 0.0) {
			// hitTransSum = setSize / hitTransSum;
			// }
			// transmitterCloseness.put(node, new Double(hitTransSum));
		}
	}

	/**
	 * Precompute the weighted degree of each node.
	 * 
	 * @param weightMatrix
	 *            Adjacency matrix with modified edge weights, node indices are
	 *            stored in a <code>node2index</code> map.
	 */
	private void precomputeDegrees(DoubleMatrix2D weightMatrix) {
		// precompute weighted degrees for every node in the network
		degrees = new DenseDoubleMatrix1D(nodeCount);
		final Iterator<Integer> it = node2index.values().iterator();
		while (it.hasNext()) {
			final int nodeIndex = it.next().intValue();
			final double degree = weightMatrix.viewRow(nodeIndex).zSum();
			// final Set<CyNode> neighbors = Utils.getNeighbors(network, node);
			// double degree = 0.0;
			// for (final CyNode neighbor : neighbors) {
			// final int neighborIndex = node2index.get(neighbor).intValue();
			// degree += weightMatrix.get(nodeIndex, neighborIndex);
			// }
			degrees.set(nodeIndex, degree);
		}
		m = 0.5 * degrees.zSum();
	}

	/**
	 * Precompute z, i.e. the sum of the "weighted" effective resistances of all
	 * nodes in respect to a source node.
	 */
	private void precomputeZ() {
		// precompute z[u] = 1/2 * \sum_{x} R(u,x)*degree[x] for every node in
		// the network;
		z = new DenseDoubleMatrix1D(nodeCount);
		final Iterator<Integer> it = node2index.values().iterator();
		while (it.hasNext()) {
			final int i = it.next().intValue();
			double value = 0.0;
			// z[i] = 0.5 * sum_{x} (degree[x] * (invLapl[i,i] - 2*invLapl[i,x]
			// + invLapl[x,x])
			final double nodeInvLapl = invLaplacian.get(i, i);
			for (int x = 0; x < nodeCount; x++) {
				value += degrees.get(x)
						* (nodeInvLapl - 2 * invLaplacian.get(i, x) + invLaplacian.get(x, x));
			}
			z.set(i, 0.5 * value);
		}
	}

	/**
	 * Map of each node in the network with its computed transmitter closeness
	 * values.
	 */
	private Map<CyNode, Double> transmitterCloseness;

	/**
	 * Map of each node in the network with its computed receiver closeness
	 * values.
	 */
	private Map<CyNode, Double> receiverCloseness;

	/**
	 * Weighted degree of each node. Indices are stored in
	 * <code>node2index</code>.
	 */
	private DoubleMatrix1D degrees;

	/**
	 * Sum of the degrees of the nodes, i.e. 1/2 * sum_{x} degree_x
	 */
	private double m;

	/**
	 * Vector with the sum of the "weighted" effective resistance of all nodes
	 * in respect to u (z[u] = 1/2 * \sum_{x} R(u,x)*d_x for every node in the
	 * network; d_x is weighted degree of x.
	 */
	private DoubleMatrix1D z;
}
