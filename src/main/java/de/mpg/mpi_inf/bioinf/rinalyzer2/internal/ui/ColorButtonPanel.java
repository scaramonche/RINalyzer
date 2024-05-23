package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

/**
 * Abstract class for a panel with color chooser buttons.
 * 
 * @author Nadezhda Doncheva
 */
public abstract class ColorButtonPanel extends JPanel {

	/**
	 * Initializes a new instance of <code>ColorButtonPanel</code>.
	 * 
	 * @param aAttributes
	 *            Names of the labels of the color choosers.
	 * @param aColorMap
	 *            Map with attribute2color mapping.
	 */
	public ColorButtonPanel(Set<String> aAttributes, Map<String, Color> aColorMap) {
		super();
		colorListeners = new ArrayList<ColorChangeListener>(4);
		attributes = aAttributes;
		colorMap = aColorMap;
	}

	/**
	 * Add a listener for color changes of any of the attributes.
	 * 
	 * @param listener
	 *            Color changes listener.
	 */
	public void addColorChangeListener(ColorChangeListener listener) {
		if (listener != null && !colorListeners.contains(listener)) {
			colorListeners.add(listener);
		}
	}

	/**
	 * Remove a color change listener from the list of colorListeners.
	 * 
	 * @param listener
	 *            Color change listener to be removed.
	 */
	public void removeColorChangeListener(ColorChangeListener listener) {
		colorListeners.remove(listener);
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 8961561167734010717L;

	/**
	 * List of {@link ColorChangeListener}s.
	 */
	protected List<ColorChangeListener> colorListeners;

	/**
	 * Names of the labels of the different color choosers.
	 */
	protected Set<String> attributes;

	/**
	 * Attribute name to color mapping.
	 */
	protected Map<String, Color> colorMap;
}
