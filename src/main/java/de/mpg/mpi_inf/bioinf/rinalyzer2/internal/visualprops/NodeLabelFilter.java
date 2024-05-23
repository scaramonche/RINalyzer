package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops;

import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Manages the node label representation of RIN.
 * 
 * @author Nadezhda Doncheva
 */
public class NodeLabelFilter {

	/**
	 * Initializes a new instance of <code>NodeLabelFilter</code>.
	 * 
	 * @param aNetwork
	 *          Network whose labels has to be shown.
	 */
	public NodeLabelFilter(CyNetwork aNetwork) {
		network = aNetwork;
		CyTable nodeTable = network.getDefaultNodeTable();
		if (nodeTable.getColumn(Messages.DI_LABEL) == null) {
			nodeTable.createColumn(Messages.DI_LABEL, String.class, false, "");
		}
		if (nodeTable.getColumn(Messages.SV_RINRESIDUE) != null) {
			nodeLabels = CyUtils.splitNodeLabels(network, Messages.SV_RINRESIDUE);
		} else {
			nodeLabels = CyUtils.splitNodeLabels(network, CyNetwork.NAME);
		}
		initEnabledLabels();
		selectedLabels = new boolean[5];
		for (int i = 0; i < selectedLabels.length; i++) {
			selectedLabels[i] = false;
		}
		threeLetterCode = true;
	}

	/**
	 * Create a node attribute called <code>aAttr</code> storing the modified node label.
	 * 
	 * @param aSeparator
	 *          String that separates the different parts of the label.
	 * @param aAttr
	 *          Name of the attribute that stores the modified label.
	 */
	public void createLabelAttr(String aSeparator, String aAttr) {
		if (network.getDefaultNodeTable().getColumn(aAttr) == null) {
			network.getDefaultNodeTable().createColumn(aAttr, String.class, false);
		}
		for (final Map.Entry<CyNode, String[]> entry : nodeLabels.entrySet()) {
			final CyNode node = entry.getKey();
			final String[] labelList = entry.getValue();
			String label = null;
			if (network.getDefaultNodeTable().getColumn(Messages.SV_RINRESIDUE) != null) {
				label = CyUtils.getLabel(aSeparator, selectedLabels, labelList,
						CyUtils.getCyName(network, node, Messages.SV_RINRESIDUE), threeLetterCode);
			} else {
				label = CyUtils.getLabel(aSeparator, selectedLabels, labelList,
						CyUtils.getCyName(network, node), threeLetterCode);
			}
			if (network.containsNode(node)) {
				network.getRow(node).set(aAttr, label);
			}
		}
	}

	/**
	 * Get the array with "selected" flags for the different label parts. It has always five elements
	 * in this order: pdb id, chain id, residue index, iCode, residue type. If the flag is
	 * <code>true</code>, the respective label part is shown.
	 * 
	 * @return Array with "selected" flags for the different label parts.
	 */
	public boolean[] getSelected() {
		return selectedLabels;
	}

	/**
	 * Get the array with the "enabled" flags for the different label parts. It has always five
	 * elements in this order: pdb id, chain id, residue index, iCode, residue type. If the flag is
	 * <code>true</code>, the respective label part can be selected to be shown.
	 * 
	 * @return Array with "enabled" flags for the different label parts.
	 */
	public boolean[] getEnabled() {
		return enabledLabels;
	}

	/**
	 * Check whether residue identifiers are displayed in 1- or 3-letter code.
	 * 
	 * @return <code>true</code> if the residue identifiers are in 3-letter code, and
	 *         <code>false</code> otherwise.
	 */
	public boolean getThreLetterCode() {
		return threeLetterCode;
	}

	/**
	 * Set a flag indicating whether residue identifiers are displayed in 1- or 3-letter code.
	 * 
	 * @param isThreeLetterCode
	 *          Flag is set to <code>true</code> if the residue identifiers are in 3-letter code, and
	 *          to <code>false</code> otherwise.
	 */
	public void setThreeLetterCode(boolean isThreeLetterCode) {
		threeLetterCode = isThreeLetterCode;
	}

	/**
	 * Set the array with "selected" flags for the different label parts (pdb id, chain id, residue
	 * index, iCode, residue type).
	 * 
	 * @param aSelected
	 *          Array with "selected" flags for the different label parts.
	 */
	public void setSelectedLabels(boolean[] aSelected) {
		selectedLabels = aSelected;
	}

	/**
	 * Initialize the "enabled" flags for the label parts (pdb id, chain id, residue index, iCode,
	 * residue type). A part can be viewed if its flag is set to <code>true</code>.
	 */
	private void initEnabledLabels() {
		enabledLabels = new boolean[5];
		for (int i = 0; i < enabledLabels.length; i++) {
			enabledLabels[i] = checkLabelPart(i);
		}
	}

	/**
	 * Check if all nodes have this part of the label.
	 * 
	 * @param index
	 *          Index of the label part (max 4).
	 * @return <code>true</code> if all nodes have this part of the label and <code>false</code>
	 *         otherwise.
	 */
	private boolean checkLabelPart(int index) {
		final Set<CyNode> nodes = nodeLabels.keySet();
		for (final CyNode node : nodes) {
			if (nodeLabels.get(node)[index] == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Map of nodes and their node labels, split and stored in a string array.
	 */
	private Map<CyNode, String[]> nodeLabels;

	private CyNetwork network;

	/**
	 * Array with "enabled" flags or the different label parts (pdb id, chain id, residue index,
	 * iCode, residue type).
	 */
	private boolean[] enabledLabels;

	/**
	 * Array with "selected" flags of the different label parts (pdb id, chain id, residue index,
	 * iCode, residue type).
	 */
	private boolean[] selectedLabels;

	/**
	 * Flag indicating that residue type is displayed using 3-letter code.
	 */
	private boolean threeLetterCode;
}
