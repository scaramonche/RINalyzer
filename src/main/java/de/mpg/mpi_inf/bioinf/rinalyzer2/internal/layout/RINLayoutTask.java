package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

//package tudo.layout;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;

/***
 * 
 * class RinLayout constructs a layout for residue interaction networks.
 * 
 * @author Karsten Klein Portions of the FR layout based on the OGDF implementation
 * 
 */
public class RINLayoutTask extends AbstractLayoutTask {

	// TODO: [Improve][RINLayout] Layout only this subset of nodes
	// TODO: [Improve][RINLayout] Implement undo support

	// private String displayName;
	// private Set<View<CyNode>> nodesToLayOut;
	// private String layoutAttribute;
	// private UndoSupport undoSupport;
	// String innerRingAttributes;
	// List of attributes for inner ring;

	private BundleContext context;
	private CyNetwork network;
	private TaskMonitor taskMonitor;
	private CyNetworkView networkView;
	private RINLayoutContext layoutContext;
	// private BundleContext context;

	// the coordinates as given by chimera
	private Map<CyNode, Float[]> rinCoord = null;

	// the nodeviews in Cytoscape
	private Map<CyNode, View<CyNode>> cnodeViews = null;

	// Index number for secondary structure a node belongs to
	private Map<CyNode, Integer> ssIndex = null;

	// The node attributes from Cytoscape
	static CyTable cyNodeTable = null;

	// stores list of nodes sorted by chimera index
	private ArrayList<CyNode> sortedNodes = null;

	// Stores edge status: true=part of parallel bunch (first in bunch set to false)
	private HashMap<CyEdge, Boolean> parEdges = null;

	// Scaling method descriptors
	private enum Scaling {
		scInput, scUserBoundingBox, scScaleFunction
	};

	// indexes in x,y,z coordinate structure from chimera
	final static int chimCoordXIndex = 0;
	final static int chimCoordYIndex = 1;
	final static int chimCoordZIndex = 2;

	// Used to check calls to halt()
	boolean interrupted;

	// Stores maximum distance in z coordinates
	double zspan = 0.0;

	// Preliminary scale factor to scale the coordinates returned by chimera
	private final static double scaleFac = 100.0;

	// Used as minimal distance between connected components
	// TODO: [RINLayout] Should be related to CC area size
	private double minDistCC = 30;

	// distance factor plane to graph (* graph z range) only real distance if z values nonnegative
	static double planeDist = 0.5;

	// Should vertices be colored according to attribute string
	final static boolean coloring = false;

	// Should projection of 3D coordinates to plane be used?
	boolean project = false;

	public RINLayoutTask(BundleContext context, String displayName, CyNetworkView networkView,
			RINLayoutContext layoutContext, Set<View<CyNode>> nodesToLayOut,
			String layoutAttribute, UndoSupport undo) {
		super(displayName, networkView, nodesToLayOut, layoutAttribute, undo);
		// this.displayName = displayName;
		// this.nodesToLayOut = nodesToLayOut;
		// this.layoutAttribute = layoutAttribute;
		// this.undoSupport = undo;
		this.context = context;
		this.networkView = networkView;
		this.layoutContext = layoutContext;
		// this.context = bc;
		network = this.networkView.getModel();
		ssIndex = new HashMap<CyNode, Integer>();
		cyNodeTable = network.getDefaultNodeTable();
	}

	@Override
	protected void doLayout(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		taskMonitor.setStatusMessage("Fetching coordinates from Chimera...");
		if (!setInitialLayout()) {
			taskMonitor.setStatusMessage("Layout aborted");
			taskMonitor.setProgress(1.0);
			return;
		}
		taskMonitor.setProgress(0.1);
		construct();
		networkView.fitContent();
		networkView.updateView();
	}

	/**
	 * Didn't find any *real* documentation how construct (entry point for layouts...) is embedded
	 * in calling process (Action vs. doLayout,...) empirical evidence tells us that it is called by
	 * dolayout and dolayout with current view as parameter is the standard interface for layout
	 * calls (dolayout() is not called by cytoscape), therefore we do the layout here .
	 * 
	 * I also did not find out how to get to the Cytoscape Taskmonitor, so I wrote a wrapper Task
	 * class...
	 * */
	public void construct() {
		if (layoutContext.debugSwitch)
			return;// only set initial coordinates

		if (taskMonitor != null)
			taskMonitor.setProgress(0.15);

		// this.interrupted = false;
		// is there something to do?
		if (network == null || networkView == null) {
			System.err.println("Cannot construct layout: Network or networkView are null");
			return;
		}
		// colorFirstNode();

		// try to fetch attribute settings for inner ring nodes
		// Tunable t = layoutProperties.get(innerRingKey);
		// if (t != null)
		// {
		/*
		 * innerRingAttributes = ((String) t.getValue()); String[] s =
		 * innerRingAttributes.split(","); //check if attributes set if (s.length == 0) {
		 * JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
		 * "No node attributes selected to define rings.", "Error", JOptionPane.ERROR_MESSAGE);
		 * return; }
		 * 
		 * for (int i = 0; i < s.length;i++) System.out.println("Inner ring attribute: "+s[i]);
		 */

		// int nodeCount = networkView.nodeCount();

		//
		// System.out.println("NodeCount"+nodeCount);
		// System.out.println(networkView.getNodeViewCount());

		// Collect the two lists of nodes for inner and outer ring
		// First simple version: No further subgroups, no weights
		// for crossing minimization

		// List of nodes on inner ring
		// ArrayList innerNodes = new ArrayList<CyNode>();

		Iterator<View<CyNode>> nodeViews = networkView.getNodeViews().iterator();
		// int count = 0;

		// debug coloring
		boolean colorFirst = false;
		// now we need to assure that the network matches the one in chimera
		while (nodeViews.hasNext()) {
			View<CyNode> nView = nodeViews.next();
			// taskMonitor.setPercentCompleted((count++/nodeCount)*100);

			// TODO: [RINLayout] Why check if node is locked?
			// if (nView.isValueLocked(BasicVisualLexicon.NODE_X_LOCATION)) {
			// continue;
			// }
			CyNode theNode = nView.getModel();
			if (theNode == null) {
				// JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
				// "No node for nodeview found.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				System.err.println("No node for nodeview found.");
				return;
			}
			// debug coloring
			if (colorFirst) {
				// fancy stuff doesn't work, this one works:
				colorNode(nView, Color.BLACK);
				colorFirst = false;
			}

			/*
			 * nView.setOffset(currX, currY); count++;
			 * 
			 * if (count == columns) { count = 0; currX = initialX; currY += distanceBetweenNodes; }
			 * else { currX += distanceBetweenNodes; }
			 */

			if (cyNodeTable.getColumn("Marked") == null) {
				cyNodeTable.createColumn("Marked", Boolean.class, false, false);
			}
			cyNodeTable.getRow(theNode.getSUID()).set("Marked", true);
			if (this.interrupted)
				return;
		}

		try {
			runFRSpringLayout();
		} catch (Exception e) {
			System.err.println("Error in FR layout" + e.getMessage());
			// e.printStackTrace();
		}

		// taskMonitor.setStatus("Starting layout");
		// RadialLayout rl = new RadialLayout();
		// rl.performLayout(innerNodes, outerNodes);

		// addStyle();
		System.gc();
		// Remove curve style for parallel edges
		// Destroys Chimera coordinates only option...
		// TODO: [Improve][RINLayout] Set straight lines or bundle?
		// VisualPropsUtils.changeEdgeLines(network, networkView, 1);
		return;
		// }
		// System.out.println("Doing layout computation construct");
		// StartBilayer menu = new StartBilayer();
		// menu.actionPerformed(new ActionEvent(this, 0, ""));
	}

