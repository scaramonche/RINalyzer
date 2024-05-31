### RIN File Format

The simplest way of representing RINs in Cytoscape is to use the simple interaction format (SIF) for defining the network and edge and/or node attribute files for additional information such as edge weights. SIF is particularly convenient for building a graph from a list of interactions. It also makes it easy to combine different interaction sets into a larger network or to add new interactions to an existing data set.

SIF files have the extension *sif*. Lines in the SIF file specify a source node, an edge type, and one or more target nodes delimited by spaces:

	nodeA <edge type> nodeB
	nodeC <edge type> nodeA
	nodeD <edge type> nodeE nodeF nodeB
	nodeG
	...
	nodeY <edge type> nodeZ

Duplicate entries are considered as the same entry and thus ignored. In order to define multiple edges between the same nodes, different edge types have to be used. The tag < edge type > can be any string. More details on SIF files can be found in the [Cytoscape User Manual]("http://www.cytoscape.org/manual/Cytoscape2_6Manual.html#SIF Format")

For RINalyzer version 1.x, the RIN name should contain the PDB identifier of the protein, for example, "pdb1abc.sif" for a RIN created form the PDB entry 1abc. This specification allows an automatic recognition of the PDB entry of the RIN and is used to automatically download the corresponding PDB file and open it in Chimera. Other information can also be contained in the file name, but it needs to be separated from the PDB identifier by an underscore "_", for example, "net_pdb1abc_new.sif". This is not necessary for RINalyzer version 2.x.

* * *

### Node Names and Edge Types

Node names must be unique because identically named nodes are treated as identical nodes in Cytoscape regardless of which network they belong to. The internal identifier of each node will be its name in the file. This might not be the case when another string is mapped onto the node using a node attributes file and the visual mapper in Cytoscape (see more in next section).

Each residue from a PDB file is uniquely identified by the PDB identifier, a chain number, residue number, insertion code identifier, and residue name/type. These are delimited by colons. If one of the fields is missing, it is replaced by an underscore. The PDB identifier needs to be included only in case that the nodes represent residues from two different PDB fiels. For example, if the 42th residue from chain A (GLU) interacts with the 57th residue from chain A (PRO) and none of them has an insertion code identifier, the line in the SIF file would look like:

	A:42:_:GLU <edge type> A:57:_:PRO

In general, the name of each edge is formed from the name of the source and target nodes plus the edge type:

	sourceName <edgeType> targetName 

Edge types consist of interaction types:

*   `combi`: generic residue interaction
*   `cnt`: interatomic contact
*   `hbond`: hydrogen bond
*   `ovl`: overlapping

and interaction subtypes:

*   `mc_mc`: main chain to main chain
*   `mc_sc`: main chain to side chain
*   `mc_lig`: main chain to ligand
*   `sc_sc`: side chain to side chain
*   `sc_lig`: side chain to ligand
*   `lig_lig`: ligand to ligand
*   `all_all`: overall

The `<edgeType>` is then defined as:

	interactionType:interactionSubtype

and we can rewrite the above example as:

	A:42:_:GLU hbond:mc_mc A:57:_:PRO

  

* * *

### Attribute Files for Cytoscape 3.x

In Cytoscape 3.x, node and edge attributes are referred to as node and edge column data. These can be any type of value associated with a given node or edge. Cytoscape offers support for importing data from delimited text and MS Excel data tables (see [Cytoscape User Manual](http://wiki.cytoscape.org/Cytoscape_3/UserManual#Node_and_Edge_Column_Data) for more details).

Each attribute can be stored in a separate file or as a table. Node and edge attribute files use the same format. Node attribute file names generated for Cytoscape 2.x often have the extension *na* and edge attribute file names have the extension *ea*.

The attribute file can have one header line that gives the name of the attribute(s). Each subsequent line contains the name of the node, followed by a separator (tabular, comma, etc.) and the value of one or more attributes.

A short example of a node attribute file for naming residues:

	NodeID    ResidueName
	A:42:_:GLU    GLU
	A:57:_:PRO    PRO

An edge attribute file has a very similar structure to the node attribute file. The name of the edge consists of the source node name, the interaction type in parentheses, and the target node name. The directionality is important, and this is why switching the source and target node will refer to a different (possibly non-existent) edge. Note that tabs are not allowed in edge names. The attribute file can have one header line that gives the name of the attribute(s). Each subsequent line contains the name of the edge, followed by a separator (tabular, comma, etc.) and the value of one or more attributes.

The following is an example for an edge attribute with edge weights:

	EdgeID    Weight
	A:42:_:GLU (hbond:mc_mc) A:57:_:PRO    12
	A:86:_:SER (hbond:sc_sc) A:88:_:ARG    4

  

* * *

### Attribute Files for Cytoscape 2.x

Node and edge attributes are attached to nodes and edges. Attributes for a given node or edge will be applied to all copies of that node or edge in all loaded network files, regardless of whether the attribute file or network file is imported first. The specifications of these file types can be found in the [Cytoscape User Manual]("http://www.cytoscape.org/manual/Cytoscape2_6Manual.html#Node and Edge Attributes").

Each attribute is stored in a separate file. Node and edge attribute files use the same format. Node attribute file names often have the extension *na*, while edge attribute file names have the extension *ea*.

Every attribute file has one header line that gives the name of the attribute, and optionally some additional meta-information. The format is as follows:

	attributeName (class=formal.class.of.value)

If present, the class field defines the formal (package qualified) name of the class of the attribute values.

*   *String* for strings
*   *Double* for floating point values
*   *Integer* for integer values

If the attribute value is a list of values, the class should be the type of the objects in the list. Numbers and text strings are the most common attribute types. All values for a given attribute are of the same type.

A node attribute file begins with the name of the attribute on the first line (note that it cannot contain spaces). Each subsequent line contains the name of the node, followed by "=" and the value of that attribute.

A short example of a node attribute file for naming residues:

	ResidueName (class=String)
	A:42:_:GLU = GLU
	A:57:_:PRO = PRO

An example for a node attribute file with residue coordinates (using Ca atoms):

	ResidueCoord (class=String)
	A:42:_:GLU = (10,-20,-30)
	A:57:_:PRO = (-5,50,-100)

An example for a file containing the alignment position (not sequence position!) is:

	ResidueAlignment (class=Integer)
	A:42:_:GLU = 43
	A:57:_:PRO = 59

An edge attribute file has a very similar structure to the node attribute file. The name of the edge consists of the source node name, the interaction type in parentheses, and the target node name. The directionality is important, and this is why switching the source and target node will refer to a different (possibly non-existent) edge. Note that tabs are not allowed in edge names. Tabs can be used to separate the edge name from the "=" delimiter, but not within the edge name itself.

The following is an example for an edge attribute with edge weights:

	Weight (class=Integer)
	A:42:_:GLU (hbond:mc_mc) A:57:_:PRO = 12
	A:86:_:SER (hbond:sc_sc) A:88:_:ARG = 4

  