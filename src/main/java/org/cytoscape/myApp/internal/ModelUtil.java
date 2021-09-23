package org.cytoscape.myApp.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ModelUtil {
	
	public static final String NONEATTRIBUTE = "--None--";
//	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public static CyNetwork getNetworkWithName(RepoApplication app, String networkName) {
		// Unfortunately Cytoscape doesn't provide an easy way to get at a network based on its name!!
		for (CyNetwork network: app.getNetworkSet()) {
			if (getNetworkName(network).equals(networkName))
				return network;
		}
		return null;
	}
	
	public static String getNetworkName(CyNetwork network) {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
	public static Map<CyNode, CyNode> getMappedCyNodes(RepoApplication app, CyNetwork mapFromNet, CyNetwork mapToNet, Set<CyNode> nodeSet){
		Map<CyNode, CyNode> nodesMap = new HashMap<CyNode, CyNode>();
		for (CyNode n: nodeSet) {
			nodesMap.put(n, getNodeWithName(app, mapToNet, getNodeName(n, mapFromNet)));
		}
		return nodesMap;
	}
	
	public static ListSingleSelection<String> getNodeAttributeList(RepoApplication app, ListSingleSelection<String> attribute) {
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListSingleSelection<String>();

		List<String> attributeArray = getAllAttributes(network, network.getDefaultNodeTable());
		if (attributeArray.size() > 0){
			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
				try {
					newAttribute.setSelectedValue(attribute.getSelectedValue());
				} catch (IllegalArgumentException e) {
					newAttribute.setSelectedValue(attributeArray.get(0));
				}
			} else
				newAttribute.setSelectedValue(attributeArray.get(0));
			
			return newAttribute;
		}
		return new ListSingleSelection<String>("--None--");
	}


	public static CyNode getNodeWithName (RepoApplication app, CyNetwork network, String nodeName) {
		for (CyNode node: network.getNodeList()) {
			if (getNodeName(node,network).equals(nodeName))
				return node;
		}
		return null;
	}
	
	/**
	 * A method that returns the corresponding set of CyNodes for the given set of node names.
	 * It only returns those nodes that can be mapped to the network and excludes the ones that cannot be found in the network.
	 * @param network The CyNetwork to look for the corresponding CyNodes
	 * @param nodeNameSet The input set of node names
	 * @return The corresponding set of CyNodes
	 */
	public static Set<CyNode> getNodeSetWithName (CyNetwork network, Set<String> nodeNameSet) {		
		Set<CyNode> nodeSet = new HashSet<CyNode>();
		for (CyNode node: network.getNodeList()) {
			if (nodeNameSet.contains(getNodeName(node,network))) {
				nodeSet.add(node);
			}
		}
		return nodeSet;
	}

	
	public static String getNodeName (CyNode node, CyNetwork network) {
		return network.getRow(node).get("name", String.class);
	}
	
	/**
	 * A method to return a map for the input set of CyNodes (as values) and their correspondig node names in the given network (as keys)
	 * @param network
	 * @param nodes
	 * @return
	 */
	public static Map<String, CyNode> getNodeNameMap (CyNetwork network, Set<CyNode> nodes){
		Map<String, CyNode> nodeNameMap = new HashMap<String, CyNode>();
		for (CyNode n: nodes) {
			nodeNameMap.put(network.getRow(n).get(CyNetwork.NAME, String.class), n);
		}
		return nodeNameMap;
	}
	

	public static CyNode getNodeWithAttr (RepoApplication app, CyNetwork network, String attrCol, String attr) {
		for (CyNode node: network.getNodeList()) {
			if (getNodeAttr(node,network, attrCol).equals(attr))
				return node;
		}
		return null;
	}
	
	public static String getNodeAttr (CyNode node, CyNetwork network, String attrCol) {
		return network.getRow(node).get(attrCol, String.class);
	}
	
	public static String getNodeNameWithAttr (RepoApplication app, CyNetwork network, String attrCol, String attr) {
		for (CyNode node: network.getNodeList()) {
			if (getNodeAttr(node,network, attrCol).equals(attr)) {
				return getNodeName(node, network);
			}
		}
		return null;
	}
	
	public static CyEdge getEdgeWithName (RepoApplication app, CyNetwork network, String edgeName) {
		for (CyEdge edge: network.getEdgeList()) {
			if (getEdgeName(edge,network).equals(edgeName))
				return edge;
		}
		return null;
	}
	
	public static String getEdgeName (CyEdge edge, CyNetwork network) {
		return network.getRow(edge).get("name", String.class);
	}
	
	/**
	 * A method that returns all the edges between set of input nodes in the network. Useful for creation of induced subnetworks 
	 * @param network
	 * @param nodes
	 * @return set of CyEdges between the input CyNodes
	 */
	public static Set<CyEdge> getEdgesBetweenNodes(CyNetwork network, Set<CyNode> nodes){
		Set <CyEdge> interEdges = new HashSet<CyEdge> ();
		for (CyNode cn: nodes) {
			for (CyNode cnn: nodes) {
				if (!cn.equals(cnn)) {
					interEdges.addAll(network.getConnectingEdgeList(cn, cnn, CyEdge.Type.ANY));
					interEdges.addAll(network.getConnectingEdgeList(cnn, cn, CyEdge.Type.ANY));
				}
			}
		}
		return interEdges;		
	}
	
	
	
