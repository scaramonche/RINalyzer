package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities;

// TODO: [Bug] create attribute SV_RINRESIDUE

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskManager;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops.RINVisualPropertiesManager;

public class RINComparator {

	private CyServiceRegistrar context;

	private RINVisualPropertiesManager rinVisPropManager;

	private String compNetName;

	private CyNetwork firstNetwork;

	private CyNetwork secondNetwork;

	private String firstNetNameAttr;

	private String secondNetNameAttr;

	private String firstNetWeightAttr;

	private String secondNetWeightAttr;

	private String weightTransf;

	private Map<CyNode, CyNode> nodeMapping;

	private Map<CyNode, CyNode> firstCompMapping = null;

	private Map<CyNode, CyNode> secondCompMapping = null;

	private CyNetwork compNet = null;

	public RINComparator(CyServiceRegistrar aContext, RINVisualPropertiesManager rinVisPropManager,
			String aCompNetName, CyNetwork aFirstNet, String aFirstNetNameAttr,
			String aFirstNetWeightAttr, CyNetwork aSecondNet, String aSecondNetNameAttr,
			String aSecondNetWeightAttr, String aWeightTransf, Map<CyNode, CyNode> aNodeMapping) {
		context = aContext;
		this.rinVisPropManager = rinVisPropManager;
		compNetName = aCompNetName;
		firstNetwork = aFirstNet;
		firstNetNameAttr = aFirstNetNameAttr;
		firstNetWeightAttr = aFirstNetWeightAttr;
		secondNetwork = aSecondNet;
		secondNetNameAttr = aSecondNetNameAttr;
		secondNetWeightAttr = aSecondNetWeightAttr;
		weightTransf = aWeightTransf;
		nodeMapping = aNodeMapping;
	}

	/**
	 * Compare two networks and create a combined network with all nodes and edges of both networks
	 * and an appropriate visualization of the differences and similarities between the two
	 * networks.
	 * 
	 * @param mappingFiles
	 *            Set of files that contain the mapping.
	 * @param nameAttr
	 *            Attribute containing the RINalyzer node ids.
	 */
	public void compare() {
		// create new network
		CyNetworkFactory netFactory = (CyNetworkFactory) CyUtils.getService(context,
				CyNetworkFactory.class);
		compNet = netFactory.createNetwork();
		compNet.getRow(compNet).set(CyNetwork.NAME, compNetName);

		// create attribute columns
		initializeAttributes();

		// add nodes
		transferNodes();

		// add all edges
		transferNetEdges(firstNetwork, firstCompMapping, Messages.NET1, firstNetWeightAttr);
		transferNetEdges(secondNetwork, secondCompMapping, Messages.NET2, secondNetWeightAttr);

		// count changes
		countEdges();

		// register the network and apply the visual style
		finalizeNetwork();
	}

