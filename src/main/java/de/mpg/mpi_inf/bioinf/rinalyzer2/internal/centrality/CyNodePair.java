package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import org.cytoscape.model.CyNode;


/**
 * Ordered pair of two Cytoscape nodes.
 * 
 * @author Nadezhda Doncheva
 */
public class CyNodePair {

	/**
	 * Initialize a new empty instance of <code>CyNodePair</code>.
	 */
	public CyNodePair() {
		first = null;
		second = null;
	}

	/**
	 * Initialize a new instance of <code>CyNodePair</code> with two nodes.
	 * 
	 * @param aFirst
	 *            First node of the pair.
	 * @param aSecond
	 *            Second node of the pair.
	 */
	public CyNodePair(CyNode aFirst, CyNode aSecond) {
		first = aFirst;
		second = aSecond;
	}

	/**
	 * Get the first node.
	 * 
	 * @return First node.
	 */
	public CyNode getFirst() {
		return first;
	}

	/**
	 * Get the second node.
	 * 
	 * @return Second node.
	 */
	public CyNode getSecond() {
		return second;
	}

	/**
	 * Set the first node.
	 * 
	 * @param aFirst
	 *            CyNode to be set as first node of the pair.
	 */
	public void setFirst(CyNode aFirst) {
		first = aFirst;
	}

	/**
	 * Set the second node.
	 * 
	 * @param aFirst
	 *            CyNode to be set as first node of the pair.
	 */
	public void setSecond(CyNode aSecond) {
		second = aSecond;
	}

	/**
	 * First node of the pair.
	 */
	private CyNode first;

	/**
	 * Second node of the pair.
	 */
	private CyNode second;
}
