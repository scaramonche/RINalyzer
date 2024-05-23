package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class AnnotateRINTask extends AbstractTask {

	private CyServiceRegistrar context;
	private CyNetwork network;

	public AnnotateRINTask(CyServiceRegistrar bc, CyNetwork aNetwork) {
		context = bc;
		network = aNetwork;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_ANNOTATERIN);
		taskMonitor.setStatusMessage(Messages.TM_ANNOTATERIN);
		NetworkTaskFactory annotateFactory = (NetworkTaskFactory) CyUtils.getService(context,
				NetworkTaskFactory.class, Messages.SV_ANNOTATECOMMANDTASK);
		if (annotateFactory != null) {
			insertTasksAfterCurrentTask(annotateFactory.createTaskIterator(network));
		}

	}

}
