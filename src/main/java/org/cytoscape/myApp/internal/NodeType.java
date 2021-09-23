package org.cytoscape.myApp.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.myApp.internal.exceptions.NodeTypeException;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public enum NodeType {	
	Gene("Gene"),
	Disease("Disorder"),
	Pathway("Pathway"),
	Drug("Drug"),
	Protein("Protein");
	
	private String type;
	
	private NodeType(String type) {
		this.type = type;
	}
	
	@Override public String toString() {
		return this.type;
	}
	
    public static NodeType determine(CyNode node, CyNetwork network) throws NodeTypeException {
    	String nodeTypeCol = "type";
    	CyRow row = network.getRow(node);
    	String nodeType = row.get(nodeTypeCol, String.class);
    	
    	if (nodeType.equals(NodeType.Gene.toString())) {
    		return NodeType.Gene;
    	} else if (nodeType.equals(NodeType.Disease.toString())) {
    		return NodeType.Disease;
    	} else if (nodeType.equals(NodeType.Drug.toString())) {
    		return NodeType.Drug;
    	} else if (nodeType.equals(NodeType.Protein.toString())) {
    		return NodeType.Protein;
    	} else if (nodeType.equals(NodeType.Pathway.toString())) {
    		return NodeType.Pathway;
    	}
    	else {
    		throw new NodeTypeException("Node type " + nodeType + " is unknown.\nPlease make sure the network contains valid node types only!");
    	}
    }
    
    public static NodeType getType(String type) throws NodeTypeException {
    	
    	if (type.equals(NodeType.Gene.toString())) {
    		return NodeType.Gene;
    	} else if (type.equals(NodeType.Disease.toString())) {
    		return NodeType.Disease;
    	} else if (type.equals(NodeType.Drug.toString())) {
    		return NodeType.Drug;
    	} else if (type.equals(NodeType.Protein.toString())) {
    		return NodeType.Protein;
    	} else if (type.equals(NodeType.Pathway.toString())) {
    		return NodeType.Pathway;
    	}
    	else {
    		throw new NodeTypeException("Node type " + type + " is unknown.\nPlease make sure the network contains valid node types only!");
    	}
    }

}
