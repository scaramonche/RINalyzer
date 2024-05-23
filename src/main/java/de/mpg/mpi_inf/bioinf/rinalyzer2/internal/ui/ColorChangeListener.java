package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.Color;

/**
 * Interface for a color change listener.
 * 
 * @author Nadezhda Doncheva
 */
public interface ColorChangeListener {

	/**
	 * Invoked when the color of an attribute has changed.
	 * 
	 * @param attr
	 *            Attribute whose color has changed.
	 * @param newColor
	 *            New color for the attribute <code>attr</code>.
	 */
	public void colorChanged(String attr, Color newColor);

}
