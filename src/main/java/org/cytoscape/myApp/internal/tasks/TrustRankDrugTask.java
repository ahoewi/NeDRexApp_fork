package org.cytoscape.myApp.internal.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.util.Set;

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
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.NodeType;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.utils.ApiRoutesUtil;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.BoundedDouble;
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
public class TrustRankDrugTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Set Parameters for TrustRank Algorithm";}
	
	@Tunable(description="Include only direct drugs", groups="TrustRank algorithm settings",
			tooltip = "Specifies, whether only drugs targeting seeds should be considered or all drugs",
			gravity = 1.0)
    public Boolean only_direct_drugs = true;
	
	@Tunable(description="Include only approved drugs", groups="TrustRank algorithm settings",
			tooltip = "Specifies, whether only approved drugs targeting seeds should be considered or all drugs",
			gravity = 1.0)
    public Boolean only_approved_drugs = true;
	
	@Tunable(description="Damping factor", groups="TrustRank algorithm settings",
			params="slider=true",
			tooltip="The larger the damping factor, the faster the trust is propagated through the network. Default:0.85",
			gravity = 2.0)
    public BoundedDouble damping_factor = new BoundedDouble(0.0, 0.85, 1.0, false, false);
	
	@Tunable(description="The number of top-ranked drugs", groups="Result size",
			params="slider=true",
			tooltip="Specifies the number of top-ranked drugs to be returned in the network result.",
			gravity = 2.0)
    public BoundedInteger result_size = new BoundedInteger(5, 50, 500, false, false);
	
	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name based on the selected algorithm parameters will be assigned",
			gravity = 5.0)
    public Boolean set_net_name = false;
	
	@Tunable(description="Name of the result network", 
	         groups="Result network", 
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 5.0)
	public String new_net_name = new String();
	
	
	public TrustRankDrugTask(RepoApplication app) {
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
			String netName = String.format("TrustRank_DF%.2f_RS%d", damping_factor.getValue(), result_size.getValue());
			if (only_approved_drugs) {
				netName = netName+"_onlyApproved";
			}
			if (only_direct_drugs) {
				netName = netName+"_onlyDirect";
			}
			newNetName = namingUtil.getSuggestedNetworkTitle(netName);
		}
		else if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
		String submit_url = Constant.API_LINK + "trustrank/submit";
		String status_url = Constant.API_LINK + "trustrank/status";
		
		String seedCol = "seed";
		
		JSONObject payload = new JSONObject();
		List<String> seeds = new ArrayList<String>();
		Double dampingFactor = damping_factor.getValue();
		Boolean onlyDirectDrugs = only_direct_drugs;
		Boolean onlyApprovedDrugs = only_approved_drugs;
		Integer resultSize = result_size.getValue();
		Boolean ggType = false;
		Map<CyNode, Set<String>> geneProteinsMap = new HashMap<CyNode, Set<String>>();
		Map<String, Set<String>> proteinGenesMap = new HashMap<String, Set<String>>();
		Set<String> primary_seeds = new HashSet<String>();
		int sleep_time = 3; //in seconds
		
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
		List<String> selectedNodeNames = new ArrayList<String>();
		if (network.getRow(selectedNodes.get(0)).get("type", String.class).equals(NodeType.Gene.toString())) {
			ggType = true;
		}
		
		if (!ggType) {
			for (CyNode n: selectedNodes) {
				String nodeName = network.getRow(n).get(CyNetwork.NAME, String.class);
				selectedNodeNames.add(nodeName);				
				if (network.getRow(n).isSet(seedCol) && network.getRow(n).get(seedCol, Boolean.class)) {
					primary_seeds.add(nodeName);
				}				
			}
		}
		else if (ggType) {
			// find all proteins encoded by the selected genes first
			Set<CyNode> selectedGenes = new HashSet<CyNode>(selectedNodes);
			geneProteinsMap = ApiRoutesUtil.getEncodedProteins(network, selectedGenes);
			logger.info("This is the gene protein map obtained via API: "+ geneProteinsMap);
			System.out.println("This is the gene protein map obtained via API: "+ geneProteinsMap);
			for (Entry<CyNode, Set<String>> entry: geneProteinsMap.entrySet()) {
				selectedNodeNames.addAll(entry.getValue());
				for (String p:entry.getValue()) {
					if (!proteinGenesMap.keySet().contains(p)) {
						proteinGenesMap.put(p, new HashSet<String>());
					}
					proteinGenesMap.get(p).add(network.getRow(entry.getKey()).get(CyNetwork.NAME, String.class));
				}
			}
			for (CyNode g: selectedGenes) {
				String nodeName = network.getRow(g).get(CyNetwork.NAME, String.class);
				if (network.getRow(g).isSet(seedCol) && network.getRow(g).get(seedCol, Boolean.class)) {
					primary_seeds.add(nodeName);
				}
			}
		} 
				
		// get the PP or GG edges between selected seeds to pass to the CreateNetRankedDrugTask
		Set<CyEdge> edgesBetwSeeds = new HashSet<CyEdge>();
		for (CyNode s1: selectedNodes) {
			for (CyNode s2: selectedNodes) {
				if (!s1.equals(s2)) {
					edgesBetwSeeds.addAll(network.getConnectingEdgeList(s1, s2, CyEdge.Type.ANY));
					edgesBetwSeeds.addAll(network.getConnectingEdgeList(s2, s1, CyEdge.Type.ANY));
				}				
			}
		}
		
		List<List<String>> edgesPP = new ArrayList<List<String>> (); // it can be also GG edges
		Set<String> proteins = new HashSet<String> ();		
		for (CyEdge e: edgesBetwSeeds) {
			List<String> nn = new ArrayList<String>();
			nn.add(network.getRow(e.getSource()).get(CyNetwork.NAME, String.class));
			nn.add(network.getRow(e.getTarget()).get(CyNetwork.NAME, String.class));			
			edgesPP.add(nn);
			proteins.add(nn.get(0));
			proteins.add(nn.get(1));
//			logger.info("The PP edges consists of ndoes: " + nn.get(0) + " and " + nn.get(1) );
		}
		
		seeds = selectedNodeNames;
		
