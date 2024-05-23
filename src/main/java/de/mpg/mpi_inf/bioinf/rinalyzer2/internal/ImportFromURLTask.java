package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.TunableSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

// TODO: [Improve][ImportRIN] duplicated code with ImportFromFileTask
public class ImportFromURLTask extends AbstractTask {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ImportFromFileTask.class);

	private String query;
	private CyServiceRegistrar context;
	private RINVisualPropertiesManager rinVisPropsManager;
	private CyNetwork newNetwork;
	private CyNetworkView netView;
	private Map<String, CyNode> nodesMap;
	private Map<String, CyEdge> edgesMap;

	public ImportFromURLTask(CyServiceRegistrar context,
			RINVisualPropertiesManager rinVisPropsManager, String query) {
		this.context = context;
		this.rinVisPropsManager = rinVisPropsManager;
		this.query = query;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_IMPORTFROMURL);
		taskMonitor.setStatusMessage(Messages.TM_IMPORTFROMURL);
		if (query.length() == 0) {
			taskMonitor.setStatusMessage(Messages.TM_IMPORTFROMURLABORT);
			return;
		}
		try {
			final InputStream urlInputStream = new URL(query).openStream();
			final ZipInputStream zin = new ZipInputStream(urlInputStream);
			ZipEntry ze = null;
			String pdbFileName = null;
			String pdbFilePath = null;
			while ((ze = zin.getNextEntry()) != null) {
				// System.out.println(ze.getName());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] content = new byte[(int) ze.getSize()];
				int bytesRead = -1;
				while ((bytesRead = zin.read(content)) != -1) {
					baos.write(content, 0, bytesRead);
				}
				if (ze.getName().endsWith(".sif")) {
					importNetwork(new ByteArrayInputStream(baos.toByteArray()), ze.getName());
				} else if (ze.getName().endsWith(".ea")) {
					importAttributes(new ByteArrayInputStream(baos.toByteArray()), ze.getName(),
							true);
				} else if (ze.getName().endsWith(".ent")) {
					pdbFileName = ze.getName();
					// Save the pdb file when importing from RINdata?
					pdbFilePath = savePDBFile(new ByteArrayInputStream(baos.toByteArray()),
							pdbFileName);
					if (pdbFilePath != null) {
						taskMonitor.setStatusMessage(Messages.TM_SAVEFROMURL + pdbFilePath);
					}
				}
				baos.close();
			}
			zin.close();
			urlInputStream.close();
			if (newNetwork.getNodeCount() == 0) {
				return;
			}
			// save attributes needed for association with Chimera objects
			if (pdbFileName != null) {
				Map<CyNode, String> chimResIDs = CyUtils.getChimeraResidueIDs(newNetwork,
						CyNetwork.NAME);
				Map<CyNode, String[]> nodeLables = CyUtils.splitNodeLabels(newNetwork,
						CyNetwork.NAME);

				newNetwork.getDefaultNodeTable().createColumn(Messages.defaultStructureKey,
						String.class, false);
				newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESCHAIN, String.class,
						false);
				newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESINDEX, String.class,
						false);
				newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESICODE, String.class,
						false);
				newNetwork.getDefaultNodeTable().createColumn(Messages.NODE_RESTYPE, String.class,
						false);

				for (CyNode node : newNetwork.getNodeList()) {
					if (chimResIDs.containsKey(node)) {
						newNetwork.getRow(node).set(Messages.defaultStructureKey,
								"\"" + pdbFileName + "\"#" + chimResIDs.get(node));
					}
					if (nodeLables.containsKey(node) && nodeLables.get(node).length == 5) {
						newNetwork.getRow(node)
								.set(Messages.NODE_RESCHAIN, nodeLables.get(node)[1]);
						newNetwork.getRow(node)
								.set(Messages.NODE_RESINDEX, nodeLables.get(node)[2]);
						newNetwork.getRow(node)
								.set(Messages.NODE_RESICODE, nodeLables.get(node)[3]);
						newNetwork.getRow(node).set(Messages.NODE_RESTYPE, nodeLables.get(node)[4]);
					}

				}
			}
			// finalize network
			finalizeNetwork();
			if (pdbFilePath != null) {
				openPDBFile(netView, pdbFilePath);
			}
		} catch (IOException ex) {
			taskMonitor.showMessage(Level.ERROR, Messages.TM_IMPORTFROMURLABORT2);
			// taskMonitor.setStatusMessage(Messages.TM_IMPORTFROMURLABORT2);
		} catch (Exception ex) {
			// ex.printStackTrace();
			// taskMonitor.setStatusMessage(Messages.TM_TASKABORT);
			taskMonitor.showMessage(Level.ERROR, Messages.TM_TASKABORT);
			// logger.warn(Messages.TM_TASKABORT, ex);
			// throw new Exception(Messages.TM_TASKABORT);
		}
	}

	private String savePDBFile(ByteArrayInputStream inputStream, String pdbFileName) {
		BufferedReader br = null;
		BufferedWriter bw = null;
		nodesMap = new HashMap<String, CyNode>();
		edgesMap = new HashMap<String, CyEdge>();
		try {
			CyApplicationConfiguration config = (CyApplicationConfiguration) CyUtils.getService(
					context, CyApplicationConfiguration.class);
			final File homeDir = new File(config.getConfigurationDirectoryLocation()
					.getAbsolutePath());
			final File dir = new File(homeDir, Messages.PDBS);
			if (!dir.exists() && !dir.mkdirs()) {
				throw new IOException("Unable to create " + dir.getAbsolutePath());
			}
			String pdbFilePath = dir.getAbsolutePath() + File.separator + pdbFileName;
			br = new BufferedReader(new InputStreamReader(inputStream));
			bw = new BufferedWriter(new FileWriter(pdbFilePath));
			String line = null;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				bw.write(line + "\n");
			}
			br.close();
			bw.close();
			return pdbFilePath;
		} catch (Exception ex) {
			// ignore, parsing failed
			logger.warn(Messages.LOG_PDBSAVEFAILED);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (bw != null) {
					bw.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
		return null;
	}

	private void openPDBFile(CyNetworkView view, String path) {
		CyLayoutAlgorithmManager manager = (CyLayoutAlgorithmManager) CyUtils.getService(context,
				CyLayoutAlgorithmManager.class);
		SynchronousTaskManager taskManager = (SynchronousTaskManager) CyUtils.getService(context,
				SynchronousTaskManager.class);

		TaskFactory openTaskFactory = (TaskFactory) CyUtils.getService(context, TaskFactory.class,
				Messages.SV_OPENFILECOMMANDTASK);
		if (openTaskFactory != null) {
			TunableSetter tunableSetter = (TunableSetter) CyUtils.getService(context,
					TunableSetter.class);
			Map<String, Object> tunables = new HashMap<String, Object>();
			tunables.put(Messages.SV_TUNABLEOPENFILE, new File(path));
			tunables.put(Messages.SV_TUNABLESHOWD, true);
			taskManager.execute(tunableSetter.createTaskIterator(
					openTaskFactory.createTaskIterator(), tunables));
			// Annotate and layout
			// taskManager.execute(new
			// ApplyRINLayoutTaskFactory(context).createTaskIterator(netView));
			// insertTasksAfterCurrentTask(new ApplyRINLayoutTask(context, netView));
			CyLayoutAlgorithm rinlayout = manager.getLayout("rin-layout");
			if (rinlayout != null) {
				taskManager
						.execute(rinlayout.createTaskIterator(view,
								rinlayout.getDefaultLayoutContext(),
								CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
			}
		} else {
			ApplyPreferredLayoutTaskFactory layoutTaskFactory = (ApplyPreferredLayoutTaskFactory) CyUtils
					.getService(context, ApplyPreferredLayoutTaskFactory.class);
			Set<CyNetworkView> views = new HashSet<CyNetworkView>();
			views.add(view);
			insertTasksAfterCurrentTask(layoutTaskFactory.createTaskIterator(views));
		}
	}

	// TODO: [Improve][ImportRIN] Import RIN with the according reader
	private void importNetwork(ByteArrayInputStream inputStream, String name) {
		CyNetworkFactory netFactory = (CyNetworkFactory) CyUtils.getService(context,
				CyNetworkFactory.class);
		newNetwork = netFactory.createNetwork();
		newNetwork.getRow(newNetwork).set(CyNetwork.NAME, name);
		BufferedReader br = null;
		nodesMap = new HashMap<String, CyNode>();
		edgesMap = new HashMap<String, CyEdge>();
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] words = line.split("\\s");
				if (words.length == 3) {
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
					if (source != null && target != null) {
						String edgeName = CyUtils.getCyName(newNetwork, source) + " (" + words[1]
								+ ") " + CyUtils.getCyName(newNetwork, target);
						// System.out.println(edgeName);
						if (!edgesMap.containsKey(edgeName)) {
							CyEdge edge = newNetwork.addEdge(source, target, true);
							newNetwork.getRow(edge).set(CyNetwork.NAME, edgeName);
							newNetwork.getRow(edge).set(CyEdge.INTERACTION, words[1]);
							edgesMap.put(edgeName, edge);
						}
						// else {
						// System.out.println("Error: duplicated entry for edge " + edgeName);
						// }
					}
				} else if (words.length == 1) {
					if (!nodesMap.containsKey(words[0])) {
						CyNode source = newNetwork.addNode();
						nodesMap.put(words[0], source);
						newNetwork.getRow(source).set(CyNetwork.NAME, words[0]);
					}
				}
			}
			br.close();
		} catch (Exception ex) {
			// ignore, parsing failed
			logger.warn(Messages.LOG_NETIMPORTFAILED);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
	}

	// TODO: [Improve][ImportRIN] Import attributes with the according reader
	private void importAttributes(ByteArrayInputStream inputStream, String name, boolean edgeAttr) {
		CyTable table = null;
		if (edgeAttr) {
			table = newNetwork.getDefaultEdgeTable();
		} else {
			table = newNetwork.getDefaultNodeTable();
		}
		BufferedReader br = null;
		String attr = "";
		Class<?> type = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			int counter = 0;
			while ((line = br.readLine()) != null) {
				counter += 1;
				if (counter == 1) {
					String[] words = line.split(" ");
					if (words.length != 2) {
						continue;
					}
					attr = words[0].trim();
					if (words[1].trim().endsWith("Integer)")) {
						table.createColumn(attr, Integer.class, false);
						type = Integer.class;
					} else if (words[1].trim().endsWith("Float)")) {
						table.createColumn(attr, Double.class, false);
						type = Double.class;
					} else {
						table.createColumn(attr, String.class, false);
						type = String.class;
					}
					continue;
				}
				String[] words = line.split("=");
				if (words.length != 2) {
					continue;
				}
				CyIdentifiable cyObj = null;
				if (edgeAttr && edgesMap.containsKey(words[0].trim())) {
					cyObj = edgesMap.get(words[0].trim());
				} else if (nodesMap.containsKey(words[0].trim())) {
					cyObj = nodesMap.get(words[0].trim());
				}
				if (cyObj != null) {
					if (type == Integer.class)
						newNetwork.getRow(cyObj).set(attr, new Integer(words[1].trim()));
					else if (type == Double.class)
						newNetwork.getRow(cyObj).set(attr, new Double(words[1].trim()));
					else
						newNetwork.getRow(cyObj).set(attr, words[1].trim());
				}
			}
			br.close();
		} catch (Exception ex) {
			// ignore, parsing failed
			logger.warn(Messages.LOG_ATTRIMPORTFAILED);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
	}

	private void finalizeNetwork() {
		// get factories and managers
		CyNetworkManager cyNetworkManager = (CyNetworkManager) CyUtils.getService(context,
				CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = (CyNetworkViewFactory) CyUtils.getService(
				context, CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManager = (CyNetworkViewManager) CyUtils.getService(
				context, CyNetworkViewManager.class);
		// TaskManager<?, ?> taskManager = (TaskManager<?, ?>)
		// CyUtils.getService(context,
		// TaskManager.class);

		// register network
		cyNetworkManager.addNetwork(newNetwork);

		// Create network view
		netView = cyNetworkViewFactory.createNetworkView(newNetwork);
		cyNetworkViewManager.addNetworkView(netView);

		// Apply default RIN visual properties
		insertTasksAfterCurrentTask(new RINInitVisPropsTaskFactory(context, rinVisPropsManager)
				.createTaskIterator(newNetwork));
	}

}
