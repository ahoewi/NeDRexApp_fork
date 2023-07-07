package org.cytoscape.nedrex.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.nedrex.internal.utils.FilterType;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ModulFunctions {
	
	public static void bridgingScore (CyNetwork newNet, Graph<Vertex,Link> graph, List<Vertex> nodes) {
		
		CyTable localNewNodeTable = newNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		String bridgingScorecolName = "Bridging_Centrality";
		String egocentricbtw = "egocentric_betweenness";
		
		if(localNewNodeTable.getColumn(bridgingScorecolName) == null) {
			localNewNodeTable.createColumn(bridgingScorecolName, Double.class, false);
		}
		if (localNewNodeTable.getColumn(egocentricbtw) == null) {
			localNewNodeTable.createColumn(egocentricbtw, Double.class, false);
		}
		
		for (Vertex v: nodes) {
			double sigRecDegNeigh = 0;
			Set<Vertex> neighbs = Graphs.neighborSetOf(graph, v);
			for (Vertex i: neighbs) {
				double deg = Double.valueOf(graph.degreeOf(i));
				sigRecDegNeigh = sigRecDegNeigh + (1 / deg);
		    }
			double bc = (1/Double.valueOf(graph.degreeOf(v)))/sigRecDegNeigh;
			int egobtwns = 0;
			int de = 0;
			for (Vertex i: neighbs) {
				for (Vertex j:neighbs) {
					if (!graph.containsEdge(i, j)) {
						egobtwns = egobtwns +1;
					}
					else if (graph.containsEdge(i, j)) {
						de++;
					}						
				}
			}
			int allp = de + egobtwns;
			double egobtwns_normAP =0;
		    if (allp != 0) {
		    	egobtwns_normAP = egobtwns / Double.valueOf(allp);
		    }
		    double brdgCntr = egobtwns_normAP * bc;
		    localNewNodeTable.getRow(Long.parseLong(v.toString())).set(egocentricbtw, egobtwns_normAP);
		    localNewNodeTable.getRow(Long.parseLong(v.toString())).set(bridgingScorecolName, brdgCntr);

		}
	}
	
	public static List<CyEdge> firstNeighPP (CyNetwork network, Set<CyNode> selCyNodes, Set<CyNode> setNeighP) {
		
		CyTable nodeTable = network.getDefaultNodeTable();
		
		List<CyNode> lisNeigh = new ArrayList<CyNode>();
		for (CyNode n: selCyNodes) {
			lisNeigh.addAll(network.getNeighborList(n, CyEdge.Type.ANY));
		}
		lisNeigh.addAll(selCyNodes);
		Set<CyNode> setNeigh = new HashSet<CyNode>(lisNeigh);
		
		for (CyNode n: setNeigh) {
			if (nodeTable.getRow(n.getSUID()).get("type", String.class).equals(NodeType.Protein.toString()) ) {
				setNeighP.add(n);
			}
		}
		
		List<CyEdge> ppEdge = new ArrayList<CyEdge>();
		for (CyNode n1: setNeighP) {
			for (CyNode n2: setNeighP) {
				// to exclude self edges
				if (n1 != n2) {
					ppEdge.addAll(network.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
				}					
			}			
		}
		
		setNeighP.addAll(selCyNodes);
		
		return (ppEdge);
		
	}
	
	public static List<CyEdge> findAssociatedGenes(CyNetwork network, Set<CyNode> selCyNodes, Set<CyNode> setAssocGenes, Map <CyNode, Set<CyNode>> disGenesMap) {
		//CyTable nodeTable = network.getDefaultNodeTable();		
		List<CyNode> lisNeighG = new ArrayList<CyNode>();
		List<CyEdge> dGEdge = new ArrayList<CyEdge>();
		// since disorders have only edges with genes in the network, this gives us only genes as neighbors of disorders
		for (CyNode n: selCyNodes) {
//			Set<CyNode> assocGenes = new HashSet<CyNode>(network.getNeighborList(n, CyEdge.Type.ANY));
			Set<CyNode> assocGenes =FilterType.neighborNodesOfType(network, n, NodeType.Gene);
			disGenesMap.put(n, assocGenes);
			//lisNeighG.addAll(network.getNeighborList(n, CyEdge.Type.ANY));
			lisNeighG.addAll(assocGenes);
//			dGEdge.addAll(network.getAdjacentEdgeList(n, CyEdge.Type.ANY));
			dGEdge.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.gene_disease));
		}
		//tavajoh kon ke line badi javab nemidahad va bayad ezafe koni liste lisNeighG ra be sete khalie setAssocGenes (injuri copy koni tu set)
		//setAssocGenes = new HashSet<CyNode>(lisNeighG);
		setAssocGenes.addAll(lisNeighG);
		return dGEdge;
		
	}
	
	/////DrugDisTargetTask
	public static List<CyEdge> findTargetProt(CyNetwork network, Set<CyNode> selCyNodes, Set<CyNode> setTargetProts, Map <CyNode, Set<CyNode>> drugProtMap){
		List<CyNode> lisNeighP = new ArrayList<CyNode>();
		List<CyEdge> drPEdge = new ArrayList<CyEdge>();
		// since drugs have only edges with prots in the network, this gives us only prots as neighbors of drugs
		for (CyNode n: selCyNodes) {
//			Set<CyNode> assocProts = new HashSet<CyNode>(network.getNeighborList(n, CyEdge.Type.ANY));
			Set<CyNode> assocProts = FilterType.neighborNodesOfType(network, n, NodeType.Protein);
			drugProtMap.put(n, assocProts);
			lisNeighP.addAll(assocProts);
			// if network has other edges than drug-protein for drugs, the following gives wrong result, instead use adjacentEdgesOfType
//			drPEdge.addAll(network.getAdjacentEdgeList(n, CyEdge.Type.ANY));
			drPEdge.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.drug_protein));
		}
		//tavajoh kon ke line badi javab nemidahad va bayad ezafe koni liste lisNeighG ra be sete khalie setAssocGenes (injuri copy koni tu set)
		//setAssocGenes = new HashSet<CyNode>(lisNeighG);
		setTargetProts.addAll(lisNeighP);
		return drPEdge;
	}
	
	public static Set<CyNode> genesProts (CyNetwork network, Set<CyNode> setAssocGenes, Set <CyEdge> gPEdges, Map <CyNode, Set<CyNode>> disGenesMap, Map <CyNode, Set<CyNode>> disProtsMap){
		Set <CyNode> encodedProts = new HashSet<CyNode> ();
		CyTable nodeTable = network.getDefaultNodeTable();
		Map<CyNode, Set<CyNode>> geneProtsMap = new HashMap<CyNode, Set<CyNode>>();
		for (CyNode gn: setAssocGenes) {
//			Set <CyNode> neighGs = new HashSet<CyNode>(network.getNeighborList(gn, CyEdge.Type.ANY));
			Set <CyNode> neighGs = FilterType.neighborNodesOfType(network, gn, NodeType.Protein);
			geneProtsMap.put(gn, new HashSet<CyNode>());
			for (CyNode pn: neighGs) {
				encodedProts.add(pn);
				gPEdges.addAll(network.getConnectingEdgeList(gn, pn, CyEdge.Type.ANY));
				geneProtsMap.get(gn).add(pn);
//				if (nodeTable.getRow(pn.getSUID()).get("type", String.class).equals(NodeType.Protein.toString())) {
//					encodedProts.add(pn);
//					gPEdges.addAll(network.getConnectingEdgeList(gn, pn, CyEdge.Type.ANY));
//					geneProtsMap.get(gn).add(pn);
//				}
			}		
		}
		
		for(Entry<CyNode, Set<CyNode>> entry: disGenesMap.entrySet()) {
			disProtsMap.put(entry.getKey(), new HashSet<CyNode>());
			for (CyNode n: entry.getValue()) {
				disProtsMap.get(entry.getKey()).addAll(geneProtsMap.get(n));
			}
		}
		return encodedProts;
	}
	
	/////DrugDisTargetTask
	public static Set<CyNode> protsGene (CyNetwork network, Set<CyNode >setTargetProts, Set<CyEdge >gPEdges, Map <CyNode, Set<CyNode>> drugProtMap){
		Set <CyNode> encodingGenes = new HashSet<CyNode> ();
		CyTable nodeTable = network.getDefaultNodeTable();
		Map<CyNode, Set<CyNode>> geneProtsMap = new HashMap<CyNode, Set<CyNode>>();
		for (CyNode pn: setTargetProts) {
			
//			Set <CyNode> neighPs = new HashSet<CyNode>(network.getNeighborList(pn, CyEdge.Type.ANY));
			Set <CyNode> neighPs = FilterType.neighborNodesOfType(network, pn, NodeType.Gene);
			geneProtsMap.put(pn, new HashSet<CyNode>());
			for (CyNode gn: neighPs) {
				if (nodeTable.getRow(gn.getSUID()).get("type", String.class).equals(NodeType.Gene.toString())) {
					encodingGenes.add(gn);
					gPEdges.addAll(network.getConnectingEdgeList(pn, gn, CyEdge.Type.ANY));					
					geneProtsMap.get(pn).add(gn);
				}
			}		
		}
		
		/*for(Entry<CyNode, Set<CyNode>> entry: drugProtMap.entrySet()) {
			disProtsMap.put(entry.getKey(), new HashSet<CyNode>());
			for (CyNode n: entry.getValue()) {
				disProtsMap.get(entry.getKey()).addAll(geneProtsMap.get(n));
			}
		}*/
		return encodingGenes;
	}
	
	public static Set<CyNode> drugProts (CyNetwork network, Set<CyNode> encodedProts, Set <CyEdge> drPEdge, Map <CyNode, Set<CyNode>> disProtsMap, 
			Map<CyNode, Set<CyNode>> drugProtsMap, Map<CyNode, Set<CyNode>> drugDisordersMap){
		Set<CyNode> targetDrugs = new HashSet<CyNode>();
//		Map<CyNode, Set<CyNode>> drugDisordersMap = new HashMap<CyNode, Set<CyNode>>();
//		Map<CyNode, Set<CyNode>> drugProtsMap = new HashMap<CyNode, Set<CyNode>>();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		/*for(CyNode pn: encodedProts) {
			Set <CyNode> neighPs = new HashSet<CyNode>(network.getNeighborList(pn, CyEdge.Type.ANY));
			for (CyNode dn: neighPs) {
				drugProtsMap.put(dn, new HashSet<CyNode>());
				if (nodeTable.getRow(dn.getSUID()).get("type", String.class).equals("RDrug")) {
					targetDrugs.add(dn);
					drPEdge.addAll(network.getConnectingEdgeList(dn, pn, CyEdge.Type.ANY));
					drugProtsMap.get(dn).add(pn);
				}
			}
		}*/
		
		for(CyNode pn: encodedProts) {
			Set <CyNode> neighPs = new HashSet<CyNode>(network.getNeighborList(pn, CyEdge.Type.ANY));
			for (CyNode dn: neighPs) {				
				if (nodeTable.getRow(dn.getSUID()).get("type", String.class).equals(NodeType.Drug.toString())) {
					if (!drugProtsMap.containsKey(dn)) {
						drugProtsMap.put(dn, new HashSet<CyNode>());
					}					
					targetDrugs.add(dn);
					drPEdge.addAll(network.getConnectingEdgeList(dn, pn, CyEdge.Type.ANY));
					drugProtsMap.get(dn).add(pn);
				}
			}
		}
		
		Map<CyNode, Set<CyNode>> protDisorderMap = new HashMap<CyNode, Set<CyNode>>();
		for(Entry<CyNode, Set<CyNode>> entry: disProtsMap.entrySet()) {
			for (CyNode n: entry.getValue()) {
				if (!protDisorderMap.containsKey(n)) {
					protDisorderMap.put(n, new HashSet<CyNode>());
				}
				protDisorderMap.get(n).add(entry.getKey());
			}
		}
		// removed to palce in main class	
		/*String targetedDisordercolName = "targeted_disorders";
		if(nodeTable.getColumn(targetedDisordercolName+"_"+newNetworkName) == null) {
			nodeTable.createColumn(targetedDisordercolName+"_"+newNetworkName, String.class, false);
		}
		else if (nodeTable.getColumn(targetedDisordercolName+"_"+newNetworkName)!= null){
			for (CyNode dr: targetDrugs) {
				network.getRow(dr).set(targetedDisordercolName, null);
			}
		}
		
		String drugtargetNum = "#targets";
		if(nodeTable.getColumn(drugtargetNum+"_"+newNetworkName) == null) {
			nodeTable.createColumn(drugtargetNum+"_"+newNetworkName, Integer.class, false);
		}
		else if (nodeTable.getColumn(drugtargetNum+"_"+newNetworkName)!= null){
			for (CyNode dr: targetDrugs) {
				network.getRow(dr).set(drugtargetNum+"_"+newNetworkName, null);
			}
		}*/
		
		for (CyNode dr: targetDrugs) {
			drugDisordersMap.put(dr, new HashSet<CyNode>());
			for(CyNode p: drugProtsMap.get(dr)) {
				// this is where we get problem, since protDisorderMap is based on the set of prots associated with selection of our disorders BUT
				// drugProtsMap has also proteins from steinerTree ==> adding a condition would resolve it
				if (protDisorderMap.containsKey(p)) {
					for(CyNode dis: protDisorderMap.get(p)) {
						drugDisordersMap.get(dr).add(dis);					
					}
				}
				/*for(CyNode dis: protDisorderMap.get(p)) {
					drugDisordersMap.get(dr).add(dis);					
				}*/
			}
			
			// removed to palce in main class
			/*String targetedDisorders = new String();
			for (CyNode n: drugDisordersMap.get(dr)) {
				targetedDisorders = targetedDisorders + nodeTable.getRow(n.getSUID()).get("displayName", String.class) + ", " ;
			}
			
			nodeTable.getRow(dr.getSUID()).set(targetedDisordercolName+"_"+newNetworkName, targetedDisorders);
			nodeTable.getRow(dr.getSUID()).set(drugtargetNum+"_"+newNetworkName, drugProtsMap.get(dr).size());*/
		}
				
		return targetDrugs;
	}
	
	//// DrugDisTargetTask
	public static Set<CyNode> disGenes(CyNetwork network, Set<CyNode> encodingGenes, Set<CyEdge> disGEdge){
		Set<CyNode> targetDisorders = new HashSet<CyNode>();
		CyTable nodeTable = network.getDefaultNodeTable();
		Map<CyNode, Set<CyNode>> geneDisMap = new HashMap<CyNode, Set<CyNode>>();
		Map<CyNode, Set<CyNode>> disGeneMap = new HashMap<CyNode, Set<CyNode>>();
//		Map<CyNode, Set<CyNode>> disorderDrugsMap = new HashMap <CyNode, Set<CyNode>> ();
		
		for (CyNode g: encodingGenes) {
//			Set <CyNode> neighDis = new HashSet<CyNode>(network.getNeighborList(g, CyEdge.Type.ANY));
			Set <CyNode> neighDis = FilterType.neighborNodesOfType(network, g, NodeType.Disease);
			geneDisMap.put(g, new HashSet<CyNode>());
			
			for (CyNode n: neighDis) {
				if (nodeTable.getRow(n.getSUID()).get("type", String.class).equals(NodeType.Disease.toString())) {
					targetDisorders.add(n);
					disGEdge.addAll(network.getConnectingEdgeList(g, n, CyEdge.Type.ANY));
					geneDisMap.get(g).add(n);
					if (!disGeneMap.containsKey(n)) {
						disGeneMap.put(n, new HashSet<CyNode>());
					}
					disGeneMap.get(n).add(g);
				}
			}
		}
		
		/*for (CyNode d: targetDisorders) {
			disorderDrugsMap.put(d, new HashSet<CyNode>());
			for(CyNode g: disGeneMap.get(d)) {
				if (geneDrugMap.containsKey(g)) {
					for(CyNode dr: geneDrugMap.get(g)) {
						disorderDrugsMap.get(d).add(dr);					
					}
				}
			}
		}*/
		return targetDisorders;
	}
	
	public static Set<CyNode> protsGenesDis (CyNetwork network, Set<CyNode> setNeighP, Set <CyEdge> neighbEdges, Set <CyEdge> neighbDEdges) {
		
		Map<CyNode, List<CyEdge>> netMap = new HashMap<CyNode, List<CyEdge>>();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		for (CyNode cn: setNeighP) {
			netMap.put(cn, network.getAdjacentEdgeList(cn, CyEdge.Type.ANY));
		}
		
		//logger.info("NetMap:" + netMap);
		
		//Map<CyNode, List<CyEdge>> neighbMap = new HashMap<CyNode, List<CyEdge>>();
		// endNode is gene
		Set <CyNode> endNode = new HashSet<CyNode> ();
		//Set <CyEdge> neighbEdges = new HashSet<CyEdge> ();
		Map<CyNode, List<CyEdge>> neighbMap = new HashMap<CyNode, List<CyEdge>>();
		for (Entry<CyNode, List<CyEdge>> entry1: netMap.entrySet()) {
			neighbMap.put(entry1.getKey(), new ArrayList<CyEdge>());
			for (CyEdge e: entry1.getValue()) {
				if (nodeTable.getRow(e.getTarget().getSUID()).get("type", String.class).equals(NodeType.Gene.toString()) ) {
					neighbMap.get(entry1.getKey()).add(e);
					endNode.add(e.getTarget());
					//neighbEdges are prot-gene edges
					neighbEdges.add(e);
				}
			}			
		}
		
		Map<CyNode, List<CyEdge>> gdMap = new HashMap<CyNode, List<CyEdge>>();
		for (CyNode n: endNode) {
			gdMap.put(n, network.getAdjacentEdgeList(n, CyEdge.Type.ANY));
		}
		//logger.info("The egdMap is:" + gdMap);
		Map<CyNode, List<CyEdge>> neighbDMap = new HashMap<CyNode, List<CyEdge>>();
		Set <CyNode> endDNode = new HashSet<CyNode> ();
		//Set <CyEdge> neighbDEdges = new HashSet<CyEdge> ();
		for (Entry<CyNode, List<CyEdge>> entry1: gdMap.entrySet()) {
			neighbDMap.put(entry1.getKey(), new ArrayList<CyEdge>());
			for (CyEdge e: entry1.getValue()) {
				if (nodeTable.getRow(e.getTarget().getSUID()).get("type", String.class).equals(NodeType.Disease.toString()) ) {
					neighbDMap.get(entry1.getKey()).add(e);
					endDNode.add(e.getTarget());
					// neighbDEdges are gene-disease edges
					neighbDEdges.add(e);
				}
			}
			//logger.info("Neigh disease Map is :" + neighbDMap);	
			
		}
		
		return (endNode);
		//logger.info("Selected Edges:" + neighbEdges);
		//logger.info("The end nodes as genes:" + endNode);
	}
	
	