///////////////// to get the edge attribute of type Integer, Double, Long
	public static ListSingleSelection<String> updateEdgeAttributeList(RepoApplication app, ListSingleSelection<String> attribute) {
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListSingleSelection<String>();

		List<String> attributeArray = getAllAttributes(network, network.getDefaultEdgeTable());
		if (attributeArray.size() > 0){
			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
				try {
					newAttribute.setSelectedValue(attribute.getSelectedValue());
				} catch (IllegalArgumentException e) {
					newAttribute.setSelectedValue(attributeArray.get(0));
				}
			} else
				newAttribute.setSelectedValue(attributeArray.get(0));
			
			return newAttribute;
		}
		return new ListSingleSelection<String>("--None--");
	}
	
	public static ListMultipleSelection<String> updateEdgeAttributeMultiList(RepoApplication app, ListMultipleSelection<String> attribute) {
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListMultipleSelection<String>();

		List<String> attributeArray = getAllAttributes(network, network.getDefaultEdgeTable());
		if (attributeArray.size() > 0){
			ListMultipleSelection<String> newAttribute = new ListMultipleSelection<String>(attributeArray);	

			if (attribute != null && attributeArray.contains(attribute.getSelectedValues())) {
				try {
					newAttribute.setSelectedValues(attribute.getSelectedValues());
				} catch (IllegalArgumentException e) {
					newAttribute.setSelectedValues(attributeArray);
				}
			} else
				newAttribute.setSelectedValues(attributeArray);
			
			return newAttribute;
		}
		return new ListMultipleSelection<String>("--None--");
	}
	
	private static List<String> getAllAttributes(CyNetwork network, CyTable table) {
		String[] attributeArray = new String[1];
		// Create the list by combining node and edge attributes into a single list?!! our CyTable here is a DefaultEdgeTable, didn't include any nodeTable!
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesList(attributeList, table);
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return Arrays.asList(attrArray);
	}
	
	private static void getAttributesList(List<String>attributeList, CyTable attributes) {
		if (attributes == null)
			return;

//		Collection<CyColumn> names = attributes.getColumns();
//		java.util.Iterator<CyColumn> itr = names.iterator();
		for (CyColumn column: attributes.getColumns()) {
			if (column.getType() == Double.class ||
					column.getType() == Integer.class || column.getType() == Long.class) {
				if (column.getName() != "SUID")
					attributeList.add(column.getName());
			}
		}
	}
	
