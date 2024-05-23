package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.io.File;
import java.util.HashMap;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class OpenStructureFileTask extends AbstractTask {

	private CyServiceRegistrar context;

	@Tunable(description = "Structure file", params = "fileCategory=unspecified;input=true", gravity = 3.0)
	public File structureFile = null;

	public OpenStructureFileTask(CyServiceRegistrar bc) {
		context = bc;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_OPENSTRUCTUREFILE);
		taskMonitor.setStatusMessage(Messages.TM_OPENSTRUCTUREFILE);
		TaskManager<?, ?> dtm = (TaskManager<?, ?>) CyUtils.getService(context, TaskManager.class);
		TunableSetter ts = (TunableSetter) CyUtils.getService(context, TunableSetter.class);
		HashMap<String, Object> tunables = new HashMap<String, Object>();
		tunables.put(Messages.SV_TUNABLEOPENFILE, structureFile);
		tunables.put(Messages.SV_TUNABLESHOWD, true);
		TaskFactory openFileTaskFactory = (TaskFactory) CyUtils.getService(context,
				TaskFactory.class, Messages.SV_OPENFILECOMMANDTASK);
		if (openFileTaskFactory != null) {
			// insertTasksAfterCurrentTask(openFileTaskFactory.createTaskIterator());
			dtm.execute(ts.createTaskIterator(openFileTaskFactory.createTaskIterator(), tunables));
		}
	}

	@ProvidesTitle
	public String getTitle() {
		return "Open structure from file";
	}

}
