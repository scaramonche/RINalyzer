Download RIN data
-----------------

In this step, it is assumed that you have chosen a protein of interest with an experimentally determined 3D structure and you know the PDB identifier of this protein. In this example, we will use the PDB entry *1hiv*, an HIV-1 protease.

*   Go to our RIN data download page (not avilable at the moment).
*   Enter a PDB identifier in the search form ([image](images/tut1.1_1.jpg)) and click the Search for RIN data button.
*   Click on the link 1hiv ([image](images/tut1.1_2.jpg)) and download the archive file *1hiv.zip* to your local file system ([image](images/tut1.1_3.jpg)).
*   The file *1hiv.zip* contains the RIN data generated for the target PDB identifier *1hiv*:
    *   a PDB file with the 3D protein structure from the original PDB file (as downloaded from the PDB) with hydrogens added (*pdb1hiv_h.ent*);
    *   a SIF file containing the residue interaction network for all chains in the PDB file (*pdb1hiv_h.sif*);
    *   an edge attribute file with double edge weights reflecting the strength of interactions between residues (*pdb1hiv_h_intsc.ea*);
    *   an edge attribute file with integer edge weights representing the number of interactions between residues (*pdb1hiv_h_nrint.ea*).
*   Extract the files from the archive into a new directory, e.g., *1hiv* ([image](images/tut1.1_4.jpg)).

  

* * *

Load RIN data into Cytoscape
----------------------------

*   Launch Cytoscape
*   Import the network file *pdb1hiv_h.sif*:
    *   Go to File → Import → Network (multiple file types)... ([image](images/tut1.2_1.jpg))
    *   Select the Local option for *Data Source Type* and click the Select button ([image](images/tut1.2_2.jpg)).
    *   Go to the *1hiv* directory that contains the RIN files ([image](images/tut1.2_3.jpg)).
    *   Select the file *pdb1hiv_h.sif* in the file browser and click on the Open button ([image](images/tut1.2_4.jpg)).
    *   Click the Import button ([image](images/tut1.2_5.jpg)).
    *   Close the dialog informing you about the successfully performed action ([image](images/tut1.2_6.jpg)).
    *   Maximize the network view by clicking the green square in the upper right corner of the network view window ([image](images/tut1.2_21.png)).
*   Import the edge attribute files *pdb1hiv_h_intsc.ea* and *pdb1hiv_h_nrint.ea* by performing the same steps for each file:
    *   Go to File → Import → Edge Attributes... ([image](images/tut1.2_9.jpg))
    *   Select the edge attribute file in the file browser and click on the Open button ([image](images/tut1.2_10.jpg)).
    *   Close the dialog informing you about the successfully performed action ([image](images/tut1.2_11.jpg)).
*   Load the PDB structure file *pdb1hiv_h.ent*:
    *   Go to Plugins → RINalyzer → Protein Structure → Open structure from file ([image](images/tut1.2_12.png)).
    *   Select the PDB file in the file browser and click on the Open button ([image](images/tut1.2_13.jpg)).
    *   It may take a while until UCSF Chimera is launched and the 3D structure is opened.
    *   Close the dialog informing you about the success of the mapping between network nodes and structure entities ([image](images/tut1.2_14.jpg)).

  

* * *

Adapt and explore views
-----------------------

*   Show all graphics details in the network view, as they are not displayed in a zoomed-out view. Go to View → Show Graphics Details ([image](images/tut1.2_8.jpg)).
*   Go to Plugins → RINalyzer → Protein Structure → Show backbone ([image](images/tut1.2_19.png)) to add protein backbone edges to the RIN and display the ribbon in the Chimera 3D view.
*   Go to Plugins → RINalyzer → Layout → RIN Layout ([image](images/tut1.2_7.png)) to layout the network according to the 3D view. In order to see the whole network click on the 1:1 ([image](images/tut1.2_22.png)) icon in the Cytoscape toolbar.
*   Adapt the visual settings of the RIN:
    *   Go to Plugins → RINalyzer → Visual Properties ([image](images/tut1.2_15.png)).
    *   In the General & Nodes tab you can choose how the node label should be displayed. For example, if you can select only residue number and type ([image](images/tut1.2_16.jpg)), you will observe that the node labels are updated.
    *   In the Edges tab, you can choose which edge types should be displayed. The network view is updated automatically each time you check or uncheck an edge type box.
    *   In the Edges tab, you can enable/disable the option Straighten edge lines if you do (not) want multiple edges to be drawn as straight parallel lines.
    *   You can change the value of each other property and you will see the resulting network view by clicking the Apply button. More details about the different options can be found [here](../visualprops.md).
    *   If you are satisfied with the view of your RIN, close the Visual Properties dialog by clicking the Close button.
*   Color residues in Chimera according to the node colors in the network view. Go to Plugins → RINalyzer → Protein Structure → Sync 3D view colors ([image](images/tut1.2_20.png)).
*   View only the protein backbone in both network and 3D view.

*   In Chimera, go to Actions → Atoms/Bonds → hide ([image](images/tut1.2_23.png)) to hide all atoms in Chimera.
*   In Cytoscape, go to Plugins → RINalyzer → Visual Properties → Edges ([image](images/tut1.2_24.png)). Uncheck the boxes beside all edge types except the backbone edges ([image](images/tut1.2_25.png)) and close the dialog.
*   You can show all edges again by opening the RINalyzer Visual Properties dialog and checking the boxes beside the edge types ([image](images/tut1.2_26.png)). Then, click the Apply button to straighten the edge lines and Close to close the dialog.
*   You can show all atoms in the 3D structure by going to Actions → Atoms/Bonds → show in Chimera ([image](images/tut1.2_27.png)).
*   You can hide the backbone in both views by clicking on Plugins → RINalyzer → Protein Structure → Hide backbone ([image](images/tut1.2_28.png)).

*   Your screen should look as in this [image](images/tut1.2_29.png).
*   Store the current network, the loaded attributes and the current network view into a Cytoscape session file called *pdb1hiv_h.cys*. The session file can be opened again anytime.
    *   Go to File → Save ([image](images/tut1.2_17.jpg)).
    *   Enter the session file name *pdb1hiv_h.cys* into the file browser and click on the Save button ([image](images/tut1.2_18.jpg)).