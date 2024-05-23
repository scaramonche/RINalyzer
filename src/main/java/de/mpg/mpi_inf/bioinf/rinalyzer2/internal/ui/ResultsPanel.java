package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ResultData;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ResultsFileFilter;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.io.ResultsWriter;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.UtilsIO;

/**
 * Class responsible for the display of the computed centrality measures (including selection filter
 * and save, show and visualize buttons) in the &quot;Results&quot; panel of Cytoscape.
 * 
 * @author Nadezhda Doncheva
 */
// TODO: [Improve][Results] Change Save all button to a drop-down list
// with options as text file, as table/cvs, etc.
public class ResultsPanel extends JPanel implements CytoPanelComponent, ActionListener {

	/**
	 * Initializes a new instance of <code>ResultsPanel</code>.
	 * 
	 */
	public ResultsPanel(CyServiceRegistrar bc, ResultData aResultData) {
		super();
		resultData = aResultData;
		context = bc;
		initialized = false;
		filterPanels = new ArrayList<FilterPanel>();
		// analyzedNets = new HashMap<CyNetwork, ResultData>();
		centButtonListeners = new HashMap<String, CentButtonListener>(4);
		init();
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	public Component getComponent() {
		return this;
	}

	public Icon getIcon() {
		return null;
	}

	public String getTitle() {
		return Messages.DT_RESULTS;
	}

	/**
	 * Display the analysis results for the network with focused network view.
	 * 
	 * @param aResultData
	 *            Results to be displayed.
	 * @param newNetwork
	 *            Flag indicating if this network has just been analyzed and the results have to be
	 *            stored in the <code>analyzedNets</code> map.
	 */
	// public void displayResults(ResultData aResultData, boolean newNetwork) {
	// resultData = aResultData;
	// save results for this network if it is a new one.
	// if (newNetwork) {
	// analyzedNets.put(network, resultData);
	// }
	// init();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnSelectNodes) {
			CyUtils.selectNodes(context, resultData.getNetwork(), resultData.getSelectedNodes());
		} else if (src == btnShowAll) {
			showAllData();
		} else if (src == btnSaveAll) {
			saveAllData();
		} else if (src == btnClose) {
			CyServiceRegistrar registrar = (CyServiceRegistrar) CyUtils.getService(context,
					CyServiceRegistrar.class);
			registrar.unregisterService(this, CytoPanelComponent.class);
		} else if (src == btnHelp) {
			((OpenBrowser) CyUtils.getService(context, OpenBrowser.class))
					.openURL(Messages.HELP_MEASURES);
		} else if (src == btnAnalysisSettings) {
			final AnalysisInfoDialog d = new AnalysisInfoDialog(CyUtils.getCyFrame(context),
					resultData.getAnalysisSettings());
			d.setVisible(true);
		}
	}

	/**
	 * Show all computed centrality measures in the <code>ResultsDialog</code>.
	 */
	private void showAllData() {
		ResultsDialog d = new ResultsDialog(CyUtils.getCyFrame(context), resultData);
		d.setLocationRelativeTo(CyUtils.getCyFrame(context));
		d.setVisible(true);
	}

	/**
	 * Save all computed centrality measures in a file.
	 */
	private void saveAllData() {
		try {
			final JFileChooser chooser = new JFileChooser(FileUtil.LAST_DIRECTORY);
			chooser.setFileFilter(new ResultsFileFilter());
			final File file = UtilsIO.getFileToSave(this, chooser, Messages.EXT_RINSTATS);
			if (file != null) {
				ResultsWriter.save(resultData, file);
			}
		} catch (Exception ex) {
			// Could not save file
			JOptionPane.showMessageDialog(this, Messages.SM_IOERRORSAVE, Messages.DT_ERROR,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Initializes the <code>ResultsPanel</code> panel.
	 */
	private void init() {
		final int BS = UtilsUI.BORDER_SIZE;
		this.setLayout(new BorderLayout(BS, BS));
		this.removeAll();
		final JComponent panResults = new Box(BoxLayout.PAGE_AXIS);
		final List<String> computed = resultData.getComputed();

		final JPanel panTop = new JPanel();
		panTop.setBorder(new TitledBorder(null, Messages.DI_ANALYSIS_INFO, TitledBorder.CENTER,
				TitledBorder.DEFAULT_POSITION));
		final GridBagLayout gbl = new GridBagLayout();
		panTop.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(BS / 2, BS / 2, BS / 2, BS / 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;

		// add general info about analysis' success:
		final JPanel panTitle = new JPanel(new FlowLayout(FlowLayout.LEADING));
		String title = null;
		if (computed.size() > 0) {
			title = Messages.DI_RESULTS_TITLE1 + resultData.getNetworkTitle()
					+ Messages.DI_RESULTS_TITLE2;
		} else {
			title = Messages.DI_NORESULTS_TITLE1 + resultData.getNetworkTitle()
					+ Messages.DI_NORESULTS_TITLE2;
		}
		panTitle.add(UtilsUI.createLabel(title, null));
		panTop.add(panTitle, gbc);
		gbc.gridy++;

		// create buttons and adjust their width
		btnSelectNodes = UtilsUI.createButton(Messages.DI_ANALYSIS_NODES, Messages.TT_ANALYSIS_SET,
				null, this);
		btnAnalysisSettings = UtilsUI.createButton(Messages.DI_ANALYSIS_SETTINGS,
				Messages.TT_ANALYSIS_SETTINGS, null, this);
		// UtilsUI.adjustWidth(new JButton[] { btnSelectNodes,
		// btnAnalysisSettings });
		btnShowAll = UtilsUI.createButton(Messages.DI_SHOW_ALL, Messages.TT_SHOW_ALL, null, this);
		btnSaveAll = UtilsUI.createButton(Messages.DI_SAVE_ALL, Messages.TT_SAVE_ALL, null, this);
		btnHelp = UtilsUI.createButton(Messages.DI_HELP, null, null, this);
		btnClose = UtilsUI.createButton(Messages.DI_CLOSE, null, null, this);
		UtilsUI.adjustWidth(new JButton[] { btnSelectNodes, btnAnalysisSettings, btnShowAll,
				btnSaveAll });

		// add selected nodes
		gbc.anchor = GridBagConstraints.CENTER;
		final JPanel panAnalysisBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 2 * BS, 0));
		final JPanel panAnalysisSet = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panAnalysisSet.add(btnSelectNodes);
		panAnalysisBtns.add(panAnalysisSet);
		// add other analysis options
		final JPanel panAnalysisOptions = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panAnalysisOptions.add(btnAnalysisSettings);
		panAnalysisBtns.add(panAnalysisOptions);
		panTop.add(panAnalysisBtns, gbc);
		gbc.gridy++;

		// add computed measures title
		gbc.anchor = GridBagConstraints.LINE_START;
		final JPanel panMeasures = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panMeasures.add(UtilsUI.createLabel(Messages.DI_COMPUTED, null));
		panTop.add(panMeasures, gbc);
		gbc.gridy++;

		// add shared buttons
		gbc.anchor = GridBagConstraints.CENTER;
		final JPanel panButtonsAll = new JPanel(new FlowLayout(FlowLayout.CENTER, 2 * BS, 0));
		final JPanel panButtonsLeft = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		final JPanel panButtonsRight = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panButtonsLeft.add(btnShowAll);
		panButtonsRight.add(btnSaveAll);
		panButtonsAll.add(panButtonsLeft);
		panButtonsAll.add(panButtonsRight);
		panTop.add(panButtonsAll, gbc);
		UtilsUI.limitHeight(panTop);
		panResults.add(panTop);

		for (final String centName : computed) {
			final JPanel panCent = new JPanel(new BorderLayout());
			final String centTitle = centName + Messages.DI_FILTERING;
			panCent.setBorder(new TitledBorder(null, centTitle, TitledBorder.CENTER,
					TitledBorder.DEFAULT_POSITION));

			// display filter only if min != max value.
			if (!resultData.getMin(centName).equals(resultData.getMax(centName))) {
				final FilterPanel panFilter = new FilterPanel(context,
						resultData.getCentralty(centName), resultData.getMinMeanMaxData(centName),
						resultData.getNetwork());
				panCent.add(panFilter, BorderLayout.CENTER);
				final Dimension dim = panFilter.getPreferredSize();
				panFilter.setPreferredSize(new Dimension(panCent.getPreferredSize().width,
						dim.height));
				filterPanels.add(panFilter);
			} else {
				final JPanel panNoFilter = new JPanel(new FlowLayout(FlowLayout.CENTER));
				panNoFilter.add(UtilsUI.createLabel(Messages.DI_RESULTS_NOFILTER, null));
				panCent.add(panNoFilter, BorderLayout.CENTER);
			}

			final JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
			final JPanel panButtons = new JPanel(new GridLayout(1, 2, 2 * BS, BS));
			CentButtonListener l = new CentButtonListener(this, centName,
					resultData.getCentralty(centName), resultData.getNetwork(),
					resultData.getSelectedNodes());
			centButtonListeners.put(centName, l);
			final JButton btnShow = UtilsUI.createButton(Messages.DI_SHOW_CENT,
					Messages.TT_SHOW_CENT, null, l);
			l.setBtnShow(btnShow);
			panButtons.add(btnShow);
			final JButton btnSave = UtilsUI.createButton(Messages.DI_SAVE_CENT,
					Messages.TT_SAVE_CENT, null, l);
			l.setBtnSave(btnSave);
			panButtons.add(btnSave);
			panBottom.add(panButtons);
			panCent.add(panBottom, BorderLayout.SOUTH);
			UtilsUI.limitHeight(panCent);
			panResults.add(panCent);
		}

		final JPanel panHelp = new JPanel(new BorderLayout(BS, BS));
		panHelp.add(btnClose, BorderLayout.LINE_START);
		panHelp.add(btnHelp, BorderLayout.LINE_END);
		UtilsUI.limitHeight(panHelp);
		panResults.add(panHelp);

		panResults.add(Box.createGlue());
		final JScrollPane scrollPane = new JScrollPane(panResults);
		this.add(scrollPane, BorderLayout.CENTER);
		this.updateUI();
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -1360699239129380693L;

	/**
	 * The {@link CytoscapeDesktop} instance.
	 */
	// private CytoscapeDesktop desktop;

	/**
	 * All data of the computed centrality measures.
	 */
	private ResultData resultData;

	/**
	 * &quot;Select&quot; button.
	 */
	private JButton btnSelectNodes;

	/**
	 * &quot;Show&quot; button.
	 */
	private JButton btnAnalysisSettings;

	/**
	 * &quot;Show All&quot; button.
	 */
	private JButton btnShowAll;

	/**
	 * &quot;Save All&quot; button.
	 */
	private JButton btnSaveAll;

	/**
	 * &quot;Help&quot; button.
	 */
	private JButton btnHelp;

	/**
	 * &quot;Close&quot; button.
	 */
	private JButton btnClose;

	/**
	 * Flag indicating if the <code>ResultsPanel</code> has been initialized.
	 */
	boolean initialized;

	/**
	 * List with the panels of all centrality filters. Should have the same length as
	 * resultData.getComputed().
	 */
	private List<FilterPanel> filterPanels;

	/**
	 * Map of all analyzed networks with the analysis results. For each network only one analysis
	 * result is stored. If the network is analyzed again the old results are overwritten.
	 */
	// private Map<CyNetwork, ResultData> analyzedNets;

	/**
	 * Map of all centrality button colorListeners indexed by centrality name. This map is used for
	 * switching between the results of different networks.
	 */
	private Map<String, CentButtonListener> centButtonListeners;

	private CyServiceRegistrar context;
}
