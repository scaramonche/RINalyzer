In this tutorial, it is assumed that you have followed the steps described in Tutorial 1, i.e. the Cytoscape session _pdb1hiv_h.cys_ is opened and the corresponding 3D structure is loaded in Chimera.

Download ConSurf-DB data
------------------------

*   Go to our RIN data download page (not avilable at the moment).
*   Enter the PDB identifier *1hiv* in the search form ([image](images/tut5.1_1.png)) and click the Search for RIN data button.
*   Besides the RIN data for *1hiv*, you can download two node attribute files with residue conservation data ([image](images/tut5.1_2.png)).
*   Click on the first link ([image](images/tut5.1_3.png)) and save the file to your local file system ([image](images/tut5.1_4.png)) under the name *1HIV_consurf.grades_scores.na* ([image](images/tut5.1_5.png)).
*   Click on the second link ([image](images/tut5.1_6.png)) and save the file to your local file system ([image](images/tut5.1_7.png)) under the name *1HIV_consurf.grades_colors.na* ([image](images/tut5.1_8.png)).

  

* * *

Load data into Cytoscape
------------------------

*   Switch to Cytoscape
*   Import the node attribute files *1HIV_consurf.grades_scores.na* and *1HIV_consurf.grades_colors.na* by performing the same steps for each file:
    *   Go to File → Import → Node Attributes... ([image](images/tut5.2_1.png))
    *   Select the node attribute file in the file browser and click on the Open button ([image](images/tut5.2_2.png)).
    *   Close the dialog informing you about the successfully performed action ([image](images/tut5.2_3.png)).

  

* * *

Map residue conservation to node/residue colors
-----------------------------------------------

*   In the Control Panel, go to VizMapper ([image](images/tut5.3_1.png)).
*   Go to the Node Color row ([image](images/tut5.4_1.png)), click on the right field to change the attribute to ResidueConsurfColor ([image](images/tut5.4_2.png)) and change the mapping type to Discrete Mapping ([image](images/tut5.4_3.png)).
*   The ConSurf-DB color scale uses 9 values to represent the conservation scores (9 - conserved, 1 - variable). Therefore, there are 9 rows with values to be mapped to colors ([image](images/tut5.4_4.png)). You can set color for each value by clicking on the right field in the row ([image](images/tut5.4_5.png)) and then on the ... button.
*   Set the colors as in this [image](images/tut5.4_6.png).
*   Color residues in Chimera according to the node colors in the network view. Go to Plugins → RINalyzer → Protein Structure → Sync 3D view colors ([image](images/tut1.2_20.png)).
*   The network and the 3D structure should look as in this [image](images/tut5.4_8.png).