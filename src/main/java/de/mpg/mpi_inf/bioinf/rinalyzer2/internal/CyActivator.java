package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout.RINLayout;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.webservice.RINdataWebServiceClient;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		RINVisualPropertiesManager rinVisPropManager = new RINVisualPropertiesManager(registrar);
		registerService(bc, rinVisPropManager, NetworkAboutToBeDestroyedListener.class,
				new Properties());

		RINdataWebServiceClient rinClient = new RINdataWebServiceClient(registrar,
				rinVisPropManager);
		registerAllServices(bc, rinClient, new Properties());

		Properties rinLayoutProps = new Properties();
		RINLayout rinLayout = new RINLayout(registrar, "rin-layout", "RINLayout", null);
		// rinLayoutProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		rinLayoutProps.setProperty(IN_MENU_BAR, "true");
		registerService(bc, rinLayout, CyLayoutAlgorithm.class, rinLayoutProps);

		// Import/Create RINs
		TaskFactory importFactory = new ImportFromFileTaskFactory(registrar, rinVisPropManager);
		Properties importProps = new Properties();
		importProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		importProps.setProperty(TITLE, "Import RIN from File");
		importProps.setProperty(COMMAND, "importRIN");
		importProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		importProps.setProperty(IN_MENU_BAR, "true");
		importProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, importFactory, TaskFactory.class, importProps);

		NetworkViewTaskFactory visPropFactory = new RINVisualPropertiesTaskFactory(registrar,
				rinVisPropManager);
		Properties visPropProps = new Properties();
		visPropProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		visPropProps.setProperty(TITLE, "RIN Visual Properties");
		visPropProps.setProperty(COMMAND, "applyVisProps");
		visPropProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		visPropProps.setProperty(ENABLE_FOR, "networkAndView");
		visPropProps.setProperty(IN_MENU_BAR, "true");
		visPropProps.setProperty(MENU_GRAVITY, "2.0");
		registerService(bc, visPropFactory, NetworkViewTaskFactory.class, visPropProps);

		TaskFactory openStructureFileFactory = new OpenStructureFileTaskFactory(registrar);
		Properties openStructureFileProps = new Properties();
		openStructureFileProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		openStructureFileProps.setProperty(TITLE, "Open Structure from File");
		openStructureFileProps.setProperty(IN_MENU_BAR, "true");
		openStructureFileProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		openStructureFileProps.setProperty(MENU_GRAVITY, "3");
		registerService(bc, openStructureFileFactory, TaskFactory.class, openStructureFileProps);

		TaskFactory openStructureFactory = new OpenStructureTaskFactory(registrar);
		Properties openStructureProps = new Properties();
		openStructureProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		openStructureProps.setProperty(TITLE, "Open Protein Structure");
		openStructureProps.setProperty(ENABLE_FOR, "networkAndView");
		openStructureProps.setProperty(IN_MENU_BAR, "true");
		openStructureProps.setProperty(MENU_GRAVITY, "3.5");
		registerService(bc, openStructureFactory, TaskFactory.class, openStructureProps);

		TaskFactory closeStructureFactory = new CloseStructureTaskFactory(registrar);
		Properties closeStructureProps = new Properties();
		closeStructureProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		closeStructureProps.setProperty(TITLE, "Close Protein Structure");
		closeStructureProps.setProperty(ENABLE_FOR, "networkAndView");
		closeStructureProps.setProperty(IN_MENU_BAR, "true");
		closeStructureProps.setProperty(MENU_GRAVITY, "4.0");
		registerService(bc, closeStructureFactory, TaskFactory.class, closeStructureProps);

		TaskFactory createRINFactory = new CreateRINTaskFactory(registrar);
		Properties createRINProps = new Properties();
		createRINProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		createRINProps.setProperty(TITLE, "Create RIN from Chimera");
		createRINProps.setProperty(IN_MENU_BAR, "true");
		createRINProps.setProperty(MENU_GRAVITY, "5.0");
		createRINProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		registerService(bc, createRINFactory, TaskFactory.class, createRINProps);

		TaskFactory annotateRINFactory = new AnnotateRINTaskFactory(registrar);
		Properties annotateRINProps = new Properties();
		annotateRINProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		annotateRINProps.setProperty(TITLE, "Annotate RIN from Chimera");
		annotateRINProps.setProperty(ENABLE_FOR, "network");
		annotateRINProps.setProperty(IN_MENU_BAR, "true");
		annotateRINProps.setProperty(MENU_GRAVITY, "6.0");
		registerService(bc, annotateRINFactory, NetworkTaskFactory.class, annotateRINProps);

		TaskFactory syncRINColorsFactory = new SyncRINColorsTaskFactory(registrar);
		Properties syncRINColorsProps = new Properties();
		syncRINColorsProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		syncRINColorsProps.setProperty(TITLE, "Sync RIN Colors with Chimera");
		syncRINColorsProps.setProperty(ENABLE_FOR, "networkAndView");
		syncRINColorsProps.setProperty(IN_MENU_BAR, "true");
		syncRINColorsProps.setProperty(MENU_GRAVITY, "7.0");
		registerService(bc, syncRINColorsFactory, NetworkViewTaskFactory.class, syncRINColorsProps);

		NetworkViewTaskFactory analysisFactory = new AnalyzeTaskFactory(registrar);
		Properties analysisProps = new Properties();
		analysisProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		analysisProps.setProperty(TITLE, "Analyze Network");
		analysisProps.setProperty(COMMAND, "analyzeNetwork");
		analysisProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		analysisProps.setProperty(ENABLE_FOR, "networkAndView");
		analysisProps.setProperty(IN_MENU_BAR, "true");
		analysisProps.setProperty(MENU_GRAVITY, "8.0");
		analysisProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		registerService(bc, analysisFactory, NetworkViewTaskFactory.class, analysisProps);

		NetworkTaskFactory extractSubnetworkFactory = new ExtractSubnetworkTaskFactory(registrar);
		Properties extractSubnetworkProps = new Properties();
		extractSubnetworkProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		extractSubnetworkProps.setProperty(TITLE, "Extract Subnetwork");
		extractSubnetworkProps.setProperty(COMMAND, "extractSubnetwork");
		extractSubnetworkProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		extractSubnetworkProps.setProperty(ENABLE_FOR, "network");
		extractSubnetworkProps.setProperty(IN_MENU_BAR, "true");
		extractSubnetworkProps.setProperty(MENU_GRAVITY, "9.0");
		registerService(bc, extractSubnetworkFactory, NetworkTaskFactory.class,
				extractSubnetworkProps);

		NetworkTaskFactory aggregatedNetworkFactory = new AggregatedNetworkTaskFactory(registrar,
				rinVisPropManager);
		Properties extractSSProps = new Properties();
		extractSSProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		extractSSProps.setProperty(TITLE, "Create Aggregated RIN");
		extractSSProps.setProperty(COMMAND, "createAggregatedRIN");
		extractSSProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		extractSSProps.setProperty(ENABLE_FOR, "network");
		extractSSProps.setProperty(IN_MENU_BAR, "true");
		extractSSProps.setProperty(MENU_GRAVITY, "10.0");
		registerService(bc, aggregatedNetworkFactory, NetworkTaskFactory.class, extractSSProps);

		NetworkTaskFactory comparisonFactory = new CompareRINsTaskFactory(registrar,
				rinVisPropManager);
		Properties comparisonProps = new Properties();
		comparisonProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		comparisonProps.setProperty(TITLE, "Compare RINs");
		comparisonProps.setProperty(COMMAND, "compareRINs");
		comparisonProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		comparisonProps.setProperty(ENABLE_FOR, "network");
		comparisonProps.setProperty(IN_MENU_BAR, "true");
		comparisonProps.setProperty(MENU_GRAVITY, "11.0");
		registerService(bc, comparisonFactory, NetworkTaskFactory.class, comparisonProps);

		RINalyzerAboutTaskFactory aboutFactory = new RINalyzerAboutTaskFactory(registrar);
		Properties aboutProps = new Properties();
		aboutProps.setProperty(PREFERRED_MENU, "Apps.RINalyzer");
		aboutProps.setProperty(TITLE, "About");
		aboutProps.setProperty(ENABLE_FOR, "always");
		aboutProps.setProperty(COMMAND, "about");
		aboutProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		aboutProps.setProperty(IN_MENU_BAR, "true");
		aboutProps.setProperty(MENU_GRAVITY, "12.0");
		aboutProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		registerService(bc, aboutFactory, TaskFactory.class, aboutProps);

		// Factories not shown in the menu
		NetworkTaskFactory initVisPropFactory = new RINInitVisPropsTaskFactory(registrar,
				rinVisPropManager);
		Properties initVisPropProps = new Properties();
		initVisPropProps.setProperty(COMMAND, "initRinVisProps");
		initVisPropProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		initVisPropProps.setProperty(ENABLE_FOR, "networkAndView");
		registerService(bc, initVisPropFactory, NetworkTaskFactory.class, initVisPropProps);

		// NetworkTaskFactory extractDegreeSubnetworkFactory = new
		// ExtractDegreeSubnetworkTaskFactory(
		// registrar);
		// Properties extractDegreeSubnetworkProps = new Properties();
		// extractDegreeSubnetworkProps.setProperty(PREFERRED_MENU, "Apps.RINalyzerUtils");
		// extractDegreeSubnetworkProps.setProperty(TITLE, "Extract Degree Subnetwork");
		// extractDegreeSubnetworkProps.setProperty(COMMAND, "extractDegreeSubnetwork");
		// extractDegreeSubnetworkProps.setProperty(COMMAND_NAMESPACE, "rinalyzer");
		// extractDegreeSubnetworkProps.setProperty(ENABLE_FOR, "network");
		// extractDegreeSubnetworkProps.setProperty(IN_MENU_BAR, "true");
		// extractDegreeSubnetworkProps.setProperty(MENU_GRAVITY, "9.0");
		// registerService(bc, extractDegreeSubnetworkFactory, NetworkTaskFactory.class,
		// extractDegreeSubnetworkProps);

	}

}
