package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.VisualPropsDialog;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class RINVisualPropertiesTask extends AbstractTask {

	private CyServiceRegistrar context;
	private RINVisualPropertiesManager rinVisPropsManager;
	private CyNetworkView networkView;

	public RINVisualPropertiesTask(CyServiceRegistrar bc, RINVisualPropertiesManager aRINVisPropManager,
			CyNetworkView aNetworkView) {
		context = bc;
		rinVisPropsManager = aRINVisPropManager;
		networkView = aNetworkView;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		VisualPropsDialog dialog = rinVisPropsManager.getDialog();
		if (dialog == null) {
			return;
		}
		dialog.initializeProps(networkView);
		dialog.setLocationRelativeTo(CyUtils.getCyFrame(context));
		dialog.setVisible(true);
	}

}
