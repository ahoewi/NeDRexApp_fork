package org.cytoscape.myApp.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.myApp.internal.CommandExecuter;
import org.cytoscape.myApp.internal.InteractionType;
import org.cytoscape.myApp.internal.NodeType;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.utils.FilterType;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CreateDiseaseomeTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private String sharedGene = "shared genes";
	private String sharedDrug = "shared drugs";
	
	private CyNetwork sourceNetwork;
	private Set<CyNode> nodes_to_add;
	//private Set<List<CyNode>> edges_to_add;
	private Set<Set<CyNode>> edges_to_add;
	//private Map <List<CyNode>, Set<CyNode>> sharedDrugMap;
	private Map <Set<CyNode>, Set<CyNode>> sharedDrugMap;
	private Map <Set<CyNode>, Set<CyNode>> sharedGeneMap;
	private String selected_option;
	
	public CreateDiseaseomeTask(RepoApplication app, CyNetwork sourceNetwork, Set<CyNode> nodes_to_add, Set<Set<CyNode>> edges_to_add, Map <Set<CyNode>, Set<CyNode>> sharedDrugMap, Map <Set<CyNode>, Set<CyNode>> sharedGeneMap, String selected_option) {
		this.app = app;
		this.sourceNetwork = sourceNetwork;
		this.nodes_to_add = nodes_to_add;
		this.edges_to_add = edges_to_add;
		this.sharedDrugMap = sharedDrugMap;
		this.sharedGeneMap = sharedGeneMap;
		this.selected_option = selected_option;
		
	}
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetworkManager netMgr = app.getActivator().getService(CyNetworkManager.class);
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		CyNetworkFactory cnf = app.getActivator().getService(CyNetworkFactory.class);
		CyNetworkViewFactory cnvf = app.getActivator().getService(CyNetworkViewFactory.class);
		CyNetworkViewManager nvm = app.getActivator().getService(CyNetworkViewManager.class);
		
		String nodeTypeCol = "type";
		String edgeTypeCol = "type";
		String nodeDisplayNameCol = "displayName";

		CyNetwork newProj = cnf.createNetwork();	
