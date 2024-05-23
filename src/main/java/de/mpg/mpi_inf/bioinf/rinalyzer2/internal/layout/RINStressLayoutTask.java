package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import java.awt.Color;
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
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.ChimUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;
//import org.cytoscape.ding.*;
//import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.RINLayoutTask.Scaling;

public class RINStressLayoutTask extends AbstractLayoutTask implements TaskObserver {

	public RINStressLayoutTask(CyServiceRegistrar context, String displayName,
			CyNetworkView networkView, RINStressLayoutContext layoutContext,
			Set<View<CyNode>> nodesToLayOut, String layoutAttribute, UndoSupport undo) {
		super(displayName, networkView, nodesToLayOut, layoutAttribute, undo);
		this.context = context;
		this.networkView = networkView;
		this.layoutContext = layoutContext;
		network = this.networkView.getModel();
		ssIndex = new HashMap<CyNode, Integer>();
		ssSucc = new HashMap<CyNode, CyNode>();
		ssPred = new HashMap<CyNode, CyNode>();
		cyNodeTable = network.getDefaultNodeTable();
		defaultNodeWidth = networkView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		defaultNodeHeight = networkView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
	}

	// TODO: [Improve][RINLayout] Layout only this subset of nodes
	// TODO: [Improve][RINLayout] Implement undo support

