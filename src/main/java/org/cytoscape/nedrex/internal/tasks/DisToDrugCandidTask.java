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
import org.cytoscape.model.CyTable;
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
public class DisToDrugCandidTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Set parameters for Disease -> Drug function";}
	
	@Tunable(description="Include Steiner tree in anaylysis", groups="Analysis parameters:",
			tooltip="Checking the box includes in the analysis the additional proteins found by Steiner tree connecting proteins associated with the given set of disorders.",
			gravity = 3.0)
	public boolean stTree = false;
	
	@Tunable(description="Collapse proteins on genes", groups="Collapse proteins on their encoding genes:      ",
			tooltip="Collapse proteins on their encoding genes in the created module.",
			gravity = 5.0)
	public boolean clpsPG = false;
	
	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name will be assigned",
			gravity = 6.0)
	public Boolean set_net_name = false;

	@Tunable(description="Name of the result network", 
			groups="Result network", 
			dependsOn="set_net_name=true",
			tooltip="Enter the name you would like to have assigned to the result network",
			gravity = 6.0)
	public String new_net_name = new String();
	
	public DisToDrugCandidTask(RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {		
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		Set<CyEdge> alledges = new HashSet<CyEdge>();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		Collection <CyRow> selNRows = nodeTable.getMatchingRows("selected", true);
		Set <CyNode> selCyNodes = new HashSet<CyNode> ();
		for (CyRow snr: selNRows) {
			selCyNodes.add(network.getNode(snr.get("SUID", Long.class)));			
		}
		logger.info("Selected nodes:" + selCyNodes);
		
		Set<CyNode> setAssocGenes = new HashSet<CyNode>();
		//
		Map <CyNode, Set<CyNode>> disGenesMap = new HashMap <CyNode, Set<CyNode>>();
		List<CyEdge> dGEdge = ModulFunctions.findAssociatedGenes(network, selCyNodes, setAssocGenes, disGenesMap);
		logger.info("Set of associated genes with disorders:" + setAssocGenes);
		logger.info("Set of gene-disorder edges:" + dGEdge);
		
		Set <CyEdge> gPEdges = new HashSet<CyEdge> ();
		Map <CyNode, Set<CyNode>> disProtsMap = new HashMap <CyNode, Set<CyNode>>();
		Set<CyNode> encodedProts = ModulFunctions.genesProts(network, setAssocGenes, gPEdges, disGenesMap, disProtsMap);
		logger.info("Set of encoded proteins from genes:" + encodedProts);
		
		///**** adding steinerTree in the analysis:
		List<Vertex> protsNotConnected = new ArrayList<Vertex>();
		Set<CyNode> protsNotConnectedCy = new HashSet<CyNode>();
		UndirectedNetwork out = new UndirectedNetwork();
		List<Vertex> encodedProtsVertices = new ArrayList<Vertex>();
		
		if (stTree==true) {			
			CyNetToGraphConverter cnvrt = new CyNetToGraphConverter(app);
//			UndirectedNetwork out = new UndirectedNetwork();
			List <Vertex> terminalNodes = cnvrt.cyNetworkToPPINet(out, network, encodedProts);
			encodedProtsVertices = new ArrayList<Vertex>(terminalNodes);
			logger.info("All the terminal nodes (encoded protes): " + "size= " + terminalNodes.size() + " : "+ terminalNodes);
			
			protsNotConnected = cnvrt.inConnectedComponent(terminalNodes, out);
			for (Vertex v: protsNotConnected) {
				Long nSUID = Long.parseLong(v.toString());
				protsNotConnectedCy.add(network.getNode(nSUID));
			}
			logger.info("The terminal nodes (encoded protes) in connected component:" + "size= " + terminalNodes.size());
			logger.info("The terminal nodes (encoded protes) NOT in connected component:"+ "size= " +protsNotConnected.size()+" :" +protsNotConnected);
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
			encodedProts.addAll(stCyNodes);
			encodedProtsVertices.addAll(strNodes);
			
			/*BridgingCentrality brdg = new BridgingCentrality (app);			
			brdg.bridgingScore(out, encodedProtsVertices);*/
		}
				
		Set<CyEdge> drPEdge = new HashSet<CyEdge>();
		Map<CyNode, Set<CyNode>> drugProtsMap = new HashMap<CyNode, Set<CyNode>>();
		Map<CyNode, Set<CyNode>> drugDisordersMap = new HashMap<CyNode, Set<CyNode>>();
		Set<CyNode> targetDrugs = ModulFunctions.drugProts(network, encodedProts, drPEdge, disProtsMap, drugProtsMap, drugDisordersMap);	
			
		Set<CyNode> allnodes = new HashSet<CyNode>();
		if (!clpsPG) {
			allnodes.addAll(setAssocGenes);
		}
		allnodes.addAll(encodedProts);
		allnodes.addAll(targetDrugs);
		
		/// notice that p-p edges are not included in the created subnetwork, neither from steiner tree nor between encoded prots
//		Set<CyEdge> alledges = new HashSet<CyEdge>();
		if (!clpsPG) {
			alledges.addAll(dGEdge);
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
		
		// selecting gene, protein, drug nodes in the network (disorders were already selected, starting point was from selected disorders as seed nodes)
		for (CyNode cn: allnodes) {
			Collection <CyRow> matchedERows = network.getDefaultNodeTable().getMatchingRows("SUID", cn.getSUID());
			for (CyRow cr: matchedERows) {
				cr.set("selected", true);
			}
		}
		
		String newNetName = new String();
		if (!set_net_name) {		
			newNetName = namingUtil.getSuggestedNetworkTitle("Disease_to_Drug");
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
		CyTable localNewNodeTable = ModelUtil.getNetworkWithName(app, newNetName).getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);		
		String targetedDisordercolName = "targeted_disorders";
		if(localNewNodeTable.getColumn(targetedDisordercolName) == null) {
			localNewNodeTable.createColumn(targetedDisordercolName, String.class, false);
		}
		String drugtargetNum = "#targets";
		if(localNewNodeTable.getColumn(drugtargetNum) == null) {
			localNewNodeTable.createColumn(drugtargetNum, Integer.class, false);
		}
		
		for (CyNode dr: targetDrugs) {
			String targetedDisorders = new String();
			for (CyNode n: drugDisordersMap.get(dr)) {
				targetedDisorders = targetedDisorders + nodeTable.getRow(n.getSUID()).get("displayName", String.class) + ", " ;
			}
			localNewNodeTable.getRow(dr.getSUID()).set(targetedDisordercolName, targetedDisorders);
			localNewNodeTable.getRow(dr.getSUID()).set(drugtargetNum, drugProtsMap.get(dr).size());
		}
		
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
