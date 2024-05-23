package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.view.model.View;

/****
 * 
 * Implements pivot MDS for initialization of stress minimization, as suggested by Brandes et al.
 * 
 * @author kklein
 * 
 */
public class PivotMDS {
	private final double EPSILON = 1 - 1e-10;
	private final double FACTOR = -0.5;
	private final int DIMENSION_COUNT = 3;
	// Seed of the random number generator.
	private final static int SEED = 0;

	// The number of pivots.
	private int numberOfPivots;

	// The size of the pivot array
	private int pivotSize;

	// The costs to traverse an edge.
	private double edgeCosts;
	// Tells whether the pivot mds is based on uniform edge costs or a
	// edge costs attribute
	private boolean hasEdgeCostsAttribute;
	private boolean useNodeSizes;
	private CyNetwork network;
	NetworkAttributes nAttr = null;
	private static RINStressLayoutContext layoutContext;

	int curPivotNum() {
		if (nAttr == null)
			return 0;
		return Math.min(nAttr.numberOfNodes(), numberOfPivots);
	}

	public void call(NetworkAttributes na, CyNetwork network) {
		// TODO: [RINLayout] Assert connectivity here
		nAttr = na;
		this.network = network;
		pivotMDSLayout(na);

	}

	PivotMDS(RINStressLayoutContext layoutContext) {
		numberOfPivots = 250;
		edgeCosts = 100;
		hasEdgeCostsAttribute = false;
		pivotSize = 0;
		useNodeSizes = false;
		this.layoutContext = layoutContext;
	}

	// The dimension count determines the number of evecs that
	// will be computed. Nevertheless PivotMDS only takes the first two
	// with the highest eigenwert into account.

	// Sets the number of pivots. If the new value is smaller or equal 0
	// the default value (250) is used.
	void setNumberOfPivots(int numberOfPivots) {
		this.numberOfPivots = (numberOfPivots < DIMENSION_COUNT) ? DIMENSION_COUNT : numberOfPivots;
	}

	// Sets the desired distance between adjacent nodes. If the new value is smaller or equal
	// 0 the default value (100) is used.
	void setEdgeCosts(double edgeCosts) {
		this.edgeCosts = edgeCosts;
	}

	void useEdgeCostsAttribute(boolean useEdgeCostsAttribute) {
		this.hasEdgeCostsAttribute = useEdgeCostsAttribute;
	}

	void setUseNodeSizes(boolean b) {
		this.useNodeSizes = b;
	}

	// Centers the pivot matrix.
	void centerPivotmatrix(double[][] pivotMatrix) {
		int numberOfPivots = curPivotNum();
		// The graph size is at least 2!
		int nodeCount = nAttr.numberOfNodes();

		double normalizationFactor = 0.0;
		double rowColNormalizer = 0.0;
		double[] colNormalization = new double[numberOfPivots];

		for (int i = 0; i < numberOfPivots; i++) {
			rowColNormalizer = 0;
			for (int j = 0; j < nodeCount; j++) {
				rowColNormalizer += pivotMatrix[i][j] * pivotMatrix[i][j];
			}
			normalizationFactor += rowColNormalizer;
			colNormalization[i] = rowColNormalizer / nodeCount;
		}
		normalizationFactor = normalizationFactor / (nodeCount * numberOfPivots);
		for (int i = 0; i < nodeCount; i++) {
			rowColNormalizer = 0;
			for (int j = 0; j < numberOfPivots; j++) {
				double square = pivotMatrix[j][i] * pivotMatrix[j][i];
				pivotMatrix[j][i] = square + normalizationFactor - colNormalization[j];
				rowColNormalizer += square;
			}
			rowColNormalizer /= numberOfPivots;
			for (int j = 0; j < numberOfPivots; j++) {
				pivotMatrix[j][i] = FACTOR * (pivotMatrix[j][i] - rowColNormalizer);
			}
		}
	}

