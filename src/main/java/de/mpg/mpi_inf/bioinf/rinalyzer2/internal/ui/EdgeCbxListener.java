package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

/**
 * Interface for a check box listener.
 * 
 * @author Nadezhda Doncheva
 */
public interface EdgeCbxListener {

	/**
	 * Invoke when the value of a "show edge type" check box changes.
	 * 
	 * @param attr
	 *            Edge type to be shown/hidden.
	 * @param newValue
	 *            New value of the show/hide edge type flag.
	 */
	public void cbxValueChanged(String attr, Boolean newValue);
}
