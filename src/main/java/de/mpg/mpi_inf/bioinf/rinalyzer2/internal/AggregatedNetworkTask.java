package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout.NetworkAttributes;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout.RINStressLayoutContext;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout.StressMinimization;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.AggregatedGenerationDialog;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

/*
 * Creates a network based on the secondary structure elements in the RIN.
 * Could be extended for all string attributes.

 * @author Nadezhda Doncheva, Karsten Klein
 */
// TODO: [Release] Found multiple edges between two nodes?
// TODO: [improve] Add all residue identifiers to pdbFileName
public class AggregatedNetworkTask extends AbstractTask {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.AggregatedNetworkTask.class);

	@Tunable(description = "Enter name", gravity = 1.1)
	public String newNetName;

	@Tunable(description = "Select attribute", gravity = 1.3)
	public ListSingleSelection<String> attributeList;

	private CyServiceRegistrar context;
	private CyNetwork network;
	private RINVisualPropertiesManager visManager;
	// private CyNetwork subnetwork;
	private AggregatedGenerationDialog dialog;
	private RINFormatChecker rinChecker;
	private ArrayList<CyNode> sortedNodes = null; // the list of nodes in the RIN
	private Map<CyNode, Integer> ssIndex = null;
	// Successor of a node in a secondary structure element
	private Map<CyNode, CyNode> ssSucc = null;
	// Predecessor of a node in a secondary structure element
	private Map<CyNode, CyNode> ssPred = null;
	private static CyTable cyNodeTable = null;

	// User adjustable parameters
	private static Double defaultNodeHeight = 10.0;
	private static int maxEdgeWidth = 4; // Width for edges in secondary structure network
	private static String attributeSS = "SS";
	private String attribute = null;

	// Stores information about a secondary structure element
	// private class SSInfo {
	// SSInfo() {
	// this.type = SSType.sstUndef;
	// this.startIndex = -1;
	// length = 0;
	// x = 0.0;
	// y = 0.0;
	// }
	//
	// public SSType type;
	// public int startIndex;
	// public int length;
	// public double x; // takes up center coordinates
	// public double y;
	// };

	// Stores information about an aggregation element
	private class AttrInfo {
		AttrInfo() {
			this.type = "undef";
			this.startIndex = -1;
			length = 0;
			x = 0.0;
			y = 0.0;
		}

		public String type;
		public int startIndex;
		public int length;
		public double x; // takes up center coordinates
		public double y;
	};

	private class Connector {
		public CyEdge edge = null;
		public CyNode node = null;
		public int weight = 0;
		public boolean chain = false; // is part of the chain

		public boolean equals(Object c) {
			if (this == c)
				return true;
			if (!(c instanceof Connector))
				return false;

			return ((Connector) c).node.equals(node);
		}

		public Connector(CyNode n) {
			this.node = n;
			this.weight = 0;
			this.edge = null;
		}
	};

	public AggregatedNetworkTask(CyServiceRegistrar bc, CyNetwork aNetwork,
			RINVisualPropertiesManager aVisManager) {
		context = bc;
		network = aNetwork;
		visManager = aVisManager;

		// initialize
		ssIndex = new HashMap<CyNode, Integer>();
		ssSucc = new HashMap<CyNode, CyNode>();
		ssPred = new HashMap<CyNode, CyNode>();
		cyNodeTable = network.getDefaultNodeTable();

		newNetName = "aggregated_" + CyUtils.getCyName(network, network);
		List<String> nodeAttr = CyUtils.getStringAttributes(network, CyNode.class);
		attributeList = new ListSingleSelection<String>(nodeAttr);
		if (nodeAttr.contains(Messages.SS_ATTR_NAME)) {
			attributeList.setSelectedValue(Messages.SS_ATTR_NAME);
		}

		// ensure RIN format
		Map<CyNode, String[]> nodeLables = null;
		if (network.getDefaultNodeTable().getColumn(Messages.SV_RINRESIDUE) != null) {
			nodeLables = CyUtils.splitNodeLabels(network, Messages.SV_RINRESIDUE);
		} else {
			nodeLables = CyUtils.splitNodeLabels(network, CyNetwork.NAME);
		}
		rinChecker = new RINFormatChecker(network, nodeLables);
		// init();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_CREATEAGGRIN);

		// continue only if user clicked ok
		// if (dialog == null || dialog.isCanceled()) {
		// return;
		// }

		// create network from nodes in selected chains and their adjacent edges
		// final List<String> selChains = dialog.getSelectedChains();
		// if (selChains.size() == 0) {
		// return;
		// }
		// String name = dialog.getNetworkTitle();

		// get nodes and edges
		final List<CyNode> nodes = new ArrayList<CyNode>();
		final List<CyEdge> edges = new ArrayList<CyEdge>();
		if (attributeList == null) {
			taskMonitor.setTitle(Messages.LOG_NOATTR);
			return;
		}
		attribute = attributeList.getSelectedValue();

		// create network
		// nodes.clear();
		createAggregatedNetwork(nodes, edges, newNetName);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Create Aggregated RIN Options";
	}

	private boolean createAggregatedNetwork(List<CyNode> nodes, List<CyEdge> edges, String name) {
		// stores list of nodes sorted by chimera index
		sortedNodes = new ArrayList<CyNode>();
		// get sorted node list
		TreeMap<String, CyNode> indexMap = rinChecker.getResNodeMap();
		// New way to get sorted list, based on ChimeraUtils's sorted map
		Set<Map.Entry<String, CyNode>> set = indexMap.entrySet();
		// Just run through the already sorted map and construct list
		// sort and store result
		Iterator<Map.Entry<String, CyNode>> mapIterator = set.iterator();
		while (mapIterator.hasNext()) {
			Map.Entry<String, CyNode> me = (Map.Entry<String, CyNode>) mapIterator.next();
			sortedNodes.add(me.getValue());
		}

		// get factories and managers
		CyNetworkViewManager cyNetworkViewManager = (CyNetworkViewManager) CyUtils.getService(
				context, CyNetworkViewManager.class);

		// Create the new network
		CyNetworkFactory networkFactory = (CyNetworkFactory) CyUtils.getService(this.context,
				CyNetworkFactory.class);
		CyNetwork myNet = networkFactory.createNetwork();

		// Set name for network
		myNet.getRow(myNet).set(CyNetwork.NAME, name);

		// create attributes
		myNet.getDefaultNodeTable().createColumn("tooltip", String.class, false);
		myNet.getDefaultNodeTable().createColumn("type", String.class, false);
		myNet.getDefaultNodeTable().createColumn("width", Double.class, false);
		myNet.getDefaultNodeTable().createListColumn(Messages.defaultStructureKey, String.class,
				false);
		myNet.getDefaultNodeTable().createColumn(Messages.SV_RINRESIDUE, String.class, false);

		myNet.getDefaultEdgeTable().createColumn("isChain", Boolean.class, false);
		myNet.getDefaultEdgeTable().createColumn("width", Double.class, false);

		HashMap<CyNode, CyNode> nodeCopy = new HashMap<CyNode, CyNode>();
		// Node size for the ss network
		HashMap<CyNode, Integer> ssLength = new HashMap<CyNode, Integer>();

		// We also need the position and sizes from the parent view
		Collection<CyNetworkView> vc = cyNetworkViewManager.getNetworkViews(network);
		if (vc.size() > 1)
			logger.warn("Warning: Parent network view is supposed to be the first view in the view list");
		if (vc.size() == 0) {
			// TODO: [SSnetworks] do some fallback here
			logger.warn("Warning: Parent network view is supposed to exist");
		}
		Iterator<CyNetworkView> itv = vc.iterator();
		CyNetworkView parentView = itv.next();
		// /

		Iterator<CyNode> si = sortedNodes.iterator();

		// Get info on secondary structure
		ArrayList<AttrInfo> secStructures = new ArrayList<AttrInfo>();
		int ssicnt = 0;
		ssIndex.clear();
		int largeSSCount = 0, maxSS = 0;
		// ArrayList<Node> longSS;
		ArrayList<CyNode> tmplongSS = new ArrayList<CyNode>();
		String lastSS = "";// unique dummy string

		// TODO: [Improve] Check also for a change in chain identifier?
		// we detect the secondary structures by checking for a switch in
		// the SS attribute string
		CyNode lastNode = null;
		CyNode lastCopy = null; // Copy of representative (leading) node for a secondary structure
		int index = 0;
		while (si.hasNext()) {
			CyNode n = si.next();
			String s = null;
			if (cyNodeTable.getColumn(attribute) != null
					&& cyNodeTable.getColumn(attribute).getType() == String.class) {
				s = cyNodeTable.getRow(n.getSUID()).get(attribute, String.class);
			}
			if (s == null) {
				s = "";
			}
			if (!s.equals(lastSS)) {// new SS begins
				nodes.add(n);
				lastCopy = myNet.addNode();
				ssLength.put(lastCopy, 1);

				ssPred.put(n, null);
				if (lastNode != null)
					ssSucc.put(lastNode, null);
				AttrInfo ssi = new AttrInfo();
				ssi.startIndex = index;
				ssi.type = s;
				myNet.getRow(lastCopy).set(CyNetwork.NAME, s + " " + String.valueOf(ssicnt));
				myNet.getRow(lastCopy).set("type", s);
				myNet.getRow(lastCopy).set(Messages.SV_RINRESIDUE,
						network.getRow(n).get(CyNetwork.NAME, String.class));
				List<String> pdbIDs = new ArrayList<String>();
				if (network.getDefaultNodeTable().getColumn(Messages.defaultStructureKey) != null) {
					Class<?> colType = network.getDefaultNodeTable()
							.getColumn(Messages.defaultStructureKey).getType();
					if (colType == String.class) {
						pdbIDs.add(network.getRow(n)
								.get(Messages.defaultStructureKey, String.class));
					} else if (colType == List.class) {
						pdbIDs.addAll(network.getRow(n).getList(Messages.defaultStructureKey,
								String.class));
					}
				}
				myNet.getRow(lastCopy).set(Messages.defaultStructureKey, pdbIDs);
				// if (s.toLowerCase().equals("sheet")) {
				// ssi.type = SSType.sstSheet;
				// // System.out.println(" sh ");
				// } else if (s.toLowerCase().equals("helix")) {
				// ssi.type = SSType.sstHelix;
				// // System.out.println(" he ");
				// } else if (s.toLowerCase().equals("loop")) {
				// ssi.type = SSType.sstLoop;
				// // System.out.println(" lo ");
				// } else {
				// ssi.type = SSType.sstUndef;
				// }
				if (secStructures.size() > 0) {
					secStructures.get(secStructures.size() - 1).length = index
							- secStructures.get(secStructures.size() - 1).startIndex;
					secStructures.get(secStructures.size() - 1).x = secStructures.get(secStructures
							.size() - 1).x / secStructures.get(secStructures.size() - 1).length;
					secStructures.get(secStructures.size() - 1).y = secStructures.get(secStructures
							.size() - 1).y / secStructures.get(secStructures.size() - 1).length;
				}
				secStructures.add(ssi);
				ssicnt++;

				//
				if (largeSSCount > maxSS) {
					maxSS = largeSSCount;
					// longSS = tmplongSS;
				} else {
					tmplongSS.clear();
				}
				tmplongSS = new ArrayList<CyNode>();
				lastSS = s;
				largeSSCount = 1;

			} else { // continue old SS
				largeSSCount++;
				ssPred.put(n, lastNode);
				ssLength.put(lastCopy, ssLength.get(lastCopy) + 1);

				if (lastNode != null)
					ssSucc.put(lastNode, n);

				// // Debug
				ArrayList<CyEdge> adjEdges = new ArrayList<CyEdge>();
				adjEdges.addAll(network.getAdjacentEdgeList(n, Type.ANY));
				boolean found = false;
				for (CyEdge e : adjEdges) {
					CyNode es = e.getSource();
					CyNode et = e.getTarget();
					CyNode opp = null;

					if ((!es.equals(n))) {
						opp = es;
					}
					if ((!et.equals(n))) {
						opp = et;
					}
					if (opp == lastNode) {
						found = true;
						break;
					}
					// if (!found) System.err.println("Predecessor not found!!!!!!!!");
					// else System.out.println("Predecessor found/////////");
				}

				// //
			}
			assert lastCopy != null : "Copy is null";
			nodeCopy.put(n, lastCopy);
			tmplongSS.add(n);
			lastNode = n;
			secStructures.get(secStructures.size() - 1).x += parentView.getNodeView(n)
					.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			secStructures.get(secStructures.size() - 1).y += parentView.getNodeView(n)
					.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			// colorNode(cnodeViews.get(n), Color.GREEN);

			// TODO: [SSnetworks] Check how this is consistent with multiple connected components
			ssIndex.put(n, new Integer(ssicnt));
			index++;
		}// forall sortedNodes
		assert secStructures.size() == nodes.size() : "Node number does not correspond to secondary structures";

		// Collect info on types/labels
		HashMap<CyNode, String> vLabel = new HashMap<CyNode, String>();

		if (secStructures.size() > 0)
			secStructures.get(secStructures.size() - 1).length = index
					- secStructures.get(secStructures.size() - 1).startIndex;
		for (int l = 0; l < secStructures.size(); l++) {
			AttrInfo sinfo = secStructures.get(l);
			// /
			// Rectangle2D bbox = ViewUtils.getNodeBoundingBox(node, size, view, pos, scale);
			// List<CyCustomGraphics> cgList = new List<CyCustomGraphics>();
			// cgList.add(new CyCustomGraphics());
			// /viewer.getCustomGraphics(args, values, labels, bbox, view);
			// ViewUtils.addCustomGraphics(cgList, node, view);
			// /
			// System.out.println("Type: "+sinfo.type);
			CyNode v = nodes.get(l); // The representative for the ss
			StringBuffer sb = new StringBuffer();
			sb.append(sinfo.type);
			sb.append(" [");

			// if (sinfo.type == SSType.sstSheet) {
			ListIterator<CyNode> li = sortedNodes.listIterator(sinfo.startIndex);
			// System.out.println(" Structure length "+sinfo.length);
			// Run through all nodes of this structure
			for (int k = 0; k < sinfo.length; k++) {
				CyNode u = li.next();
				// Get the type label for u

				// if (cyNodeTable.getColumn("type") != null
				// && cyNodeTable.getColumn("type").getType() == String.class) {
				// sb.append(cyNodeTable.getRow(u.getSUID()).get("type", String.class));
				// sb.append(" ");
				// }
				if (cyNodeTable.getColumn("ResidueLabel") != null
						&& cyNodeTable.getColumn("ResidueLabel").getType() == String.class) {
					sb.append(" ");
					sb.append(cyNodeTable.getRow(u.getSUID()).get("ResidueLabel", String.class));
				}
				// Actually appended part could be null...?

				// View<CyNode> nview = cnodeViews.get(v);
				// TODO: [SSnetworks] Do something here
				// nview.setVisualProperty(BasicVisualLexicon.NODE_SHAPE,
				// NodeShapeVisualProperty.DIAMOND);//.PARALLELOGRAM);
				// How to get standard size to double it for debugging?
				// nview.setVisualProperty(BasicVisualLexicon.NODE_WIDTH,
				// 2.0*this.defaultNodeWidth);
				// nview.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT,
				// 2.0*this.defaultNodeHeight);
			}
			// }
			sb.append(" ]");
			vLabel.put(v, sb.toString());
		} // end forall secStructures elements

		// create network
		// In order to be able to use the subnetwork features we use the first node in each
		// structure
		// as a representative (would like to create a NEW network, though!)
		// System.out.println("Number of nodes: " + nodes.size());

		// /
		// Set name for new nodes
		// myNet.getRow(node1).set(CyNetwork.NAME, "Node1");
		// myNet.getRow(node2).set(CyNetwork.NAME, "Node2");

		// Add the network to Cytoscape
		CyNetworkManager networkManager = (CyNetworkManager) CyUtils.getService(this.context,
				CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = (CyNetworkViewFactory) CyUtils.getService(
				context, CyNetworkViewFactory.class);
		networkManager.addNetwork(myNet);
		CyNetworkView networkView = cyNetworkViewFactory.createNetworkView(myNet);
		cyNetworkViewManager.addNetworkView(networkView);
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(networkView);

		// Create edge structure
		int maxWeight = 0; // Max number of connection between ss nodes
		HashMap<CyNode, ArrayList<Connector>> connection = new HashMap<CyNode, ArrayList<Connector>>();
		HashMap<CyNode, Boolean> processed = new HashMap<CyNode, Boolean>(); // edges already
																				// considered?

		Iterator<CyNode> itn = sortedNodes.iterator();
		CyNode prevNode = null;
		while (itn.hasNext()) {
			CyNode u = itn.next();
			HashSet<CyEdge> adjEdges = new HashSet<CyEdge>();
			adjEdges.addAll(network.getAdjacentEdgeList(u, Type.ANY));
			// get ss element for this res node
			CyNode c = nodeCopy.get(u);
			assert c != null : "Fehler im System";
			// Is not really true at each point, but as nodes are consecutive, we can live with that
			// even though we first need to process all originals for a copy node.
			if (processed.get(c) == null) {
				processed.put(c, new Boolean(true));
			}

			boolean chainFound = false;
			for (CyEdge e : adjEdges) {
				CyNode opp = null;
				if ((!e.getSource().equals(u))) {
					opp = e.getSource();
				} else if (!e.getTarget().equals(u)) {
					opp = e.getTarget();
				}
				// TODO: [Bug] assertion is not working
				// assert opp != null : "Loop found";
				if (opp == null) {
					continue;
				}
				boolean isChain = false;
				// Check if we have a connection between different secondary structures
				// Our second case are chain connections which are not represented by an edge
				if (!(ssIndex.get(u).equals(ssIndex.get(opp)))) {
					// System.out.println("Connection between "+ ssIndex.get(u) + " " +
					// ssIndex.get(opp));
					// check if we found the chain predecessor or successor
					if (Math.abs(ssIndex.get(u) - ssIndex.get(opp)) == 1) {
						chainFound = true;
						isChain = true;
					}
					/*
					 * if (prevNode != null) { if (nodeCopy.get(prevNode) == nodeCopy.get(opp)) {
					 * chainFound = true; isChain = true; } }
					 */
					CyNode copp = nodeCopy.get(opp);
					// Check if the connection was already recorded (might just add all edges and
					// remove multiple edges to speed up the network creation
					if (processed.get(copp) != null)
						continue;

					if (connection.get(c) == null) {
						connection.put(c, new ArrayList<Connector>());
					}
					// Yes, not really fast
					Connector conn = new Connector(copp);
					if (isChain)
						conn.chain = true;
					int i = connection.get(c).indexOf(conn);
					if (i == -1) {
						conn.weight = 1;
						connection.get(c).add(conn);
						i = 0;
					} else {
						// Is it possible to hit a chain predecessor here, where the chain edge is
						// missing?
						// No, because here we only check the direct predecessor, so a later node in
						// the ss
						// will not have the predecessor
						connection.get(c).get(i).weight++;
					}

					if (connection.get(c).get(i).weight > maxWeight)
						maxWeight = connection.get(c).get(i).weight;
				}
			}
			// Now check if we need to add a chain edge to the predecessor
			if (connection.get(c) == null) {
				connection.put(c, new ArrayList<Connector>());
			}
			if (prevNode != null && !chainFound) {
				CyNode cpn = nodeCopy.get(prevNode);
				if (cpn != c) {
					Connector conn = new Connector(cpn);
					conn.chain = true;
					conn.weight = 1;
					int i = connection.get(c).indexOf(conn);
					if (i == -1)
						connection.get(c).add(conn);
					else
						connection.get(c).get(i).chain = true;

				}
			}
			prevNode = u;
		}// end getting edges for all sorted nodes

		// Fill the data structure for the layout call

		// Add edges to the substructure network, set sizes and labels
		ArrayList<CyEdge> addedEdges = new ArrayList<CyEdge>();
		ArrayList<CyNode> nodeList = new ArrayList<CyNode>();
		ArrayList<Double> weights = new ArrayList<Double>();
		ArrayList<Boolean> isChain = new ArrayList<Boolean>(); // total meschugge
		itn = nodes.iterator();
		int ind = 0; // used to run in parallel through ss list
		while (itn.hasNext()) {
			CyNode u = itn.next();
			CyNode c = nodeCopy.get(u);
			// View<CyNode> view = networkView.getNodeView(c);
			// AttrInfo sinfo = secStructures.get(ind);

			nodeList.add(c);

			myNet.getRow(c).set("tooltip", vLabel.get(u));
			myNet.getRow(c).set("width",
					nodeWidth(1.0) + nodeWidth(Math.max(1.0, new Double(2.0 * ssLength.get(c)))));
			// view.setVisualProperty(BasicVisualLexicon.NODE_SHAPE,
			// NodeShapeVisualProperty.RECTANGLE);
			// view.setVisualProperty(BasicVisualLexicon.NODE_TOOLTIP, vLabel.get(u));
			// view.setVisualProperty(BasicVisualLexicon.NODE_WIDTH,
			// nodeWidth(1.0) + nodeWidth(Math.max(1.0, new Double(2.0 * ssLength.get(c)))));
			// view.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, nodeHeight(1.0));

			// switch (sinfo.type) {
			// case sstSheet:
			// view.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.RED);
			// break;
			// case sstHelix:
			// view.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.GREEN);
			// break;
			// case sstLoop:
			// view.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.BLUE);
			// break;
			// default:
			// System.err.println("Unknown structure type encountered, ignored");
			// }
			ArrayList<Connector> al = connection.get(c);
			if (al != null) {
				Iterator<Connector> ital = al.iterator();
				while (ital.hasNext()) {
					Connector conn = ital.next();
					CyNode opp = conn.node;
					assert c != null;
					CyEdge edge = myNet.addEdge(c, opp, false);
					myNet.getRow(edge).set(
							CyNetwork.NAME,
							CyUtils.getCyName(myNet, c) + " (sscnt) "
									+ CyUtils.getCyName(myNet, opp));
					myNet.getRow(edge).set(Messages.EDGE_INTERACTIONS, "sscnt");
					myNet.getRow(edge).set("isChain", new Boolean(conn.chain));
					addedEdges.add(edge);
					weights.add(new Double(conn.weight));
					isChain.add(new Boolean(conn.chain));
				}
			}
			ind++;
		}

		// System.out.println("Max " + maxWeight);
		for (int k = 0; k < addedEdges.size(); k++) {
			myNet.getRow(addedEdges.get(k)).set("width",
					Math.max(1.0, edgeScale * maxEdgeWidth / (double) maxWeight * weights.get(k)));
			// View<CyEdge> eview = networkView.getEdgeView(addedEdges.get(k));
			// eview.setVisualProperty(BasicVisualLexicon.EDGE_WIDTH,
			// Math.max(1.0, edgeScale * maxEdgeWidth / (double) maxWeight * weights.get(k)));
			// if (!isChain.get(k)) {
			// eview.setVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE,
			// LineTypeVisualProperty.EQUAL_DASH);
			// eview.setVisualProperty(BasicVisualLexicon.EDGE_PAINT, Color.blue);
			// eview.setVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.blue);
			// }
		}

		// Do some visual and properties stuff
		createVisualStyle(networkView);

		// Do a layout

		NetworkAttributes na = new NetworkAttributes(nodeList, addedEdges);
		RINStressLayoutContext cont = new RINStressLayoutContext();
		StressMinimization sm = new StressMinimization(cont);

		for (int m = 0; m < nodeList.size(); m++) {
			CyNode cn = nodeList.get(m);
			View<CyNode> view = networkView.getNodeView(cn);
			na.w(cn, view.getVisualProperty(BasicVisualLexicon.NODE_WIDTH));
			na.h(cn, view.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT));
			na.x(cn, secStructures.get(m).x);
			na.y(cn, secStructures.get(m).y);
		}

		sm.hasInitialLayout(true);
		sm.call(na, myNet);

		// Set the coordinate values
		for (int j = 0; j < nodeList.size(); j++) {
			CyNode theNode = (CyNode) nodeList.get(j);
			View<CyNode> nView = networkView.getNodeView(theNode);

			// TODO: [RINLayout] check if removing this when moving to NetworkAttributes might cause
			// problems
			// if (na.x(theNode) != null) {
			nView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, na.x(theNode));
			nView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, na.y(theNode));
			// }
		}

		networkView.fitContent();
		networkView.updateView();

		return true;
	}

	private static double edgeScale = 1.5;
	private static double scaleFactor = 1.0;
	private static double defaultNodeWidth = 3.0;

	private double nodeHeight(double d) {
		return Math.max(1.0, scaleFactor * this.defaultNodeHeight * d);
	}

	private double nodeWidth(double d) {
		return Math.max(1.0, scaleFactor * this.defaultNodeWidth * d);
	}

	private void createVisualStyle(CyNetworkView view) {

		// To get references to services in CyActivator class
		VisualMappingManager vmmServiceRef = (VisualMappingManager) CyUtils.getService(
				this.context, VisualMappingManager.class);

		VisualStyleFactory visualStyleFactoryServiceRef = (VisualStyleFactory) CyUtils.getService(
				this.context, VisualStyleFactory.class);

		// VisualMappingFunctionFactory vmfFactoryC = (VisualMappingFunctionFactory) CyUtils
		// .getService(this.context, VisualMappingFunctionFactory.class,
		// "(mapping.type=continuous)");
		VisualMappingFunctionFactory vmfFactoryD = (VisualMappingFunctionFactory) CyUtils
				.getService(this.context, VisualMappingFunctionFactory.class,
						"(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = (VisualMappingFunctionFactory) CyUtils
				.getService(this.context, VisualMappingFunctionFactory.class,
						"(mapping.type=passthrough)");

		// To create a new VisualStyle object and set the mapping function
		VisualStyle vs = null;
		String visStyleTitle = newNetName;
		for (VisualStyle visstyle : vmmServiceRef.getAllVisualStyles()) {
			if (visstyle.getTitle().equals(visStyleTitle)) {
				vs = visstyle;
			}
		}
		if (vs == null) {
			vs = visualStyleFactoryServiceRef.createVisualStyle(vmmServiceRef
					.getDefaultVisualStyle());
			vs.setTitle(visStyleTitle);
			vmmServiceRef.addVisualStyle(vs);
		}

		// Use pass-through mapping
		Set<VisualPropertyDependency<?>> depnedencies = vs.getAllVisualPropertyDependencies();
		for (VisualPropertyDependency<?> dep : depnedencies) {
			if (dep.getDisplayName().equals("Lock node width and height")
					&& dep.isDependencyEnabled()) {
				dep.setDependency(false);
			}
		}
		vs.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.RECTANGLE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, nodeHeight(1.0));

		PassthroughMapping<String, ?> pMappingTooltip = (PassthroughMapping<String, String>) vmfFactoryP
				.createVisualMappingFunction("tooltip", String.class,
						BasicVisualLexicon.NODE_TOOLTIP);
		vs.addVisualMappingFunction(pMappingTooltip);

		PassthroughMapping<Double, Double> pMappingNodeWidth = (PassthroughMapping<Double, Double>) vmfFactoryP
				.createVisualMappingFunction("width", Double.class, BasicVisualLexicon.NODE_WIDTH);
		vs.addVisualMappingFunction(pMappingNodeWidth);

		PassthroughMapping<Double, Double> pMappingEdgeWidth = (PassthroughMapping<Double, Double>) vmfFactoryP
				.createVisualMappingFunction("width", Double.class, BasicVisualLexicon.EDGE_WIDTH);
		vs.addVisualMappingFunction(pMappingEdgeWidth);

		// TODO: [SSnetworks] Discuss coloring with Karsten
		DiscreteMapping<String, Paint> nodeColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
				.createVisualMappingFunction("type", String.class,
						BasicVisualLexicon.NODE_FILL_COLOR);
		// DiscreteMappingGenerator<Paint> test = (DiscreteMappingGenerator<Paint>) CyUtils
		// .getService(context, DiscreteMappingGenerator.class);
		// Set<String> values = new HashSet<String>();
		// values.add("Sheet");
		// values.add("Helix");
		// values.add("Loop");
		// nodeColorMapping.putAll(test.generateMap(values));
		Map<String, Color> colorMap = visManager.getColorMap(network);
		if (colorMap == null) {
			colorMap = Messages.colors;
		}
		nodeColorMapping.putMapValue(Messages.SS_SHEET, colorMap.get(Messages.SS_SHEET));
		nodeColorMapping.putMapValue(Messages.SS_HELIX, colorMap.get(Messages.SS_HELIX));
		nodeColorMapping.putMapValue(Messages.SS_LOOP, colorMap.get(Messages.SS_LOOP));
		vs.addVisualMappingFunction(nodeColorMapping);

		DiscreteMapping<Boolean, LineType> edgeTypeMapping = (DiscreteMapping<Boolean, LineType>) vmfFactoryD
				.createVisualMappingFunction("isChain", Boolean.class,
						BasicVisualLexicon.EDGE_LINE_TYPE);
		edgeTypeMapping.putMapValue(true, LineTypeVisualProperty.EQUAL_DASH);
		vs.addVisualMappingFunction(edgeTypeMapping);

		DiscreteMapping<Boolean, Paint> edgeColorMapping = (DiscreteMapping<Boolean, Paint>) vmfFactoryD
				.createVisualMappingFunction("isChain", Boolean.class,
						BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		edgeColorMapping.putMapValue(true, Color.BLUE);
		vs.addVisualMappingFunction(edgeColorMapping);

		// Apply the visual style to a NetwokView
		try {
			vmmServiceRef.setVisualStyle(vs, view);
			vs.apply(view);
		} catch (Exception ex) {
			// ignore
			// TODO: [Improve] Throws an exception sometimes. Why?
		}
	}

	// private void init() {
	// ensure RIN format
	// Map<CyNode, String[]> nodeLables = null;
	// if (network.getDefaultNodeTable().getColumn(Messages.SV_RINRESIDUE) != null) {
	// nodeLables = CyUtils.splitNodeLabels(network, Messages.SV_RINRESIDUE);
	// } else {
	// nodeLables = CyUtils.splitNodeLabels(network, CyNetwork.NAME);
	// }
	// rinChecker = new RINFormatChecker(network, nodeLables);
	//
	// if (rinChecker.getErrorStatus() != null) {
	// // msg to the user
	// JOptionPane.showMessageDialog(CyUtils.getCyFrame(context), rinChecker.getErrorStatus(),
	// Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
	// return;
	// }
	// final Set<String> chainIDs = rinChecker.getChainIDs();

	// if (cyNodeTable.getColumn(attribute) == null) {
	// // msg to the user
	// JOptionPane.showMessageDialog(CyUtils.getCyFrame(context), Messages.SM_SSATTRMISSING,
	// Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
	// return;
	// }

	// show dialog
	// dialog = new AbstractNetworkGenerationDialog(CyUtils.getCyFrame(context), chainIDs,
	// network
	// .getRow(network).get(CyNetwork.NAME, String.class));
	// dialog.setVisible(true);
	// }

}
