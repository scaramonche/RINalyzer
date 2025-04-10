Create a RIN of a ligand and its neighbors
------------------------------------------

In this tutorial, it is assumed that you have the PDB structure file for a protein of your choice. Such a file can be downloaded from the [Protein Data Bank](http://www.rcsb.org/pdb/home/home.do). In this example, we will use the file for the PDB entry *1hiv*, an HIV-1 protease, as downloaded by RINalyzer in [Tutorial 1](tutorial6.md) (part 1).

*   Launch Cytoscape
*   Load the PDB structure file *pdb1hiv_h.ent*:
    *   Go to Apps → RINalyzer → Open Structure from File ([image](images/tut10.1_1.jpg)).
    *   In the file browser, go to the `CytoscapeConfiguration/pdbs` directory that contains the PDB files downloaded by RINalyzer. Select the file *pdb1hiv_h.ent* and click on the Open button ([image](images/tut10.1_2.jpg)).
    *   If UCSF Chimera is not installed in the default location and it is the first time to launch it, go to Apps → structureViz → Settings... ([image](images/tut6.2_6.jpg)) and enter the path to the UCSF Chimera executable at the bottom of the dialog ([image](images/tut6.2_7.jpg)). Click OK to save the settings.
    *   It may take a while until UCSF Chimera is launched and the 3D structure is opened. Your screen should look as in this [image](images/tut10.1_3.jpg).
*   Select the ligand:
    *   In the Molecular Structure Navigator dialog, select the model *Model #0 pdb1hiv_h.ent* ([image](images/tut10.1_4.jpg)).
    *   Right-click the model to bring up the context menu ([image](images/tut10.1_5.jpg)) and go to Select → Ligand.
    *   All residues listed as *HETATM* in the PDB structure will get selected in the model tree ([image](images/tut10.1_6.jpg)).   
*   Create the RIN:
    *   Still in the Molecular Structure Navigator dialog, go to Chimera → Residue network generation ([image](images/tut10.1_7.jpg)).
    *   In the Residue Interaction Network Generation dialog, adapt the name for the new network to *RIN pdb1hiv_h.ent ligand* ([image](images/tut10.1_8.jpg)).
    *   In the Include interactions drop-down menu, select Between selection and all other atoms to retrieve interactions between the ligands and all other residues in the structure ([image](images/tut10.1_9.jpg)).
    *   To include or remove some types of edges, check the appropriate box. When all settings are as desired, click OK ([image](images/tut10.1_10.jpg)) and wait a few minutes to see the network.
    *   Your screen should look as in this [image](images/tut10.1_11.jpg).
  