	/**
	 * Create a visual style for the combined network by copying the default visual style and adding
	 * a discrete edge line style mapper which represents edges in both networks to solid lines, and
	 * other edges to dashed lines.
	 * 
	 * @param newNet
	 *            New network combining the networks to be compared.
	 */
	private void finalizeNetwork() {
		// get factories and managers
		CyNetworkManager cyNetworkManager = (CyNetworkManager) CyUtils.getService(context,
				CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactory = (CyNetworkViewFactory) CyUtils.getService(
				context, CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManager = (CyNetworkViewManager) CyUtils.getService(
				context, CyNetworkViewManager.class);
		ApplyPreferredLayoutTaskFactory layoutTaskFactory = (ApplyPreferredLayoutTaskFactory) CyUtils
				.getService(context, ApplyPreferredLayoutTaskFactory.class);
		TaskManager<?, ?> taskManager = (TaskManager<?, ?>) CyUtils.getService(context,
				TaskManager.class);
		VisualMappingManager cyVmManager = (VisualMappingManager) CyUtils.getService(context,
				VisualMappingManager.class);
		VisualStyleFactory cyVsFactory = (VisualStyleFactory) CyUtils.getService(context,
				VisualStyleFactory.class);

		// register network
		cyNetworkManager.addNetwork(compNet);

		// Create network view
		CyNetworkView compView = cyNetworkViewFactory.createNetworkView(compNet);
		cyNetworkViewManager.addNetworkView(compView);
		Set<CyNetworkView> views = new HashSet<CyNetworkView>();
		views.add(compView);

		// add to the vis props manager
		if (!rinVisPropManager.hasNetwork(compNet)) {
			rinVisPropManager.addNetwork(compNet, compView);
		}

		// Do a layout
		// CyLayoutAlgorithm layout = cyLayoutManager.getDefaultLayout();
		// insertTasksAfterCurrentTask(layout.createTaskIterator(rinView,
		// layout.getDefaultLayoutContext(), layout.ALL_NODE_VIEWS, null));
		taskManager.execute(layoutTaskFactory.createTaskIterator(views));

		// Set vizmap
		VisualStyle compStyle = null;
		VisualStyle compStyle2 = null;
		for (VisualStyle vs : cyVmManager.getAllVisualStyles()) {
			if (vs.getTitle().equals(Messages.COMPVISSTYLE)) {
				compStyle = vs;
			} else if (vs.getTitle().equals(Messages.COMPVISSTYLERIN)) {
				compStyle2 = vs;
			}
		}
		if (compStyle == null) {
			compStyle = cyVsFactory.createVisualStyle(cyVmManager.getDefaultVisualStyle());
			compStyle.setTitle(Messages.COMPVISSTYLE);
			setMappings(compStyle);
			cyVmManager.addVisualStyle(compStyle);
		}
		if (compStyle2 == null) {
			compStyle2 = cyVsFactory.createVisualStyle(cyVmManager.getDefaultVisualStyle());
			compStyle2.setTitle(Messages.COMPVISSTYLERIN);
			setMappingsAlt(compStyle2);
			cyVmManager.addVisualStyle(compStyle2);
		}
		cyVmManager.setVisualStyle(compStyle, compView);
		compStyle.apply(compView);
		// compView.updateView();
	}

	private void setMappings(VisualStyle vs) {
		// get factories
		VisualMappingFunctionFactory vmfFactoryD = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class,
						"(mapping.type=passthrough)");

		if (vmfFactoryD != null && vmfFactoryP != null) {
			// Discrete mapping for edge line style
			DiscreteMapping<String, LineType> lineStyleMapping = (DiscreteMapping<String, LineType>) vmfFactoryD
					.createVisualMappingFunction(Messages.EDGE_BELONGSTO, String.class,
							BasicVisualLexicon.EDGE_LINE_TYPE);
			lineStyleMapping.putMapValue(Messages.NET1, LineTypeVisualProperty.LONG_DASH);
			lineStyleMapping.putMapValue(Messages.NET2, LineTypeVisualProperty.DOT);
			lineStyleMapping.putMapValue(Messages.BOTH, LineTypeVisualProperty.SOLID);
			vs.addVisualMappingFunction(lineStyleMapping);

			// Discrete mapping for edge color
			DiscreteMapping<String, Paint> lineColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
					.createVisualMappingFunction(Messages.EDGE_BELONGSTO, String.class,
							BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
			lineColorMapping.putMapValue(Messages.NET1, Color.GREEN);
			lineColorMapping.putMapValue(Messages.NET2, Color.RED);
			lineColorMapping.putMapValue(Messages.BOTH, Color.BLACK);
			vs.addVisualMappingFunction(lineColorMapping);

			// Discrete mapping for node border color
			// DiscreteMapping<String, Paint> nodeBorderColorMapping = (DiscreteMapping<String,
			// Paint>) vmfFactoryD
			// .createVisualMappingFunction(Messages.NODE_BELONGSTO, String.class,
			// BasicVisualLexicon.NODE_BORDER_PAINT);
			// nodeBorderColorMapping.putMapValue(Messages.NET1,
			// vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT));
			// nodeBorderColorMapping.putMapValue(Messages.NET2,
			// vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT));
			// nodeBorderColorMapping.putMapValue(Messages.BOTH, Color.ORANGE);
			// vs.addVisualMappingFunction(nodeBorderColorMapping);
			//
			// // Discrete mapping for node border width
			// DiscreteMapping<String, Double> nodeBorderWidthMapping = (DiscreteMapping<String,
			// Double>) vmfFactoryD
			// .createVisualMappingFunction(Messages.NODE_BELONGSTO, String.class,
			// BasicVisualLexicon.NODE_BORDER_WIDTH);
			// nodeBorderWidthMapping.putMapValue(Messages.NET1,
			// vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH));
			// nodeBorderWidthMapping.putMapValue(Messages.NET2,
			// vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH));
			// nodeBorderWidthMapping.putMapValue(Messages.BOTH, new Double(3.0));
			// vs.addVisualMappingFunction(nodeBorderWidthMapping);

			// Discrete mapping for node color
			DiscreteMapping<String, Paint> nodeColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
					.createVisualMappingFunction(Messages.NODE_BELONGSTO, String.class,
							BasicVisualLexicon.NODE_FILL_COLOR);
			nodeColorMapping.putMapValue(Messages.NET1, Color.GREEN);
			nodeColorMapping.putMapValue(Messages.NET2, Color.RED);
			nodeColorMapping.putMapValue(Messages.BOTH, Color.LIGHT_GRAY);
			vs.addVisualMappingFunction(nodeColorMapping);

			// Passthrough mapping for node label
			PassthroughMapping<String, String> labelMapping = (PassthroughMapping<String, String>) vmfFactoryP
					.createVisualMappingFunction(Messages.NODE_COMBILABEL, String.class,
							BasicVisualLexicon.NODE_LABEL);
			vs.addVisualMappingFunction(labelMapping);

			// Background color
			vs.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.WHITE);

			// Node color
			vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.LIGHT_GRAY);
		}
	}

