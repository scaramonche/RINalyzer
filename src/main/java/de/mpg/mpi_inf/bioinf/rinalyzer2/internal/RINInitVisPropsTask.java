package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class RINInitVisPropsTask extends AbstractTask {

	private CyServiceRegistrar context;
	private RINVisualPropertiesManager rinVisPropsManager;
	private CyNetwork network;

	public RINInitVisPropsTask(CyServiceRegistrar bc,
			RINVisualPropertiesManager aRINVisPropManager, CyNetwork aNetwork) {
		context = bc;
		rinVisPropsManager = aRINVisPropManager;
		network = aNetwork;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_INITVISPROPS);
		taskMonitor.setStatusMessage(Messages.TM_INITVISPROPS);
		CyNetworkViewManager netViewMgr = (CyNetworkViewManager) CyUtils.getService(context,
				CyNetworkViewManager.class);
		Collection<CyNetworkView> views = netViewMgr.getNetworkViews(network);
		// Apply RIN properties to each view
		for (CyNetworkView networkView : views) {
			if (!rinVisPropsManager.hasNetwork(network)) {
				rinVisPropsManager.addNetwork(network, networkView);
			}
			// change network background
			rinVisPropsManager.changeBackgroundColor(network);
			// color nodes
			rinVisPropsManager.changeNodeColor(network);
			// label nodes
			rinVisPropsManager.labelOperation(new boolean[] { false, false, true, false, true },
					true, network);
			// change edge color and width
			rinVisPropsManager.changeEdgeColor(network);
			rinVisPropsManager.changeEdgeWidth(network);
			// rinVisPropsManager.hideDistEdges(network, networkView);
			// rinVisPropsManager.addEdgeBends(network, networkView);
			networkView.updateView();
		}
	}

}
