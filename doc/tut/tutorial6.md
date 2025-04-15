Load RIN data into Cytoscape (automatically)
--------------------------------------------

In this step, it is assumed that you have chosen a protein of interest with an experimentally determined 3D structure and you know the PDB identifier of this protein. In this example, we will use the PDB entry *1hiv*, an HIV-1 protease.

*   Launch Cytoscape
*   Go to **File → Import → Network → Public Databases...** ([image](images/tut6.1_1.jpg)).
*   In the **Data source** drop-down menu, choose the *RINdata Web Service Client* ([image](images/tut6.1_2.jpg)).
*   Enter a PDB identifier in the search form ([image](images/tut6.1_3.jpg)).
*   If UCSF Chimera is not installed in the default location and it is your first time to use this service, enter the path to the UCSF Chimera executable ([image](images/tut6.1_4.jpg)).
*   Click the **Retrieve RIN data** button to automatically retrieve the archive file *1hiv.zip*, which contains the RIN data generated for the target PDB identifier *1hiv*:
    *   a PDB file with the 3D protein structure from the original PDB file (as downloaded from the PDB) with hydrogens added (*pdb1hiv_h.ent*);
    *   a SIF file containing the residue interaction network for all chains in the PDB file (*pdb1hiv_h.sif*);
    *   an edge attribute file with double edge weights reflecting the strength of interactions between residues (*pdb1hiv_h_intsc.ea*);
    *   an edge attribute file with integer edge weights representing the number of interactions between residues (*pdb1hiv_h_nrint.ea*).
*   The network and edge attribute files are loaded automatically. The structure file is saved to `USER_HOME/CytoscapeConfigureation/pdbs/` and opened in UCSF Chimera. The default visual properties and the RIN layout are applied automatically. Your screen should look as in this [image](images/tut6.1_5.jpg).
*   If you are do not want to import other RINs, close the dialog using the X button in the upper right corner ([image](images/tut6.1_6.jpg)).

  

* * *

Load RIN data into Cytoscape (manually)
---------------------------------------

*   Import the network file *pdb1hiv_h.sif*:
    *   Go to **Apps → RINalyzer → Import RIN from file** ([image](images/tut6.2_1.jpg))
    *   Click the **Open a File...** button ([image](images/tut6.2_2.jpg)).
    *   Go to the `1hiv` directory that contains the RIN files. Select the file *pdb1hiv_h.sif* in the file browser and click on the **Open** button ([image](images/tut6.2_3.jpg)).
    *   Enter *pdb1hiv_h.ent* for the name of the structure associated with this RIN ([image](images/tut6.2_4.jpg)).
    *   Click the **OK** button to load the data into Cytoscape. Your screen should look as in this [image](images/tut6.2_5.jpg).
*   Load the PDB structure file *pdb1hiv_h.ent*:
    *   If UCSF Chimera is not installed in the default location and it is the first time to launch it, go to **Apps → structureViz → Settings...** ([image](images/tut6.2_6.jpg)) and enter the path to the UCSF Chimera executable at the bottom of the dialog ([image](images/tut6.2_7.jpg)). Click **OK** to save the settings.
    *   Go to **Apps → RINalyzer → Open Structure from File** ([image](images/tut6.2_8.jpg)).
    *   Select the PDB file in the file browser and click on the **Open** button ([image](images/tut6.2_9.jpg)).
    *   It may take a while until UCSF Chimera is launched and the 3D structure is opened. Your screen should look as in [this image](images/tut6.2_10.jpg).
*   Import the edge attribute files *pdb1hiv_h_intsc.ea* and *pdb1hiv_h_nrint.ea* by performing the same steps for each file:
    *   Go to **File → Import → Table → File...** ([image](images/tut6.2_11.jpg))
    *   Select the edge attribute file in the file browser and click on the **Open** button ([image](images/tut6.2_12.jpg)).
    *   In the **Import Columns from Table** dialog, choose *Edge Table Columns* in the **Import Data** drop-down list, check the *Show Text File Import Options* checkbox, choose the option **Other** and enter **=** as *Delimiter*, select the option *Transfer first line as column names* ([image](images/tut6.2_13.jpg)).
    *   To set the column names, right-click the column names and enter the new names, *ID* for the first column ([image](images/tut6.2_14.jpg)), *InteractionScore* for the second ([image](images/tut6.2_15.jpg)).
    *   In everything looks as in this [image](images/tut6.2_16.jpg), click **OK** to complete the import.

  

* * *

Adapt and enrich the views
--------------------------

*   Show all graphics details in the network view, as they are not displayed in a zoomed-out view. Go to **View → Show Graphics Details** ([image](images/tut6.3_8.jpg)).
*   Annotate the RIN with structural properties:
    *   Go to **Apps → RINalyzer → Annotate RIN from Chimera** ([image](images/tut6.3_1.jpg)).
    *   Select one or all (by clicking Ctrl + A) residue attributes to be transferred as node attributes ([image](images/tut6.3_2.jpg)).
*   Synchronize the colors:
    *   Go to **Apps → RINalyzer → Sync RIN colors with Chimera** ([image](images/tut6.3_3.jpg)).
    *   Choose the first option to transfer the colors of the nodes to the residues ([image](images/tut6.3_4.jpg)).
    *   Your screen should look as in this [image](images/tut6.3_5.jpg).
*   Rotate the 3D structure and to layout the network according to the 3D view, go to **Layout → RINLayout** ([image](images/tut6.3_6.jpg)). In order to see the whole network click on the 1:1 ([image](images/tut6.3_7.jpg)) icon in the Cytoscape toolbar.
*   Adapt the visual settings of the RIN:
    *   Go to **Apps → RINalyzer → RIN Visual Properties** ([image](images/tut6.3_9.jpg)).
    *   In the **General & Nodes** tab you can choose how the node label should be displayed. For example, if you can select only residue number and type ([image](images/tut6.3_10.jpg)), you will observe that the node labels are updated.
    *   In the **Edges** tab, you can choose which edge types should be displayed. The network view is updated automatically each time you check or uncheck an edge type box.
    *   You can add backbone edges by checking the box on the top of the dialog ([image](images/tut6.3_11.jpg)).
    *   In the **Edges** tab, you can enable/disable the option *Straighten edge lines* if you do (not) want multiple edges to be drawn as straight parallel lines as in this [image](images/tut6.3_11.jpg).
    *   You can change the value of each other property and you will see the resulting network view by clicking the **Apply** button. More details about the different options can be found [here](../docu/visualprops.md).
    *   If you are satisfied with the view of your RIN, apply the changes by clicking the Apply button and close the dialog with the Close button.
*   Store the current network, the loaded attributes and the current network view into a Cytoscape session file called *pdb1hiv_h.cys*. The session file can be opened again anytime.
    *   Go to **File → Save** ([image](images/tut6.1_7.jpg)).
    *   Enter the session file name *pdb1hiv_h.cys* into the file browser and click on the **Save** button ([image](images/tut6.1_8.jpg)).

