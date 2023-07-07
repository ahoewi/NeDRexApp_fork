package org.cytoscape.nedrex.internal.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.ModelUtil;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.algorithms.DiamondAPI;
import org.cytoscape.nedrex.internal.algorithms.MuSTAPI;
import org.cytoscape.nedrex.internal.utils.ApiRoutesUtil;
import org.cytoscape.nedrex.internal.utils.FilterType;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @modified by: Andreas Maier
 */
public class QuickStartTask extends AbstractTask{
	private Logger logger = LoggerFactory.getLogger(getClass());
    private RepoApplication app;
    private String must = "MuST";
    private String diamond = "DIAMOnD";
    
    @ProvidesTitle
	public String getTitle() {return "Quick Start with Drug Repurposing";}
    
    @Tunable(description = "Disease module identification method:", groups="Method selection", params="displayState=uncollapsed", 
	         tooltip = "Select the method you want to start the drug repusposing with.",
	         gravity = 1.0)
	public ListSingleSelection<String> methodOptions = new ListSingleSelection<String> (diamond, must);
    
    @Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name based on the selected algorithm parameters will be assigned",
			gravity = 5.0)
    public Boolean set_net_name = false;
	
	@Tunable(description="Name of the result network", 
	         groups="Result network", 
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 5.0)
	public String new_net_name = new String();
    
    public QuickStartTask (RepoApplication app) {
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
		Map<CyNode, Set<String>> descendantNameMap = new HashMap<CyNode, Set<String>> ();
		Set<String> allDescendantsNames = new HashSet<String>();
		Set<CyNode> disordersGenes = new HashSet<CyNode>();
		
		////Step 1: Get all descendants of selected disorders
		allDescendantsNames = this.apiUtils.getDisordersDescendants(network, selectedDisorders, descendantNameMap);
		logger.info("The descendantsMap: " + descendantNameMap);
		logger.info("All the descendant names: " + allDescendantsNames);	
		Set<CyNode> descendantsNodesMapedInNet= ModelUtil.getNodeSetWithName(network, allDescendantsNames);			
		for (CyNode n: descendantsNodesMapedInNet) {
			network.getRow(n).set("selected", true);
		}		
		selectedDisorders.addAll(descendantsNodesMapedInNet);
		
		////Step 2: Get all genes associated to disorders from step 1 and deselect any selected disorders
		FilterType.keepSelectedNodesOfType(network, NodeType.Gene);
		
		for (CyNode n: selectedDisorders) {
			Set<CyNode> genes = FilterType.neighborNodesOfType(network, n, NodeType.Gene);
			disordersGenes.addAll(genes);
		}		
		// select all diseaseGenes subnetwork
		for (CyNode n: disordersGenes) {
			network.getRow(n).set("selected", true);
		}
		logger.info("The list of disorder genes: " + disordersGenes);
		
		String newNetName = new String();
		String netName = new String();
		if (!set_net_name) {					
			if (methodOptions.getSelectedValue().equals(must)) {
				netName = "QuickStart_MuST";
			}
			else if (methodOptions.getSelectedValue().equals(diamond)) {
				netName = "QuickStart_DIAMOnD";
			}
			newNetName = namingUtil.getSuggestedNetworkTitle(netName);
		}
		else if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
		////Step 3: Run the selected disease module algorithms on the seeds from step 2
		if (methodOptions.getSelectedValue().equals(must)) {
			MuSTAPI must = new MuSTAPI(network, disordersGenes, 5, 5);			
			Set<String> seeds_in_network = must.getSeedsInNetwork();
			Set<List<String>> edges = must.getMuSTEdges();
			Set<String> mustNodes = must.getMuSTNodes();
			Map<String, Integer> nodeParticipationMap = must.getNodesParticipation();
			Map<List<String>, Integer> edgeParticipationMap = must.getEdgesParticipation();
			
			if(must.getSuccess()) {
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			////Step 4: Closeness centrality will be run on all the nodes returned in the disease module at step 3, this happens at the end of MuSTapiCreateNetTask
				taskmanager.execute(new TaskIterator(new MuSTapiCreateNetTask(app, true, mustNodes, edges, nodeParticipationMap, edgeParticipationMap, seeds_in_network, true, newNetName)));
			}			
		}		
		else if (methodOptions.getSelectedValue().equals(diamond)) {
			DiamondAPI diamond = new DiamondAPI(network, disordersGenes, 200, 1);
			Set<String> seeds_in_network = diamond.getSeedsInNetwork();
			Set<List<String>> edges = diamond.getDiamondEdges();
			Set<String> diamondNodes = diamond.getDiamondNodes();
			Map<String, Integer> scoreMap = diamond.getScore();
			Map<String, Double> pHyperMap = diamond.getPHyper();
			
			if(diamond.getSuccess()) {
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			////Step 4: Closeness centrality will be run on all the nodes returned in the disease module at step 3, this happens at the end of DiamondCreateNetTask
				taskmanager.execute(new TaskIterator(new DiamondCreateNetTask(app, true, diamondNodes, edges, scoreMap, pHyperMap, seeds_in_network, true, newNetName)));
			}
		}

		insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));
		
		insertTasksAfterCurrentTask(new DeselectAll(app, network));
		
	}

}
