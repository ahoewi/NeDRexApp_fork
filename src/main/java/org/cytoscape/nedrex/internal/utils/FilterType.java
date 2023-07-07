package org.cytoscape.nedrex.internal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.InteractionType;
import org.cytoscape.nedrex.internal.NodeType;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class FilterType {
	
	public static final String edgeTypeCol = "type";
	public static final String nodeTypeCol = "type";
	
	/**
     * This method gets the CyNetwork and desired NodeType and returns a set of all CyNodes in the network of this type.
     */
	public static Set<CyNode> nodesOfType (CyNetwork network, NodeType nodetype){
		Set<CyNode> nodes = new HashSet<CyNode>();		
		Collection<CyRow> lcr = network.getDefaultNodeTable().getMatchingRows(nodeTypeCol, nodetype.toString());
		for (CyRow cr: lcr) {
			nodes.add(network.getNode(cr.get("SUID", Long.class)));
		}
		return nodes;
	}
	
	/**
     * This method gets the CyNetwork and desired NodeType and returns a list of all CyNodes in the network of this type.
     */
	public static List<CyNode> nodesOfTypeList (CyNetwork network, NodeType nodetype){
		List<CyNode> nodes = new ArrayList<CyNode>();		
		Collection<CyRow> lcr = network.getDefaultNodeTable().getMatchingRows(nodeTypeCol, nodetype.toString());
		for (CyRow cr: lcr) {
			nodes.add(network.getNode(cr.get("SUID", Long.class)));
		}
		return nodes;
	}
	
	public static List<CyNode> nodesInPPI (CyNetwork network){
		List<CyNode> protsInPPI = new ArrayList<CyNode>();		
		
		for(CyNode node : network.getNodeList()) {
			if (network.getRow(node).get("type", String.class).equals(NodeType.Protein.toString()) &&
					FilterType.adjacentEdgesOfType(network, node, InteractionType.protein_protein).size()!=0) {
				protsInPPI.add(node);
			}
		}		
		return protsInPPI;
	}
	
	/**
     * This method gets the CyNetwork and desired InteractionType and returns all CyEdges in the network of this type.
     */
	public static Set<CyEdge> edgesOfType (CyNetwork network, InteractionType edgetype){
		Set<CyEdge> edges = new HashSet<CyEdge>();		
		Collection<CyRow> lcr = network.getDefaultEdgeTable().getMatchingRows(edgeTypeCol, edgetype.toString());
		for (CyRow cr: lcr) {
			edges.add(network.getEdge(cr.get("SUID", Long.class)));
		}
		return edges;
	}
	
	/**
     * This method gets the CyNetwork, CyNode and desired NodeType and returns set of neighbor nodes of this node with the
     * specified NodeType.
     */
	public static Set<CyNode> neighborNodesOfType (CyNetwork network, CyNode node, NodeType nodetype){
		Set<CyNode> nodesOfType = new HashSet<CyNode>();
		Set<CyNode> neighbors = new HashSet<CyNode>(network.getNeighborList(node, CyEdge.Type.ANY));
		for (CyNode n : neighbors) {
			if (network.getRow(n).get(nodeTypeCol, String.class).equals(nodetype.toString())) {
				nodesOfType.add(n);
			}
		}
		return nodesOfType;
	}
	
	/**
     * This method gets the CyNetwork, CyNode and desired NodeType and returns list of neighbor nodes of this node with the
     * specified NodeType.
     */
	
	public static List<CyNode> neighborNodesOfTypeList (CyNetwork network, CyNode node, NodeType nodetype){
		List<CyNode> nodesOfType = new ArrayList<CyNode>();
		Set<CyNode> neighbors = new HashSet<CyNode>(network.getNeighborList(node, CyEdge.Type.ANY));
		for (CyNode n : neighbors) {
			if (network.getRow(n).get(nodeTypeCol, String.class).equals(nodetype.toString())) {
				nodesOfType.add(n);
			}
		}
		return nodesOfType;
	}
	
	/**
     * This method gets the CyNetwork, CyNode and desired InteractionType and returns all adjacent edges of this node with the
     * specified InteractionType.
     */
	public static Set<CyEdge> adjacentEdgesOfType (CyNetwork network, CyNode node, InteractionType edgetype){
		Set<CyEdge> edgesOfType = new HashSet<CyEdge>();
		Set<CyEdge> adjacents = new HashSet<CyEdge>(network.getAdjacentEdgeList(node, CyEdge.Type.ANY));
		for (CyEdge e : adjacents) {
			if (network.getRow(e).get(edgeTypeCol, String.class).equals(edgetype.toString())) {
				edgesOfType.add(e);
			}
		}
		return edgesOfType;
	}
	
	/**
     * This method gets the CyNetwork, list of CyNodes and desired NodeType and returns a filtered list of nodes based on the
     * specified NodeType. It also deselects all the selected nodes which don't have the specified type in the network
     */
	public static Set<CyNode> keepNodesOfType (CyNetwork network, List<CyNode> nodes, NodeType nodeType){
		Set<CyNode> nodesToKeep = new HashSet<CyNode>(nodes);
		Set<CyNode> nodesToRemove = new HashSet<CyNode>();
		for (CyNode n: nodes) {
			if (!network.getRow(n).get("type", String.class).equals(nodeType.toString())) {
				nodesToRemove.add(n);
				network.getRow(n).set("selected", false);
			}			
		}
		nodesToKeep.removeAll(nodesToRemove);
		return nodesToKeep;
	}
	/**
	 * This method gets the CyNetwork and desired NodeType and deselect all the selected nodes which don't have the specified NodeType in the network
	 * @param network
	 * @param nodeType
	 */
	public static void keepSelectedNodesOfType (CyNetwork network, NodeType nodeType) {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
		for (CyNode n: selectedNodes) {
			if (!network.getRow(n).get("type", String.class).equals(nodeType.toString())) {
				network.getRow(n).set("selected", false);
			}			
		}	
	}

}