public static Set <CyNode> protsDis (CyNetwork network, Set<CyNode> setNeighP, Map<String, Set<String>> sPtDmap, Map<CyNode, Set<CyNode>>sPtDCyNodemap) {
	
	Set <CyNode> endDNode = new HashSet<CyNode> ();
	CyTable nodeTable = network.getDefaultNodeTable();
	//Set<String> targetDNode = new HashSet<String>();
	for(CyNode n: setNeighP) {
		List<CyNode> lcn = network.getNeighborList(n, CyEdge.Type.ANY);
		Set<CyNode> scn = new HashSet<CyNode>(lcn);
		for (CyNode gn: scn) {
			if (nodeTable.getRow(gn.getSUID()).get("type", String.class).equals(NodeType.Gene.toString()) ) {
				List<CyNode> ldis = network.getNeighborList(gn, CyEdge.Type.ANY);
				Set<CyNode> sdis = new HashSet<CyNode>(ldis);
				for (CyNode dn: sdis) {
					if (nodeTable.getRow(dn.getSUID()).get("type", String.class).equals(NodeType.Disease.toString()) ) {
						sPtDmap.get(nodeTable.getRow(n.getSUID()).get("name", String.class)).add(nodeTable.getRow(dn.getSUID()).get("name", String.class));
						sPtDCyNodemap.get(n).add(dn);
						endDNode.add(dn);
					}
				}
			}
		}
		
	}
	
	return(endDNode);

	}
	
	public static void compute_brokerage (CyNetwork network, CyNetwork newNet, Set<CyNode> setNeighP) {
		CyTable nodeTable = network.getDefaultNodeTable();
		//String brokScolName = "brokerage_score";
		String brokScolName = "subnet_participation_degree";
		String degreeColName = "degree_PPI";
		String degreeSubColName = "degreeSubPPI";
		
		/*if(nodeTable.getColumn(degreeColName) == null) {
			nodeTable.createColumn(degreeColName, Integer.class, false);
		}
		if (nodeTable.getColumn(degreeSubColName) == null) {
			nodeTable.createColumn(degreeSubColName, Integer.class, false);
		}
		if (nodeTable.getColumn(brokScolName) == null) {
			nodeTable.createColumn(brokScolName, String.class, false);
		}*/
		
		CyTable localNewNodeTable = newNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		if(localNewNodeTable.getColumn(degreeColName) == null) {
			localNewNodeTable.createColumn(degreeColName, Integer.class, false);
		}
		if (localNewNodeTable.getColumn(degreeSubColName) == null) {
			localNewNodeTable.createColumn(degreeSubColName, Integer.class, false);
		}
		if (localNewNodeTable.getColumn(brokScolName) == null) {
			localNewNodeTable.createColumn(brokScolName, String.class, false);
		}
		
		for (CyNode n: setNeighP) {
			double degreePPI = 0.0;
			double degreeSubPPI = 0.0;
			Set<CyNode> neighNodeSet = new HashSet<CyNode>(network.getNeighborList(n, CyEdge.Type.ANY));
			// to exclude self loops in PPI
			neighNodeSet.remove(n);
			for (CyNode nn: neighNodeSet) {
				if (nodeTable.getRow(nn.getSUID()).get("type", String.class).equals(NodeType.Protein.toString())) {
					degreePPI +=1;
					if (setNeighP.contains(nn)) {
						degreeSubPPI +=1;
					}
				}
			}
			Integer degree = Integer.valueOf((int)degreePPI);
			Integer degreeSubset = Integer.valueOf((int)degreeSubPPI);
			double brokerage = degreeSubPPI/degreePPI;
			//Double brk = Double.valueOf(brokerage);
			localNewNodeTable.getRow(n.getSUID()).set(degreeColName, degree);
			localNewNodeTable.getRow(n.getSUID()).set(degreeSubColName, degreeSubset);			
			localNewNodeTable.getRow(n.getSUID()).set(brokScolName, String.format("%.5f", brokerage));

			
		}
		
	}

}
