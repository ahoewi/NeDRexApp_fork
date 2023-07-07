package org.cytoscape.nedrex.internal.tasks;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.nedrex.internal.InteractionType;
import org.cytoscape.nedrex.internal.ModelUtil;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class AddDrugDiseaseEdgeTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Map<CyNode, Set<CyNode>> disorderDrugMap;
	private String newNetworkName;
	
	public AddDrugDiseaseEdgeTask(RepoApplication app, String newNetworkName, Map<CyNode, Set<CyNode>> disorderDrugMap) {
		this.app = app;
		this.newNetworkName = newNetworkName;
		this.disorderDrugMap = disorderDrugMap;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		String edgeTypeCol = "type";		
		CyNetwork currNet = ModelUtil.getNetworkWithName(app, newNetworkName);
		CyTable edgeTable  = currNet.getDefaultEdgeTable();
		logger.info("The newly built network:" + currNet.getSUID().toString());
		for (Entry<CyNode, Set<CyNode>> entry: disorderDrugMap.entrySet()) {
			for (CyNode dr: entry.getValue()) {
				if(!currNet.containsEdge(dr, entry.getKey())) { // to avoid duplicate edges, it can happen that the parent already have the edges to the child's drugs
					CyEdge ce = currNet.addEdge(dr, entry.getKey(), false);
					edgeTable.getRow(ce.getSUID()).set("name",  currNet.getRow(ce.getSource()).get("name", String.class) + " (-) " + currNet.getRow(ce.getTarget()).get("name", String.class));
					edgeTable.getRow(ce.getSUID()).set("sourceDomainId", currNet.getRow(ce.getSource()).get("name", String.class));
					edgeTable.getRow(ce.getSUID()).set("targetDomainId", currNet.getRow(ce.getTarget()).get("name", String.class));
					edgeTable.getRow(ce.getSUID()).set(edgeTypeCol, InteractionType.drug_disease.toString());
				}				
			}			
		}
		
		// Update the layout as following:
				CyLayoutAlgorithmManager layAlgMan = app.getCyLayoutAlgorithmManager();
				CyLayoutAlgorithm layAlg = layAlgMan.getLayout("force-directed");
				TaskIterator itr = layAlg.createTaskIterator(app.getCurrentNetworkView(),layAlg.createLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS,null);
				app.getTaskManager().execute(itr);
		
	}

}
