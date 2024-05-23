package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class CreateRINTask extends AbstractTask {

	private CyServiceRegistrar context;

	public CreateRINTask(CyServiceRegistrar bc) {
		context = bc;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_CREATERIN);
		taskMonitor.setStatusMessage(Messages.TM_CREATERIN);
		TaskFactory createStrNetTaskFactory = (TaskFactory) CyUtils.getService(context,
				TaskFactory.class, Messages.SV_CREATERINCOMMANDTASK);
		if (createStrNetTaskFactory != null) {
			insertTasksAfterCurrentTask(createStrNetTaskFactory.createTaskIterator());
		}

	}

}
