package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class ApplyRINLayoutTaskFactory extends AbstractTaskFactory implements
		NetworkViewTaskFactory {
	
	private CyServiceRegistrar context;

	public ApplyRINLayoutTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady(CyNetworkView netView) {
		NetworkTaskFactory annotateTaskFactory = (NetworkTaskFactory) CyUtils.getService(context,
				NetworkTaskFactory.class, Messages.SV_ANNOTATECOMMANDTASK);
		if (annotateTaskFactory != null) {
			return annotateTaskFactory.isReady(netView.getModel());
		} else if (netView.getModel().getDefaultNodeTable().getColumn("resCoord.x") != null) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new ApplyRINLayoutTask(context, networkView));
	}

}