	private void setMappingsAlt(VisualStyle vs) {
		// get factories
		VisualMappingFunctionFactory vmfFactoryD = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = (VisualMappingFunctionFactory) CyUtils
				.getService(context, VisualMappingFunctionFactory.class,
						"(mapping.type=passthrough)");

		if (vmfFactoryD != null && vmfFactoryP != null) {
			// Discrete mapping for edge line style
			DiscreteMapping<String, LineType> lineStyleMapping = (DiscreteMapping<String, LineType>) vmfFactoryD
					.createVisualMappingFunction(Messages.EDGE_BELONGSTO, String.class,
							BasicVisualLexicon.EDGE_LINE_TYPE);
			lineStyleMapping.putMapValue(Messages.NET1, LineTypeVisualProperty.LONG_DASH);
			lineStyleMapping.putMapValue(Messages.NET2, LineTypeVisualProperty.DOT);
			lineStyleMapping.putMapValue(Messages.BOTH, LineTypeVisualProperty.SOLID);
			vs.addVisualMappingFunction(lineStyleMapping);

			// Discrete mapping for edge color
			DiscreteMapping<String, Paint> lineColorMapping = null;
			if (compNet.getDefaultEdgeTable().getColumn(Messages.SV_INTSUBTYPE) != null) {
				lineColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
						.createVisualMappingFunction(Messages.SV_INTSUBTYPE, String.class,
								BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
			} else {
				lineColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
						.createVisualMappingFunction(CyEdge.INTERACTION, String.class,
								BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
			}

			for (final String entry : rinVisPropManager.getEdgeTypes(compNet)) {
				lineColorMapping.putMapValue(entry,
						rinVisPropManager.getColorMap(compNet).get(entry));
			}
			vs.addVisualMappingFunction(lineColorMapping);

			// Discrete mapping for node border color
			DiscreteMapping<String, Paint> nodeBorderColorMapping = (DiscreteMapping<String, Paint>) vmfFactoryD
					.createVisualMappingFunction(Messages.NODE_BELONGSTO, String.class,
							BasicVisualLexicon.NODE_BORDER_PAINT);
			nodeBorderColorMapping.putMapValue(Messages.NET1, Color.GREEN);
			nodeBorderColorMapping.putMapValue(Messages.NET2, Color.RED);
			// nodeBorderColorMapping.putMapValue(Messages.BOTH, Color.GRAY);
			vs.addVisualMappingFunction(nodeBorderColorMapping);

			// Discrete mapping for node border width
			DiscreteMapping<String, Double> nodeBorderWidthMapping = (DiscreteMapping<String, Double>) vmfFactoryD
					.createVisualMappingFunction(Messages.NODE_BELONGSTO, String.class,
							BasicVisualLexicon.NODE_BORDER_WIDTH);
			nodeBorderWidthMapping.putMapValue(Messages.NET1,
					vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH) + 2);
			nodeBorderWidthMapping.putMapValue(Messages.NET2,
					vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH) + 2);
			nodeBorderWidthMapping.putMapValue(Messages.BOTH,
					vs.getDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH));
			vs.addVisualMappingFunction(nodeBorderWidthMapping);

			PassthroughMapping<String, String> labelMapping = (PassthroughMapping<String, String>) vmfFactoryP
					.createVisualMappingFunction(Messages.NODE_COMBILABEL, String.class,
							BasicVisualLexicon.NODE_LABEL);
			vs.addVisualMappingFunction(labelMapping);

			// Background color
			vs.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.WHITE);

