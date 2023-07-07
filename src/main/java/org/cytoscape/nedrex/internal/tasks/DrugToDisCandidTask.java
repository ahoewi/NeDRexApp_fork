package org.cytoscape.nedrex.internal.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.CommandExecuter;
import org.cytoscape.nedrex.internal.CreatePDisEdgesTask;
import org.cytoscape.nedrex.internal.CyNetToGraphConverter;
import org.cytoscape.nedrex.internal.CySteinerTree;
import org.cytoscape.nedrex.internal.Link;
import org.cytoscape.nedrex.internal.ModelUtil;
import org.cytoscape.nedrex.internal.ModulFunctions;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.UndirectedNetwork;
import org.cytoscape.nedrex.internal.Vertex;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.jgrapht.graph.WeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class DrugToDisCandidTask extends AbstractTask{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Set parameters for Drug -> Disease function";}
	
	@Tunable(description="Include Steiner tree in anaylysis", groups="Analysis parameters:",
			tooltip="Checking the box includes in the analysis the additional proteins found by Steiner tree connecting proteins targeted by the set of drugs.",
			gravity = 3.0)
	public boolean stTree = false;
	
	@Tunable(description="Collapse proteins on genes", groups="Collapse proteins on their encoding genes:      ",
			tooltip="Collapse proteins on their encoding genes in the created module.",
			gravity = 5.0)
	public boolean clpsPG = false;
	
	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name based on the selected algorithm parameters will be assigned",
			gravity = 6.0)
	public Boolean set_net_name = false;

	@Tunable(description="Name of the result network", 
			groups="Result network", 
			dependsOn="set_net_name=true",
			tooltip="Enter the name you would like to have assigned to the result network",
			gravity = 6.0)
	public String new_net_name = new String();
	
	public DrugToDisCandidTask(RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {		
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		Set<CyEdge> alledges = new HashSet<CyEdge>();
		Set<CyNode> allnodes = new HashSet<CyNode>();		
		Set <CyNode> selCyNodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true));
		
		logger.info("Selected nodes:" + selCyNodes);
		
		Set<CyNode> setTargetProts = new HashSet<CyNode>();
		//
		Map <CyNode, Set<CyNode>> drugProtMap = new HashMap <CyNode, Set<CyNode>>();
		List<CyEdge> drPEdge = ModulFunctions.findTargetProt(network, selCyNodes, setTargetProts, drugProtMap);
		logger.info("Set of protein targets for selected drugs:" + setTargetProts);
		logger.info("Set of drug-protein edges:" + drPEdge);
		
		///**** adding steinerTree in the analysis:		
		List<Vertex> protsNotConnected = new ArrayList<Vertex>();
		Set<CyNode> protsNotConnectedCy = new HashSet<CyNode>();
		UndirectedNetwork out = new UndirectedNetwork();
		List<Vertex> encodedProtsVertices = new ArrayList<Vertex>();
		
		if (stTree==true) {			
			CyNetToGraphConverter cnvrt = new CyNetToGraphConverter(app);
			
			List <Vertex> terminalNodes = cnvrt.cyNetworkToPPINet(out, network, setTargetProts);
			encodedProtsVertices = new ArrayList<Vertex>(terminalNodes);
			logger.info("All the terminal nodes (encoded protes): " + "size= " + terminalNodes.size() + " : "+ terminalNodes);

			protsNotConnected = cnvrt.inConnectedComponent(terminalNodes, out);
			for (Vertex v: protsNotConnected) {
				Long nSUID = Long.parseLong(v.toString());
				protsNotConnectedCy.add(network.getNode(nSUID));
			}
			logger.info("The terminal nodes (encoded protes) in connected component:" + "size= " + terminalNodes.size() + " : "+ terminalNodes);
			logger.info("Number of nodes in out network: " + out.vertexSet().size());
			logger.info("Number of edges in out network: " + out.edgeSet().size());
			
			CySteinerTree st = new CySteinerTree(out, terminalNodes);			
			WeightedMultigraph<Vertex, Link> steiner = st.getSteinerTree();			
			logger.info("This is the final Steiner-tree, including edges: " + steiner);			
			Set<Link> stEdges = steiner.edgeSet();
			Set<CyEdge> stCyEdges = new HashSet<CyEdge>();
			Set<Vertex> strNodes = steiner.vertexSet();
			Set<CyNode> stCyNodes = new HashSet<CyNode>();
			
			for (Link l: stEdges) {
				Long snSUID = Long.parseLong(l.getSource().toString());
				Long tnSUID = Long.parseLong(l.getTarget().toString());
				List<CyEdge> lce = network.getConnectingEdgeList(network.getNode(snSUID), network.getNode(tnSUID), CyEdge.Type.ANY);
				stCyEdges.add(lce.get(0));
			}
			alledges.addAll(stCyEdges);
			
			for (Vertex v: strNodes) {
				Long nSUID = Long.parseLong(v.toString());
				stCyNodes.add(network.getNode(nSUID));
			}
			setTargetProts.addAll(stCyNodes);
			encodedProtsVertices.addAll(strNodes);
			
			/*BridgingCentrality brdg = new BridgingCentrality (app);
			List<Vertex> strNodeslist = new ArrayList<Vertex>(strNodes);
			brdg.bridgingScore(out, strNodeslist);*/
		}
		
		Set <CyEdge> gPEdges = new HashSet<CyEdge> ();
		Set<CyNode> encodingGenes = ModulFunctions.protsGene(network, setTargetProts, gPEdges, drugProtMap);
		logger.info("Set of encoding genes for proteins:" + encodingGenes);

		Set<CyEdge> disGEdge = new HashSet<CyEdge>();
		Set<CyNode> targetDisorders = ModulFunctions.disGenes(network, encodingGenes, disGEdge);
		logger.info("Set of disorders associated with genes:" + targetDisorders);
		logger.info("Set of disorder-gene edges:" + disGEdge);
		
		Map <CyNode, Set<CyNode>> geneProtsMap = new HashMap <CyNode, Set<CyNode>>();
		for (CyEdge e: gPEdges) {
			if (!geneProtsMap.containsKey(e.getTarget())) {
				geneProtsMap.put(e.getTarget(), new HashSet<CyNode>());					
				geneProtsMap.get(e.getTarget()).add(e.getSource());			
			}
			else if (geneProtsMap.containsKey(e.getTarget())) {
				geneProtsMap.get(e.getTarget()).add(e.getSource());
			}
		}
		System.out.println("This is the geneProtsMap: " + geneProtsMap);
		Map <CyNode, Set<CyNode>> disProtsMap = new HashMap <CyNode, Set<CyNode>>();
		for (CyEdge e: disGEdge) {
			if (!disProtsMap.containsKey(e.getTarget())) {
				disProtsMap.put(e.getTarget(), new HashSet<CyNode>());					
				geneProtsMap.get(e.getSource());
				disProtsMap.get(e.getTarget()).addAll(geneProtsMap.get(e.getSource()));					
			}
			else if (disProtsMap.containsKey(e.getTarget())) {
				disProtsMap.get(e.getTarget()).addAll(geneProtsMap.get(e.getSource()));		
			}
		}
		
		if (!clpsPG) {
			allnodes.addAll(encodingGenes);
		}
		allnodes.addAll(setTargetProts);
		allnodes.addAll(targetDisorders);
		
		if (!clpsPG) {
			alledges.addAll(disGEdge);
			alledges.addAll(gPEdges);
		}		
		alledges.addAll(drPEdge);
		
		// selecting disorder-gene-protein-drug edges in the network		
		for (CyEdge ce: alledges) {
			Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
			for (CyRow cr: matchedERows) {
				cr.set("selected", true);
			}
		}

		// selecting gene, protein, drug nodes in the network (drugs were already selected, starting point was from selected drugs as seed nodes)
		for (CyNode cn: allnodes) {
			Collection <CyRow> matchedERows = network.getDefaultNodeTable().getMatchingRows("SUID", cn.getSUID());
			for (CyRow cr: matchedERows) {
				cr.set("selected", true);
			}
		}
		
		String newNetName = new String();
		if (!set_net_name) {					
			newNetName = namingUtil.getSuggestedNetworkTitle("Drug_to_Disease");
		}
		else if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
		////// creating a network from selected nodes & edges, by command:			
		Map<String, Object> args = new HashMap<>();
		//network create edgeList=selected, nodeList=selected, source=current
		args.put("edgeList", "selected");
		args.put("nodeList", "selected");
		//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
		args.put("excludeEdges", true);
		args.put("source", "current");
		args.put("networkName", newNetName);
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("network", "create", args, null);
		
		// moved here from ModulFunctions.drugProts	and changed to localTables		
		/*CyTable localNewNodeTable = ModelUtil.getNetworkWithName(app, newNetName).getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);		
		String indicatedDrugcolName = "indicated_drugs";
		if(localNewNodeTable.getColumn(indicatedDrugcolName) == null) {
			localNewNodeTable.createColumn(indicatedDrugcolName, String.class, false);
		}
		String drugtargetNum = "#targets";
		if(localNewNodeTable.getColumn(drugtargetNum) == null) {
			localNewNodeTable.createColumn(drugtargetNum, Integer.class, false);
		}

		for (CyNode d: targetDisorders) {
			String targetedDisorders = new String();
			for (CyNode n: disorderDrugsMap.get(d)) {
				targetedDisorders = targetedDisorders + nodeTable.getRow(n.getSUID()).get("displayName", String.class) + ", " ;
			}
			localNewNodeTable.getRow(d.getSUID()).set(indicatedDrugcolName, targetedDisorders);
//			localNewNodeTable.getRow(d.getSUID()).set(drugtargetNum, drugProtsMap.get(d).size());
		}*/

		if (stTree==true) {
			ModulFunctions.bridgingScore(ModelUtil.getNetworkWithName(app, newNetName), out, encodedProtsVertices);
		}
				
		if (clpsPG) {
			insertTasksAfterCurrentTask(new CreatePDisEdgesTask(app, disProtsMap, newNetName));
		}

		insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));

		insertTasksAfterCurrentTask(new DeselectAll(app, network));
		
	}

}
