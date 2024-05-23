RINalyzer Features
------------------

RINalyzer provides several features that support the generation, analysis and visualization of RINs. They can be grouped in the following categories:

1.  [Import and Create RINs](import.md)  
    This is a group of features including the automatic retrieval of RINs from the [RINdata](http://rinalyzer.de/rindata.php) web service, the import of RINs into Cytoscape as well as the generation of RINs from a current selection of residues or structures in UCSF Chimera.
    
2.  [Visual Properties](visualprops.md)  
    This menu option opens a dialog that allows adjusting the visual style of the network view by changing different properties. They include node color (based on the secondary structure of the protein represented by the RIN), node label format, node and label size, edge color (based on the edge type), edge width and edge line type. All these properties are individually adjustable for each network. A group of properties can be stored as default and restored anytime. In addition, edges representing the protein backbone can be added to the network, edges can be shown or hidden in the network view depending on their type or sequence distance between their interacting residues.
    
3.  [Interface to UCSF Chimera](structure.md)  
    This is a group of menu items that provide options for loading the PDB structure of the currently selected RIN in Chimera. The PDB file can be opened either from the local file system or downloaded from the PDB web server by Chimera. When both the network and the corresponding structure are loaded, a selection of entities in either of the views will lead to a selection of the respective entities in the other. Additionally, the node colors and the network layout of the RIN can be synchronized with the 3D view in Chimera.  
    Since version 2.x of RINalyzer, the [structureViz app](http://apps.cytoscape.org/apps/structureViz2) has to be installed for this and additional functionality such as the generation of RINs from a selection in UCSF Chimera and the annotation of RINs with structural properties.
    
4.  [Analyze Network](cent_analysis.md)  
    This menu option invokes the centrality analysis of the selected network with respect to a subset of selected nodes within the network. The computed centrality measures are weighted degree; shortest path betweenness and closeness; current flow betweenness and closeness; random walk betweenness and closeness. One can select an edge attribute that defines weights, specify default weight for missing values, and pick among different options for handling multiple edges, negative weights, and converting similarity scores into distance scores. The computed centrality values are stored as node attributes and can be displayed in a table or saved to a text file. For each centrality measure, a selection filter is created that selects nodes according to their centrality values.
    
5.  [Compare RINs](comparison.md)  
    RINalyzer offers the functionality to compare RINs by constructing a combined RIN that allows investigating the residue interaction differences between the superimposed 3D protein structures. The RIN nodes are mapped according to the structure alignment of the corresponding 3D protein structures, which is provided by the user. The view of the combined network is customized to allow the investigation of the presence and absence of essential residue interactions in protein structures, which are difficult to see in 3D.
    
6.  [Extract Subnetwork](subnetwork.md)  
    This action is intended for RINs generated from proteins with multiple chains and allows extracting a single chain, a group of chains, or the interface between chains as a new network.
    
7.  [Create Aggregated RIN](aggnetwork.md)  
    This feature invokes the generation of an aggregated RIN, in which nodes represent groups of consecutive residues with the same characteristic, for example, chains, domains, or secondary structure elements.
    
8.  [Manage Node Sets](nodesets.md)  
    This is the interface for defining and storing node sets. New sets can be created from selected nodes in the network view or from boolean node attributes. Each set or group of sets can be saved locally and loaded again into the Cytoscape session. Selected nodes can be added to and deleted from the sets, and basic operations like inversion, union, intersection, and difference of sets can be performed. In addition, subnetwork can be created that contains solely the nodes from a selected set and their adjacent edges. Last but not least, the centrality analysis can be started directly from this menu.  
    Since version 2.x of RINalyzer, the node sets functionality has been replaced by a new app called [setsApp](http://apps.cytoscape.org/apps/setsApp), which is available for download from the Cytoscape App Store. In addition to sets of nodes, it also supports various operations on sets of edges. For more documentation, see [here](http://www.cgl.ucsf.edu/cytoscape/utilities3/setsApp.shtml).
    

* * *

Complementary Cytoscape 3.x Apps
--------------------------------

All Cytoscape 3.x apps are described [here](http://apps.cytoscape.org/). A list of useful apps in addition to RINalyzer is given below:

*   [setsApp](http://apps.cytoscape.org/apps/setsapp) allows the user to create and manipulate sets of nodes or edges.
*   [PathExplorer](http://apps.cytoscape.org/apps/pathexplorer) highlights paths from and to a user-defined source and target node, respectively
*   [eXamine](http://apps.cytoscape.org/apps/examine) is a set-oriented visual analysis approach for annotated modules that displays set membership as contours on top of a node-link layout.
*   [CentiScaPe](http://apps.cytoscape.org/apps/centiscape) computes many centrality parameters describing the network topology.
*   Clustering:
    *   [clusterMaker](http://apps.cytoscape.org/apps/clustermaker2) unifies different clustering techniques (MCL and FORCE) and displays them in a single interface and can be used to explore the cluster statistics and relationships.
    *   [clusterViz](http://apps.cytoscape.org/apps/clusterviz) is a plugin for clustering biological networks using the FAG-EC, EAGLE or MCODE algorithms.
    *   [MCODE](http://apps.cytoscape.org/apps/mcode) finds clusters (highly interconnected regions) in a network.
    *   [ClusterONE](http://apps.cytoscape.org/apps/clusterone) implements a graph clustering algorithm that is able to handle weighted graphs and readily generates overlapping clusters.

  

* * *

Complementary Cytoscape 2.x Plugins
-----------------------------------

All Cytoscape 2.x plugins are described [here](http://apps.cytoscape.org/). A list of useful plugins in addition to RINalyzer is given below:

*   [NetworkAnalyzer](http://med.bioinf.mpi-inf.mpg.de/netanalyzer/) performs analysis of biological networks and calculates network topology parameters. It is included in Cytoscape since version 2.8.
*   [NeighborHighlight](http://chianti.ucsd.edu/cyto_web/plugins/pluginjardownload.php?id=136) highlights the current node and all its neighboring nodes and edges when the user hovers the mouse over it.
*   [ShortestPath](http://www.rbvi.ucsf.edu/Research/cytoscape/shortestPath/index.html) shows the shortest path between two selected nodes in the current network.
*   [HiderSlider](http://sourceforge.net/projects/hanalyzer/) allows a user to interactively explore a network by hiding/showing nodes or edges with attribute values above an adjustable threshold using a slider bar.
*   [EdgeLister](http://chianti.ucsd.edu/cyto_web/plugins/pluginjardownload.php?id=105) provides a means of tracking edge selection.
*   [RandomNetworks](http://sites.google.com/site/randomnetworkplugin/) can be used to generate random networks, randomize existing networks and compare existing networks to random models.
*   Clustering:
	*   [clusterMaker](http://www.rbvi.ucsf.edu/cytoscape/cluster/clusterMaker.html) unifies different clustering techniques (MCL and FORCE) and displays them in a single interface and [clusterExplorer](http://www.rbvi.ucsf.edu/cytoscape/cluster/clusterExplorer.html) may be used to explore the cluster statistics and relationships.
	*   [clusterViz](http://code.google.com/p/clusterviz-cytoscape/) is a plugin for clustering biological networks using the FAG-EC, EAGLE or MCODE algorithms.
	*   [MCODE](http://baderlab.org/Software/MCODE) finds clusters (highly interconnected regions) in a network.
	*   [CytoMCL](http://bioingegneria.unicz.it/cytomcl/) clusters networks using the MCL clustering algorithm.

  