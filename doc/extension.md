### Using RINalyzer 2.x as a Library

The main features of RINalyzer 2.x are implemented as separate tasks and can be called by other apps or executed as commands. The RINalyzer tasks use the namespace `rinalyzer` and a list of the corresponding commands is given below:
*   Import RIN from File: `importRIN`
*   Initialize Default RIN Visual Properties: `initRinVisProps`
*   Compare RINs: `compareRINs`
*   Create Aggregated RIN: `createAggregatedRIN`
  
Available only in the GUI version:  
*   RIN Visual Properties: `applyVisProps`
*   Analyze Network: `analyzeNetwork`
*   Extract Subnetwork: `extractSubnetwork`

RINalyzer also makes use of the structureViz tasks with the namespace structureViz. A comprehensive description of the `structureViz2` commands and how they can be used is available [here](http://www.cgl.ucsf.edu/cytoscape/structureViz2/#commands). Below is a short list of the commands important for RINalyzer users:

*   Create Residue Network: `createRIN`
*   Annotate Residue Network: `annotateRIN`
*   Syncronize Residue Colors: `syncColors`
  
*   Open Structures: `open`
*   Close Structures: `close`
*   Launch Chimera: `launch`
*   Exit Chimera: `exit`
*   Send Command to Chimera: `send`
*   Settings...: `set`

Here is an example for using one of the tasks listed above. To call another task, just replace `commandNamespace` and `command` with the appropriate values.

	// get the registrar in the CyActivator class
	CyServiceRegistrar registrar = getService(bundleContext, CyServiceRegistrar.class);
	
	...
	
	// find the task factory with namespace structureViz and command createRIN
	TaskFactory createStrNetTaskFactory = (TaskFactory) registrar.getService(context, TaskFactory.class, (&(commandNamespace=structureViz)(command=createRIN)));
	if (createStrNetTaskFactory != null) {
		// execute the task using the task factory
		insertTasksAfterCurrentTask(createStrNetTaskFactory.createTaskIterator());
	}

An example for applying the RIN layout is shown below:

	// get the registrar in the CyActivator class
	CyServiceRegistrar registrar = getService(bundleContext, CyServiceRegistrar.class);
	
	...
	
	// get the layout manager
	CyLayoutAlgorithmManager manager = (CyLayoutAlgorithmManager) registrar.getService(CyLayoutAlgorithmManager.class);
	// get the RIN layout
	CyLayoutAlgorithm rinlayout = manager.getLayout("rin-layout");
	if (rinlayout != null) {
		// get the task manager
		TaskManager taskManager = (TaskManager) registrar.getService(TaskManager.class);
		// execute the layout
		taskManager.execute(rinlayout.createTaskIterator(rinView, rinlayout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL\_NODE\_VIEWS, null));
	} 	


* * *

### Using RINalyzer 1.x as a Library

Some of the methods implemented by RINalyzer can be accessed by other plugins. For this purpose, the RINalyzer.jar file has to be stored in an appropriate location on your file system and you need to add it to your Java classpath. For example, in order to implement a new layout algorithm for RINs, you will need the current coordinates of the residues in UCSF Chimera, which can be accessed as shown in the code fragment below.

	// get selected network
	final CyNetwork network = Cytoscape.getCurrentNetwork();
	if (network == null) {
		return;
	}
	// get chimera instance
	final Chimera chimera = RINalyzerPlugin.getChimera();
	if (!chimera.isLaunched()) {
		// tell the user to launch Chimera and load the corresponding structure
		return;
	}
	// get residue coordinates for selected network
	final Map resCoord = chimera.getResidueCoordinates(network);
	if (resCoord == null || resCoord.size() == 0) {
		return;
	}
	// use coordinates
	// ...


* * *

### Developing a new Cytoscape Plugin

Cytoscape is an open-source platform, which provides a basic set of features for data integration and visualization. Additional features can be implemented as apps or plugins. A wide range of such apps/plugins is already publicly available [here](http://apps.cytoscape.org/). Users are always encouraged to extend Cytoscape and to combine different apps/plugins in their analysis workflow. Therefore, it is convenient to add new functionality to RINalyzer following the same idea.

For example, RINalyzer can be extended by implementing new centrality measures for the analysis of RINs. The input is the RIN data, i.e., the network and attributes as downloaded from our web page and loaded into Cytoscape (see [Tutorial 1](./tut/tutorial6.md)). The RIN data is always formatted as described [here](rins_spec.md). Once the new measures are computed, the results can be visualized in the network using the [Filters](http://wiki.cytoscape.org/Cytoscape_3/UserManual#Finding_and_Filtering_Nodes_and_Edges) provided by Cytoscape and in the protein structure using [UCSF Chimera](structure.md).

Quick start guides for developing new Cytoscape apps/plugins are available [here](http://www.cytoscape.org/documentation_developers.html). You may also want to become a member of the Cytoscape's [mailing lists](http://www.cytoscape.org/community.html).


  