			// Node color
			vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.LIGHT_GRAY);
		}
	}

	private void transferNodes() {
		// map of first network nodes and comparison network nodes
		firstCompMapping = new HashMap<CyNode, CyNode>();
		// map of second network nodes and comparison network nodes
		secondCompMapping = new HashMap<CyNode, CyNode>();
		// nodes in the second network
		List<CyNode> nodes2 = secondNetwork.getNodeList();
		// get default node tables
		CyTable firstNetTable = firstNetwork.getDefaultNodeTable();
		CyTable secondNetTable = secondNetwork.getDefaultNodeTable();
		CyTable compNetTable = compNet.getDefaultNodeTable();

		// 1. add all nodes from the first network to the comparison network
		for (CyNode node1 : firstNetwork.getNodeList()) {
			// create new node
			final CyNode newNode = compNet.addNode();
			firstCompMapping.put(node1, newNode);
			// save original name from first network
			String name1 = CyUtils.getCyName(firstNetwork, node1, firstNetNameAttr);
			compNet.getRow(newNode).set(Messages.NODE_ORIGINALNET1, name1);
			// start creating name for the new node
			String newNodeName = name1;
			String newNodeLabel = name1;
			// get PDB identifiers associated with this node
			List<String> compNetPDBAttr = getPDBAttrValues(firstNetTable, node1);
			// 2. check if nodes in the first network can be mapped to nodes in the second network
			// if so, create a combined node
			if (nodeMapping.containsKey(node1)) {
				// new node belongs to both networks
				final CyNode node2 = nodeMapping.get(node1);
				nodes2.remove(node2);
				secondCompMapping.put(node2, newNode);
				// save original name from second network
				String name2 = CyUtils.getCyName(secondNetwork, node2, secondNetNameAttr);
				newNodeName = name1 + Messages.COMPDELIMITER1 + name2;
				compNet.getRow(newNode).set(Messages.NODE_ORIGINALNET2, name2);
				// get PDB identifiers associated with this node
				compNetPDBAttr.addAll(getPDBAttrValues(secondNetTable, node2));
				// set node belongs to
				compNet.getRow(newNode).set(Messages.NODE_BELONGSTO, Messages.BOTH);
				// get substitution score
				getSubstScore(name1, name2, newNode);
				// create new node name consisting of both
				if (name1.split(":").length == 5
						&& name1.substring(name1.indexOf(":")).equals(
								name2.substring(name2.indexOf(":")))) {
					newNodeLabel = name1.substring(5);
					// getSubstScore(name1, name2, newNode);
				} else if (name1.split(":").length == 4 && name1.equals(name2)) {
					newNodeLabel = name1;
					// getSubstScore(name1, name2, newNode);
				} else {
					newNodeLabel = name1 + Messages.COMPDELIMITER2 + name2;
				}
			} else {
				// new node belongs to first network only
				compNet.getRow(newNode).set(Messages.NODE_BELONGSTO, Messages.NET1);
			}
			// save accumulated PDB identifiers as list
			compNetTable.getRow(newNode.getSUID())
					.set(Messages.defaultStructureKey, compNetPDBAttr);
			compNet.getRow(newNode).set(CyNetwork.NAME, newNodeName);
			compNet.getRow(newNode).set(Messages.NODE_COMBILABEL, newNodeLabel);
		}
		// add all remaining nodes from the second network
		for (CyNode node2 : nodes2) {
			final CyNode newNode = compNet.addNode();
			final String name2 = CyUtils.getCyName(secondNetwork, node2, secondNetNameAttr);
			compNet.getRow(newNode).set(Messages.NODE_BELONGSTO, Messages.NET2);
			compNet.getRow(newNode).set(CyNetwork.NAME, name2);
			compNet.getRow(newNode).set(Messages.NODE_COMBILABEL, name2);
			compNet.getRow(newNode).set(Messages.NODE_ORIGINALNET2, name2);
			compNetTable.getRow(newNode.getSUID()).set(Messages.defaultStructureKey,
					getPDBAttrValues(secondNetTable, node2));
			secondCompMapping.put(node2, newNode);
		}
	}

	/**
	 * Transfer the edges of the first network to the combined network.
	 * 
	 * @param newEdges
	 *            Container for newly created edges.
	 * @param net
	 *            First network of the alignment and for the comparison.
	 * @param nodes
	 *            Nodes contained only in the first network but not in the combined network.
	 * @param mapping
	 *            Mapping of nodes with keys being the nodes of the first network.
	 * @param internNetID
	 *            An intern identifier of the first network. Also used for the edge attribute.
	 */
	private void transferNetEdges(CyNetwork net, Map<CyNode, CyNode> mapping, String internNetID,
			String weightAtt) {
		// iterate over all edges in the original network
		for (CyEdge edge : net.getEdgeList()) {
			final CyNode newSource = mapping.get(edge.getSource());
			final CyNode newTarget = mapping.get(edge.getTarget());
			// TODO: [Improve][Comparison] Choose edge attribute for the interaction?
			// get interaction type to keep track of multiple edges
			final String iType = net.getRow(edge).get(CyEdge.INTERACTION, String.class);
			final List<CyEdge> compEdges = compNet.getConnectingEdgeList(newSource, newTarget,
					Type.ANY);
			CyEdge newEdge = null;
			// iterate over all edges between source and target in the comparison network
			if (compEdges.size() > 0) {
				for (CyEdge compEdge : compEdges) {
					if (iType
							.equals(compNet.getRow(compEdge).get(CyEdge.INTERACTION, String.class))) {
						newEdge = compEdge;
						// set belongsto attribute
						String belongsTo = compNet.getRow(compEdge).get(Messages.EDGE_BELONGSTO,
								String.class);
						if (!belongsTo.equals("") && !belongsTo.contains(internNetID)) {
							belongsTo += "," + internNetID;
						}
						compNet.getRow(newEdge).set(Messages.EDGE_BELONGSTO, belongsTo);
						setCompEdgeWeight(newEdge, net, edge, weightAtt);
					}
				}
			}
			if (newEdge == null) {
				// create edge and set attribute
				newEdge = compNet.addEdge(newSource, newTarget, true);
				compNet.getRow(newEdge).set(
						CyNetwork.NAME,
						CyUtils.getCyName(compNet, newSource) + " " + iType + " "
								+ CyUtils.getCyName(compNet, newTarget));
				compNet.getRow(newEdge).set(CyEdge.INTERACTION, iType);
				compNet.getRow(newEdge).set(Messages.EDGE_BELONGSTO, internNetID);
				setOrigEdgeWeight(newEdge, net, edge, internNetID, weightAtt);
			}
		}

	}

	private void countEdges() {
		for (CyNode source : compNet.getNodeList()) {
			List<CyEdge> edges = compNet.getAdjacentEdgeList(source, Type.ANY);
			double count1 = 0.0;
			double count2 = 0.0;
			double count12 = 0.0;
			// System.out.println(CyUtils.getCyName(compNet, source));
			for (CyEdge edge : edges) {
				String belongsTo = compNet.getRow(edge).get(Messages.NODE_BELONGSTO, String.class)
						.trim();
				if (belongsTo.equals(Messages.NET1)) {
					count1 += 1;
				} else if (belongsTo.equals(Messages.NET2)) {
					count2 += 1;
				} else if (belongsTo.equals(Messages.BOTH)) {
					count12 += 1;
				}
				// System.out.println(belongsTo + "\t" + count1 + "\t" + count2 + "\t" + count12);
			}
			compNet.getRow(source).set(Messages.NODE_NUMEDGES1, count1 / edges.size());
			compNet.getRow(source).set(Messages.NODE_NUMEDGES2, count2 / edges.size());
			compNet.getRow(source).set(Messages.NODE_NUMEDGES12, count12 / edges.size());
		}
	}

	private void initializeAttributes() {
		// node attributes
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_COMBILABEL, String.class, false,
				"");
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_ORIGINALNET1, String.class, false,
				"");
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_ORIGINALNET2, String.class, false,
				"");
		compNet.getDefaultNodeTable()
				.createColumn(Messages.NODE_BELONGSTO, String.class, false, "");
		compNet.getDefaultNodeTable().createListColumn(Messages.defaultStructureKey, String.class,
				false);
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_NUMEDGES1, Double.class, false);
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_NUMEDGES2, Double.class, false);
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_NUMEDGES12, Double.class, false);
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_SUBSTITUTION, String.class, false,
				"");
		compNet.getDefaultNodeTable().createColumn(Messages.NODE_SUBSTSCORE, Double.class, false);
		// edge attributes
		compNet.getDefaultEdgeTable()
				.createColumn(Messages.EDGE_BELONGSTO, String.class, false, "");
		compNet.getDefaultEdgeTable().createColumn(Messages.EDGE_COMPWEIGHT, Double.class, false);
		compNet.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS).createColumn(
				Messages.EDGE_TMPWEIGHT, Double.class, false);
	}

	private List<String> getPDBAttrValues(CyTable table, CyNode node) {
		List<String> compNetPDBAttr = new ArrayList<String>();
		for (String columnName : Arrays.asList(Messages.defaultStructureKeys)) {
			if (table.getColumn(columnName) != null) {
				Class<?> colType = table.getColumn(columnName).getType();
				if (colType == String.class) {
					compNetPDBAttr.add(table.getRow(node.getSUID()).get(columnName, String.class));
				} else if (colType == List.class) {
					compNetPDBAttr.addAll(table.getRow(node.getSUID()).getList(columnName,
							String.class));
				}
			}
		}
		return compNetPDBAttr;
	}

	private void getSubstScore(String name1, String name2, CyNode newNode) {
		// System.out.println("get subst score: " + name1 + " " + name2);
		String name1Type = name1.substring(name1.length() - 3, name1.length());
		String name2Type = name2.substring(name2.length() - 3, name2.length());
		String resNumber = "|";
		if (name1.split(":").length == 5) {
			resNumber = name1.split(":")[2];
		} else if (name1.split(":").length == 4) {
			resNumber = name1.split(":")[1];
		}
		// only do if different type of AA
		if (!name1Type.equals(name2Type)) {
			if (Messages.aaNames.containsKey(name1Type) && Messages.aaNames.containsKey(name2Type)) {
				final String aa1 = Messages.aaNames.get(name1Type).substring(0, 1);
				final String aa2 = Messages.aaNames.get(name2Type).substring(0, 1);
				compNet.getRow(newNode).set(Messages.NODE_SUBSTITUTION, aa1 + resNumber + aa2);
				final int aa1ind = Messages.blosum62AA.indexOf(aa1);
				final int aa2ind = Messages.blosum62AA.indexOf(aa2);
				if (aa1ind > -1 && aa2ind > -1) {
					compNet.getRow(newNode).set(Messages.NODE_SUBSTSCORE,
							Messages.blosum62[aa1ind][aa2ind]);
				}
				// TODO: [Improve][Comparison] Handle special cases
				// else {
				// compNet.getRow(newNode).set(Messages.NODE_SUBSTSCORE, -4);
				// }
			}
		}
	}

	private void setCompEdgeWeight(CyEdge newEdge, CyNetwork net, CyEdge edge, String weightAtt) {
		// set weight
		if (weightAtt == null) {
			return;
		}
		Double tmpWeight = compNet.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS)
				.getRow(newEdge.getSUID()).get(Messages.EDGE_TMPWEIGHT, Double.class);
		// Set missing weights to 0.0
		Double edgeWeight = net.getRow(edge).get(weightAtt, Double.class);
		if (edgeWeight == null) {
			edgeWeight = 0.0;
		}
		if (tmpWeight != null && tmpWeight != Double.NaN && edgeWeight != null
				&& edgeWeight != Double.NaN) {
			Double newWeight = null;
			if (weightTransf.equals("difference")) {
				newWeight = tmpWeight.doubleValue() - edgeWeight.doubleValue();
			} else if (weightTransf.equals("ratio")) {
				newWeight = tmpWeight.doubleValue() / edgeWeight.doubleValue();
			} else if (weightTransf.equals("log ratio")) {
				newWeight = Math.log10(tmpWeight.doubleValue() / edgeWeight.doubleValue());
			}
			if (newWeight != null) {
				compNet.getRow(newEdge).set(Messages.EDGE_COMPWEIGHT, newWeight);
			}
		}
	}

	private void setOrigEdgeWeight(CyEdge newEdge, CyNetwork net, CyEdge edge, String internNetID,
			String weightAtt) {
		if (weightAtt == null) {
			return;
		}
		// Set missing weights to 0.0
		Double weight = net.getRow(edge).get(weightAtt, Double.class);
		if (weight == null) {
			return;
		}
		compNet.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS).getRow(newEdge.getSUID())
				.set(Messages.EDGE_TMPWEIGHT, weight);
		if (internNetID.equals("net1")) {
			if (weightTransf.equals("difference")) {
				compNet.getRow(newEdge).set(Messages.EDGE_COMPWEIGHT, weight.doubleValue());
			}
			// else if (weightTransf.equals("ratio")) {
			// compNet.getRow(newEdge).set(Messages.EDGE_COMPWEIGHT, 100 + weight.doubleValue());
			// } else if (weightTransf.equals("log ratio")) {
			// compNet.getRow(newEdge).set(Messages.EDGE_COMPWEIGHT, 100 + weight.doubleValue());
			// }
		} else {
			if (weightTransf.equals("difference")) {
				compNet.getRow(newEdge).set(Messages.EDGE_COMPWEIGHT, -weight.doubleValue());
			}
			// else if (weightTransf.equals("ratio")) {
			// compNet.getRow(newEdge).set(Messages.EDGE_COMPWEIGHT, -100 - weight.doubleValue());
			// } else if (weightTransf.equals("log ratio")) {
			// compNet.getRow(newEdge).set(Messages.EDGE_COMPWEIGHT, -100 - weight.doubleValue());
			// }
		}
	}

}