	protected boolean setInitialLayout() {
		// for an initial guess, we set the layout as a projection from the chimera layout
		if (!getRinCoordinates()) {
			// Dialog blocks the thread...
			// JOptionPane.showMessageDialog(
			// Cytoscape.getDesktop(),
			// "Please launch chimera and open the pdb structure corresponding to the selected network!",
			// Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
			System.err.println("RINLayout: No coordinates found, aborting");
			return false;
		}
		// we have to flip the y coordinates
		double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;
		// simple debug coloring
		// NodeView lastV =null;
		// int count = 0;
		// int nodeCount = networkView.nodeCount();

		// we also store the nodeviews in the same run
		cnodeViews = new HashMap<CyNode, View<CyNode>>();
		// now rinCoord should be filled with the coordinates from chimera
		Collection<View<CyNode>> nodeViews = networkView.getNodeViews();
		Iterator<View<CyNode>> nodeViewIterator = nodeViews.iterator();
		while (nodeViewIterator.hasNext()) {
			View<CyNode> nView = nodeViewIterator.next();
			// taskMonitor.setPercentCompleted((count++/nodeCount)*100);

			// lastV = nView;
			// for the initial setting we don't care if the node is locked
			// if (isLocked(nView)) {
			// continue;
			// }

			CyNode theNode = nView.getModel();
			if (theNode == null) {
				// JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
				// "No node for nodeview found.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				System.err.println("No node for nodeview found.");
				return false;
			}
			cnodeViews.put(theNode, nView);
			if (!rinCoord.containsKey(theNode)) {
				// return false;
				// TODO: [RINLayout] Should we just ignore nodes without coordinates?
				continue;
			}
			Float[] coords = rinCoord.get(theNode);
			// we scale, as the orders of magnitude for coordinates in chimera and cytoscape are
			// different to adapt to node sizes, see code fragment in comments below
			double yval = coords[chimCoordYIndex] * scaleFac;
			// nView.setXPosition(coords[chimCoordXIndex] * scaleFac);
			// nView.setYPosition(-yval);// flip here
			nView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, coords[chimCoordXIndex]
					* scaleFac);
			nView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, -yval);

			// this is just a preparation for flipping (if not just taking the negative)
			// if (yval < minY) minY = yval;
			// if (yval > maxY) maxY = yval;

