package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities;

import java.util.Comparator;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class CyNodeDoubleComparator implements Comparator<Map.Entry<CyNode, Double>> {

	private CyNetwork network;

	public CyNodeDoubleComparator(CyNetwork network) {
		this.network = network;
	}

	public int compare(Map.Entry<CyNode, Double> e1, Map.Entry<CyNode, Double> e2) {
		return CyUtils.getCyName(network, e1.getKey()).compareTo(
				CyUtils.getCyName(network, e2.getKey()));
	}
}
