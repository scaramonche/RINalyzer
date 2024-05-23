package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Storage class for the results of one run of the analysis. It contains the network identifier, the set of
 * selected nodes, the names of the centrality measures computed and their values.
 * 
 * Important: The names of the centrality measures have to be updated here, when new measures are added.
 * 
 * @author Nadezhda Doncheva
 */
// TODO: [Improve][Analysis] Integrate Z-Score (different font in table, set in selection filter)
public class ResultData {

	/**
	 * Initializes a new (empty) instance of <code>ResultData</code>. Data is filled in during computation.
	 */
	public ResultData() {
		centralitiesData = new HashMap<String, Map<CyNode, Double>>();
		minMeanMaxData = new HashMap<String, Double[]>();
		analysisSettings = new String[11];
		networkTitle = "";
		network = null;
		selected = null;
	}

	/**
	 * Get the analysis options array.
	 * 
	 * @return Array with all analysis options as strings.
	 */
	public String[] getAnalysisSettings() {
		return analysisSettings;
	}

	/**
	 * Return a list of the names of computed centralities.
	 * 
	 * @return List of the names of the computed centralities. Can be empty but not null.
	 */
	public List<String> getComputed() {
		List<String> computed = new ArrayList<String>();
		for (int i = 0; i < Messages.centralities.length; i++) {
			if (centralitiesData.containsKey(Messages.centralities[i])) {
				computed.add(Messages.centralities[i]);
			}
		}
		return computed;
	}

	/**
	 * Return a map containing the computed values for the centrality <code>aCentrName</code>.
	 * 
	 * @param aCentrName
	 *            Name of the centrality measure, whose values have to be returned.
	 * @return Map containing the computed values for the centrality <code>aCentrName</code>. Can't be
	 *         null.
	 */
	public Map<CyNode, Double> getCentralty(String aCentrName) {
		return centralitiesData.get(aCentrName);
	}

	/**
	 * Return an array with the minimum, mean, and maximum value of the centrality measure
	 * <code>aCentrName</code>.
	 * 
	 * @param aCentrName
	 *            Name of the centrality measure.
	 * @return An array with the minimum, mean, and maximum value of the centrality measure
	 *         <code>aCentrName</code>. Can't be null.
	 */
	public Double[] getMinMeanMaxData(String aCentrName) {
		return minMeanMaxData.get(aCentrName);
	}

	/**
	 * Get the smallest value for the centrality measure <code>aCentrName</code>.
	 * 
	 * @param aCentrName
	 *            Name of the centrality measure.
	 * @return The smallest value for the centrality measure <code>aCentrName</code>.
	 */
	public Double getMin(String aCentrName) {
		return minMeanMaxData.get(aCentrName)[0];
	}

	/**
	 * Get the mean value for the centrality measure <code>aCentrName</code>.
	 * 
	 * @param aCentrName
	 *            Name of the centrality measure.
	 * @return The mean value for the centrality measure <code>aCentrName</code>.
	 */
	public Double getMean(String aCentrName) {
		return minMeanMaxData.get(aCentrName)[1];
	}

	/**
	 * Get the biggest value for the centrality measure <code>aCentrName</code>.
	 * 
	 * @param aCentrName
	 *            Name of the centrality measure.
	 * @return The biggest value for the centrality measure <code>aCentrName</code>.
	 */
	public Double getMax(String aCentrName) {
		return minMeanMaxData.get(aCentrName)[2];
	}

	/**
	 * Get the title of the analyzed network.
	 * 
	 * @return Title of the analyzed network.
	 */
	public String getNetworkTitle() {
		return networkTitle;
	}

	/**
	 * Get the id of the analyzed network.
	 * 
	 * @return ID of the analyzed network.
	 */
	public CyNetwork getNetwork() {
		return network;
	}
	
	/**
	 * Get the set of nodes selected for the analysis.
	 * 
	 * @return Set of nodes selected for the analysis.
	 */
	public Set<CyNode> getSelectedNodes() {
		return selected;
	}