	// Computes the pivot mds layout of the graph in NA.
	void pivotMDSLayout(NetworkAttributes na) {

		if (na.numberOfNodes() <= 1) {
			// make it exception save
			CyNode v;
			ListIterator<CyNode> it = na.nodeList.listIterator();
			while (it.hasNext()) {
				v = it.next();
				na.x(v, 0.0);
				na.y(v, 0.0);
				if (DIMENSION_COUNT > 2)
					na.z(v, 0.0);
			}
			return;
		}
		// check whether the graph is a path or not
		final CyNode head = getRootedPath(na, this.network);
		if (head != null) {
			doPathLayout(na, head);
		} else {
			double[][] pivDistMatrix = null;
			// compute the pivot matrix
			getPivotDistanceMatrix(na, pivDistMatrix);
			// center the pivot matrix
			centerPivotmatrix(pivDistMatrix);
			// init the coordinate matrix
			double[][] coord = new double[DIMENSION_COUNT][na.nodeList.size()];

			// init the eigen values array
			double[] eVals = new double[DIMENSION_COUNT];
			singularValueDecomposition(pivDistMatrix, coord, eVals);
			// compute the correct aspect ratio
			for (int i = 0; i < DIMENSION_COUNT; i++) {
				eVals[i] = Math.sqrt(eVals[i]);
				for (int j = 0; j < na.nodeList.size(); j++) {
					coord[i][j] *= eVals[i];
				}
			}
			// set the new positions to the graph
			int i = 0;

			for (CyNode v : na.nodeList) {
				na.x(v, coord[0][i]);
				na.y(v, coord[1][i]);
				if (DIMENSION_COUNT > 2) {
					na.z(v, coord[2][i]);// cout << coord[2][i] << "\n";
				}
				++i;
			}
		}
	}

	// Computes the layout of a path.
	void doPathLayout(NetworkAttributes na, CyNode v) {
		double xPos = 0;
		CyNode prev = v;
		CyNode cur = v;
		// since the given node is the beginning of the path just
		// use bfs and increment the x coordinate by the average
		// edge costs.
		HashSet<CyEdge> adjEdges = new HashSet<CyEdge>();
		do {
			na.x(cur, xPos);
			na.y(cur, 0);
			CyNode adj;
			adjEdges.clear();
			adjEdges.addAll(network.getAdjacentEdgeList(cur, Type.ANY));
			for (CyEdge ce : adjEdges) {
				adj = (ce.getSource().equals(cur) ? ce.getTarget() : cur);
				if (!(adj.equals(prev)) || adj.equals(cur)) { // Could still be loop
					prev = cur;
					cur = adj;
					if (hasEdgeCostsAttribute) {
						xPos += na.weight(ce);
					} else {
						xPos += edgeCosts;
					}
					break;
				}
				prev = cur;
			}
		} while (prev != cur);
	}

	// Computes the eigen value decomposition based on power iteration.
	// K is of square dimension pivotnum, eVecs of DIMENSIONCOUNT times pivotnum (== dim1).
	void eigenValueDecomposition(double[][] K, double[][] eVecs, double[] eValues, final int dim1) {
		randomize(eVecs);
		double r = 0;
		for (int i = 0; i < DIMENSION_COUNT; i++) {
			eValues[i] = normalize(eVecs[i], dim1);
		}
		while (r < EPSILON) {
			if (Double.isNaN(r) || Double.isInfinite(r)) {
				// Throw arithmetic exception (Shouldn't occur
				// for DIMENSION_COUNT = 2
				throw new ArithmeticException("Arithmetic error in Eigenvalue decomposition");
			}
			// remember prev values
			double[][] tmpOld = new double[DIMENSION_COUNT][dim1];
			for (int i = 0; i < DIMENSION_COUNT; i++) {
				for (int j = 0; j < dim1; j++) {
					tmpOld[i][j] = eVecs[i][j];
					eVecs[i][j] = 0;
				}
			}
			// multiply matrices
			for (int i = 0; i < DIMENSION_COUNT; i++) {
				for (int j = 0; j < dim1; j++) {
					for (int k = 0; k < dim1; k++) {
						eVecs[i][k] += K[j][k] * tmpOld[i][j];
					}
				}
			}
			// orthogonalize
			for (int i = 0; i < DIMENSION_COUNT; i++) {
				for (int j = 0; j < i; j++) {
					double fac = prod(eVecs[j], eVecs[i], dim1) / prod(eVecs[j], eVecs[j], dim1);
					for (int k = 0; k < dim1; k++) {
						eVecs[i][k] -= fac * eVecs[j][k];
					}
				}
			}
			// normalize
			for (int i = 0; i < DIMENSION_COUNT; i++) {
				eValues[i] = normalize(eVecs[i], dim1);
			}
			r = 1;
			for (int i = 0; i < DIMENSION_COUNT; i++) {
				// get absolute value (abs only defined for int)
				double tmp = prod(eVecs[i], tmpOld[i], dim1);
				if (tmp < 0) {
					tmp *= -1;
				}
				r = Math.min(r, tmp);
			}
		}
	}