//		newProj.getRow(newProj).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("Drug-based Diseasome"));
		Map <CyNode, CyNode> mapCyNode = new HashMap<CyNode, CyNode> ();
		
		newProj.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
		newProj.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
		
		if (selected_option.equals(sharedDrug)) {
			newProj.getRow(newProj).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("Drug-based Diseasome"));
			newProj.getDefaultNodeTable().createColumn("degree_drug", Integer.class, false);
			newProj.getDefaultNodeTable().createColumn("icd10", String.class, false);
			newProj.getDefaultNodeTable().createColumn("synonyms", String.class, false);
			
			for (CyNode sourceNode: nodes_to_add) {
				CyNode node = newProj.addNode();
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("name", sourceNetwork.getRow(sourceNode).get(CyNetwork.NAME, String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("displayName", sourceNetwork.getRow(sourceNode).get("displayName", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("degree_drug", FilterType.neighborNodesOfType(sourceNetwork, sourceNode, NodeType.Drug).size());
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("icd10", sourceNetwork.getRow(sourceNode).get("icd10", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("synonyms", sourceNetwork.getRow(sourceNode).get("synonyms", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set(nodeTypeCol, NodeType.Disease.toString());				
				mapCyNode.put(sourceNode, node);
			}
			
			newProj.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
			newProj.getDefaultEdgeTable().createColumn("sourceDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("targetDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("List of shared drugs", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("#shared drugs", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("#union drugs", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("JaccardIndex", Double.class, false);
			
			//for (List<CyNode> sourceEdge: edges_to_add) {
			for (Set<CyNode> edgeS: edges_to_add) {
				List<CyNode> sourceEdge = new ArrayList<CyNode> (edgeS);
				CyEdge edge = newProj.addEdge(mapCyNode.get(sourceEdge.get(0)), mapCyNode.get(sourceEdge.get(1)), false);
				String snode = sourceNetwork.getRow(sourceEdge.get(0)).get(CyNetwork.NAME, String.class);
				String tnode = sourceNetwork.getRow(sourceEdge.get(1)).get(CyNetwork.NAME, String.class);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", snode + " (-) " + tnode);				
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("sourceDomainId", snode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("targetDomainId", tnode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set(edgeTypeCol, InteractionType.disease_drug_disease.toString());				
				//newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#shared drugs", sharedDrugMap.get(sourceEdge).size());
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#shared drugs", sharedDrugMap.get(edgeS).size());
				
				Set<CyNode> snodeDrugs = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(0), NodeType.Drug);
				Set<CyNode> tnodeDrugs = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(1), NodeType.Drug);
				
				/*Set<CyNode> intersection = new HashSet<CyNode>(snodeDrugs);
				intersection.retainAll(tnodeDrugs);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#shared drugs", intersection.size());*/
				
				snodeDrugs.addAll(tnodeDrugs);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#union drugs", snodeDrugs.size());
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("JaccardIndex", (double)sharedDrugMap.get(edgeS).size()/(double)snodeDrugs.size());
				
				String shared_drugs = "";
				for (CyNode drug: sharedDrugMap.get(edgeS)) {
					shared_drugs = shared_drugs + sourceNetwork.getRow(drug).get(CyNetwork.NAME, String.class) + ", ";
				}
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("List of shared drugs", shared_drugs);				
			}		
		}
		
		else if (selected_option.equals(sharedGene)) {
			newProj.getRow(newProj).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("Gene-based Diseasome"));
			newProj.getDefaultNodeTable().createColumn("degree_gene", Integer.class, false);
			newProj.getDefaultNodeTable().createColumn("icd10", String.class, false);
			newProj.getDefaultNodeTable().createColumn("synonyms", String.class, false);
			
			for (CyNode sourceNode: nodes_to_add) {
				CyNode node = newProj.addNode();
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("name", sourceNetwork.getRow(sourceNode).get(CyNetwork.NAME, String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("displayName", sourceNetwork.getRow(sourceNode).get("displayName", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("degree_gene", FilterType.neighborNodesOfType(sourceNetwork, sourceNode, NodeType.Gene).size());
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("icd10", sourceNetwork.getRow(sourceNode).get("icd10", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("synonyms", sourceNetwork.getRow(sourceNode).get("synonyms", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set(nodeTypeCol, NodeType.Disease.toString());				
				mapCyNode.put(sourceNode, node);
			}
			
			newProj.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
			newProj.getDefaultEdgeTable().createColumn("sourceDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("targetDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("List of shared genes", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("#shared genes", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("#union genes", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("JaccardIndex", Double.class, false);
			
			//for (List<CyNode> sourceEdge: edges_to_add) {
			for (Set<CyNode> edgeS: edges_to_add) {
				List<CyNode> sourceEdge = new ArrayList<CyNode> (edgeS);
				CyEdge edge = newProj.addEdge(mapCyNode.get(sourceEdge.get(0)), mapCyNode.get(sourceEdge.get(1)), false);
				String snode = sourceNetwork.getRow(sourceEdge.get(0)).get(CyNetwork.NAME, String.class);
				String tnode = sourceNetwork.getRow(sourceEdge.get(1)).get(CyNetwork.NAME, String.class);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", snode + " (-) " + tnode);				
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("sourceDomainId", snode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("targetDomainId", tnode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set(edgeTypeCol, InteractionType.disease_disease.toString());				
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#shared genes", sharedGeneMap.get(edgeS).size());
				
				Set<CyNode> snodeGenes = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(0), NodeType.Gene);
				Set<CyNode> tnodeGenes = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(1), NodeType.Gene);
				
				snodeGenes.addAll(tnodeGenes);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#union genes", snodeGenes.size());
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("JaccardIndex", (double)sharedGeneMap.get(edgeS).size()/(double)snodeGenes.size());
				
				String shared_genes = "";
				for (CyNode gene: sharedGeneMap.get(edgeS)) {
					shared_genes = shared_genes + sourceNetwork.getRow(gene).get(CyNetwork.NAME, String.class) + ", ";
				}
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("List of shared genes", shared_genes);				
			}
			
		}
		
		netMgr.addNetwork(newProj);
		
		Map<String, Object> args = new HashMap<>();
		args.put("network", "current");
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("view", "create", args, null);

		String style = "NeDRex";
		Map<String, Object> argsAS = new HashMap<>();
		argsAS.put("styles", style);
		CommandExecuter cmdexAS = new CommandExecuter(app);
		cmdexAS.executeCommand("vizmap", "apply", argsAS, null);
		
	}

}
