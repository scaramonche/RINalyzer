package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities;

import org.cytoscape.work.ObservableTask;
import org.osgi.framework.BundleContext;

public class SendCommandThread extends Thread {

	private BundleContext context;
	private String command;
	private boolean finished;
	private ObservableTask task;
	private Object result;

	public SendCommandThread(BundleContext context, String command) {
		this.context = context;
		this.command = command;
		finished = false;
		task = null;
		result = null;
	}

	public void run() {
		// System.out.println("send command " + command);
	}

	public Object getResults(Class<?> resultsType) {
		// TaskFactory sendCommandFactory = (TaskFactory) CyUtils.getService(context,
		// TaskFactory.class, Messages.SV_SENDCOMMANDTASK);
		// if (sendCommandFactory != null && sendCommandFactory.isReady()) {
		// TunableSetter tunableSetter = (TunableSetter) CyUtils.getService(context,
		// TunableSetter.class);
		// Map<String, Object> tunables = new HashMap<String, Object>();
		// tunables.put(Messages.SV_COMMANDTUNABLE, command);
		// TaskManager<?, ?> tm = (TaskManager<?, ?>) CyUtils.getService(context,
		// TaskManager.class);
		// tm.execute(tunableSetter.createTaskIterator(sendCommandFactory.createTaskIterator(),
		// tunables));
		// }
		// if (sendCommandFactory == null || !sendCommandFactory.isReady()) {
		// return null;
		// }
		// CyTableManager manager = (CyTableManager) CyUtils.getService(context,
		// CyTableManager.class);
		// CyTable chimOutputTable = null;
		// Set<CyTable> tables = manager.getAllTables(true);
		// for (CyTable table : tables) {
		// if (table.getTitle().equals(Messages.SV_CHIMERATABLE)) {
		// chimOutputTable = table;
		// }
		// }
		// if (chimOutputTable != null) {
		// while (true) {
		// if (chimOutputTable.rowExists(command)) {
		// if (chimOutputTable.getRow(command).isSet(Messages.SV_CHIMERAOUTPUT)) {
		// List<String> output = chimOutputTable.getRow(command).getList(
		// Messages.SV_CHIMERAOUTPUT, String.class);
		// chimOutputTable.getRow(command).set(Messages.SV_CHIMERAOUTPUT, null);
		// return output;
		// }
		// }
		// }
		// }
		// return task.getResults(resultsType);
		return null;
	}
}
