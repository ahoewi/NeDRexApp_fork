package org.cytoscape.nedrex.internal.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.utils.FilterType;
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
public class DiseasomeTask extends AbstractTask{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String sharedGene = "shared genes";
	private String sharedDrug = "shared drugs";
	
	@ProvidesTitle
	public String getTitle() {return "Disease-Disease network";}
	
	@Tunable(description = "Create Diseasome based on:", groups="Options", //params="displayState=uncollapsed", 
	        // tooltip = "",
	         gravity = 2.0)
	public ListSingleSelection<String> options = new ListSingleSelection<String> (sharedDrug, sharedGene);
	
	public DiseasomeTask (RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network = app.getCurrentNetwork();		
		Set<CyNode> nodes_to_add = new HashSet<CyNode>();
		Set<Set<CyNode>> edges_to_add = new HashSet<Set<CyNode>>();
		
		Map <Set<CyNode>, Set<CyNode>> sharedDrugMap = new HashMap  <Set<CyNode>, Set<CyNode>> ();
		Map <Set<CyNode>, Set<CyNode>> sharedGeneMap = new HashMap  <Set<CyNode>, Set<CyNode>> ();
		
		if (options.getSelectedValue().equals(sharedDrug)) {
			Set<CyNode> drugs = FilterType.nodesOfType(network, NodeType.Drug);
			for (CyNode dr: drugs) {
				List<CyNode> neighbDiseases = FilterType.neighborNodesOfTypeList(network, dr, NodeType.Disease);
				if (neighbDiseases.size() > 1) {
					for (int i=0; i < neighbDiseases.size()-1; i ++) {
						for (int j=i+1; j < neighbDiseases.size(); j++) {
							Set<CyNode> ddEdge = new HashSet<CyNode>();
							ddEdge.add(neighbDiseases.get(i));
							ddEdge.add(neighbDiseases.get(j));
							sharedDrugMap.putIfAbsent(ddEdge, new HashSet<CyNode>());
							sharedDrugMap.get(ddEdge).add(dr);
							nodes_to_add.add(neighbDiseases.get(i));
							nodes_to_add.add(neighbDiseases.get(j));
							edges_to_add.add(ddEdge);
						}						
					}
				}
			}
		}
		
		else if (options.getSelectedValue().equals(sharedGene)) {
			Set<CyNode> genes = FilterType.nodesOfType(network, NodeType.Gene);
			for (CyNode g: genes) {
				List<CyNode> neighbDiseases = FilterType.neighborNodesOfTypeList(network, g, NodeType.Disease);
				if (neighbDiseases.size() > 1) {
					for (int i=0; i < neighbDiseases.size()-1; i ++) {
						for (int j=i+1; j < neighbDiseases.size(); j++) {
							Set<CyNode> ddEdge = new HashSet<CyNode>();
							ddEdge.add(neighbDiseases.get(i));
							ddEdge.add(neighbDiseases.get(j));
							sharedGeneMap.putIfAbsent(ddEdge, new HashSet<CyNode>());
							sharedGeneMap.get(ddEdge).add(g);
							nodes_to_add.add(neighbDiseases.get(i));
							nodes_to_add.add(neighbDiseases.get(j));
							edges_to_add.add(ddEdge);
						}						
					}
				}
			}
		}
			
		insertTasksAfterCurrentTask(new CreateDiseaseomeTask(app, network, nodes_to_add, edges_to_add, sharedDrugMap, sharedGeneMap, options.getSelectedValue()));
		
	}

}
