Prepare data
------------

For this tutorial, we will use the protein structure of the human deoxyhaemoglobin with PDB identifier *4hhb*. We will compare one of the α subunits with one of the β subunits, i.e., chain A with chain B, but we will refer to them as two different structures.

*   Get a structure alignment file using the RCSB PDB Protein Comparison Tool.
    *   Go to the [RCSB PDB Protein Comparison Tool web site](http://www.rcsb.org/pdb/workbench/workbench.do) ([image](images/tut4.1_1.png)).
    *   Enter *4hhb* in the text field for PDB1 and the tool will automatically select chain A ([image](images/tut4.1_2.png)).
    *   Enter the same identifier in the text field for PDB2 and select chain B from the drop-down menu ([image](images/tut4.1_3.png)).
    *   In the Select Comparison Method drop-down menu, choose the *jCE algorithm* ([image](images/tut4.1_4.png)).
    *   Click the Compare button ([image](images/tut4.1_5.png)).
    *   If you want you may allow your browser to run the Java application for visualizing the structure alignment with Jmol by clicking the Run button ([image](images/tut4.1_6.png)).
    *   Scroll down to the Download Alignment panel ([image](images/tut4.1_7.png)). Right-click the link *Download XML* and select the option Save Link As... ([image](images/tut4.1_8.png)).
    *   In the file browser, select a directory where the file should be saved, enter a name for it, e.g., *4hhbA_vs_4hhbB.xml*, and click the Save button ([image](images/tut4.1_9.png)).
    *   Close the Protein Comparison Tool.
*   Download the RIN data for PDB entry *4hhb* and open the corresponding RIN in Cytoscape following the instructions in [Tutorial 1](tutorial1.md) until the layout is applied on the network view.
*   Extract chain A and chain B as new networks.
    *   In Cytoscape, go to Plugins → RINalyzer → Extract chain(s) ([image](images/tut4.1_10.png)).
    *   Check the box of chain A, enter a name for the new network, e.g., *pdb4hhb_A*, and click the OK button ([image](images/tut4.1_11.png)). A new network is created that contains all nodes belonging to chain A ([image](images/tut4.1_12.png)).
    *   In the Control Panel, select network *pdb4hhb_h.sif* ([image](images/tut4.1_13.png)).
    *   Go to Plugins → RINalyzer → Extract chain(s) ([image](images/tut4.1_14.png)).
    *   Check the box of chain B, enter a name for the new network, e.g., *pdb4hhb_B*, and click the OK button ([image](images/tut4.1_15.png)). A new network is created that contains all nodes belonging to chain B ([image](images/tut4.1_16.png)).
    *   You can apply a layout to both networks and customize their visual properties (go to Plugins → RINalyzer → Visual Properties). The Organic layout by yFiles (go to Layout → yFiles → Organic) can be useful in case you do not want to open the corresponding 3D structure in Chimera, which is a precondition for applying the RINLayout.

  

* * *

Perform comparison
------------------

*   Go to Plugins → RINalyzer → Compare RINs ([image](images/tut4.2_1.png)).
*   Select network *pdb4hhb_A* as first network ([image](images/tut4.2_2.png)), network *pdb4hhb_B* as second network ([image](images/tut4.2_3.png)), enter a name for the new network, e.g., *comparison* ([image](images/tut4.2_4.png)).
*   Click the ... button ([image](images/tut4.2_5.png)) and open the alignment file downloaded earlier ([image](images/tut4.2_6.png)).
*   Click the Compare button to perform the comparison ([image](images/tut4.2_7.png)).
*   A new network with 148 nodes and 2405 edges is created. You can maximize the network view window ([image](images/tut4.2_8.png)), apply the organic layout and the RIN visual properties.
*   If you use Cytoscape 2.6.x, the network should look as in [this image](images/tut4.2_9.png). The solid edge lines represent residue interactions present in both networks, while the dashed edge lines are interactions found in only one of the structures. If you use Cytoscape 2.7 and above, then you will observe three different edge line styles as in [this image](images/tut4.2_25.png): solid lines for interactions present in both networks, dashed lines for network 1, and dotted lines for network 2.
*   Customize the network view for interpreting the result:
    *   Go to Plugins → RINalyzer → Visual Properties and select the Edges tab ([image](images/tut4.2_10.png)).
    *   Hide all edges except combi:all_all by unchecking the boxes beside each edge type and close the RIN Visual Properties dialog ([image](images/tut4.2_12.png)).
    *   In the Control Panel, go to VizMapper ([image](images/tut4.2_13.png)).
    *   Double-click the field Edge Color ([image](images/tut4.2_14.png)), select the attribute BelongsTo ([image](images/tut4.2_16.png)) and the mapping type Discrete Mapping ([image](images/tut4.2_17.png)).
    *   For each attribute: click on the field beside the attribute and then on the appeared ... button ([image](images/tut4.2_18.png)); select a color and click OK ([image](images/tut4.2_19.png)). We picked black for edges in both networks, green for edges in the first network, and red for edges in the second ([image](images/tut4.2_20.png)).
    *   Repeat the same actions for mapping the node color using the BelongsTo attribute ([image](images/tut4.2_21.png)).
    *   Click on the field beside the visual property Node Label and select the attribute CombinedLabel ([image](images/tut4.2_23.png)).
    *   Now, all node labels are composed of the node labels of the nodes from the compared networks ([image](images/tut4.2_24.png)).
*   Now, the network looks as in [this image](images/tut4.2_22.png) (in Cytoscape 2.6.x) and by zooming-in you can observe the residue interaction differences between the superimposed α and β subunits of the human deoxyhaemoglobin.