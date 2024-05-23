package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JColorChooser;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Implements a simple version of the {@link ColorButtonPanel} with labels and
 * color choosers and a listener for color button changes.
 * 
 * @author Nadezhda Doncheva
 */
public class SimpleColorButtonPanel extends ColorButtonPanel implements ActionListener {

	/**
	 * Initializes a new instance of <code>SimpleColorButtonPanel</code>.
	 * 
	 * @param aAttributes
	 *            Set of attributes to be added to the panel with a color
	 *            chooser.
	 * @param aColorMap
	 *            Map of colors for some of the attributes in
	 *            <code>aAttributes</code>.
	 */
	public SimpleColorButtonPanel(Set<String> aAttributes, Map<String, Color> aColorMap) {
		super(aAttributes, aColorMap);
		initPanel();
	}

	/**
	 * Notify all color change listeners in this class when a new color has been
	 * chosen.
	 */
	public void actionPerformed(ActionEvent e) {
		final JButton btn = (JButton) e.getSource();
		final Color newColor = JColorChooser.showDialog(null, Messages.DT_CHOOSECOLOR,
				btn.getForeground());
		if (newColor != null) {
			btn.setForeground(newColor);
			for (final ColorChangeListener listener : colorListeners) {
				listener.colorChanged(e.getActionCommand(), newColor);
			}
		}
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
			this.add(UtilsUI.createLabel(entry, Messages.getFullName(entry)), gbc);
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
	private static final long serialVersionUID = -8483001146406152929L;
}
