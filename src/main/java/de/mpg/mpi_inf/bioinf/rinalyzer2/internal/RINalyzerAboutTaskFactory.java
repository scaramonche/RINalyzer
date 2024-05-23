package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class RINalyzerAboutTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private CyServiceRegistrar context;

	public RINalyzerAboutTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady() {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RINalyzerAboutTask(context));
	}

}
