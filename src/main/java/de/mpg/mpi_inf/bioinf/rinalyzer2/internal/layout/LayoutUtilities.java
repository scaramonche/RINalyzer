package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

public class LayoutUtilities {
	/***
	 * 
	 * @param connComps
	 *            Contains ArrayLists of CyNodes for each CC at return. connComps has to be an
	 *            ArrayList.
	 */
	public static void getConnectedComponents(ArrayList<ArrayList<CyNode>> connComps, 
			CyNetwork network, 
			CyNetworkView networkView) {
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

}
