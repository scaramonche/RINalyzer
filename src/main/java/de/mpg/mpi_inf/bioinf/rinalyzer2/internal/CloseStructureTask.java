package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class CloseStructureTask extends AbstractTask {

	private CyServiceRegistrar context;

	public CloseStructureTask(CyServiceRegistrar bc) {
		context = bc;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_CLOSESTRUCTURE);
		taskMonitor.setStatusMessage(Messages.TM_CLOSESTRUCTURE);
		// if (CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true).size() ==
		// 0) {
		// NodeViewTaskFactory closeTaskFactory = (NodeViewTaskFactory) CyUtils.getService(
		// context, NodeViewTaskFactory.class, Messages.SV_CLOSECOMMANDTASK);
		// if (closeTaskFactory != null && netView.getNodeViews().size() > 0) {
		// View<CyNode> nodeView = netView.getNodeViews().iterator().next();
		// netView.getModel().getRow(nodeView.getModel()).set(CyNetwork.SELECTED, true);
		// insertTasksAfterCurrentTask(closeTaskFactory.createTaskIterator(nodeView, netView));
		// }
		// } else {
		// DialogTaskManager dtm = (DialogTaskManager) CyUtils.getService(context,
		// DialogTaskManager.class);
		TaskFactory closeTaskFactory = (TaskFactory) CyUtils.getService(context, TaskFactory.class,
				Messages.SV_CLOSECOMMANDTASK);
		if (closeTaskFactory != null) {
			insertTasksAfterCurrentTask(closeTaskFactory.createTaskIterator());
			// dtm.execute(closeTaskFactory.createTaskIterator());
		}
		// }

	}

}
