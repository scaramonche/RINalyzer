package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExtractDegreeSubnetworkTaskFactory extends AbstractTaskFactory implements
		NetworkTaskFactory {

	private CyServiceRegistrar context;

	public ExtractDegreeSubnetworkTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady(CyNetwork network) {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new ExtractDegreeSubnetworkTask(context, network));
	}

}
