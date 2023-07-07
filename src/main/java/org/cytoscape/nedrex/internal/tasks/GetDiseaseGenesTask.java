package org.cytoscape.nedrex.internal.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.CommandExecuter;
import org.cytoscape.nedrex.internal.InteractionType;
import org.cytoscape.nedrex.internal.ModelUtil;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.utils.ApiRoutesUtil;
import org.cytoscape.nedrex.internal.utils.FilterType;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @modified by: Andreas Maier
 */
public class GetDiseaseGenesTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Get genes associated to selected disorders";}
	
	@Tunable(description="Include all subtypes of disorders", groups="Disease option",
			tooltip="<html>" + "If selected, all the subtypes of selected disorders (all descendants in the disease hierarchy from MONDO) will be considered."
					+ "<br> Otherwise, only the selected disorders will be considered"+"</html>",
			gravity = 2.0)
	public Boolean include_all_subtypes = false;
	
	@Tunable(description="Export the obtained disease genes to a file", groups="Export to file",
			tooltip="If selected, you can export the obtained disease genes to a file for later use.",
			gravity = 3.0)
	public Boolean export_to_file = false;
	
	@Tunable(description="Select the path to output file:", groups="Export to file",
			dependsOn="export_to_file=true",
			tooltip="Export the obtained disease genes to a file for later use. Select the path and enter the name for the file.",
			gravity = 4.0)
	public File out_file = new File(System.getProperty("user.home")+"/disease_genes");
	
	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name will be assigned",
			gravity = 5.0)
    public Boolean set_net_name = false;
	
	@Tunable(description="Name of the result network", 
	         groups="Result network", 
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 5.0)
	public String new_net_name = new String();
	
	/**
	 * this method writes list of disease genes in a txt file.
	 */
	private void writeOutput(CyNetwork network, Set<CyNode> disordersGenes) {
		FileWriter fileWriter = null;
		final String TAB = "\t";
		final String NewLine = "\n";
		String output_fileName = out_file.getPath()+".txt";
		
		try{
			fileWriter = new FileWriter(output_fileName);
			Set<String> gene_set = new HashSet<String>();
			for (CyNode g: disordersGenes) {
				gene_set.add(network.getRow(g).get("name", String.class));			
			}			
			for (String g: gene_set) {
				StringBuffer sb = new StringBuffer();
				sb.append(g);
				fileWriter.append(sb.toString());
				fileWriter.append(NewLine);
			}			
		} catch (Exception e) {
			System.out.println("Error in FileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try{
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}
		}
		
	}
	
	public GetDiseaseGenesTask(RepoApplication app) {
		this.app = app;
	}

	private ApiRoutesUtil apiUtils;
	@Reference
	public void setAPIUtils(ApiRoutesUtil apiUtils) {
		this.apiUtils = apiUtils;
	}

	public void unsetAPIUtils(ApiRoutesUtil apiUtils) {
		if (this.apiUtils == apiUtils)
			this.apiUtils = null;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
		Set<CyNode> selectedDisorders = FilterType.keepNodesOfType(network, selectedNodes, NodeType.Disease);
		Map<CyNode, Set<String>> childrenNameMap = new HashMap<CyNode, Set<String>> ();
		Set<String> allChildrenNames = new HashSet<String>();
		Map<CyNode, Set<String>> descendantNameMap = new HashMap<CyNode, Set<String>> ();
		Set<String> allDescendantsNames = new HashSet<String>();
		Set<CyNode> disordersGenes = new HashSet<CyNode>();
		Map<CyNode, Set<CyNode>> disorderGeneMap = new HashMap<CyNode, Set<CyNode>>(); // Only the Map parent nodes to the genes of their children
		Set<CyEdge> disorderGeneEdges = new HashSet<CyEdge> ();
		
		String newNetName = new String();
		if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
//		if (!include_subtypes && !include_all_subtypes) {
		if (!include_all_subtypes) {
			Set<CyEdge> disDisEdges = ModelUtil.getEdgesBetweenNodes(network, selectedDisorders);
			for (CyNode n: selectedDisorders) {
				Set<CyNode> genes = FilterType.neighborNodesOfType(network, n, NodeType.Gene);
				disordersGenes.addAll(genes);
				disorderGeneEdges.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.gene_disease));
			}
			
			// select all current dis-dis and dis-gene edges and necessary nodes and create a subnetwork
			for (CyNode n: disordersGenes) {
				network.getRow(n).set("selected", true);
			}
			for (CyEdge e: disDisEdges) {
				network.getRow(e).set("selected", true);
			}
			for (CyEdge e: disorderGeneEdges) {
				network.getRow(e).set("selected", true);
			}
//			String  netName= network.getRow(network).get(CyNetwork.NAME, String.class);
			if(!set_net_name) {
				newNetName = namingUtil.getSuggestedNetworkTitle("Disorders_associatedGenes");
			}			
			Map<String, Object> args = new HashMap<>();
			//network create edgeList=selected, nodeList=selected, source=current
			args.put("edgeList", "selected");
			args.put("nodeList", "selected");
			//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
			args.put("excludeEdges", true);
			args.put("source", network);
			args.put("networkName", newNetName);
			CommandExecuter cmdex = new CommandExecuter(app);
			cmdex.executeCommand("network", "create", args, null);
			
			String styleName = "NeDRex";		
			Map<String, Object> argsAS = new HashMap<>();
			argsAS.put("styles", styleName);
			CommandExecuter cmdexAS = new CommandExecuter(app);
			cmdexAS.executeCommand("vizmap", "apply", argsAS, null);

			insertTasksAfterCurrentTask(new DeselectAll(app, network));
			
		}
	//// GET Children