///////////////// to get the edge attribute of type String
	public static ListSingleSelection<String> updateEdgeAttributeListSt(RepoApplication app, ListSingleSelection<String> attribute) {
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListSingleSelection<String>();

		List<String> attributeArray = getAllAttributesSt(network, network.getDefaultEdgeTable());
		if (attributeArray.size() > 0){
			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
				try {
					newAttribute.setSelectedValue(attribute.getSelectedValue());
				} catch (IllegalArgumentException e) {
					newAttribute.setSelectedValue(attributeArray.get(0));
				}
			} else
				newAttribute.setSelectedValue(attributeArray.get(0));
			
			return newAttribute;
		}
		return new ListSingleSelection<String>("--None--");
	}
	
	private static List<String> getAllAttributesSt(CyNetwork network, CyTable table) {
		String[] attributeArray = new String[1];
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesListSt(attributeList, table);
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return Arrays.asList(attrArray);
	}
	
	private static void getAttributesListSt(List<String>attributeList, CyTable attributes) {
		if (attributes == null)
			return;

		for (CyColumn column: attributes.getColumns()) {
			if (column.getType() == String.class & column.getName() != "name" & column.getName() != "shared name" & column.getName() != "shared interaction") {
				attributeList.add(column.getName());
			}
		}
	}
////////////////
	

///////////////// to get the Node attribute of type Integer, Double, Long
	public static ListSingleSelection<String> updateNodeAttributeList(RepoApplication app, ListSingleSelection<String> attribute) {
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListSingleSelection<String>();

		List<String> attributeArray = getAllNodeAttributes(network, network.getDefaultNodeTable());
		if (attributeArray.size() > 0){
			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
				try {
					newAttribute.setSelectedValue(attribute.getSelectedValue());
				} catch (IllegalArgumentException e) {
					newAttribute.setSelectedValue(attributeArray.get(0));
				}
			} else
				newAttribute.setSelectedValue(attributeArray.get(0));

			return newAttribute;
		}
		return new ListSingleSelection<String>("--None--");
	}

	private static List<String> getAllNodeAttributes(CyNetwork network, CyTable table) {
		String[] attributeArray = new String[1];
		// Create the list by combining node and edge attributes into a single list?!! our CyTable here is a DefaultEdgeTable, didn't include any nodeTable!
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesList(attributeList, table);
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return Arrays.asList(attrArray);
	}

