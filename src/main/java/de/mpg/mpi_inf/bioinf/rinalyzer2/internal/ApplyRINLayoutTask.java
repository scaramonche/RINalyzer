package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.ChimUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

// TODO: [Improve] Can be removed if not needed anymore
public class ApplyRINLayoutTask extends AbstractTask implements TaskObserver {

	private CyServiceRegistrar context;

	private CyNetworkView view;

	private TaskMonitor monitor;

	public ApplyRINLayoutTask(CyServiceRegistrar bc, CyNetworkView netView) {
		context = bc;
		view = netView;
	}

	public void allFinished(FinishStatus arg0) {
		CyLayoutAlgorithmManager manager = (CyLayoutAlgorithmManager) CyUtils.getService(context,
				CyLayoutAlgorithmManager.class);
		CyLayoutAlgorithm rinlayout = manager.getLayout("rin-layout");
		if (rinlayout != null) {
			TaskManager<?, ?> taskManager = (TaskManager<?, ?>) CyUtils.getService(context,
					TaskManager.class);
			taskManager.execute(rinlayout.createTaskIterator(view,
					rinlayout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
		} else {
			monitor.setStatusMessage(Messages.TM_APPLYRINLAYOUTERROR);
		}
	}

	public void taskFinished(ObservableTask arg0) {
		// do nothing
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		monitor = taskMonitor;
		monitor.setTitle(Messages.TM_APPLYRINLAYOUT);
		ChimUtils.getCoordinates(context, this, view.getModel());
	}

}
