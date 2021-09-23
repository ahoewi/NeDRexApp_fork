package org.cytoscape.myApp.internal.tasks;

import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.myApp.internal.NodeType;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.utils.FilterType;
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
public class SelectAllNodeOfType extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@ProvidesTitle
	public String getTitle() {return "Select nodes of type";}
	
	@Tunable(description = "Node type:", groups="Options", params="displayState=uncollapsed", 
			tooltip = "Select all nodes of the specified type in the current network.",
	         gravity = 1.0)
	public ListSingleSelection<String> nodeTypes = new ListSingleSelection<String> (NodeType.Gene.toString(), NodeType.Disease.toString(), NodeType.Protein.toString(), NodeType.Drug.toString(), NodeType.Pathway.toString());
	
	public SelectAllNodeOfType(RepoApplication app) {
		this.app = app;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network =app.getCurrentNetwork();		
		Set<CyNode> nodesToSelect = FilterType.nodesOfType(network, NodeType.getType(nodeTypes.getSelectedValue()));		
		for (CyNode n: nodesToSelect) {
			network.getRow(n).set("selected", true);
		}
	}

}
