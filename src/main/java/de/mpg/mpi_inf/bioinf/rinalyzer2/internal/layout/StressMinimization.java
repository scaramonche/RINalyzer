package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import java.util.Arrays;
import java.util.HashMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/****
 * 
 * Implements stress minimization for graph layout computation. This method basically tries to
 * minimize the error between the distances of the graph's nodes in the layout and given distances,
 * where a weight factor can be applied to prioritize certain distances. If no specific distances
 * are set, the graph theoretic distance is used. The optimization is done in an iterative process.
 * Precondition: The graph induced by the nodes from the list in NA has to be connected.
 * 
 * @author kklein TODO: [RINLayout] Adapt weights according to the secondary structure elements
 * 
 */
public class StressMinimization {
	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout.StressMinimization.class);

	CyNetwork network; // The Cytoscape network
	// / Some parameter members to tweak the computation
	boolean hasEdgeCosts, hasInitialLayout; // Does the NetworkAttributes input give us costs and
											// positions
	int numberOfIterations; // Number of iterations performed by the stress minimization.
	double edgeCosts; // Uniform edge costs in case we don't have individual ones in NA
	// The average edge costs. Needed to define distances of nodes belonging to
	// different graph components.
	double avgEdgeCosts; // Not functional so far
	boolean componentLayout; // Indicates whether the components should be treated separately (if
								// set to true), not functional so far.
	boolean fixXCoords;
	boolean fixYCoords;
	boolean fixZCoords;
	boolean useNodeSizes; // Node size is respected when calculating distances
	boolean threeD; // Tells us if we HAVE z coordinates, might still not set them
	// Matrix for shortest path lengths
	double[][] shortestPathMatrix = null;
	// Matrix for edge length priority weights
	double[][] weightMatrix = null;
	// Anchor coordinates, store initial layout
	private HashMap<CyNode, Double> anX;
	private HashMap<CyNode, Double> anY;
	// Mainly needed to access tunable parameters
	RINStressLayoutContext layoutContext;

	// Convergence constant.
	final static double EPSILON = 10e-4;

	// Default number of pivots used for the initial Pivot-MDS layout
	final static int DEFAULT_NUMBER_OF_PIVOTS = 50;

	// Indicates whether epsilon convergence is used or not.
	TERMINATION_CRITERION terminationCriterion;

	public enum TERMINATION_CRITERION {
		NONE, POSITION_DIFFERENCE, STRESS
	};

	// Constructor: Constructs instance of stress majorization.
	public StressMinimization(RINStressLayoutContext layoutContext) {
		hasEdgeCosts = false;
		hasInitialLayout = false;
		numberOfIterations = 200;
		edgeCosts = 100.0;
		avgEdgeCosts = -1;
		componentLayout = true;
		terminationCriterion = TERMINATION_CRITERION.NONE;
		fixXCoords = false;
		fixYCoords = false;
		fixZCoords = true;
		threeD = false;
		useNodeSizes = true;
		this.layoutContext = layoutContext;
	}

	// / call method, calculates a new layout
	// / @param NA the network attributes, i.e. nodes and corresponding coordinates for the graph
	public boolean call(NetworkAttributes na, CyNetwork net) {
		this.network = net;
		// System.out.println("Network "+net);
		// if the graph has at most one node nothing to do
		if (na.numberOfNodes() <= 1) {
			// make it exception save
			logger.warn("Isolated node");
			for (CyNode v : na.nodeList) {
				na.x(v, 0.0);
				na.y(v, 0.0);
			}
			return true;
		}

		// / TODO: [RINLayout] Check here if connected, otherwise abort if componentLayout is true

		// /
		this.shortestPathMatrix = null;
		this.weightMatrix = null;
		initMatrices(na);
		// System.err.println(Arrays.deepToString(shortestPathMatrix));
		// if the edge costs are defined by the attribute copy it to an array and
		// construct the proper shortest path matrix
		try {
			if (hasEdgeCosts) {
				// TODO: [RINLayout] Check here if costs indeed are defined
				avgEdgeCosts = ShortestPath.dijkstra_SPAP(na, shortestPathMatrix);
				// compute shortest path all pairs
			} else {
				avgEdgeCosts = layoutContext.optDistFactor * edgeCosts;
				ShortestPath.bfs_SPAP(na, shortestPathMatrix, layoutContext.optDistFactor
						* edgeCosts, this.network, this.useNodeSizes);
			}
		} catch (Exception e) {
			logger.warn("Error in shortest path calculation");
		}
		call(na, net, this.shortestPathMatrix, this.weightMatrix);
		return true;
	}

	// Runs the stress for a given Graph, shortest path and weight matrix.
	public void call(NetworkAttributes na, CyNetwork net, double[][] shortestPathMatrix,
			double[][] weightMatrix) {
		this.network = net;
		this.shortestPathMatrix = shortestPathMatrix;
		this.weightMatrix = weightMatrix;
		// compute the initial layout if necessary
		if (!hasInitialLayout) {
			computeInitialLayout(na);
			// System.out.println("Initial layout computed");
		}

		// replace infinity distances by sqrt(n) and compute weights.
		// / TODO: [RINLayout] Check here if connected, otherwise abort if componentLayout is true
		// if (!isConnected(network)) {
		// replaceInfinityDistances(na.numberOfNodes() - 1, shortestPathMatrix,
		// m_avgEdgeCosts * Math.sqrt((double)(na.numberOfNodes())));
		// }
		// calculate the weights
		try {
			calcWeights(na, na.numberOfNodes() - 1);
		} catch (Exception e) {
			logger.warn("Error in initial weight calculation");
		}
		// minimize the stress
		try {
			minimizeStress(na);
		} catch (Exception e) {
			logger.warn("Error in stress minimization");
		}

	}

	// Tells whether the current layout should be used or the initial layout
	// needs to be computed.
	public void hasInitialLayout(boolean hasInitialLayout) {
		this.hasInitialLayout = hasInitialLayout;
	}

	// Tells whether the x coordinates are allowed to be modified or not.
	public void fixXCoordinates(boolean fix) {
		fixXCoords = fix;
	}

	// Tells whether the y coordinates are allowed to be modified or not.
	public void fixYCoordinates(boolean fix) {
		fixYCoords = fix;
	}

	// Tells whether the z coordinates are allowed to be modified or not.
	public void fixZCoordinates(boolean fix) {
		fixZCoords = fix;
	}

	// Sets whether the graph's components should be laid out separately or a dummy
	// distance should be used for nodes within different components.
	public void layoutComponentsSeparately(boolean separate) {
		componentLayout = separate;
	}

	// Sets the desired distance between adjacent nodes. If the new value is smaller or equal
	// 0 the default value (100) is used.
	public void setEdgeCosts(double edgeCosts) {
		this.edgeCosts = (edgeCosts > 0 ? edgeCosts : 100);
	}

	// Sets a fixed number of iterations for stress majorization. If the new value is smaller or
	// equal
	// 0 the default value (200) is used.
	public void setIterations(int numberOfIterations) {
		this.numberOfIterations = (numberOfIterations > 0 ? numberOfIterations : 100);
	}

	// Tells which \a TERMINATION_CRITERIA should be used
	public void convergenceCriterion(TERMINATION_CRITERION criterion) {
		this.terminationCriterion = criterion;
	}

	// Tells whether the edge costs are uniform or defined by some edge costs attribute.
	public void useEdgeCostsAttribute(boolean useEdgeCostsAttribute) {
		this.hasEdgeCosts = useEdgeCostsAttribute;
	}

	// Calculates the stress for the given layout
	double calcStress(NetworkAttributes na) {
		// int nodeCount = na.numberOfNodes() - 1;
		double stress = 0;
		// We start with the anchor positions already, no need to slowly move there
		double constraintFac = (this.hasInitialLayout ? 0.6 : 0.0);
		for (int v = 0; v < na.numberOfNodes(); v++) {
			for (int w = v + 1; w < na.numberOfNodes(); w++) {
				double xDiff = na.indx(v) - na.indx(w);
				double yDiff = na.indy(v) - na.indy(w);
				double zDiff = 0.0;
				if (threeD) {
					zDiff = na.indz(v) - na.indz(w);
				}
				double dist = Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
				if (dist != 0) {
					if (this.hasInitialLayout && constraintFac > 0.3)
						constraintFac -= 0.01;
					stress += (1.0 - constraintFac)
							* (this.weightMatrix[v][w] * (this.shortestPathMatrix[v][w] - dist) * (this.shortestPathMatrix[v][w] - dist));
				}
			}
			if (this.hasInitialLayout) {
				double xDiff = na.indx(v) - anX.get(na.nodeList.get(v));
				double yDiff = na.indy(v) - anY.get(na.nodeList.get(v));
				double anchorDist = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
				stress += constraintFac * anchorDist;
			}
		}
		// System.out.println("Stress"+stress);
		return stress;
	}

	// Calculates the weight matrix of the shortest path matrix. This is done by w_ij = s_ij^{-2}
	void calcWeights(NetworkAttributes na, int dimension) {
		// System.out.println(na.numberOfNodes());
		for (int v = 0; v < na.numberOfNodes(); v++) {
			for (int w = 0; w < na.numberOfNodes(); w++) {
				if (v != w) {
					// w_ij = d_ij^-2
					// Index here is in CC!
					double factor = (na.ssindex(na.nodeList.get(v)) == na.ssindex(na.nodeList
							.get(w)) ? layoutContext.ssinfac : layoutContext.ssoutfac);
					this.weightMatrix[v][w] = factor
							/ (this.shortestPathMatrix[v][w] * this.shortestPathMatrix[v][w]);
				}
			}
		}
	}

	// Calculates the initial layout of the graph if necessary.
	// Usually we would want to just use coordinates coming from Chimera.
	void computeInitialLayout(NetworkAttributes na) {
		try {
			PivotMDS pivMDS = new PivotMDS(layoutContext);
			pivMDS.setNumberOfPivots(DEFAULT_NUMBER_OF_PIVOTS);
			pivMDS.useEdgeCostsAttribute(hasEdgeCosts);
			pivMDS.setEdgeCosts(edgeCosts);
			pivMDS.setUseNodeSizes(this.useNodeSizes);
			if (!componentLayout) {
				logger.warn("Internal decomposition not supported yet.");
			} else {
				pivMDS.call(na, this.network);
			}
		} catch (Exception e) {
			logger.warn("Error in initial layout computation");
		}
	}

	// Convenience method copying the layout of the graph in case of epsilon convergence.
	void copyLayout(NetworkAttributes na, double[] newX, double[] newY) {
		// copy the layout
		for (int v = 0; v < na.numberOfNodes(); v++) {
			newX[v] = na.indx(v);
			newY[v] = na.indy(v);
		}
	}

	// Convenience method copying the layout of the graph in case of epsilon convergence for 3D.
	void copyLayout(NetworkAttributes na, double[] newX, double[] newY, double[] newZ) {
		// copy the layout
		for (int v = 0; v < na.numberOfNodes(); v++) {
			newX[v] = na.indx(v);
			newY[v] = na.indy(v);
			newZ[v] = na.indz(v);
		}
	}

	// Checks for epsilon convergence and whether the performed number of iterations
	// exceed the predefined maximum number of iterations.
	boolean finished(NetworkAttributes na, int numberOfPerformedIterations, double[] prevXCoords,
			double[] prevYCoords, double prevStress, double curStress) {
		if (numberOfPerformedIterations == this.numberOfIterations) {
			return true;
		}

		switch (this.terminationCriterion) {
		case POSITION_DIFFERENCE: {
			double eucNorm = 0;
			double dividend = 0;
			double diffX;
			double diffY;
			// compute the translation of all nodes between
			// the consecutive layouts
			for (int i = 0; i < na.numberOfNodes(); i++) {
				diffX = prevXCoords[i] - na.indx(i);
				diffY = prevYCoords[i] - na.indy(i);
				dividend += diffX * diffX + diffY * diffY;
				eucNorm += prevXCoords[i] * prevXCoords[i] + prevYCoords[i] * prevYCoords[i];
			}
			return Math.sqrt(dividend) / Math.sqrt(eucNorm) < EPSILON;
		}
		case STRESS:
			return curStress == 0 || prevStress - curStress < prevStress * EPSILON;

		default:
			return false;
		}
	}

	// Convenience method to initialize the matrices.
	void initMatrices(NetworkAttributes na) {
		try {
			if (this.hasInitialLayout) {
				this.anX = new HashMap<CyNode, Double>();
				this.anY = new HashMap<CyNode, Double>();
			}
			int numNodes = na.nodeList.size();
			this.shortestPathMatrix = new double[numNodes][numNodes];
			this.weightMatrix = new double[numNodes][numNodes];

			for (int i = 0; i < numNodes; i++) {
				// init shortest path matrix by infinity distances
				Arrays.fill(this.shortestPathMatrix[i], Double.POSITIVE_INFINITY);
				// Arrays.fill(weightMatrix[i], 0); // Done automatically
				this.shortestPathMatrix[i][i] = 0.0; // Diagonal, done automatically
				// but it is faster to reassign than to check for i==k in a loop
				if (this.hasInitialLayout) {
					anX.put(na.nodeList.get(i), na.indx(i));
					anY.put(na.nodeList.get(i), na.indy(i));
				}
			}
			// System.err.println(Arrays.deepToString(shortestPathMatrix));
		} catch (Exception e) {
			logger.warn("Error in matrix initialization");
		}
	}

	// Minimizes the stress for each component separately given
	// the shortest path matrix and the weight matrix.
	void minimizeStress(NetworkAttributes na) {
		int numberOfPerformedIterations = 0;

		double prevStress = Double.MAX_VALUE;
		double curStress = Double.MAX_VALUE;

		if (terminationCriterion == TERMINATION_CRITERION.STRESS) {
			curStress = calcStress(na);
		}

		double[] newX = null;
		double[] newY = null;
		double[] newZ = null;

		if (terminationCriterion == TERMINATION_CRITERION.POSITION_DIFFERENCE) {
			newX = new double[na.numberOfNodes()];
			newY = new double[na.numberOfNodes()];
			if (threeD)
				newZ = new double[na.numberOfNodes()];
		}
		do {
			if (terminationCriterion == TERMINATION_CRITERION.POSITION_DIFFERENCE) {
				if (threeD)
					copyLayout(na, newX, newY, newZ);
				else
					copyLayout(na, newX, newY);
			}
			nextIteration(na, this.shortestPathMatrix, this.weightMatrix);
			if (terminationCriterion == TERMINATION_CRITERION.STRESS) {
				prevStress = curStress;
				curStress = calcStress(na);
			}
		} while (!finished(na, ++numberOfPerformedIterations, newX, newY, prevStress, curStress));

		// System.out.println("Iteration count:\t" + numberOfPerformedIterations
		// + "\tStress:\t" + calcStress(na) );

	}

	// Runs the next iteration of the stress minimization process. Note that serial update
	// is used.
	void nextIteration(NetworkAttributes na, double[][] shortestPathMatrix, double[][] weightMatrix) {
		double newXCoord;
		double newYCoord;
		double newZCoord;
		double totalWeight;

		double desDistance;
		double euclideanDist;
		double weight;
		double voteX;
		double voteY;
		double voteZ;
		double xDiff;
		double yDiff;
		double zDiff;

		for (int v = 0; v < na.numberOfNodes(); v++) {
			newXCoord = 0.0;
			newYCoord = 0.0;
			newZCoord = 0.0;
			double currXCoord = na.indx(v);
			double currYCoord = na.indy(v);
			totalWeight = 0;
			for (int w = 0; w < na.numberOfNodes(); w++) {
				if (v == w) {
					continue;
				}
				// TODO: [RINLayout]
				// calculate euclidean distance between both points and adapt to constraints
				xDiff = currXCoord - na.indx(w);
				yDiff = currYCoord - na.indy(w);
				if (threeD)
					zDiff = na.indz(v) - na.indz(w);
				else
					zDiff = 0.0;
				euclideanDist = Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
				// get the weight
				weight = weightMatrix[v][w];
				// get the desired distance
				desDistance = shortestPathMatrix[v][w];
				// reset the voted x coordinate
				voteX = 0.0;
				// if x is not fixed
				if (!fixXCoords) {
					voteX = na.indx(w);
					if (euclideanDist != 0) {
						// calc the vote
						voteX += desDistance * (currXCoord - voteX) / euclideanDist;
					}
					// add the vote
					newXCoord += weight * voteX;
				}
				// reset the voted y coordinate
				voteY = 0.0;
				// y is not fixed
				if (!fixYCoords) {
					voteY = na.indy(w);
					if (euclideanDist != 0) {
						// calc the vote
						voteY += desDistance * (currYCoord - voteY) / euclideanDist;
					}
					newYCoord += weight * voteY;
				}
				if (threeD) {
					// reset the voted z coordinate
					voteZ = 0.0;
					// z is not fixed
					if (!fixZCoords) {
						voteZ = na.indz(w);
						if (euclideanDist != 0) {
							// calc the vote
							voteZ += desDistance * (na.indz(v) - voteZ) / euclideanDist;
						}
						newZCoord += weight * voteZ;
					}
				}
				// sum up the weights
				totalWeight += weight;
			}
			// update the positions
			if (totalWeight != 0) {
				if (!fixXCoords) {
					na.indx(v, newXCoord / totalWeight);
				}
				if (!fixYCoords) {
					na.indy(v, newYCoord / totalWeight);
				}
				if (threeD)
					if (!fixZCoords) {
						na.indz(v, newZCoord / totalWeight);
					}
			}
		}

	}

	// Replaces infinite distances to the given value
	void replaceInfinityDistances(int dimension, double[][] shortestPathMatrix, double newVal) {
		for (int i = 0; i < dimension; i++) {
			for (int j = i + 1; j <= dimension; j++) {
				if (Double.isInfinite(shortestPathMatrix[i][j])) {
					shortestPathMatrix[i][j] = newVal;
					shortestPathMatrix[j][i] = newVal;
				}
			}
		}
	}
}
