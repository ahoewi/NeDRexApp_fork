package org.cytoscape.nedrex.internal;

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
import org.cytoscape.nedrex.internal.tasks.NeDRexVisStyleTask;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class FindEdgeBtwNodesTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Induced subnetwork";}
	
	@Tunable(description = "Create a new network from the induced subnetwork:", groups="New network", params="displayState=uncollapsed",
			tooltip = "<html>"+"If selected, it creates a new network from the induced subnetwork of selected nodes. "
					+ "<br> Otherwise, the edges of induced subnetwork will be just selected." + "</html>",
//			tooltip = "In addition to select the edges between selected nodes, create a new network from the induced subnetwork of selected nodes.",
			gravity = 1.0)
	public Boolean create = false;
	
	@Tunable(description="Use custom name for the induced network", groups="New network",
			dependsOn="create=true",
			tooltip = "Select, if you would like to use your own name for the created induced network, otherwise a default name will be assigned",
			gravity = 5.0)
    public Boolean set_net_name = false;
	
	@Tunable(description="Name of the induced network", groups="New network", 
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 5.0)
	public String new_net_name = new String();
	
	public FindEdgeBtwNodesTask(RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network = app.getCurrentNetwork();
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
		Set <CyEdge> interEdges = new HashSet<CyEdge> ();
		
		for (CyNode cn: selectedNodes) {
			for (CyNode cnn: selectedNodes) {
				if (!cn.equals(cnn)) {
					interEdges.addAll(network.getConnectingEdgeList(cn, cnn, CyEdge.Type.ANY));
					interEdges.addAll(network.getConnectingEdgeList(cnn, cn, CyEdge.Type.ANY));
				}
			}
		}
		
		for (CyEdge ce: interEdges) {
			Collection <CyRow> matchedRows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
			for (CyRow cr: matchedRows) {
				cr.set("selected", true);
			}
		}
		
		if (interEdges.size() == 0) {
			logger.info("~~~~ There is no edge between the selected nodes! ");
		}
		
		if (create) {
			if (!set_net_name) {
				/// NewNetworkSelectedNodesAndEdgesTaskFactory: this interface provides a task iterator for creating networks from selected nodes and edges:			
				NewNetworkSelectedNodesAndEdgesTaskFactory newNetTF = app.getActivator().getService(NewNetworkSelectedNodesAndEdgesTaskFactory.class);
				insertTasksAfterCurrentTask(newNetTF.createTaskIterator(network));
			}
			else if(set_net_name) {
				CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
				String newNetName = new String();
				newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
				
				Map<String, Object> argsC = new HashMap<>();
				argsC.put("networkName", newNetName);
				argsC.put("nodeList", "selected");
				argsC.put("edgeList", "selected");
				argsC.put("excludeEdges", true);
				argsC.put("source", "current");
				CommandExecuter cmdexC = new CommandExecuter(app);
				cmdexC.executeCommand("network", "create", argsC, null);
			}
			
			insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));
		}		
	}

}
