package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class OpenStructureTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private CyServiceRegistrar context;

	public OpenStructureTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady() {
		TaskFactory openTaskFactory = (TaskFactory) CyUtils.getService(context, TaskFactory.class,
				Messages.SV_OPENCOMMANDTASK);
		if (openTaskFactory != null) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new OpenStructureTask(context));
	}

}
