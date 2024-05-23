package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class AnalyzeTaskFactory extends AbstractTaskFactory implements NetworkViewTaskFactory {

	private CyServiceRegistrar context;

	public AnalyzeTaskFactory(CyServiceRegistrar aContext) {
		context = aContext;
	}

	public boolean isReady(CyNetworkView netView) {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		// Get all of the selected nodes
		return new TaskIterator(new AnalyzeTask(context, netView));
	}

}
