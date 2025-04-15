Prepare data for analysis
-------------------------

*   Make sure that the backbone edges in the network are hidden as they are only meant to help for visual analysis of the RIN. To hide them go to Plugins → RINalyzer → Protein Structure → Hide backbone ([image](images/tut3.0_1.png)).
*   Hiding the backbone edges in the RIN will also hide the ribbons in the 3D view. Thus, switch to Chimera and if you do not see the 3D structure anymore, go to Actions → Atoms/Bonds → show to show the atoms ([image](images/tut3.0_2.png)).
*   The RIN generated from the PDB entry *1hiv* contains a chain that represents an inhibitor bound to the HIV-1 protease. The nodes that belong to this chain should be hidden before performing centrality analysis on the network. In order to select this chain, go to Select → Chain → I in Chimera ([image](images/tut3.0_3.png)). Then, switch to Cytoscape and go to Edit → Delete Selected Nodes and Edges to delete the selected nodes ([image](images/tut3.0_4.jpg)).

  

* * *

Perform centrality analysis
---------------------------

*   Go to Select → Nodes → Select all nodes ([image](images/tut3.1_1.jpg)). This action can take a few seconds because both the nodes in the network and the residues in the 3D structure are selected.
*   Go to Plugins → RINalyzer → Analyze Network ([image](images/tut3.1_2.png)).
*   You will see a warning dialog ([image](images/tut3.1_3.jpg)) informing you that the network contains more than one connected components. That is because the nodes A:40:_:GLY and B:37:_:SER are not connected to any other node in the network. You have two options:
    *   Proceed with the analysis by clicking the Yes button and keep in mind that these nodes are disconnected from all other nodes in the network.
    *   Cancel the analysis by clicking the No button. Then, select the two disconnected nodes in the network ([image](images/tut3.1_4.png)) and delete them by clicking Edit → Delete Selected Nodes and Edges ([image](images/tut3.1_5.jpg)). Afterwards, select all nodes again and go to Plugins → RINalyzer → Analyze Network.
*   In the RINalyzer Analysis Settings dialog, check the boxes of all centrality measures. You can get more information about each of the analysis settings by clicking the Help button at the bottom of the dialog or by visiting the [Documentation page](../docu/cent_analysis.php).  
    Here, we will show you four reasonable combinations.
    1.  **Neglect weights and multiple edges**: the centrality measures are computed assuming the edge weight between each pair of connected nodes is equal to one, i.e. choose the option *Average weight*. Setting the degree cutoff to 1 means that the degree of a node will be equal to the fraction of its first neighbors ([image](images/tut3.1_6jpg)). 
    2.  **Neglect weights and consider multiple edges**: the centrality measures are computed assuming the edge weight between each pair of connected nodes is equal to the number of edges between the two nodes. Since the edge weights will be proportional to the number of edges, we need to choose a method to convert them into distances. We can set the degree cutoff to 3, i.e., the degree will be equal to the fraction of nodes that are connected by at most 3 edges to the node of interest ([image](images/tut3.1_7.jpg)). 
    3.  **Consider weights and multiple edges**: choose the edge attribute *NrInteractions* (an integer value representing the number of interactions between two residues) and the option *Sum of weights*. Then, the centrality measures are computed assuming that the edge weight between each pair of connected nodes is equal to the sum of the weights of all edges connecting the two nodes. We choose the *max-value* method for converting scores into distances. In this case, the degree (with cutoff set to 5) will be equal to the fraction of nodes that are at most at distance 5 from the node of interest ([image](images/tut3.1_8.jpg)). 
    4.  **Consider weights and only one edge type**: choose the edge attribute *InteractionScore* and the edge type *combi:all_all*, which represents the overall residue interactions, i.e., the centrality measures will be computed assuming the edge weight between each pair of nodes is equal to the weight of the *cobmi:all_all* edge connecting them. We can set the method for converting scores into distances to *max-value* and the degree cutoff to 2 ([image](images/tut3.1_9.jpg)). 
*   After setting the appropriate values for each setting, click the Analyze button.
*   A Progress... dialog showing the progress of the analysis will appear ([image](images/tut3.1_10.jpg)). The time for computing the centrality measures depends on the size of the network and the number of selected nodes. You can cancel the analysis any time by clicking the Cancel button in the Progress... dialog.

  

* * *

Explore centrality analysis results
-----------------------------------

*   After a successful computation, you can see the results in the RINalyzer Centralities tab of the Cytoscape's Results Panel ([image](images/tut3.2_2.jpg)). Go to View → Hide Data Panel to free more display space for the centrality analysis results ([image](images/tut3.2_1.jpg)).
*   View general analysis information in the General Information panel:
    *   See the name of the analyzed network: *pdb1hiv_h.sif* ([image](images/tut3.2_3.jpg)).
    *   Click the Selected Nodes button if you want to see the selected set of nodes in the network view ([image](images/tut3.2_4.jpg)).
    *   Click the Analysis Settings button to see the settings chosen for this analysis run ([image](images/tut3.2_5.jpg)).
*   Move the scrollbar on the right of the panel until the Shortest Path Closeness Filtering panel ([image](images/tut3.2_6.jpg)) is in a good position.
*   View the raw centrality values:
    *   Click the Show button. You will see a table with two columns ([image](images/tut3.2_7.jpg)). The first one contains the names of all nodes in the network and the second one the corresponding closeness values.
    *   Click on the name of the second column and the rows will be ordered according to the closeness centrality values ([image](images/tut3.2_8.jpg)). An up-arrow indicates ascending order, and a down-arrow descending order. This allows you to see the nodes with lowest or highest values, respectively.
*   Use the selection filter to see those nodes in the network view that have centrality values in a specified range:
    *   The numbers above the slider show the lowest and the highest bound for the selection filter, while the numbers below the slider are the current bounds as defined by the slider.
    *   Move the left end of the slider to the right. The left number should change and move to the right as you move the slider. In the same time, you should see that the node selection in the network changes. You can stop when you reach 0.25 and you will see in the network which nodes have closeness centrality above 0.25 ([image](images/tut3.2_9.png)).
    *   Double-click the slider and you will see a window where you can manually enter the lower and upper bound for the selection range. Set the lower bound to 0.26 ([image](images/tut3.2_10.jpg)) and click the OK button to set the new selection bounds ([image](images/tut3.2_11.png)).
    *   If the RINalyzer Node Sets panel is active, you can go to File → New → Set from selected nodes ([image](images/tut3.2_13.jpg)).
    *   Enter a name for the set to be created, e.g., *Highest SPC*, in the text field and click OK ([image](images/tut3.2_12.jpg)).

  

* * *

Save centrality analysis results
--------------------------------

*   Save the values of one centrality measure, e.g., shortest path closeness:
    *   Click on the Save button in the Shortest Path Closeness Filtering panel ([image](images/tut3.3_1.jpg)).
    *   In the file browser that appears, select the directory where you want to save the file ([image](images/tut3.3_2.jpg)).
    *   Enter the name of the file, e.g., *pdb1hiv_spc*, and click the Save button. RINalyzer will automatically add the file extension "centstats" to the file name ([image](images/tut3.3_3.jpg)).
*   Save all centrality measures:
    *   Move the scrollbar on the right of the panel until it reaches the beginning of the panel.
    *   Click the Save All button ([image](images/tut3.3_4.jpg)).
    *   In the file browser that appears, select the directory where you want to save the file ([image](images/tut3.3_5.jpg)).
    *   Enter the name of the file, e.g., *pdb1hiv_all*, and click the Save button ([image](images/tut3.3_6.jpg)). RINalyzer will automatically add the file extension "centstats" to the file name.