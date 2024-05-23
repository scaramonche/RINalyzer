package de.mpg.mpi_inf.bioinf.rinalyzer2.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.function.tdouble.DoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ConnComp;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.CurrentFlowCentrality;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.IntPair;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.RandomWalkCentrality;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ResultData;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ShortestPathCentrality;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.AnalysisDialog;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.ResultsPanel;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

public class AnalyzeTask extends AbstractTask {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.AnalyzeTask.class);

	/**
	 * Flag indicating if batch analysis is performed.
	 */
	// private boolean batch;

	private CyServiceRegistrar context;

	private AnalysisDialog dialog;

	/**
	 * Map matching each node with an index for intern representation.
	 */
	private Map<CyNode, Integer> node2index;

	/**
	 * {@link CyNetwork} that has been analyzed.
	 */
	private CyNetwork network;

	/**
	 * Connected components of selected set.
	 */
	private ConnComp netConnComp;

	/**
	 * Set of nodes selected for the analysis.
	 */
	private Set<CyNode> selectedNodes;

	/**
	 * Data containing all computed values of the centrality measures.
	 */
	private ResultData resultData;

	public AnalyzeTask(CyServiceRegistrar bc, CyNetworkView aNetView) {
		context = bc;
		network = aNetView.getModel();
		dialog = null;
		selectedNodes = null;
		node2index = null;
		resultData = null;
		netConnComp = null;
		// batch = false;
		init();
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(Messages.TM_ANALYZENET);
		taskMonitor.setStatusMessage(Messages.TM_INITCOMPUT);
		// Perform analysis
		if (dialog == null || dialog.isCanceled()) {
			return;
		}
		// initialize variables for analysis
		final boolean[] measuresToCompute = dialog.getMeasuresToCompute();
		int n = network.getNodeCount();
		int p = (n) * (n - 1);
		int s = selectedNodes.size();
		float maxprogress = (n + s + s + p) + ((n > p ? n : p) + s) + (p + n);
		int progress = 0;
		taskMonitor.setProgress(progress);
		resultData = new ResultData();
		resultData.setNetworkTitle(network.getRow(network).get(CyNetwork.NAME, String.class));
		resultData.setNetwork(network);
		resultData.setSelectedCyNodes(selectedNodes);
		resultData.setAnalysisOptions(network.getRow(network).get(CyNetwork.NAME, String.class),
				measuresToCompute, dialog.getWeightOptions(), dialog.getDefWeight(),
				dialog.getDegreeCutoff(), dialog.useConnComp());
		try {
			// Compute centralities
			final DoubleMatrix2D adjMatrix = initWeights(network, n, dialog.getWeightOptions(),
					dialog.getDefWeight());
			Set<IntPair> pairs = null;
			if (dialog.useConnComp()) {
				pairs = netConnComp.getPairsMap(node2index);
				p = netConnComp.getPairsNumber();
			}
			if (cancelled) {
				return;
			}
			if (measuresToCompute[1] || measuresToCompute[2]) {
				DoubleMatrix2D invLapl = getInverseLaplacian2(adjMatrix);
				if (invLapl != null) {
					if (measuresToCompute[1]) {
						// Current flow centrality
						CurrentFlowCentrality cfc = new CurrentFlowCentrality(node2index,
								selectedNodes, network, invLapl);

						taskMonitor.setStatusMessage(Messages.LOG_COMPCFCLOS);
						resultData.setCentralty(Messages.CENT_CFC, cfc.closenessCentrality());
						progress += (n > p ? n : p);
						taskMonitor.setProgress(maxprogress / progress);
						if (cancelled) {
							return;
						}

						taskMonitor.setStatusMessage(Messages.LOG_COMPCFBETW);
						resultData.setCentralty(Messages.CENT_CFB,
								cfc.betweennessCentrality(pairs, adjMatrix));
						progress += s;
						taskMonitor.setProgress(maxprogress / progress);
						if (cancelled) {
							return;
						}
						// System.gc();
					} else {
						// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPCF_NOT);
						progress += (n > p ? n : p) + s;
						taskMonitor.setProgress(maxprogress / progress);
					}
					if (measuresToCompute[2]) {
						// Random walk centrality
						RandomWalkCentrality rwc = new RandomWalkCentrality(node2index, adjMatrix,
								selectedNodes, network, invLapl);

						taskMonitor.setStatusMessage(Messages.LOG_COMPRWRCLOS);
						resultData.setCentralty(Messages.CENT_RWRC,
								rwc.receiverClosenessCentrality());
						progress += n;
						taskMonitor.setProgress(maxprogress / progress);
						if (cancelled) {
							return;
						}

						taskMonitor.setStatusMessage(Messages.LOG_COMPRWBETW);
						resultData
								.setCentralty(Messages.CENT_RWB, rwc.betweennessCentrality(pairs));
						progress += p;
						taskMonitor.setProgress(maxprogress / progress);
						if (cancelled) {
							return;
						}
						// System.gc();
					} else {
						progress += n + p;
						taskMonitor.setProgress(maxprogress / progress);
						// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPRW_NOT);
					}
				}
			} else {
				progress += (n > p ? n : p) + s + n + p;
				taskMonitor.setProgress(progress);
				// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPCF_NOT);
				// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPRW_NOT);
			}

			// Compute shortest path centralities
			if (measuresToCompute[0]) {
				ShortestPathCentrality spc = new ShortestPathCentrality(node2index, selectedNodes,
						network);
				// FileWriter file = new FileWriter(new File("weights.txt"));
				// file.write(adjMatrix.toString());
				// file.close();
				modifyWeights(adjMatrix, dialog.getWeightOptions());
				// file = new FileWriter(new File("weights_mod.txt"));
				// file.write(adjMatrix.toString());
				// file.close();

				taskMonitor.setStatusMessage(Messages.LOG_COMPSPDEG);
				resultData.setCentralty(Messages.CENT_SPD,
						spc.degreeCentrality(adjMatrix, dialog.getDegreeCutoff()));
				progress += n;
				taskMonitor.setProgress(maxprogress / progress);
				if (cancelled) {
					return;
				}

				taskMonitor.setStatusMessage(Messages.LOG_COMPSPBETW);
				resultData.setCentralty(Messages.CENT_SPB,
						spc.betweennessCentrality(pairs, adjMatrix));
				progress += p;
				taskMonitor.setProgress(maxprogress / progress);
				if (cancelled) {
					return;
				}

				taskMonitor.setStatusMessage(Messages.LOG_COMPSPCLOS);
				resultData.setCentralty(Messages.CENT_SPC, spc.closenessCentrality());
				progress += s + s;
				taskMonitor.setProgress(maxprogress / progress);
				// System.gc();
			} else {
				progress += n + s + s + p;
				taskMonitor.setProgress(maxprogress / progress);
				// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPSP_NOT);
			}
		} catch (Exception e) {
			// logger.error(Messages.SM_ERRORCOMP, e);
			// taskMonitor.setStatusMessage(Messages.SM_ERRORCOMP);
			throw new Exception(Messages.SM_ERRORCOMP);
		}

		taskMonitor.setStatusMessage(Messages.TM_COMPUTATIONSUCC);
		// Analysis finished successfully, show results
		taskMonitor.setStatusMessage(Messages.TM_DISPLAYRESULTS);
		displayResults();
		// TODO: [Improve][Batch analysis] Enable support for batch analysis
		// If batch analysis, save results and destroy the network.
		// if (batch) {
		// saveResults(resultData, new File("all.centstats"));
		// TODO: [Improve][Batch analysis] Implement unload network
		// Utils.unloadNetwork(centralityAnalyzer.getNetwork());
		// } else {
		// }
	}

	@ProvidesTitle
	public String getTitle() {
		return "Analyze Network Options";
	}

	/**
	 * Creates a panel in the Cytoscape's Results panel and visualizes the computed data. It
	 * initializes the <code>ResultsPanel</code> the first time it is called.
	 */
	private void displayResults() {
		// don't show results if no centralities have been computed.
		if (resultData.getComputed().size() == 0) {
			// Nothing has been computed
			JOptionPane.showMessageDialog(CyUtils.getCyFrame(context), Messages.SM_NOTHINGCOMP,
					Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
		}
		ResultsPanel panel = new ResultsPanel(context, resultData);
		CyServiceRegistrar registrar = (CyServiceRegistrar) CyUtils.getService(context,
				CyServiceRegistrar.class);
		registrar.registerService(panel, CytoPanelComponent.class, new Properties());
		CytoPanel cytoPanel = ((CySwingApplication) CyUtils.getService(context,
				CySwingApplication.class)).getCytoPanel(CytoPanelName.EAST);
		if (cytoPanel.getState() == CytoPanelState.HIDE) {
			cytoPanel.setState(CytoPanelState.DOCK);
		}
		cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(panel));
	}

	/**
	 * Save all computed centrality values into a file. Invoked when doing batch analysis.
	 */
	// private void saveResults(File resultsFile) {
	// // don't show results if no centralities have been computed.
	// if (resultData.getComputed().size() > 0) {
	// try {
	// ResultsWriter.save(resultData, resultsFile);
	// } catch (IOException e) {
	// JOptionPane.showMessageDialog(CyUtils.getCyFrame(context), Messages.SM_IOERRORSAVE,
	// Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
	// logger.warn(Messages.SM_IOERRORSAVE, e);
	// }
	// return;
	// }
	// }

	/**
	 * Create an adjacency matrix with weights for further computation. The nodes are internally
	 * indexed and these indices are used for the adjacency matrix. The internal indices are stored
	 * in <code>node2index</code> map.
	 * 
	 * The weights are taken from the values of the attribute <code>weightAttr</code> and if there
	 * are multiple edges the user can choose exactly one type of edges to be considered in the
	 * computation ( <code>edgeType</code>), otherwise the weights are summed up. Missing values are
	 * replaced by the default weight value <code>defWeight</code>. Therefore, if no weight
	 * attribute is chosen, the edges are weighted with default weight (e.g. 1) and the network is
	 * actually not weighted.
	 * 
	 * Note: this method also removes the negative weights before the edge weights are summed up.
	 */
	public DoubleMatrix2D initWeights(CyNetwork network, int n, String[] weightOpts,
			double defWeight) {
		// initialize
		node2index = new HashMap<CyNode, Integer>(n);
		DoubleMatrix2D adjMatrix = new SparseDoubleMatrix2D(n, n);
		DoubleMatrix2D edgeCountMatrix = null;
		if (weightOpts[1].equals(Messages.DI_WEIGHTAVE)) {
			edgeCountMatrix = new SparseDoubleMatrix2D(n, n);
		}
		int counter = 0;
		CyTable edgeTable = network.getDefaultEdgeTable();
		// for each node get its adjacent edges
		for (final CyNode node : network.getNodeList()) {
			counter = countCyNode(counter, node);
			final List<CyEdge> adjacentEdges = network.getAdjacentEdgeList(node, Type.ANY);
			for (final CyEdge edge : adjacentEdges) {
				// consider weight either if use multiple edges, i.e if we sum the weights up, or if
				// we have an edge with the interaction type aEdgeType [weightAttr, edgeType,
				// negWeight, simToDist]
				if (weightOpts[1].equals(Messages.DI_WEIGHTAVE)
						|| weightOpts[1].equals(Messages.DI_WEIGHTSUM)
						|| weightOpts[1].equals(Messages.DI_WEIGHTMAX)
						|| weightOpts[1].equals(Messages.DI_WEIGHTMIN)
						|| edgeTable.getRow(edge.getSUID()).get(CyEdge.INTERACTION, String.class)
								.equals(weightOpts[1])) {
					double weight = defWeight;
					// double weight = Math.random()*10;
					if (edgeTable.getRow(edge.getSUID()).isSet(weightOpts[0])) {
						if (edgeTable.getColumn(weightOpts[0]).getType() == Integer.class) {
							weight = edgeTable.getRow(edge.getSUID())
									.get(weightOpts[0], Integer.class).doubleValue();
						} else if (edgeTable.getColumn(weightOpts[0]).getType() == Double.class) {
							weight = edgeTable.getRow(edge.getSUID())
									.get(weightOpts[0], Double.class).doubleValue();
						}
						if (weight < 0.0d) {
							// Take care of negative weights
							if (weightOpts[2].equals(Messages.DI_NEGWEIGHT_IGNORE)) {
								weight = 0.0d;
							} else if (weightOpts[2].equals(Messages.DI_NEGWEIGHT_REVERT)) {
								weight *= -1;
							}

						}
					}
					// TODO: [Improve][Analysis] Distinguish undirected and directed case?
					// so far only the undirected case is considered.
					final CyNode target = edge.getTarget();
					counter = countCyNode(counter, target);
					if (target != node) {
						final int sourceIndex = node2index.get(node).intValue();
						final int targetIndex = node2index.get(target).intValue();
						double old_weight = adjMatrix.get(sourceIndex, targetIndex);
						if (weightOpts[1].equals(Messages.DI_WEIGHTMIN)) {
							if (old_weight != 0.0 && weight > old_weight) {
								weight = old_weight;
							}
						} else if (weightOpts[1].equals(Messages.DI_WEIGHTMAX)) {
							if (weight < old_weight) {
								weight = old_weight;
							}
						} else {
							weight += old_weight;
						}
						adjMatrix.set(sourceIndex, targetIndex, weight);
						adjMatrix.set(targetIndex, sourceIndex, weight);
						if (edgeCountMatrix != null) {
							final double edgeCount = edgeCountMatrix.get(sourceIndex, targetIndex) + 1;
							edgeCountMatrix.set(sourceIndex, targetIndex, edgeCount);
							edgeCountMatrix.set(targetIndex, sourceIndex, edgeCount);
						}
					}
				}
			}
		}
		// compute mean weight
		if (weightOpts[1].equals(Messages.DI_WEIGHTAVE)) {
			adjMatrix.assign(edgeCountMatrix, new DoubleDoubleFunction() {
				public final double apply(double a, double b) {
					return b > 0.0 ? a / b : a;
				}
			});
		}
		return adjMatrix;
	}

	/**
	 * Convert the weight values from similarity to distance scores.
	 * 
	 * @param adjMatrix
	 *            Matrix containing the edge weights.
	 */
	private void modifyWeights(DoubleMatrix2D adjMatrix, String[] weightOpts) {
		if (weightOpts[3].equals(Messages.DI_SIMTODIST1)) {
			adjMatrix.assign(new DoubleFunction() {
				public final double apply(double a) {
					return a > 0.0 ? 1.0 / a : a;
				}
			});
		} else if (weightOpts[3].equals(Messages.DI_SIMTODIST2)) {
			final double max = adjMatrix.aggregate(DoubleFunctions.max, DoubleFunctions.identity) + 1.0;
			adjMatrix.assign(new DoubleFunction() {
				public final double apply(double a) {
					return a > 0.0 ? max - a : a;
				}
			});
		}
	}

	/**
	 * Put the node <code>aCyNode</code> with a successive index in the <code>node2index</code> map,
	 * if it is not already contained in it.
	 * 
	 * @param aCounter
	 *            Current node index.
	 * @param aCyNode
	 *            Current node to be indexed.
	 * @return Next node index.
	 */
	private int countCyNode(int aCounter, CyNode aCyNode) {
		int counter = aCounter;
		if (!node2index.containsKey(aCyNode)) {
			node2index.put(aCyNode, new Integer(aCounter));
			counter++;
		}
		return counter;
	}

	/**
	 * Get the inverse laplacian of a graph using its adjacency matrix A. It's computed as (D-A)^-1,
	 * where D is the degree matrix of the graph represented by A.
	 * 
	 * The Laplacian of the connected graph has (n-1) rank. The inverse is obtained by inverting an
	 * arbitrary (n-1)x(n-1) submatrix and setting the remaining row and column to zero.
	 * 
	 * @return 2d matrix with the inverse laplacian for the graph represented by
	 *         <code>weightMatrix</code>.
	 */
	// TODO: [Improve][Analysis] Is this really the better method?
	private DoubleMatrix2D getInverseLaplacian2(DoubleMatrix2D weightMatrix) {
		// look if its already computed, and if not compute it
		try {
			final int nodeCount = weightMatrix.columns();
			// build degree matrix D
			DoubleMatrix2D degreeMatrix = new SparseDoubleMatrix2D(nodeCount, nodeCount);
			for (int row = 0; row < weightMatrix.rows(); row++) {
				degreeMatrix.set(row, row, weightMatrix.viewRow(row).zSum());
			}
			// build laplacian by subtracting D-A
			degreeMatrix.assign(weightMatrix, DoubleFunctions.minus);
			// invert if not singular
			// TODO: [Release!!!] Change to Property.ZERO.isSingular(degreeMatrix)
			// new DoubleProperty(1.0E-15).isSingular(degreeMatrix)
			if (!DoubleProperty.ZERO.isSingular(degreeMatrix)) {
				DenseDoubleEigenvalueDecomposition evd = new DenseDoubleEigenvalueDecomposition(
						degreeMatrix);
				DoubleMatrix1D d = DoubleFactory2D.sparse.diagonal(evd.getD());
				DoubleMatrix2D v = evd.getV();
				// final int size = d.size();
				// final DoubleMatrix2D swap = new DenseDoubleMatrix2D(size,
				// size+1);
				// swap.viewColumn(0).assign(d);
				// swap.viewPart(0, 1, size, size).assign(v);
				// v = swap.viewSorted(0).viewPart(0, 1, size, size);
				// d = swap.viewColumn(0);
				DoubleMatrix2D invLaplacian = new SparseDoubleMatrix2D(nodeCount, nodeCount);
				for (int i = nodeCount - 1; i > 0; i--) {
					final DoubleMatrix2D ev = new SparseDoubleMatrix2D(nodeCount, 1);
					ev.viewColumn(0).assign(v.viewColumn(i));
					final DoubleMatrix2D result = new SparseDoubleMatrix2D(nodeCount, nodeCount);
					ev.zMult(ev, result, 1, 0, false, true);
					result.assign(DoubleFunctions.div(d.get(i)));
					invLaplacian.assign(result, DoubleFunctions.plus);
				}
				return invLaplacian;
			}
		} catch (IllegalArgumentException ex) {
			// If the degreeMatrix is singular, than no inverse could be
			// computed.
			logger.warn(Messages.SM_ERRORLAPLACIAN);
		} catch (Exception ex) {
			// ignore
		}
		return null;
	}

	private void init() {
		selectedNodes = new HashSet<CyNode>();
		selectedNodes.addAll(CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true));
		if (selectedNodes.size() == 0) {
			selectedNodes.addAll(network.getNodeList());
		}
		// check how many connected components the network has
		Set<CyNode> allNodes = new HashSet<CyNode>();
		allNodes.addAll(network.getNodeList());
		netConnComp = new ConnComp(network, allNodes);
		dialog = new AnalysisDialog(CyUtils.getCyFrame(context), context, network, selectedNodes,
				netConnComp.getConnCompNumber() > 1);
		dialog.setVisible(true);
	}
}
