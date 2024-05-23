package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Dialog getting the user input for the construction of a new subnetwork from the currently
 * selected network. These input features are the chain identifiers and the name of the new network.
 * The user can select whether to create a network from the selected chains or only from the
 * interface between them.
 * 
 * @author Nadezhda Doncheva
 */
public class SubnetworkGenerationDialog extends JDialog implements ActionListener {

	/**
	 * Initializes a new instance of <code>SubNetworkGenerationDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aChainSet
	 *            Set with all chain identifiers found in this network.
	 * @param aNetTitle
	 *            Identifier of the parent network.
	 */
	public SubnetworkGenerationDialog(Frame aOwner, Set<String> aChainSet, String aNetTitle) {
		super(aOwner, Messages.DT_SUBNETWORK, true);
		owner = aOwner;
		chainSet = aChainSet;
		selectedChains = new ArrayList<String>();
		canceled = true;
		netTitle = aNetTitle + "_part";
		chainNet = true;
		addEdges = false;
		init();
		setLocationRelativeTo(aOwner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		final String command = e.getActionCommand();
		if (chainSet.contains(command)) {
			final JCheckBox cbx = (JCheckBox) src;
			if (cbx.isSelected()) {
				selectedChains.add(command);
			} else if (selectedChains.contains(command)) {
				selectedChains.remove(command);
			}
			if (selectedChains.size() > 0) {
				btnOK.setEnabled(true);
			} else {
				btnOK.setEnabled(false);
			}
		} else if (src == cbxAddEdges) {
			addEdges = cbxAddEdges.isSelected();
		} else if (src == btnOK) {
			final String text = txtfTitle.getText();
			if (text == null || text.trim().length() == 0) {
				JOptionPane.showMessageDialog(owner, Messages.SM_ENTERNAME, Messages.DT_ERROR,
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// else if (!Utils.checkNetworkName(text)) {
			// return;
			// }
			netTitle = text.trim();
			chainNet = rdbChain.isSelected();
			addEdges = cbxAddEdges.isSelected();
			canceled = false;
			setVisible(false);
			dispose();
		} else if (src == btnCancel) {
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Get the set of chain identifiers to include in the new network.
	 * 
	 * @return Set of chain identifiers.
	 */
	public List<String> getSelectedChains() {
		return selectedChains;
	}

	/**
	 * Get the name of the new network.
	 * 
	 * @return Name of the new network.
	 */
	public String getNetworkTitle() {
		return netTitle;
	}

	/**
	 * Check if chain or interface should be extracted.
	 * 
	 * @return <code>true</code> if extract chain(s).
	 */
	public boolean isChainNetwork() {
		return chainNet;
	}

	/**
	 * Check whether to add edges within the interface.
	 * 
	 * @return <code>true</code> if edges should be added.
	 */
	public boolean addEdges() {
		return addEdges;
	}

	/**
	 * Check if the dialog has been canceled by the user.
	 * 
	 * @return <code>true</code> if dialog has been canceled by the user, and <code>false</code>
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

		final JPanel panTitle = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panTitle.add(UtilsUI.createLabel(Messages.DI_NEWCHAINNET, null));
		contentPane.add(panTitle, BorderLayout.NORTH);

		final JComponent panOptions = new Box(BoxLayout.PAGE_AXIS);
		// add choice
		final JPanel panChoice = new JPanel(new GridLayout(1, 2, BS, BS));
		panChoice.setBorder(new TitledBorder(null, Messages.DI_CREATENETWORKFOR));
		rdbChain = new JRadioButton(Messages.DI_CHAINS);
		rdbChain.setSelected(true);
		rdbChain.addActionListener(this);
		rdbInterface = new JRadioButton(Messages.DI_INTERFACES);
		// rdbInterface.setHorizontalAlignment(JRadioButton.TRAILING);
		// rdbInterface.setHorizontalTextPosition(JRadioButton.TRAILING);
		// rdbInterface.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		rdbInterface.addActionListener(this);
		ButtonGroup group = new ButtonGroup();
		group.add(rdbChain);
		group.add(rdbInterface);
		panChoice.add(rdbChain);
		panChoice.add(rdbInterface);
		panOptions.add(panChoice);

		// add chains selection panel
		final JPanel panChains = new JPanel();
		panChains.setBorder(new TitledBorder(null, Messages.DI_CHAINSELECT));
		GridBagLayout gbl = new GridBagLayout();
		panChains.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(BS / 2, BS / 2, BS / 2, BS / 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		// add chain id check boxes
		int i = 0;
		for (final String chain : chainSet) {
			panChains.add(UtilsUI.createCheckBox(chain, chain, false, this), gbc);
			i++;
			if (i % 4 == 0) {
				gbc.gridy++;
				gbc.gridx = 0;
			} else {
				gbc.gridx++;
			}
		}
		panOptions.add(panChains);

		// add text field for network title
		final JPanel panGeneral = new JPanel(new BorderLayout(BS, BS));
		panGeneral.setBorder(new TitledBorder(null, Messages.DI_GENERAL));
		final JPanel panNetTitle = new JPanel(new GridLayout(1, 2, BS, BS));
		panNetTitle.add(UtilsUI.createLabel(Messages.DI_ENTERNAME, null));
		txtfTitle = new JTextField(netTitle);
		panNetTitle.add(txtfTitle);
		panGeneral.add(panNetTitle, BorderLayout.CENTER);
		cbxAddEdges = new JCheckBox(Messages.DI_ADDINTERFACEEDGES);
		panGeneral.add(cbxAddEdges, BorderLayout.SOUTH);
		panOptions.add(panGeneral);
		contentPane.add(panOptions, BorderLayout.CENTER);

		// add buttons
		final JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel panButtons = new JPanel(new GridLayout(1, 2, BS, BS));
		btnOK = UtilsUI.createButton(Messages.DI_OK, null, null, this);
		btnOK.setEnabled(false);
		panButtons.add(btnOK);
		btnCancel = UtilsUI.createButton(Messages.DI_CANCEL, null, null, this);
		panButtons.add(btnCancel);
		panBottom.add(panButtons);
		contentPane.add(panBottom, BorderLayout.SOUTH);

		setContentPane(contentPane);
		pack();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6855974684005114921L;

	/**
	 * Flag indicating if the dialog has been canceled by the user.
	 */
	private boolean canceled;

	/**
	 * Set of all chain identifiers contained in the current network.
	 */
	private Set<String> chainSet;

	/**
	 * Set of chain identifiers selected by the user, i.e. to be included in the new network.
	 */
	private List<String> selectedChains;

	/**
	 * Name of the new network.
	 */
	private String netTitle;

	private boolean chainNet;

	private boolean addEdges;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;OK&quot; button.
	 */
	private JButton btnOK;

	/**
	 * Text field for the new network name.
	 */
	private JTextField txtfTitle;

	private JRadioButton rdbChain;

	private JRadioButton rdbInterface;

	private JCheckBox cbxAddEdges;

	private Frame owner;
}
