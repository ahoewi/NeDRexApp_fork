package org.cytoscape.nedrex.internal.tasks;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.*;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * NeDRex App
 * @author Andreas Maier
 */
public class RobustTask extends AbstractTask{

	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@ProvidesTitle
	public String getTitle() {return "Set Parameters for ROBUST Algorithm";}

	@Tunable(description="Initial Fraction", groups="Algorithm settings",
			params="slider=true",
			tooltip="Adjusts the allowed variation in Steiner Trees (0.0 generates the same Steiner Tree every time).",
			gravity = 2.0)
    public BoundedFloat init_fract = new BoundedFloat(0.0f, 0.25f, 1.0f, false, false);

	@Tunable(description="Reduction Factor", groups="Algorithm settings",
			params="slider=true",
			tooltip="Adjusts the openness to add non-seed genes to the module (0.0 is restrictive, 1.0 inclusive).",
			gravity = 2.5)
	public BoundedFloat reduct_fact = new BoundedFloat(0.0f, 0.9f, 1.0f, false, false);

	@Tunable(description="Threshold", groups="Algorithm settings",
			params="slider=true",
			tooltip="Adjusts the tradeoff between explorativeness and robustness (0.0 is explorative, 1.0 robust).",
			gravity = 3.0)
	public BoundedFloat threshold = new BoundedFloat(0.0f, 0.1f, 1.0f, false, false);


	@Tunable(description="Trees", groups="Algorithm settings",
			params="slider=true",
			tooltip="The number of individual Steiner Trees generated",
			gravity = 3.5)
	public BoundedInteger trees = new BoundedInteger(2, 30, 100, false, false);

	@Tunable(description="Read seeds from a file", groups="Input seeds",
			tooltip="If selected, gene seeds will be read from a file. Otherwise, selected nodes in the current network will be taken as seeds.",
			gravity = 4.0)
	public Boolean seedFile = false;


	@Tunable(description="Input file for seeds:" ,  params= "input=true",
			groups="Input seeds",
			dependsOn="seedFile=true",
			tooltip="Input file contains list of seeds, one seed per line. The file should be a tab-separated file and the first column will be taken as seeds. Genes with Entrez IDs are acceptable as input.",
			gravity = 4.5)
	public File inputFile = new File(System.getProperty("user.home"));

	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name based on the selected algorithm parameters will be assigned.",
			gravity = 6.0)
    public Boolean set_net_name = false;

	@Tunable(description="Name of the result network",
	         groups="Result network",
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 6.0)
	public String new_net_name = new String();

	public RobustTask(RepoApplication app) {
		this.app = app;
		this.setNedrexService(app.getNedrexService());
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

	private NeDRexService nedrexService;
	@Reference
	public void setNedrexService(NeDRexService nedrexService) {
		this.nedrexService = nedrexService;
	}

	public void unsetNedrexService(NeDRexService nedrexService) {
		if (this.nedrexService == nedrexService)
			this.nedrexService = null;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		String newNetName = new String();
		if (!set_net_name) {
			String netName = String.format("ROBUST_init%.3f_reduc%.3f_thr%.3f_trees%d",
					init_fract.getValue(),
					reduct_fact.getValue(),
					threshold.getValue(),
					trees.getValue());
			newNetName = namingUtil.getSuggestedNetworkTitle(netName);
		}
		else {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
		String submit_url = this.nedrexService.API_LINK + "robust/submit";
		String status_url = this.nedrexService.API_LINK + "robust/status";
		String results_url = this.nedrexService.API_LINK + "robust/results";
		
		JSONObject payload = new JSONObject();
		List<String> seeds = new ArrayList<>();
		List<String> selectedNodeNames = new ArrayList<>();
		Boolean ggType = false;		
		Set<String> seeds_in_network = new HashSet<>();
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
		payload.put("initial_fraction", init_fract.getValue());
		payload.put("reduction_factor", reduct_fact.getValue());
		payload.put("num_trees", trees.getValue());
		payload.put("threshold", threshold.getValue());
		payload.put("network", "DEFAULT");

		logger.info("The post JSON converted to string: " + payload);
		
		HttpPost post = new HttpPost(submit_url);
		post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
		System.out.println(payload.toString());
		String uidd = new String();
		Boolean failedSubmit = false;
		System.out.println(status_url);
		try {
			HttpResponse response = nedrexService.send(post);
			HttpEntity entity = response.getEntity();
			BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = "";
			logger.info("Response entity: ");
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("The status code of the response: " + statusCode);
			if (statusCode != 200) {
				failedSubmit=true;
			}
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				logger.info("The URI of the response to the post: "+line + "\n");
				uidd = line;
				if (line.contains("No seed genes submitted")) {
					failedSubmit=true;
				}
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
				HttpResponse response = nedrexService.send(request);
				boolean Failed = false;
				  
				double n = 200;
				for (int t=0; t<200; t++) {
					taskMonitor.setProgress(0.1+ t* (1.0-0.1)/n);
					StringBuilder responseText = new StringBuilder();
					BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
						System.out.println(line);
//						logger.info("The response entity of the status: " +line);
						if (line.contains("completed"))
							Success=true;
							responseText.append(line);
						if (line.contains("failed")) {
							Failed=true;
						}
					}
					if(Success) {
						HttpGet request_result = new HttpGet(results_url);
						((HttpRequestBase) request_result).setURI(new URIBuilder(request_result.getURI()).addParameter("uid", uid).build());
						HttpResponse result_response = nedrexService.send(request_result);
						logger.info("The run is successfully completed! This is the response: " + result_response.getParams());
						System.out.println(results_url);

						BufferedReader br = new BufferedReader (new InputStreamReader(result_response.getEntity().getContent()));
						String l = "";
						StringBuilder resultText = new StringBuilder();
						while ((l = br.readLine()) != null) {
							resultText.append(l);
						}

						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(resultText.toString());
						System.out.println(json);

						JSONArray jarrEdges = (JSONArray) json.get("links");
						JSONArray jarrRobustNodes = (JSONArray) json.get("nodes");

						Set<List<String>> edges = new HashSet<List<String>> ();
						Set<String> robustNodes = new HashSet<String> ();
						Map<String, Integer> occurenceMap = new HashMap <String, Integer>();
						Map<String, Double> significanceMap = new HashMap <String, Double>();
						
						for (Object e: jarrEdges) {
							JSONObject nn = (JSONObject)e;
							String[] edge = new String[2];
							edge[0] = (String) nn.get("source");
							edge[1] = (String) nn.get("target");
							edges.add(Arrays.asList(edge));
							robustNodes.add(edge[0]);
							robustNodes.add(edge[1]);
						}
						
						for (Object robustNode: jarrRobustNodes) {
							JSONObject dnobj = (JSONObject) robustNode;
							String node_id = (String)dnobj.get("id");
							robustNodes.add(node_id);
							if((boolean)dnobj.get("isSeed")){
								 seeds_in_network.add(node_id);
							 }else{
								 double significance = (double)dnobj.get("significance");
								 int nrOfOccurrences = Math.toIntExact((long)dnobj.get("nrOfOccurrences"));
								 significanceMap.put(node_id, significance);
								 occurenceMap.put(node_id, nrOfOccurrences);
							 }

						}
						

						DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
						taskmanager.execute(new TaskIterator(new RobustCreateNetTask(app, false, robustNodes, edges, occurenceMap, significanceMap, seeds_in_network, ggType, newNetName)));
						break;
					}
					if (Failed) {
						logger.info("The run is failed!");
						showFailed();
						break;
					}
					response = nedrexService.send(request);
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
