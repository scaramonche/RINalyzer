### Centrality Results File

The results of the centrality analysis can be stored into a text file. It contains the network name, a list of the selected nodes, the name of the saved centrality parameter and a list of nodes along with their respective centrality values. The nodes are sorted alphabetically. A small example is presented below.

	pdb1abc.sif
	Selected nodes: A, B, C
	Shortest Path Betweenness
	A:42:\_:GLU	0.2
	A:86:\_:SER	0.0
	A:57:\_:PRO	0.3
	A:88:\_:ARG	1.0

If the user saves all measures in one file, then the network name and the list of selected nodes are included only at the beginning of the file. The files with such format have the extension *centstats*.

* * *

### Node Set File

The node set file has a simple structure. A header line defines the file type. The file can contain more than one set separated by new lines. Each set is defined by a line consisting of the set name and the name of the network, to which the nodes in the set belong. These are separated by a tabular and followed by a list of node names/identifiers. The extension for node set files is *nodeset*. An example for a node set file is shown below:

	# RINalyzer node sets
	Set1    pdb1abc.sif
	A:42:\_:GLU
	A:57:\_:PRO

	Set2    pdb1abc.sif	
	A:86:\_:SER
	A:88:\_:ARG

* * *

### Visual Properties File

The default visual properties values provided by RINalyzer use the Java Properties format. They include the secondary structure node colors, the edge type colors, the node and label size, the line width of backbone edges and all other edges, and the width of the space between parallel edges. Here, the default properties file, the *rinalyzer.props*, can be seen:

	## RINalyzer visual properties
	#Sat Jul 11 21:15:49 CEST 2009
	bbedgewidth=4
	edgewidth=3
	edgespace=4
	nodesize=40
	labelsize=14
	color.Background=255,255,255
	color.default=255,146,148
	color.Helix=255,0,0
	color.Sheet=0,0,255
	color.Loop=153,153,153
	color.backbone=0,0,0
	color.combi\\:all\_all=0,0,0
	color.cnt\\:mc\_mc=0,0,255
	color.cnt\\:mc\_sc=0,153,255
	color.cnt\\:sc\_sc=153,204,255
	color.cnt\\:mc\_lig=0,153,51
	color.cnt\\:sc\_lig=102,255,0
	color.hbond\\:mc\_mc=153,0,51
	color.hbond\\:mc\_sc=255,0,0
	color.hbond\\:sc\_sc=255,204,204
	color.hbond\\:sc\_lig=255,255,0
	color.hbond\\:mc\_lig=153,153,0
	color.ovl\\:mc\_mc=51,51,51
	color.ovl\\:mc\_sc=153,153,153
	color.ovl\\:sc\_sc=204,204,204
	color.pp=0,0,0

  