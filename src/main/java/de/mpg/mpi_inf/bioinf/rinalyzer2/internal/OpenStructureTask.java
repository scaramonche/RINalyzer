package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class OpenStructureTask extends AbstractTask {

	private CyServiceRegistrar context;

	public OpenStructureTask(CyServiceRegistrar bc) {
		context = bc;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_OPENSTRUCTURE);
		taskMonitor.setStatusMessage(Messages.TM_OPENSTRUCTURE);
		// if (CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true).size() ==
		// 0) {
		// NodeViewTaskFactory openTaskFactory = (NodeViewTaskFactory) CyUtils.getService(context,
		// NodeViewTaskFactory.class, Messages.SV_OPENCOMMANDTASK);
		// if (openTaskFactory != null && netView.getNodeViews().size() > 0) {
		// View<CyNode> nodeView = netView.getNodeViews().iterator().next();
		// netView.getModel().getRow(nodeView.getModel()).set(CyNetwork.SELECTED, true);
		// insertTasksAfterCurrentTask(openTaskFactory.createTaskIterator(nodeView, netView));
		// }
		// } else {
		TaskFactory openTaskFactory = (TaskFactory) CyUtils.getService(context, TaskFactory.class,
				Messages.SV_OPENCOMMANDTASK);
		if (openTaskFactory != null) {
			insertTasksAfterCurrentTask(openTaskFactory.createTaskIterator());
			// dtm.execute(ts.createTaskIterator(openTaskFactory.createTaskIterator(), tunables));
			// dtm.execute(openTaskFactory.createTaskIterator());
		}

		// }
	}

	@ProvidesTitle
	public String getTitle() {
		return "Open structure";
	}

}