	void copyArrays(double[] copyTo, double[] copyFrom, int len) {
		for (int i = 0; i < len; i++) {
			copyTo[i] = copyFrom[i];
		}
	}

	// Computes the pivot distance matrix based on the maxmin strategy
	void getPivotDistanceMatrix(NetworkAttributes na, double[][] pivDistMatrix) {
		// lower the number of pivots if necessary
		int numberOfPivots = curPivotNum();
		// number of pivots times n matrix used to store the graph distances
		pivDistMatrix = new double[numberOfPivots][nAttr.numberOfNodes()];

		// edges costs array
		HashMap<CyEdge, Double> edgeCosts = null;
		boolean hasEdgeCosts = false;
		// already checked whether this attribute exists or not (see call method)
		if (hasEdgeCostsAttribute) {
			edgeCosts = new HashMap<CyEdge, Double>();
			CyEdge e;
			ListIterator<CyEdge> it = na.edgeList.listIterator();
			while (it.hasNext()) {
				e = it.next();
				edgeCosts.put(e, na.weight(e));
			}

			hasEdgeCosts = true;
		}
		// used for min-max strategy
		HashMap<CyNode, Double> minDistances = new HashMap<CyNode, Double>(); // (G,
																				// std::numeric_limits<double>::infinity());
		double[] shortestPathSingleSource = new double[na.nodeList.size()];

		// We assume the order of the nodes in na's nodelist is fixed and use it.
		// But for random access we still need a mapping
		HashMap<CyNode, Integer> nIndex = new HashMap<CyNode, Integer>();
		for (int l = 0; l < na.nodeList.size(); l++) {
			nIndex.put(na.nodeList.get(l), l);
		}
		int index = 0;

		// the current pivot node
		CyNode pivNode = na.nodeList.get(0);
		int pivIndex = 0; // Stored to keep correspondence of index and node for random access at
							// pivot
		for (int i = 0; i < numberOfPivots; i++) {
			// get the shortest path from the currently processed pivot node to
			// all other nodes in the graph
			for (int l = 0; l < na.nodeList.size(); l++) {
				shortestPathSingleSource[l] = Double.POSITIVE_INFINITY;
			}

			if (hasEdgeCosts) {
				ShortestPath.dijkstra_SPSS(pivNode, nIndex, na, shortestPathSingleSource,
						edgeCosts, this.network);
			} else {
				ShortestPath.bfs_SPSS(pivNode, nIndex, na, shortestPathSingleSource,
						this.edgeCosts, this.network, this.useNodeSizes);
			}
			copyArrays(pivDistMatrix[i], shortestPathSingleSource, na.nodeList.size());
			// update the pivot and the minDistances array ... to ensure the
			// correctness set minDistance of the pivot node to zero
			minDistances.put(pivNode, 0.0);
			// Uses same order in nodelist and our arrays!
			for (int l = 0; l < na.nodeList.size(); l++) {
				CyNode v = na.nodeList.get(l);
				minDistances.put(v, Math.min(minDistances.get(v), shortestPathSingleSource[l]));
				if (minDistances.get(v) > minDistances.get(pivNode)) {
					pivNode = v;
					pivIndex = l;
				}
			}
		}
	}

