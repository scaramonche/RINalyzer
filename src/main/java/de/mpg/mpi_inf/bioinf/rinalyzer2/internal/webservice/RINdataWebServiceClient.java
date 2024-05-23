package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.webservice;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ImportFromURLTask;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.UtilsUI;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class RINdataWebServiceClient extends AbstractWebServiceGUIClient implements
		NetworkImportWebServiceClient, ActionListener {

	private static String uri = "http://rinalyzer.de/rin-download/download?filename=";
	private static String displayName = "RINdata Web Service Client";
	private static String descripton = "RINdata Web Service Client. Retreives RIN data from http://www.rinalyzer.de/rindata.php";
	private CyServiceRegistrar context;
	private RINVisualPropertiesManager rinVisPropsManager;

	public RINdataWebServiceClient(CyServiceRegistrar context, RINVisualPropertiesManager rinManager) {
		super(uri, displayName, descripton);
		this.context = context;
		this.rinVisPropsManager = rinManager;
		init();
	}

	public TaskIterator createTaskIterator() {
		return null;
	}

	public TaskIterator createTaskIterator(Object query) {
		if (query != null && query.toString().length() != 0) {
			return new TaskIterator(new ImportFromURLTask(context, rinVisPropsManager, uri
					+ query.toString().toLowerCase()));
		} else {
			return new TaskIterator(new ImportFromURLTask(context, rinVisPropsManager, ""));
		}
	}

	public Container getQueryBuilderGUI() {
		return mainPanel;
	}

	private JTextField jtxtPDB;

	private JButton jbtnQuery;

	private JPanel mainPanel;

	private JTextField jtxtChimeraPath;

	private JPanel panChimera;

	public void actionPerformed(ActionEvent e) {
		final Object src = e.getSource();
		String pdb = null;
		if (src == jbtnQuery) {
			pdb = jtxtPDB.getText();
			if (!"".equals(jtxtChimeraPath.getText())) {
				CyUtils.setDefaultChimeraPath(context, Messages.SV_CHIMERAPROPERTYNAME,
						Messages.SV_CHIMERAPATHPROPERTYKEY, jtxtChimeraPath.getText());
				// init();
			}
			TaskManager<?, ?> taskManager = (TaskManager<?, ?>) CyUtils.getService(context,
					TaskManager.class);
			taskManager.execute(this.createTaskIterator(pdb));
		}
	}

	private void init() {
		final int BS = UtilsUI.BORDER_SIZE;
		mainPanel = new JPanel(new BorderLayout(BS, BS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));
		// if (CyUtils.getDefaultChimeraPath(context, Messages.SV_CHIMERAPROPERTYNAME,
		// Messages.SV_CHIMERAPATHPROPERTYKEY).equals("")) {
		// }
		// mainPanel.add(pan2, BorderLayout.SOUTH);
		panChimera = new JPanel(new BorderLayout(BS, BS));
		jtxtChimeraPath = new JTextField(20);
		panChimera.add(UtilsUI.createLabel(Messages.DI_CHIMERAPATH, null), BorderLayout.LINE_START);
		panChimera.add(jtxtChimeraPath, BorderLayout.CENTER);
		mainPanel.add(panChimera, BorderLayout.PAGE_START);
		JPanel pan = new JPanel(new BorderLayout(BS, BS));
		jtxtPDB = new JTextField(10);
		jbtnQuery = new JButton(Messages.DI_RETRIEVEDATA);
		jbtnQuery.addActionListener(this);
		pan.add(UtilsUI.createLabel(Messages.DI_ENTERPDB, null), BorderLayout.LINE_START);
		pan.add(jtxtPDB, BorderLayout.CENTER);
		mainPanel.add(pan, BorderLayout.CENTER);
		JPanel pan3 = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		pan3.add(jbtnQuery);
		mainPanel.add(pan3, BorderLayout.PAGE_END);
	}

}
