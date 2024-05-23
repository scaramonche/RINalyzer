package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import org.cytoscape.model.CyNode;

/**
 * Class for a pair of Cytoscape node and a double value. It provides comparison methods for nodes, based on
 * the double value.
 * 
 * @author Nadezhda Doncheva
 */

public class CyNodeDoublePair {

	/**
	 * Initializes a new instance of <code>CyCyNodeDoublePair</code>.
	 * 
	 * @param aCyNode
	 *            Cytoscape node.
	 * @param aDoubleValue
	 *            Double value assosicated with <code>aCyNode</code>.
	 */
	public CyNodeDoublePair(CyNode aCyNode, Double aDoubleValue) {
		node = aCyNode;
		value = aDoubleValue;
	}

	/**
	 * Compares two {@link CyCyNodeDoublePair}s based on their double values.
	 * 
	 * @param o
	 *            Object to compare <code>dijkstraCyNode</code> to.
	 * @return the value 0 if <code>o</code>'s <code>doubleValue</code> is numerically equal to this
	 *         node's <code>doubleValue</code>; a value less than 0 if it is numerically less than
	 *         <code>doubleValue</code>; and a value greater than 0 if it is numerically greater.
	 */
	public int compareTo(Object o) {
		CyNodeDoublePair dn = (CyNodeDoublePair) o;

		return value.compareTo(dn.value());
	}

	/**
	 * Checks if two {@link CyCyNodeDoublePair}s are equal. They are equal if their Cytoscape nodes are equal
	 * and have equal distances.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof CyNodeDoublePair)) {
			return false;
		}
		CyNodeDoublePair pair = (CyNodeDoublePair) o;
		return pair.equals(pair.node()) && value.equals(pair.value());
	}

	/**
	 * Get Cytoscape node.
	 * 
	 * @return Cytoscape node.
	 */
	public CyNode node() {
		return node;
	}

	/**
	 * Get double value.
	 * 
	 * @return Double value.
	 */
	public Double value() {
		return value;
	}

	/**
	 * Cytoscape node.
	 */
	private CyNode node;

	/**
	 * Double value.
	 */
	private Double value;
}
