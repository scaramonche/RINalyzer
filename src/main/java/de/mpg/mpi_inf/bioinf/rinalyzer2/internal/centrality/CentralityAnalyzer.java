package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.function.tdouble.DoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.DoubleProperty;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.io.ResultsWriter;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui.ResultsPanel;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Class controlling the analysis. CyNode that the actual analysis is started not till the
 * <code>compute</code> method is called. However, the data needed for computation is stored in the
 * class upon initialization of the constructor.
 * 
 * @author Nadezhda Doncheva
 */

public class CentralityAnalyzer {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.CentralityAnalyzer.class);

	/**
	 * Initializes a new instance of <code>CentralityAnalyzer</code> with the data needed for batch
	 * analysis.
	 * 
	 * @param aNetwork
	 *            Network to be analyzed.
	 * @param aSelected
	 *            Set of selected nodes.
	 * @param aWeightOpts
	 *            [weightAttr, edgeType, negWeight, simToDist]
	 * @param aConnComp
	 *            Pairs of nodes for betweenness computation.
	 * @param aDegreeCutoff
	 *            Degree cutoff for the centrality degree.
	 * @param aDefWeight
	 *            Default weight to use for missing weight values.
	 * @param aMeasuresToComp
	 *            Array with flags indicating which measures should be computed.
	 * @param aResultsFile
	 *            File for saving all centrality values for the analyzed network.
	 */
	public CentralityAnalyzer(CyServiceRegistrar bundle, CyNetwork aNetwork, Set<CyNode> aSelected,
			String[] aWeightOpts, ConnComp aConnComp, double aDegreeCutoff, double aDefWeight,
			boolean[] aMeasuresToComp, File aResultsFile) {
		context = bundle;
		resultData = new ResultData();
		network = aNetwork;
		n = network.getNodeCount();
		p = (n) * (n - 1);
		resultData.setNetworkTitle(network.getRow(network).get(CyNetwork.NAME, String.class));
		resultData.setNetwork(network);
		selected = aSelected;
		s = selected.size();
		resultData.setSelectedCyNodes(selected);
		weightOpts = aWeightOpts;
		connComp = aConnComp;
		if (connComp != null) {
			p = connComp.getPairsNumber();
		}
		defWeight = aDefWeight;
		degreeCutoff = aDegreeCutoff;
		measuresToCompute = aMeasuresToComp;
		resultsFile = aResultsFile;
		resultData.setAnalysisOptions(network.getRow(network).get(CyNetwork.NAME, String.class),
				measuresToCompute, weightOpts, defWeight, degreeCutoff, connComp != null);
		progress = 0;
		cancelled = false;
		node2index = null;
	}

	/**
	 * Initializes a new instance of <code>CentralityAnalyzer</code> with the data needed for single
	 * network analysis.
	 * 
	 */
	public CentralityAnalyzer(CyServiceRegistrar bundle, CyNetwork aNetwork, Set<CyNode> aSelected,
			String[] aWeightOpts, ConnComp aConnComp, double aDegreeCutoff, double aDefWeight,
			boolean[] aMeasuresToComp) {
		this(bundle, aNetwork, aSelected, aWeightOpts, aConnComp, aDegreeCutoff, aDefWeight,
				aMeasuresToComp, null);
	}

	/**
	 * Run the computation of all centrality measures and save them in the same instance of
	 * <code>ResultData</code>.
	 */
	public boolean compute() {
		try {
			// Compute centralities
			// long time = System.currentTimeMillis();
			final DoubleMatrix2D adjMatrix = initWeights();
			Set<IntPair> pairs = null;
			if (connComp != null) {
				pairs = connComp.getPairsMap(node2index);
			}
			// if (cancelled) {
			// return;
			// }
			if (measuresToCompute[1] || measuresToCompute[2]) {
				DoubleMatrix2D invLapl = getInverseLaplacian2(adjMatrix);
				if (invLapl != null) {
					if (measuresToCompute[1]) {
						// Current flow centrality
						CurrentFlowCentrality cfc = new CurrentFlowCentrality(node2index, selected,
								network, invLapl);
						//System.out.print(Messages.LOG_COMPCFCLOS);
						resultData.setCentralty(Messages.CENT_CFC, cfc.closenessCentrality());
						//System.out.print(Messages.LOG_DONE);
						// if (cancelled) {
						// return;
						// }
						//System.out.print(Messages.LOG_COMPCFBETW);
						resultData.setCentralty(Messages.CENT_CFB,
								cfc.betweennessCentrality(pairs, adjMatrix));
						//System.out.print(Messages.LOG_DONE);
						// if (cancelled) {
						// return;
						// }
						System.gc();
						// System.out.println("Time: " +
						// (System.currentTimeMillis() - time) +
						// " ms");
					} else {
						progress += (n > p ? n : p) + s;
						// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPCF_NOT);
					}
					if (measuresToCompute[2]) {
						// Random walk centrality
						RandomWalkCentrality rwc = new RandomWalkCentrality(node2index, adjMatrix,
								selected, network, invLapl);
						//System.out.print(Messages.LOG_COMPRWRCLOS);
						resultData.setCentralty(Messages.CENT_RWRC,
								rwc.receiverClosenessCentrality());
						//System.out.print(Messages.LOG_DONE);
						// if (cancelled) {
						// return;
						// }
						//System.out.print(Messages.LOG_COMPRWBETW);
						resultData
								.setCentralty(Messages.CENT_RWB, rwc.betweennessCentrality(pairs));
						// System.out.print(Messages.LOG_DONE);
						// if (cancelled) {
						// return;
						// }
						System.gc();
					} else {
						progress += n + p;
						// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPRW_NOT);
					}
				}
			} else {
				progress += (n > p ? n : p) + s + n + p;
				// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPCF_NOT);
				// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPRW_NOT);
			}

			// Compute shortest path centralities
			if (measuresToCompute[0]) {
				ShortestPathCentrality spc = new ShortestPathCentrality(node2index, selected,
						network);
				// FileWriter file = new FileWriter(new File("weights.txt"));
				// final Iterator<?> it = network.nodesIterator();
				// final List<CyNode> nodes = new LinkedList<CyNode>();
				// CyAttributes attr = Cytoscape.getCyNodeAttributes();
				// while (it.hasNext()) {
				// final CyNode node = (CyNode) it.next();
				// nodes.add(node);
				// file.write("\t" +
				// attr.getStringAttribute(node.getIdentifier(), "GeneName"));
				// }
				// file.write("\n");
				// for (final CyNode node : nodes) {
				// file.write(attr.getStringAttribute(node.getIdentifier(),
				// "GeneName"));
				// for (final CyNode node2 : nodes) {
				// file.write("\t" +
				// String.valueOf((int)adjMatrix.get(node2index.get(node),
				// node2index.get(node2))));
				// }
				// file.write("\n");
				// }
				// file.close();
				// FileWriter file = new FileWriter(new File("weights.txt"));
				// file.write(adjMatrix.toString());
				// file.close();
				modifyWeights(adjMatrix);
				// file = new FileWriter(new File("weights_mod.txt"));
				// file.write(adjMatrix.toString());
				// file.close();
				//System.out.print(Messages.LOG_COMPSPDEG);
				resultData.setCentralty(Messages.CENT_SPD,
						spc.degreeCentrality(adjMatrix, degreeCutoff));
				//System.out.print(Messages.LOG_DONE);
				// if (cancelled) {
				// return;
				// }
				//System.out.print(Messages.LOG_COMPSPBETW);
				resultData.setCentralty(Messages.CENT_SPB,
						spc.betweennessCentrality(pairs, adjMatrix));
				//System.out.print(Messages.LOG_DONE);
				// if (cancelled) {
				// return;
				// }
				//System.out.print(Messages.LOG_COMPSPCLOS);
				resultData.setCentralty(Messages.CENT_SPC, spc.closenessCentrality());
				//System.out.print(Messages.LOG_DONE);
				System.gc();
				// if (cancelled) {
				// return;
				// }
				// System.out.println("Time: " + (System.currentTimeMillis() -
				// time) + " ms");
			} else {
				progress += n + s + s + p;
				// RINalyzerPlugin.logWarningMessage(Messages.LOG_COMPSP_NOT);
			}
			// Computation time
			// System.out.println("Computation time: " +
			// (System.currentTimeMillis() - time) / 1000
			// + " s");
		} catch (Exception e) {
			// e.printStackTrace();
			JOptionPane.showMessageDialog(CyUtils.getCyFrame(context), Messages.SM_ERRORCOMP,
					Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
			logger.warn(Messages.SM_ERRORCOMP);
		}
		return true;
	}

	/**
	 * Gets the current progress of the analyzer as a number of steps.
	 * 
	 * @return Number of steps completed in the analysis process.
	 */
	public int getCurrentProgress() {
		return progress;
	}

	/**
	 * Gets the maximum progress of the analyzer as a number of steps. (spb = n*p, spc = n*s, spd =
	 * n*s, aspp = n*n, cfb = n*p, cfc = n*s, rwb = n*p rwc = n*s, where n = node count in network,
	 * s = selected nodes, p = pairs for betweenness)
	 * 
	 * @return Total number of steps required for the analyzer to finish.
	 */
	public int getMaxProgress() {
		// aspp = n*n, spb = p*n, spc = s*n, spd = s*n, cfb = n*p, cfc = s*n,
		// rwb = p*n rwc = n*s
		// aspp = n, spd = s, spc = s, spb = p, cfb = n, cfc = s, rwb = p, rwc =
		// n
		return (n + s + s + p) + ((n > p ? n : p) + s) + (p + n);
	}

	/**
	 * Cancels the process of network analysis.
	 * <p>
	 * Note that this method does not force the analyzer to cancel immediately; it takes an
	 * unspecified period of time until the analysis thread actually stops.
	 * </p>
	 */
	public void cancel() {
		cancelled = true;
	}

	/**
	 * Creates a panel in the Cytoscape's Results panel and visualizes the computed data. It
	 * initializes the <code>ResultsPanel</code> the first time it is called.
	 */
	public void displayResults() {
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
	public void saveResults() {
		// don't show results if no centralities have been computed.
		if (resultData.getComputed().size() > 0) {
			try {
				ResultsWriter.save(resultData, resultsFile);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(CyUtils.getCyFrame(context), Messages.SM_IOERRORSAVE,
						Messages.DT_ERROR, JOptionPane.ERROR_MESSAGE);
				logger.warn(Messages.SM_IOERRORSAVE);
			}
			return;
		}
	}

	/**
	 * Get the analyzed network.
	 */
	public CyNetwork getNetwork() {
		return network;
	}

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
	private DoubleMatrix2D initWeights() {
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
					// TODO: [Old] Distinguish undirected and directed case?
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
	private void modifyWeights(DoubleMatrix2D adjMatrix) {
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
	protected DoubleMatrix2D getInverseLaplacian(DoubleMatrix2D weightMatrix) {
		// look if its already computed, and if not compute it
		try {
			final int nodeCount = weightMatrix.columns();
			// build degree matrix D
			DoubleMatrix2D degreeMatrix = new SparseDoubleMatrix2D(nodeCount, nodeCount);
			for (int row = 0; row < weightMatrix.rows(); row++) {
				degreeMatrix.set(row, row, weightMatrix.viewRow(row).zSum());
			}
			// build subtraction D-A
			degreeMatrix.assign(weightMatrix, DoubleFunctions.minus);
			// invert
			DoubleMatrix2D invLaplacianPart = new DenseDoubleAlgebra().inverse(degreeMatrix
					.viewPart(0, 0, nodeCount - 1, nodeCount - 1));
			DoubleMatrix2D invLaplacian = new SparseDoubleMatrix2D(nodeCount, nodeCount);
			invLaplacian.viewPart(0, 0, nodeCount - 1, nodeCount - 1).assign(invLaplacianPart);
			return invLaplacian;
		} catch (IllegalArgumentException ex) {
			// If the degreeMatrix is singular, than no inverse could be
			// computed.
			logger.warn(Messages.SM_ERRORLAPLACIAN);
		} catch (Exception ex) {
			// ignore
		}
		return null;
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
	// TODO: [Old] Is this really the better method?
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
			// TODO: [Old] Change to Property.ZERO.isSingular(degreeMatrix) for release
			if (!new DoubleProperty(1.0E-15).isSingular(degreeMatrix)) {
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

	/**
	 * Data containing all computed values of the centrality measures.
	 */
	private ResultData resultData;

	/**
	 * {@link CyNetwork} that has been analyzed.
	 */
	private CyNetwork network;

	/**
	 * Map matching each node with an index for intern representation.
	 */
	private Map<CyNode, Integer> node2index;

	/**
	 * Set of nodes selected for the analysis.
	 */
	private Set<CyNode> selected;

	/**
	 * Degree cutoff needed for the computation of the degree centrality.
	 */
	private double degreeCutoff;

	/**
	 * [weightAttr, edgeType, negWeight, simToDist]
	 */
	private String[] weightOpts;

	/**
	 * Default value for missing edge weight values.
	 */
	private double defWeight;

	/**
	 * Connected components of selected set.
	 */
	private ConnComp connComp;

	/**
	 * Array with boolean flags for each group of centrality measures. If the flag is
	 * <code>true</code>, the measure should be computed.
	 */
	private boolean[] measuresToCompute;

	/**
	 * File to save the centrality results.
	 */
	private File resultsFile;

	// private boolean compOnlySP;

	/**
	 * Current progress of the analysis.
	 * <p>
	 * The progress of the analyzer is measured in number of steps. Extender classes are responsible
	 * for maintaining the value of this field up to date. The progress must be a natural number not
	 * greater than the maximal progress.
	 * </p>
	 * 
	 * @see #getMaxProgress()
	 */
	public static int progress;

	/**
	 * Flag indicating if the process of analysis was canceled by the user.
	 * <p>
	 * This flag should only be modified by calling {@link #cancel()}. Extender classes should
	 * terminate the analysis once this flag is set to <code>true</code>. Therefore, in the process
	 * of analysis, the value of this flag should be checked at regular intervals.
	 * </p>
	 */
	public static boolean cancelled;

	/**
	 * Number of pairs,
	 */
	private int p;

	/**
	 * Number of nodes.
	 */
	private int n;

	/**
	 * Number of selected nodes.
	 */
	private int s;

	private CyServiceRegistrar context;
}
