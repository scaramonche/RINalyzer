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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ConnComp;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Dialog for defining the analysis settings. It contains the information for
 * different user-definable analysis features.
 * 
 * @author Nadezhda Doncheva
 */
// TODO: [Improve][Analysis] "Handle negative weights -> Shift"
// TODO: [Improve][Analysis] Add option "Load last options"
public class AnalysisDialog extends JDialog implements ActionListener {

	/**
	 * Initializes a new instance of <code>AnalysisDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aNetwork
	 *            Target network.
	 * @param aSelectedSet
	 *            Set of selected nodes in the network.
	 */
	public AnalysisDialog(Frame aOwner, CyServiceRegistrar bc, CyNetwork aNetwork,
			Set<CyNode> aSelectedSet, boolean aCompOnlySP) {
		super(aOwner, Messages.DT_SETTINGS, true);
		context = bc;
		network = aNetwork;
		selectedSet = aSelectedSet;
		compOnlySP = aCompOnlySP;
		weightAttr = "";
		edgeType = "";
		negWeight = "";
		simToDist = "";
		hasConnComp = false;
		useConnComp = false;
		measures = new boolean[] { true, false, false };
		degreeCutoff = DEFAULTDEGREE;
		defWeight = DEFAULTWEIGHT;
		canceled = true;
		// look for different connected components among the set of selected
		// nodes.
		connComps = new ConnComp(network, selectedSet);
		if (connComps.getConnCompNumber() > 1) {
			hasConnComp = true;
		}
		List<String> attrList = CyUtils.getNumericAttributes(network, CyEdge.class);
		attrList.add(0, "");
		attrs = attrList.toArray(new String[attrList.size()]);
		// initialize
		init();
		setResizable(false);
		setLocationRelativeTo(aOwner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnAnalyze) {
			if (cbxAttr.getSelectedItem() != null) {
				weightAttr = (String) cbxAttr.getSelectedItem();
			}
			if (cbxEdgeTypes.getSelectedItem() != null) {
				edgeType = (String) cbxEdgeTypes.getSelectedItem();
			}
			SpinnerNumberModel model = (SpinnerNumberModel) spinDefWeight.getModel();
			if (model.getNumber() != null) {
				defWeight = model.getNumber().doubleValue();
			}
			if (cbxNegWeights.getSelectedItem() != null) {
				negWeight = (String) cbxNegWeights.getSelectedItem();
			}
			if (cbxSimToDist.getSelectedItem() != null) {
				simToDist = (String) cbxSimToDist.getSelectedItem();
			}
			model = (SpinnerNumberModel) spinDegree.getModel();
			if (model.getNumber() != null) {
				degreeCutoff = model.getNumber().doubleValue();
			}
			if (!measures[0] && !measures[1] && !measures[2]) {
				JOptionPane.showMessageDialog(this, Messages.SM_NOTHINGTOCOMP, Messages.DT_ERROR,
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			canceled = false;
			setVisible(false);
			dispose();
		} else if (src == cbxUseConnComp) {
			useConnComp = cbxUseConnComp.isSelected();
		} else if (src == cbxCompSP) {
			measures[0] = cbxCompSP.isSelected();
		} else if (src == cbxCompCF) {
			measures[1] = cbxCompCF.isSelected();
		} else if (src == cbxCompRW) {
			measures[2] = cbxCompRW.isSelected();
		} else if (src == btnHelp) {
			((OpenBrowser) CyUtils.getService(context, OpenBrowser.class))
					.openURL(Messages.HELP_ANALYSIS);
		} else if (src == btnCancel) {
			canceled = true;
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Return the connected components within the set of selected nodes.
	 * 
	 * @return An instance of the class {@link ConnComp} on the currently
	 *         selected set of nodes.
	 */
	public ConnComp getConnComp() {
		return connComps;
	}

	/**
	 * Get the user defined default weight.
	 * 
	 * @return Default weight to replace missing edge weights.
	 */
	public double getDefWeight() {
		return defWeight;
	}

	/**
	 * Returns the user-defined value for degree cutoff.
	 * 
	 * @return User-defined value for degree cutoff.
	 */
	public double getDegreeCutoff() {
		return degreeCutoff;
	}

	/**
	 * Get array with flags indicating which measures should be computed ([sp
	 * measures, cf measures, rw measures])
	 * 
	 * @return Array with flags for measures computation.
	 */
	public boolean[] getMeasuresToCompute() {
		return measures;
	}

	/**
	 * Get the user defined options for modifying the weights. [weightAttr,
	 * edgeType, negWeight, simToDist]
	 * 
	 * @return Array with weight options.
	 */
	public String[] getWeightOptions() {
		return new String[] { weightAttr, edgeType, negWeight, simToDist };
	}

	/**
	 * Status of the dialog's ending.
	 * 
	 * @return <code>true</code> if the <code>AnalysisDialog</code> has been
	 *         canceled by the user, and <code>false</code> otherwise.
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Check if only pairs that have nodes in different sets should be used for
	 * computation.
	 * 
	 * @return <code>true</code> if the user chose this option, and
	 *         <code>false</code> otherwise.
	 */
	public boolean useConnComp() {
		return useConnComp;
	}

	/**
	 * Initializes the <code>AnalysisDialog</code>.
	 */
	private void init() {
		final int BS = UtilsUI.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		contentPane.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));

		// Panel with title
		final JPanel panTitle = new JPanel();
		final String title = Messages.DI_COMPUTE1
				+ network.getRow(network).get(CyNetwork.NAME, String.class) + Messages.DI_COMPUTE2;
		if (connComps.getConnCompNumber() == 1) {
			panTitle.add(UtilsUI.createLabel(title, null));
		} else {
			panTitle.setLayout(new GridLayout(2, 1, BS, BS));
			panTitle.add(UtilsUI.createLabel(title, null));
			panTitle.add(UtilsUI.createLabel(Messages.SM_CONNCOMP, null));
		}

		// Panel with options
		final JComponent panOptions = new Box(BoxLayout.PAGE_AXIS);

		// Add Centrality settings
		final JPanel panCentrality = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(BS / 2, BS / 2, BS / 2, BS / 2);
		panCentrality.setLayout(gbl);
		panCentrality.setBorder(new TitledBorder(null, Messages.DI_CENTMEASURES));

		// Add compute shortest path measures check box
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panCentrality.add(UtilsUI.createLabel(Messages.DI_COMPSPCENT, null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		cbxCompSP = UtilsUI.createCheckBox("", null, measures[0], this);
		cbxCompSP.setEnabled(true);
		panCentrality.add(cbxCompSP, gbc);
		gbc.gridy++;

		// Add compute current flow measures check box
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panCentrality.add(UtilsUI.createLabel(Messages.DI_COMPCFCENT, null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		cbxCompCF = UtilsUI.createCheckBox("", null, measures[1], this);
		cbxCompCF.setEnabled(!compOnlySP);
		panCentrality.add(cbxCompCF, gbc);
		gbc.gridy++;

		// Add compute random walk measures check box
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panCentrality.add(UtilsUI.createLabel(Messages.DI_COMPRWCENT, null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		cbxCompRW = UtilsUI.createCheckBox("", null, measures[2], this);
		cbxCompRW.setEnabled(!compOnlySP);
		panCentrality.add(cbxCompRW, gbc);

		// Add weight attribute choice
		final JPanel panWeight = new JPanel();
		gbl = new GridBagLayout();
		panWeight.setLayout(gbl);
		panWeight.setBorder(new TitledBorder(null, Messages.DI_CENTANASET));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panWeight.add(UtilsUI.createLabel(Messages.DI_CHOOSEATTRIBUTE, null), gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.weightx = 1;
		cbxAttr = new JComboBox(attrs);
		panWeight.add(cbxAttr, gbc);
		gbc.gridy++;

		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panWeight.add(UtilsUI.createLabel(Messages.DI_MULTIPLEEDGES, Messages.TT_MULTIPLEEDGES),
				gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.weightx = 1;
		List<String> items = CyUtils.getStringAttrValues(network, Messages.EDGE_INTERACTIONS,
				CyEdge.class);
		Collections.sort(items);
		items.add(0, Messages.DI_WEIGHTMIN);
		items.add(0, Messages.DI_WEIGHTMAX);
		items.add(0, Messages.DI_WEIGHTSUM);
		items.add(0, Messages.DI_WEIGHTAVE);
		cbxEdgeTypes = new JComboBox(items.toArray());
		// cbxEdgeTypes.setToolTipText(Messages.TT_EDGETYPE);
		panWeight.add(cbxEdgeTypes, gbc);
		gbc.gridy++;

		// add combo box for negative weights
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panWeight.add(
				UtilsUI.createLabel(Messages.DI_REMOVE_NEGWEIGHT, Messages.TT_REMOVE_NEGWEIGHT),
				gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.weightx = 1;
		cbxNegWeights = new JComboBox(new String[] { Messages.DI_NEGWEIGHT_IGNORE,
				Messages.DI_NEGWEIGHT_REVERT });
		// cbxNegWeights.setToolTipText();
		panWeight.add(cbxNegWeights, gbc);
		gbc.gridy++;

		// add combo box for converting weights
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panWeight.add(UtilsUI.createLabel(Messages.DI_CONVERTWEIGHT, Messages.TT_CONVERTWEIGHT),
				gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.weightx = 1;
		cbxSimToDist = new JComboBox(new String[] { "", Messages.DI_SIMTODIST1,
				Messages.DI_SIMTODIST2 });
		// cbxSimToDist.setToolTipText();
		panWeight.add(cbxSimToDist, gbc);
		gbc.gridy++;

		// add default weight spinner
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panWeight.add(UtilsUI.createLabel(Messages.DI_DEFWEIGHTVALUE, Messages.TT_DEFWEIGHTVALUE),
				gbc);
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.weightx = 1;
		spinDefWeight = new JSpinner(new SpinnerNumberModel(defWeight, 0.0, MAXWEIGHT, 0.01));
		panWeight.add(spinDefWeight, gbc);
		gbc.gridy++;

		// Add degree cutoff
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panWeight.add(UtilsUI.createLabel(Messages.DI_DEGREECUTOFF, Messages.TT_DEGREECUTOFF), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		spinDegree = new JSpinner(new SpinnerNumberModel(degreeCutoff, 0.01, MAXDEGREE, 0.01));
		// panDegree.setToolTipText(Messages.TT_DEGREECUTOFF);
		panWeight.add(spinDegree, gbc);
		gbc.gridy++;

		// Add use connected components check box
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		final JLabel connCompLabel = UtilsUI.createLabel(Messages.DI_USECONNCOMP, null);
		panWeight.add(connCompLabel, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		cbxUseConnComp = UtilsUI.createCheckBox("", null, useConnComp, this);
		cbxUseConnComp.setEnabled(hasConnComp);
		if (!hasConnComp) {
			connCompLabel.setToolTipText(Messages.TT_BETWPAIRS);
		}
		panWeight.add(cbxUseConnComp, gbc);

		// gbc.gridx = 0;
		// gbc.weightx = 1;
		// gbc.anchor = GridBagConstraints.LINE_START;
		// cbxUseConnComp = UtilsUI.createCheckBox(Messages.DI_USECONNCOMP,
		// null, useConnComp,
		// this);
		// cbxUseConnComp.setEnabled(hasConnComp);
		// cbxUseConnComp.setToolTipText(Messages.TT_BETWPAIRS);
		// panCentrality.add(cbxUseConnComp, gbc);

		UtilsUI.limitHeight(panWeight);
		panOptions.add(panCentrality);
		UtilsUI.limitHeight(panCentrality);
		panOptions.add(panWeight);
		panOptions.add(Box.createGlue());
		// adjust width of combo boxes
		UtilsUI.adjustWidth(new JComponent[] { cbxAttr, cbxEdgeTypes, cbxNegWeights, cbxSimToDist });

		// add Analyze and Cancel buttons
		final JPanel panButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, BS, 0));
		btnAnalyze = UtilsUI.createButton(Messages.DI_ANALYZE, null, null, this);
		btnCancel = UtilsUI.createButton(Messages.DI_CANCEL, null, null, this);
		btnHelp = UtilsUI.createButton(Messages.DI_HELP, null, null, this);
		UtilsUI.adjustWidth(new JButton[] { btnAnalyze, btnCancel, btnHelp });
		panButtons.add(btnAnalyze);
		panButtons.add(btnCancel);
		panButtons.add(Box.createHorizontalStrut(BS * 2));
		panButtons.add(btnHelp);

		contentPane.add(panTitle, BorderLayout.NORTH);
		contentPane.add(panOptions, BorderLayout.CENTER);
		contentPane.add(panButtons, BorderLayout.SOUTH);
		setContentPane(contentPane);
		pack();
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -5884769060506203234L;

	/**
	 * Available numeric edge attributes that could be used for edge weight.
	 */
	private String[] attrs;

	private CyServiceRegistrar context;

	/**
	 * Network to be analyzed.
	 */
	private CyNetwork network;

	/**
	 * Set of selected nodes.
	 */
	private Set<CyNode> selectedSet;

	/**
	 * Flag indicating that only sp measures can be computed.
	 */
	private boolean compOnlySP;

	/**
	 * User-defined edge attribute to be used as an edge weight.
	 */
	private String weightAttr;

	/**
	 * User-defined edge type to be used when there are multiple edges.
	 */
	private String edgeType;

	/**
	 * Option for removing negative edge weights.
	 */
	private String negWeight;

	/**
	 * Option for the conversion of similarity score info distance scores.
	 */
	private String simToDist;

	/**
	 * Flag indicating if the <code>AnalysisDialog</code> has been canceled by
	 * the user.
	 */
	private boolean canceled;

	/**
	 * Flag indicating if there are different connected components.
	 */
	private boolean hasConnComp;

	/**
	 * Flag indicating if the user wants to use the connected components for
	 * betweenness pairs.
	 */
	private boolean useConnComp;

	/**
	 * Set of connected components within the set of selected nodes.
	 */
	private ConnComp connComps;

	/**
	 * Array with boolean flags for each group of centrality measures. If the
	 * flag is <code>true</code>, the measure should be computed.
	 */
	private boolean[] measures;

	/**
	 * &quot;Analyze&quot; button.
	 */
	private JButton btnAnalyze;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnCancel;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnHelp;

	/**
	 * Drop-down menu with edge attributes.
	 */
	private JComboBox cbxAttr;

	/**
	 * Drop-down menu with edge attribute types.
	 */
	private JComboBox cbxEdgeTypes;

	/**
	 * Drop-down menu with options for removing negative weights.
	 */
	private JComboBox cbxNegWeights;

	/**
	 * Drop-down menu with options for converting similarity scores to
	 * distances.
	 */
	private JComboBox cbxSimToDist;

	/**
	 * Check-box for the flag <code>measures[0]</code>, i.e. compute SP
	 * measures.
	 */
	private JCheckBox cbxCompSP;

	/**
	 * Check-box for the flag <code>measures[1]</code>, i.e. compute CF
	 * measures.
	 */
	private JCheckBox cbxCompCF;

	/**
	 * Check-box for the flag <code>measures[2]</code>, i.e. compute RW
	 * measures.
	 */
	private JCheckBox cbxCompRW;

	/**
	 * Check-box for the flag <code>useConnComp</code>.
	 */
	private JCheckBox cbxUseConnComp;

	/**
	 * Text field for entering the degree cutoff value <code>degreeCutoff</code>
	 * .
	 */
	private JSpinner spinDegree;

	/**
	 * Text field for entering the default weight value
	 * <code>defaultWeight</code>.
	 */
	private JSpinner spinDefWeight;

	/**
	 * Default value for degree cutoff.
	 */
	private double degreeCutoff = 1.0;

	/**
	 * Default value for missing edge weight values.
	 */
	private double defWeight = 1.0;

	/**
	 * Default value for degree cutoff.
	 */
	private static double DEFAULTDEGREE = 1.0;

	/**
	 * Default value for missing edge weight values.
	 */
	private static double DEFAULTWEIGHT = 1.0;

	/**
	 * Maximum value for degree cutoff.
	 */
	private static double MAXDEGREE = 10000.0;

	/**
	 * Maximum value for missing edge weight values.
	 */
	private static double MAXWEIGHT = 10000.0;
}
