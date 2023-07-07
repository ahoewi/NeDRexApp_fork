package org.cytoscape.nedrex.internal.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.CommandExecuter;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.utils.FilterType;
import org.cytoscape.session.CyNetworkNaming;
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
public class SelectNeighborNodeOfType extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private String connector = "Only edges connecting selected nodes and the neighbors of specified type";
	private String allEdges = "All edges between nodes (selected + neighbors of specified type)";
	
	@ProvidesTitle
	public String getTitle() {return "Select neighbors of specific type";}
	
	@Tunable(description = "Neighbor type:", groups="Options", params="displayState=uncollapsed", 
			tooltip = "Select nodes of specified type which are direct neighbors of selected nodes.",
	         gravity = 1.0)
	public ListSingleSelection<String> neighbTypes = new ListSingleSelection<String> (NodeType.Gene.toString(), NodeType.Disease.toString(), NodeType.Protein.toString(), NodeType.Drug.toString(), NodeType.Pathway.toString());
	

	@Tunable(description = "Edge selection:", groups="Options", params="displayState=uncollapsed", 
			tooltip = "Options for edge selections.",
	         gravity = 2.0)
	public ListSingleSelection<String> edgeSelectionOptions = new ListSingleSelection<String> (connector, allEdges);
	
	@Tunable(description="Create a new network from selection", groups="New network", params="displayState=uncollapsed",
			tooltip = "If selected, it also creates a new network from the selected nodes and edges.",
			gravity = 4.0)
	public boolean create = false;
	
	@Tunable(description="Use custom name for the new network", groups="New network",
			dependsOn="create=true",
			tooltip = "Select, if you would like to use your own name for the created induced network, otherwise a default name will be assigned",
			gravity = 5.0)
    public Boolean set_net_name = false;
	
	@Tunable(description="Name of the new network", groups="New network", 
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 5.0)
	public String new_net_name = new String();
	
	public SelectNeighborNodeOfType (RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network =app.getCurrentNetwork();
		Set<CyEdge> connectorEdges = new HashSet<CyEdge>();
		Set<CyNode> selectedNodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(app.getCurrentNetwork(),"selected",true));
		Set<CyNode> neighborNodes = new HashSet<CyNode>();
		for (CyNode n: selectedNodes) {
			neighborNodes.addAll(FilterType.neighborNodesOfType(network, n, NodeType.getType(neighbTypes.getSelectedValue())));
			if (edgeSelectionOptions.getSelectedValue().equals(connector)) {
				for (CyNode nn: FilterType.neighborNodesOfType(network, n, NodeType.getType(neighbTypes.getSelectedValue()))) {
					connectorEdges.addAll(network.getConnectingEdgeList(n, nn, CyEdge.Type.ANY));
					connectorEdges.addAll(network.getConnectingEdgeList(nn, n, CyEdge.Type.ANY));
				}
			}
		}
		for (CyNode n: neighborNodes) {
			network.getDefaultNodeTable().getRow(n.getSUID()).set("selected", true);
		}
		if (edgeSelectionOptions.getSelectedValue().equals(connector)) {
			for (CyEdge e: connectorEdges) {
				network.getDefaultEdgeTable().getRow(e.getSUID()).set("selected", true);
			}
		}
		else if (edgeSelectionOptions.getSelectedValue().equals(allEdges)) {
			selectedNodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(app.getCurrentNetwork(),"selected",true));
			for (CyNode n: selectedNodes) {
				for (CyNode nn: selectedNodes) {
					if (!n.equals(nn)) {
						connectorEdges.addAll(network.getConnectingEdgeList(n, nn, CyEdge.Type.ANY));
						connectorEdges.addAll(network.getConnectingEdgeList(nn, n, CyEdge.Type.ANY));
					}
				}
			}
			for (CyEdge e: connectorEdges) {
				network.getDefaultEdgeTable().getRow(e.getSUID()).set("selected", true);
			}
		}
		
		if (create == true) {
			String newNetName = new String();
			/// now create a new network from the last step as a subnetwork of the entire network
			if (set_net_name) {
				CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);			
				newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
			}			
			Map<String, Object> argsC = new HashMap<>();
			argsC.put("networkName", newNetName);
			argsC.put("nodeList", "selected");
			argsC.put("edgeList", "selected");
			argsC.put("excludeEdges", true);
			argsC.put("source", "current");
			CommandExecuter cmdexC = new CommandExecuter(app);
			cmdexC.executeCommand("network", "create", argsC, null);
			
			insertTasksAfterCurrentTask(new NeDRexVisStyleTask(app));
		}

	}

}
