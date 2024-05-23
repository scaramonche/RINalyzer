package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.ChimUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class CompareRINsWrapperTask extends AbstractTask implements TaskObserver {

	private CyServiceRegistrar context;

	private Set<CyNetwork> netSet;

	private List<String> models;

	private List<String> chains;

	private boolean foundChimera;

	private boolean finished;

	private RINVisualPropertiesManager rinVisPropManager;
	
	public CompareRINsWrapperTask(CyServiceRegistrar aContext, Set<CyNetwork> aNetSet, RINVisualPropertiesManager rinVisPropManager) {
		context = aContext;
		netSet = aNetSet;
		this.rinVisPropManager = rinVisPropManager;
		models = new ArrayList<String>();
		chains = new ArrayList<String>();
		foundChimera = false;
		finished = false;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		foundChimera = ChimUtils.sendCommand(context, this, Messages.CC_LISTMMOL);
		if (!foundChimera) {
			insertTasksAfterCurrentTask(new CompareRINsTask(context, rinVisPropManager, netSet, chains));
		} else {
			while (!finished) {
				Thread.sleep(100);
			}
			insertTasksAfterCurrentTask(new CompareRINsTask(context, rinVisPropManager, netSet, chains));
		}
	}

	public void allFinished(FinishStatus arg0) {
	}

	public void taskFinished(ObservableTask task) {
		if (task == null) {
			finished = true;
			return;
		}
		String result = task.getResults(String.class);
		if (result == null) {
			finished = true;
			return;
		}

		List<String> reply = Arrays.asList(result.split("\n"));
		if (reply.size() > 0) {
			String command = reply.get(0);
			if (command.equals(Messages.CC_LISTMMOL)) {
				// try to get models
				int counter = 0;
				for (String line : reply) {
					counter += 1;
					if (counter == 1) {
						continue;
					}
					String modelName = line.substring(line.indexOf("name") + 5).trim();
					String modelID = line.substring(line.indexOf("#"), line.indexOf("type")).trim();
					if (modelName != null && modelID != null) {
						models.add(modelID + " " + modelName);
						ChimUtils.sendCommand(context, this, Messages.CC_LISTMODELCHAINS + modelID);
					}
				}
				// System.out.println("models: " + models.size());
			} else if (command.startsWith(Messages.CC_LISTMODELCHAINS)) {
				String modelID = command.substring(Messages.CC_LISTMODELCHAINS.length(),
						command.length());
				// System.out.println(modelID);
				// try to get chains
				List<String> modelChains = new ArrayList<String>();
				int counter = 0;
				for (String line : reply) {
					counter += 1;
					if (counter == 1) {
						continue;
					}
					String chainID = line.substring(line.lastIndexOf("chain")).trim();
					if (chainID != null) {
						modelChains.add(chainID);
					}
				}
				String thisModel = null;
				for (String model : models) {
					if (model.startsWith(modelID)) {
						thisModel = model;
						break;
					}
				}
				if (thisModel != null) {
					models.remove(thisModel);
					for (String chain : modelChains) {
						chains.add(thisModel + " " + chain);
					}
				}
				// System.out.println("chains: " + chains.size());
			}
		}
		if (models.size() == 0) {
			finished = true;
		}
	}
}
