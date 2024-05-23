package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class AnnotateRINTaskFactory extends AbstractTaskFactory implements NetworkTaskFactory {

	private CyServiceRegistrar context;

	public AnnotateRINTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady(CyNetwork network) {
		NetworkTaskFactory annotateTaskFactory = (NetworkTaskFactory) CyUtils.getService(context,
				NetworkTaskFactory.class, Messages.SV_ANNOTATECOMMANDTASK);
		if (annotateTaskFactory != null) {
			return annotateTaskFactory.isReady(network);
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new AnnotateRINTask(context, network));
	}
}
