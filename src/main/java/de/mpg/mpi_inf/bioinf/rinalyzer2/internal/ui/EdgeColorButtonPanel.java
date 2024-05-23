package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Extends the {@link ColorButtonPanel} to create not only labels with color
 * choosers, but also a check box for each attribute.
 * 
 * @author Nadezhda Doncheva
 */
public class EdgeColorButtonPanel extends ColorButtonPanel implements ActionListener {

	/**
	 * Initialize a new instance of <code>EdgeColorButtonPanel</code>.
	 * 
	 * @param aEdgeTypes
	 *            Set of edge types to be added to the panel with color choosers
	 *            and check boxes.
	 * @param aColorMap
	 *            map of different attributes (incl. edge types) and colors.
	 * @param aShownEdges
	 *            Map of edge types and their visibility flags.
	 */
	public EdgeColorButtonPanel(Set<String> aEdgeTypes, Map<String, Color> aColorMap,
			Map<String, Boolean> aShownEdges) {
		super(aEdgeTypes, aColorMap);
		shownEdges = aShownEdges;
		edgeCbxListeners = new ArrayList<EdgeCbxListener>();
		initPanel();
	}

	/**
	 * Notify all in this instance registered listeners for color changes and
	 * check box state changes.
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src instanceof JButton) {
			final JButton btn = (JButton) src;
			final Color newColor = JColorChooser.showDialog(null, Messages.DT_CHOOSECOLOR,
					btn.getForeground());
			if (newColor != null) {
				btn.setForeground(newColor);
				for (final ColorChangeListener listener : colorListeners) {
					listener.colorChanged(e.getActionCommand(), newColor);
				}
			}
		} else if (src instanceof JCheckBox) {
			final JCheckBox cbx = (JCheckBox) src;
			for (final EdgeCbxListener listener : edgeCbxListeners) {
				listener.cbxValueChanged(cbx.getActionCommand(), new Boolean(cbx.isSelected()));
			}
		}
	}

	/**
	 * Add a listener for color changes of any of the attributes.
	 * 
	 * @param listener
	 *            Color changes listener.
	 */
	public void addCheckBoxListener(EdgeCbxListener listener) {
		if (listener != null && !edgeCbxListeners.contains(listener)) {
			edgeCbxListeners.add(listener);
		}
	}

	/**
	 * Remove a color change listener from the list of colorListeners.
	 * 
	 * @param listener
	 *            Color change listener to be removed.
	 */
	public void removeEdgeCbxListener(EdgeCbxListener listener) {
		edgeCbxListeners.remove(listener);
	}

	/**
	 * Initialize panel.
	 */
	private void initPanel() {
		final int BS = UtilsUI.BORDER_SIZE;
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(BS / 2, BS / 2, BS / 2, BS / 2);
		gbc.gridy = 0;
		gbc.weightx = 0;
		// Panel for color buttons
		final GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		for (final String entry : attributes) {
			gbc.gridx = 0;
			gbc.weightx = 0;
			gbc.anchor = GridBagConstraints.LINE_START;
			final JCheckBox cbx = UtilsUI.createCheckBox(entry, entry, shownEdges.get(entry)
					.booleanValue(), this);
			cbx.setToolTipText(Messages.getFullName(entry));
			this.add(cbx, gbc);
			gbc.gridx++;
			gbc.weightx = 1;
			gbc.anchor = GridBagConstraints.LINE_END;
			final JButton colorButton = UtilsUI.createButton(Messages.DI_COLORBUTTON, null, entry,
					this);
			colorButton.setForeground(colorMap.get(entry));
			this.add(colorButton, gbc);
			gbc.gridy++;
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1964562519822469854L;

	/**
	 * Map of attributes and their visibility flags that definde the selected
	 * state of the check boxes.
	 */
	Map<String, Boolean> shownEdges;

	/**
	 * List of listeners for the attribute check boxes.
	 */
	List<EdgeCbxListener> edgeCbxListeners;
}
