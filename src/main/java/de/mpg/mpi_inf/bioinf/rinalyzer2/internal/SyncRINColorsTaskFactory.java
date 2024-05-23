package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class SyncRINColorsTaskFactory extends AbstractTaskFactory implements NetworkViewTaskFactory {

	private CyServiceRegistrar context;

	public SyncRINColorsTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady(CyNetworkView networkView) {
		NetworkViewTaskFactory taskFactory = (NetworkViewTaskFactory) CyUtils.getService(context,
				NetworkViewTaskFactory.class, Messages.SV_SYNCCOLORSTASK);
		if (taskFactory != null) {
			return taskFactory.isReady(networkView);
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new SyncRINColorsTask(context, networkView));
	}

}
