package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.cytoscape.model.CyNode;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

/**
 * Implementation of the Dijkstra algorithm for finding the shortest path from a source node to all other
 * nodes in a weighted graph (network).
 * 
 * @author Nadezhda Doncheva
 */

public class Dijkstra {

	/**
	 * Initializes a new instance of <code>Dijkstra</code>. The constructor doesn't compute the shortest
	 * paths. To do so, you have to run the <code>computeDijkstra</code> method.
	 * 
	 * @param aCyNodeCount
	 *            Number of network nodes.
	 * @param aCyNode2Index
	 *            Map of nodes in the network and the indices of the adjacency matrix.
	 */
	public Dijkstra(Map<CyNode, Integer> aCyNode2Index) {
		node2index = aCyNode2Index;
		nodeCount = node2index.size();
		index2node = new HashMap<Integer, CyNode>();
		createIndex2CyNode();
	}

	/**
	 * Compute the shortest paths from <code>sourceCyNode</code> to all other nodes in the network
	 * <code>network</code> using the edge weights <code>weights</code> and stores all predecessor(s) on
	 * the shortest path(s) to <code>sourceCyNode</code>. The shortest paths are stored in a map
	 * <code>distances</code> and the predecessors in <code>predMap</code>.
	 * 
	 * @param sourceCyNode
	 *            Source node.
	 * @param network
	 *            Network to be analyzed.
	 * @param weights
	 *            Adjacency matrix of the network containing the weights of the edges. The indices are mapped
	 *            to the nodes in <code>aCyNode2Index</code>.
	 * @param predMap
	 *            Map to store the predecessors. Can be <code>null</code> and then the predecessors don't
	 *            need to be stored.
	 * @return
	 */
	public DoubleMatrix1D computeDistances(CyNode sourceCyNode, DoubleMatrix2D weights,
			Map<CyNode, List<CyNode>> predMap) {
		final DoubleMatrix1D distances = new DenseDoubleMatrix1D(nodeCount);
		// predMap = new HashMap<CyNode, List<CyNode>>(nodeCount);
		Set<CyNode> nodes = new HashSet<CyNode>(nodeCount);
		nodes.add(sourceCyNode);
		if (predMap != null) {
			predMap.put(sourceCyNode, new ArrayList<CyNode>());
		}
		PriorityQueue<CyNodeDoublePair> queue = new PriorityQueue<CyNodeDoublePair>(20,
				new DistanceComparator());
		final CyNodeDoublePair source = new CyNodeDoublePair(sourceCyNode, new Double(0.0));
		queue.offer(source);
		while (!queue.isEmpty()) {
			final CyNodeDoublePair current = queue.poll();
			final int currentIndex = node2index.get(current.node()).intValue();
			final Set<Integer> neighbors = getNeighbors(weights.viewRow(currentIndex));
			for (final Integer neighborIndex : neighbors) {
				final CyNode neighborCyNode = index2node.get(neighborIndex);
				final double weight = weights.get(currentIndex, neighborIndex.intValue());
				if (weight > 0.0d) {
					final double neighborDist = current.value().doubleValue() + weight;
					final double oldNeighborDist = distances.get(neighborIndex.intValue());
					// shorter path to neighbor found?
					if (!nodes.contains(neighborCyNode)) {
						nodes.add(neighborCyNode);
						queue.offer(new CyNodeDoublePair(neighborCyNode, new Double(neighborDist)));
						// update distance
						distances.set(neighborIndex.intValue(), neighborDist);
						// update pred
						if (predMap != null) {
							predMap.put(neighborCyNode, createPredList(current.node()));
						}
					} else if (oldNeighborDist > neighborDist) {
						// update queue with the new distance
						queue.remove(new CyNodeDoublePair(neighborCyNode, new Double(oldNeighborDist)));
						queue.offer(new CyNodeDoublePair(neighborCyNode, new Double(neighborDist)));
						// update distance
						distances.set(neighborIndex.intValue(), neighborDist);
						// update pred
						if (predMap != null) {
							predMap.put(neighborCyNode, createPredList(current.node()));
						}
					} else if (neighborDist != 0.0 && oldNeighborDist == neighborDist && predMap != null) {
						final List<CyNode> predecessors = predMap.get(neighborCyNode);
						predecessors.add(current.node());
					}
				}
			}
		}
		return distances;
	}

