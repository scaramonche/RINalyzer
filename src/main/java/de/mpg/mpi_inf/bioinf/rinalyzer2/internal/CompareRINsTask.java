package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.ChimUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.ParseUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINComparator;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

// TODO: [Improve][Comparison] Compute difference for all numeric edge (node?) attributes 
public class CompareRINsTask extends AbstractTask implements TaskObserver {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.CompareRINsTask.class);

	private CyServiceRegistrar context;
	private CyNetworkManager netManager;
	private RINVisualPropertiesManager rinVisPropManager;
	private Map<String, String> resMapping;
	private boolean commandSend;
	private boolean taskFinished;

	private Map<String, ListSingleSelection<String>> nodeNames1;
	private Map<String, ListSingleSelection<String>> edgeWeights1;

	private Map<String, ListSingleSelection<String>> nodeNames2;
	private Map<String, ListSingleSelection<String>> edgeWeights2;

	@Tunable(description = "Enter name", groups = "Comparison network settings", gravity = 1.1, tooltip = "Enter name for the comparison network")
	public String newNetName;

	@Tunable(description = "Consider edge weights", groups = "Comparison network settings", gravity = 1.2, tooltip = "Check if edge weights should be included in the comparison")
	public boolean considerWeights;

	@Tunable(description = "Compare weights using", groups = "Comparison network settings", gravity = 1.3, dependsOn = "considerWeights=true", tooltip = "Choose how to compare the weights")
	public ListSingleSelection<String> weightTransform;

	public ListSingleSelection<String> firstNetworkNames = null;

	@Tunable(description = "Select network", groups = "First (reference) network settings", gravity = 2.1, tooltip = "Select the reference network in the comparison")
	public ListSingleSelection<String> getFirstNetworkNames() {
		return firstNetworkNames;
	}

	public void setFirstNetworkNames(ListSingleSelection<String> input) {
	}

	public ListSingleSelection<String> firstNameAttribute = null;

	@Tunable(description = "Select node name attribute", groups = "First (reference) network settings", gravity = 2.2, listenForChange = "FirstNetworkNames", tooltip = "Select an attribute in the reference network to be used for mapping node names")
	public ListSingleSelection<String> getFirstNameAttribute() {
		return nodeNames1.get(firstNetworkNames.getSelectedValue());
	}

	public void setFirstNameAttribute(ListSingleSelection<String> input) {
		// ignore
	}

	public ListSingleSelection<String> firstWeightAttribute = null;

	@Tunable(description = "Select edge weight attribute", groups = "First (reference) network settings", gravity = 2.3, dependsOn = "considerWeights=true", listenForChange = "FirstNetworkNames", tooltip = "Select an attribute in the reference network to be used for edge weights")
	public ListSingleSelection<String> getFirstWeightAttribute() {
		return edgeWeights1.get(firstNetworkNames.getSelectedValue());
	}

	public void setFirstWeightAttribute(ListSingleSelection<String> input) {
		// ignore
	}

	public ListSingleSelection<String> secondNetworkNames = null;

	@Tunable(description = "Select network", groups = "Second (match) network settings", gravity = 3.1, tooltip = "Select network to be matched to the reference network in the comparison")
	public ListSingleSelection<String> getSecondNetworkNames() {
		return secondNetworkNames;
	}

	public void setSecondNetworkNames(ListSingleSelection<String> input) {
	}

	public ListSingleSelection<String> secondNameAttribute = null;

	@Tunable(description = "Select node name attribute", groups = "Second (match) network settings", gravity = 3.2, listenForChange = "SecondNetworkNames", tooltip = "Select an attribute in the match network to be used for mapping node names")
	public ListSingleSelection<String> getSecondNameAttribute() {
		// return secondNameAttribute;
		return nodeNames2.get(secondNetworkNames.getSelectedValue());
	}

	public void setSecondNameAttribute(ListSingleSelection<String> input) {
		// ignore
	}

	public ListSingleSelection<String> secondWeightAttribute = null;

	@Tunable(description = "Select edge weight attribute", groups = "Second (match) network settings", gravity = 3.3, dependsOn = "considerWeights=true", listenForChange = "SecondNetworkNames", tooltip = "Select an attribute in the match network to be used for edge weights")
	public ListSingleSelection<String> getSecondWeightAttribute() {
		// return secondWeightAttribute;
		return edgeWeights2.get(secondNetworkNames.getSelectedValue());
	}

	public void setSecondWeightAttribute(ListSingleSelection<String> input) {
		// ignore
	}

	@Tunable(description = "Select first (reference) chain", groups = "Structure alignment in UCSF Chimera", gravity = 4.1, tooltip = "Select referenece chain that is also associated with the reference (first) network")
	public ListSingleSelection<String> firstModelNames;

	@Tunable(description = "Select second (match) chain", groups = "Structure alignment in UCSF Chimera", gravity = 4.2, tooltip = "Select match model that is also associated with the match (second) network")
	public ListSingleSelection<String> secondModelNames;

	@Tunable(description = "Align chains in Chimera (using MatchMaker)", groups = "Structure alignment in UCSF Chimera", gravity = 4.3, tooltip = "Aligns the chains selected as first and second model")
	public boolean alignModels;

	@Tunable(description = "Get mapping from the sequence alignment", groups = "Structure alignment in UCSF Chimera", gravity = 4.4, tooltip = "Use the MatchMaker output to map the nodes from the reference and match network")
	public boolean getSeqAlignMap;

	// @Tunable(description = "Consider structure alignment", groups =
	// "Structure alignment in UCSF Chimera", gravity = 4.5)
	public boolean getStrAlignMap;

	@Tunable(description = "Mapping file", params = "fileCategory=unspecified;input=true", groups = "External mapping settings", gravity = 5.0, tooltip = "Select a tab-separated text / AFASTA / XML alignment file containing a node-to-node mapping. If left empty, the node names are mapped to each other.")
	public File mappingFile;

	public CompareRINsTask(CyServiceRegistrar aContext,
			RINVisualPropertiesManager rinVisPropManager, Set<CyNetwork> aNetSet) {
		this(aContext, rinVisPropManager, aNetSet, new ArrayList<String>());
	}

	public CompareRINsTask(CyServiceRegistrar aContext,
			RINVisualPropertiesManager rinVisPropManager, Set<CyNetwork> aNetSet,
			List<String> chains) {
		context = aContext;
		this.rinVisPropManager = rinVisPropManager;
		resMapping = new HashMap<String, String>();
		commandSend = false;
		taskFinished = false;
		newNetName = "";
		considerWeights = false;
		weightTransform = new ListSingleSelection<String>(Messages.weightTransfList);

		netManager = (CyNetworkManager) CyUtils.getService(context, CyNetworkManager.class);
		List<String> netNames = CyUtils.getAllNetNames(netManager, aNetSet);

		firstNetworkNames = new ListSingleSelection<String>(netNames);
		firstNetworkNames.setSelectedValue(netNames.get(0));
		secondNetworkNames = new ListSingleSelection<String>(netNames);
		secondNetworkNames.setSelectedValue(netNames.get(1));

		nodeNames1 = new HashMap<String, ListSingleSelection<String>>();
		nodeNames2 = new HashMap<String, ListSingleSelection<String>>();
		edgeWeights1 = new HashMap<String, ListSingleSelection<String>>();
		edgeWeights2 = new HashMap<String, ListSingleSelection<String>>();
		initAttributeLists(aNetSet);

		firstNameAttribute = nodeNames1.get(netNames.get(0));
		secondNameAttribute = nodeNames2.get(netNames.get(1));
		firstWeightAttribute = edgeWeights1.get(netNames.get(0));
		secondWeightAttribute = edgeWeights2.get(netNames.get(1));

		alignModels = false;
		getStrAlignMap = false;
		getSeqAlignMap = false;
		mappingFile = null;

		if (chains.size() == 0) {
			chains.add(Messages.NONE);
		} else {
			alignModels = true;
			getSeqAlignMap = true;
		}
		firstModelNames = new ListSingleSelection<String>(chains);
		secondModelNames = new ListSingleSelection<String>(chains);
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		// JOptionPane.showMessageDialog(null, Messages.SM_LOADNETWORKS,
		// Messages.DT_ERROR,
		// JOptionPane.ERROR_MESSAGE);
		taskMonitor.setTitle(Messages.TM_COMPARERINS);

		// get the networks
		taskMonitor.setStatusMessage(Messages.TM_GETMAPPING);
		final CyNetwork firstNet = CyUtils.getNetwork(netManager,
				firstNetworkNames.getSelectedValue());
		final CyNetwork secondNet = CyUtils.getNetwork(netManager,
				secondNetworkNames.getSelectedValue());
		if (firstNet == null || secondNet == null || firstNet.equals(secondNet)) {
			logger.warn(Messages.LOG_SAMENETWORK);
			return;
		}

		// align models in Chimera and get residue to residue mapping
		if (alignModels) {
			String firstModel = firstModelNames.getSelectedValue();
			String secondModel = secondModelNames.getSelectedValue();
			if (!firstModel.equals(Messages.NONE) && !secondModel.equals(Messages.NONE)
					&& !firstModel.equals(secondModel)) {
				// align and get result
				commandSend = align(context, firstModel, secondModel);
				if (commandSend) {
					while (!taskFinished) {
						Thread.sleep(100);
					}
				}
				if (resMapping.size() == 0) {
					logger.warn(Messages.LOG_ALIGNFAIL);
				}
			}
		}

		// get a residue to residue mapping from file if not alignMap = true
		if (mappingFile != null && ((getSeqAlignMap && resMapping == null) || !getSeqAlignMap)) {
			if (mappingFile.getName().endsWith("xml")) {
				resMapping = ParseUtils.parseXMLFile(mappingFile);
			} else if (mappingFile.getName().endsWith("afasta")) {
				resMapping = ParseUtils.parseFASTAFile(mappingFile);
			} else if (mappingFile.getName().endsWith("txt")
					|| mappingFile.getName().endsWith("tsv")) {
				resMapping = ParseUtils.parseTXTFile(mappingFile);
			}
			if (resMapping.size() == 0) {
				logger.warn(Messages.LOG_MAPPINGFAIL);
			}
		}

		// get node to node mapping
		// System.out.println("Mapping of residues:" + resMapping.size());
		Map<CyNode, CyNode> nodeMapping = getResToNodeMapping(firstNet, secondNet, resMapping);
		if (nodeMapping.size() == 0) {
			taskMonitor.setStatusMessage(Messages.TM_MAPPINGEMPTY);
			logger.warn(Messages.TM_MAPPINGEMPTY);
			return;
		}

		taskMonitor.setStatusMessage(Messages.TM_CREATECOMPRIN);
		// perform comparison
		// System.out.println(nodeNames1.get(firstNetworkNames.getSelectedValue()).getSelectedValue()
		// + "\t" + edgeWeights1.get(firstNetworkNames.getSelectedValue()).getSelectedValue());
		// System.out.println(nodeNames2.get(secondNetworkNames.getSelectedValue()).getSelectedValue()
		// + "\t" + edgeWeights2.get(secondNetworkNames.getSelectedValue()).getSelectedValue());
		RINComparator rinComp = null;
		if (considerWeights) {
			rinComp = new RINComparator(context, rinVisPropManager, newNetName, firstNet,
					nodeNames1.get(firstNetworkNames.getSelectedValue()).getSelectedValue(),
					edgeWeights1.get(firstNetworkNames.getSelectedValue()).getSelectedValue(),
					secondNet, nodeNames2.get(secondNetworkNames.getSelectedValue())
							.getSelectedValue(), edgeWeights2.get(
							secondNetworkNames.getSelectedValue()).getSelectedValue(),
					weightTransform.getSelectedValue(), nodeMapping);
		} else {
			rinComp = new RINComparator(context, rinVisPropManager, newNetName, firstNet,
					nodeNames1.get(firstNetworkNames.getSelectedValue()).getSelectedValue(), null,
					secondNet, nodeNames2.get(secondNetworkNames.getSelectedValue())
							.getSelectedValue(), null, weightTransform.getSelectedValue(),
					nodeMapping);
		}
		rinComp.compare();
	}

	@ProvidesTitle
	public String getTitle() {
		return "RIN Comparison Options";
	}

	/**
	 * Initialize comparison by creating a node to node mapping between the two networks based on
	 * the information in the mapping file.
	 * 
	 * @param firstNetwork
	 *            First network for the comparison
	 * @param secondNetwork
	 *            Second network for the comparison
	 * @param mappingFiles
	 *            Set of files with mapping information
	 * @param nameAttr
	 *            Attribute to look for matching names
	 * @return Node to node mapping between the two networks. If a node is not in the map, it could
	 *         not be matched to a node in the other network.
	 */
	private Map<CyNode, CyNode> getResToNodeMapping(CyNetwork firstNetwork,
			CyNetwork secondNetwork, Map<String, String> resMapping) {
		boolean matchNodeNames = false;
		if (resMapping.size() == 0) {
			matchNodeNames = true;
		}
		// create node to node mapping
		Map<CyNode, CyNode> nodeMapping = new HashMap<CyNode, CyNode>();
		String nameAttr1 = nodeNames1.get(firstNetworkNames.getSelectedValue()).getSelectedValue();
		String nameAttr2 = nodeNames2.get(secondNetworkNames.getSelectedValue()).getSelectedValue();
		// System.out.println("Attribute " + nameAttr1 + " and " + nameAttr2);
		if (firstNetwork.getDefaultNodeTable().getColumn(nameAttr1) == null
				|| secondNetwork.getDefaultNodeTable().getColumn(nameAttr2) == null) {
			logger.warn("Attribute " + nameAttr1 + " or " + nameAttr2
					+ "does not exist. No mapping could be created.");
			return nodeMapping;
		}
		// get RIN format labels
		Map<CyNode, String[]> nodeLabelsFirst = CyUtils.splitNodeLabels(firstNetwork, nameAttr1);
		Map<CyNode, String[]> nodeLabelsSecond = CyUtils.splitNodeLabels(secondNetwork, nameAttr2);
		// go over all nodes
		for (CyNode node1 : firstNetwork.getNodeList()) {
			final String nodeName1 = firstNetwork.getRow(node1).get(nameAttr1, String.class);
			// [pdb id, chain id, residue index, iCode, residue type]
			final String resName1 = nodeLabelsFirst.get(node1)[1] + ":"
					+ nodeLabelsFirst.get(node1)[2];
			// if there is no mapping information or the mapping is based on
			// residue information
			if (matchNodeNames || resMapping.containsKey(resName1)) {
				for (CyNode node2 : secondNetwork.getNodeList()) {
					final String nodeName2 = secondNetwork.getRow(node2).get(nameAttr2,
							String.class);
					final String resName2 = nodeLabelsSecond.get(node2)[1] + ":"
							+ nodeLabelsSecond.get(node2)[2];
					if ((matchNodeNames && nodeName1.equals(nodeName2))
							|| (resMapping.containsKey(resName1) && resMapping.get(resName1)
									.equals(resName2))) {
						nodeMapping.put(node1, node2);
					}
				}
			} else if (resMapping.containsKey(nodeName1)) {
				// if the mapping is based just on node names, just read it in
				CyNode node2 = CyUtils.getNode(secondNetwork, resMapping.get(nodeName1), nameAttr2);
				if (node2 != null) {
					nodeMapping.put(node1, node2);
				}
			}
		}
		return nodeMapping;
	}

	private void initAttributeLists(Set<CyNetwork> aNetSet) {
		for (CyNetwork network : aNetSet) {
			String netName = network.getRow(network).get(CyNetwork.NAME, String.class);
			if (!nodeNames1.containsKey(netName)) {
				List<String> attrList = CyUtils.getStringAttributes(network, CyNode.class);
				if (attrList.size() == 0) {
					attrList.add("No attributes available");
				} else {
					if (attrList.indexOf("RINalyzerResidue") != -1) {
						attrList.set(attrList.indexOf("RINalyzerResidue"), attrList.get(0));
						attrList.set(0, "RINalyzerResidue");
					} else if (attrList.indexOf("name") != -1) {
						attrList.set(attrList.indexOf("name"), attrList.get(0));
						attrList.set(0, "name");
					}
				}
				nodeNames1.put(netName, new ListSingleSelection<String>(attrList));
			}
			if (!edgeWeights1.containsKey(netName)) {
				List<String> attrList2 = CyUtils.getNumericAttributes(network, CyEdge.class);
				if (attrList2.size() == 0) {
					attrList2.add("No attributes available");
				}
				edgeWeights1.put(netName, new ListSingleSelection<String>(attrList2));
			}
			if (!nodeNames2.containsKey(netName)) {
				List<String> attrList = CyUtils.getStringAttributes(network, CyNode.class);
				if (attrList.size() == 0) {
					attrList.add("No attributes available");
				} else {
					if (attrList.indexOf("RINalyzerResidue") != -1) {
						attrList.set(attrList.indexOf("RINalyzerResidue"), attrList.get(0));
						attrList.set(0, "RINalyzerResidue");
					} else if (attrList.indexOf("name") != -1) {
						attrList.set(attrList.indexOf("name"), attrList.get(0));
						attrList.set(0, "name");
					}
				}
				nodeNames2.put(netName, new ListSingleSelection<String>(attrList));
			}
			if (!edgeWeights2.containsKey(netName)) {
				List<String> attrList2 = CyUtils.getNumericAttributes(network, CyEdge.class);
				if (attrList2.size() == 0) {
					attrList2.add("No attributes available");
				}
				edgeWeights2.put(netName, new ListSingleSelection<String>(attrList2));
			}
		}
	}

	public void allFinished(FinishStatus arg0) {
		// List<String> openChains = ChimUtils.getSortedOpenModelChains(aContext);
	}

	public void taskFinished(ObservableTask task) {
		if (task == null) {
			taskFinished = true;
			return;
		}
		String result = task.getResults(String.class);
		if (result == null) {
			taskFinished = true;
			return;
		}

		// Sequences:
		// 1PSC, chain A SPTLRAS
		// 1pta, chain A SPTL...
		// Residues:
		// #0 SER 359.A, #0 PRO 360.A, #0 THR 361.A, #0 LEU 362.A, #0 ARG 363.A,
		// None, None
		// #1 SER 359.A, #1 PRO 360.A, #1 THR 361.A, #1 LEU 362.A
		// Residue usage in match (1=used, 0=unused):
		// 1, 1, 1, 1, 0, 0, 0
		// 1, 1, 1, 1
		// System.out.println(result);
		List<String> reply = Arrays.asList(result.split("\n"));
		if (getSeqAlignMap && reply != null && reply.size() > 0) {
			int counter = 0;
			for (counter = 0; counter < reply.size(); counter++) {
				if (reply.get(counter).startsWith("Sequences:")) {
					break;
				}
			}

			// get the AA sequences
			String[] sequence1 = reply.get(counter + 1).split(",|\t|\\s");
			String[] sequence2 = reply.get(counter + 2).split(",|\t|\\s");

			// Check for missing chains
			String refChain = null;
			String matchChain = null;
			if (sequence1.length == 5 && sequence1[2].equals("chain")) {
				refChain = sequence1[3].trim();
			} else if (sequence1.length == 2) {
				refChain = "_";
			} else {
				taskFinished = true;
				return;
			}
			if (sequence2.length == 5 && sequence2[2].equals("chain")) {
				matchChain = sequence2[3].trim();
			} else if (sequence2.length == 2) {
				matchChain = "_";
			} else {
				taskFinished = true;
				return;
			}

			// the AA sequences are always the last string in the line
			String[] sequences = new String[] { sequence1[sequence1.length - 1],
					sequence2[sequence2.length - 1] };

			// get the list of residues as arrays
			String[][] residues = new String[][] { reply.get(counter + 4).split(", "),
					reply.get(counter + 5).split(", ") };

			// get residue usage
			String[][] residueUsage = new String[][] { reply.get(counter + 7).split(", "),
					reply.get(counter + 8).split(", ") };

			// get the lowest residue index
			int startIndex1 = 0;
			int startIndex2 = 0;
			int shift1 = 0;
			int shift2 = 0;
			for (int a = 0; a < residues[0].length; a++) {
				if (!residues[0][a].equals("None")) {
					startIndex1 = Integer.parseInt(residues[0][a].split("\\s")[2].split("\\.")[0]);
					break;
				} else {
					shift1 += 1;
				}
			}
			for (int b = 0; b < residues[1].length; b++) {
				if (!residues[1][b].equals("None")) {
					startIndex2 = Integer.parseInt(residues[1][b].split("\\s")[2].split("\\.")[0]);
					break;
				} else {
					shift2 += 1;
				}
			}
			// System.out.println("sequences length: " + sequences[0].length() + "\t"
			// + sequences[1].length());
			// System.out
			// .println("residues length: " + residues[0].length + "\t" + residues[1].length);
			// System.out.println("start1: " + startIndex1 + "\t start2: " + startIndex2);
			// System.out.println("shift1: " + shift1 + "\t shift2: " + shift2);
			// try to match only if the sequences have equal length
			if (sequences[0].length() == sequences[1].length()) {
				int residueCount1 = 0;
				int residueCount2 = 0;
				for (int i = 0; i < sequences[0].length(); i++) {
					String res1 = String.valueOf(sequences[0].charAt(i));
					String res2 = String.valueOf(sequences[1].charAt(i));
					// System.out.println((i) + "\t" + res1 + "\t" + startIndex1 + "\t" + res2 +
					// "\t"
					// + startIndex2);
					// System.out.println(residueCount1 + "\t" + residueCount2);
					// if residues aligned
					// TODO: [Improve][Comparison] Test mapping of residues in different cases
					if (!res1.equals(".") && !res2.equals(".")) {
						if (!getStrAlignMap) {
							resMapping.put(refChain + ":" + String.valueOf(startIndex1 - shift1),
									matchChain + ":" + String.valueOf(startIndex2 - shift2));
							// resMapping.put(refChain + ":" + String.valueOf(start +
							// residueCount1),
							// matchChain + ":" + String.valueOf(start + residueCount2));
						} else if (!residues[0][residueCount1].equals("None")
								&& !residues[1][residueCount2].equals("None")
								&& !residueUsage[0][residueCount1].equals("0")
								&& !residueUsage[1][residueCount2].equals("0")) {
							// TODO: [Improve][Comparison] Include structure alignment
							// resMapping.put(refChain + ":" + String.valueOf(startIndex1),
							// matchChain
							// + ":" + String.valueOf(startIndex2));
							resMapping.put(
									refChain + ":" + String.valueOf(startIndex1 + residueCount1),
									matchChain + ":" + String.valueOf(startIndex2 + residueCount2));
						}
					}
					// increase count only if there is an amino acid and not a gap
					if (!res1.equals(".")) {
						residueCount1 += 1;
						startIndex1 += 1;
					}
					if (!res2.equals(".")) {
						residueCount2 += 1;
						startIndex2 += 1;
					}
				}
			}
		}
		taskFinished = true;
	}

	private boolean align(CyServiceRegistrar context, String ref, String match) {
		// get correct model/chain ids for the commnad
		String cmdRef = ref.split("\\s")[0];
		String cmdMatch = match.split("\\s")[0];
		if (ref.contains("chain")) {
			cmdRef += ":." + ref.split("\\s")[ref.split("\\s").length - 1];
		}
		if (match.contains("chain")) {
			cmdMatch += ":." + match.split("\\s")[match.split("\\s").length - 1];
		}
		String command = Messages.CC_MATCH1 + cmdRef + " " + cmdMatch + Messages.CC_MATCH2;

		// make Chimera match the selected models/chains
		return ChimUtils.sendCommand(context, this, command);
	}

}
