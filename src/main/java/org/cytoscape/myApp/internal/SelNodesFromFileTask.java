package org.cytoscape.myApp.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
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
public class SelNodesFromFileTask extends AbstractTask{
	
	private RepoApplication app;
	
	@ProvidesTitle
	public String getTitle() {return "Select nodes from file";}
	
	@Tunable(description="Select nodes with exact matching name", groups="Select from ID list file",
			tooltip="If the box is not checked, the result will also include the nodes whose names partly match with the given list",
			gravity = 2.0)
	public boolean exactMatch = true;
	
	@Tunable(description="File path:" ,  params= "input=true", groups="Select from ID list file")
	public File file;
	
	private ListSingleSelection<String> nodeAttr ;
	@Tunable(description = "The column to select node ids from:", groups="Select from ID list file", params="displayState=uncollapsed", 
	         longDescription = "If no node type is used, select ```--NONE---```",
	         gravity = 4.0)
	
	public ListSingleSelection<String> getnodeAttr(){
		nodeAttr = ModelUtil.updateNodeAttributeListSt(app, nodeAttr);
		return nodeAttr;
	}
	public void setnodeAttr(ListSingleSelection<String> attr) { }
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public SelNodesFromFileTask(RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network = app.getCurrentNetwork();
		Set<String> nodesToSelect = new HashSet<String>();
		String fp = file.getPath();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fp));
			String dataRow = br.readLine();//skip header line?
			while (dataRow != null){
				String[] data = dataRow.split("\t");
				nodesToSelect.add(data[0]);
				dataRow = br.readLine();
			}
		}
		
		catch (IOException e) {e.printStackTrace();}
		
		logger.info("The selected nodeAttr: " + nodeAttr.getSelectedValue());
		logger.info("This is the list of nodes to be selected from the loaded file: " + nodesToSelect);
		logger.info("The number of nodes to be selected from the loaded file: " + nodesToSelect.size());
				
		String colSelFrom = nodeAttr.getSelectedValue();
		CyTable nodeTable= network.getDefaultNodeTable();
		
		int numbSelectedNodes = 0;
		if (exactMatch) {
			for (String nodeName:nodesToSelect) {
				List <CyRow> rows = new ArrayList<CyRow>(network.getDefaultNodeTable().getMatchingRows(colSelFrom, nodeName));
				if (rows.size() > 0) {
					rows.get(0).set("selected", true);
					numbSelectedNodes += 1;
				}			
			}			
		}
		
		else if (!exactMatch) {
			for (CyNode node: network.getNodeList()) {
				for (String nodeName:nodesToSelect) {
					CyRow cr = nodeTable.getRow(node.getSUID());
					if (cr.isSet(colSelFrom) && cr.get(colSelFrom, String.class).toLowerCase().contains(nodeName.toLowerCase())) {
						cr.set("selected", true);
						numbSelectedNodes += 1;
					}					
				}
			}
		}		
		logger.info("The number of selected nodes in the current network based on the input file: " + numbSelectedNodes);		
	}

}
