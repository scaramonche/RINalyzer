# RINalyzer

News
----

**June 2014**   
A new version of RINalyzer for Cytoscape 3.x is released. RINalyzer has been greatly extended with new functionality and is now using the [structureViz2 app](http://www.cgl.ucsf.edu/cytoscape/structureViz2/) to interact with UCSF Chimera. For a brief introduction of the new features click [here](./doc/app_features.md). The RINalyzer app is now available for download [here](https://apps.cytoscape.org/apps/rinalyzer) and the structureViz app [here](http://apps.cytoscape.org/apps/structureViz2). All requirements and installation instructions are available [here](./doc/install.md).  

**December 2014**   
A new version of RINerator is released. In particular, it computes conservation scores from a user-specified multiple sequence alignment, retrieves biochemical amino acid properties from external resources, and generates RINs compatible with Cytoscape 3.x and RINalyzer 2.x. For more information, see the [RINerator webpage](./doc/rinerator.md).  

**March 2014**   
We presented a poster at VIZBI 2014 (abstract available [here](http://www.vizbi.org/Posters/2014/D16)).

![Figure](doc/images/app3.png)


* * *

Description
-----------

RINalyzer provides a number of important methods for analyzing and visualizing residue interaction networks (RINs) as shown in these [examples](./doc/gallery.md). A RIN is constructed from the three-dimensional structure of a protein as stored in PDB files from the [Protein Data Bank](http://www.rcsb.org/). Network nodes and edges represent amino acid residues and their molecular interactions, respectively. The network topology of RINs is normally characterized by undirected and weighted interaction edges between residue nodes. RINalyzer is a Java plugin for [Cytoscape](http://www.cytoscape.org), a free software platform for the analysis and visualization of molecular interaction networks.

RINalyzer allows simultaneous, interactive 2D visualization and exploration of a RIN in Cytoscape, together with the corresponding molecular 3D structure visualized in [UCSF Chimera](http://www.cgl.ucsf.edu/chimera/). Furthermore, RINalyzer offers the computation and illustration of a comprehensive set of weighted centrality measures for relating spatially distant residue nodes and discovering critical residues and their long-range interaction paths in protein structures. Another feature is the network comparison of aligned protein structures by constructing a combined RIN, which enables the detailed comparative analysis of residue interactions in different proteins. In addition, RINalyzer supports the interactive exploration by providing easy-to-use filters and the generation of subnetworks and aggregated networks. A list of all main features of RINalyzer as well as an overview screenshot can be found [here](./doc/features.md).

RINalyzer is complemented by the [RINerator module](./doc/rinerator.md), which generates user-defined RINs from a 3D protein structure. In contrast to previous simplistic interaction definition approaches based on spatial atomic distance between residues, RINerator enables a more realistic representation by considering different biochemical interaction types and even quantifying the strength of individual interactions.

Since version 2.0 RINalyzer is using the [structureViz2 app](http://www.cgl.ucsf.edu/cytoscape/structureViz2/) to interact with UCSF Chimera and both apps complement each other.

A detailed documentation as well as storage and format specifications can be found [here](./doc/documentation.md).

A tutorial for the first steps of using RINalyzer is available [here](./doc/tutorials.md). Further information and tutorials on using Cytoscape are available [here](http://cytoscape.org/documentation_users.html).

    

* * *

References
----------

- Main paper   
Doncheva, N.T., Klein, K., Domingues, F.S., Albrecht, M. (2011). **Analyzing and visualizing residue networks of protein structures.** *Trends in Biochemical Sciences*, **36**(4):179-182, [doi:10.1016/j.tibs.2011.01.002](http://dx.doi.org/doi:10.1016/j.tibs.2011.01.002).

- Tutorial   
Doncheva, N.T., Assenov, Y., Domingues, F.S., Albrecht, M. (2012). **Topological analysis and interactive visualization of biological networks and protein structures.** *Nature Protocols*, **7**:670-685, [doi:10.1038/nprot.2012.004](http://dx.doi.org/doi:10.1038/nprot.2012.004).

- Application   
Doncheva, N.T., Klein, K., Morris, J.H., Wybrow, M., Domingues, F.S., and Albrecht, M. (2014). **Integrative visual analysis of protein sequence mutations.** *BMC Proceedings*, **8**(Suppl 2):S2, [doi:10.1186/1753-6561-8-S2-S2](http://dx.doi.org/doi:10.1186/1753-6561-8-S2-S2).


* * *

Releases
--------

You can download the latest release of RINalyzer [here](https://apps.cytoscape.org/apps/rinalyzer). For Cytoscape 3.x, you also need to download [structureViz](http://apps.cytoscape.org/apps/structureViz2). If you have trouble installing RINalyzer, you can read further instructions [here](./doc/install.md).

**Release 2.0 (June 2014) is verified to work with Cytoscape 3.1.x and UCSF Chimera 1.8.1 (and above).**    
Release 1.3 (November 2011) is verified to work with Cytoscape 2.8.x and UCSF Chimera 1.5.x.  
Release 1.2 (January 2011) is verified to work with Cytoscape 2.8.0 and the newest version of UCSF Chimera 1.5.  
Release 1.1 (September 2010) is verified to work with Cytoscape 2.7.0 and UCSF Chimera 1.2, 1.3, 1.4.  
Release 1.0 (September 2010) is verified to work with Cytoscape 2.6.x and UCSF Chimera 1.2, 1.3, 1.4.

* * *

RIN Data
--------

RINs compatible with RINalyzer and structureViz can be created in several different ways:

1.  RINs can be generated on the fly from a currently open structure in UCSF Chimera using the Chimera built-in methods for contact and hydrogen bond detection.  
2.  The [RINerator](./doc/rinerator.md) module can be used to generate specific RINs, for example, including ligands, or in a batch fashion for multiple structures. 
3.  The [RING](http://protein.cribi.unipd.it/ring/) web server can also be used to generate different types of RINs.
  

* * *

Contributors
------------

Nadezhda T. Doncheva (1), Karsten Klein (2), John "Scooter" Morris (3), Francisco S. Domingues (1,4), and Mario Albrecht (1,5,6,7)

Affiliations as of 2014:  
1. Max Planck Institute for Informatics, Saarbrücken, Germany  
2. The University of Sydney, Sydney, Australia  
3. University of California, San Francisco, San Francisco, USA  
4. Institute of Genetic Medicine, EURAC research, Bolzano, Italy  
5. University Medicine Greifswald, Greifswald, Germany  
6. Graz University of Technology, Graz, Austria  
7. BioTechMed-Graz, Graz, Austria  


* * *

Contact
-------

If you have any questions, comments, suggestions or criticism, feel free to create an issue here on GitHub.

