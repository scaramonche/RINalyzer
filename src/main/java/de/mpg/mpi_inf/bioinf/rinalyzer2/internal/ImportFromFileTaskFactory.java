package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class ImportFromFileTaskFactory extends AbstractTaskFactory implements TaskFactory {

	private CyServiceRegistrar context;

	private RINVisualPropertiesManager rinVisPropsManager;

	public ImportFromFileTaskFactory(CyServiceRegistrar bc,
			RINVisualPropertiesManager aVisPropsManager) {
		context = bc;
		rinVisPropsManager = aVisPropsManager;
	}

	public boolean isReady() {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ImportFromFileTask(context, rinVisPropsManager));
	}

}
