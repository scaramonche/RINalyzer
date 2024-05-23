package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.FileUtil;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ResultsFileFilter;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.io.ResultsWriter;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.UtilsIO;

/**
 * Listener for a set of buttons, attached to the result's display of each
 * computed centrality measure.
 * 
 * @author Nadezhda Doncheva
 */

public class CentButtonListener implements ActionListener {

	/**
	 * Initializes a new instance of <code>CentButtonListener</code>.
	 * 
	 * @param aCentName
	 *            Name of the displayed centrality measure.
	 * @param aCentData
	 *            Computed data of the displayed centrality measure.
	 * @param aNetworkID
	 *            Network id of the analyzed network.
	 * @param aSelected
	 *            Set of the selected nodes used for analysis.
	 */
	public CentButtonListener(Component aDesktop, String aCentName, Map<CyNode, Double> aCentData,
			CyNetwork aNetwork, Set<CyNode> aSelected) {
		desktop = aDesktop;
		centName = aCentName;
		centData = aCentData;
		network = aNetwork;
		selected = aSelected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		if (src == btnSave) {
			saveData();
		} else if (src == btnShow) {
			showData();
		}
	}

	/**
	 * Save the centrality data in a file.
	 */
	private void saveData() {
		try {
			final JFileChooser chooser = new JFileChooser(FileUtil.LAST_DIRECTORY);
			chooser.setFileFilter(new ResultsFileFilter());
			final File file = UtilsIO.getFileToSave(desktop, chooser, Messages.EXT_RINSTATS);
			if (file != null) {
				ResultsWriter.save(centData, centName, network, selected, file);
			}
		} catch (IOException ex) {
			// Could not save file
			JOptionPane.showMessageDialog(desktop, Messages.SM_IOERRORSAVE, Messages.DT_ERROR,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Show the centrality data as a list in a separate window.
	 */
	private void showData() {
		final List<String> computed = new ArrayList<String>(1);
		computed.add(centName);
		ResultsDialog d = new ResultsDialog(null, centData, computed, network);
		d.setLocationRelativeTo(null);
		d.setVisible(true);
	}

	/**
	 * Set the save button {@link CentButtonListener#btnSave}.
	 * 
	 * @param aBtnSave
	 *            JButton to be set.
	 */
	public void setBtnSave(JButton aBtnSave) {
		btnSave = aBtnSave;
	}

	/**
	 * Set the show button {@link CentButtonListener#btnShow}.
	 * 
	 * @param aBtnShow
	 *            JButton to be set.
	 */
	public void setBtnShow(JButton aBtnShow) {
		btnShow = aBtnShow;
	}

	/**
	 * Set new centrality data.
	 * 
	 * @param aCentData
	 *            New centrality data.
	 */
	public void setCentData(Map<CyNode, Double> aCentData) {
		centData = aCentData;
	}

	/**
	 * Set new network identifier.
	 * 
	 * @param aNetworkID
	 *            New network identifier.
	 */
	public void setNetwork(CyNetwork aNetwork) {
		network = aNetwork;
	}

	/**
	 * Set new set of nodes.
	 * 
	 * @param aSelected
	 *            New set of nodes.
	 */
	public void setSelected(Set<CyNode> aSelected) {
		selected = aSelected;
	}

	/**
	 * The {@link CytoscapeDesktop} instance.
	 */
	private Component desktop;

	/**
	 * The computed centrality data stored as a map.
	 */
	private Map<CyNode, Double> centData;

	/**
	 * Name of the computed centrality measure.
	 */
	private String centName;

	/**
	 * Analyzed network.
	 */
	private CyNetwork network;

	/**
	 * Set of the selected nodes used for the analysis.
	 */
	private Set<CyNode> selected;

	/**
	 * &quot;Save&quot; button.
	 */
	private JButton btnSave;

	/**
	 * &quot;Show&quot; button.
	 */
	private JButton btnShow;

}
