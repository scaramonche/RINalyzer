In this tutorial, we assume that you have loaded a RIN into Cytoscape and opened the corresponding PDB structure in UCSF Chimera.

Create node sets (residue selections)
-------------------------------------

*   In Cytoscape, go to Plugins → RINalyzer → Manage Node Sets ([image](images/tut2.1_1.png)) and a new panel for node sets will appear in the Cytoscape's Control Panel.
*   In UCSF Chimera, go to Select → Chain → A ([image](images/tut2.1_2.jpg)). Now, all residues in chain A are selected (indicated by green color) and the corresponding nodes in Cytoscape are also selected (yellow) ([image](images/tut2.1_3.png)).
*   In the RINalyzer Node Sets panel in Cytoscape, go to File → New → Set from selected nodes ([image](images/tut2.1_4.jpg)).
*   Enter a name for the set to be created, e.g., *Chain A*, in the text field and click OK ([image](images/tut2.1_5.jpg)).
*   In UCSF Chimera, go to Select → Chain → B. This will change the current selection in both viewers ([image](images/tut2.1_6.jpg)).
*   In the RINalyzer Node Sets panel in Cytoscape, go to File → New → Set from selected nodes ([image](images/tut2.1_7.jpg)).
*   Enter a name for the set to be created, e.g., *Chain B*, in the text field and click OK ([image](images/tut2.1_8.jpg)).

  

* * *

Perform set operations
----------------------

*   Select both sets (by left-clicking and pressing the Ctrl key) and go to Operations → Union ([image](images/tut2.1_9.jpg)). This action will create a new set, which is the union of the selected sets.
*   Enter a name for the set to be created, e.g., *All nodes*, in the text field and click OK ([image](images/tut2.1_10.jpg)).
*   Right-click the set *All nodes* and go to Select nodes ([image](images/tut2.1_11.png)), in order to select all set nodes in the network view ([image](images/tut2.1_12.png)).
*   Right-click the set *All nodes* and go to Delete set(s), in order to delete this set ([image](images/tut2.1_13.png)).
*   Click somewhere in the Network View window in order to clear the current selection.

  

* * *

Color nodes in a set
--------------------

*   Right-click the set *Chain A* and go to Visual Mapping Bypass → Node Color ([image](images/tut2.1_14.png)).
*   Select a color and click OK. This will color all nodes in the set in the selected color ([image](images/tut2.1_15.jpg)).
*   Right-click the same set again and go to Sync 3D view colors ([image](images/tut2.1_32.png)) to color the corresponding residues in Chimera with the same color.
*   Do the same for the set *Chain B* ([image 1](images/tut2.1_16.png), [image 2](images/tut2.1_17.jpg), and [image 3](images/tut2.1_33.png)).
*   Now, the network view and the 3D view should look as in this [image](images/tut2.1_34.png).

  

* * *

Discover and highlight active site residues
-------------------------------------------

*   From the literature we know that the active site residues of the HIV-1 protease are ASP 25, THR 26, and GLY 27 either in chain A or in chain B. Thus, we will create a set with all 6 residues.
*   In the RINalyzer Node Sets panel in Cytoscape, go to File → New → Empty set ([image](images/tut2.1_18.jpg)).
*   Enter the name *Active site* and click OK ([image](images/tut2.1_19.jpg)).
*   Go to the Search field in the Cytoscape menu bar, in order to select the nodes representing the active site residues. Enter *a:25* and you will see one hit, which is exactly the node we want to select. Press Enter ([image](images/tut2.1_20.jpg)).
*   In the RINalyzer Node Sets panel, go to Edit → Add nodes ([image](images/tut2.1_21.jpg)) and the selected node will be added to the currently selected node set, which is *Active site*.
*   Repeat these actions for the other 5 active site residues.
*   The set *Active site* should contain 6 nodes ([image](images/tut2.1_22.jpg)).
*   Right-click the set *Active site* and go to Visual Mapping Bypass → Node Color ([image](images/tut2.1_23.png)).
*   Select a color different from the previously chosen colors and click OK ([image](images/tut2.1_24.jpg)).
*   In the Cytoscape menu bar, click the 1:1 icon to see the whole network ([image](images/tut2.1_26.jpg)).
*   Right-click the set *Active site* and go to Select nodes ([image](images/tut2.1_25.png)). Switch to the UCSF Chimera window and you will see exactly where the active site residues of the HIV-1 protease are located ([image](images/tut2.1_27.png)).

  

* * *

Save node sets
--------------

*   Select all sets by left-clicking and pressing the Ctrl key ([image](images/tut2.1_28.jpg)).
*   In the RINalyzer Node Sets panel, go to File → Save selected set(s) ([image](images/tut2.1_29.jpg)).
*   Enter the file name into the file browser, e.g., *hiv1_sets* and click on the Save button ([image](images/tut2.1_30.jpg)).
*   Close the dialog informing you about the successfully performed action ([image](images/tut2.1_31.jpg)).