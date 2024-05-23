package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ResultData;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Dialog displaying a list of the values of either one or more centrality measures at once. If they
 * are more than one measures showed, they are separated in different tabs.
 * 
 * @author Nadezhda Doncheva
 */
public class ResultsDialog extends JDialog implements ActionListener {

	/**
	 * Initializes a new instance of <code>ResultsDialog</code> for the display of all computed
	 * centrality measures.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aResultData
	 *            The centrality data of all computed measures.
	 */
	public ResultsDialog(Frame aOwner, ResultData aResultData) {
		super(aOwner, Messages.DT_RESULTS, true);
		final List<String> computed = aResultData.getComputed();
		final List<Map<CyNode, Double>> centData = new ArrayList<Map<CyNode, Double>>(
				computed.size());
		for (final String centrName : computed) {
			centData.add(aResultData.getCentralty(centrName));
		}
		model = new ResultsTableModel(getRowData(centData, aResultData.getNetwork()),
				getColumnNames(computed));
		init();
	}

	/**
	 * Initializes a new instance of <code>ResultsDialog</code> for the display of one computed
	 * centrality measure.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aCentData
	 *            Map of the values of a computed centrality measure.
	 * @param aComputed
	 *            List containing the name of one computed centrality measure, which is to be
	 *            displayed.
	 */
	public ResultsDialog(Frame aOwner, Map<CyNode, Double> aCentData, List<String> aComputed,
			CyNetwork aNetwork) {
		super(aOwner, Messages.DT_RESULTS, true);
		final List<Map<CyNode, Double>> centData = new ArrayList<Map<CyNode, Double>>(
				aComputed.size());
		centData.add(aCentData);
		model = new ResultsTableModel(getRowData(centData, aNetwork), getColumnNames(aComputed));
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnClose) {
			setVisible(false);
			dispose();
		}
	}

	/**
	 * Define the column names for the table.
	 * 
	 * @param computed
	 *            List containing the names of the computed centrality measures, i.e. the column
	 *            names.
	 */
	private String[] getColumnNames(List<String> computed) {
		final String[] columnNames = new String[computed.size() + 1];
		columnNames[0] = Messages.CENT_NODEID;
		for (int i = 0; i < computed.size(); i++) {
			columnNames[i + 1] = computed.get(i);
		}
		return columnNames;
	}

	/**
	 * Get all centrality values as an array.
	 * 
	 * @param aCentData
	 *            Map of the values of computed centrality measures.
	 * @param aComputed
	 *            List containing the names of one computed centrality measure, which is to be
	 *            displayed.
	 */
	private String[][] getRowData(List<Map<CyNode, Double>> centData, CyNetwork aNetwork) {
		final String[][] rowData = new String[centData.get(0).size()][centData.size() + 1];
		final List<CyNode> nodes = new ArrayList<CyNode>(centData.get(0).keySet());
		Collections.sort(nodes, new CyNodeComparator(aNetwork));
		int j = 0;
		for (final CyNode node : nodes) {
			rowData[j][0] = CyUtils.getCyName(aNetwork, node);
			for (int i = 0; i < centData.size(); i++) {
				rowData[j][i + 1] = UtilsUI
						.getSixDigits(centData.get(i).get(node), RoundingMode.UP);
			}
			j++;
		}
		return rowData;
	}

	/**
	 * Initializes the <code>ResultsDialog</code>.
	 */
	private void init() {
		final int BS = UtilsUI.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		contentPane.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));

		final JTable table = new JTable(model);
		// table.setAutoCreateRowSorter(true);
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				model.sortByColumn(table.columnAtPoint(evt.getPoint()));
			}
		});
		table.getTableHeader().setDefaultRenderer(new ResultsTableHeaderRenderer());
		model.sortByColumn(0);

		final JScrollPane scrollPane = new JScrollPane(table);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// add OK and Cancel buttons
		final JPanel panBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		btnClose = UtilsUI.createButton(Messages.DI_CLOSE, null, null, this);
		panBottom.add(btnClose);
		contentPane.add(panBottom, BorderLayout.SOUTH);

		setContentPane(contentPane);
		pack();
	}

	class CyNodeComparator implements Comparator<CyNode> {

		private CyNetwork network;

		public CyNodeComparator(CyNetwork network) {
			this.network = network;
		}

		public int compare(CyNode n1, CyNode n2) {
			return CyUtils.getCyName(network, n1).compareTo(CyUtils.getCyName(network, n2));
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 406154913728837354L;

	/**
	 * &quot;Cancel&quot; button.
	 */
	private JButton btnClose;

	/**
	 * Table model containing the centrality data to be visualized.
	 */
	protected ResultsTableModel model;
}
