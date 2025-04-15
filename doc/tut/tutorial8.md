Prepare data
------------

For this tutorial, we will use the protein structure of the human deoxyhaemoglobin with PDB identifier *4hhb*. We will compare one of the α subunits with one of the β subunits, i.e., chain A with chain B, but we will refer to them as two different structures.

*   Get a structure alignment file using the RCSB PDB Protein Comparison Tool.
    *   Go to the [RCSB PDB Protein Comparison Tool web site](http://www.rcsb.org/pdb/workbench/workbench.do) ([image](images/tut8.1_1.jpg)).
    *   Enter *4hhb* in the text field for ID 1 and the tool will automatically show all chains. Select chain A by clicking on it ([image](images/tut8.1_2.jpg)).
    *   Enter the same identifier in the text field for ID 2 and select chain B ([image](images/tut8.1_3.jpg)).
    *   In the **Select Comparison Method** drop-down menu, choose the *jCE algorithm* ([image](images/tut8.1_4.jpg)).
    *   Click the *Compare* button ([image](images/tut8.1_5.jpg)).
    *   Scroll down to the **Download Alignment panel** ([image](images/tut8.1_6.jpg)). Right-click the link *Download XML* and select the option *Save Link As...* ([image](images/tut8.1_7.jpg)).
    *   In the file browser, select a directory where the file should be saved, enter a name for it, e.g., *4hhbA_vs_4hhbB.xml*, and click the *Save* button ([image](images/tut8.1_8.jpg)).
    *   Close the **Protein Comparison Tool**.
*   Retrieve the RIN data for PDB entry *4hhb* following the instructions in [Tutorial 1](tutorial6.md) (part 1). Go to **Apps → structureViz → Exit Chimera** to close UCSF Chimera properly as we do not need it for this tutorial.
*   Extract chain A and chain B as new networks.
    *   In Cytoscape, go to **Apps → RINalyzer → Extract Subnetwork** ([image](images/tut8.1_9.jpg)).
    *   Check the box *Chains* and then chain A, enter a name for the new network, e.g., *pdb4hhb_A*, and click the *OK* button ([image](images/tut8.1_10.jpg)). A new network is created that contains all nodes belonging to chain A.
    *   In the Control Panel, select network *pdb4hhb_h.sif* ([image](images/tut8.1_11.jpg)).
    *   Go to **Apps → RINalyzer → Extract Subnetwork** ([image](images/tut8.1_9.jpg)).
    *   Check the box *Chains* and then chain B, enter a name for the new network, e.g., *pdb4hhb_B*, and click the *OK* button ([image](images/tut8.1_12.jpg)). A new network is created that contains all nodes belonging to chain B.
    *   You can apply a layout to both networks and customize their visual properties (go to **Apps → RINalyzer → Visual Properties**). The Organic layout by yFiles (go to **Layout → yFiles → Organic**) can be useful in case you do not want to open the corresponding 3D structure in UCSF Chimera, which is a precondition for applying the RINLayout.

  

* * *

Perform comparison
------------------

*   Go to **Apps → RINalyzer → Compare RINs** ([image](images/tut8.2_1.jpg)).
*   Enter a name for the new network, e.g., *comparison*, select network *pdb4hhb_A* as first network, network *pdb4hhb_B* as second network ([image](images/tut8.2_3.jpg)).
*   Click the *Open a File...* button and open the alignment file downloaded earlier ([image](images/tut8.2_4.jpg)).
*   Click the *OK* button to perform the comparison ([image](images/tut8.2_5.jpg)).
*   A new network with 148 nodes and 2405 edges is created ([image](images/tut8.2_6.jpg)). Solid black lines represent interactions present in both networks, dashed green lines interactions in the first network only, and dotted red lines interactions in the second network only.
*   Customize the network view for interpreting the result:
    *   Go to **Apps → RINalyzer → Visual Properties** ([image](images/tut8.2_7.jpg)).
    *   Hide all edges except *combi:all_all* by unchecking the boxes beside each edge type and close the **RIN Visual Properties** dialog ([image](images/tut8.2_8.jpg)).
    *   In the **Control Panel**, select the **Style** tab and click the drop-down list to select the default comparison network style called *Comparison style* ([image](images/tut8.2_9.jpg)).
*   Now, the network looks as in [image](images/tut8.2_10.jpg) and by zooming-in you can observe the residue interaction differences between the superimposed α and β subunits of the human deoxyhaemoglobin ([image](images/tut8.2_11.jpg)).
