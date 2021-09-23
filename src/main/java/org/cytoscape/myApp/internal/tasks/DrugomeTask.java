package org.cytoscape.myApp.internal.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class DrugomeTask extends AbstractTask{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String sharedDis = "shared indications";
	private String sharedTarget = "shared targets";
	
	@ProvidesTitle
	public String getTitle() {return "Drug-Drug network";}
	
	@Tunable(description = "Create Drugome based on:", groups="Options", //params="displayState=uncollapsed", 
	        // tooltip = "",
	         gravity = 2.0)
	public ListSingleSelection<String> options = new ListSingleSelection<String> (sharedDis, sharedTarget);
	
	
	public DrugomeTask (RepoApplication app) {
		this.app = app;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		CyNetwork network = app.getCurrentNetwork();		
		Set<CyNode> nodes_to_add = new HashSet<CyNode>();
		Set<Set<CyNode>> edges_to_add = new HashSet<Set<CyNode>>();
		
		Map <Set<CyNode>, Set<CyNode>> sharedDisMap = new HashMap  <Set<CyNode>, Set<CyNode>> ();
		Map <Set<CyNode>, Set<CyNode>> sharedTargetMap = new HashMap  <Set<CyNode>, Set<CyNode>> ();
		
		if (options.getSelectedValue().equals(sharedDis)) {
			Set<CyNode> diseases = FilterType.nodesOfType(network, NodeType.Disease);
			for (CyNode d: diseases) {
				List<CyNode> neighbDrugs = FilterType.neighborNodesOfTypeList(network, d, NodeType.Drug);
				if (neighbDrugs.size() > 1) {
					for (int i=0; i < neighbDrugs.size()-1; i ++) {
						for (int j=i+1; j <neighbDrugs.size(); j++) {
							Set<CyNode> ddEdge = new HashSet<CyNode>();
							ddEdge.add(neighbDrugs.get(i));
							ddEdge.add(neighbDrugs.get(j));
							sharedDisMap.putIfAbsent(ddEdge, new HashSet<CyNode>());
							sharedDisMap.get(ddEdge).add(d);
							nodes_to_add.add(neighbDrugs.get(i));
							nodes_to_add.add(neighbDrugs.get(j));
							edges_to_add.add(ddEdge);
						}						
					}
				}
			}				
		}
		
		else if (options.getSelectedValue().equals(sharedTarget)) {
			Set<CyNode> targets = FilterType.nodesOfType(network, NodeType.Protein);
			for (CyNode t: targets) {
				List<CyNode> neighbDrugs = FilterType.neighborNodesOfTypeList(network, t, NodeType.Drug);
				if (neighbDrugs.size() > 1) {
					for (int i=0; i < neighbDrugs.size()-1; i ++) {
						for (int j=i+1; j <neighbDrugs.size(); j++) {
							Set<CyNode> ddEdge = new HashSet<CyNode>();
							ddEdge.add(neighbDrugs.get(i));
							ddEdge.add(neighbDrugs.get(j));
							sharedTargetMap.putIfAbsent(ddEdge, new HashSet<CyNode>());
							sharedTargetMap.get(ddEdge).add(t);
							nodes_to_add.add(neighbDrugs.get(i));
							nodes_to_add.add(neighbDrugs.get(j));
							edges_to_add.add(ddEdge);
						}						
					}
					
				}
			}	
		}
		insertTasksAfterCurrentTask(new CreateDrugomeTask(app, network, nodes_to_add, edges_to_add, sharedDisMap, sharedTargetMap, options.getSelectedValue()));
		
	}

}
