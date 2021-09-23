package org.cytoscape.myApp.internal.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.myApp.internal.InteractionType;
import org.cytoscape.myApp.internal.Link;
import org.cytoscape.myApp.internal.UndirectedNetwork;
import org.cytoscape.myApp.internal.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class GraphUtils {
	
	/**
	 * This method gets the terminal/seed CyNodes and the CyNetwork and returns the seeds as vertices and also
	 * convert CyNetwork to graph the graph. We use this function when our network is simply a PPI network and 
	 * not a heterogeneous network
	 */
	public static List <Vertex> cyNetworkToNetwork(Graph<Vertex,Link> graph, CyNetwork network, List<CyNode> proteins, List<CyNode> terminalCyNodes, Boolean weightsSet, Boolean penalized) {
		List <Vertex> terminalNodes = new ArrayList<Vertex> ();
		Map<Long,Vertex> nodeMap = new HashMap<>();
		for(CyNode cynode : proteins) {
			Vertex node = new Vertex(Long.toString(cynode.getSUID()));
			if (terminalCyNodes.contains(cynode)) {
				terminalNodes.add(node);
			}
			nodeMap.put(cynode.getSUID(), node);
			graph.addVertex(node);
		}
		double weight = 0.0;

		for(CyEdge cyedge : network.getEdgeList()) {
			// to exclude selfLoops from PPI
			boolean selfLoop = false;
			if (cyedge.getSource().equals(cyedge.getTarget())) {
				selfLoop = true;
			}        	
			if (!selfLoop) {
				Vertex source = nodeMap.get(cyedge.getSource().getSUID());
				Vertex target = nodeMap.get(cyedge.getTarget().getSUID());
				if (weightsSet && penalized) {
					weight = network.getRow(cyedge).get("weight", Double.class);
				}
				else if (!weightsSet && penalized) {
					weight = 1.0;
				}
				else if (!penalized) {
					weight = 1.0;
				}

				//else weight = 1.0;
				Link edge = new Link(source, target, cyedge.getSUID(), weight);
				graph.addEdge(source, target, edge);
				graph.setEdgeWeight(edge, weight);
			}
		}
		return terminalNodes;
	}
	
	
	/// Note: this creates the PPI network without isolated proteins (proteins without any neighbors),...
	/**
     * This method gets the terminal/seed CyNodes and the CyNetwork and returns the seeds as vertices and also
     * convert CyNetwork to graph without isolated node, only includes the one participating in p-p interacrtions. 
     * Specifically we use this function when we have PPI network as part of a larger heterogeneous network. 
     * Set of isolated proteins will be updated as well. Also return the seeds vertex set which are not isolated.
     */
	public static List <Vertex> cyNetworkToPPINetNoIsolate(Graph<Vertex,Link> graph, CyNetwork network, List<CyNode> proteins, List<CyNode> terminalCyNodes, Boolean weightsSet, Boolean penalized) {
		List <Vertex> terminalNodes = new ArrayList<Vertex> ();
		Map<Long,Vertex> nodeMap = new HashMap<>();
		
		for(CyNode cynode : proteins) {
			//if (FilterType.adjacentEdgesOfType(network, cynode, InteractionType.protein_protein).size()!=0) {
				Vertex node = new Vertex(Long.toString(cynode.getSUID()));
				if (terminalCyNodes.contains(cynode)) {
					terminalNodes.add(node);
				}
				nodeMap.put(cynode.getSUID(), node);
				graph.addVertex(node);
			//}
		}

		double weight = 0.0;

		for(CyEdge cyedge : network.getEdgeList()) {
			// to exclude selfLoops from PPI
			boolean selfLoop = false;
			if (cyedge.getSource().equals(cyedge.getTarget())) {
				selfLoop = true;
			}
			if (network.getRow(cyedge).get("type", String.class).equals(InteractionType.protein_protein.toString()) && !selfLoop) {
				Vertex source = nodeMap.get(cyedge.getSource().getSUID());
				Vertex target = nodeMap.get(cyedge.getTarget().getSUID());
				if (weightsSet && penalized) {
					weight = network.getRow(cyedge).get("weight", Double.class);
				}
				else if (!weightsSet && penalized) {
					weight = 1.0;
				}
				else if (!penalized) {
					weight = 1.0;
				}
				//else weight = 1.0;
				Link edge = new Link(source, target, cyedge.getSUID(), weight);
				graph.addEdge(source, target, edge);
				graph.setEdgeWeight(edge, weight);
			}			
		}
		return terminalNodes;
	}
	
	/**
     * This method gets the terminal/seed nodes and the graph and updates the terminal/seed nodes and only
     * keeps the ones in LCC.
     */
	
	public static void inLCC (List <Vertex> terminalNodes, UndirectedNetwork graph) {
		ConnectivityInspector<Vertex, Link> cIns = new ConnectivityInspector<Vertex, Link>(graph);
	    List<Set<Vertex>> cnctdcomps = cIns.connectedSets();
	    //System.out.println("The number of connected components: " + cnctdcomps.size());
	    int maxcopmSize = 0;
	    Set<Vertex> maxCnctdComp = new HashSet<Vertex>();
	    for (Set<Vertex> s: cnctdcomps) {
	    	if (s.size() > maxcopmSize) {
	    		maxcopmSize = s.size();
	    		maxCnctdComp = s;
	    	}
	    }
	   // System.out.println("The largest connected component size: " + maxCnctdComp.size());
	    terminalNodes.retainAll(maxCnctdComp);
	}
	
	public static void setEdgeWeight(UndirectedNetwork graph, CyNetwork network, Double hubPenalty, double totalAvDeg) {
		if (network.getDefaultEdgeTable().getColumn("weight") == null) {
			network.getDefaultEdgeTable().createColumn("weight", Double.class, false);
		}
		for (Link l: graph.edgeSet()) {
			double eAvDeg = ((double)(graph.degreeOf(l.getSource()) + graph.degreeOf(l.getTarget())))/2;
			double w = (1-hubPenalty)*totalAvDeg+(hubPenalty*eAvDeg);
			Double weight = BigDecimal.valueOf(w).setScale(5, RoundingMode.HALF_UP).doubleValue(); 
			network.getRow(network.getEdge(l.getSuid())).set("weight", weight);
			graph.setEdgeWeight(l, weight);
		}		
	}
	
	public static double getAvDeg(UndirectedNetwork graph) {
		double totalAvDeg = 0;
		double sumDeg = 0;
		for (Vertex v: graph.vertexSet()) {
			sumDeg += graph.degreeOf(v);
		}
		totalAvDeg = sumDeg/(double)graph.vertexSet().size();
		return totalAvDeg;
	}
	
	

}
