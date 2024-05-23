package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class CloseStructureTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private CyServiceRegistrar context;

	public CloseStructureTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady() {
		// TaskFactory closeTaskFactory = (TaskFactory) CyUtils.getService(context,
		// TaskFactory.class,
		// Messages.SV_CLOSECOMMANDTASK);
		// if (closeTaskFactory != null) {
		// return true;
		// }
		TaskFactory createStrNetTaskFactory = (TaskFactory) CyUtils.getService(context,
				TaskFactory.class, Messages.SV_CREATERINCOMMANDTASK);
		if (createStrNetTaskFactory != null) {
			return createStrNetTaskFactory.isReady();
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CloseStructureTask(context));
	}

}
