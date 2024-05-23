package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

/**
 * Class for checking RIN (node) format and grouping information about the RIN contained in the
 * network nodes, e.g. chain IDs.
 * 
 * @author Nadezhda Doncheva
 */
// TODO: [Improve][General] Adapt to more than one pdb in a network
public class RINFormatChecker {

	/**
	 * Create a new instance of <code>RINFormatChecker</code>.
	 * 
	 * @param aNetwork
	 *            The network to be checked.
	 */
	public RINFormatChecker(CyNetwork aNetwork, Map<CyNode, String[]> aNodeLabelMap) {
		pdbID = null;
		chainIDs = new TreeSet<String>();
		chainNodes = new HashMap<String, Set<CyNode>>(4);
		res2node = new TreeMap<String, CyNode>(new AlphanumComparator());
		node2labelMap = aNodeLabelMap;
		status = null;
		checkRIN();
	}

	public String getErrorStatus() {
		return status;
	}

	/**
	 * Ensure that the network nodes have RIN format by checking the node labels. Return
	 * <code>null</code> if everything is ok or a message with the error.
	 * 
	 * @return An error message. If <code>null</code> the network has RIN format.
	 */
	private void checkRIN() {
		final Set<CyNode> nodeSet = node2labelMap.keySet();
		for (final CyNode node : nodeSet) {
			final String[] labels = node2labelMap.get(node);
			// check pdb id
			if (labels[0] != null) {
				if (pdbID == null) {
					pdbID = labels[0];
				} else {
					if (!pdbID.equals(labels[0])) {
						status = Messages.SM_INVFORMAT_PDB;
					}
				}
			}
			// look for chains
			if (labels[1] == "") {
				status = Messages.SM_INVFORMAT_CHAIN;
			}
			// if there is a chain id, store it
			if (!chainIDs.contains(labels[1])) {
				chainIDs.add(labels[1]);
				chainNodes.put(labels[1], new HashSet<CyNode>());
			}
			chainNodes.get(labels[1]).add(node);
			final String resKey = labels[1] + ":" + labels[2] + ":" + labels[3] + ":" + labels[4];
			if (!res2node.containsKey(resKey)) {
				res2node.put(resKey, node);
			} else {
				status = Messages.SM_INVFORMAT_RES;	
			}
		}
	}

	/**
	 * Get the IDs of all chains contained in this RIN.
	 * 
	 * @return Set of chain IDs.
	 */
	public Set<String> getChainIDs() {
		return chainIDs;
	}

	/**
	 * Get a map of chains and the corresponding nodes contained in each chain.
	 * 
	 * @return Map of chains and nodes.
	 */
	public Map<String, Set<CyNode>> getChainNodes() {
		return chainNodes;
	}

	/**
	 * Get all nodes belonging to chain <code>chainID</code>.
	 * 
	 * @param chainID
	 *            ID of the chain whose nodes are to retrieved.
	 * @return A set of nodes belonging to the chain <code>chainID</code> or <code>null</code> if
	 *         there is no such chain in this RIN.
	 */
	public Set<CyNode> getChainNodes(String chainID) {
		if (chainNodes.containsKey(chainID)) {
			return chainNodes.get(chainID);
		}
		return null;
	}

	/**
	 * Get a map of all nodes and their corresponding node labels split by ":".
	 * 
	 * @return Map of nodes and node label parts.
	 */
	public Map<CyNode, String[]> getNodeLabels() {
		return node2labelMap;
	}

	/**
	 * Get map of short residue id (resChain:resIndex) and nodes.
	 * 
	 * @return Sorted map of short residue ids and nodes.
	 */
	public TreeMap<String, CyNode> getResNodeMap() {
		return res2node;
	}

	/**
	 * Get pdb ID of this RIN.
	 * 
	 * @return PDB ID of this RIN.
	 */
	public String getPdbID() {
		return pdbID;
	}

	/**
	 * Map of nodes and their labels split in the respective parts (pdb id, chain id, res index,
	 * icode, res type).
	 */
	private Map<CyNode, String[]> node2labelMap;

	/**
	 * PDB identifier of this network.
	 */
	private String pdbID;

	/**
	 * Set of chain identifiers in this network, ordered alphabetically.
	 */
	private Set<String> chainIDs;

	/**
	 * Map of chain identifiers and the nodes contained in each chain.
	 */
	private Map<String, Set<CyNode>> chainNodes;

	/**
	 * Map of residues (chainID:resIndex) and nodes.
	 */
	private TreeMap<String, CyNode> res2node;

	private String status;
}
