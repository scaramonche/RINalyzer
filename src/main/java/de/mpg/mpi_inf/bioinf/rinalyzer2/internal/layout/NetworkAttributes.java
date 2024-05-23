package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import java.util.HashMap;
import java.util.ArrayList;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;


/***
 * Stores data necessary for layout computation
 * 
 * @author kklein
 *
 */
public class NetworkAttributes {
	
	public NetworkAttributes(ArrayList<CyNode> nodes, ArrayList<CyEdge> edges) {
		nodeList = nodes;
		edgeList = edges;
		xcord = new HashMap<CyNode, Double>();
		ycord = new HashMap<CyNode, Double>();
		zcord = new HashMap<CyNode, Double>();
		height = new HashMap<CyNode, Double>();
		width = new HashMap<CyNode, Double>();
		ssindex = new HashMap<CyNode, Integer>();
		sssucc = new HashMap<CyNode, CyNode>();
		sspred = new HashMap<CyNode, CyNode>();
		for (CyNode v : nodes) {
			xcord.put(v,  0.0);
			ycord.put(v,  0.0);
			zcord.put(v,  0.0);
			ssindex.put(v,  -1);
			sssucc.put(v, null);
			sspred.put(v, null);
		}
		doubleWeight = new HashMap<CyEdge, Double>();
		
	}
	private HashMap<CyNode, Double> xcord;
	private HashMap<CyNode, Double> ycord;
	private HashMap<CyNode, Double> zcord;
	private HashMap<CyNode, Double> width;
	private HashMap<CyNode, Double> height;
	private HashMap<CyEdge, Double> doubleWeight;
	private HashMap<CyNode, Integer> ssindex; //index of secondary structure element
	// Not efficient or generic, but simple
	private HashMap<CyNode, CyNode> sspred; // Predecessor in same ss
	private HashMap<CyNode, CyNode> sssucc; // Successor in same ss
	public ArrayList<CyNode> nodeList;
	public ArrayList<CyEdge> edgeList;
	
	HashMap<CyNode,Double> getXCoordinate() {return this.xcord;}
	HashMap<CyNode,Double> getYCoordinate() {return this.ycord;}
	HashMap<CyNode,Double> getZCoordinate() {return this.zcord;}
	public double x(CyNode v) {return this.xcord.get(v);}
	public double y(CyNode v) {return this.ycord.get(v);}
	public double z(CyNode v) {return this.zcord.get(v);}
	public double h(CyNode v) {return this.height.get(v);}
	public double w(CyNode v) {return this.width.get(v);}
	int ssindex(CyNode v) {return this.ssindex.get(v);}
	CyNode ssPred(CyNode v) {return this.sspred.get(v);}
	CyNode ssSucc(CyNode v) {return this.sssucc.get(v);}
	//Don't use that more than once per node, store the result
	double radius(CyNode v) {
		return Math.sqrt(this.width.get(v)*this.width.get(v) + this.height.get(v)*this.height.get(v))/2.0;
	}
	//Versions using an index (NA is seen as a static network structure)
	public double indx(int ind) {return this.xcord.get(nodeList.get(ind));}
	public double indy(int ind) {return this.ycord.get(nodeList.get(ind));}
	public double indz(int ind) {return this.zcord.get(nodeList.get(ind));}
	int indssindex(int ind) {return this.ssindex.get(nodeList.get(ind));}
	
	public void x(CyNode v, double d) {this.xcord.put(v, new Double(d));}
	public void y(CyNode v, double d) {this.ycord.put(v, d);}
	public void z(CyNode v, double d) {this.zcord.put(v, d);}
	public void w(CyNode v, double d) {this.width.put(v, d);}
	public void h(CyNode v, double d) {this.height.put(v, d);}
	void ssindex(CyNode v, int i) {this.ssindex.put(v,i);}
	void ssSucc(CyNode v, CyNode s) {this.sssucc.put(v,  s);}
	void ssPred(CyNode v, CyNode p) {this.sspred.put(v,  p);}
	//Versions using an index
	public void indx(int ind, double d) {this.xcord.put(nodeList.get(ind), new Double(d));}
	public void indy(int ind, double d) {this.ycord.put(nodeList.get(ind), d);}
	public void indz(int ind, double d) {this.zcord.put(nodeList.get(ind), d);}
	void indssindex(int ind, int i) {this.ssindex.put(nodeList.get(ind), i);}
	void indssSucc(int v, CyNode s) {this.sssucc.put(nodeList.get(v),  s);}
	void indssPred(int v, CyNode p) {this.sspred.put(nodeList.get(v),  p);}
	public int numberOfNodes() {return nodeList.size();}
	public double weight(CyEdge e) {return doubleWeight.get(e);}
}
