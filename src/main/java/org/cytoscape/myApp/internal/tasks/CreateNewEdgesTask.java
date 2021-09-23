package org.cytoscape.myApp.internal.tasks;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.myApp.internal.InteractionType;
import org.cytoscape.myApp.internal.ModelUtil;
import org.cytoscape.myApp.internal.RepoApplication;
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
public class CreateNewEdgesTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Map<String, Set<String>> sPtDmap;
	private Map<CyNode, Set<CyNode>> sPtDCyNodemap;
	private String netSuid;
	private String newNetworkName;
	
	public CreateNewEdgesTask (RepoApplication app, Map<String, Set<String>> sPtDmap, Map<CyNode, Set<CyNode>> sPtDCyNodemap, String netSuid, String newNetworkName) {
		this.app = app;
		this.sPtDmap = sPtDmap;
		this.sPtDCyNodemap = sPtDCyNodemap;
		this.netSuid = netSuid;
		this.newNetworkName = newNetworkName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		String edgeTypeCol = "type";		
		CyNetwork currNet = ModelUtil.getNetworkWithName(app, newNetworkName);
		CyTable edgeTable  = currNet.getDefaultEdgeTable();
		logger.info("Here we get the newly built network:" + currNet.getSUID().toString());
		for (Entry<CyNode, Set<CyNode>> entry: sPtDCyNodemap.entrySet()) {
			for (CyNode dn: entry.getValue()) {
				CyEdge ce = currNet.addEdge(entry.getKey(), dn, false);
				edgeTable.getRow(ce.getSUID()).set("name",  currNet.getRow(ce.getSource()).get("name", String.class) + " (-) " + currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set(edgeTypeCol, InteractionType.protein_disease.toString());
			}			
		}
		
		// we can update the layout as following:
		CyLayoutAlgorithmManager layAlgMan = app.getCyLayoutAlgorithmManager();
		CyLayoutAlgorithm layAlg = layAlgMan.getLayout("force-directed");
		TaskIterator itr = layAlg.createTaskIterator(app.getCurrentNetworkView(),layAlg.createLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS,null);
		app.getTaskManager().execute(itr);
		
	}
	

}