/*	private static void getAttributesList(List<String>attributeList, CyTable attributes) {
		if (attributes == null)
			return;

		//Collection<CyColumn> names = attributes.getColumns();
		//java.util.Iterator<CyColumn> itr = names.iterator();
		for (CyColumn column: attributes.getColumns()) {
			if (column.getType() == Double.class ||
					column.getType() == Integer.class || column.getType() == Long.class) {
				if (column.getName() != "SUID")
					attributeList.add(column.getName());
			}
		}
	}*/
	
	
	///////////////// to get the node attribute of type String
	public static ListSingleSelection<String> updateNodeAttributeListSt(RepoApplication app, ListSingleSelection<String> attribute) {
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListSingleSelection<String>();

		List<String> attributeArray = getAllNodeAttributesSt(network, network.getDefaultNodeTable());
		if (attributeArray.size() > 0){
			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
				try {
					newAttribute.setSelectedValue(attribute.getSelectedValue());
				} catch (IllegalArgumentException e) {
					newAttribute.setSelectedValue(attributeArray.get(0));
				}
			} else
				newAttribute.setSelectedValue(attributeArray.get(0));

			return newAttribute;
		}
		return new ListSingleSelection<String>("--None--");
	}

	private static List<String> getAllNodeAttributesSt(CyNetwork network, CyTable table) {
		String[] attributeArray = new String[1];
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesNodeListSt(attributeList, table);
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return Arrays.asList(attrArray);
	}

	/*private static void getAttributesNodeListSt(List<String>attributeList, CyTable attributes) {
		if (attributes == null)
			return;

		for (CyColumn column: attributes.getColumns()) {
			if (column.getType() == String.class & column.getName() != "name" & column.getName() != "shared name") {
				attributeList.add(column.getName());
			}
		}
	}*/
	
	private static void getAttributesNodeListSt(List<String>attributeList, CyTable attributes) {
		if (attributes == null)
			return;

		for (CyColumn column: attributes.getColumns()) {
			if (column.getType() == String.class) {
				attributeList.add(column.getName());
			}
		}
	}
	
	////////////////
	public static ListSingleSelection<String> updateNetworksList(RepoApplication app, ListSingleSelection<String> sourceNets) {
		
		// get names of all networks
		Set <CyNetwork> netSet = app.getNetworkSet();
		Set <String> netSetNames = new HashSet<String>();
		for (CyNetwork n: netSet) {
			//n.getDefaultNetworkTable().getAllRows().get(0).get("name", String.class);
			// 2 different ways of getting the name of a network:
			//netSetNames.add(n.getDefaultNetworkTable().getAllRows().get(0).get("name", String.class));
			netSetNames.add(n.getRow(n).get(CyNetwork.NAME, String.class));
		}
		List <String> netListNames = new ArrayList<String>();
		netListNames.add(NONEATTRIBUTE);
		for (String s: netSetNames) {
			netListNames.add(s);
		}
		String[] netArray = new String[1];
		String[] netNamesArray = netListNames.toArray(netArray);
		if (netNamesArray.length > 1) 
			Arrays.sort(netNamesArray);
		netListNames = Arrays.asList(netNamesArray);
		
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListSingleSelection<String>();

		//List<String> attributeArray = getAllAttributes(network, network.getDefaultEdgeTable());
		if (netListNames.size() > 0){
			ListSingleSelection<String> newSourceNets = new ListSingleSelection<String>(netListNames);	
			if (sourceNets != null && netListNames.contains(sourceNets.getSelectedValue())) {
				try {
					newSourceNets.setSelectedValue(sourceNets.getSelectedValue());
				} catch (IllegalArgumentException e) {
					newSourceNets.setSelectedValue(netListNames.get(0));
				}
			} else
				newSourceNets.setSelectedValue(netListNames.get(0));
			
			return newSourceNets;
		}
		return new ListSingleSelection<String>("--None--");
	}
	
	public static Long getNetworkSUID (RepoApplication app, String networkName) {
		Set <CyNetwork> netSet = app.getNetworkSet();
		Long suid = new Long(1);
		for (CyNetwork cn: netSet) {
			if(networkName.equals(cn.getRow(cn).get(CyNetwork.NAME, String.class))) {
				suid = cn.getDefaultNetworkTable().getAllRows().get(0).get("SUID", Long.class);
			}	
		}
		return suid;
	}
	
	/*public static ListSingleSelection<String> updateProjectionsList(RepoApplication app, ListSingleSelection<String> projectionsList){
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ListSingleSelection<String>();
		
		List<ProjectionType> lpUpdated = ProjectionType.listProjections();
		lpUpdated.remove(ProjectionType.determine(network));
		List <String> avProjections = new ArrayList <String>();
		for (ProjectionType p: lpUpdated) {
			avProjections.add(p.toString());
		}
		
		String[] projArray = new String[1];
		String[] projNamesArray = avProjections.toArray(projArray);
		if (projNamesArray.length > 1) 
			Arrays.sort(projNamesArray);
		avProjections = Arrays.asList(projNamesArray);
		
		if (avProjections.size() > 0){
			ListSingleSelection<String> newProjectionsList = new ListSingleSelection<String>(avProjections);	
			if (projectionsList != null && avProjections.contains(projectionsList.getSelectedValue())) {
				try {
					newProjectionsList.setSelectedValue(projectionsList.getSelectedValue());
				} catch (IllegalArgumentException e) {
					newProjectionsList.setSelectedValue(avProjections.get(0));
				}
			} else
				newProjectionsList.setSelectedValue(avProjections.get(0));
			
			return newProjectionsList;
		}
		return new ListSingleSelection<String>("--None--");	
	}*/
	
	public static BoundedDouble updateBounds(RepoApplication app) {
		//BoundedDouble edgeCutOff = new BoundedDouble(0.0, 2.5, 5.0, false, false);
		BoundedDouble edgeCutOff = new BoundedDouble(0.0, 0.5, 1.0, false, false);
		return edgeCutOff;
	}
	
