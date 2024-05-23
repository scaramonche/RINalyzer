package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;

public class ExtractSubnetworkTaskFactory extends AbstractTaskFactory implements NetworkTaskFactory {

	private CyServiceRegistrar context;

	public ExtractSubnetworkTaskFactory(CyServiceRegistrar bc) {
		context = bc;
	}

	public boolean isReady(CyNetwork network) {
		Map<CyNode, String[]> nodeLables = null;
		if (network.getDefaultNodeTable().getColumn(Messages.SV_RINRESIDUE) != null) {
			nodeLables = CyUtils.splitNodeLabels(network, Messages.SV_RINRESIDUE);
		} else {
			nodeLables = CyUtils.splitNodeLabels(network, CyNetwork.NAME);
		}
		RINFormatChecker rinChecker = new RINFormatChecker(network, nodeLables);
		if (rinChecker.getErrorStatus() == null) {
			return true;
		}
		return false;
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new ExtractSubnetworkTask(context, network));
	}

}
