package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class ExtractDegreeSubnetworkTask extends AbstractTask {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ExtractSubnetworkTask.class);

	private CyServiceRegistrar context;
	private CyNetwork network;
	private CyNetwork subnetwork;

	@Tunable(description = "Minimum connecting edges")
	public int minEdges = 3;

	@Tunable(description = "New network name")
	public String newNetName;

	// private SubnetworkGenerationDialog dialog;
	// private RINFormatChecker rinChecker;

	public ExtractDegreeSubnetworkTask(CyServiceRegistrar bc, CyNetwork aNetwork) {
		context = bc;
		network = aNetwork;
		// dialog = null;
		// rinChecker = null;
		// init();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_SUBNETWORK);
		taskMonitor.setStatusMessage(Messages.TM_SUBNETWORK);

		// create network from nodes in selected chains and their adjacent edges
		// get nodes and edges
		final List<CyNode> nodes = new ArrayList<CyNode>();
		final List<CyEdge> edges = new ArrayList<CyEdge>();
		for (CyNode node1 : network.getNodeList()) {
			final List<CyNode> neighbors = network.getNeighborList(node1, Type.ANY);
			for (CyNode node2 : neighbors) {
				// get all edges between nodes in different chains
				List<CyEdge> connEdges = network.getConnectingEdgeList(node1, node2, Type.ANY);
				if (connEdges.size() >= minEdges) {
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

		// create network
		createNetwork(nodes, edges);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Extract Degree Subnetwork Options";
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
		subnetwork.getRow(subnetwork).set(CyNetwork.NAME, newNetName);

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
