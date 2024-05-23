New Features
------------

### Interface to UCSF Chimera   
The new interface to UCSF Chimera is implemented in the structureViz app and basically runs in the background. structureViz keeps track of open structures and automatically associates them with the corresponding networks, nodes and edges in Cytoscape if and only if the nodes are correctly annotated. The required attributes are generated automatically for each RIN if any of the three methods for importing and generating RINs described below is used. See an extended description [here](./structure.md).

### Import and generation of RINs
- Import RIN from Web Service  
This feature allows the direct retrieval of RIN data from our web service [RINdata](http://rinalyzer.de/rindata.php). It automatically imports the RIN and the associated attributes as well as opens the corresponding PDB structure in UCSF Chimera. Since the protein structure cannot be saved in Cytoscape, it is saved for further use into USER\_HOME/CytoscapeConfigureation/pdbs/.
- Import RIN from File  
This option should be used to import any RIN, which is supported so far by RINalyzer or follows [these specifications](./rins_spec.md)). In this way, the attribute data required for associating the RIN with a protein structure in UCSF Chimera is generated automatically.
- Create RIN from Chimera
The completely new functionality to generate RINs from a selection of residues in UCSF Chimera is implemented in the structureViz app and can be invoked from RINalyzer as well. The selection can include amino acid residues, solvent molecules, ligands, etc. Currently, five types of edges can be created: contacts, clashes, hydrogen bonds, connectivity (backbone), and Cα distances. See also the [structureViz app webpage](http://www.cgl.ucsf.edu/cytoscape/structureViz2/) for more details.

### RIN Layout  
This layout is specifically implemented for RINs and synchronizes orientation and location between a RIN and the corresponding structure. It automatically retrieves the current 3D coordinates of the residues and uses their projection on a 2D plane as a starting point for a distance-based stress minimization layout.
  
### Annotation of RINs  
The RINalyzer and structureViz apps can transfer residue attributes from UCSF Chimera as node attributes to the corresponding RIN in Cytoscape. In particular, these attributes include secondary structure, residue coordinates, hydrophobicity, solvent accessible surface area (if already computed in UCSF Chimera), occupancy, etc. See also the [structureViz app webpage](http://www.cgl.ucsf.edu/cytoscape/structureViz2/) for more details.

### Exploration of RINs
- Extract Interfaces  
In addition to creating a new network for a single or multiple chains in a RIN, RINalyzer can extract a new subnetwork consisting of the interface residues and their interactions between two or more chains. Interface residues are defined as residues with at least one non-covalent interaction to a residue in another chain. This option can be found in the RINalyzer menu Extract Subnetwork.
- Create Aggregated RIN  
This feature invokes the generation of an aggregated network, in which a node represents a group of consecutive residues with the same characteristic, for example, protein chain, domain, or secondary structure element.
- Edge Distance Filter  
This option is included in the RIN Visual Properties dialog and allows hiding edges between residues closer in sequence than the specified threshold.
  
### Comparison of RINs
The comparison functionality of RINalyzer has been greatly improved and linked to the structure alignment tool of UCSF Chimera, i.e., two RINs can be compared based on the sequence or structure alignment of their associated protein structures. Alternatively, the mapping of nodes can be provided as a FASTA alignment file or a simple node-to-node text mapping file. In addition, the difference of edge weights (represented by a numeric edge attribute with the same name in both RINs) can be computed and mapped to the edges in the comparison network. See more documentation [here](./comparison.md).
  
### Node and edge sets  
The node sets functionality of RINalyzer has been replaced by a new app called [setsApp](http://apps.cytoscape.org/apps/setsApp), which is available for download from the Cytoscape App Store. In addition to sets of nodes, it also supports various operations on sets of edges. For more documentation, see [here](http://www.cgl.ucsf.edu/cytoscape/utilities3/setsApp.shtml).
  


* * *

RINalyzer App Menus
-------------------

* `RINdata Web Service Client`  
Import a RIN for a user-specified PDB identifier from [RINdata](http://rinalyzer.de/rindata.php). The corresponding edge attributes and protein structure are also imported. See more documentation [here](./import.md#import).
  
* `Import RIN from File`  
Import a RIN as supported so far by RINalyzer (see [RIN Specification](rins_spec.md)). See more documentation [here](./import.md#import).
  
* `RIN Visual Properties`  
Hide edges by type or distance and change the appearance of a RIN by mapping some node or edge attributes to visual properties of the network. See more documentation [here](./visualprops.md).
  
* `Open Structure from File`  
Open a protein structure file provided by the user for the current RIN (the structureViz app should be installed). See more documentation [here](./structure.md).
  
* `Open Protein Structure`  
Open a protein structure associated with an arbitrary node in the current RIN (the structureViz app should be installed). See more documentation [here](./structure.md).
  
* `Close Protein Structure`  
Close a protein structure associated with an arbitrary node in the current RIN (the structureViz app should be installed). See more documentation [here](./structure.md).
  
* `Create RIN from Chimera`  
Create a RIN from a current selection in UCSF Chimera (the structureViz app should be installed). See more documentation [here](./import.md#generation).
  
* `Annotate RIN from Chimera`  
Annotate the current RIN with structural information from a currently open and associated protein structure in UCSF Chimera (the structureViz app should be installed). See more documentation [here](./structure.md).
  
* `Sync RIN Colors with Chimera`  
Synchronize node and residue colors between the current RIN and its associated protein structure (the structureViz app should be installed). See more documentation [here](./structure.md).
  
* `Analyze Network`  
Compute weighted centrality measures with respect to a set of selected nodes. See more documentation [here](./cent_analysis.md).
  
* `Extract Subnetwork`  
Create a new network containing the nodes and interactions within or between user-specified protein chain(s). See more documentation [here](./subnetwork.md).
  
* `Create Aggregated RIN`  
Create an aggregated RIN based on a property such as secondary structure. See more documentation [here](./aggnetwork.md).
  
* `Compare RINs`  
Compare two RINs based on a structure alignment of the corresponding 3D protein structures. See more documentation [here](./comparison.md).
  
* `About`  
Show the About box of RINalyzer including contributors and reference for citation.
