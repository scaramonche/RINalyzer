package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import javax.swing.SwingUtilities;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.AboutDialog;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;

public class RINalyzerAboutTask extends AbstractTask {

	private CyServiceRegistrar context;

	public RINalyzerAboutTask(CyServiceRegistrar bc) {
		context = bc;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// show about dialog
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				init();
			}
		});
	}

	private void init() {
		AboutDialog d = new AboutDialog(CyUtils.getCyFrame(context));
		d.setVisible(true);
	}
}
