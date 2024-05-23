package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Class for filtering the edges shown in the network according to their
 * interaction type.
 * 
 * @author Nadezhda Doncheva
 */
public class EdgeFilter {

	/**
	 * Initialize a new instance of <code>EdgeFilter</code>.
	 * 
	 * @param aNetwork
	 *            Network whose edges will be filtered
	 */
	public EdgeFilter(CyNetwork aNetwork) {
		network = aNetwork;
		initializeEdges();
	}

	/**
	 * Add a new edge type <code>edgeType</code>, that has appeared in the
	 * <code>network</code> after initializing this class.
	 * 
	 * @param edgeType
	 *            The new edge type.
	 * @param edges
	 *            The edge indices of the edges that have this interaction type.
	 */
	public void addEdgeType(String edgeType, Set<CyEdge> edges) {
		edgeTypes.add(edgeType);
		edgeTypesMap.put(edgeType, edges);
		shownEdgeTypes.put(edgeType, new Boolean(true));
	}

	/**
	 * Remove an edge type, that is no longer present.
	 * 
	 * @param edgeType
	 *            Edge type to be removed from the list of edge types.
	 */
	public void removeEdgeType(String edgeType) {
		if (edgeTypes.contains(edgeType)) {
			edgeTypes.remove(edgeType);
			edgeTypesMap.remove(edgeType);
			shownEdgeTypes.remove(edgeType);
		}
	}

	/**
	 * Return the names of the edge types of <code>network</code>.
	 * 
	 * @return Set of edge types names.
	 */
	public Set<String> getEdgeTypes() {
		return edgeTypes;
	}

	/**
	 * Get all network edges.
	 * 
	 * @return Set of edges.
	 */
	public Set<CyEdge> getAllEdges() {
		final Set<CyEdge> edges = new HashSet<CyEdge>();
		for (final String edgeType : edgeTypesMap.keySet()) {
			edges.addAll(edgeTypesMap.get(edgeType));
		}
		return edges;
	}

	/**
	 * Get a copy of the map of edge types and their current visibility flags.
	 * 
	 * @return Map of edge types and their current visibility flags.
	 */
	public Map<String, Boolean> getShownEdges() {
		return new HashMap<String, Boolean>(shownEdgeTypes);
	}

	/**
	 * Set the visibility flag of an edge type <code>attr</code> to the new
	 * value <code>newValue</code>.
	 * 
	 * @param attr
	 *            Edge type.
	 * @param newValue
	 *            New visibility value.
	 */
	public void setShownEdgeType(String attr, Boolean newValue) {
		shownEdgeTypes.put(attr, newValue);
	}

	/**
	 * Show only the edges in the network that have an edge type that is
	 * visible. Visibility flags are stored in the <code>shownEdges</code> map.
	 */
	public void showEdges(CyNetworkView aView) {
		for (final String edgeType : edgeTypes) {
			if (shownEdgeTypes.get(edgeType).booleanValue()) {
				Set<CyEdge> edges = edgeTypesMap.get(edgeType);
				for (CyEdge edge : edges) {
					if (aView.getEdgeView(edge) != null) {
						aView.getEdgeView(edge).clearValueLock(BasicVisualLexicon.EDGE_VISIBLE);
						aView.getEdgeView(edge).setLockedValue(BasicVisualLexicon.EDGE_VISIBLE,
								true);
					}
				}
			} else {
				Set<CyEdge> edges = edgeTypesMap.get(edgeType);
				for (CyEdge edge : edges) {
					if (aView.getEdgeView(edge) != null) {
						aView.getEdgeView(edge).clearValueLock(BasicVisualLexicon.EDGE_VISIBLE);
						aView.getEdgeView(edge).setLockedValue(BasicVisualLexicon.EDGE_VISIBLE,
								false);
					}
				}
			}
		}
		aView.updateView();
	}

	/**
	 * Pre-process network's edges. Build a map of all edge types and the
	 * respective edge indices of edges having this interaction type.
	 */
	private void initializeEdges() {
		shownEdgeTypes = new HashMap<String, Boolean>();
		edgeTypesMap = new HashMap<String, Set<CyEdge>>();
		edgeTypes = new TreeSet<String>();
		for (CyEdge edge : network.getEdgeList()) {
			// TODO: [Improve][VisProps] Show edges for a user-specified attribute
			String edgeAttr = null;
			if (network.getDefaultEdgeTable().getColumn(Messages.SV_INTSUBTYPE) != null) {
				edgeAttr = network.getRow(edge).get(Messages.SV_INTSUBTYPE, String.class);	
			} else {
				edgeAttr = network.getRow(edge).get(CyEdge.INTERACTION, String.class);
			}	
			if (edgeAttr == null) {
				continue;
			}
			if (!edgeTypesMap.containsKey(edgeAttr)) {
				edgeTypes.add(edgeAttr);
				final Set<CyEdge> edges = new HashSet<CyEdge>();
				edges.add(edge);
				edgeTypesMap.put(edgeAttr, edges);
				shownEdgeTypes.put(edgeAttr, new Boolean(true));
			} else {
				edgeTypesMap.get(edgeAttr).add(edge);
			}
		}
	}

	/**
	 * Set of edge types present in the network.
	 */
	private Set<String> edgeTypes;

	/**
	 * Map of edge types and edge indices arrays of edges having the respective
	 * edge type.
	 */
	private Map<String, Set<CyEdge>> edgeTypesMap;

	/**
	 * Target network.
	 */
	private CyNetwork network;

	/**
	 * Map of edge types and their current visibility flags.
	 */
	private Map<String, Boolean> shownEdgeTypes;
}