/*	public static BoundedDouble updateBounds(RepoApplication app, BoundedDouble edgeCutOff, ListSingleSelection<String> attribute) {
		if (attribute == null || attribute.getSelectedValue().equals("--None--")) {
			// System.out.println("Setting bounds to: "+min+","+max);
			edgeCutOff.setBounds(0.0, 10.0);
			//edgeCutOff.setValue(5.0);
			return edgeCutOff;
		}
		Class <?> typeCls = app.getCurrentNetwork().getDefaultEdgeTable().getColumn(attribute.getSelectedValue()).getType();
		List<Double> allVals = app.getCurrentNetwork().getDefaultEdgeTable().getColumn(attribute.getSelectedValue()).getValues(Double.class);
		Double bndMax = Collections.max(allVals);
		Double bndMin = Collections.min(allVals);
		edgeCutOff.setBounds(bndMin, bndMax);
		return edgeCutOff;
				
	}*/
	

	public static BoundedDouble updateCutoffThreshold(RepoApplication app) {
		BoundedDouble cutoff = new BoundedDouble(0.0, 0.5, 1.0, false, false);
		return cutoff;
	}

	public static BoundedInteger updateExpressionPercentile(RepoApplication app) {
		BoundedInteger percentile = new BoundedInteger(1, 75, 100, false, false);
		return percentile;
	}
	
	public static CyTable getCurrentNodes(RepoApplication app){

//		CyTableFactory tableFactory;
//		CyTable geneNodesTable = tableFactory.createTable("Gene nodes Table", "111", String.class, true, true); CyTable();
		
//		for (CyNode node : geneNodesTable.getAllRows()) {
			
//		}
		return app.getCurrentNetwork().getDefaultNodeTable();
	}
	
	public static List<String> getAllNodeAttributes(RepoApplication app) {
				
		// Create the list of node in a list
		List<String> attributeList = new ArrayList<String>();
		
		CyNetwork network = app.getCurrentNetwork();
		if (network == null)
			return new ArrayList(); //empty list
		
		for (CyColumn column: network.getDefaultNodeTable().getColumns()) {
			attributeList.add(column.getName());
//			if (column.getType() == Double.class ||
//					column.getType() == Integer.class || column.getType() == Long.class) {
//				if (column.getName() != "SUID")
//					attributeList.add(column.getName());
			}
//		}

//		List<String> attributeArray = getAllAttributes(network, network.getDefaultEdgeTable());
//		if (attributeArray.size() > 0){
//			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
//			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
//				try {
//					newAttribute.setSelectedValue(attribute.getSelectedValue());
//				} catch (IllegalArgumentException e) {
//					newAttribute.setSelectedValue(attributeArray.get(0));
//				}
//			} else
//				newAttribute.setSelectedValue(attributeArray.get(0));
//			
//			return newAttribute;
//		}
//		
//		attributeList.add(NONEATTRIBUTE);
//		getAttributesList(attributeList, table);
//		String[] attrArray = attributeList.toArray(attributeArray);
//		if (attrArray.length > 1) 
//			Arrays.sort(attrArray);
//		return Arrays.asList(attrArray);
		return attributeList;
	}

}	