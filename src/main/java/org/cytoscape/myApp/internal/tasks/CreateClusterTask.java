package org.cytoscape.myApp.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.myApp.internal.CommandExecuter;
import org.cytoscape.myApp.internal.ModelUtil;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CreateClusterTask extends AbstractTask{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Tunable(description="Name of the new clustered network:", groups="New network properties", params="displayState=uncollapsed",
			gravity = 6.0)
	public String subnetName;
	
	@Tunable(description="File path:" ,  params= "input=true", groups="Select from ID list file",
			gravity = 4.0,
			tooltip = "Select the file containing the disorders you want to view their clusters")
	public File file;
	
	
	/*private ListSingleSelection<String> attributeNodeName ;
	@Tunable(description = "The column to use as name for nodes:", groups= {"Viewing clusters"}, params="displayState=uncollapsed", 
			tooltip = "The column to use as name for nodes should be selected.",
	         gravity = 1.0)
	
	public ListSingleSelection<String> getattributeNodeName(){
		attributeNodeName = ModelUtil.updateNodeAttributeListSt(app, attributeNodeName);
		return attributeNodeName;
	}
	public void setattributeNodeName(ListSingleSelection<String> attr) { }*/
	
	
	private ListSingleSelection<String> attributeEdge ;
	@Tunable(description = "The column to copy as edge weight:", groups= {"Viewing clusters"}, params="displayState=uncollapsed", 
			tooltip = "The column which contains edge weight should be selected.",
	         gravity = 3.0)
	
	public ListSingleSelection<String> getattributeEdge(){
		attributeEdge = ModelUtil.updateEdgeAttributeList(app, attributeEdge);
		return attributeEdge;
	}
	public void setattributeEdge(ListSingleSelection<String> attr) { }
	
	
	private ListSingleSelection<String> displayNameAttr ;
	@Tunable(description = "The column containing description:", groups= {"Viewing clusters"}, params="displayState=uncollapsed", 
			tooltip = "The column which contains description about nodes should be selected.",
	         gravity = 2.0)
	
	public ListSingleSelection<String> getdisplayNameAttr(){
		displayNameAttr = ModelUtil.updateNodeAttributeListSt(app, displayNameAttr);
		return displayNameAttr;
	}
	public void setdisplayNameAttr(ListSingleSelection<String> attr) { }
	
	/*private ListMultipleSelection<String> attributeEdge ;
	@Tunable(description = "The column to copy as edge weight:", groups= {"Viewing clusters"}, params="displayState=uncollapsed", 
			tooltip = "The column which contains edge weight should be selected.",
	         gravity = 2.0)
	
	public ListMultipleSelection<String> getattributeEdge(){
		attributeEdge = ModelUtil.updateEdgeAttributeMultiList(app, attributeEdge);
		return attributeEdge;
	}
	public void setattributeEdge(ListMultipleSelection<String> attr) { }*/
	
	private ListSingleSelection<String> clusterCol ;
	@Tunable(description = "The column to use as cluster name:", groups= {"Viewing clusters"}, params="displayState=uncollapsed", 
			tooltip = "The column which contains clusters names should be selected.",
	         gravity = 1.0)
	
	public ListSingleSelection<String> getclusterCol(){
		clusterCol = ModelUtil.updateNodeAttributeListSt(app, clusterCol);
		return clusterCol;
	}
	public void setclusterCol(ListSingleSelection<String> attr) { }
	
	
	public CreateClusterTask (RepoApplication app) {
		this.app = app;
	}
	
	public void makeClusters (CyNetwork network, CyNetwork newNet, String clustName) {

		String eAttrCol = attributeEdge.getSelectedValue();
//		List<String> eAttrCols = attributeEdge.getSelectedValues();
		String clCol = clusterCol.getSelectedValue();
//		String nodeNameCol = attributeNodeName.getSelectedValue();
		String dispNameCol = displayNameAttr.getSelectedValue();
		String nodeNameCol = "name";
		
		CyTable nodeTable = network.getDefaultNodeTable();
		Collection <CyRow> selCyRowNodes = new HashSet<CyRow>();
		if (nodeTable.getColumn(clCol).getType().equals(String.class)) {
			selCyRowNodes = nodeTable.getMatchingRows(clCol, clustName);
		}
		else if (nodeTable.getColumn(clCol).getType().equals(Integer.class)){
			selCyRowNodes = nodeTable.getMatchingRows(clCol, Integer.valueOf(clustName));
		}
		String delims = "[ ()]";
		
		Map <String, String> nAttrMap = new HashMap<String, String> ();
		Map <String, String> dispMap = new HashMap<String, String> ();
		Map <String, CyNode> selectedNodes = new HashMap<String, CyNode> ();
		for (CyRow cr: selCyRowNodes) {	
			selectedNodes.put(cr.get(nodeNameCol, String.class), network.getNode(cr.get("SUID", Long.class)));
			if (nodeTable.getColumn(clCol).getType().equals(String.class)) {
				nAttrMap.put(cr.get(nodeNameCol, String.class), cr.get(clCol, String.class));
			}
			else if (nodeTable.getColumn(clCol).getType().equals(Integer.class)) {
				nAttrMap.put(cr.get(nodeNameCol, String.class), cr.get(clCol, Integer.class).toString());
			}
			dispMap.put(cr.get(nodeNameCol, String.class), cr.get(dispNameCol, String.class));
		}
		
		List <CyRow> lcr = network.getDefaultEdgeTable().getAllRows();
		Map<Set<String>, Double> eAttrMap = new HashMap<Set<String>, Double>();
		for (CyRow cr : lcr) {
			String edgeName = cr.get("name", String.class);
			String [] edgeTokens = edgeName.split(delims);
			Set<String> nSet = new HashSet<String>();
			if(selectedNodes.containsKey(edgeTokens[0]) && selectedNodes.containsKey(edgeTokens[edgeTokens.length-1])) {
				nSet.add(edgeTokens[0]);
				nSet.add(edgeTokens[edgeTokens.length-1]);
				//eAttrMap.put(nSet, cr.get("logOfRR", Double.class));
				eAttrMap.put(nSet, cr.get(eAttrCol, Double.class));
			}
		}

		Map <String, CyNode> mapCyNode = new HashMap<String, CyNode> ();
		
		for (Entry<String, CyNode> entry: selectedNodes.entrySet()) {
			CyNode node = newNet.addNode();
			newNet.getDefaultNodeTable().getRow(node.getSUID()).set("name", entry.getKey());
			mapCyNode.put(entry.getKey(), node);
			newNet.getDefaultNodeTable().getRow(node.getSUID()).set(clCol, nAttrMap.get(entry.getKey()));
			newNet.getDefaultNodeTable().getRow(node.getSUID()).set(dispNameCol, dispMap.get(entry.getKey()));
		}
		
		for (Entry<String, CyNode> entry1: selectedNodes.entrySet()) {
			for (Entry<String, CyNode> entry2: selectedNodes.entrySet()) {
				if (!entry1.getKey().equals(entry2.getKey())) {
					if(network.containsEdge(entry1.getValue(), entry2.getValue()) && newNet.getDefaultEdgeTable().getMatchingRows("name", entry2.getKey() + " (-) " + entry1.getKey()).size()==0) {
						CyEdge e = newNet.addEdge(mapCyNode.get(entry1.getKey()), mapCyNode.get(entry2.getKey()), false);
						newNet.getDefaultEdgeTable().getRow(e.getSUID()).set("name", entry1.getKey() + " (-) " + entry2.getKey());
						newNet.getDefaultEdgeTable().getRow(e.getSUID()).set("interaction", "(-)");
						Set<String> nSet = new HashSet<String>();
						nSet.add(entry1.getKey());
						nSet.add(entry2.getKey());
						newNet.getDefaultEdgeTable().getRow(e.getSUID()).set(eAttrCol, eAttrMap.get(nSet));
					}					
				}
			}
		}

	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetworkManager netMgr = app.getActivator().getService(CyNetworkManager.class);
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		CyNetworkFactory cnf = app.getActivator().getService(CyNetworkFactory.class);
		CyNetwork network = app.getCurrentNetwork();
		Set<String> clustNames = new HashSet<String>();
		
		Set<String> nodeNameSet = new HashSet<String>();
		String fp = file.getPath();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fp));
			String dataRow = br.readLine();//skip header line?
			while (dataRow != null){
				String[] data = dataRow.split("\t");
				//nodesToSelect.add(dataRow);
				nodeNameSet.add(data[0]);
				dataRow = br.readLine();
			}
		}		
		catch (IOException e) {e.printStackTrace();}
		
		for (String nodeName:nodeNameSet) {
			List <CyRow> rows = new ArrayList<CyRow>(network.getDefaultNodeTable().getMatchingRows("name", nodeName));
			if (rows.size() > 0) {
				for (CyRow cr: rows) {
					clustNames.add(cr.get(clusterCol.getSelectedValue(), String.class));
				}
			}			
		}

		CyNetwork newNet = cnf.createNetwork();
		newNet.getRow(newNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(subnetName));
		newNet.getDefaultEdgeTable().createColumn(attributeEdge.getSelectedValue(), Double.class, false);
