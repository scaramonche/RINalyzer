package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class SyncRINColorsTask extends AbstractTask {

	private CyServiceRegistrar context;
	private CyNetworkView netView;

	public SyncRINColorsTask(CyServiceRegistrar bc, CyNetworkView aView) {
		context = bc;
		netView = aView;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_SYNCRINCOLORS);
		taskMonitor.setStatusMessage(Messages.TM_SYNCRINCOLORS);
		NetworkViewTaskFactory syncColorsTaskFactory = (NetworkViewTaskFactory) CyUtils.getService(
				context, NetworkViewTaskFactory.class, Messages.SV_SYNCCOLORSTASK);
		if (syncColorsTaskFactory != null) {
			insertTasksAfterCurrentTask(syncColorsTaskFactory.createTaskIterator(netView));
		}
	}

}
