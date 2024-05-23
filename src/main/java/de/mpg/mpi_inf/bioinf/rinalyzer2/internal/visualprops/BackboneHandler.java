package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;

/**
 * This class provides a handler for the actions in the &quot;Backbone&quot; menu. There is a
 * different handler for each network for keeping a track of the removed and added edges.
 * 
 * @author Nadezhda Doncheva
 */
public class BackboneHandler {

	/**
	 * Initializes a new instance of <code>BackboneHandler</code>.
	 * 
	 * @param aNet
	 *            Network on which actions should be applied.
	 */
	public BackboneHandler(CyNetwork aNet) {
		network = aNet;
		backboneShown = false;
		bbEdges = new HashSet<CyEdge>();
	}

	/**
	 * Show the backbone edges of the protein. If they have been already created by calling the
	 * method <code>addBackboneEdges</code>, they are just restored, otherwise they are created.
	 * 
	 */
	public Set<CyEdge> showBackbone(CyNetworkView aView) {
		// If bbEdges are computed already, just show them, otherwise compute
		// and save them
		if (bbEdges.size() > 0) {
			for (CyEdge edge : bbEdges) {
				if (aView.getEdgeView(edge) != null) {
					aView.getEdgeView(edge).clearValueLock(BasicVisualLexicon.EDGE_VISIBLE);
					aView.getEdgeView(edge).setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);
				}
			}

		} else {
			addBackboneEdges();
		}
		backboneShown = true;
		aView.updateView();
		return bbEdges;
	}

	/**
	 * Look for already defined backbone edges and save them, so that they can be removed from the
	 * network. backbone edges are edges whose interaction type is "backbone".
	 */
	public void recognizeBackboneEdges() {
		for (CyEdge edge : network.getEdgeList()) {
			final String edgeInteraction = network.getRow(edge).get(CyEdge.INTERACTION,
					String.class);
			if (edgeInteraction != null && edgeInteraction.equals(Messages.EDGE_BACKBONE)) {
				bbEdges.add(edge);
			}
		}
		if (bbEdges.size() > 0) {
			backboneShown = true;
		}
	}

	/**
	 * Add backbone edges to the network. Backbone edges are defined as edges between residues
	 * (nodes) with successive residue number. They are drawn as black arrows and have an
	 * "interaction" type "backbone".
	 * 
	 * @return List with the indices of the newly created backbone edges.
	 */
	private void addBackboneEdges() {
		TreeMap<String, CyNode> index2node = null;
		if (network.getDefaultNodeTable().getColumn(Messages.SV_RINRESIDUE) != null) {
			RINFormatChecker rinChecker = new RINFormatChecker(network, CyUtils.splitNodeLabels(
					network, Messages.SV_RINRESIDUE));
			if (rinChecker.getErrorStatus() == null) {
				index2node = rinChecker.getResNodeMap();
			}
		} else {
			RINFormatChecker rinChecker = new RINFormatChecker(network, CyUtils.splitNodeLabels(
					network, CyNetwork.NAME));
			if (rinChecker.getErrorStatus() == null) {
				index2node = rinChecker.getResNodeMap();
			}
		}
		if (index2node.size() > 0) {
			final Iterator<Map.Entry<String, CyNode>> it = index2node.entrySet().iterator();
			String previousKey = index2node.firstKey();
			while (it.hasNext()) {
				final Map.Entry<String, CyNode> entry = it.next();
				// should be in the same chain and have consecutive residue
				// indices
				if (!entry.getKey().equals(previousKey)
						&& entry.getKey().startsWith(previousKey.substring(0, 1))) {
					addBackboneEdge(index2node.get(previousKey), entry.getValue());
				}
				previousKey = entry.getKey();
			}
		}
	}

	/**
	 * Add a new backbone edge between the nodes <code>source</code> and <code>target</code>.
	 * 
	 * @param source
	 *            Source node of the new edge.
	 * @param target
	 *            Target node of the new edge.
	 * @return Index of the newly created edge.
	 */
	private void addBackboneEdge(CyNode source, CyNode target) {
		final CyEdge edge = network.addEdge(source, target, true);
		network.getRow(edge).set(
				CyNetwork.NAME,
				CyUtils.getCyName(network, source) + " (" + Messages.EDGE_BACKBONE + ") "
						+ CyUtils.getCyName(network, target));
		network.getRow(edge).set(CyEdge.INTERACTION, Messages.EDGE_BACKBONE);
		if (network.getRow(edge).get(Messages.SV_INTSUBTYPE, String.class) != null) {
			network.getRow(edge).set(Messages.SV_INTSUBTYPE, Messages.EDGE_BACKBONE);
		}
		bbEdges.add(edge);
	}

	/**
	 * Remove (hide) the backbone edges from the network view.
	 */
	public void removeBackboneEdges(CyNetworkView aView) {
		CyNetwork net = aView.getModel();
		if (net.equals(network)) {
			net.removeEdges(bbEdges);
			bbEdges.clear();
		}
		// for (CyEdge edge : bbEdges) {
		// if (aView.getEdgeView(edge) != null) {
		// aView.getEdgeView(edge).clearValueLock(BasicVisualLexicon.EDGE_VISIBLE);
		// aView.getEdgeView(edge).setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
		// }
		// }
		aView.updateView();
		backboneShown = false;
	}

	/**
	 * Get the network stored in this instance of <code>BackboneHandler</code>.
	 * 
	 * @return Network stored in this instance of <code>BackboneHandler</code>.
	 */
	public CyNetwork getNetwork() {
		return network;
	}

	/**
	 * Check if backbone edges are shown in the network view.
	 * 
	 * @return <code>true</code> if backbone edges are shown in the network view, and
	 *         <code>false</code> otherwise.
	 */
	public boolean isBackboneShow() {
		return backboneShown;
	}

	/**
	 * Flag indicating if backbone edges are shown in the network view.
	 */
	private boolean backboneShown;

	/**
	 * Target network.
	 */
	private CyNetwork network;

	/**
	 * Array with the indices of backbone edges.
	 */
	private Set<CyEdge> bbEdges;

}
