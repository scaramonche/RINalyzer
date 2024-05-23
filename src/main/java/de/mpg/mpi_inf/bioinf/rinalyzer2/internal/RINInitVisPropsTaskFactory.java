package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class RINInitVisPropsTaskFactory extends AbstractTaskFactory implements NetworkTaskFactory {

	private CyServiceRegistrar context;
	private RINVisualPropertiesManager rinVisPropManager;

	public RINInitVisPropsTaskFactory(CyServiceRegistrar bc,
			RINVisualPropertiesManager aRINVisPropManager) {
		context = bc;
		this.rinVisPropManager = aRINVisPropManager;
	}

	public boolean isReady(CyNetwork network) {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(
				new RINInitVisPropsTask(context, rinVisPropManager, network));
	}

}
