package org.cytoscape.nedrex.internal.tasks;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.*;
import org.cytoscape.nedrex.internal.utils.ApiRoutesUtil;
import org.cytoscape.nedrex.internal.utils.FilterType;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class GetDiseaseDrugsTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Get drugs indicated in the selected disorders";}
	
	@Tunable(description="Include subtypes of selected disorders", groups="Disease subtype option",
			tooltip = "Specifies whether only the selected disorders should be considered or also their 1st level subtypes (based on disease hierarchy from MONDO)",
			gravity = 1.0)
    public Boolean include_subtypes = false;
	
	@Tunable(description="Include all subtypes of disorders", groups="Disease subtype option",
			dependsOn="include_subtypes=true",
			tooltip="<html>" + "If selected, all the subtypes of disorders (all descendants in the disease hierarchy from MONDO) will be considered."
					+ "<br> Otherwise, only the first level of subtypes will be considered"+"</html>",
			gravity = 2.0)
	public Boolean include_all_subtypes = false;
	
	public GetDiseaseDrugsTask (RepoApplication app) {
		this.app = app;
		this.apiUtils = app.getApiRoutesUtil();
	}

	private ApiRoutesUtil apiUtils;

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetwork network = app.getCurrentNetwork();
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
		Set<CyNode> selectedDisorders = FilterType.keepNodesOfType(network, selectedNodes, NodeType.Disease);
		Map<CyNode, Set<String>> childrenNameMap = new HashMap<CyNode, Set<String>> ();
		Set<String> allChildrenNames = new HashSet<String>();
		Map<CyNode, Set<String>> descendantNameMap = new HashMap<CyNode, Set<String>> ();
		Set<String> allDescendantsNames = new HashSet<String>();
		Set<CyNode> disordersDrugs = new HashSet<CyNode>();
		Map<CyNode, Set<CyNode>> disorderDrugMap = new HashMap<CyNode, Set<CyNode>>(); // Only the Map parent nodes to the drug of their children
		Set<CyEdge> disorderDrugEdges = new HashSet<CyEdge> ();
		
		if (!include_subtypes && !include_all_subtypes) {

			Set<CyEdge> disDisEdges = ModelUtil.getEdgesBetweenNodes(network, selectedDisorders);
			for (CyNode n: selectedDisorders) {
				Set<CyNode> drugs = FilterType.neighborNodesOfType(network, n, NodeType.Drug);
				disordersDrugs.addAll(drugs);
				disorderDrugEdges.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.drug_disease));
			}
			
			// select all current dis-dis and dis-gene edges and necessary nodes and create a subnetwork
			for (CyNode n: disordersDrugs) {
				network.getRow(n).set("selected", true);
			}
			for (CyEdge e: disDisEdges) {
				network.getRow(e).set("selected", true);
			}
			for (CyEdge e: disorderDrugEdges) {
				network.getRow(e).set("selected", true);
			}
			CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
//			String  netName= network.getRow(network).get(CyNetwork.NAME, String.class);		
			String newNetworkName = namingUtil.getSuggestedNetworkTitle("drugs_indicated_disorders");
			
			Map<String, Object> args = new HashMap<>();
			//network create edgeList=selected, nodeList=selected, source=current
			args.put("edgeList", "selected");
			args.put("nodeList", "selected");
			//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
			args.put("excludeEdges", true);
			args.put("source", network);
			args.put("networkName", newNetworkName);
			CommandExecuter cmdex = new CommandExecuter(app);
			cmdex.executeCommand("network", "create", args, null);
			
			String styleName = "NeDRex";		
			Map<String, Object> argsAS = new HashMap<>();
			argsAS.put("styles", styleName);
			CommandExecuter cmdexAS = new CommandExecuter(app);
			cmdexAS.executeCommand("vizmap", "apply", argsAS, null);

			insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));
			insertTasksAfterCurrentTask(new DeselectAll(app, network));
			
		}
	//// GET Children
		if (include_subtypes && !include_all_subtypes) {
			allChildrenNames = this.apiUtils.getDisordersChildren(network, selectedDisorders, childrenNameMap);
			logger.info("The childrenMap: " + childrenNameMap);
			logger.info("All the children names: " + allChildrenNames);
			
			Set<CyNode> childrenNodesMapedInNet= ModelUtil.getNodeSetWithName(network, allChildrenNames);			
			for (CyNode n: childrenNodesMapedInNet) {
				network.getRow(n).set("selected", true);
			}
			
			selectedDisorders.addAll(childrenNodesMapedInNet);
			Set<CyEdge> disDisEdges = ModelUtil.getEdgesBetweenNodes(network, selectedDisorders);
			for (CyNode n: selectedDisorders) {
				Set<CyNode> drugs = FilterType.neighborNodesOfType(network, n, NodeType.Drug);
				disordersDrugs.addAll(drugs);
				disorderDrugEdges.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.drug_disease));
			}
			
			// Map parent nodes to the genes of their children
			for (Entry<CyNode, Set<String>> entry: childrenNameMap.entrySet()) {
				disorderDrugMap.put(entry.getKey(), new HashSet<CyNode>());
				Set<CyNode> childrenNodes = ModelUtil.getNodeSetWithName(network, entry.getValue());
				for (CyNode childNode: childrenNodes) {
					disorderDrugMap.get(entry.getKey()).addAll(FilterType.neighborNodesOfType(network, childNode, NodeType.Drug));
				}				
			}
			
			// select all current dis-dis and dis-drug edges and necessary nodes and create a subnetwork
			for (CyNode n: disordersDrugs) {
				network.getRow(n).set("selected", true);
			}
			for (CyEdge e: disDisEdges) {
				network.getRow(e).set("selected", true);
			}
			for (CyEdge e: disorderDrugEdges) {
				network.getRow(e).set("selected", true);
			}
			CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
