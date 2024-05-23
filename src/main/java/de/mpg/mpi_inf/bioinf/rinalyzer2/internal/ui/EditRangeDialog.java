package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Dialog for editing the range of the centrality measure's slider.
 * 
 * @author Nadezhda Doncheva
 */
public class EditRangeDialog extends JDialog implements ActionListener {

	/**
	 * Initialize a new instance of <code>EditRangeDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aName
	 *            Name of the dialog, i.e. the centrality measure for which the range has to be edited.
	 * @param aBoundVector
	 *            Vector with boundary values (low, high, min, max). Note that this is a reference and the
	 *            values in the vector are changed by this class.
	 */
	public EditRangeDialog(Frame aOwner, String aName, Vector<String> aBoundVector) {
		super(aOwner, Messages.DT_EDITRANGE, true);
		boundVector = aBoundVector;
		canceled = true;
		name = aName;
		init();
		setResizable(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnOK) {
			double min = new Double(boundVector.elementAt(2)).doubleValue();
			double max = new Double(boundVector.elementAt(3)).doubleValue();
			final String lowValue = txtfLowBound.getText();
			if (lowValue != null && lowValue.length() > 0) {
				final double lowBound = new Double(lowValue).doubleValue();
				if (lowBound >= min && lowBound <= max) {
					boundVector.setElementAt(lowValue, 0);
				} else {
					boundVector.setElementAt(String.valueOf(min), 0);
				}
			}
			final String highValue = txtfHighBound.getText();
			if (highValue != null && highValue.length() > 0) {
				final double highBound = new Double(highValue).doubleValue();
				if (highBound >= min && highBound <= max) {
					boundVector.setElementAt(highValue, 1);
				} else {
					boundVector.setElementAt(String.valueOf(max), 1);
				}
			}
			canceled = false;
			setVisible(false);
			dispose();
		} else if (src == btnCancel) {
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Check if the dialog has been canceled.
	 * 
	 * @return <code>true</code> if the dialog has been canceled by the user and <code>false</code>
	 *         otherwise.
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Initialize dialog.
	 */
	private void init() {
		final int BS = UtilsUI.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		contentPane.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));

		final JPanel panTop = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panTop.add(UtilsUI.createLabel(Messages.DI_SELRANGE + name, null));
		contentPane.add(panTop, BorderLayout.NORTH);

		final JPanel panRange = new JPanel(new GridLayout(2, 2, BS, BS));
		panRange.add(UtilsUI.createLabel(Messages.DI_BOUND_LOW, null));
		txtfLowBound = new JTextField(boundVector.get(0));
		panRange.add(txtfLowBound);
		panRange.add(UtilsUI.createLabel(Messages.DI_BOUND_HIGH, null));
		txtfHighBound = new JTextField(boundVector.get(1));
		panRange.add(txtfHighBound);
		contentPane.add(panRange, BorderLayout.CENTER);

		final JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel panButtons = new JPanel(new GridLayout(1, 2, BS, BS));
		btnOK = UtilsUI.createButton(Messages.DI_OK, null, null, this);
		btnCancel = UtilsUI.createButton(Messages.DI_CANCEL, null, null, this);
		panButtons.add(btnOK);
		panButtons.add(btnCancel);
		panBottom.add(panButtons);
		contentPane.add(panBottom, BorderLayout.SOUTH);

		setContentPane(contentPane);
		pack();
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1193473041205659599L;

	/**
	 * Vector with boundary values: low, high, min, max.
	 */
	private Vector<String> boundVector;

	/**
	 * Flag indicating if the dialog has been canceled.
	 */
	private boolean canceled;

	/**
	 * Centrality measure name.
	 */
	private String name;
	
	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * Text field for low bound.
	 */
	private JTextField txtfLowBound;

	/**
	 * Text field for high bound.
	 */
	private JTextField txtfHighBound;
}
