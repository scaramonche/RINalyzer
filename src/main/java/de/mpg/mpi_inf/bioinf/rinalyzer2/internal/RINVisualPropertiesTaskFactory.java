package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class RINVisualPropertiesTaskFactory extends AbstractTaskFactory implements
		NetworkViewTaskFactory {

	private CyServiceRegistrar context;
	private RINVisualPropertiesManager rinVisPropManager;

	public RINVisualPropertiesTaskFactory(CyServiceRegistrar bc,
			RINVisualPropertiesManager aRINVisPropManager) {
		context = bc;
		this.rinVisPropManager = aRINVisPropManager;
	}

	public boolean isReady(CyNetworkView networkView) {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(
				new RINVisualPropertiesTask(context, rinVisPropManager, networkView));
	}

}