	/**
	 * Create a new predecessor list containing the <code>pred</code> node.
	 * 
	 * @param pred
	 *            CyNode to be added as predecessor.
	 */
	private List<CyNode> createPredList(CyNode pred) {
		// update predecessor list
		List<CyNode> predecessors = new ArrayList<CyNode>(1);
		predecessors.add(pred);
		return predecessors;
	}

	/**
	 * Compute the number of shortest paths and store it in the map <code>spCount</code>. This method
	 * should be called only after calling <code>computeDijkstra</code>.
	 * 
	 * @param sourceCyNode
	 *            Source node.
	 * @param distances
	 *            Distance from <code>sourceCyNode</code> to any other node in the network.
	 * @param predMap
	 *            Map to store the predecessors. Can be <code>null</code> and then the predecessors don't
	 *            need to be stored.
	 */
	public DoubleMatrix1D computeSPCount(CyNode sourceCyNode, DoubleMatrix1D distances,
			Map<CyNode, List<CyNode>> predMap) {
		final DoubleMatrix1D spCount = new DenseDoubleMatrix1D(nodeCount);
		// initialize for source
		spCount.set(node2index.get(sourceCyNode).intValue(), 1.0);
		// sort nodes in non-increasing order of distances
		final Set<CyNode> nodes = node2index.keySet();
		final List<CyNodeDoublePair> dCyNodes = new ArrayList<CyNodeDoublePair>(nodeCount);
		for (CyNode node : nodes) {
			dCyNodes
					.add(new CyNodeDoublePair(node,
							new Double(distances.get(node2index.get(node).intValue()))));
		}
		Collections.sort(dCyNodes, new DistanceComparator());
		// accumulate shortest path count
		for (CyNodeDoublePair dn : dCyNodes) {
			final CyNode currentCyNode = dn.node();
			if (currentCyNode == sourceCyNode) {
				continue;
			}
			final List<CyNode> predecessors = predMap.get(currentCyNode);
			if (predecessors != null) {
				for (CyNode predCyNode : predecessors) {
					double count = spCount.get(node2index.get(currentCyNode).intValue())
							+ spCount.get(node2index.get(predCyNode).intValue());
					spCount.set(node2index.get(currentCyNode).intValue(), count);
				}
			}
		}
		return spCount;
	}

	/**
	 * Comparator of two {@link CyCyNodeDoublePair}s.
	 */
	public class DistanceComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			CyNodeDoublePair dn1 = (CyNodeDoublePair) o1;
			CyNodeDoublePair dn2 = (CyNodeDoublePair) o2;

			return (dn1.compareTo(dn2));
		}
	}

	/**
	 * Get all neighbors (directed and undirected) of a node.
	 * 
	 * @param aNetwork 
	 *            Network containing <code>aCyNode</code> and its neighbors.
	 * @param aCyNode 
	 *            CyNode whose neighbors have to be returned.
	 * @return Set of neighboring nodes of <code>aCyNode</code>.
	 */
	private Set<Integer> getNeighbors(DoubleMatrix1D nodeWeights) {
		Set<Integer> neighbors = new HashSet<Integer>();
		for (int i = 0; i < nodeCount; i++) {
			if (nodeWeights.get(i) != 0.0) {
				neighbors.add(new Integer(i));
			}
		}		
		return neighbors;
	}

	/**
	 * Create an index to node map.
	 */
	private void createIndex2CyNode(){
		final Iterator<Map.Entry<CyNode, Integer>> it = node2index.entrySet().iterator();
		while(it.hasNext()) {
			final Map.Entry<CyNode, Integer> entry = it.next();
			index2node.put(entry.getValue(), entry.getKey());
		}
	}
	
	/**
	 * Number of nodes in he network.
	 */
	private int nodeCount;

	/**
	 * Map of nodes in the network and the indices of the adjacency matrix.
	 */
	private Map<CyNode, Integer> node2index;

	/**
	 * Map of nodes in the network and the indices of the adjacency matrix.
	 */
	private Map<Integer, CyNode> index2node;
}
