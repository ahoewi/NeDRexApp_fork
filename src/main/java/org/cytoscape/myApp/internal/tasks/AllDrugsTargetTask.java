package org.cytoscape.myApp.internal.tasks;

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
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.myApp.internal.NodeType;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.utils.ApiRoutesUtil;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class AllDrugsTargetTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Return drugs targeting the selection";}
	
	@Tunable(description="Include only approved drugs", groups="Drug option",
			tooltip = "Specifies, whether only approved drugs targeting the selection should be considered or all drugs",
			gravity = 1.0)
    public Boolean only_approved_drugs = false;
	
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
	
	public AllDrugsTargetTask(RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		String newNetName = new String();
		if (!set_net_name) {					
			String netName = "AllDrugs";
			if(only_approved_drugs) {
				netName = netName + "_approved";
			}
			newNetName = namingUtil.getSuggestedNetworkTitle(netName);
		}
		else if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
		List<String> seeds = new ArrayList<String>();
		Boolean ggType = false;
//		Map<CyNode, Set<String>> geneDrugsMap = new HashMap<CyNode, Set<String>>();
		Map<CyNode, Set<String>> proteinDrugsMap = new HashMap<CyNode, Set<String>>(); // in case of GG network geneDrugsMap				
		Set<String> proteins = new HashSet<String> (); // in case of GG network it's genes
		Set<String> drugs = new HashSet<String>();
		List<List<String>> edgesPP = new ArrayList<List<String>> (); // it can be also GG edges
		List<List<String>> edgesDrP = new ArrayList<List<String>> ();
		Map<String, Double> drugScoreMap = new HashMap <String, Double>();
		
		Set<CyNode> selectedNodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true));
		List<String> selectedNodeNames = new ArrayList<String>();
		Set<String> primary_seeds = new HashSet<String>();
		String seedCol = "seed";
		
/*		if (network.getRow(selectedNodes.get(0)).get("type", String.class).equals(NodeType.Gene.toString())) {
			ggType = true;
		}*/
		
		if (network.getRow(selectedNodes.iterator().next()).get("type", String.class).equals(NodeType.Gene.toString())) {
			ggType = true;
		}
		for (CyNode n: selectedNodes) {
			String nodeName = network.getRow(n).get(CyNetwork.NAME, String.class);
			selectedNodeNames.add(nodeName);
			if(network.getRow(n).isSet(seedCol) && network.getRow(n).get(seedCol, Boolean.class)) {
				primary_seeds.add(nodeName);
			}
		}
		
		Set<String> targeting_drugs = new HashSet<String>();
		if (!ggType) {
			proteinDrugsMap = ApiRoutesUtil.getDrugsTargetingProteins(network, selectedNodes, targeting_drugs);			
		}
		else if (ggType) {
			proteinDrugsMap = ApiRoutesUtil.getDrugsTargetingGenes(network, selectedNodes, targeting_drugs);
		}
		logger.info("Here's the set of targeting drugs: " + targeting_drugs);
		if(only_approved_drugs) {
			List<String> approved_drugs = ApiRoutesUtil.getApprovedDrugsList(targeting_drugs);
			logger.info("Here's the set of approved drugs: " + approved_drugs);
			for(Entry<CyNode, Set<String>> entry: proteinDrugsMap.entrySet()) {
				entry.getValue().retainAll(approved_drugs);
			}
		}
		
		for (Entry<CyNode, Set<String>> entry: proteinDrugsMap.entrySet()) {
			for(String dr: entry.getValue()) {
				List<String> nn = new ArrayList<String>();
				nn.add(dr);
				String p = network.getRow(entry.getKey()).get(CyNetwork.NAME, String.class);
				nn.add(p);
				edgesDrP.add(nn);
				drugs.add(dr);
				proteins.add(p);
			}
		}
				
		// get the PP or GG edges between selected seeds to pass to the CreateNetRankedDrugTask
		Set<CyEdge> edgesBetwSeeds = new HashSet<CyEdge>();
		for (CyNode s1: selectedNodes) {
			for (CyNode s2: selectedNodes) {
				if (!s1.equals(s2)) {
					edgesBetwSeeds.addAll(network.getConnectingEdgeList(s1, s2, CyEdge.Type.ANY));
					edgesBetwSeeds.addAll(network.getConnectingEdgeList(s2, s1, CyEdge.Type.ANY));
				}				
			}
		}
						
		for (CyEdge e: edgesBetwSeeds) {
			List<String> nn = new ArrayList<String>();
			nn.add(network.getRow(e.getSource()).get(CyNetwork.NAME, String.class));
			nn.add(network.getRow(e.getTarget()).get(CyNetwork.NAME, String.class));			
			edgesPP.add(nn);
			proteins.add(nn.get(0));
			proteins.add(nn.get(1));
		}
		
		DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
//		taskmanager.execute(new TaskIterator(new TrustRankCreateNetTask(app, proteins, drugs, edgesDrP, drugScoreMap, edgesPP)));
		String rankinFunction = "AllDrugs";
		taskmanager.execute(new TaskIterator(new CreateNetRankedDrugTask(app, false, proteins, drugs, edgesDrP, drugScoreMap, edgesPP, rankinFunction, newNetName, primary_seeds, ggType)));
		
	}

}
