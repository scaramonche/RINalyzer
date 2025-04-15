Perform comparison
------------------

*   Retrieve the RIN data for two myoglobin structures with PDB entries *1mbn* ([image](images/tut9.1_2.jpg)) and *1mbo* ([image](images/tut9.1_1.jpg)) using the RINdata Web Service in Cytoscape (see [first part of Tutorial 1](tutorial6.md) for instructions. Once ready, your screen should look as in [this image](images/tut9.1_3.jpg).
*   Go to **Apps → RINalyzer → Compare RINs** ([image](images/tut9.1_4.jpg)).
*   Adjust the comparison settings:
    *   Enter a name for the new network, e.g., *comparison*.
    *   The first (reference) network should automatically be set to *pdb1mbn_h.sif* and the second (match) network to *pdb1mbo_h.sif* ([image](images/tut9.1_5.jpg)).
    *   The firs (reference) chain should be set to *#0 pdb1mbn_h.ent chain A*. Select *#1 pdb1mbo_h.ent chain A* as the second (match) chain ([image](images/tut9.1_6.jpg)).
    *   The options *Align chains in Chimera (using MatchMaker)* and *Get mapping from the sequence alignment* should be checked automatically if protein structures are open in UCSF Chimera.
*   Click the *OK* button to perform the comparison ([image](images/tut9.1_7.jpg)).
*   A new network with 153 nodes and 2550 edges is created and the two protein structures are aligned ([image](images/tut9.1_8.jpg)). Solid black lines represent interactions present in both networks, dashed green lines interactions in the first network only, and dotted red lines interactions in the second network only.
*   Customize the network view for interpreting the result:
    *   Go to **Apps → RINalyzer → Visual Properties** ([image](images/tut9.1_9.jpg)).
    *   Hide all edges except *combi:all_all* by unchecking the boxes beside each edge type and close the **RIN Visual Properties** dialog ([image](images/tut9.1_10.jpg)).
    *   If the previous action changes the current visual style applied to the network view, you can switch back to the right one by selecting the **Style** tab in the **Control Panel** and clicking the drop-down list comparison ([image](images/tut9.1_11.jpg)) to select the default comparison network style called *Comparison style* ([image](images/tut9.1_12.jpg)) or the alternative *Comparison style RIN*, which retains the default edge interaction colors.
*   Rotate the aligned structures and adjust the node positions:
    *   In UCSF Chimera, rotate the aligned myoglobin structures to see the *HEM* group and the bound oxygen molecule ([image](images/tut9.1_13.jpg)).
    *   In Cytoscape, go to **Layout → RIN Layout** to apply the RIN Layout considering the new coordinates of the 3D structures ([image](images/tut9.1_14.jpg)).
    *   Your screen should look similar to [this image](images/tut9.1_15.jpg).
    *   If you would like to see how the *HEM* group interacts with the myoglobin residues, you can create a RIN from UCSF Chimera following [tutorial 2](tutorial10.md).
*   Select nodes with most edges belonging only to one of the networks:
    *   In the **Control Panel**, select the **Select** tab ([image](images/tut9.1_16.jpg)) to create new node or edge filters.
    *   Click the **+** button to add a new filter and select *Column Filter* from the drop-down list ([image](images/tut9.1_17.jpg)) for an attribute-based filter.
    *   Click the **Choose column...** drop-down list and select the entry *Node: EdgeFracBoth* ([image](images/tut9.1_18.jpg)).
    *   Move the left arrow of the slider to the right to increase the range, for example to *.600* ([image](images/tut9.1_19.jpg)).
*   Now, the network looks as in [this image](images/tut9.1_20.jpg) and by zooming-in you can observe the residue interaction differences between the superimposed myoglobin structures.