	/**
	 * Fill in the <code>analysisOptions</code> array: [Network title, Weight attribute, Multiple edges,
	 * Negative weight, SimToDist, default weight, compute sp, compute cf, compute rw, degree cutoff, exclude
	 * paths]
	 * 
	 * @param netTitle
	 *            network title
	 * @param measuresToCompute
	 *            [compute sp, compute cf, compute rw]
	 * @param weightOpts
	 *            [weightAttr, edgeType, negWeight, simToDist]
	 * @param defWeight
	 *            default weight
	 * @param degreeCutoff
	 *            degree cutoff
	 * @param connComp
	 *            exclude conn comp
	 */
	public void setAnalysisOptions(String netTitle, boolean[] measuresToCompute, String[] weightOpts,
			double defWeight, double degreeCutoff, boolean connComp) {
		analysisSettings[0] = netTitle;
		analysisSettings[1] = String.valueOf(measuresToCompute[0]);
		analysisSettings[2] = String.valueOf(measuresToCompute[1]);
		analysisSettings[3] = String.valueOf(measuresToCompute[2]);
		analysisSettings[4] = weightOpts[0];
		analysisSettings[5] = weightOpts[1];
		analysisSettings[6] = weightOpts[2];
		analysisSettings[7] = weightOpts[3];
		analysisSettings[8] = String.valueOf(defWeight);
		analysisSettings[9] = String.valueOf(degreeCutoff);
		analysisSettings[10] = String.valueOf(connComp);
	}

	/**
	 * Store the computed values of a centrality measure in this instance of <code>ResultData</code> , only
	 * if the map contains some measures. This method also computes the min, mean and max value for the new
	 * data.
	 * 
	 * @param aCentrName
	 *            Name of the computed centrality.
	 * @param aCentrData
	 *            Map with the computed values.
	 */
	public void setCentralty(String aCentrName, Map<CyNode, Double> aCentrData) {
		if (aCentrData != null && aCentrData.size() > 0) {
			compMinMeanMax(aCentrName, aCentrData);
			centralitiesData.put(aCentrName, aCentrData);
		}
	}

	/**
	 * Store the title of the analyzed network.
	 * 
	 * @param aNetworkTitle
	 *            Title of the analyzed network.
	 */
	public void setNetworkTitle(String aNetworkTitle) {
		networkTitle = aNetworkTitle;
	}

	/**
	 * Store the id of the analyzed network.
	 * 
	 * @param aNetworkID
	 *            Id of the analyzed network.
	 */
	public void setNetwork(CyNetwork aNetwork) {
		network = aNetwork;
	}
	
	/**
	 * Store the set of nodes selected for the analysis.
	 * 
	 * @param aSelected
	 *            Set of selected nodes.
	 */
	public void setSelectedCyNodes(Set<CyNode> aSelected) {
		selected = aSelected;
	}

	/**
	 * Compute the minimum, mean and maximum value for this data.
	 * 
	 * @param aCentrName
	 *            Name of the centrality measure.
	 * @param aCentrData
	 *            Map with the computed values of the centrality measure.
	 */
	private void compMinMeanMax(String aCentrName, Map<CyNode, Double> aCentrData) {
		Double[] values = new Double[3];
		Object[] centrValues = (aCentrData.values()).toArray();
		if (centrValues.length > 0) {
			Arrays.sort(centrValues);
			final int size = centrValues.length;
			// save min
			values[0] = (Double) centrValues[0];
			// save max
			values[2] = (Double) centrValues[size - 1];
			// save mean
			double mean = 0.0d;
			for (Object centrValue : centrValues) {
				mean += ((Double) centrValue).doubleValue();
			}
			values[1] = new Double(mean / centrValues.length);
		}
		minMeanMaxData.put(aCentrName, values);
	}

	/**
	 * Map containing for each centrality measure its computed values stored again as a map.
	 */
	private Map<String, Map<CyNode, Double>> centralitiesData;

	/**
	 * Map containing for each centrality measure its array with min, max and mean values.
	 */
	private Map<String, Double[]> minMeanMaxData;

	/**
	 * Network title of the analyzed network.
	 */
	private String networkTitle;

	/**
	 * Network identifier of the analyzed network.
	 */
	private CyNetwork network;

	/**
	 * Set of nodes selected for the analysis.
	 */
	private Set<CyNode> selected;

	/**
	 * Array with all analysis options: [Network title, compute sp, compute cf, compute rw, Weight attribute,
	 * Multiple edges, Negative weight, SimToDist, default weight, degree cutoff, exclude paths]
	 */
	private String[] analysisSettings;
}
