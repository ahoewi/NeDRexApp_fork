package org.cytoscape.myApp.internal.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.myApp.internal.ModelUtil;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class MapSelectionToNetTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Map selection to another network";}
	
	private ListSingleSelection<String> allNets ;
	@Tunable(description = "Name of the target network to map the selection to:", groups="Target network", params="displayState=uncollapsed", 
	         longDescription = "If no network is used, select ```--NONE---```",
	         gravity = 3.0)
	public ListSingleSelection<String> getallNets(){
		//attribute = new ListSingleSelection<>("Current collection", "--Create new network collection--");
		allNets = ModelUtil.updateNetworksList(app, allNets);
		return allNets;
	}
	public void setallNets(ListSingleSelection<String> nets) { }

	
	public MapSelectionToNetTask (RepoApplication app) {
		this.app = app;
	}
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		boolean tm = false;
		
		/// make a list of selected nodes in the current network
		CyNetwork network = app.getCurrentNetwork();
		
		/// selecting nodes
		CyNetwork targetNet = ModelUtil.getNetworkWithName(app, allNets.getSelectedValue());				
		Set <CyNode> selectedCyNodesCurNet = new HashSet<CyNode>(CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true));
		Map <CyNode, String> selectedNodeMap = new HashMap<CyNode, String>();
		for (CyNode cn: selectedCyNodesCurNet) {
			selectedNodeMap.put(cn, network.getRow(cn).get(CyNetwork.NAME, String.class));
		}

		for (Entry<CyNode, String> entry: selectedNodeMap.entrySet()) {
			CyNode cnSNet = ModelUtil.getNodeWithName(app, targetNet, entry.getValue());
			if (cnSNet != null) {
				targetNet.getRow(cnSNet).set("selected", true);
			}
			else {
				tm = true;
			}			
		}
		
		/// selecting edges				
		Set <CyEdge> selectedCyEdgesCurNet = new HashSet<CyEdge>(CyTableUtil.getEdgesInState(network,CyNetwork.SELECTED,true));
		Map <CyEdge, String> selectedEdgeMap = new HashMap<CyEdge, String>();
		for (CyEdge ce: selectedCyEdgesCurNet) {
			selectedEdgeMap.put(ce, network.getRow(ce).get(CyNetwork.NAME, String.class));
		}
		
		for (Entry<CyEdge, String> entry: selectedEdgeMap.entrySet()) {
			CyEdge ceSNet = ModelUtil.getEdgeWithName(app, targetNet, entry.getValue());
			if (ceSNet != null) {
				targetNet.getRow(ceSNet).set("selected", true);
			}
			else {
				tm = true;
			}			
		}
		
		
		if (tm == true) {
			logger.info("From the logger: Some of the nodes or edges from the selection could not be found in the target network!");
			taskMonitor.showMessage(TaskMonitor.Level.INFO,"Some of the nodes or edges from the selection could not be found in the target network!");
			//throw new Exception ("Somee of the nodes or edges from the selection could not be found in the target network!");
		}
		// to check the status message problem:
		/*taskMonitor.setTitle("Creating Result Panel");
		taskMonitor.setStatusMessage("Result Panel created successful.");*/
		
	}

}