//		logger.info("The list of selected nodes or seeds: " + seeds);
		
		payload.put("seeds", seeds);
		payload.put("damping_factor", dampingFactor);
		payload.put("only_direct_drugs", onlyDirectDrugs);
		payload.put("only_approved_drugs", onlyApprovedDrugs);
		payload.put("N", resultSize);
		
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
//			logger.info("Response entity: ");
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("The status code of the response: " + statusCode);
			if (statusCode != 200) {
				failedSubmit=true;
			}
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				logger.info("The uri of the response to the post: "+line + "\n");
				uidd = line;
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
			logger.info("The uid: " + uid);		
			
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
//						System.out.println(line);
//						logger.info("The response entity of the status: " +line);
						if (line.contains("completed"))
							Success = true;
							responseText = line;
						if (line.contains("failed")) {
							Failed = true;
						}
						if (responseText.length() < 3) {
							Failed = true;
						}
					}
					if(Success) {
						logger.info("The run is successfully completed! This is the response: " + response.getParams());
//						logger.info("The status line of the response:" + response.getStatusLine());						
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(responseText);
//						logger.info("The result values of the response json onbject: " + json.get("results"));
						JSONObject json2 = (JSONObject) json.get("results");
						JSONArray jarrEdges = (JSONArray) json2.get("edges");
						JSONArray jarrDrugs = (JSONArray) json2.get("drugs");
						List<List<String>> edgesDrP = new ArrayList<List<String>> ();
						Set<String> drugs = new HashSet<String>();
						Map<String, Double> drugScoreMap = new HashMap <String, Double>();
						
						for (Object e: jarrEdges) {
							List<String> nn = (ArrayList<String>) e;						
							if (!ggType) {
								edgesDrP.add(nn);
								drugs.add(nn.get(0));
								////// instead of p here I should get the genes for them when ggType
								proteins.add(nn.get(1));
							}						
							else if (ggType) {
							////// instead of p here I should get the genes for them when ggType
								Set<String> gs= proteinGenesMap.get(nn.get(1));
								for (String g: gs) {
									nn.remove(1);
									nn.add(g);
									edgesDrP.add(nn);
									proteins.add(g);	// it's actually genes not proteins
								}							
								drugs.add(nn.get(0));							
							}
						}
						
						for (Object drug: jarrDrugs) {
							 JSONObject drugobj = (JSONObject) drug;
							 String dr = (String) drugobj.get("drug_name");
							 String sc = (String) drugobj.get("score");
							 Double score = Double.parseDouble(sc);						 
							 if (score > 0.0 ) {
								 drugScoreMap.put(dr, score);
								 drugs.add(dr);
							 }
							 
						}
						
						DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
//						taskmanager.execute(new TaskIterator(new TrustRankCreateNetTask(app, proteins, drugs, edgesDrP, drugScoreMap, edgesPP)));
						String rankinFunction = "TrustRank";
						logger.info("This is the list of seeds at the end of trustrank task: " + seeds);
						taskmanager.execute(new TaskIterator(new CreateNetRankedDrugTask(app, false, proteins, drugs, edgesDrP, drugScoreMap, edgesPP, rankinFunction, newNetName, primary_seeds, ggType)));
						
						
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
