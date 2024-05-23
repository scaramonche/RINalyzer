package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

/**
 * Base class for all classes performing centrality computation.
 * 
 * @author Nadezhda Doncheva
 */
public abstract class Centrality {

	/**
	 * Initializes a new instance of the <code>Centrality</code> with inverse Laplacian.
	 * 
	 * @param aNetwork
	 *          Network to be analyzed.
	 * @param aCyNode2Index
	 *          Mapping of nodes and node indices.
	 * @param aSet
	 *          Set of selected nodes.
	 * @param aInvLapl
	 *          Inverse Laplacian
	 */
	protected Centrality(Map<CyNode, Integer> aCyNode2Index, Set<CyNode> aSet, CyNetwork aNetwork,
			DoubleMatrix2D aInvLapl) {
		nodeCount = aCyNode2Index.size();
		node2index = aCyNode2Index;
		selectedSet = aSet;
		network = aNetwork;
		setSize = aSet.size();
		invLaplacian = aInvLapl;
	}

	/**
	 * Initializes a new instance of the <code>Centrality</code> without inverse Laplacian.
	 * 
	 * @param aNetwork
	 *          Network to be analyzed.
	 * @param aCyNode2Index
	 *          Mapping of nodes and node indices.
	 * @param aSet
	 *          Set of selected nodes.
	 * @param aInvLapl
	 *          Inverse Laplacian
	 */
	protected Centrality(Map<CyNode, Integer> aCyNode2Index, Set<CyNode> aSet, CyNetwork aNetwork) {
		nodeCount = aCyNode2Index.size();
		node2index = aCyNode2Index;
		selectedSet = aSet;
		network = aNetwork;
		setSize = aSet.size();
		invLaplacian = null;
	}

	/**
	 * Inverse laplacian.
	 */
	protected DoubleMatrix2D invLaplacian;

	/**
	 * Number of nodes in the network.
	 */
	protected int nodeCount;

	protected CyNetwork network;

	/**
	 * Size of the set of selected nodes.
	 */
	protected int setSize;

	/**
	 * Set of selected nodes.
	 */

	protected Set<CyNode> selectedSet;

	/**
	 * Mapping of nodes and node indices.
	 */
	protected final Map<CyNode, Integer> node2index;
}
