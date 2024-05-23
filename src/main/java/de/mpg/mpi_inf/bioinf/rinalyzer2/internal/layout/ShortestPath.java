package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortestPath {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout.ShortestPath.class);

	/**
	 * BFS to compute shortest path all pairs. The costs for traversing an edge corresponds to /a
	 * edgeCosts.
	 * 
	 * @param useNodeSize
	 *            If set to true, distances are computed respecting the size (radius) of the node to
	 *            avoid overlap
	 */
	public static void bfs_SPAP(NetworkAttributes na, double[][] distance, double edgeCosts,
			CyNetwork network, boolean useNodeSize) {
		HashMap<CyNode, Integer> nIndex = new HashMap<CyNode, Integer>();
		try {
			for (int v = 0; v < na.numberOfNodes(); v++) {
				nIndex.put(na.nodeList.get(v), v);
			}
		} catch (Exception e) {
			logger.warn("BFS: No node info in nodelist entry");
		}
		for (int v = 0; v < na.numberOfNodes(); v++) {
			CyNode n = na.nodeList.get(v);
			try {
				bfs_SPSS(n, nIndex, na, distance[v], edgeCosts, network, useNodeSize);
			} catch (Exception e) {
				logger.warn("Error in BFS calculation " + network);
			}
		}
		// System.out.println(edgeCosts);
	}

	/**
	 * Dijkstra algorithm to compute shortest path all pairs. The costs for traversing edge e
	 * corresponds to doubleWeight(e) in na
	 * 
	 * @return returns the average edge costs
	 * @throws InvalidAlgorithmParameterException
	 */
	public static double dijkstra_SPAP(NetworkAttributes na, double[][] shortestPathMatrix)
			throws InvalidAlgorithmParameterException {
		// TODO: [RINLayout]
		throw new InvalidAlgorithmParameterException();
		// return 0.0;
	}

	// Computes Single source shortest path for node pivNode with Dijkstra's algorithm
	public static void dijkstra_SPSS(CyNode pivNode, HashMap<CyNode, Integer> nIndex,
			NetworkAttributes na, double[] shortestPathSingleSource,
			HashMap<CyEdge, Double> edgeCosts, CyNetwork network) {

	}

	// Computes Single source shortest path for node pivNode with bfs
	public static void bfs_SPSS(CyNode pivNode, HashMap<CyNode, Integer> nIndex,
			NetworkAttributes na, double[] distances, double edgeCosts, CyNetwork network,
			boolean useNodeSize) {

		HashMap<CyNode, Boolean> marked = new HashMap<CyNode, Boolean>();
		HashMap<CyNode, Boolean> inCC = new HashMap<CyNode, Boolean>(); // Make sure to only follow
																		// edges to parts in na
		for (CyNode v : na.nodeList) {
			inCC.put(v, new Boolean(true));
		}
		try {
			HashMap<CyNode, Double> radius = null;
			if (useNodeSize) {
				radius = new HashMap<CyNode, Double>();
				for (CyNode v : na.nodeList) {
					radius.put(v, Math.sqrt(na.w(v) * na.w(v) + na.h(v) * na.h(v)) / 2.0);
				}
			}

			ArrayList<CyNode> bfs = new ArrayList<CyNode>();
			bfs.add(pivNode);
			// mark v and set distance to itself 0
			marked.put(pivNode, true);
			distances[nIndex.get(pivNode)] = 0.0;
			CyNode w;

			HashSet<CyEdge> adjEdges = new HashSet<CyEdge>();
			while (!(bfs.size() == 0)) {
				w = bfs.remove(0);

				double d = distances[nIndex.get(w)] + radius.get(w) + edgeCosts;
				adjEdges.clear();
				adjEdges.addAll(network.getAdjacentEdgeList(w, Type.ANY));

				for (CyEdge e : adjEdges) {
					CyNode es = e.getSource();
					CyNode et = e.getTarget();
					CyNode opp = null;
					// TODO: [RINLayout] Is that faster than containsKey?
					if ((marked.get(es) == null) && (!es.equals(w))) {
						opp = es;
					}
					if ((marked.get(et) == null) && (!et.equals(w))) {
						opp = et;
					}
					if (opp == null)
						continue;
					// TODO: [RINLayout] is that always reasonable
					if (!inCC.containsKey(opp)) {
						// logger.warn("Edge connection outgoing of CC");
						continue;
					}
					bfs.add(opp);
					marked.put(opp, new Boolean(true));
					distances[nIndex.get(opp)] = d + radius.get(opp);
				}
				// Try to add predecessor and successor of w in secondary structure
				// Predecessor
				CyNode opp = na.ssPred(w);
				if (opp != null) {
					// TODO: [RINLayout] check cases here
					if (inCC.get(opp) != null) {
						if (marked.get(opp) == null) {
							bfs.add(opp);
							marked.put(opp, new Boolean(true));
							distances[nIndex.get(opp)] = d + radius.get(opp);
						} else
							distances[nIndex.get(opp)] = Math.min(d + radius.get(opp),
									distances[nIndex.get(opp)]);
					}
				}
				// Successor
				opp = na.ssSucc(w);
				if (opp != null) {
					if (inCC.get(opp) != null) {
						if (marked.get(opp) == null) {
							bfs.add(opp);
							marked.put(opp, new Boolean(true));
							distances[nIndex.get(opp)] = d + radius.get(opp);
						} else
							distances[nIndex.get(opp)] = Math.min(d + radius.get(opp),
									distances[nIndex.get(opp)]);
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Error in BFS internal " + network);
		}
	}

}
