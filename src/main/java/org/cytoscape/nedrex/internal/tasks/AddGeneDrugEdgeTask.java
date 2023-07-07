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
public class AddGeneDrugEdgeTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Map<CyNode, Set<CyNode>> geneDrugMap;
	private Map<CyNode, Set<CyNode>> ggiEdgeMap;
	private Map<CyNode, Set<CyNode>> ppiEdgeMap;
	private String newNetworkName;
	
	public AddGeneDrugEdgeTask(RepoApplication app, Map<CyNode, Set<CyNode>> geneDrugMap, Map<CyNode, Set<CyNode>> ggiEdgeMap, Map<CyNode, Set<CyNode>> ppiEdgeMap, String newNetworkName) {
		this.app = app;
		this.geneDrugMap = geneDrugMap;
		this.ggiEdgeMap = ggiEdgeMap;
		this.ppiEdgeMap = ppiEdgeMap;
		this.newNetworkName = newNetworkName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		String edgeTypeCol = "type";		
		CyNetwork currNet = ModelUtil.getNetworkWithName(app, newNetworkName);
		CyTable edgeTable  = currNet.getDefaultEdgeTable();
		logger.info("The newly built network:" + currNet.getSUID().toString());
		for (Entry<CyNode, Set<CyNode>> entry: geneDrugMap.entrySet()) {
			for (CyNode dr: entry.getValue()) {
				CyEdge ce = currNet.addEdge(dr, entry.getKey(), false);
				edgeTable.getRow(ce.getSUID()).set("name",  currNet.getRow(ce.getSource()).get("name", String.class) + " (-) " + currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set("sourceDomainId", currNet.getRow(ce.getSource()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set("targetDomainId", currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set(edgeTypeCol, InteractionType.drug_gene.toString());
			}			
		}
		
		for (Entry<CyNode, Set<CyNode>> entry: ppiEdgeMap.entrySet()) {
			for (CyNode p: entry.getValue()) {
				CyEdge ce = currNet.addEdge(p, entry.getKey(), false);
				edgeTable.getRow(ce.getSUID()).set("name",  currNet.getRow(ce.getSource()).get("name", String.class) + " (-) " + currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set("sourceDomainId", currNet.getRow(ce.getSource()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set("targetDomainId", currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set(edgeTypeCol, InteractionType.protein_protein.toString());
			}			
		}
		
		for (Entry<CyNode, Set<CyNode>> entry: ggiEdgeMap.entrySet()) {
			for (CyNode g: entry.getValue()) {
				CyEdge ce = currNet.addEdge(g, entry.getKey(), false);
				edgeTable.getRow(ce.getSUID()).set("name",  currNet.getRow(ce.getSource()).get("name", String.class) + " (-) " + currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set("sourceDomainId", currNet.getRow(ce.getSource()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set("targetDomainId", currNet.getRow(ce.getTarget()).get("name", String.class));
				edgeTable.getRow(ce.getSUID()).set(edgeTypeCol, InteractionType.gene_gene.toString());
			}			
		}
		
		// we can update the layout as following:
		CyLayoutAlgorithmManager layAlgMan = app.getCyLayoutAlgorithmManager();
		CyLayoutAlgorithm layAlg = layAlgMan.getLayout("force-directed");
		TaskIterator itr = layAlg.createTaskIterator(app.getCurrentNetworkView(),layAlg.createLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS,null);
		app.getTaskManager().execute(itr);
		
	}

}