			// We also would like to compute the total zspan
			double dz = coords[chimCoordZIndex] * scaleFac;
			if (dz < minZ)
				minZ = dz;
			if (dz > maxZ)
				maxZ = dz;
		}// while
			// store maximum z distance of all components, i.e. we do not degrade over the total
			// range
		zspan = Math.max(zspan, Math.abs(maxZ - minZ));
		// System.out.println("Z: "+zspan);
		// flip
		// just use neg yval currently
		// color some nodes for animation debug purposes
		int rcol = 255, gcol = 200, bcol = 0;
		Iterator<CyNode> si = sortedNodes.iterator();

		int ssicnt = 0;
		ssIndex.clear();
		int largeSSCount = 0, maxSS = 0;
		// ArrayList<Node> longSS;
		ArrayList<CyNode> tmplongSS = new ArrayList<CyNode>();
		String lastSS = "";// unique dummy string

		// we detect the secondary structures by checking for a switch in
		// the SS attribute string
		while (si.hasNext()) {
			CyNode n = si.next();
			String s = null;
			if (cyNodeTable.getColumn("SS") != null
					&& cyNodeTable.getColumn("SS").getType() == String.class) {
				s = cyNodeTable.getRow(n.getSUID()).get("SS", String.class);
			}
			if (s == null) {
				s = "";
			}
			if (!s.equals(lastSS))// new SS begins
			{
				ssicnt++;
				if (coloring) {
					// first we color the nodes
					rcol = Math.abs((ssicnt * 15) % 255);
					gcol = Math.abs((gcol + 40 * ssicnt) % 255);
					bcol = (ssicnt * 25) % 255;
					Iterator<CyNode> ito = tmplongSS.iterator();
					while (ito.hasNext()) {
						CyNode v = ito.next();
						colorNode(cnodeViews.get(v), new Color(rcol, gcol, bcol));
					}
				}
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

			} else {
				largeSSCount++;
			}
			tmplongSS.add(n);
			// colorNode(cnodeViews.get(n), Color.GREEN);

			ssIndex.put(n, new Integer(ssicnt));
		}// forall sortedNodes
			// System.out.println("SS: " + ssIndex.size());

		// Now we set the edge type from curved to straight
		// Should do this by attribute /appearance
		// VisualPropsUtils.changeEdgeLines(network, networkView,1);
		/*
		 * Appearance ap = new Appearance();
		 * 
		 * for (Iterator i = networkView.getEdgeViewsIterator(); i.hasNext(); ) { EdgeView edView =
		 * (EdgeView)i.next(); //if (edView.getLineType() == EdgeView.CURVED_LINES) {
		 * edView.setLineType(EdgeView.STRAIGHT_LINES); //} CyEdge e = (CyEdge)edView.getEdge();
		 * 
		 * cytoscape.data.CyAttributes edgeAtts = Cytoscape.getEdgeAttributes();
		 * 
		 * VisualPropertyType type = VisualPropertyType.EDGE_LINETYPE;
		 * 
		 * final Object defaultObj = type.setDefault(Cytoscape.getVisualMappingManager
		 * ().getVisualStyle(),);
		 * 
		 * //edgeAtts.setAttribute(e.getIdentifier(),, ); //ap.set(VisualPropertyType.EDGE_LINETYPE,
		 * new LineType(EdgeView.STRAIGHT_LINES, 2.0)); //ap.applyAppearance(edView); }
		 */
		return true;
	}// setInitialLayout

	/**
	 * Colors node represented by <code>nView</code> with Color <code>c</code>
	 * 
	 * @param nView
	 *            NodeView of the node to be colored.
	 * @param c
	 *            Color to be used.
	 */
	protected void colorNode(View<CyNode> nView, Color c) {
		// fancy stuff doesn't work, this one works:
		nView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, c);
	}

	/**
	 * Retrieves the coordinates from chimera and computes a node sorting based on a string
	 * concatenation of chain ID and residue index.
	 * 
	 * @return Success.
	 */
	private boolean getRinCoordinates() {
		// TODO: [RINLayout] Get coordinates from Chimera using annotate (see RINStressLayoutTask)
		// NetworkTaskFactory annotateFactory = (NetworkTaskFactory) CyUtils.getService(context,
		// NetworkTaskFactory.class, Messages.SV_ANNOTATECOMMANDTASK);
		// if (annotateFactory != null) {
		// TunableSetter tunableSetter = (TunableSetter) CyUtils.getService(context,
		// TunableSetter.class);
		// Map<String, Object> tunables = new HashMap<String, Object>();
		// ListMultipleSelection<String> resAttrTun = new ListMultipleSelection<String>(
		// Messages.SV_RESCOORDINATES);
		// resAttrTun.setSelectedValues(resAttrTun.getPossibleValues());
		// tunables.put("residueAttributes", resAttrTun);
		// TaskManager<?, ?> taskManager = (TaskManager<?, ?>) CyUtils.getService(context,
		// TaskManager.class);
		// taskManager.execute(tunableSetter.createTaskIterator(
		// annotateFactory.createTaskIterator(network), tunables));
		// }

		// ChimUtils.getCoordinatesOld(context, network);
		if (cyNodeTable == null || cyNodeTable.getColumn("resCoord.x") == null
				|| cyNodeTable.getColumn("resCoord.y") == null
				|| cyNodeTable.getColumn("resCoord.z") == null) {
			return false;
		}
		// get coordinates
		rinCoord = new HashMap<CyNode, Float[]>();
		try {
			for (CyNode node : network.getNodeList()) {
				Float[] nCoord = new Float[3];
				Double x = cyNodeTable.getRow(node.getSUID()).get("resCoord.x", Double.class);
				Double y = cyNodeTable.getRow(node.getSUID()).get("resCoord.y", Double.class);
				Double z = cyNodeTable.getRow(node.getSUID()).get("resCoord.z", Double.class);
				if (x == null || y == null || z == null) {
					System.err.println("No coordinates for " + CyUtils.getCyName(network, node));
					continue;
				}
				nCoord[0] = x.floatValue();
				nCoord[1] = y.floatValue();
				nCoord[2] = z.floatValue();
				rinCoord.put(node, nCoord);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (rinCoord.size() == 0) {
			return false;
		}
		// get sorted node list
		sortedNodes = new ArrayList<CyNode>();
		String attrName = CyNetwork.NAME;
		if (network.getDefaultNodeTable().getColumn(Messages.SV_RINRESIDUE) != null) {
			attrName = Messages.SV_RINRESIDUE;
		}
		RINFormatChecker rinChecker = new RINFormatChecker(network, CyUtils.splitNodeLabels(
				network, attrName));
		if (rinChecker.getErrorStatus() == null) {
			TreeMap<String, CyNode> indexMap = rinChecker.getResNodeMap();
			// New way to get sorted list, based on ChimeraUtils's sorted map
			Set<Map.Entry<String, CyNode>> set = indexMap.entrySet();
			// Just run through the already sorted map and construct list
			// sort and store result
			Iterator<Map.Entry<String, CyNode>> i = set.iterator();
			while (i.hasNext()) {
				Map.Entry<String, CyNode> me = (Map.Entry<String, CyNode>) i.next();
				sortedNodes.add(me.getValue());
			}
		} else {
			System.err.println("RINLayout: Not a RIN format");
			return false;
		}

		return true;
	}// getRINCoordinates

	// *************************************************************************
	// Fruchterman Reingold
	// *************************************************************************

	// algorithm control parameters
	double iterations, fineness, pageRatio, scaleFactor;
	// Bounding box parameters
	double xleft, ysmall, xright, ybig, bbXmin, bbXmax, bbYmin, bbYmax;
	double tx, ty, txNull, tyNull, width, height;

	// Width/Height are divided by these to derive maximum allowed movement
	double widdiv, heidiv;
	boolean noise;
	double kk, k2, thek;
	int cF, ki;
	// ArrayList<CyNode>[][] m_A; //Matrix of node lists, not allowed in Java
	HashMap<Integer, HashMap<Integer, ArrayList<CyNode>>> m_A;
	// Used to determine the scaling method
	Scaling scaling;
	// current number of iterations, used for fading z coord repulsion weakening
	int its;

	private void initFRSpringLayout() {
		// default parameters
		iterations = 800;
		fineness = 0.51;

		widdiv = 200;
		heidiv = 200;

		xleft = ysmall = 0.0;
		xright = ybig = 400.0;
		noise = true;

		// scScaleFunction;
		scaling = Scaling.scInput;

		scaleFactor = 8.0;
		bbXmin = 0.0;
		bbXmax = 100.0;
		bbYmin = 0.0;
		bbYmax = 100.0;

		// minDistCC = 20;
		pageRatio = 1.0;
	}

	/***
	 * 
	 * @param connComps
	 *            Contains ArrayLists of CyNodes for each CC at return. connComps has to be an
	 *            ArrayList.
	 */
	private void getConnectedComponents(ArrayList<ArrayList<CyNode>> connComps) {
		connComps.clear();
		Iterator<View<CyNode>> nodeViews = networkView.getNodeViews().iterator();
		if (!nodeViews.hasNext()) {
			return; // nothing to do
		}
		// int count = 0;
		// we collect the nodes in the current network.
		// make a standard DFS run, run over all nodes, recursion on trees.
		// we mark the visited nodes, value null if node was not visited yet.
		HashMap<CyNode, Boolean> marked = new HashMap<CyNode, Boolean>();

		CyNode theNode;

		while (nodeViews.hasNext()) {

			View<CyNode> nView = nodeViews.next();

			// check if we visited theNode already
			if (marked.get(nView.getModel()) == null) {
				// Start a new connected component
				ArrayList<CyNode> currentCC = new ArrayList<CyNode>();
				connComps.add(currentCC);
				// we build a new ArrayList that stores and processes the current DFS level
				// nonrecursively
				ArrayList<CyNode> currentLevel = new ArrayList<CyNode>();
				currentLevel.add(nView.getModel());
				marked.put(nView.getModel(), new Boolean(true));
				HashSet<CyEdge> adjEdges = new HashSet<CyEdge>();
				do {
					int cls = currentLevel.size();
					// collect neighbors of each node
					for (int i = 0; i < cls; i++) {
						theNode = currentLevel.remove(0);
						currentCC.add(theNode);
						// add neighbors
						adjEdges.clear();
						adjEdges.addAll(network.getAdjacentEdgeList(theNode, Type.ANY));
						for (CyEdge e : adjEdges) {
							CyNode es = e.getSource();
							CyNode et = e.getTarget();
							if ((marked.get(es) == null) && (!e.getSource().equals(theNode))) {
								currentLevel.add(es);
								marked.put(es, new Boolean(true));
							}
							if ((marked.get(et) == null) && (!e.getTarget().equals(theNode))) {
								currentLevel.add(et);
								marked.put(et, new Boolean(true));
							}
						}
					}
				} while (currentLevel.size() != 0);

			}

		}

	}

	/***
	 * 
	 * Runs actual layout algorithm by repeatedly executing the FR mainstep
	 * 
	 * @return success
	 */

	private boolean runFRSpringLayout() {
		initFRSpringLayout();
		// TODO: [RINLayout] initialize m_A (matrix of arraylists of CyNodes
		taskMonitor.setProgress(0.25);
		taskMonitor.setStatusMessage("Starting RINLayout");
		// compute connected component of our network

		// check: create in getconnectedcomponents and return?
		ArrayList<ArrayList<CyNode>> connComps = new ArrayList<ArrayList<CyNode>>();

		getConnectedComponents(connComps);
		// Debug
		/*
		 * System.out.println("Anzahl CC ist:"+connComps.size()); for (int k = 0; k <
		 * connComps.size(); k++) { System.out.println("Groesse:"+((ArrayList
		 * <CyNode>)connComps.get(k)).size()); }
		 */
		// Simple packing: First CC on top, rest in line below
		double yofs = 0.0; // height of first CC
		double[] xSizes = new double[connComps.size()];

		// EdgeArray<edge> auxCopy(G);
		// Array<DPoint> boundingBox(numCC);

		double minX = 0.0, maxX = 0.0, minY = 0.0, maxY = 0.0; // Coordinate
																// boundaries
		int i;
		for (i = 0; i < connComps.size(); ++i) {
			ArrayList<CyNode> nodeList = new ArrayList<CyNode>();
			for (CyNode node : connComps.get(i)) {
				if (rinCoord.containsKey(node)) {
					nodeList.add(node);
				}
			}

			// number of nodes in processed component
			int numNodes = nodeList.size();
			// System.out.println("Component size "+numNodes);
			// arrays to store the coordinates
			// we have to map neighbors to coords later on, therefore we don't use an array
			HashMap<CyNode, Double> xcoord = new HashMap<CyNode, Double>();
			HashMap<CyNode, Double> ycoord = new HashMap<CyNode, Double>();
			// zcoords are only used to weaken repulsion in main step
			HashMap<CyNode, Double> zcoord = new HashMap<CyNode, Double>();
			CyNode vFirst = (CyNode) nodeList.get(0);
			// double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;

			// set the initial layout as given
			// we also store the maximum z coordinate distance for repulsion correction some
			// projection values
			double camOfs = layoutContext.camDist * zspan;
			double zofs = camOfs + planeDist * zspan;
			for (int j = 0; j < numNodes; j++) {
				CyNode theNode = nodeList.get(j);
				Float[] coords = rinCoord.get(theNode);

				// we scale, as the orders of magnitude for coordinates in chimera and cytoscape are
				// different
				// to adapt to node sizes, see code fragment in comments below
				View<CyNode> nview = cnodeViews.get(theNode);
				// projection
				double xpos = nview.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double ypos = nview.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double dz = coords[chimCoordZIndex] * scaleFac;

				if (project && (layoutContext.camDist > 0.0) && (zspan > 0.0)) {
					xpos = camOfs * xpos / (zofs + dz);
					ypos = camOfs * ypos / (zofs + dz);
				}
				xcoord.put(theNode, xpos);
				ycoord.put(theNode, ypos);
				zcoord.put(theNode, new Double(dz));
			}

			if ((numNodes > 1) && initFR(xcoord, ycoord, nodeList)) {
				taskMonitor.setStatusMessage("Layout iterations");
				for (its = 1; its <= iterations; its++) {
					if (this.interrupted)
						return false;
					try {
						// System.out.println("iteration: " + its);
						mainStep(xcoord, ycoord, zcoord, nodeList);
						taskMonitor.setProgress((20.0d + (79 * its / iterations)) / 100);
					} catch (Exception e) {
						System.err.println("RINLayout: Error in main step" + e.getMessage());
					}
				}
			}

			try {
				// do the cleanup in memory, m_a is no longer needed
				m_A = null;

				minX = xcoord.get(vFirst);
				maxX = xcoord.get(vFirst);
				minY = ycoord.get(vFirst);
				maxY = ycoord.get(vFirst);

				// run over all nodes and compute bounding box
				try {
					for (int j = 0; j < numNodes; j++) {
						CyNode theNode = (CyNode) nodeList.get(j);
						if (xcoord.get(theNode)
								- cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_WIDTH) / 2.0 < minX)
							minX = xcoord.get(theNode)
									- cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_WIDTH) / 2.0;
						if (xcoord.get(theNode)
								+ cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_WIDTH) / 2.0 > maxX)
							maxX = xcoord.get(theNode)
									+ cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_WIDTH) / 2.0;
						if (ycoord.get(theNode)
								- cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_HEIGHT) / 2.0 < minY)
							minY = ycoord.get(theNode)
									- cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_HEIGHT) / 2.0;
						if (ycoord.get(theNode)
								+ cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_HEIGHT) / 2.0 > maxY)
							maxY = ycoord.get(theNode)
									+ cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_HEIGHT) / 2.0;
					}
				} catch (Exception e) {
					System.out.println("RINLayout BB error" + e.getMessage());

				}
				minX -= minDistCC;
				minY -= minDistCC;
				// System.out.println("Zmax, Zmin: "+maxZ+" "+minZ);

				xSizes[i] = maxX - minX;

				if (layoutContext.pack || connComps.size() == 1)
					for (int j = 0; j < numNodes; j++) {
						CyNode theNode = (CyNode) nodeList.get(j);
						xcoord.put(theNode, xcoord.get(theNode) - minX);
						ycoord.put(theNode, ycoord.get(theNode) - minY);
					}

				try {

					for (int j = 0; j < numNodes; j++) {
						CyNode theNode = (CyNode) nodeList.get(j);
						View<CyNode> nView = cnodeViews.get(theNode);

						if (xcoord.get(theNode) != null) {
							nView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
									xcoord.get(theNode));
							nView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
									ycoord.get(theNode));
						}
					}
				} catch (Exception e) {
					System.out.println("RINLayout set position " + e.getMessage());
				}

				// boundingBox[i] = DPoint(maxX - minX, maxY - minY);
			} catch (Exception e) {
				System.err.println("RINLayout: Error in Bounding Box computation" + e.getMessage());
			}

			// Set offset for packing
			if (i == 0) {
				yofs = maxY - minY;
			}
		}// for all connected components

		// TODO: [RINLayout] apply clever packer here:
		// The arrangement is given by offset to the origin of the coordinate
		// system. We still have to shift each node and edge by the offset
		// of its connected component.
		// simple step: first CC on top, then horizontal strip of remaining CCs
		// (size = max CC height)
		// centered vertically

		// TODO: [RINLayout] Assure that single node components assign nodes to zero center
		// Add offset for CCs
		// TODO: [RINLayout] Only make BB computations above when pack is true
		if (layoutContext.pack) {
			double xofs = 0; // second CC placed just below first one, all
								// others in line
			for (int k = 1; k < connComps.size(); k++) {
				ArrayList<CyNode> nodeList = (ArrayList<CyNode>) connComps.get(k);
				// number of nodes in processed component
				int numNodes = nodeList.size();
				for (int icc = 0; icc < numNodes; icc++) {
					CyNode cn = (CyNode) nodeList.get(icc);
					View<CyNode> cnv = cnodeViews.get(cn);
					// TODO: [RINLayout] we currently set the position twice, one time after
					// computation, one time for shifting, not necessary.
					cnv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
							cnv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) + xofs);
					cnv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
							cnv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) + yofs);
				}
				xofs += xSizes[k];

			}

		}
		return true;

	}// runFRSpringLayout

	double mylog2(int x) {
		double l = 0.0;
		while (x > 0) {
			l++;
			x >>= 1;
		}
		return l / 2;
	}

	double fRepulse(double d) {
		return ((k2 > (d)) ? kk / (d) : 0);
	}

	// precondition 0<=zinfluence<=1
	void mainStep(HashMap<CyNode, Double> xcoord, HashMap<CyNode, Double> ycoord,
			HashMap<CyNode, Double> zcoord, ArrayList<CyNode> nodeList) {
		int numNodes = nodeList.size();
		HashMap<CyNode, Double> xdisp = new HashMap<CyNode, Double>();
		HashMap<CyNode, Double> ydisp = new HashMap<CyNode, Double>();

		// Store CC status for each node
		HashMap<CyNode, Boolean> inCC = new HashMap<CyNode, Boolean>();

		// repulsive forces
		for (int i = 0; i < numNodes; i++) {
			CyNode cn = nodeList.get(i);
			double xv = (Double) xcoord.get(cn);
			double yv = (Double) ycoord.get(cn);
			inCC.put(cn, new Boolean(true));

			// if (xdisp.get(cn)==null)
			xdisp.put(cn, new Double(0.0));
			// if (ydisp.get(cn)==null)
			ydisp.put(cn, new Double(0.0));

			int li = (int) ((xv - xleft) / ki);
			int j = (int) ((yv - ysmall) / ki);
			try {
				// watch the neighbors
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						if ((m_A.get(li + m) == null))
							continue;
						if ((m_A.get(li + m).get(j + n) == null))
							continue;
						Iterator<?> it = m_A.get(li + m).get(j + n).iterator();

						while (it.hasNext()) {
							CyNode cm = (CyNode) it.next();
							assert cm != null : cm;
							assert cn != null : cn;
							if (cm.equals(cn))
								continue;
							double xdist = xv - (Double) xcoord.get(cm);
							double ydist = yv - (Double) ycoord.get(cm);
							double dist = Math.sqrt(xdist * xdist + ydist * ydist);
							if (dist < 1e-3)
								dist = 1e-3;

							// depending on the affiliation to the same structure, repulsion is
							// adapted
							double fac = (ssIndex.get(cm).intValue() == ssIndex.get(cn).intValue() ? layoutContext.ssinfac
									: layoutContext.ssoutfac);
							// System.out.println(fac);
							// depending on the distance in z coordinate, repulsion is adapted
							double zfac = 0.0;
							if (zspan > 0.0)
								zfac = layoutContext.zinfluence
										* (Math.abs((Double) zcoord.get(cn)
												- (Double) zcoord.get(cm)) / zspan);
							// Fade the weakening as the iterations proceed
							zfac = zfac * (1 - its / iterations);
							zfac = Math.min(0.99, zfac);
							zfac = Math.max(zfac, 0.0);

							xdisp.put(cn, new Double(xdisp.get(cn) + (1.0 - zfac) * fac
									* fRepulse(dist) * xdist / dist));
							ydisp.put(cn, new Double(ydisp.get(cn) + (1.0 - zfac) * fac
									* fRepulse(dist) * ydist / dist));
							// System.out.println((1.0-zfac)*fac*fRepulse(dist) * ydist / dist);
						}
					}
				}// for neighbors
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("RINLayout Mainstep Index error");
			} catch (Exception e) {
				// e.printStackTrace();
				System.out.println("RINLayout Mainstep error: " + e.toString());
				return;
			}
		}

		// attractive forces
		// we have two types of attraction: Edge based and induced by direct neighborhood in same
		// SSC (may not be modelled by an edge!)

		if (layoutContext.gluess) {
			// we run through the sorted list of vertices and insert attraction for a virtual edge
			// between subsequent vertices.
			Iterator<CyNode> si = sortedNodes.iterator();
			int lastssval = -1;// Store index of last vertex' ssc
			CyNode lastn = null;
			while (si.hasNext()) {
				try {
					CyNode n = (CyNode) si.next();
					// Values for vertices from other connected components are not initialized
					if (xcoord.get(n) == null)
						continue;

					int curssval = ssIndex.get(n).intValue(); // defined for all vertices

					// vertices from same secondary structure component
					if (curssval == lastssval) {
						// Treat pair of vertices as connected by (strong) edge
						// Same code as below for real edges, except user defined strengthening
						// factor
						assert n != null : "glue vertex null";
						assert lastn != null : "second glue vertex null";
						double xdist = (Double) xcoord.get(n) - (Double) xcoord.get(lastn);
						double ydist = (Double) ycoord.get(n) - (Double) ycoord.get(lastn);
						double dist = Math.sqrt(xdist * xdist + ydist * ydist);

						// Make movement slightly dependent on degrees
						// do not bring hubs to close together
						double f = (network.getAdjacentEdgeList(n, Type.ANY).size() + network
								.getAdjacentEdgeList(lastn, Type.ANY).size()) / 6.0;
						dist /= f;
						double fac = dist / thek;
						fac = fac * layoutContext.ssinAtt;

						// would be faster with arrays, but we need to access the values over v,u
						xdisp.put(n, new Double(xdisp.get(n) - xdist * fac));
						ydisp.put(n, new Double(ydisp.get(n) - ydist * fac));
						xdisp.put(lastn, new Double(xdisp.get(lastn) + xdist * fac));
						ydisp.put(lastn, new Double(ydisp.get(lastn) + ydist * fac));
					}
					lastn = n;
					lastssval = curssval;
				} catch (Exception e) {
					System.err.println("RINLayout mainstep: Glue err " + e.getMessage());
					// e.printStackTrace();
					break;
				}
			}
		}// gluess

		for (View<CyEdge> edView : networkView.getEdgeViews()) {
			try {
				// first get the corresponding node in the network
				CyEdge edge = (CyEdge) edView.getModel();

				// Skip parallel edges
				Boolean bb = parEdges.get(edge);
				if (bb != null)
					if (bb.booleanValue() == true)
						continue;

				CyNode u = (CyNode) edge.getSource();
				CyNode v = (CyNode) edge.getTarget();
				assert u != null : "Null pointer";
				assert v != null : "Null pointer";
				if ((inCC.get(u) == null) || (inCC.get(v) == null))
					continue;

				// Edge treatment code, same as above for subsequent vertices in SSC
				double xdist = (Double) xcoord.get(v) - (Double) xcoord.get(u);
				double ydist = (Double) ycoord.get(v) - (Double) ycoord.get(u);
				double dist = Math.sqrt(xdist * xdist + ydist * ydist);

				// Make movement slightly dependent on degrees do not bring hubs to close together
				double f = (network.getAdjacentEdgeList(u, Type.ANY).size() + network
						.getAdjacentEdgeList(v, Type.ANY).size()) / 6.0;
				dist /= f;

				double fac = dist / thek;
				// we put a strength factor on the movement, preventions (see below) make sure we
				// don't overdo
				if (ssIndex.get(u).intValue() == ssIndex.get(v).intValue()) {
					fac = fac * layoutContext.ssinAtt;
				}

				// would be faster with arrays, but we need to access the values
				// over v,u
				xdisp.put(v, new Double(xdisp.get(v) - xdist * fac));
				ydisp.put(v, new Double(ydisp.get(v) - ydist * fac));
				xdisp.put(u, new Double(xdisp.get(u) + xdist * fac));
				ydisp.put(u, new Double(ydisp.get(u) + ydist * fac));
			} catch (Exception e) {
				System.err.println("RINLayout mainstep: Placement error " + e.getMessage());
				// e.printStackTrace();
				break;
			}
		}

		// Debug
		// System.out.println("Kantenanzahl: "+edgeCount);

		// noise
		if (noise) {
			Random generator = new Random();
			for (int i = 0; i < numNodes; i++) {
				CyNode cn = nodeList.get(i);
				// generate a random offset between 0.75 and 1.25
				xdisp.put(cn, new Double(xdisp.get(cn)
						* ((750.0 + generator.nextDouble() * 500.0) / 1000)));
				ydisp.put(cn, new Double(ydisp.get(cn)
						* ((750.0 + generator.nextDouble() * 500.0) / 1000)));
			}
		}

		// Anchor
		double adjustFac = 0.2;
		if (layoutContext.anchor)
			for (int i = 0; i < numNodes; i++) {
				// Test: Try to stay close to original position
				CyNode v = nodeList.get(i);
				Float[] coords = rinCoord.get(v);
				// Original coordinates
				double dx = coords[chimCoordXIndex] * scaleFac;
				double dy = -coords[chimCoordYIndex] * scaleFac;// flip y value
				// Displacement computed
				double xd = xdisp.get(v);
				double yd = ydisp.get(v);

				double dist = Math.sqrt(xd * xd + yd * yd);
				// Real displacement
				// Could do this one time for both loops
				xd = tx * xd / dist;
				yd = ty * yd / dist;
				double newx = (Double) xcoord.get(v) + xd;
				double newy = (Double) ycoord.get(v) + yd;
				double distx = newx - dx;
				double disty = newy - dy;
				double ordist = Math.sqrt(distx * distx + disty * disty);
				double adjustx = tx * distx / ordist;
				double adjusty = ty * disty / ordist;
				xdisp.put(v, new Double(xdisp.get(v) - adjustx * adjustFac));
				ydisp.put(v, new Double(ydisp.get(v) - adjusty * adjustFac));
			}

		// preventions
		for (int i = 0; i < numNodes; i++) {
			CyNode v = nodeList.get(i);
			double xv = (Double) xcoord.get(v);
			double yv = (Double) ycoord.get(v);

			int i0 = (int) ((xv - xleft) / ki);
			int j0 = (int) ((yv - ysmall) / ki);

			double xd = xdisp.get(v);
			double yd = ydisp.get(v);

			double dist = Math.sqrt(xd * xd + yd * yd);

			if (dist < 1)
				dist = 1;

			xd = tx * xd / dist;
			yd = ty * yd / dist;

			double xp = xv + xd;
			double yp = yv + yd;

			int itmp, j;

			if ((xp > xleft) && (xp < xright)) {
				xcoord.put(v, new Double(xp));
				itmp = (int) ((xp - xleft) / ki);
			} else
				itmp = i0;

			if ((yp > ysmall) && (yp < ybig)) {
				ycoord.put(v, new Double(yp));
				j = (int) ((yp - ysmall) / ki);
			} else
				j = j0;
			// Change in grid structure
			if ((itmp != i0) || (j != j0)) {
				ArrayList<CyNode> al = null;
				try {
					al = m_A.get(i0).get(j0);
					assert al != null : "FR grid node list null" + i0 + " " + j0;
					// Remove entry i from al and move it to front in m_A(itmp,j)
					al.remove(v);
				} catch (Exception e) {
					System.out.println("Index error for FR grid remove in Preventions" + i0 + " "
							+ j0 + " " + itmp);
				}
				if (m_A.get(itmp) == null)
					m_A.put(itmp, new HashMap<Integer, ArrayList<CyNode>>());
				if (m_A.get(itmp).get(j) == null)
					m_A.get(itmp).put(j, new ArrayList<CyNode>());
				try {
					(m_A.get(itmp).get(j)).add(0, v);
				} catch (Exception e) {
					System.out.println("Index error for FR grid adding in Preventions" + i0 + " "
							+ j0 + " " + itmp);

				}
			}
		}

		tx = txNull / mylog2(cF);
		ty = tyNull / mylog2(cF);

		cF++;
	}// mainStep

	/**
	 * Initializes data structures for current connected component
	 * 
	 * @param xcoord
	 * @param ycoord
	 * @param nodeList
	 * @return
	 */

	boolean initFR(HashMap<CyNode, Double> xcoord, HashMap<CyNode, Double> ycoord,
			ArrayList<CyNode> nodeList) {
		int numNodes = nodeList.size();
		if (numNodes <= 1)
			return false;

		m_A = null;

		// Detect parallel edges
		parEdges = findParallel(nodeList);

		// compute a suitable area (xleft,ysmall), (xright,ybig)
		// zoom the current layout into that area

		double w_sum = 0.0, h_sum = 0.0;
		double xmin, xmax, ymin, ymax;

		CyNode v = (CyNode) nodeList.get(0);

		xmin = xmax = xcoord.get(v);
		ymin = ymax = ycoord.get(v);

		for (int i = 0; i < numNodes; i++) {
			v = (CyNode) nodeList.get(i);
			if (xcoord.get(v) < xmin)
				xmin = xcoord.get(v);
			if (xcoord.get(v) > xmax)
				xmax = xcoord.get(v);
			if (ycoord.get(v) < ymin)
				ymin = ycoord.get(v);
			if (ycoord.get(v) > ymax)
				ymax = ycoord.get(v);
			w_sum += cnodeViews.get(v).getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			h_sum += cnodeViews.get(v).getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
		}

		// System.out.println("Scaling");
		switch (scaling) {
		case scInput:
			xleft = xmin;
			xright = xmax;
			ysmall = ymin;
			ybig = ymax;
			break;

		case scUserBoundingBox:
		case scScaleFunction:
			if (scaling == Scaling.scUserBoundingBox) {
				xleft = bbXmin;
				xright = bbXmax;
				ysmall = bbYmin;
				ybig = bbYmax;

			} else {
				// Attention: these cases break relative CC positions!
				double sqrt_n = Math.sqrt((double) numNodes);
				xleft = 0;
				ysmall = 0;
				xright = ((w_sum > 0) ? scaleFactor * w_sum / sqrt_n : 1.0);
				ybig = ((h_sum > 0) ? scaleFactor * h_sum / sqrt_n : 1.0);
			}
			// Compute scaling such that layout coordinates fit into used bounding box
			double fx = (xmax == xmin) ? 1.0 : xright / (xmax - xmin);
			double fy = (ymax == ymin) ? 1.0 : ybig / (ymax - ymin);
			// Adjust coordinates accordingly
			for (int i = 0; i < numNodes; i++) {
				v = (CyNode) nodeList.get(i);
				xcoord.put(v, new Double(xleft + (xcoord.get(v) - xmin) * fx));
				ycoord.put(v, new Double(ysmall + (ycoord.get(v) - ymin) * fy));
			}
			break;
		default:
			throw new AssertionError(scaling);
		}// switch

		width = xright - xleft;
		height = ybig - ysmall;

		assert ((width >= 0) && (height >= 0)) : "Bounding box error";

		// System.out.println("width " + width);
		// bound the degree of freedom for the node movement
		txNull = width / widdiv * mylog2(numNodes); // dependent on numNodes?
		tyNull = height / heidiv * mylog2(numNodes);
		tx = txNull;
		ty = tyNull;

		// m_k = sqrt(m_width*m_height / G.numberOfNodes()) / 2;
		thek = fineness * Math.sqrt(width * height / numNodes);
		// System.out.println("K: "+thek);
		k2 = 2.0 * thek;
		kk = thek * thek * layoutContext.optDistFactor;// optDistFactor allows user influence
		// on distance radius

		ki = (int) thek;

		if (ki == 0)
			ki = 1;

		cF = 1;

		// build matrix of node lists
		int xA = (int) (width / ki + 1);
		int yA = (int) (height / ki + 1);
		m_A = new HashMap<Integer, HashMap<Integer, ArrayList<CyNode>>>();
		// ArrayList<CyNode>[xA+1][yA+1];
		// (-1,xA,-1,yA);

		// initialize m_A and put the nodes on the grid according to their current coordinates
		for (int i = 0; i < numNodes; i++) {
			v = (CyNode) nodeList.get(i);
			double xv = xcoord.get(v);
			double yv = ycoord.get(v);

			int hi = (int) ((xv - xleft) / ki);
			int j = (int) ((yv - ysmall) / ki);
			// System.out.println("hi,j: "+hi+" "+j);

			assert ((hi < xA) && (hi > -1)) : "Grid index error";
			assert ((j < yA) && (j > -1)) : "Grid index error";
			// (*m_A)(i,j).pushFront(v);
			// initialize necessary arrays and place node
			try {
				if (m_A.get(hi) == null)
					m_A.put(hi, new HashMap<Integer, ArrayList<CyNode>>());
				if (m_A.get(hi).get(j) == null)
					m_A.get(hi).put(j, new ArrayList<CyNode>());
				m_A.get(hi).get(j).add(0, v);

			} catch (Exception e) {
				System.err.println("Grid  initialization error" + e.toString());
				return false;
			}
		}// for

		return true;
	}

	// Simple Bucketsort to allow to ignore parallel edges. We could sort the edges first by src,
	// then by tgt. Faster and more space efficient than to scan node pairs. Instead we run over all
	// nodes, all adjacent edges and just detect if we get to an opposite vertex twice (a bit more
	// simpler to code). Returns HashMap on edges where true is stored if edge is one of a parallel
	// bunch, except for the first that stores false, null if edge not visited.
	private HashMap<CyEdge, Boolean> findParallel(ArrayList<CyNode> nodeList) {
		HashMap<CyEdge, Boolean> isPar = new HashMap<CyEdge, Boolean>();
		// Run over all vertices and their adjacent edges
		for (int i = 0; i < nodeList.size(); i++) {
			CyNode n = nodeList.get(i);
			// Using a HashMap makes our life easy here
			HashMap<CyNode, Boolean> nbor = new HashMap<CyNode, Boolean>();
			List<CyEdge> adjEdges = network.getAdjacentEdgeList(n, Type.ANY);
			for (CyEdge e : adjEdges) {
				CyNode es = e.getSource();
				CyNode et = e.getTarget();
				// Find opposite
				CyNode op = ((es.equals(n)) ? et : es);
				// Did we already visit the node?
				if (nbor.put(op, new Boolean(true)) != null) {
					isPar.put(e, new Boolean(true));
					// System.out.println("Parallel edge detected between" +
					// es.toString()+" - "+ et.toString());
				}

			}
		}
		return isPar;
	}// findparallel

}