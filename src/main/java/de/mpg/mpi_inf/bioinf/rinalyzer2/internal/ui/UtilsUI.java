package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Utility class providing helper methods for dialog manipulation.
 * 
 * @author Nadezhda Doncheva
 * 
 */
public class UtilsUI {

	/**
	 * Add menu item <code>aItem</code> to the menu <code>aMenu</code>. Set the menu item's action
	 * listener and action command.
	 * 
	 * @param aItem
	 *            New menu item to be added.
	 * @param aCommand
	 *            Action command of the item.
	 * @param aMenu
	 *            Menu to add the item to.
	 * @param aListener
	 *            Menu clicked action listener.
	 */
	public static void addMenuItem(JMenuItem aItem, String aCommand, JMenu aMenu, ActionListener aListener) {
		aItem.setActionCommand(aCommand);
		aItem.addActionListener(aListener);
		aMenu.add(aItem);
	}

	/**
	 * Add menu item <code>aItem</code> to the menu <code>aMenu</code>. Set the menu item's action
	 * listener and action command.
	 * 
	 * @param aItem
	 *            New menu item to be added.
	 * @param aCommand
	 *            Action command of the item.
	 * @param aMenu
	 *            Menu to add the item to.
	 * @param aListener
	 *            Menu clicked action listener.
	 */
	public static void addPopupMenuItem(JMenuItem aItem, String aCommand, JPopupMenu aMenu,
			ActionListener aListener) {
		aItem.setActionCommand(aCommand);
		aItem.addActionListener(aListener);
		aMenu.add(aItem);
	}

	/**
	 * Make all the components in the array to have the same width = the width of the widest component among
	 * them.
	 * 
	 * @param components
	 *            Array with swing components.
	 */
	public static void adjustWidth(JComponent[] components) {
		Dimension dim = components[0].getPreferredSize();
		int width = dim.width;
		for (int i = 1; i < components.length; i++) {
			dim = components[i].getPreferredSize();
			if (dim.width > width) {
				width = dim.width;
			}
		}
		for (final JComponent cbx : components) {
			dim = cbx.getPreferredSize();
			dim.width = width;
			cbx.setPreferredSize(dim);
		}
	}

	/**
	 * Creates a new button.
	 * 
	 * @param aText
	 *            Text of the button.
	 * @param aTooltip
	 *            Tooltip text for the button. Set this to <code>null</code> if no tooltip is to be
	 *            displayed.
	 * @param actionCommand
	 *            Action command of the button.
	 * @param aListener
	 *            Button click's listener.
	 * @return Newly created instance of <code>JButton</code>.
	 */
	public static JButton createButton(String aText, String aTooltip, String actionCommand,
			ActionListener aListener) {
		JButton button = new JButton(aText);
		button.setToolTipText(aTooltip);
		button.setActionCommand(actionCommand);
		button.addActionListener(aListener);
		button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getHeight()));
		return button;
	}

	/**
	 * Creates a new check box.
	 * 
	 * @param aText
	 *            Text of the check box.
	 * @param actionCommand
	 *            Action command of the check box.
	 * @param aSelected
	 *            Flag for the selection state.
	 * @param aListener
	 *            Check box click's listener.
	 * @return Newly created instance of <code>JCheckBox</code>.
	 */
	public static JCheckBox createCheckBox(String aText, String actionCommand, boolean aSelected,
			ActionListener aListener) {
		JCheckBox cbx = new JCheckBox(aText);
		cbx.setActionCommand(actionCommand);
		cbx.setSelected(aSelected);
		cbx.addActionListener(aListener);
		cbx.setMaximumSize(new Dimension(Short.MAX_VALUE, cbx.getHeight()));
		return cbx;
	}

	/**
	 * Creates a new text label.
	 * 
	 * @param aText
	 *            Text of the label.
	 * @param aToolTip
	 *            Tooltip text of the label. Set this to <code>null</code> if no tooltip is to be displayed.
	 * @return Newly created instance of <code>JLabel</code>.
	 */
	public static JLabel createLabel(String aText, String aToolTip) {
		JLabel l = new JLabel(aText);
		l.setToolTipText(aToolTip);
		return l;
	}

	/**
	 * Get a decimal representation of a number with at most six digits after the dot.
	 * 
	 * @param aNumber
	 *            Number to be formatted.
	 * @return Formatted string representation of the number.
	 */
	public static String getSixDigits(Object aNumber, RoundingMode rm) {
		DecimalFormat decFormat = new DecimalFormat("#,##0.00####");
		decFormat.setRoundingMode(rm);
		if (decFormat.format(aNumber).equals("0.000000")) {
			decFormat = new DecimalFormat("0.0#E0");
		}
		return decFormat.format(aNumber);
	}

	/**
	 * Get a decimal representation of a number with at most four digits after the dot.
	 * 
	 * @param aNumber
	 *            Number to be formatted.
	 * @return Formatted string representation of the number.
	 */
	public static String getThreeDigits(Object aNumber) {
		DecimalFormat decFormat = new DecimalFormat("#,##0.00#");
		if (decFormat.format(aNumber).equals("0.000")) {
			decFormat = new DecimalFormat("0.0#E0");
		}
		return decFormat.format(aNumber);
	}

	/**
	 * Get a decimal representation of a number with at most four digits after the dot.
	 * 
	 * @param aNumber
	 *            Number to be formatted.
	 * @return Formatted string representation of the number.
	 */
	public static String getTwoDigits(Object aNumber) {
		DecimalFormat decFormat = new DecimalFormat("#,##0.00");
		if (decFormat.format(aNumber).equals("0.00")) {
			decFormat = new DecimalFormat("0.0#E0");
		}
		return decFormat.format(aNumber);
	}

	/**
	 * Limit components maximum height to the preferred height.
	 * 
	 * @param aComponent
	 *            A swing component.
	 */
	public static void limitHeight(JComponent aComponent) {
		final Dimension maxSize = aComponent.getMaximumSize();
		maxSize.height = aComponent.getPreferredSize().height;
		aComponent.setMaximumSize(maxSize);
	}

	/**
	 * Default border size.
	 */
	public static final int BORDER_SIZE = 8;
}
