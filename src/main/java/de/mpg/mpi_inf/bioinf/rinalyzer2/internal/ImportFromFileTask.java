package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

// TODO: [Improve][ImportRIN] duplicated code with ImportFromURLTask
// TODO: [Bug][Import with weights does not work]


public class ImportFromFileTask extends AbstractTask {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ImportFromFileTask.class);

	private CyServiceRegistrar context;

	private RINVisualPropertiesManager rinVisPropsManager;

	@Tunable(description = "Network file", params = "fileCategory=unspecified;input=true", gravity = 1.0, groups = "Default options", tooltip = "Select network file to be imported")
	public File file;

	@Tunable(description = "Delimiter", gravity = 2.0, groups = "Default options", tooltip = "Enter a delimiter, e.g., \\s for space, \\t for tab")
	public String delimiter;

	// TODO: [Improve][ImportRIN] Change from string to file and open automatically
	@Tunable(description = "Protein structure associated with the network", gravity = 3.0, groups = "Default options", tooltip = "Enter a PDB identifier or a file name in parentheses")
	public String pdb;

	@Tunable(description = "Header", gravity = 4.0, groups = "Advanced options", params = "displayState=collapsed", tooltip = "Check if there is a header in the network file")
	public boolean header;

	@Tunable(description = "Network file includes numeric edge attributes", gravity = 5.0, groups = "Advanced options", tooltip = "Check if there are numeric edge attributes as additional columns in the file")
	public boolean includesEdgeAttr;

	@Tunable(description = "Number of edge attributes in file", gravity = 6.0, groups = "Advanced options", dependsOn = "includesEdgeAttr=true", tooltip = "Enter the number of numeric attribute columns")
	public int numEdgeAttrs;

	public ImportFromFileTask(CyServiceRegistrar registrar,
			RINVisualPropertiesManager aVisPropsManager) {
		context = registrar;
		file = null;
		pdb = "";
		delimiter = "\\s";
		header = false;
		includesEdgeAttr = false;
		numEdgeAttrs = 0;
		rinVisPropsManager = aVisPropsManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_IMPORTFROMFILE);
		taskMonitor.setStatusMessage(Messages.TM_IMPORTFROMFILE);
		// exit if file cannot be read
		if (file == null || !(file.isFile())) {
			taskMonitor.setStatusMessage("Could not read file, aborting ...");
			return;
		}
		// create network
		CyNetworkFactory netFactory = (CyNetworkFactory) CyUtils.getService(context,
				CyNetworkFactory.class);
		CyNetwork newNetwork = netFactory.createNetwork();
		String name = file.getName();
		newNetwork.getRow(newNetwork).set(CyNetwork.NAME, name);

		// create edge attributes if needed
		List<String> attrNames = new ArrayList<String>();
		if (includesEdgeAttr && !header) {
			System.out.println(numEdgeAttrs);
			for (int i = 0; i < numEdgeAttrs; i++) {
				newNetwork.getDefaultEdgeTable().createColumn("RINAttr" + String.valueOf(i),
						Double.class, false);
				attrNames.add("RINAttr" + String.valueOf(i));
			}
		}

		// read network file and save nodes and edges
		BufferedReader br = null;
		Map<String, CyNode> nodesMap = new HashMap<String, CyNode>();
		Map<String, CyEdge> edgesMap = new HashMap<String, CyEdge>();
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			int counter = 0;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(delimiter);
				int wordsmax = words.length - 3;
				// line contains an edge
				// System.out.println("words: " + words.length);
				if (words.length == 3 || (includesEdgeAttr && wordsmax > 0)) {
					// Save names from header if available
					if (header && counter == 0) {
						for (int i = 0; i < wordsmax; i++) {
							newNetwork.getDefaultEdgeTable().createColumn(words[3 + i],
									Double.class, false);
							attrNames.add(words[3 + i]);
						}
					}
					// get or create source and target nodes
					CyNode source = null;
					CyNode target = null;
					if (!nodesMap.containsKey(words[0])) {
						source = newNetwork.addNode();
						nodesMap.put(words[0], source);
						newNetwork.getRow(source).set(CyNetwork.NAME, words[0]);
					} else {
						source = nodesMap.get(words[0]);
					}
					if (!nodesMap.containsKey(words[2])) {
						target = newNetwork.addNode();
						nodesMap.put(words[2], target);
						newNetwork.getRow(target).set(CyNetwork.NAME, words[2]);
					} else {
						target = nodesMap.get(words[2]);
					}
					// create edge
					if (source != null && target != null) {
						String edgeName = CyUtils.getCyName(newNetwork, source) + " (" + words[1]
								+ ") " + CyUtils.getCyName(newNetwork, target);
						// System.out.println(edgeName);
						if (!edgesMap.containsKey(edgeName)) {
							CyEdge edge = newNetwork.addEdge(source, target, true);
							newNetwork.getRow(edge).set(CyNetwork.NAME, edgeName);
							newNetwork.getRow(edge).set(CyEdge.INTERACTION, words[1]);
							// save edge attributes
							if (includesEdgeAttr && wordsmax > 0) {
								for (int i = 0; i < wordsmax; i++) {
									try {
										newNetwork.getRow(edge).set(attrNames.get(i),
												Double.valueOf(words[i + 3]));
									} catch (NumberFormatException ex) {
										// not a number, save as string
									}
								}
							}
							edgesMap.put(edgeName, edge);
						}
						// else {
						// System.out.println("Error: duplicated entry for edge " + edgeName);
						// }
					}
				} else if (words.length == 1) {
					// create a single node
					if (!nodesMap.containsKey(words[0])) {
						CyNode source = newNetwork.addNode();
						nodesMap.put(words[0], source);
						newNetwork.getRow(source).set(CyNetwork.NAME, words[0]);
					}
				} else {
					taskMonitor.setStatusMessage(Messages.LOG_WRONGATTRNO);
					logger.warn(Messages.LOG_WRONGATTRNO);
					break;
				}
				counter += 1;
			}
			br.close();
		} catch (Exception ex) {
			// ignore, parsing failed
			// logger.error(Messages.LOG_NETIMPORTFAILED);
			throw new Exception(Messages.LOG_NETIMPORTFAILED);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
		// return if new network empty
		if (newNetwork.getNodeCount() == 0) {
			taskMonitor.setStatusMessage(Messages.TM_NONODESFOUND);
			return;
		}
		// ID db not set, assume that network corresponds to a structure with the same name
		String pdbFileName = pdb;
		if (pdbFileName == null || pdbFileName.equals("") || pdbFileName.equals(" ")) {
			pdbFileName = "\"" + name.substring(0, name.length() - 3) + "ent\"";
		}
		// save attributes needed for association with Chimera objects
		// save other attributes like res index, chain and type, isnertion code
		Map<CyNode, String> chimResIDs = CyUtils.getChimeraResidueIDs(newNetwork, CyNetwork.NAME);
		Map<CyNode, String[]> nodeLables = CyUtils.splitNodeLabels(newNetwork, CyNetwork.NAME);
		RINFormatChecker checker = new RINFormatChecker(newNetwork, nodeLables);
		if (checker.getErrorStatus() == null) {
			newNetwork.getDefaultNodeTable().createColumn(Messages.defaultStructureKey,
					String.class, false);
			newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESCHAIN, String.class,
					false);
			newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESINDEX, Integer.class,
					false);
			newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESICODE, String.class,
					false);
			newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESTYPE, String.class,
					false);
			for (CyNode node : newNetwork.getNodeList()) {
				if (chimResIDs.containsKey(node)) {
					newNetwork.getRow(node).set(Messages.defaultStructureKey,
							pdbFileName + "#" + chimResIDs.get(node));
				}
				if (nodeLables.containsKey(node) && nodeLables.get(node).length == 5) {
					newNetwork.getRow(node).set(Messages.NODE_RESCHAIN, nodeLables.get(node)[1]);
					newNetwork.getRow(node).set(Messages.NODE_RESICODE, nodeLables.get(node)[3]);
					newNetwork.getRow(node).set(Messages.NODE_RESTYPE, nodeLables.get(node)[4]);
					if (nodeLables.get(node)[2] != null) {
						try {
							newNetwork.getRow(node).set(Messages.NODE_RESINDEX,
									Integer.valueOf(nodeLables.get(node)[2]));
						} catch (NumberFormatException ex) {
							// ignore
						}
					}
				}
			}
		} else {
			taskMonitor.setStatusMessage("Not a RIN format, cannot create attibutes.");
		}
		// finalize network
		finalizeNetwork(newNetwork);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Import RIN from File Options";
	}

	private void finalizeNetwork(CyNetwork newNetwork) {
		// get factories and managers
		CyNetworkManager cyNetworkManager = (CyNetworkManager) CyUtils.getService(context,
				CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = (CyNetworkViewFactory) CyUtils.getService(
				context, CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManager = (CyNetworkViewManager) CyUtils.getService(
				context, CyNetworkViewManager.class);
		ApplyPreferredLayoutTaskFactory layoutTaskFactory = (ApplyPreferredLayoutTaskFactory) CyUtils
				.getService(context, ApplyPreferredLayoutTaskFactory.class);

		// register network
		cyNetworkManager.addNetwork(newNetwork);

		// Create network view
		CyNetworkView netView = cyNetworkViewFactory.createNetworkView(newNetwork);
		cyNetworkViewManager.addNetworkView(netView);

		// Apply default RIN visual properties
		insertTasksAfterCurrentTask(new RINInitVisPropsTaskFactory(context, rinVisPropsManager)
				.createTaskIterator(newNetwork));

		// Do a layout
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(netView);
		insertTasksAfterCurrentTask(layoutTaskFactory.createTaskIterator(views));
	}
}
