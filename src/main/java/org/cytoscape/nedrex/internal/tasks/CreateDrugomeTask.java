package org.cytoscape.nedrex.internal.tasks;

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
import org.cytoscape.nedrex.internal.CommandExecuter;
import org.cytoscape.nedrex.internal.InteractionType;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.utils.FilterType;
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
public class CreateDrugomeTask extends AbstractTask{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private String sharedDis = "shared indications";
	private String sharedTarget = "shared targets";
	
	private CyNetwork sourceNetwork;
	private Set<CyNode> nodes_to_add;
	private Set<Set<CyNode>> edges_to_add;
	private Map <Set<CyNode>, Set<CyNode>> sharedDisMap;
	private Map <Set<CyNode>, Set<CyNode>> sharedTargetMap;
	private String selected_option;
	
	public CreateDrugomeTask (RepoApplication app, CyNetwork sourceNetwork ,Set<CyNode> nodes_to_add, Set<Set<CyNode>>edges_to_add, Map <Set<CyNode>, Set<CyNode>> sharedDisMap, Map <Set<CyNode>, Set<CyNode>> sharedTargetMap, String selected_option) {
		this.app = app;
		this.sourceNetwork = sourceNetwork;
		this.nodes_to_add = nodes_to_add;
		this.edges_to_add = edges_to_add;
		this.sharedDisMap = sharedDisMap;
		this.sharedTargetMap = sharedTargetMap;
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
//		newProj.getRow(newProj).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("Drugome"));
		Map <CyNode, CyNode> mapCyNode = new HashMap<CyNode, CyNode> ();
		
		newProj.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
		newProj.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
		
		if (selected_option.equals(sharedDis)) {
			newProj.getRow(newProj).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("Indication-based Drugome"));
			newProj.getDefaultNodeTable().createColumn("degree_disease", Integer.class, false);
			newProj.getDefaultNodeTable().createColumn("drugGroups", String.class, false);
			
			for (CyNode sourceNode: nodes_to_add) {
				CyNode node = newProj.addNode();
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("name", sourceNetwork.getRow(sourceNode).get(CyNetwork.NAME, String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("displayName", sourceNetwork.getRow(sourceNode).get("displayName", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("degree_disease", FilterType.neighborNodesOfType(sourceNetwork, sourceNode, NodeType.Disease).size());
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("drugGroups", sourceNetwork.getRow(sourceNode).get("drugGroups", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set(nodeTypeCol, NodeType.Drug.toString());				
				mapCyNode.put(sourceNode, node);
			}
			
			newProj.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
			newProj.getDefaultEdgeTable().createColumn("sourceDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("targetDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("List of shared disorders", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("#shared disorders", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("#union disorders", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("JaccardIndex", Double.class, false);
			
			for (Set<CyNode> edgeS: edges_to_add) {
				List<CyNode> sourceEdge = new ArrayList<CyNode> (edgeS);
				CyEdge edge = newProj.addEdge(mapCyNode.get(sourceEdge.get(0)), mapCyNode.get(sourceEdge.get(1)), false);
				String snode = sourceNetwork.getRow(sourceEdge.get(0)).get(CyNetwork.NAME, String.class);
				String tnode = sourceNetwork.getRow(sourceEdge.get(1)).get(CyNetwork.NAME, String.class);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", snode + " (-) " + tnode);				
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("sourceDomainId", snode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("targetDomainId", tnode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set(edgeTypeCol, InteractionType.drug_indication_drug.toString());				
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#shared disorders", sharedDisMap.get(edgeS).size());
				
				Set<CyNode> snodeDisorders = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(0), NodeType.Disease);
				Set<CyNode> tnodeDisorders = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(1), NodeType.Disease);
				
				snodeDisorders.addAll(tnodeDisorders);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#union disorders", snodeDisorders.size());
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("JaccardIndex", (double)sharedDisMap.get(edgeS).size()/(double)snodeDisorders.size());
				
				String shared_disorders = "";
				for (CyNode dis: sharedDisMap.get(edgeS)) {
					shared_disorders = shared_disorders + sourceNetwork.getRow(dis).get(CyNetwork.NAME, String.class) + ", ";
				}
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("List of shared disorders", shared_disorders);
				
				/*Set<CyNode> geneNeighbsSource = FilterType.neighborNodesOfType(sourceNet, ModelUtil.getNodeWithName(app, sourceNet, ee.get(0)), NodeType.Gene);
				Set<CyNode> geneNeighbsTarget = FilterType.neighborNodesOfType(sourceNet, ModelUtil.getNodeWithName(app, sourceNet, ee.get(1)), NodeType.Gene);
				geneNeighbsSource.addAll(geneNeighbsTarget);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#union genes", geneNeighbsSource.size());*/
				
				/*Set<CyNode> geneNeighbsSource = new HashSet<CyNode>(diseaseGenesMap.get(ee.get(0)));
				Set<CyNode> geneNeighbsTarget = new HashSet<CyNode>(diseaseGenesMap.get(ee.get(1)));					
				Integer minGenes = Math.min(geneNeighbsSource.size(), geneNeighbsTarget.size());*/
				
				/*if (ee.get(0).equals("Esophageal squamous cell carcinoma") && ee.get(1).equals("Colonic adenoma recurrence, reduced risk of")) {
					logger.info("~~~ The set of genes cynode for Esophageal squamous cell carcinoma:" + geneNeighbsSource);						
					logger.info("~~~ The set of genes cynode for Colonic adenoma recurrence, reduced risk of:" + geneNeighbsTarget);						
				}*/
				
				/*geneNeighbsSource.addAll(geneNeighbsTarget);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#union genes", geneNeighbsSource.size());
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("JaccardIndex", (double)sharedMap.get(ee).size()/(double)geneNeighbsSource.size());
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("overlapCoefficient", (double)sharedMap.get(ee).size()/(double)minGenes);
				*/
				
			}
			
		}
		
		else if (selected_option.equals(sharedTarget)) {
			newProj.getRow(newProj).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("Target-based Drugome"));
			newProj.getDefaultNodeTable().createColumn("degree_target", Integer.class, false);
			newProj.getDefaultNodeTable().createColumn("drugGroups", String.class, false);
			
			for (CyNode sourceNode: nodes_to_add) {
				CyNode node = newProj.addNode();
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("name", sourceNetwork.getRow(sourceNode).get(CyNetwork.NAME, String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("displayName", sourceNetwork.getRow(sourceNode).get("displayName", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("degree_target", FilterType.neighborNodesOfType(sourceNetwork, sourceNode, NodeType.Protein).size());
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set("drugGroups", sourceNetwork.getRow(sourceNode).get("drugGroups", String.class));
				newProj.getDefaultNodeTable().getRow(node.getSUID()).set(nodeTypeCol, NodeType.Drug.toString());				
				mapCyNode.put(sourceNode, node);
			}
			
			newProj.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
			newProj.getDefaultEdgeTable().createColumn("sourceDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("targetDomainId", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("List of shared targets", String.class, false);
			newProj.getDefaultEdgeTable().createColumn("#shared targets", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("#union targets", Integer.class, false);
			newProj.getDefaultEdgeTable().createColumn("JaccardIndex", Double.class, false);
			
			for (Set<CyNode> edgeS: edges_to_add) {
				List<CyNode> sourceEdge = new ArrayList<CyNode> (edgeS);
				CyEdge edge = newProj.addEdge(mapCyNode.get(sourceEdge.get(0)), mapCyNode.get(sourceEdge.get(1)), false);
				String snode = sourceNetwork.getRow(sourceEdge.get(0)).get(CyNetwork.NAME, String.class);
				String tnode = sourceNetwork.getRow(sourceEdge.get(1)).get(CyNetwork.NAME, String.class);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", snode + " (-) " + tnode);				
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("sourceDomainId", snode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("targetDomainId", tnode);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set(edgeTypeCol, InteractionType.drug_target_drug.toString());				
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#shared targets", sharedTargetMap.get(edgeS).size());
				
				Set<CyNode> snodeTargets = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(0), NodeType.Protein);
				Set<CyNode> tnodeTargets = FilterType.neighborNodesOfType(sourceNetwork, sourceEdge.get(1), NodeType.Protein);
				
				snodeTargets.addAll(tnodeTargets);
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("#union targets", snodeTargets.size());
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("JaccardIndex", (double)sharedTargetMap.get(edgeS).size()/(double)snodeTargets.size());
				
				String shared_targets = "";
				for (CyNode target: sharedTargetMap.get(edgeS)) {
					shared_targets = shared_targets + sourceNetwork.getRow(target).get(CyNetwork.NAME, String.class) + ", ";
				}
				newProj.getDefaultEdgeTable().getRow(edge.getSUID()).set("List of shared targets", shared_targets);
				
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
