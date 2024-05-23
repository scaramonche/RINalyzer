package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.VisualPropsDialog;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.RINFormatChecker;

/**
 * Manager for the visual properties. Listens for commands coming from the
 * <code>VisualPropsDialog</code> and keeps track of all networks that have been modified somehow,
 * e.g. networks to which backbone edges have been added, or whose node labels have been modified.
 * 
 * @author Nadezhda Doncheva
 */
public class RINVisualPropertiesManager implements NetworkAboutToBeDestroyedListener {

	/**
	 * Initializes a new instance of <code>RINVisualPropertiesManager<code>.
	 */
	public RINVisualPropertiesManager(CyServiceRegistrar aContext) {
		context = aContext;
		networks = new HashSet<CyNetwork>();
		backboneHandlers = new HashMap<CyNetwork, BackboneHandler>();
		colorMaps = new HashMap<CyNetwork, Map<String, Color>>();
		edgeFilters = new HashMap<CyNetwork, EdgeFilter>();
		labelFilters = new HashMap<CyNetwork, NodeLabelFilter>();
		sizeConstants = new HashMap<CyNetwork, int[]>();
		visualStyles = new HashMap<CyNetwork, VisualStyle>();
		dialog = new VisualPropsDialog(CyUtils.getCyFrame(context), context, this);
		CyServiceRegistrar registrar = (CyServiceRegistrar) CyUtils.getService(context,
				CyServiceRegistrar.class);
		registrar.registerService(dialog, SetCurrentNetworkViewListener.class, new Properties());
		RINVisualPropertiesSerializer.loadVisProps(context);
	}

	/**
	 * Add a new network for processing. Create a backbone and node label handler for it. Retrieve
	 * the loaded colors and edge constants from the <code>RINalyzerPlugin</code> instance.
	 * 
	 * @param aNetwork
	 *            Network to be processed.
	 */
	public void addNetwork(CyNetwork aNetwork, CyNetworkView aView) {
		if (!networks.contains(aNetwork)) {
			networks.add(aNetwork);
			backboneHandlers.put(aNetwork, new BackboneHandler(aNetwork));
			colorMaps.put(aNetwork, RINVisualPropertiesSerializer.getColorMap());
			edgeFilters.put(aNetwork, new EdgeFilter(aNetwork));
			labelFilters.put(aNetwork, new NodeLabelFilter(aNetwork));
			sizeConstants.put(aNetwork, RINVisualPropertiesSerializer.getSizeConst());
			initVisualStyle(aNetwork);
		}
	}

	/**
	 * Remove processed network with all associated containers.
	 * 
	 * @param aNetwork
	 *            Network to be removed.
	 */
	public void removeNetwork(CyNetwork aNetwork) {
		if (networks.contains(aNetwork)) {
			networks.remove(aNetwork);
			backboneHandlers.remove(aNetwork);
			colorMaps.remove(aNetwork);
			edgeFilters.remove(aNetwork);
			labelFilters.remove(aNetwork);
			sizeConstants.remove(aNetwork);
			visualStyles.remove(aNetwork);
		}
	}

