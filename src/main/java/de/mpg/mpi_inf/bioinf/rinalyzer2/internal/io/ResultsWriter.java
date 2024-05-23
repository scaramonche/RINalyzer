package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality.ResultData;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyNodeDoubleComparator;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.UtilsIO;

/**
 * Class for saving the analysis results in a file. Each file contains the network ID of the
 * analyzed network, the set of nodes that have been selected for the analysis, and the name and
 * computed values of one or more centrality measures.
 * 
 * @author Nadezhda Doncheva
 * 
 */
public class ResultsWriter {

	/**
	 * Save the data of all centrality measures in a file <code>aFilename</code> .
	 * 
	 * @param aResultData
	 *            Computed centrality measures data.
	 * @param aFile
	 *            The file to save data to.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public static void save(ResultData aResultData, File aFile) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(aFile);

			final List<String> computed = aResultData.getComputed();
			writer.write(aResultData.getNetworkTitle() + "\n");
			writer.write(Messages.LOG_SELECTEDNODES
					+ nodeSetToString(aResultData.getSelectedNodes(), aResultData.getNetwork())
					+ "\n");
			for (final String comp : computed) {
				writer.write(comp + "\n");
				writer.write(getCyNodeCentToString(aResultData.getCentralty(comp),
						aResultData.getNetwork()));
			}
			writer.close();
		} catch (IOException ex) {
			UtilsIO.closeStream(writer);
			throw ex;
		}
	}

	/**
	 * Save the computed values of one centrality measure in a file <code>aFilename</code>.
	 * 
	 * @param aCentData
	 *            Computed centrality values.
	 * @param aComputed
	 *            Name of the computed centrality measure.
	 * @param aNetworkID
	 *            Identifier of the analyzed network.
	 * @param aSelected
	 *            Set of the selected nodes for the analysis.
	 * @param aFile
	 *            The file to save the data to.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public static void save(Map<CyNode, Double> aCentData, String aComputed, CyNetwork aNetwork,
			Set<CyNode> aSelected, File aFile) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(aFile);
			writer.write(aNetwork.getRow(aNetwork).get(CyNetwork.NAME, String.class) + "\n");
			writer.write(Messages.LOG_SELECTEDNODES + nodeSetToString(aSelected, aNetwork) + "\n");
			writer.write(aComputed + "\n");
			writer.write(getCyNodeCentToString(aCentData, aNetwork));
			writer.close();
		} catch (IOException ex) {
			UtilsIO.closeStream(writer);
			throw ex;
		}
	}

	/**
	 * Get a string representation of all nodes and their corresponding centrality values stored in
	 * <code>aCentData</code>.
	 * 
	 * @param aCentData
	 *            Map of nodes and their centrality values.
	 * @return String containing a list of the nodes ant their centrality values (tab-separated and
	 *         10-digit doubles).
	 */
	public static String getCyNodeCentToString(Map<CyNode, Double> aCentData, CyNetwork aNetwork) {
		final DecimalFormat fiveDigit = new DecimalFormat("#,##0.0#########");
		final StringBuilder sb = new StringBuilder();
		List<Map.Entry<CyNode, Double>> mapEntries = getSortedCentEntries(aCentData, aNetwork);
		for (final Map.Entry<CyNode, Double> entry : mapEntries) {
			sb.append(aNetwork.getRow(entry.getKey()).get(CyNetwork.NAME, String.class) + "\t"
					+ fiveDigit.format(entry.getValue()) + "\n");
		}
		return sb.toString();
	}

	/**
	 * Create a String representation of a set of nodes. Each node is represented by its identifier
	 * and the nodes are separated by comma. Used to save the set of selected nodes for analysis in
	 * a file.
	 * 
	 * @param aNodeSet
	 *            Set of nodes to be written as one String.
	 * @return String representation of the <code>aNodeSet</code>, where each node is written as its
	 *         identifier and is separated by a comma from the others.
	 */
	public static String nodeSetToString(Set<CyNode> aNodeSet, CyNetwork aNetwork) {
		String nodeNames = "";
		if (aNodeSet.size() > 0) {
			for (final CyNode n : aNodeSet) {
				nodeNames += CyUtils.getCyName(aNetwork, n) + ", ";
			}
			return nodeNames.substring(0, nodeNames.length() - 2);
		}
		return nodeNames;
	}

	/**
	 * Get a sorted list of the map entries. Sorted according to the keys, i.e. to the node
	 * identifiers.
	 * 
	 * @param aCentData
	 *            Map of nodes and corresponding double values
	 * @return A list of map entries sorted according to the node identifiers in the map.
	 */
	public static List<Map.Entry<CyNode, Double>> getSortedCentEntries(
			Map<CyNode, Double> aCentData, CyNetwork aNetwork) {
		List<Map.Entry<CyNode, Double>> mapEntries = new ArrayList<Map.Entry<CyNode, Double>>(
				aCentData.entrySet());
		Collections.sort(mapEntries, new CyNodeDoubleComparator(aNetwork));
		return mapEntries;
	}

}