//		for (String s: attributeEdge.getSelectedValues()) {
//			newNet.getDefaultEdgeTable().createColumn(s, network.getDefaultEdgeTable().getColumn(s).getType(), false);
//		}
		newNet.getDefaultNodeTable().createColumn(clusterCol.getSelectedValue(), String.class, false);
		newNet.getDefaultNodeTable().createColumn(displayNameAttr.getSelectedValue(), String.class, false);
		
		for (String s: clustNames) {
			makeClusters(network, newNet, s);
		}
		
		netMgr.addNetwork(newNet);
		
	    // create a view for the current network
		Map<String, Object> args = new HashMap<>();
		args.put("network", "current");
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("view", "create", args, null);
		
		// Update the layout as following:
		CyLayoutAlgorithmManager layAlgMan = app.getCyLayoutAlgorithmManager();
		/*List<CyLayoutAlgorithm>  layalglist = new ArrayList<CyLayoutAlgorithm>(layAlgMan.getAllLayouts());
		logger.info("The list of all layout algorithms: " + layalglist);
		for (CyLayoutAlgorithm cla: layalglist) {
			logger.info("The name of the layout algorithm for: " + cla + "---> " + cla.getName());
		}*/
		CyLayoutAlgorithm layAlg = layAlgMan.getLayout("circular");
		TaskIterator itr = layAlg.createTaskIterator(app.getCurrentNetworkView(),layAlg.createLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS,null);
		app.getTaskManager().execute(itr);
		
	}

}
