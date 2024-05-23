package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.SubnetworkGenerationDialog;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;

public class ExtractSubnetworkTask extends AbstractTask {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ExtractSubnetworkTask.class);

	private CyServiceRegistrar context;
	private CyNetwork network;
	private CyNetwork subnetwork;
	private SubnetworkGenerationDialog dialog;
	private RINFormatChecker rinChecker;

	public ExtractSubnetworkTask(CyServiceRegistrar bc, CyNetwork aNetwork) {
		context = bc;
		network = aNetwork;
		dialog = null;
		rinChecker = null;
		init();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_SUBNETWORK);
		taskMonitor.setStatusMessage(Messages.TM_SUBNETWORK);
		// continue only if user clicked ok
		if (dialog == null || dialog.isCanceled()) {
			return;
		}
		// create network from nodes in selected chains and their adjacent edges
		final List<String> selChains = dialog.getSelectedChains();
		final boolean chainNetFlag = dialog.isChainNetwork();
		if (selChains.size() == 0) {
			return;
		}
		// get nodes and edges
		final List<CyNode> nodes = new ArrayList<CyNode>();
		final List<CyEdge> edges = new ArrayList<CyEdge>();
		if (chainNetFlag) {
			// get all nodes in selected chains and all edges between them
			for (final String chain : selChains) {
				final Set<CyNode> chainNodes = rinChecker.getChainNodes(chain);
				if (chainNodes == null) {
					continue;
				}
				nodes.addAll(chainNodes);
			}
		} else if (selChains.size() == 1) {
			// get interface of one chain
			final Set<CyNode> chainNodes = rinChecker.getChainNodes(selChains.get(0));
			if (chainNodes != null) {
				for (CyNode node : chainNodes) {
					// get all neighbors from other chains
					List<CyNode> neighbors = network.getNeighborList(node, Type.ANY);
					neighbors.removeAll(chainNodes);
					if (neighbors.size() > 0) {
						nodes.add(node);
					}
				}
			}
		} else {
			// if more than one chain and interface
			for (int i = 0; i < selChains.size(); i++) {
				final Set<CyNode> chainNodes1 = rinChecker.getChainNodes(selChains.get(i));
				if (chainNodes1 == null) {
					continue;
				}
				for (int j = i + 1; j < selChains.size(); j++) {
					final Set<CyNode> chainNodes2 = rinChecker.getChainNodes(selChains.get(j));
					if (chainNodes2 == null) {
						continue;
					}
					for (CyNode node1 : chainNodes1) {
						for (CyNode node2 : chainNodes2) {
							// get all edges between nodes in different chains
							List<CyEdge> connEdges = network.getConnectingEdgeList(node1, node2,
									Type.ANY);
							if (connEdges.size() > 0) {
								edges.addAll(connEdges);
								if (!nodes.contains(node1)) {
									nodes.add(node1);
								}
								if (!nodes.contains(node2)) {
									nodes.add(node2);
								}
							}
						}
					}
				}
			}
		}
		// Add edges between single chains
		if (chainNetFlag || dialog.addEdges()) {
			// get edges between all nodes in the set
			for (int i = 0; i < nodes.size(); i++) {
				for (int j = i; j < nodes.size(); j++) {
					edges.addAll(network.getConnectingEdgeList(nodes.get(i), nodes.get(j), Type.ANY));
				}
			}
		}

		// create network
		createNetwork(nodes, edges);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Extract Subnetwork Options";
	}

	private void init() {
		// ensure RIN format
		Map<CyNode, String[]> nodeLables = null;
		if (network.getDefaultNodeTable().getColumn(Messages.SV_RINRESIDUE) != null) {
			nodeLables = CyUtils.splitNodeLabels(network, Messages.SV_RINRESIDUE);
		} else {
			nodeLables = CyUtils.splitNodeLabels(network, CyNetwork.NAME);
		}
		rinChecker = new RINFormatChecker(network, nodeLables);
		if (rinChecker.getErrorStatus() != null) {
			// msg to the user
			JOptionPane.showMessageDialog(CyUtils.getCyFrame(context), rinChecker.getErrorStatus(),
					Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
			logger.warn(rinChecker.getErrorStatus());
			return;
		}
		final Set<String> chainIDs = rinChecker.getChainIDs();

		// show dialog
		dialog = new SubnetworkGenerationDialog(CyUtils.getCyFrame(context), chainIDs, network
				.getRow(network).get(CyNetwork.NAME, String.class));
		dialog.setVisible(true);
	}

	private void createNetwork(List<CyNode> nodes, List<CyEdge> edges) {
		// get factories and managers
		CyRootNetworkManager manager = (CyRootNetworkManager) CyUtils.getService(context,
				CyRootNetworkManager.class);
		CyNetworkManager cyNetworkManager = (CyNetworkManager) CyUtils.getService(context,
				CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = (CyNetworkViewFactory) CyUtils.getService(
				context, CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManager = (CyNetworkViewManager) CyUtils.getService(
				context, CyNetworkViewManager.class);
		ApplyPreferredLayoutTaskFactory layoutTaskFactory = (ApplyPreferredLayoutTaskFactory) CyUtils
				.getService(context, ApplyPreferredLayoutTaskFactory.class);
		TaskManager<?, ?> taskManager = (TaskManager<?, ?>) CyUtils.getService(context,
				TaskManager.class);
		VisualMappingManager cyVmManager = (VisualMappingManager) CyUtils.getService(context,
				VisualMappingManager.class);

		// create network
		subnetwork = manager.getRootNetwork(network).addSubNetwork(nodes, edges);
		subnetwork.getRow(subnetwork).set(CyNetwork.NAME, dialog.getNetworkTitle());

		// register network
		cyNetworkManager.addNetwork(subnetwork);

		// Create network view
		CyNetworkView subnetworkView = cyNetworkViewFactory.createNetworkView(subnetwork);
		cyNetworkViewManager.addNetworkView(subnetworkView);
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(subnetworkView);

		// Do a layout
		taskManager.execute(layoutTaskFactory.createTaskIterator(views));

		// Set vizmap
		VisualStyle currentStyle = cyVmManager.getCurrentVisualStyle();
		cyVmManager.setVisualStyle(currentStyle, subnetworkView);
		currentStyle.apply(subnetworkView);
		subnetworkView.updateView();
	}

}
