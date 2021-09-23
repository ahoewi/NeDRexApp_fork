package org.cytoscape.myApp.internal;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
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
/// to be completed for DisDrugTargetTask
public class CreatePDisEdgesTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Map<CyNode, Set<CyNode>> disProtsMap;
	private String newNetworkName;
	
	public CreatePDisEdgesTask (RepoApplication app, Map<CyNode, Set<CyNode>> disProtsMap, String newNetworkName) {
		this.app = app;
		this.disProtsMap = disProtsMap;
		this.newNetworkName = newNetworkName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		String edgeTypeCol = "type";		
		CyNetwork currNet = ModelUtil.getNetworkWithName(app, newNetworkName);
		CyTable edgeTable  = currNet.getDefaultEdgeTable();
		logger.info("Here we get the newly built network:" + currNet.getSUID().toString());
		
		for (Entry<CyNode, Set<CyNode>> entry: disProtsMap.entrySet()) {
			for (CyNode pn: entry.getValue()) {
				logger.info("Protein node as source to use for the new edge: " + pn);
				CyEdge ce = currNet.addEdge(pn, entry.getKey(), false);
				edgeTable.getRow(ce.getSUID()).set("name",  currNet.getRow(ce.getSource()).get("name", String.class) + " (-) " + currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set(edgeTypeCol, InteractionType.protein_disease.toString());
				edgeTable.getRow(ce.getSUID()).set("sourceDomainId",  currNet.getRow(ce.getSource()).get("primaryDomainId", String.class));
				edgeTable.getRow(ce.getSUID()).set("targetDomainId",  currNet.getRow(ce.getTarget()).get("primaryDomainId", String.class));
			}
		}
		
		CyLayoutAlgorithmManager layAlgMan = app.getCyLayoutAlgorithmManager();
		CyLayoutAlgorithm layAlg = layAlgMan.getLayout("force-directed");
		TaskIterator itr = layAlg.createTaskIterator(app.getCurrentNetworkView(),layAlg.createLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS,null);
		app.getTaskManager().execute(itr);
				
	}

}
