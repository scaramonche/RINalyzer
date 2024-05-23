package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class CompareRINsTaskFactory extends AbstractTaskFactory implements NetworkTaskFactory {

	private CyServiceRegistrar context;
	private CyNetworkManager netManager;
	private RINVisualPropertiesManager rinVisPropManager;

	public CompareRINsTaskFactory(CyServiceRegistrar bc,
			RINVisualPropertiesManager rinVisPropManager) {
		context = bc;
		netManager = (CyNetworkManager) CyUtils.getService(context, CyNetworkManager.class);
		this.rinVisPropManager = rinVisPropManager;
	}

	public boolean isReady(CyNetwork arg0) {
		if (netManager != null && netManager.getNetworkSet().size() > 1) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new CompareRINsWrapperTask(context, netManager.getNetworkSet(), rinVisPropManager));
		// return new TaskIterator(new CompareRINsTask(context, netManager.getNetworkSet()));
	}

}
