package org.cytoscape.myApp.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.NodeType;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.BoundedInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class DiamondTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Set Parameters for DIAMOnD Algorithm";}
	
	@Tunable(description="Number of DIAMOnD genes (iteration)", groups="Algorithm settings",
			params="slider=true",
			tooltip="Desired number of DIAMOnD genes/proteins, or number of iterations",
			gravity = 2.0)
    public BoundedInteger iter = new BoundedInteger(1, 200, 500, false, false);
	
	@Tunable(description="Weight of seeds", groups="Algorithm settings",
			params="slider=true",
			tooltip="Alpha parameter, representing initial weight of the seeds, default value: 1",
			gravity = 2.5)
    public BoundedInteger alpha = new BoundedInteger(1, 1, 10, false, false);
	
	@Tunable(description="Read seeds from a file", groups="Input seeds",
			tooltip="If selected, gene seeds will be read from a file. Otherwise, selected nodes in the current network will be taken as seeds.",
			gravity = 3.0)
    public Boolean seedFile = false;
	
	@Tunable(description="Input file for seeds:" ,  params= "input=true", 
			groups="Input seeds",
			dependsOn="seedFile=true",
			tooltip="Input file contains list of seeds, one seed per line. The file should be a tab-separated file and the first column will be taken as seeds. Genes with Entrez IDs are acceptable as input.",
			gravity = 3.5)
	public File inputFile = new File(System.getProperty("user.home"));
	
	@Tunable(description="Return all edges in the result disease module", groups="Result network",
			tooltip = "<html>" +
		    		"If selected, all edges between genes in the result disease module will be returned,"
		    		+ "<br> otherwise only edges between seeds and new nodes will be returned."
		    		+ "<br> For large disease modules, the latter option is recommended for better visualization."
		    		+ "</html>",
			gravity = 4.0)
    public Boolean all_edges = false;
	
	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name based on the selected algorithm parameters will be assigned.",
			gravity = 5.0)
    public Boolean set_net_name = false;
	
	@Tunable(description="Name of the result network", 
	         groups="Result network", 
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 5.0)
	public String new_net_name = new String();
	
	public DiamondTask (RepoApplication app) {
		this.app = app;
	}
	
	protected void showWarningTime() {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, "The computation is taking very long! It continues running in the backend, to get the results please try again using the same parameters and input for the algorithm in 15 mins!", "Long run-time", JOptionPane.WARNING_MESSAGE);
				}
			}
		);
	}
	
	protected void showFailed() {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
//					JOptionPane.showMessageDialog(null, "The request to run the algorithm is failed! Please make sure that either proteins or genes are selected in the network. Uniport AC and entrez Id are acceptable as names for proteins and genes.", "Error", JOptionPane.ERROR_MESSAGE);
					JOptionPane.showMessageDialog(null, "The request to run the algorithm is failed! Please check your inputs.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		String newNetName = new String();
		if (!set_net_name) {					
			String netName = String.format("DIAMOnD_iter%d_alpha%d", iter.getValue(), alpha.getValue());
			newNetName = namingUtil.getSuggestedNetworkTitle(netName);
		}
		else if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
//		String submit_url = "https://api.repotrial.net/diamond/submit";
//		String status_url = "https://api.repotrial.net/diamond/status";
		String submit_url = Constant.API_LINK + "diamond/submit";
		String status_url = Constant.API_LINK + "diamond/status";
		
		JSONObject payload = new JSONObject();
		List<String> seeds = new ArrayList<String>();		
		List<String> selectedNodeNames = new ArrayList<String>();
		Boolean ggType = false;		
		Set<String> seeds_in_network = new HashSet<String>();
		int sleep_time = 3; //in seconds
		
		if (!seedFile) {
			List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
			if (network.getRow(selectedNodes.get(0)).get("type", String.class).equals(NodeType.Gene.toString())) {
				ggType = true;
			}
			if (!ggType) {
				for (CyNode n: selectedNodes) {
					selectedNodeNames.add(network.getRow(n).get(CyNetwork.NAME, String.class).replaceFirst("uniprot.", ""));
				}
			}
			else if (ggType) {
				for (CyNode n: selectedNodes) {
					selectedNodeNames.add(network.getRow(n).get(CyNetwork.NAME, String.class).replaceFirst("entrez.", ""));
				}
			}			
			seeds = selectedNodeNames;
		
		}
		else if (seedFile) { // we assume from file we always have genes and not proteins
			ggType = true;
			List<String> nodesToSelect = new ArrayList<String>();
			String fp = inputFile.getPath();
			try {
				BufferedReader br = new BufferedReader(new FileReader(fp));
				String dataRow = br.readLine();//skip header line
				while (dataRow != null){
					String[] data = dataRow.split("\t");
					nodesToSelect.add(data[0].replaceFirst("entrez.", ""));
					dataRow = br.readLine();
				}
			}
			
			catch (IOException e) {e.printStackTrace();}
			seeds = nodesToSelect;
		}
		
		payload.put("seeds", seeds);
		payload.put("alpha", alpha.getValue());
		payload.put("n", iter.getValue());
		payload.put("network", "DEFAULT");
		if (!all_edges) {
			payload.put("edges", "limited");
		}
		
		logger.info("The post JSON converted to string: " + payload.toString());
		
		HttpPost post = new HttpPost(submit_url);
		HttpClient client = new DefaultHttpClient();
		post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
		String uidd = new String();
		Boolean failedSubmit = false;
		try {
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = "";
			logger.info("Response entity: ");
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("The status code of the response: " + statusCode);
			if (statusCode != 200) {
				failedSubmit=true;
			}
//		    assertThat(statusCode, equals(HttpStatus.SC_OK));
//		    assertThat(statusCode, equalTo(HttpStatus.SC_OK));
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				logger.info("The uri of the response to the post: "+line + "\n");
				uidd = line;
				if (line.contains("No seed genes submitted")) {
					failedSubmit=true;
				}
				/*if (line.contains("Internal Server Error")) {
					failedSubmit=true;
				}
				if (line.length() < 3) {
					failedSubmit = true;
				}*/				
			  }
			EntityUtils.consume(entity);
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//// now GET
		if (!failedSubmit) {
			taskMonitor.setProgress(0.1);
			taskMonitor.setStatusMessage("Processing your request...");
			String uid = uidd.replace("\"", "");
			HttpGet request = new HttpGet(status_url);
			URI uri = new URIBuilder(request.getURI()).addParameter("uid", uid).build();
			((HttpRequestBase) request).setURI(uri);
			
//			logger.info("The URI: "+ uri);
			logger.info("The uid: " + uid);		
//			logger.info("The request URI: "+request.getURI().toString());
//			logger.info("The request line: "+ request.getRequestLine());
			
			boolean Success = false;
			try {
				HttpResponse response = client.execute(request);
//				boolean Success = false;
				boolean Failed = false;
				  
				  // we're letting it to run for t*3 seconds
				double n = 200;
				for (int t=0; t<200; t++) {
					taskMonitor.setProgress(0.1+ t* (1.0-0.1)/n);
					String  responseText = "";
					BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
						System.out.println(line);
//						logger.info("The response entity of the status: " +line);
						if (line.contains("completed"))
							Success=true;
							responseText = line;
						if (line.contains("failed")) {
							Failed=true;
						}
					}
					if(Success) {
						logger.info("The run is successfully completed! This is the response: " + response.getParams());
//						logger.info("The status line of the response:" + response.getStatusLine());
//						logger.info("This is the response text of the successful: " + responseText);
						
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(responseText);
						logger.info("The result values of the response json onbject: " + json.get("results"));
						JSONObject json2 = (JSONObject) json.get("results");
						
						JSONArray jarrEdges = (JSONArray) json2.get("edges");
						JSONArray jarrDiamondNodes = (JSONArray) json2.get("diamond_nodes");
						JSONArray jarrSeeds = (JSONArray) json2.get("seeds_in_network");
						
						Set<List<String>> edges = new HashSet<List<String>> ();
						Set<String> diamondNodes = new HashSet<String> ();
						Map<String, Integer> scoreMap = new HashMap <String, Integer>();
						Map<String, Double> pHyperMap = new HashMap <String, Double>();
						
						for (Object e: jarrEdges) {
							List<String> nn = (ArrayList<String>)e;						
							edges.add(nn);
							diamondNodes.add(nn.get(0));
							diamondNodes.add(nn.get(1));					
//							logger.info(e.toString() + " - and the ndoes: " + nn.get(0) + " and " + nn.get(1) );
						}
						
						for (Object diamondNode: jarrDiamondNodes) {
							 JSONObject dnobj = (JSONObject) diamondNode;
							 String dnName = (String) dnobj.get("DIAMOnD_node");
							 String rk = (String) dnobj.get("rank");
							 Integer rank = Integer.parseInt(rk);
							 String ph = (String) dnobj.get("p_hyper");
							 Double phyp = Double.parseDouble(ph);
							 diamondNodes.add(dnName);
							 scoreMap.put(dnName, rank);
							 pHyperMap.put(dnName, phyp);
						}
						
						for (Object seedObj: jarrSeeds) {
							 String seed = (String) seedObj;
							 diamondNodes.add(seed);
							 seeds_in_network.add(seed);
						}
						
						DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
						taskmanager.execute(new TaskIterator(new DiamondCreateNetTask(app, false, diamondNodes, edges, scoreMap, pHyperMap, seeds_in_network, ggType, newNetName)));
						break;
					}
					if (Failed) {
						logger.info("The run is failed!");
						showFailed();
						break;
					}
					response = client.execute(request);
					try {
						logger.info(String.format("Waiting for run to complete, sleeping for %d seconds...", sleep_time));
						Thread.sleep(sleep_time*1000);
					} catch (InterruptedException e0) {
						// TODO Auto-generated catch block
						e0.printStackTrace();
					}
				}
				
				if (!Success & !Failed) {
					logger.info("The run is taking very long (more than 10 mins), please try again in 15 mins!");
					showWarningTime();
					taskMonitor.showMessage(Level.WARN, "The computation is taking very long! It continues running in the backend, to get the results please try again using the same parameters and input for the algorithm in 15 mins!");
//					JOptionPane.showMessageDialog(null, "The run is taking very long (more than 10 mins), please try again in 15 mins!", "Run-time out", 3);
				}
							  
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}			
		}
		
		else if (failedSubmit) {
			logger.info("The request is failed!");
			showFailed();
		}
	}

}
