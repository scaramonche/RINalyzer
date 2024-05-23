package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Class providing methods for computation of the connected components among a set of nodes and
 * storing these. It also has a method that returns all pairs of nodes that belong to different
 * connected components.
 * 
 * @author Nadezhda Doncheva
 */
public class ConnComp {

	/**
	 * Initialize an instance of <code>ConnComp</code>. Compute the connected components in the set
	 * of nodes <code>aSelectedCyNodes</code> using the underlying network <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Network containing the set of selected nodes.
	 * @param aCyNodesSet
	 *            Set of selected nodes from the network <code>aNetwork</code>.
	 */
	public ConnComp(CyNetwork aNetwork, Set<CyNode> aCyNodesSet) {
		connCompRoots = new HashSet<CyNode>();
		connComps = new HashMap<CyNode, Set<CyNode>>();
		findConnComp(aNetwork, aCyNodesSet);
	}

	/**
	 * Get the number of connected components.
	 * 
	 * @return Number of connected components.
	 */
	public int getConnCompNumber() {
		return connCompRoots.size();
	}

	/**
	 * Return the biggest connected component.
	 * 
	 * @return Set of nodes contained in the biggest connected component.
	 */
	public Set<CyNode> getBiggestConnComp() {
		return connComps.get(biggestConnCompRoot);
	}

	/**
	 * Get number of pairs of nodes in different connected components. (sum_ij n_i * n_j for i!=j,
	 * where i, j are indices of connected components and n_i is the size of the connected component
	 * with index i).
	 * 
	 * @return Number of pairs.
	 */
	public int getPairsNumber() {
		int pairs = 0;
		for (final CyNode root1 : connCompRoots) {
			for (final CyNode root2 : connCompRoots) {
				if (!root1.equals(root2)) {
					pairs += connComps.get(root1).size() * connComps.get(root2).size();
				}
			}
		}
		return pairs;
	}

	/**
	 * Get a set of node pairs, such that the nodes belong to different connected components, e.g. a
	 * pair (a, b) is contained in the set only if a in A, b in B, where A and B are different
	 * connected components.
	 * 
	 * @return Set of node pairs.
	 */
	public Set<IntPair> getPairsMap(Map<CyNode, Integer> node2index) {
		final Set<IntPair> pairs = new HashSet<IntPair>();
		if (connCompRoots.size() > 1) {
			// retrieve connected components
			for (final CyNode rootCyNodeFirst : connCompRoots) {
				for (final CyNode rootCyNodeSecond : connCompRoots) {
					if (!rootCyNodeFirst.equals(rootCyNodeSecond)) {
						final Set<CyNode> nodeSetFirst = connComps.get(rootCyNodeFirst);
						final Set<CyNode> nodeSetSecond = connComps.get(rootCyNodeSecond);
						// retrieve pairs
						for (final CyNode first : nodeSetFirst) {
							for (final CyNode second : nodeSetSecond) {
								if (!first.equals(second)) {
									pairs.add(new IntPair(node2index.get(first).intValue(),
											node2index.get(second).intValue()));
								}
							}
						}
					}
				}
			}
		}
		return pairs;
	}

	/**
	 * Compute the connected components of the set of nodes <code>selectedCyNodes</code> from the
	 * network <code>aNetwork</code>. Store the root nodes of each connected component in
	 * <code>connCompRoots</code>, and a map <code>connCompCyNodes</code> with key = root node and
	 * value = the set of nodes in the respective connected component. *
	 * 
	 * @param aNetwork
	 *            Network containing the set of selected nodes.
	 * @param aSelectedCyNodes
	 *            Set of selected nodes from the network <code>aNetwork</code>.
	 */
	private void findConnComp(CyNetwork aNetwork, Set<CyNode> nodesSet) {
		Set<CyNode> traversed = new HashSet<CyNode>(nodesSet.size());
		int sizeBiggestConnComp = 0;
		for (final CyNode node : nodesSet) {
			if (!traversed.contains(node)) {
				connCompRoots.add(node);
				final Set<CyNode> connCompCyNodes = new HashSet<CyNode>();
				connCompCyNodes.add(node);
				final LinkedList<CyNode> toTraverse = new LinkedList<CyNode>();
				traversed.add(node);
				toTraverse.add(node);
				while (!toTraverse.isEmpty()) {
					final CyNode currentCyNode = toTraverse.removeFirst();
					final List<CyNode> neighbors = aNetwork.getNeighborList(currentCyNode, Type.ANY);
					for (final CyNode nb : neighbors) {
						if (nodesSet.contains(nb) && !traversed.contains(nb)) {
							connCompCyNodes.add(nb);
							toTraverse.add(nb);
							traversed.add(nb);
						}
					}
				}
				connComps.put(node, connCompCyNodes);
				if (connCompCyNodes.size() > sizeBiggestConnComp) {
					sizeBiggestConnComp = connCompCyNodes.size();
					biggestConnCompRoot = node;
				}
			}
		}
	}

	/**
	 * Set of nodes representing the different connected components.
	 */
	private Set<CyNode> connCompRoots;

	/**
	 * Map of connected components (key = root node and value = the set of nodes in the respective
	 * connected component inclusive the root node.
	 */
	private Map<CyNode, Set<CyNode>> connComps;

	/**
	 * Root node of biggest connected component.
	 */
	private CyNode biggestConnCompRoot;
}