	/**
	 * Checks whether the given graph is a path or not.
	 * 
	 * @param na
	 * @param network
	 * @return the root
	 */
	CyNode getRootedPath(NetworkAttributes na, CyNetwork network) {
		CyNode head = null;
		int numberOfNodesWithDeg1 = 0;
		int degree;
		CyEdge e;
		CyNode v;
		CyNode adj;
		HashMap<CyNode, Boolean> visited = new HashMap<CyNode, Boolean>();
		ArrayList<CyNode> neighbors = new ArrayList<CyNode>();
		// in every path there are two nodes with degree 1 and
		// each node has at most degree 2

		HashSet<CyEdge> adjEdges = new HashSet<CyEdge>();
		// /

		// /
		ListIterator<CyNode> it = na.nodeList.listIterator();
		while (it.hasNext()) {
			v = it.next();
			degree = 0;
			visited.put(v, new Boolean(true));
			neighbors.add(v);
			adjEdges.clear();
			adjEdges.addAll(network.getAdjacentEdgeList(v, Type.ANY));
			for (CyEdge ce : adjEdges) {
				CyNode es = ce.getSource();
				CyNode et = ce.getTarget();
				CyNode oppv = (es.equals(v) ? et : es);
				if ((visited.get(oppv) == null)) {
					neighbors.add(oppv);
					visited.put(oppv, new Boolean(true));
					degree++;
				}
			}

			if (degree > 2) {
				neighbors.clear();
				return null;
			}
			if (degree == 1) {
				head = v;
			}
			for (int j = 0; j < neighbors.size(); j++) {
				visited.put(neighbors.get(j), false);
			}
			neighbors.clear();
		}
		return head;
	}

	/**
	 * Normalizes the vector \a x.
	 * 
	 * @param x
	 * @param len
	 * @return
	 */
	double normalize(double[] x, int len) {
		double norm = Math.sqrt(prod(x, x, len));
		if (norm != 0) {
			for (int i = 0; i < len; i++) {
				x[i] /= norm;
			}
		}
		return norm;
	}

	/**
	 * Computes the product of two vectors \a x and \a y.
	 * 
	 * @param x
	 * @param y
	 * @param len
	 * @return vector product
	 */
	double prod(double[] x, double[] y, int len) {
		double result = 0;
		for (int i = 0; i < len; i++) {
			result += x[i] * y[i];
		}
		return result;
	}

	/**
	 * Fills the given \a matrix with random doubles d 0 <= d <= 1.
	 * 
	 * @param matrix
	 */
	void randomize(double[][] matrix) {
		for (int i = 0; i < curPivotNum(); i++) {
			for (int j = 0; j < nAttr.numberOfNodes(); j++) {
				matrix[i][j] = Math.random();
			}
		}
	}

	// Computes the self product of \a d.
	void selfProduct(final double[][] d, double[][] result, int dim1, int dim2) {
		double sum;
		for (int i = 0; i < dim1; i++) {
			for (int j = 0; j <= i; j++) {
				sum = 0;
				for (int k = 0; k < dim2; k++) {
					sum += d[i][k] * d[j][k];
				}
				result[i][j] = sum;
				result[j][i] = sum;
			}
		}
	}

	// Computes the singular value decomposition of matrix \a K.
	void singularValueDecomposition(double[][] pivDistMatrix, double[][] eVecs, double[] eVals) {
		final int l = curPivotNum();
		final int n = nAttr.numberOfNodes();
		double[][] K = new double[l][l];

		// Calculate C^TC
		selfProduct(pivDistMatrix, K, l, n);

		double[][] tmp = new double[DIMENSION_COUNT][l];

		eigenValueDecomposition(K, tmp, eVals, l);

		// Calculate C^Tx
		for (int i = 0; i < DIMENSION_COUNT; i++) {
			eVals[i] = Math.sqrt(eVals[i]);
			for (int j = 0; j < n; j++) { // node j
				eVecs[i][j] = 0;
				for (int k = 0; k < l; k++) { // pivot k
					eVecs[i][j] += pivDistMatrix[k][j] * tmp[i][k];
				}
			}
		}
		for (int i = 0; i < DIMENSION_COUNT; i++) {
			normalize(eVecs[i], curPivotNum());
		}
	}

}