	// private String displayName;
	// private Set<View<CyNode>> nodesToLayOut;
	// private String layoutAttribute;
	// private UndoSupport undoSupport;
	// String innerRingAttributes;
	// List of attributes for inner ring;

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout.RINStressLayoutTask.class);

	private CyNetwork network;
	private TaskMonitor taskMonitor;
	private CyNetworkView networkView;
	private static RINStressLayoutContext layoutContext;
	private CyServiceRegistrar context;

	// the coordinates as given by chimera
	private Map<CyNode, Float[]> rinCoord = null;

	// the nodeviews in Cytoscape
	private Map<CyNode, View<CyNode>> cnodeViews = null;

	// Index number for secondary structure a node belongs to
	private Map<CyNode, Integer> ssIndex = null;
	// Successor of a node in a secondary structure element
	private Map<CyNode, CyNode> ssSucc = null;
	// Predecessor of a node in a secondary structure element
	private Map<CyNode, CyNode> ssPred = null;

	// The node attributes from Cytoscape
	static CyTable cyNodeTable = null;

	// stores list of nodes sorted by chimera index
	private ArrayList<CyNode> sortedNodes = null;

	// Stores edge status: true=part of parallel bunch (first in bunch set to false)
	private HashMap<CyEdge, Boolean> parEdges = null;

	// Stores default node size from visual properties
	double defaultNodeWidth;
	double defaultNodeHeight;

	// Scaling method descriptors
	private enum Scaling {
		scInput, scUserBoundingBox, scScaleFunction
	};

	// Secondary structure types
	public enum SSType {
		sstSheet, sstHelix, sstLoop, sstUndef
	};

	// Stores information about a secondary structure element
	private class SSInfo {
		SSInfo() {
			this.type = SSType.sstUndef;
			this.startIndex = -1;
			length = 0;
		}

		public SSType type;
		public int startIndex;
		public int length;
	};

	// Stores info on all secondary structure elements
	ArrayList<SSInfo> secStructures;

	// indexes in x,y,z coordinate structure from chimera
	final static int chimCoordXIndex = 0;
	final static int chimCoordYIndex = 1;
	final static int chimCoordZIndex = 2;

	// Used to check calls to halt()
	boolean interrupted;

	// Stores maximum distance in z coordinates
	double zspan = 0.0;

	// Preliminary scale factor to scale the coordinates returned by chimera
	// TODO: [RINLayout] Scale until smallest distance allows nodes without overlap
	private final static double scaleFac = 50.0;

	// Used as minimal distance between connected components
	// TODO: [RINLayout] Should be related to CC area size
	private double minDistCC = 30;

	// distance factor plane to graph (* graph z range) only real distance if z values nonnegative
	static double planeDist = 0.5;

	// Should vertices be colored according to attribute string
	final static boolean coloring = false;// true;

	// Should projection of 3D coordinates to plane be used?
	boolean project = false;

	boolean fetchedCoordinates = false;

	public void allFinished(FinishStatus arg0) {
		// ignore
	}

	public void taskFinished(ObservableTask arg0) {
		init();
	}

	@Override
	protected void doLayout(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
		taskMonitor.setStatusMessage("Fetching coordinates from Chimera...");
		fetchedCoordinates = ChimUtils.getCoordinates(context, this, network);
		if (fetchedCoordinates) {
			init();
		} else {
			logger.warn("No coordinates found, aborting");
			taskMonitor.setStatusMessage("No coordinates found, aborting.");
		}
	}

	private void init() {
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
			logger.warn("Cannot construct layout: Network or networkView are null");
			return;
		}
		// colorFirstNode();

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
				logger.warn("No node for nodeview found.");
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
			runStressLayout();
		} catch (Exception e) {
			logger.warn("Error in RINlayout");
			taskMonitor.setStatusMessage("Error in RINlayout");
		}

		// addStyle();
		System.gc();
		// Remove curve style for parallel edges
		// Destroys Chimera coordinates only option...
		// TODO: [Improve][RINLayout] Set straight lines or bundle?
		// VisualPropsUtils.changeEdgeLines(network, networkView, 1);
		return;
	}

	protected boolean setInitialLayout() {
		// for an initial guess, we set the layout as a projection from the chimera layout
		if (!getRinCoordinates()) {
			// Dialog blocks the thread...
			// JOptionPane.showMessageDialog(
			// Cytoscape.getDesktop(),
			// "Please launch chimera and open the pdb structure corresponding to the selected network!",
			// Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
			// logger.warn("No coordinates found, aborting");
			// taskMonitor.setStatusMessage("No coordinates found, aborting.");
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
				logger.warn("No node for a node view found.");
				taskMonitor.setStatusMessage("No node for a node view found.");
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

		// Get info on secondary structure
		secStructures = new ArrayList<SSInfo>();
		int ssicnt = 0;
		ssIndex.clear();
		int largeSSCount = 0, maxSS = 0;
		// ArrayList<Node> longSS;
		ArrayList<CyNode> tmplongSS = new ArrayList<CyNode>();
		String lastSS = "";// unique dummy string

		// we detect the secondary structures by checking for a switch in
		// the SS attribute string
		CyNode lastNode = null;
		int index = 0;
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
				ssPred.put(n, null);
				if (lastNode != null)
					ssSucc.put(lastNode, null);
				SSInfo ssi = new SSInfo();
				ssi.startIndex = index;
				if (s.toLowerCase().equals("sheet")) {
					ssi.type = SSType.sstSheet;
				} else if (s.toLowerCase().equals("helix")) {
					ssi.type = SSType.sstHelix;
				} else if (s.toLowerCase().equals("loop")) {
					ssi.type = SSType.sstLoop;
				} else {
					ssi.type = SSType.sstUndef;
				}
				// switch (s.toLowerCase()) {
				// case "sheet": ssi.type = SSType.sstSheet; break;
				// case "helix": ssi.type = SSType.sstHelix; break;
				// case "loop": ssi.type = SSType.sstLoop; break;
				// default: ssi.type = SSType.sstUndef;
				// }
				if (secStructures.size() > 0)
					secStructures.get(secStructures.size() - 1).length = index
							- secStructures.get(secStructures.size() - 1).startIndex;
				secStructures.add(ssi);
				ssicnt++;
				if (coloring) {
					// first we color the nodes
					rcol = Math.abs((ssicnt * 15) % 255);
					gcol = Math.abs((gcol + 40 * ssicnt) % 255);
					bcol = (ssicnt * 25) % 255;
					// / Or completely random
					// /Random r = new Random();
					// /Color c = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
					Color ssColor = new Color(rcol, gcol, bcol);
					Iterator<CyNode> ito = tmplongSS.iterator();
					while (ito.hasNext()) {
						CyNode v = ito.next();
						colorNode(cnodeViews.get(v), ssColor);
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
				ssPred.put(n, lastNode);
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
			tmplongSS.add(n);
			lastNode = n;
			// colorNode(cnodeViews.get(n), Color.GREEN);

			// TODO: [RINLayout] Check how this is consistent with multiple connected components
			ssIndex.put(n, new Integer(ssicnt));
			index++;
		}// forall sortedNodes
		if (secStructures.size() > 0)
			secStructures.get(secStructures.size() - 1).length = index
					- secStructures.get(secStructures.size() - 1).startIndex;
		for (int l = 0; l < secStructures.size(); l++) {
			SSInfo sinfo = secStructures.get(l);
			// /
			// Rectangle2D bbox = ViewUtils.getNodeBoundingBox(node, size, view, pos, scale);
			// List<CyCustomGraphics> cgList = new List<CyCustomGraphics>();
			// cgList.add(new CyCustomGraphics());
			// /viewer.getCustomGraphics(args, values, labels, bbox, view);
			// ViewUtils.addCustomGraphics(cgList, node, view);
			// /
			// System.out.println("Type: "+sinfo.type);
			if (sinfo.type == SSType.sstSheet) {
				ListIterator<CyNode> li = sortedNodes.listIterator(sinfo.startIndex);
				// System.out.println(" Structure length "+sinfo.length);
				for (int k = 0; k < sinfo.length; k++) {
					CyNode v = li.next();
					View<CyNode> nview = cnodeViews.get(v);
					// TODO: [RINLayout] use original here and depending on boolean parameter
					// nview.setVisualProperty(BasicVisualLexicon.NODE_SHAPE,
					// NodeShapeVisualProperty.DIAMOND);//.PARALLELOGRAM);
					// How to get standard size to double it for debugging?
					// nview.setVisualProperty(BasicVisualLexicon.NODE_WIDTH,
					// 2.0*this.defaultNodeWidth);
					// nview.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT,
					// 2.0*this.defaultNodeHeight);
				}
			}
		}
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
		if (cyNodeTable == null) {
			logger.warn("CyNode table is null");
			return false;
		}
		if (cyNodeTable.getColumn("resCoord.x") == null
				|| cyNodeTable.getColumn("resCoord.y") == null
				|| cyNodeTable.getColumn("resCoord.z") == null) {
			logger.warn("Coordinates attributes not found");
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
					logger.warn("No coordinates for " + CyUtils.getCyName(network, node));
					continue;
				}
				nCoord[0] = x.floatValue();
				nCoord[1] = y.floatValue();
				nCoord[2] = z.floatValue();
				rinCoord.put(node, nCoord);
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.warn("Coould not parse coordinates");
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
			logger.warn("Not a RIN format");
			return false;
		}

		return true;
	}// getRINCoordinates

	// algorithm control parameters
	double iterations, fineness, pageRatio, scaleFactor;
	// Bounding box parameters
	double xleft, ysmall, xright, ybig, bbXmin, bbXmax, bbYmin, bbYmax;
	double tx, ty, txNull, tyNull, width, height;

	// Width/Height are divided by these to derive maximum allowed movement
	double widdiv, heidiv;
	// ArrayList<CyNode>[][] m_A; //Matrix of node lists, not allowed in Java
	HashMap<Integer, HashMap<Integer, ArrayList<CyNode>>> m_A;
	// Used to determine the scaling method
	Scaling scaling;
	// current number of iterations, used for fading z coord repulsion weakening
	int its;

	private void initLayout() {
		// default parameters
	}

	// TODO: [RINLayout] Remove and use version from LayoutUtilities instead, and use parameter
	// gluess
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
						CyNode opp = ssPred.get(theNode);
						if (opp != null) {
							if (marked.get(opp) == null) {
								currentLevel.add(opp);
								marked.put(opp, new Boolean(true));
							}
						}
						// Successor
						opp = ssSucc.get(theNode);
						if (opp != null) {
							if (marked.get(opp) == null) {
								currentLevel.add(opp);
								marked.put(opp, new Boolean(true));
							}
						}
					}
				} while (currentLevel.size() != 0);

			}

		} // all views
	}

	/***
	 * 
	 * Runs actual layout algorithm by calling StressMinimization
	 * 
	 * @return success
	 */

	private boolean runStressLayout() {
		initLayout();
		// TODO: [RINLayout] initialize m_A (matrix of arraylists of CyNodes
		taskMonitor.setProgress(0.25);
		taskMonitor.setStatusMessage("Starting RINLayout");
		// compute connected component of our network

		// check: create in getconnectedcomponents and return?
		ArrayList<ArrayList<CyNode>> connComps = new ArrayList<ArrayList<CyNode>>();
		// A reminder: We assume here that there are no isolated nodes in a chain!
		getConnectedComponents(connComps);

		// Simple packing: First CC on top, rest in line below
		double yofs = 0.0; // height of first CC
		double[] xSizes = new double[connComps.size()];

		// EdgeArray<edge> auxCopy(G);
		// Array<DPoint> boundingBox(numCC);

		double minX = 0.0, maxX = 0.0, minY = 0.0, maxY = 0.0; // Coordinate
																// boundaries
		// For each of the components we run a stress minimization phase
		int i;
		for (i = 0; i < connComps.size(); ++i) {
			ArrayList<CyNode> nodeList = new ArrayList<CyNode>();
			for (CyNode node : connComps.get(i)) {
				if (rinCoord.containsKey(node)) {
					nodeList.add(node);
				}
			}
			ArrayList<CyEdge> edgeList = new ArrayList<CyEdge>();
			// TODO: [RINLayout] add the edges here
			NetworkAttributes na = new NetworkAttributes(nodeList, edgeList);

			// number of nodes in processed component
			int numNodes = nodeList.size();
			// System.out.println("Component size "+numNodes);
			// arrays to store the coordinates
			// we have to map neighbors to coords later on, therefore we don't use an array
			CyNode vFirst = (CyNode) nodeList.get(0);
			// double minZ = Double.MAX_VALUE, maxZ = Double.MIN_VALUE;

			// Set the initial layout as given by Chimera or other data
			// we also store the maximum z coordinate distance for repulsion correction some
			// projection values
			double camOfs = layoutContext.camDist * zspan;
			double zofs = camOfs + planeDist * zspan;
			for (int j = 0; j < numNodes; j++) {
				CyNode theNode = nodeList.get(j);

				Float[] coords = rinCoord.get(theNode);
				na.ssindex(theNode, ssIndex.get(theNode));
				na.ssPred(theNode, ssPred.get(theNode));
				na.ssSucc(theNode, ssSucc.get(theNode));

				// we scale, as the orders of magnitude for coordinates in chimera and cytoscape are
				// different
				// to adapt to node sizes, see code fragment in comments below
				View<CyNode> nview = cnodeViews.get(theNode);
				// / Test custom graphics
				// DNodeView dnv = (DNodeView) nview;

				// dnv.addCustomGraphic();
				// /
				// projection
				double xpos = nview.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double ypos = nview.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				Double w = nview.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
				Double h = nview.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
				double dz = coords[chimCoordZIndex] * scaleFac;

				if (project && (layoutContext.camDist > 0.0) && (zspan > 0.0)) {
					xpos = camOfs * xpos / (zofs + dz);
					ypos = camOfs * ypos / (zofs + dz);
				}
				na.x(theNode, xpos);
				na.y(theNode, ypos);
				na.z(theNode, dz);
				na.h(theNode, h);
				na.w(theNode, w);
			}

			// Now run the layout calculation
			if (numNodes > 1) {
				// taskMonitor.setStatusMessage("Layout iterations");
				// for (its = 1; its <= iterations; its++) {
				// if (this.interrupted)
				// return false;
				try {
					// System.out.println("iteration: " + its);
					StressMinimization sm = new StressMinimization(layoutContext);
					sm.convergenceCriterion(StressMinimization.TERMINATION_CRITERION.STRESS);
					sm.hasInitialLayout(true);// false);
					sm.call(na, this.network);
					// TODO: [RINLayout] Do a stepwise layout call to update monitor
					taskMonitor.setProgress((20.0d + (79 * its / iterations)) / 100);
				} catch (Exception e) {
					logger.warn("Error in layout computation step");
				}
				// }
			}

			try {

				minX = na.x(vFirst);
				maxX = na.x(vFirst);
				minY = na.y(vFirst);
				maxY = na.y(vFirst);

				// run over all nodes and compute bounding box
				try {
					for (int j = 0; j < numNodes; j++) {
						CyNode theNode = (CyNode) nodeList.get(j);
						// Scale by optimal distance parameter
						// Ok, this is just scaling for now, real distance is prepared in shortest
						// path computation
						// na.x(theNode, na.x(theNode)*layoutContext.optDistFactor);
						// na.y(theNode, na.y(theNode)*layoutContext.optDistFactor);
						if (na.x(theNode)
								- cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_WIDTH) / 2.0 < minX)
							minX = na.x(theNode)
									- cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_WIDTH) / 2.0;
						if (na.x(theNode)
								+ cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_WIDTH) / 2.0 > maxX)
							maxX = na.x(theNode)
									+ cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_WIDTH) / 2.0;
						if (na.y(theNode)
								- cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_HEIGHT) / 2.0 < minY)
							minY = na.y(theNode)
									- cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_HEIGHT) / 2.0;
						if (na.y(theNode)
								+ cnodeViews.get(theNode).getVisualProperty(
										BasicVisualLexicon.NODE_HEIGHT) / 2.0 > maxY)
							maxY = na.y(theNode)
									+ cnodeViews.get(theNode).getVisualProperty(
											BasicVisualLexicon.NODE_HEIGHT) / 2.0;
					}
				} catch (Exception e) {
					logger.warn("RINLayout BB error");

				}
				minX -= minDistCC;
				minY -= minDistCC;
				// System.out.println("Zmax, Zmin: "+maxZ+" "+minZ);

				xSizes[i] = maxX - minX;

				if (layoutContext.pack || connComps.size() == 1)
					for (int j = 0; j < numNodes; j++) {
						CyNode theNode = (CyNode) nodeList.get(j);
						na.x(theNode, na.x(theNode) - minX);
						na.y(theNode, na.y(theNode) - minY);
					}

				try {

					for (int j = 0; j < numNodes; j++) {
						CyNode theNode = (CyNode) nodeList.get(j);
						View<CyNode> nView = cnodeViews.get(theNode);

						// TODO: [RINLayout] check if removing this when moving to NetworkAttributes
						// might cause problems
						// if (na.x(theNode) != null) {
						nView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, na.x(theNode));
						nView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, na.y(theNode));
						// }
					}
				} catch (Exception e) {
					logger.warn("RINLayout set position ");
				}

				// boundingBox[i] = DPoint(maxX - minX, maxY - minY);
			} catch (Exception e) {
				logger.warn("Error in Bounding Box computation");
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

	}// runStressLayout

	double mylog2(int x) {
		double l = 0.0;
		while (x > 0) {
			l++;
			x >>= 1;
		}
		return l / 2;
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
