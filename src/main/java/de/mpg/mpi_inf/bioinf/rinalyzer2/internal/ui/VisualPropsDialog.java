package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesSerializer;

/**
 * Defines and presents in a dialog the basic visual properties of a RIN. These are the colors for
 * the secondary structure of the nodes and for the different types of interactions represented by
 * the edges, backbone edges, edge width, background color.
 * 
 * @author Nadezhda Doncheva
 */
// TODO: [Improve][VisProps] Check "restore button" behavior under windows?
// TODO: [Improve][VisProps] Save/Load independent properties for each network
// TODO: [Improve][VisProps] Uncouple show/hide edges and visual props
// TODO: [Improve][VisProps] Change the add backbone edges to a button
public class VisualPropsDialog extends JDialog implements ActionListener, ColorChangeListener,
		ChangeListener, EdgeCbxListener, SetCurrentNetworkViewListener {

	/**
	 * Initializes a new instance of <code>VisualPropsDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aNetwork
	 *            The Cytoscape network that is currently selected.
	 * @param aRINvisPropManager
	 *            Hanlder for changes in the visual properties of RINs.
	 * 
	 */
	public VisualPropsDialog(Frame aOwner, CyServiceRegistrar bc,
			RINVisualPropertiesManager aRINvisPropManager) {
		super(aOwner, Messages.DT_VISUALPROPS, false);
		context = bc;
		rinVisPropManager = aRINvisPropManager;
		// addChimeraColorListener(RINalyzerPlugin.getChimera());
		straightLines = true;
		showBackbone = false;
		nodeTypes = new TreeSet<String>(Arrays.asList(Messages.nodesSecondStruct));
		initPanel();
		setResizable(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		// if Close button, do nothing and exit
		if (src == btnClose) {
			setVisible(false);
			dispose();
			return;
		} else if (src == btnHelp) {
			((OpenBrowser) CyUtils.getService(context, OpenBrowser.class))
					.openURL(Messages.HELP_VISPROPS);
			return;
		}
		// otherwise, create/get new visual style
		if (src == btnApply) {
			// set background color
			rinVisPropManager.changeBackgroundColor(network);
			// color nodes
			rinVisPropManager.changeNodeColor(network);
			// label nodes
			rinVisPropManager.labelOperation(getSelectedLabels(), isThreeLetterCode(), network);
			// add/hide backbone edges
			if (showBackbone) {
				if (!rinVisPropManager.isBackboneShown(network)) {
					rinVisPropManager.backboneOperation(Messages.DI_BACKBONE_ADD, network);
				}
			} else {
				if (rinVisPropManager.isBackboneShown(network)) {
					rinVisPropManager.backboneOperation(Messages.DI_BACKBONE_HIDE, network);
				}
			}
			// apply edge layout
			rinVisPropManager.changeEdgeColor(network);
			rinVisPropManager.changeEdgeWidth(network);
			rinVisPropManager.hideDistEdges(network, view);
			// show only checked edge types
			rinVisPropManager.showEdges(network);
			// straighten lines
			if (straightLines) {
				rinVisPropManager.addEdgeBends(network, view);
			} else {
				rinVisPropManager.removeEdgeBends(network, view);
			}
			// re-initialize panel and apply changes to network
			initializeProps(view);

		} else if (src == cbxStraightEdges) {
			straightLines = cbxStraightEdges.isSelected();
			// straighten lines
			if (straightLines) {
				rinVisPropManager.addEdgeBends(network, view);
			} else {
				rinVisPropManager.removeEdgeBends(network, view);
			}
			// re-initialize panel and apply changes to network
			initializeProps(view);

		} else if (src == cbxShowBackbone) {
			showBackbone = cbxShowBackbone.isSelected();
			if (showBackbone) {
				if (!rinVisPropManager.isBackboneShown(network)) {
					rinVisPropManager.backboneOperation(Messages.DI_BACKBONE_ADD, network);
				}
			} else {
				if (rinVisPropManager.isBackboneShown(network)) {
					rinVisPropManager.backboneOperation(Messages.DI_BACKBONE_HIDE, network);
				}
			}
			// re-initialize panel and apply changes to network
			initializeProps(view);
		} else if (e.getActionCommand().equals(Messages.DI_RESTORE)) {
			rinVisPropManager.setColorMap(network, RINVisualPropertiesSerializer.getColorMap());
			rinVisPropManager.setSizeConst(network, RINVisualPropertiesSerializer.getSizeConst());
			initializeProps(view);
		} else if (e.getActionCommand().equals(Messages.DI_SETDEFAULT)) {
			RINVisualPropertiesSerializer
					.storeDefaultVisualProps(context, this, rinVisPropManager.getColorMap(network),
							rinVisPropManager.getSizeConst(network));
		} else if (e.getActionCommand().equals(Messages.DI_LABEL) || src == rbThreeLetterCode
				|| src == rbOneLetterCode) {
			adaptExampleLabel();
			// label nodes
			rinVisPropManager.labelOperation(getSelectedLabels(), isThreeLetterCode(), network);
			// re-initialize panel and apply changes to network
			initializeProps(view);
		}
	}

	/**
	 * Listen for spinner model changes and save them.
	 */
	public void stateChanged(ChangeEvent e) {
		final Object src = e.getSource();
		if (src == spinnerBBEdgeWidth) {
			final SpinnerNumberModel model = (SpinnerNumberModel) spinnerBBEdgeWidth.getModel();
			if (model.getNumber() != null) {
				rinVisPropManager.setBBEdgeWidth(network, model.getNumber().intValue());
			}
		} else if (src == spinnerEdgeDistFilter) {
			final SpinnerNumberModel model = (SpinnerNumberModel) spinnerEdgeDistFilter.getModel();
			if (model.getNumber() != null) {
				rinVisPropManager.setEdgeDistFilter(network, model.getNumber().intValue());
			}
		} else if (src == spinnerEdgeWidth) {
			final SpinnerNumberModel model = (SpinnerNumberModel) spinnerEdgeWidth.getModel();
			if (model.getNumber() != null) {
				rinVisPropManager.setEdgeWidth(network, model.getNumber().intValue());
			}
		} else if (src == spinnerEdgeSpace) {
			final SpinnerNumberModel model = (SpinnerNumberModel) spinnerEdgeSpace.getModel();
			if (model.getNumber() != null) {
				rinVisPropManager.setEdgeSpace(network, model.getNumber().intValue());
			}
		} else if (src == spinnerLabelSize) {
			final SpinnerNumberModel model = (SpinnerNumberModel) spinnerLabelSize.getModel();
			if (model.getNumber() != null) {
				rinVisPropManager.setLabelSize(network, model.getNumber().intValue());
			}
		} else if (src == spinnerNodeSize) {
			final SpinnerNumberModel model = (SpinnerNumberModel) spinnerNodeSize.getModel();
			if (model.getNumber() != null) {
				rinVisPropManager.setNodeSize(network, model.getNumber().intValue());
			}
		}
	}

	/**
	 * Catch color changes from the color buttons and saves the new color.
	 */
	public void colorChanged(String attr, Color newColor) {
		rinVisPropManager.setColor(network, attr, newColor);
	}

	/**
	 * Listen for "network view changed" events and update the visual properties dialog.
	 */
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (!isVisible()) {
			return;
		}
		CyNetworkView currentView = e.getNetworkView();
		if (currentView != null) {
			CyNetwork currentNet = e.getNetworkView().getModel();
			if (currentNet != null && network != null && !network.equals(currentNet)) {
				initializeProps(currentView);
			}
		}
	}

	/**
	 * Listen for "check box value changed" events and show/hide edges.
	 */
	public void cbxValueChanged(String attr, Boolean newValue) {
		// show/hide edges
		rinVisPropManager.setShownEdgeType(network, attr, newValue);
		rinVisPropManager.showEdges(network);
		// re-initialize panel and apply changes to network
		initializeProps(view);
		view.updateView();
	}

	/**
	 * Update example label.
	 */
	private void adaptExampleLabel() {
		final String label = CyUtils.getLabel(Messages.DI_LABEL_SEP, getSelectedLabels(),
				EXAMPLE_LABEL_PARTS, EXAMPLE_LABEL, isThreeLetterCode());
		exampleLabel.setText("<html><font size=\"-2\">" + Messages.DI_EXAMPLE + "<br> <center><b>"
				+ label + "</b></center></font><html>");
	}

	/**
	 * Check if residue type should be displayed in three- or one-letter code.
	 * 
	 * @return <code>true</code> if three-letter code is selected;
	 */
	private boolean isThreeLetterCode() {
		if (rbThreeLetterCode.isSelected()) {
			return true;
		}
		return false;
	}

	/**
	 * Label nodes with the labels that the user has selected.
	 */
	private boolean[] getSelectedLabels() {
		return new boolean[] { cbxLabelPdb.isSelected(), cbxLabelChain.isSelected(),
				cbxLabelIndex.isSelected(), cbxLabelICode.isSelected(), cbxLabelType.isSelected() };
	}

	/**
	 * Initialize the default values of the props and set the network. This method is called each
	 * time the network view is switched or a new network is opened.
	 * 
	 * @param aNetwork
	 *            Target network.
	 */
	public void initializeProps(CyNetworkView aNetworkView) {
		if (aNetworkView == null) {
			return;
		}
		view = aNetworkView;
		network = aNetworkView.getModel();
		if (network == null) {
			return;
		}
		// boolean newNetwork = false;
		if (!rinVisPropManager.hasNetwork(network)) {
			rinVisPropManager.addNetwork(network, view);
			// newNetwork = true;
		}
		initNodeProps();
		initEdgeProps();
		pack();
		view.updateView();
	}

	/**
	 * Initialize the dialog. This method is called only once upon initialization.
	 */
	private void initPanel() {
		BS = UtilsUI.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		contentPane.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));

		// final JPanel panAllColors = new JPanel(new BorderLayout(BS, BS));
		final JTabbedPane options = new JTabbedPane();
		panEdge = new JPanel(new BorderLayout());
		options.addTab(Messages.DI_EDGE_PROPS, panEdge);
		panNode = new JPanel(new BorderLayout());
		options.addTab(Messages.DI_GEN_NODE_PROPS, panNode);
		contentPane.add(options, BorderLayout.CENTER);

		// initialize example label
		exampleLabel = new JLabel();
		// final Font labelFont = exampleLabel.getFont();
		// exampleLabel.setFont(new Font(labelFont.getFamily(),
		// labelFont.getStyle(), labelFont
		// .getSize() - 2));

		// add buttons
		final JPanel panBottom = new JPanel(new BorderLayout(BS, BS));
		final JPanel panButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel panHelp = new JPanel(new FlowLayout(FlowLayout.CENTER));
		btnApply = UtilsUI.createButton(Messages.DI_APPLY, Messages.TT_APPLY_VIS, null, this);
		btnClose = UtilsUI.createButton(Messages.DI_CLOSE, null, null, this);
		btnHelp = UtilsUI.createButton(Messages.DI_HELP, null, null, this);
		UtilsUI.adjustWidth(new JButton[] { btnApply, btnClose, btnHelp });
		panButtons.add(btnApply);
		panButtons.add(btnClose);
		panHelp.add(btnHelp);
		panBottom.add(panButtons, BorderLayout.LINE_END);
		panBottom.add(panHelp, BorderLayout.LINE_START);
		contentPane.add(panBottom, BorderLayout.SOUTH);
		setContentPane(contentPane);
	}

	/**
	 * Initialize the edge properties panel. The panel is initialized again each time the network
	 * view is switched or a new network is opened.
	 */
	private void initEdgeProps() {
		panEdge.removeAll();
		final Set<String> edgeTypes = rinVisPropManager.getEdgeTypes(network);
		final JComponent panEdgeProps = new Box(BoxLayout.PAGE_AXIS);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(BS / 2, BS / 2, BS / 2, BS / 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		// Panel for backbone edges
		GridBagLayout gbl = new GridBagLayout();
		final JPanel panBackbone = new JPanel();
		panBackbone.setBorder(new TitledBorder(null, Messages.DI_BACKBONE_EDGES));
		panBackbone.setLayout(gbl);
		// add check box for showing bb edges
		if (edgeTypes.contains(Messages.EDGE_BACKBONE)) {
			rinVisPropManager.recognizeBackboneEdges(network);
		}
		showBackbone = rinVisPropManager.isBackboneShown(network);
		cbxShowBackbone = UtilsUI
				.createCheckBox(Messages.DI_BACKBONE_ADD, null, showBackbone, this);
		panBackbone.add(cbxShowBackbone, gbc);
		gbc.gridy++;
		// add text field for bb edges width
		gbc.gridx = 0;
		panBackbone.add(UtilsUI.createLabel(Messages.DI_BACKBONE_EDGE_WIDTH, null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		final int bbEdgeWidth = rinVisPropManager.getBBEdgeWidth(network);
		rinVisPropManager.setBBEdgeWidth(network, bbEdgeWidth);
		spinnerBBEdgeWidth = new JSpinner(new SpinnerNumberModel(bbEdgeWidth, 1, MAXEDGEWIDTH, 1));
		spinnerBBEdgeWidth.addChangeListener(this);
		panBackbone.add(spinnerBBEdgeWidth, gbc);
		gbc.gridy++;
		UtilsUI.limitHeight(panBackbone);
		panEdgeProps.add(panBackbone);

		// Panel for edge distance filter
		gbl = new GridBagLayout();
		final JPanel panEdgeDistFilter = new JPanel();
		panEdgeDistFilter.setBorder(new TitledBorder(null, "Edge Distance Filter"));
		panEdgeDistFilter.setLayout(gbl);
		// add text field for bb edges width
		gbc.gridx = 0;
		panEdgeDistFilter.add(UtilsUI.createLabel("Hide edges below threshold", null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		final int edgeDist = rinVisPropManager.getEdgeDistFilter(network);
		rinVisPropManager.setEdgeDistFilter(network, edgeDist);
		spinnerEdgeDistFilter = new JSpinner(new SpinnerNumberModel(edgeDist, 1, MAXEDGEDIST, 1));
		spinnerEdgeDistFilter.addChangeListener(this);
		panEdgeDistFilter.add(spinnerEdgeDistFilter, gbc);
		gbc.gridy++;
		UtilsUI.limitHeight(panEdgeDistFilter);
		panEdgeProps.add(panEdgeDistFilter);

		// Panel for edge colors depending on interaction type.
		final EdgeColorButtonPanel panEdgeColors = new EdgeColorButtonPanel(edgeTypes,
				rinVisPropManager.getColorMap(network), rinVisPropManager.getShownEdges(network));
		panEdgeColors.setBorder(new TitledBorder(null, Messages.DI_EDGE_COLORS));
		panEdgeColors.addColorChangeListener(this);
		panEdgeColors.addCheckBoxListener(this);
		UtilsUI.limitHeight(panEdgeColors);
		panEdgeProps.add(panEdgeColors);

		// Panel for edge width and space
		final JPanel panEdgeLines = new JPanel();
		panEdgeLines.setBorder(new TitledBorder(null, Messages.DI_EDGELINES));
		gbl = new GridBagLayout();
		gbc.gridx = 0;
		gbc.gridy = 0;
		panEdgeLines.setLayout(gbl);
		// Add check box for straighten multiple edges
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		cbxStraightEdges = UtilsUI.createCheckBox(Messages.DI_STRAIGHTEDGES, null, straightLines,
				this);
		panEdgeLines.add(cbxStraightEdges, gbc);
		gbc.gridy++;
		// Add spinner for edge width
		panEdgeLines.add(UtilsUI.createLabel(Messages.DI_EDGE_WIDTH, null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		final int edgeWidth = rinVisPropManager.getEdgeWidth(network);
		rinVisPropManager.setEdgeWidth(network, edgeWidth);
		spinnerEdgeWidth = new JSpinner(new SpinnerNumberModel(edgeWidth, 1, MAXEDGEWIDTH, 1));
		spinnerEdgeWidth.addChangeListener(this);
		panEdgeLines.add(spinnerEdgeWidth, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		// Add spinner for edge space
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panEdgeLines.add(UtilsUI.createLabel(Messages.DI_EDGE_SPACE, Messages.TT_EDGESPACE), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		final int edgeSpace = rinVisPropManager.getEdgeSpace(network);
		rinVisPropManager.setEdgeSpace(network, edgeSpace);
		spinnerEdgeSpace = new JSpinner(new SpinnerNumberModel(edgeSpace, 0, MAXEDGEWIDTH + 1, 1));
		spinnerEdgeSpace.addChangeListener(this);
		panEdgeLines.add(spinnerEdgeSpace, gbc);
		UtilsUI.limitHeight(panEdgeLines);
		panEdgeProps.add(panEdgeLines);
		panEdgeProps.add(Box.createGlue());
		panEdge.add(panEdgeProps, BorderLayout.NORTH);

		// add panel with buttons
		final JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel panButtons = new JPanel(new GridLayout(1, 2, BS, BS));
		final JButton btnSave = UtilsUI.createButton(Messages.DI_SETDEFAULT,
				Messages.TT_SETDEFAULT, Messages.DI_SETDEFAULT, this);
		final JButton btnRestore = UtilsUI.createButton(Messages.DI_RESTORE, Messages.TT_RESTORE,
				Messages.DI_RESTORE, this);
		panButtons.add(btnSave);
		panButtons.add(btnRestore);
		panBottom.add(panButtons);
		panEdge.add(panBottom, BorderLayout.SOUTH);
	}

	/**
	 * Initialize the node & general properties panel. The panel is initialized again each time the
	 * network view is switched or a new network is opened.
	 */
	private void initNodeProps() {
		panNode.removeAll();
		final JComponent panNodeProps = new Box(BoxLayout.PAGE_AXIS);

		// Add button for background color
		final Set<String> generalTypes = new HashSet<String>(1);
		generalTypes.add(Messages.BGCOLOR);
		final SimpleColorButtonPanel panBackground = new SimpleColorButtonPanel(generalTypes,
				rinVisPropManager.getColorMap(network));
		panBackground.addColorChangeListener(this);
		panBackground.setBorder(new TitledBorder(null, Messages.DI_GENERAL));
		UtilsUI.limitHeight(panBackground);
		panNodeProps.add(panBackground);

		// Panel for node colors depending on secondary structure
		final SimpleColorButtonPanel panSSColor = new SimpleColorButtonPanel(nodeTypes,
				rinVisPropManager.getColorMap(network));
		panSSColor.addColorChangeListener(this);
		panSSColor.setBorder(new TitledBorder(null, Messages.DI_SS_COLORS));
		UtilsUI.limitHeight(panSSColor);
		panNodeProps.add(panSSColor);

		// Panel for node label
		final JPanel panNodeLabel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		panNodeLabel.setLayout(gbl);
		panNodeLabel.setBorder(new TitledBorder(null, Messages.DI_LABEL));
		// add label options
		final JPanel panNodeLabelOpt = new JPanel(new GridLayout(7, 1, 0, BS / 2));
		panNodeLabelOpt.setBorder(new EmptyBorder(0, BS / 2, BS / 2, BS / 2));
		final boolean[] selectedLabels = rinVisPropManager.getSelectedLabels(network);
		final boolean[] enabledLabels = rinVisPropManager.getEnabledLabels(network);
		cbxLabelPdb = createNodeLabelCbx(Messages.DI_LABEL_PDB, selectedLabels[0], enabledLabels[0]);
		panNodeLabelOpt.add(cbxLabelPdb);
		cbxLabelChain = createNodeLabelCbx(Messages.DI_LABEL_CHAIN, selectedLabels[1],
				enabledLabels[1]);
		panNodeLabelOpt.add(cbxLabelChain);
		cbxLabelIndex = createNodeLabelCbx(Messages.DI_LABEL_INDEX, selectedLabels[2],
				enabledLabels[2]);
		panNodeLabelOpt.add(cbxLabelIndex);
		cbxLabelICode = createNodeLabelCbx(Messages.DI_LABEL_ICODE, selectedLabels[3],
				enabledLabels[3]);
		panNodeLabelOpt.add(cbxLabelICode);
		cbxLabelType = createNodeLabelCbx(Messages.DI_LABEL_TYPE, selectedLabels[4],
				enabledLabels[4]);
		panNodeLabelOpt.add(cbxLabelType);
		final ButtonGroup group = new ButtonGroup();
		final boolean selectedCode = rinVisPropManager.getThreeLetterCode(network);
		rbThreeLetterCode = createNodeLabelRb(Messages.DI_LABEL_TYPE_3LC, selectedCode,
				selectedLabels[4]);
		group.add(rbThreeLetterCode);
		panNodeLabelOpt.add(rbThreeLetterCode);
		rbOneLetterCode = createNodeLabelRb(Messages.DI_LABEL_TYPE_1LC, !selectedCode,
				selectedLabels[4]);
		group.add(rbOneLetterCode);
		panNodeLabelOpt.add(rbOneLetterCode);

		// add example
		final JPanel panNodeLabelExp = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel panExp = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panExp.setBorder(new TitledBorder(""));
		adaptExampleLabel();
		panExp.add(exampleLabel);
		panNodeLabelExp.add(panExp);
		// add panels to node label panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 0;
		panNodeLabel.add(panNodeLabelOpt, gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		panNodeLabel.add(panNodeLabelExp, gbc);
		UtilsUI.limitHeight(panNodeLabel);
		panNodeProps.add(panNodeLabel);

		// Panel with other options, i.e. node size and label size
		final JPanel panOther = new JPanel();
		panOther.setBorder(new TitledBorder(null, Messages.DI_OTHER_OPTIONS));
		gbl = new GridBagLayout();
		panOther.setLayout(gbl);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(BS / 2, BS / 2, BS / 2, BS / 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		// Add spinner for label size
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panOther.add(UtilsUI.createLabel(Messages.DI_LABEL_SIZE, null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		final int labelSize = rinVisPropManager.getLabelSize(network);
		rinVisPropManager.setLabelSize(network, labelSize);
		spinnerLabelSize = new JSpinner(new SpinnerNumberModel(labelSize, 6, MAXLABELSIZE, 1));
		spinnerLabelSize.addChangeListener(this);
		panOther.add(spinnerLabelSize, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		// Add spinner for node size
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		panOther.add(UtilsUI.createLabel(Messages.DI_NODE_SIZE, null), gbc);
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		final int nodeSize = rinVisPropManager.getNodeSize(network);
		rinVisPropManager.setNodeSize(network, nodeSize);
		spinnerNodeSize = new JSpinner(new SpinnerNumberModel(nodeSize, 1, MAXNODESIZE, 1));
		spinnerNodeSize.addChangeListener(this);
		panOther.add(spinnerNodeSize, gbc);
		UtilsUI.limitHeight(panOther);
		panNodeProps.add(panOther);

		panNodeProps.add(Box.createGlue());
		panNode.add(panNodeProps, BorderLayout.NORTH);

		// add panel with buttons
		final JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel panButtons = new JPanel(new GridLayout(1, 2, BS, BS));
		final JButton btnSave = UtilsUI.createButton(Messages.DI_SETDEFAULT,
				Messages.TT_SETDEFAULT, Messages.DI_SETDEFAULT, this);
		final JButton btnRestore = UtilsUI.createButton(Messages.DI_RESTORE, Messages.TT_RESTORE,
				Messages.DI_RESTORE, this);
		panButtons.add(btnSave);
		panButtons.add(btnRestore);
		panBottom.add(panButtons);
		panNode.add(panBottom, BorderLayout.SOUTH);
	}

	/**
	 * Create a label part check box.
	 * 
	 * @param labelPart
	 *            Name of the check box.
	 * @param selected
	 *            "Selected" flag.
	 * @param enabled
	 *            "Enabled" flag.
	 * @return Created check box.
	 */
	private JCheckBox createNodeLabelCbx(String labelPart, boolean selected, boolean enabled) {
		JCheckBox cbx = UtilsUI.createCheckBox(labelPart, Messages.DI_LABEL, selected, this);
		cbx.setEnabled(enabled);
		return cbx;
	}

	/**
	 * Create a residue type radio button.
	 * 
	 * @param name
	 *            Name of the radio button.
	 * @param selected
	 *            "Selected" flag.
	 * @param enabled
	 *            "Enabled" flag.
	 * @return Created radio button.
	 */
	private JRadioButton createNodeLabelRb(String name, boolean selected, boolean enabled) {
		JRadioButton rb = new JRadioButton(name);
		rb.setEnabled(enabled);
		rb.setSelected(selected);
		rb.addActionListener(this);
		return rb;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 2120035190778415155L;

	/**
	 * Target network.
	 */
	private CyNetwork network;

	/**
	 * Target network view.
	 */
	private CyNetworkView view;

	/**
	 * Handler for all visual properties operations.
	 */
	private RINVisualPropertiesManager rinVisPropManager;

	private CyServiceRegistrar context;

	/**
	 * Listens for node colors changes according to secondary structure.
	 */
	// private NodeColorListener ccListener;

	/**
	 * Flag indicating if the edge lines should be straightened.
	 */
	private boolean straightLines;

	private boolean showBackbone;

	/**
	 * A list of the different values of the node attribute &quot;SS&quot;.
	 */
	private Set<String> nodeTypes;

	/**
	 * &quot;Apply&quot; button.
	 */
	private JButton btnApply;

	/**
	 * &quot;Close&quot; button.
	 */
	private JButton btnClose;

	/**
	 * &quot;Help&quot; button.
	 */
	private JButton btnHelp;

	/**
	 * Checkbox for showing backbone edges.
	 */
	private JCheckBox cbxShowBackbone;

	/**
	 * Checkbox for straightening edges.
	 */
	private JCheckBox cbxStraightEdges;

	/**
	 * Checkbox for showing pdb id in node label.
	 */
	private JCheckBox cbxLabelPdb;

	/**
	 * Checkbox for showing chain id in node label.
	 */
	private JCheckBox cbxLabelChain;

	/**
	 * Checkbox for showing residue index in node label.
	 */
	private JCheckBox cbxLabelIndex;

	/**
	 * Checkbox for showing iCode in node label.
	 */
	private JCheckBox cbxLabelICode;

	/**
	 * Checkbox for showing residue type in node label.
	 */
	private JCheckBox cbxLabelType;

	/**
	 * Radio button for 1-letter code for residue type.
	 */
	private JRadioButton rbOneLetterCode;

	/**
	 * Radio button for 3-letter code for residue type.
	 */
	private JRadioButton rbThreeLetterCode;

	/**
	 * Example label of a node.
	 */
	private JLabel exampleLabel;

	/**
	 * Edge properties panel.
	 */
	private JPanel panEdge;

	/**
	 * Node properties panel.
	 */
	private JPanel panNode;

	/**
	 * Backbone edge width spinner.
	 */
	private JSpinner spinnerBBEdgeWidth;

	/**
	 * Edge width spinner.
	 */
	private JSpinner spinnerEdgeWidth;

	private JSpinner spinnerEdgeDistFilter;

	/**
	 * Edge space spinner.
	 */
	private JSpinner spinnerEdgeSpace;

	/**
	 * Node label size spinner.
	 */
	private JSpinner spinnerLabelSize;

	/**
	 * Node size spinner.
	 */
	private JSpinner spinnerNodeSize;

	/**
	 * Border size.
	 */
	private static int BS;

	/**
	 * Maximal allowed edge width.
	 */
	private static int MAXEDGEDIST = 100;

	/**
	 * Maximal allowed edge width.
	 */
	private static int MAXEDGEWIDTH = 15;

	/**
	 * Maximal allowed label size.
	 */
	private static int MAXLABELSIZE = 40;

	/**
	 * Maximal allowed node size.
	 */
	private static int MAXNODESIZE = 99;

	/**
	 * Example label.
	 */
	private static String EXAMPLE_LABEL = "pdb1abc:A:1:_:GLY";

	/**
	 * Array with the example label parts.
	 */
	private static String[] EXAMPLE_LABEL_PARTS = EXAMPLE_LABEL.split(":");

}