	/**
	 * Listen for network about to be destroyed events and remove the network from our maps.
	 */
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		// TODO: [Bug] Could be null?
		if (e.getNetwork() != null) {
			removeNetwork(e.getNetwork());
		}
	}

	/**
	 * Get dialog instance.
	 * 
	 * @return The dialog.
	 */
	public VisualPropsDialog getDialog() {
		return dialog;
	}

	/**
	 * Initialize a new visual style named with the network name and register it in the visual
	 * mapping manager.
	 * 
	 * @return The new visual style.
	 */
	public VisualStyle initVisualStyle(CyNetwork aNetwork) {
		VisualMappingManager cyVmManager = (VisualMappingManager) CyUtils.getService(context,
				VisualMappingManager.class);
		VisualStyleFactory cyVsFactory = (VisualStyleFactory) CyUtils.getService(context,
				VisualStyleFactory.class);
		CyRootNetworkManager netManager = (CyRootNetworkManager) CyUtils.getService(context,
				CyRootNetworkManager.class);
		CyNetwork rootNet = netManager.getRootNetwork(aNetwork);
		String newVisStyleName = rootNet.getRow(rootNet).get(CyNetwork.NAME, String.class);
		// if (vsName.endsWith(".sif")) {
		// vsName = vsName.substring(0, vsName.length() - 4);
		// }
		VisualStyle newVisStyle = null;
		for (VisualStyle vs : cyVmManager.getAllVisualStyles()) {
			if (vs.getTitle().equals(newVisStyleName)) {
				newVisStyle = vs;
			}
		}
		if (newVisStyle == null) {
			newVisStyle = cyVsFactory.createVisualStyle(cyVmManager.getDefaultVisualStyle());
			newVisStyle.setTitle(newVisStyleName);
			cyVmManager.addVisualStyle(newVisStyle);
		}
		if (getVisualStyle(aNetwork) == null) {
			visualStyles.put(aNetwork, newVisStyle);
		}
		CyNetworkViewManager viewManager = (CyNetworkViewManager) CyUtils.getService(context,
				CyNetworkViewManager.class);
		for (CyNetworkView netView : viewManager.getNetworkViews(aNetwork)) {
			cyVmManager.setVisualStyle(newVisStyle, netView);
			newVisStyle.apply(netView);
			netView.updateView();
		}
		return newVisStyle;
	}

	/**
	 * Perform a backbone operation, i.e. show/hide backbone/other network edges.
	 * 
	 * @param aCommand
	 *            Command to be executed.
	 * @param aNetwork
	 *            The network.
	 */
	public void backboneOperation(String aCommand, CyNetwork aNetwork) {
		final BackboneHandler bbhandler = backboneHandlers.get(aNetwork);
		CyNetworkViewManager manager = (CyNetworkViewManager) CyUtils.getService(context,
				CyNetworkViewManager.class);
		if (aCommand.equals(Messages.DI_BACKBONE_ADD)) {
			Set<CyEdge> bbEdges = new HashSet<CyEdge>();
			for (CyNetworkView view : manager.getNetworkViews(aNetwork)) {
				bbEdges.addAll(bbhandler.showBackbone(view));
			}
			addEdgeType(aNetwork, Messages.EDGE_BACKBONE, bbEdges);
		} else if (aCommand.equals(Messages.DI_BACKBONE_HIDE)) {
			for (CyNetworkView view : manager.getNetworkViews(aNetwork)) {
				bbhandler.removeBackboneEdges(view);
			}
			removeEdgeType(aNetwork, Messages.EDGE_BACKBONE);
		}
	}

	/**
	 * Perform a label operation, i.e. create a new node attribute storing modified node labels.
	 * 
	 * @param selectedLabels
	 *            Array with flags indicating which label parts should be shown, i.e. stored in the
	 *            node attribute "Node Label".
	 * @param isThreeLetterCode
	 *            Flag indicating if residue type should be displayed in three- or one-letter code.
	 * @param aNetowrk
	 *            The network.
	 */
	public void labelOperation(boolean[] selectedLabels, boolean isThreeLetterCode,
			CyNetwork aNetowrk) {
		final NodeLabelFilter labelFilter = labelFilters.get(aNetowrk);
		labelFilter.setSelectedLabels(selectedLabels);
		labelFilter.setThreeLetterCode(isThreeLetterCode);
		labelFilter.createLabelAttr(Messages.DI_LABEL_SEP, Messages.DI_LABEL);
		changeNodeLabel(visualStyles.get(aNetowrk), Messages.DI_LABEL);
		labelFilters.put(aNetowrk, labelFilter);
	}

	/**
	 * Get the backbone edge width for the network <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            The Network.
	 * @return Backbone edge width.
	 */
	public int getBBEdgeWidth(CyNetwork aNetwork) {
		return sizeConstants.get(aNetwork)[0];
	}

	/**
	 * Set the value of the backbone edges width in the network <code>aNetwork</code> to the new
	 * value <code>newBBEdgeWidth</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @param newEdgeSpace
	 *            New backbone edge width to be stored.
	 */
	public void setBBEdgeWidth(CyNetwork aNetwork, int newBBEdgeWidth) {
		final int[] constants = sizeConstants.get(aNetwork);
		constants[0] = newBBEdgeWidth;
		sizeConstants.put(aNetwork, constants);
	}

	/**
	 * Get the edge width for the network <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @return Edge width.
	 */
	public int getEdgeWidth(CyNetwork aNetwork) {
		return sizeConstants.get(aNetwork)[1];
	}

	/**
	 * Set the value of the edge width in the network <code>aNetwork</code> to the new value
	 * <code>newEdgeWidth</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @param newEdgeSpace
	 *            New edge width to be stored.
	 */
	public void setEdgeWidth(CyNetwork aNetwork, int newEdgeWidth) {
		final int[] constants = sizeConstants.get(aNetwork);
		constants[1] = newEdgeWidth;
		sizeConstants.put(aNetwork, constants);
	}

	/**
	 * Get the edge space between parallel edges for the network <code>aNetowrk</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @return Space between the edges.
	 */
	public int getEdgeSpace(CyNetwork aNetwork) {
		return sizeConstants.get(aNetwork)[2];
	}

	/**
	 * Set the value of the constant for space between parallel edges in the network
	 * <code>aNetowrk</code> to the new value <code>newEdgeSpace</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @param newEdgeSpace
	 *            New edge space to be stored.
	 */
	public void setEdgeSpace(CyNetwork aNetwork, int newEdgeSpace) {
		final int[] constants = sizeConstants.get(aNetwork);
		constants[2] = newEdgeSpace;
		sizeConstants.put(aNetwork, constants);
	}

	/**
	 * Get the label size for the network <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @return Node label size.
	 */
	public int getLabelSize(CyNetwork aNetwork) {
		return sizeConstants.get(aNetwork)[3];
	}

	/**
	 * Set the value of the constant for label size in the network <code>aNetwork</code> to the new
	 * value <code>newLabelSize</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @param newEdgeSpace
	 *            New label size to be stored.
	 */
	public void setLabelSize(CyNetwork aNetwork, int newLabelSize) {
		final int[] constants = sizeConstants.get(aNetwork);
		constants[3] = newLabelSize;
		sizeConstants.put(aNetwork, constants);
	}

	/**
	 * Get the node size for the network <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @return Space between the edges.
	 */
	public int getNodeSize(CyNetwork aNetwork) {
		return sizeConstants.get(aNetwork)[4];
	}

	/**
	 * Set the value of the constant for node size in the network <code>aNetwork</code> to the new
	 * value <code>newNodeSize</code>.
	 * 
	 * @param aNetwork
	 *            The network.
	 * @param newNodeSize
	 *            New node size to be stored.
	 */
	public void setNodeSize(CyNetwork aNetwork, int newNodeSize) {
		final int[] constants = sizeConstants.get(aNetwork);
		constants[4] = newNodeSize;
		sizeConstants.put(aNetwork, constants);
	}

	public int getEdgeDistFilter(CyNetwork aNetwork) {
		return sizeConstants.get(aNetwork)[5];
	}

	public void setEdgeDistFilter(CyNetwork aNetwork, int aValue) {
		final int[] constants = sizeConstants.get(aNetwork);
		constants[5] = aValue;
		sizeConstants.put(aNetwork, constants);
	}

	/**
	 * Get the values of all size constants (stored alphabetically, i.e. [bb edge width, edge width,
	 * edge space, node label, node size]) for the network with id <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @return Array with the size constants values.
	 */
	public int[] getSizeConst(CyNetwork aNetwork) {
		return sizeConstants.get(aNetwork);
	}

	/**
	 * Store the values of all size constants (stored alphabetically, i.e. [bb edge width, edge
	 * width, edge space, node label, node size]) for the network with id <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @param aSizeConst
	 *            Array with the three edge constant values.
	 */
	public void setSizeConst(CyNetwork aNetwork, int[] aSizeConst) {
		sizeConstants.put(aNetwork, aSizeConst);
	}

	/**
	 * Return the color mapping of nodes, edges and background for the network with id
	 * <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @return Map with defined color mappings.
	 */
	public Map<String, Color> getColorMap(CyNetwork aNetwork) {
		return colorMaps.get(aNetwork);
	}

	/**
	 * Store a new color mapping for the attribute <code>aEntry</code> of the network with id
	 * <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @param aEntry
	 *            Attribute that has a new color.
	 * @param newColor
	 *            The new color for the attribute <code>aEntry</code>.
	 */
	public void setColor(CyNetwork aNetwork, String aEntry, Color newColor) {
		final Map<String, Color> colorMap = colorMaps.get(aNetwork);
		colorMap.put(aEntry, newColor);
		colorMaps.put(aNetwork, colorMap);
	}

	/**
	 * Set the new color mapping of nodes, edges and background for the network with id
	 * <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @aColorMap Map with defined color mappings.
	 */
	public void setColorMap(CyNetwork aNetwork, Map<String, Color> aColorMap) {
		colorMaps.put(aNetwork, aColorMap);
	}

	/**
	 * Get the visual style of the network with id <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @return Visual style of the network or <code>null</code> if the network has not yet been
	 *         processed.
	 */
	public VisualStyle getVisualStyle(CyNetwork aNetwork) {
		if (visualStyles.containsKey(aNetwork)) {
			return visualStyles.get(aNetwork);
		}
		return null;
	}

	/**
	 * Check if the network with id <code>aNetwork</code> has been processed (stored in this
	 * instance) already.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @return <code>true</code> if this network has already been processed/added to this instance
	 *         of <code>VisualCommandListener</code>, and <code>false</code> otherwise.
	 */
	public boolean hasNetwork(CyNetwork aNetwork) {
		return networks.contains(aNetwork);
	}

	/**
	 * Check if the backbone edges of the network with id <code>aNetwork</code> are shown.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @return <code>true</code> if the backbone edges are shown, and <code>false</code> otherwise.
	 */
	public boolean isBackboneShown(CyNetwork aNetwork) {
		if (backboneHandlers.containsKey(aNetwork)) {
			return backboneHandlers.get(aNetwork).isBackboneShow();
		}
		return false;
	}

	/**
	 * Look for backbone edges, i.e. edges with interaction type "backbone", in the network with id
	 * <code>aNetwork</code> and save them.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 */
	public void recognizeBackboneEdges(CyNetwork aNetwork) {
		backboneHandlers.get(aNetwork).recognizeBackboneEdges();
	}

	/**
	 * Check which parts of the node labels of the nodes in the network with id
	 * <code>aNetwork</code> are shown. The label parts are pdb id, chain id, residue index, iCode,
	 * residue type.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @return Array of flags having value <code>true</code> if the the respective label part is
	 *         selected, and <code>false</code> otherwise.
	 */
	public boolean[] getSelectedLabels(CyNetwork aNetwork) {
		return labelFilters.get(aNetwork).getSelected();
	}

	/**
	 * Check which parts of the node labels of the nodes in the network with id
	 * <code>aNetwork</code> can be shown. The label parts are pdb id, chain id, residue index,
	 * iCode, residue type.
	 * 
	 * @param aNetwork
	 *            Id of the network.
	 * @return Array of flags having value <code>true</code> if the the respective label part can be
	 *         shown, and <code>false</code> otherwise.
	 */
	public boolean[] getEnabledLabels(CyNetwork aNetwork) {
		return labelFilters.get(aNetwork).getEnabled();
	}

	/**
	 * Check whether residue identifiers for network with ID <code>aNetwork</code> are displayed in
	 * 1- or 3-letter code.
	 * 
	 * @param aNetwork
	 *            Target network.
	 * @return <code>true</code> if the residue identifiers are in 3-letter code, and
	 *         <code>false</code> otherwise.
	 */
	public boolean getThreeLetterCode(CyNetwork aNetwork) {
		return labelFilters.get(aNetwork).getThreLetterCode();
	}

	/**
	 * Add a new edge type <code>edgeType</code>, that has appeared in the network with id
	 * <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Identifier of the target network.
	 * @param edgeType
	 *            New edge type added to the target network.
	 * @param edges
	 *            Array of edge indices of the edges of type <code>edgeType</code>.
	 */
	private void addEdgeType(CyNetwork aNetwork, String edgeType, Set<CyEdge> edges) {
		edgeFilters.get(aNetwork).addEdgeType(edgeType, edges);
	}

	/**
	 * Remove the edge type <code>edgeType</code>, that is no longer present in the network with id
	 * <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Identifier of the target network.
	 * @param edgeType
	 *            Edge type to be removed from the list of edge types for this network.
	 */
	private void removeEdgeType(CyNetwork aNetwork, String edgeType) {
		edgeFilters.get(aNetwork).removeEdgeType(edgeType);
	}

	/**
	 * Return the names of the edge types of edges in the network with id <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Identifier of the target network.
	 * @return Names of the edge types of edges in the network with id <code>aNetwork</code>.
	 */
	public Set<String> getEdgeTypes(CyNetwork aNetwork) {
		return edgeFilters.get(aNetwork).getEdgeTypes();
	}

	/**
	 * Get all edges in the network with id <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Identifier of the target network.
	 * @return Set of all edges in the network with id <code>aNetwork</code>.
	 */
	public Set<CyEdge> getAllEdges(CyNetwork aNetwork) {
		return edgeFilters.get(aNetwork).getAllEdges();
	}

	/**
	 * Get the map of edge types and their current visibility flags in the network with id
	 * <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Identifier of the target network.
	 * @return Map of edge types and their current visibility flags in the network with id
	 *         <code>aNetwork</code>.
	 */
	public Map<String, Boolean> getShownEdges(CyNetwork aNetwork) {
		return edgeFilters.get(aNetwork).getShownEdges();
	}

	/**
	 * Change the visibility value of the edge type <code>attr</code> in the network with id
	 * <code>aNetwork</code>.
	 * 
	 * @param aNetwork
	 *            Identifier of the target network.
	 * @param attr
	 *            Edge type with new visibility flag's value.
	 * @param newValue
	 *            New value of the visibility flag.
	 */
	public void setShownEdgeType(CyNetwork aNetwork, String attr, Boolean newValue) {
		edgeFilters.get(aNetwork).setShownEdgeType(attr, newValue);
	}

	/**
	 * Show the edges that have a visibility flag <code>true</code> in the network.
	 * 
	 * @param aNetwork
	 *            Identifier of the target network.
	 */
	public void showEdges(CyNetwork aNetwork) {
		CyNetworkViewManager viewManager = (CyNetworkViewManager) CyUtils.getService(context,
				CyNetworkViewManager.class);
		for (CyNetworkView view : viewManager.getNetworkViews(aNetwork)) {
			edgeFilters.get(aNetwork).showEdges(view);
		}
	}

	/**
	 * Change the background color in the visual style <code>aVisualStyle</code> according to the
	 * color keyed as <code>Messages.BGCOLOR</code> in <code>colorMap</code>.
	 * 
	 * @param aNetwork
	 *            Target network to apply new visual style to.
	 */
	public void changeBackgroundColor(CyNetwork aNetwork) {
		if (!networks.contains(aNetwork)) {
			return;
		}
		getVisualStyle(aNetwork).setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT,
				getColorMap(aNetwork).get(Messages.BGCOLOR));
	}

	/**
	 * Change the edge color mapping in the visual style <code>aVisualStyle</code> according to the
	 * colors in <code>colorMap</code> and the edge attribute {@link Messages#EDGE_INTERACTIONS}.
	 * 
	 * @param aNetwork
	 *            Target network to apply new visual style to.
	 */
	public void changeEdgeColor(CyNetwork aNetwork) {
		if (!networks.contains(aNetwork)) {
			return;
		}
		VisualMappingFunctionFactory vmfFactoryD = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		final VisualStyle vs = getVisualStyle(aNetwork);
		DiscreteMapping<String, Paint> lineColorMapping = null;
		if (aNetwork.getDefaultEdgeTable().getColumn(Messages.SV_INTSUBTYPE) != null) {
			lineColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
					.createVisualMappingFunction(Messages.SV_INTSUBTYPE, String.class,
							BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		} else {
			lineColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
					.createVisualMappingFunction(CyEdge.INTERACTION, String.class,
							BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);

		}

		for (final String entry : getEdgeTypes(aNetwork)) {
			lineColorMapping.putMapValue(entry, getColorMap(aNetwork).get(entry));
		}
		vs.addVisualMappingFunction(lineColorMapping);
	}

	/**
	 * Change the edge width mapping in the visual style <code>aVisualStyle</code> according to the
	 * edge attribute {@link Messages#EDGE_INTERACTIONS}.
	 * 
	 * @param aVisualStyle
	 *            Visual style of the target network.
	 * @param aNetwork
	 *            Target network to apply new visual style to.
	 * @param edgeTypes
	 *            Set of the interaction edge types names in the network <code>aNetwork</code>.
	 * @param aEdgeWidth
	 *            Edge lines width.
	 * @param aBBEdgeWidth
	 *            Backbone edge line width.
	 */
	public void changeEdgeWidth(CyNetwork aNetwork) {
		final VisualStyle vs = getVisualStyle(aNetwork);
		// Discrete mapping for edge line width
		VisualMappingFunctionFactory vmfFactoryD = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		DiscreteMapping<String, Double> lineWidthMapping = (DiscreteMapping<String, Double>) vmfFactoryD
				.createVisualMappingFunction("InteractionSubtype", String.class,
						BasicVisualLexicon.EDGE_WIDTH);
		for (final String entry : getEdgeTypes(aNetwork)) {
			if (entry.equals(Messages.EDGE_BACKBONE)) {
				lineWidthMapping.putMapValue(Messages.EDGE_BACKBONE, new Double(
						getBBEdgeWidth(aNetwork)));
			} else {
				lineWidthMapping.putMapValue(entry, new Double(getEdgeWidth(aNetwork)));
			}
		}
		vs.addVisualMappingFunction(lineWidthMapping);
	}

	/**
	 * Make multiple edge lines look straight and parallel.
	 * 
	 * @param aNetwork
	 *            Target network to apply new visual style to.
	 * @param aView
	 *            Network view of the target network.
	 * @param aEdgeSpace
	 *            Space to leave between the edges.
	 */
	public void addEdgeBends(CyNetwork aNetwork, CyNetworkView aView) {
		BendFactory bendFactory = (BendFactory) CyUtils.getService(context, BendFactory.class);
		HandleFactory handleFactory = (HandleFactory) CyUtils.getService(context,
				HandleFactory.class);
		final VisualProperty<Double> xLoc = BasicVisualLexicon.NODE_X_LOCATION;
		final VisualProperty<Double> yLoc = BasicVisualLexicon.NODE_Y_LOCATION;
		int aEdgeSpace = getEdgeSpace(aNetwork);

		final Set<CyEdge> doneEdges = new HashSet<CyEdge>();
		for (CyEdge edge : aNetwork.getEdgeList()) {
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			List<CyEdge> connEdges = aNetwork.getConnectingEdgeList(source, target, Type.ANY);
			int space = 0;
			for (final CyEdge connEdge : connEdges) {
				if (!doneEdges.contains(connEdge)) {
					try {
						final View<CyEdge> edgeView = aView.getEdgeView(connEdge);
						final View<CyNode> sourceView = aView.getNodeView(connEdge.getSource());
						final View<CyNode> targetView = aView.getNodeView(connEdge.getTarget());
						final double x1 = sourceView.getVisualProperty(xLoc);
						final double y1 = sourceView.getVisualProperty(yLoc);
						final double x2 = targetView.getVisualProperty(xLoc);
						final double y2 = targetView.getVisualProperty(yLoc);
						final double h = sourceView
								.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
						final Bend bend = bendFactory.createBend();
						// System.out.println("Node1: "
						// + aNetwork.getRow(source).get(CyNetwork.NAME, String.class)
						// + " Node2: "
						// + aNetwork.getRow(target).get(CyNetwork.NAME, String.class));
						// System.out.println("x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " +
						// y2
						// + "h: " + h);
						final Point2D point1 = new Point();
						point1.setLocation(x1, y1);
						final Point2D point2 = new Point();
						point2.setLocation(x2, y2);
						final double d1 = Math.abs(x1 - x2);
						final double d2 = Math.abs(y1 - y2);
						final double d3 = point1.distance(point2);
						// System.out.println("d1: " + d1 + " d2: " + d2 + " d3: " + d3);
						final double a1 = (d1 * h) / (2 * d3);
						final double a2 = (d2 * h) / (2 * d3);
						// System.out.println("a1: " + a1 + " a2: " + a2);
						// System.out.println("Edge: " + connEdge.getIdentifier() +
						// " Space: " + space);
						double h1x = 0.0;
						double h1y = 0.0;
						double h2x = 0.0;
						double h2y = 0.0;
						double h3x = 0.0;
						double h3y = 0.0;
						if (x1 < x2) {
							if (y1 < y2) {
								h1x = x1 + a1 + space;
								h1y = y1 + a2 - space;
								h2x = x1 + d1 / 2 + space;
								h2y = y1 + d2 / 2 - space;
								h3x = x2 - a1 + space;
								h3y = y2 - a2 - space;

							} else {
								h1x = x1 + a1 - space;
								h1y = y1 - a2 - space;
								h2x = x1 + d1 / 2 - space;
								h2y = y1 - d2 / 2 - space;
								h3x = x2 - a1 - space;
								h3y = y2 + a2 - space;
							}
						} else {
							if (y1 < y2) {
								h1x = x1 - a1 + space;
								h1y = y1 + a2 + space;
								h2x = x1 - d1 / 2 + space;
								h2y = y1 + d2 / 2 + space;
								h3x = x2 + a1 + space;
								h3y = y2 - a2 + space;
							} else {
								h1x = x1 - a1 + space;
								h1y = y1 - a2 - space;
								h2x = x1 - d1 / 2 + space;
								h2y = y1 - d2 / 2 - space;
								h3x = x2 + a1 + space;
								h3y = y2 + a2 - space;
							}
						}
						final Handle handle1 = handleFactory
								.createHandle(aView, edgeView, h1x, h1y);
						final Handle handle2 = handleFactory
								.createHandle(aView, edgeView, h2x, h2y);
						final Handle handle3 = handleFactory
								.createHandle(aView, edgeView, h3x, h3y);
						bend.insertHandleAt(0, handle1);
						bend.insertHandleAt(1, handle2);
						bend.insertHandleAt(2, handle3);
						edgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bend);
						doneEdges.add(connEdge);
						space += aEdgeSpace;
					} catch (Exception ex) {
						// ignore
					}
				}
			}
		}
		aView.updateView();
	}

	public void removeEdgeBends(CyNetwork aNetwork, CyNetworkView aView) {
		for (CyEdge edge : aNetwork.getEdgeList()) {
			View<CyEdge> edgeView = aView.getEdgeView(edge);
			edgeView.clearValueLock(BasicVisualLexicon.EDGE_BEND);
		}
		aView.updateView();
	}

	/**
	 * Change the the node color mapping in the visual style <code>aVisualStyle</code> according to
	 * the colors in <code>colorMap</code> and the node attribute {@link Messages#SS_ATTR_NAME} or
	 * {@link Messages#SS_ATTR_NAME_ALT}.
	 * 
	 * @param aNetwork
	 *            Target network.
	 */
	public void changeNodeColor(CyNetwork aNetwork) {
		final VisualStyle vs = getVisualStyle(aNetwork);
		final Map<String, Color> colorMap = getColorMap(aNetwork);

		// set default values
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, colorMap.get(Messages.SS_DEFAULT));
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SIZE, new Double(getNodeSize(aNetwork)));
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, getLabelSize(aNetwork));

		// create and set node color mapping
		VisualMappingFunctionFactory vmfFactoryD = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		DiscreteMapping<String, Paint> nodeColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
				.createVisualMappingFunction(Messages.SS_ATTR_NAME, String.class,
						BasicVisualLexicon.NODE_FILL_COLOR);
		nodeColorMapping.putMapValue(Messages.SS_HELIX, colorMap.get(Messages.SS_HELIX));
		nodeColorMapping.putMapValue(Messages.SS_HELIX_ALT, colorMap.get(Messages.SS_HELIX));
		nodeColorMapping.putMapValue(Messages.SS_SHEET, colorMap.get(Messages.SS_SHEET));
		nodeColorMapping.putMapValue(Messages.SS_SHEET_ALT1, colorMap.get(Messages.SS_SHEET));
		nodeColorMapping.putMapValue(Messages.SS_SHEET_ALT2, colorMap.get(Messages.SS_SHEET));
		nodeColorMapping.putMapValue(Messages.SS_LOOP, colorMap.get(Messages.SS_LOOP));
		nodeColorMapping.putMapValue(Messages.SS_LOOP_ALT1, colorMap.get(Messages.SS_LOOP));
		nodeColorMapping.putMapValue(Messages.SS_LOOP_ALT2, colorMap.get(Messages.SS_LOOP));
		nodeColorMapping.putMapValue(Messages.SS_DEFAULT_ALT1, colorMap.get(Messages.SS_DEFAULT));
		nodeColorMapping.putMapValue(Messages.SS_DEFAULT_ALT2, colorMap.get(Messages.SS_DEFAULT));
		vs.addVisualMappingFunction(nodeColorMapping);
	}

	/**
	 * Change the node label mapping in the visual style <code>aVisualStyle</code>. Use the labels
	 * stored in the node attribute <code>attrName</code>.
	 * 
	 * @param aVisualStyle
	 *            Visual style of the target network.
	 * @param attrName
	 *            Name of the attribute storing the new node label.
	 */
	public void changeNodeLabel(VisualStyle vs, String attrName) {
		VisualMappingFunctionFactory vmfFactoryP = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class,
						"(mapping.type=passthrough)");
		PassthroughMapping<String, String> labelMapping = (PassthroughMapping<String, String>) vmfFactoryP
				.createVisualMappingFunction(attrName, String.class, BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(labelMapping);
	}

	public void hideDistEdges(CyNetwork network, CyNetworkView view) {
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
		if (index2node == null || index2node.size() == 0) {
			return;
		}
		for (final String index1 : index2node.keySet()) {
			for (final String index2 : index2node.keySet()) {
				if (!index1.equals(index2) && index1.startsWith(index2.substring(0, 1))) {
					List<CyEdge> edges = network.getConnectingEdgeList(index2node.get(index1),
							index2node.get(index2), Type.ANY);
					for (CyEdge edge : edges) {
						view.getEdgeView(edge).clearValueLock(BasicVisualLexicon.EDGE_VISIBLE);
						boolean edgeVis = true;
						try {
							int val1 = Integer.valueOf(index1.split(":")[1]).intValue();
							int val2 = Integer.valueOf(index2.split(":")[1]).intValue();
							if (Math.abs(val1 - val2) < getEdgeDistFilter(network)) {
								edgeVis = false;
							}
						} catch (Exception ex) {
							// ignore
						}
						view.getEdgeView(edge).setLockedValue(BasicVisualLexicon.EDGE_VISIBLE,
								edgeVis);
					}
				}
			}
		}
	}

	/**
	 * Map of {@link BackboneHandler}s. Each network has its own backbone handler.
	 */
	private Map<CyNetwork, BackboneHandler> backboneHandlers;

	/**
	 * Map of color mappings. Each network has its own color mapping.
	 */
	private Map<CyNetwork, Map<String, Color>> colorMaps;

	/**
	 * Map of {@link EdgeFilter}s. Each network has its own edge filter for showing/hiding different
	 * edge types.
	 */
	private Map<CyNetwork, EdgeFilter> edgeFilters;

	/**
	 * Map of {@link NodeLabelFilter}s. Each network has its own node label handler.
	 */
	private Map<CyNetwork, NodeLabelFilter> labelFilters;

	/**
	 * Map of edge constants. Each network has its own size constants array: [bb edge width, edge
	 * width, edge space, node label, node size]
	 */
	private Map<CyNetwork, int[]> sizeConstants;

	/**
	 * Map of visual styles. Each network has its own visual style.
	 */
	private Map<CyNetwork, VisualStyle> visualStyles;

	/**
	 * List of networks that have been processed.
	 */
	private Set<CyNetwork> networks;

	/**
	 * Bundle context containing all services.
	 */
	private CyServiceRegistrar context;

	/**
	 * Dialog with all properties.
	 */
	private VisualPropsDialog dialog;
}