/*		if (include_subtypes && !include_all_subtypes) {
			allChildrenNames = ApiRoutesUtil.getDisordersChildren(network, selectedDisorders, childrenNameMap);
			logger.info("The childrenMap: " + childrenNameMap);
			logger.info("All the children names: " + allChildrenNames);
			
			Set<CyNode> childrenNodesMapedInNet= ModelUtil.getNodeSetWithName(network, allChildrenNames);			
			for (CyNode n: childrenNodesMapedInNet) {
				network.getRow(n).set("selected", true);
			}
			
			selectedDisorders.addAll(childrenNodesMapedInNet);
			Set<CyEdge> disDisEdges = ModelUtil.getEdgesBetweenNodes(network, selectedDisorders);
			for (CyNode n: selectedDisorders) {
				Set<CyNode> genes = FilterType.neighborNodesOfType(network, n, NodeType.Gene);
				disordersGenes.addAll(genes);
				disorderGeneEdges.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.gene_disease));
			}
			
			// Map parent nodes to the genes of their children
			for (Entry<CyNode, Set<String>> entry: childrenNameMap.entrySet()) {
				disorderGeneMap.put(entry.getKey(), new HashSet<CyNode>());
				Set<CyNode> childrenNodes = ModelUtil.getNodeSetWithName(network, entry.getValue());
				for (CyNode childNode: childrenNodes) {
					disorderGeneMap.get(entry.getKey()).addAll(FilterType.neighborNodesOfType(network, childNode, NodeType.Gene));
				}				
			}
			
			// select all current dis-dis and dis-gene edges and necessary nodes and create a subnetwork
			for (CyNode n: disordersGenes) {
				network.getRow(n).set("selected", true);
			}
			for (CyEdge e: disDisEdges) {
				network.getRow(e).set("selected", true);
			}
			for (CyEdge e: disorderGeneEdges) {
				network.getRow(e).set("selected", true);
			}

			if(!set_net_name) {
				newNetName = namingUtil.getSuggestedNetworkTitle("Disorders+level1_subtypes_associatedGenes");
			}
			
			Map<String, Object> args = new HashMap<>();
			//network create edgeList=selected, nodeList=selected, source=current
			args.put("edgeList", "selected");
			args.put("nodeList", "selected");
			//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
			args.put("excludeEdges", true);
			args.put("source", network);
			args.put("networkName", newNetName);
			CommandExecuter cmdex = new CommandExecuter(app);
			cmdex.executeCommand("network", "create", args, null);
			
			String styleName = "NeDRex";		
			Map<String, Object> argsAS = new HashMap<>();
			argsAS.put("styles", styleName);
			CommandExecuter cmdexAS = new CommandExecuter(app);
			cmdexAS.executeCommand("vizmap", "apply", argsAS, null);

			// then create new edges between parents and genes of their children			
			insertTasksAfterCurrentTask(new AddGeneDiseaseEdgeTask(app, newNetName, disorderGeneMap));
			insertTasksAfterCurrentTask(new DeselectAll(app, network));
			
		}	*/	
				
	//// GET all Descendants
//		if (include_subtypes && include_all_subtypes) {
		if (include_all_subtypes) {
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
				Set<CyNode> genes = FilterType.neighborNodesOfType(network, n, NodeType.Gene);
				disordersGenes.addAll(genes);
				disorderGeneEdges.addAll(FilterType.adjacentEdgesOfType(network, n, InteractionType.gene_disease));
			}
			
			// Map parent nodes to the genes of their descendants
			for (Entry<CyNode, Set<String>> entry: descendantNameMap.entrySet()) {
				disorderGeneMap.put(entry.getKey(), new HashSet<CyNode>());
				Set<CyNode> descendantsNodes = ModelUtil.getNodeSetWithName(network, entry.getValue());
				for (CyNode descendNode: descendantsNodes) {
					disorderGeneMap.get(entry.getKey()).addAll(FilterType.neighborNodesOfType(network, descendNode, NodeType.Gene));
				}				
			}
			
			// select all current dis-dis and dis-gene edges and necessary nodes and create a subnetwork
			for (CyNode n: disordersGenes) {
				network.getRow(n).set("selected", true);
			}
			for (CyEdge e: disDisEdges) {
				network.getRow(e).set("selected", true);
			}
			for (CyEdge e: disorderGeneEdges) {
				network.getRow(e).set("selected", true);
			}
//			String  netName= network.getRow(network).get(CyNetwork.NAME, String.class);	
			if(!set_net_name) {
				newNetName = namingUtil.getSuggestedNetworkTitle("Disorders_subtypes_associatedGenes");
			}
			
			
			Map<String, Object> args = new HashMap<>();
			//network create edgeList=selected, nodeList=selected, source=current
			args.put("edgeList", "selected");
			args.put("nodeList", "selected");
			//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
			args.put("excludeEdges", true);
			args.put("source", network);
			args.put("networkName", newNetName);
			CommandExecuter cmdex = new CommandExecuter(app);
			cmdex.executeCommand("network", "create", args, null);
			
			String styleName = "NeDRex";		
			Map<String, Object> argsAS = new HashMap<>();
			argsAS.put("styles", styleName);
			CommandExecuter cmdexAS = new CommandExecuter(app);
			cmdexAS.executeCommand("vizmap", "apply", argsAS, null);
			
			// then create new edges between parents and genes of their descendatns						
			insertTasksAfterCurrentTask(new AddGeneDiseaseEdgeTask(app, newNetName, disorderGeneMap));
			insertTasksAfterCurrentTask(new DeselectAll(app, network));
			
		}
				
		insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));
		
		if (export_to_file) {
			writeOutput(network, disordersGenes);
		}
		
	}

}