//			String  netName= network.getRow(network).get(CyNetwork.NAME, String.class);		
			String newNetworkName = namingUtil.getSuggestedNetworkTitle("drugs_indicated_disorders_alevel1_subtypes");
			
			Map<String, Object> args = new HashMap<>();
			//network create edgeList=selected, nodeList=selected, source=current
			args.put("edgeList", "selected");
			args.put("nodeList", "selected");
			//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
			args.put("excludeEdges", true);
			args.put("source", network);
			args.put("networkName", newNetworkName);
			CommandExecuter cmdex = new CommandExecuter(app);
			cmdex.executeCommand("network", "create", args, null);
			
			String styleName = "NeDRex";		
			Map<String, Object> argsAS = new HashMap<>();
			argsAS.put("styles", styleName);
			CommandExecuter cmdexAS = new CommandExecuter(app);
			cmdexAS.executeCommand("vizmap", "apply", argsAS, null);

			// then create new edges between parents and genes of their children			
//			insertTasksAfterCurrentTask(new AddGeneDiseaseEdgeTask(app, newNetworkName, disorderDrugMap));
			insertTasksAfterCurrentTask(new AddDrugDiseaseEdgeTask(app, newNetworkName, disorderDrugMap));
			insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));
			insertTasksAfterCurrentTask(new DeselectAll(app, network));
			
		}		
				
	//// GET all Descendants
		if (include_subtypes && include_all_subtypes) {
			allDescendantsNames = this.apiUtils.getDisordersDescendants(network, selectedDisorders, descendantNameMap);
			logger.info("The descendantsMap: " + descendantNameMap);
			logger.info("All the descendant names: " + allDescendantsNames);
			
			Set<CyNode> descendantsNodesMapedInNet= ModelUtil.getNodeSetWithName(network, allDescendantsNames);			
			for (CyNode n: descendantsNodesMapedInNet) {
				network.getRow(n).set("selected", true);
			}
			
			selectedDisorders.addAll(descendantsNodesMapedInNet);
			Set<CyEdge> disDisEdges = ModelUtil.getEdgesBetweenNodes(network, selectedDisorders);
			for (CyNode n: selectedDisorders) {
				Set<CyNode> drugs = FilterType.neighborNodesOfType(network, n, NodeType.Drug);
				disordersDrugs.addAll(drugs);
				disorderDrugEdges.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.drug_disease));
			}
			
			// Map parent nodes to the genes of their descendants
			for (Entry<CyNode, Set<String>> entry: descendantNameMap.entrySet()) {
				disorderDrugMap.put(entry.getKey(), new HashSet<CyNode>());
				Set<CyNode> descendantsNodes = ModelUtil.getNodeSetWithName(network, entry.getValue());
				for (CyNode descendNode: descendantsNodes) {
					disorderDrugMap.get(entry.getKey()).addAll(FilterType.neighborNodesOfType(network, descendNode, NodeType.Drug));
				}				
			}
			
			// select all current dis-dis and dis-gene edges and necessary nodes and create a subnetwork
			for (CyNode n: disordersDrugs) {
				network.getRow(n).set("selected", true);
			}
			for (CyEdge e: disDisEdges) {
				network.getRow(e).set("selected", true);
			}
			for (CyEdge e: disorderDrugEdges) {
				network.getRow(e).set("selected", true);
			}
			CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
//			String  netName= network.getRow(network).get(CyNetwork.NAME, String.class);		
			String newNetworkName = namingUtil.getSuggestedNetworkTitle("drugs_indicated_disorders_allSubtypes");
			
			Map<String, Object> args = new HashMap<>();
			//network create edgeList=selected, nodeList=selected, source=current
			args.put("edgeList", "selected");
			args.put("nodeList", "selected");
			//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
			args.put("excludeEdges", true);
			args.put("source", network);
			args.put("networkName", newNetworkName);
			CommandExecuter cmdex = new CommandExecuter(app);
			cmdex.executeCommand("network", "create", args, null);
			
			String styleName = "NeDRex";		
			Map<String, Object> argsAS = new HashMap<>();
			argsAS.put("styles", styleName);
			CommandExecuter cmdexAS = new CommandExecuter(app);
			cmdexAS.executeCommand("vizmap", "apply", argsAS, null);
			
			// then create new edges between parents and genes of their descendatns			
			insertTasksAfterCurrentTask(new AddDrugDiseaseEdgeTask(app, newNetworkName, disorderDrugMap));
			insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));
			insertTasksAfterCurrentTask(new DeselectAll(app, network));
			
		}
		
	}

}
