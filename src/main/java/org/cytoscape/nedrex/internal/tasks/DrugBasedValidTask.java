package org.cytoscape.nedrex.internal.tasks;

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
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.RepoResultPanel;
import org.cytoscape.nedrex.internal.utils.FilterType;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedInteger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @modified by: Andreas Maier
 */
public class DrugBasedValidTask extends AbstractTask{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private RepoResultPanel resultPanel;
	private String pvalue;
	private String pvalue_DCG;
	
	@ProvidesTitle
	public String getTitle() {return "Set Parameters for Drug-centric Validation Algorithm";}
	
	@Tunable(description="Number of permutations", groups="Algorithm settings",
			params="slider=true",
			tooltip="The number of random lists of drugs to build background distribution",
			gravity = 2.0)
    public BoundedInteger permutations = new BoundedInteger(1000, 1000, 10000, false, false);
	
	@Tunable(description="Include only approved drugs", groups="Algorithm settings",
			tooltip="If selected, only approved (registered) drugs should be considered for validation",
			gravity = 2.5)
    public Boolean only_approved = false;
	
	@Tunable(description="Read drugs indicated for disease from a file", groups="Reference drugs",
			tooltip="If selected, drugs indicated for treatment of disease will be read from a file. Otherwise, selected drugs in the current network will be taken as indicated drugs.",
			gravity = 3.0)
    public Boolean trueDrugFile = false;
	
	@Tunable(description="Input file for drugs indicated for disease:" ,  params= "input=true", groups="Reference drugs",
			dependsOn="trueDrugFile=true",
			tooltip="Input file containing list of drugs indicated for treatment of disease, one drug per line. The file should be a tab-separated file and the first column will be taken as drugs. Drugs with DrugBank IDs are acceptable as input.",
			gravity = 3.5)
	public File inputTDFile = new File(System.getProperty("user.home"));
	
	@Tunable(description="Read drugs to be validated from a file", groups="Drugs for validation",
			tooltip="If selected, a list of drugs to-be-validated will be read from a file. Otherwise, all drugs in the current network with their ranks from node table will be taken as drugs to-be-validated.",
			gravity = 3.0)
    public Boolean resultDrugFile = false;
	
	@Tunable(description="Input file for drugs to be validated:" ,  params= "input=true", groups="Drugs for validation",
			dependsOn="resultDrugFile=true",
			tooltip="Input file containing list of drugs with their ranks, one drug per line. The file should be a tab-separated file, the first column will be taken as drugs and \"\n" + 
					"+ \"second column as ranks. Drugs with DrugBank IDs are acceptable as input.",
			gravity = 3.5)
	public File inputRDFile = new File(System.getProperty("user.home"));
	
	@Tunable(description="Description of the validation run", groups="Validation result", 
	         tooltip="Write a description of the validation job you are running to be shown in the result panel. For example, name of the disease. This helps tracking your analyses",
	         gravity = 5.0)
	public String job_description = new String();
	
	public DrugBasedValidTask(RepoApplication app, RepoResultPanel resultPanel) {
		this.app = app;
		this.resultPanel = resultPanel;
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
					JOptionPane.showMessageDialog(null, "The request to run the validation is failed! Please check your inputs.", "Error", JOptionPane.ERROR_MESSAGE);
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
		String submit_url = this.nedrexService.API_LINK + "validation/drug_based";
		String status_url = this.nedrexService.API_LINK + "validation/status";
		
		JSONObject payload = new JSONObject();
		List<String> true_drugs = new ArrayList<String>();		
		List<List<String>> result_drugs = new ArrayList<List<String>>();	
		int sleep_time = 2; //in seconds
		
		if (!trueDrugFile) {
			List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
			for (CyNode n: selectedNodes) {
				if (network.getRow(n).get("type", String.class).equals(NodeType.Drug.toString())) {
					true_drugs.add(network.getRow(n).get(CyNetwork.NAME, String.class));
				}
			}
			/*Set<CyNode> true_drugs_nodes = FilterType.nodesOfType(network, NodeType.Drug);
			for (CyNode n: true_drugs_nodes) {
				true_drugs.add(network.getRow(n).get(CyNetwork.NAME, String.class));
			}*/					
		}
		else if (trueDrugFile) {
			String fp = inputTDFile.getPath();
			try {
				BufferedReader br = new BufferedReader(new FileReader(fp));
				String dataRow = br.readLine();//skip header line
				while (dataRow != null){
					String[] data = dataRow.split("\t");
					if(!true_drugs.contains(data[0])) {
						true_drugs.add(data[0]);
					}						
					dataRow = br.readLine();
				}
			}			
			catch (IOException e) {e.printStackTrace();}
		}
		logger.info("The list of true drugs: " + true_drugs);
		logger.info("Length of true drugs list: " + true_drugs.size());
		
		if (!resultDrugFile) {
			Set<CyNode> result_drugs_nodes = FilterType.nodesOfType(network, NodeType.Drug);
			for (CyNode n: result_drugs_nodes) {
				List<String> e = new ArrayList<String>(2);
				e.add(network.getRow(n).get(CyNetwork.NAME, String.class));
				e.add(network.getRow(n).get("rank", Integer.class).toString());
				result_drugs.add(e);
			}					
		}
		else if (resultDrugFile) {
			String fp = inputRDFile.getPath();
			try {
				BufferedReader br = new BufferedReader(new FileReader(fp));
				String dataRow = br.readLine();//skip header line
				while (dataRow != null){
					String[] data = dataRow.split("\t");
					List<String> e = new ArrayList<String>(2);
					e.add(data[0]);
					e.add(data[1]);
					result_drugs.add(e);						
					dataRow = br.readLine();
				}
			}			
			catch (IOException e) {e.printStackTrace();}
		}
		logger.info("The list of result drugs: " + result_drugs);
		logger.info("Length of result drugs list: " + result_drugs.size());
		
		payload.put("test_drugs", result_drugs);
		payload.put("true_drugs", true_drugs);
		payload.put("permutations", permutations.getValue());
		payload.put("only_approved_drugs", only_approved);
		
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
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				logger.info("The uri of the response to the post: "+line + "\n");
				uidd = line;
				if (line.contains("Internal Server Error")) {
					failedSubmit=true;
				}
//				if (line.length() < 3) {
//					failedSubmit = true;
//				}			
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
				  
				  // we're letting it to run for t*2 seconds
				double n = 150;
				for (int t=0; t<150; t++) {
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
						logger.info("The result values of the response json onbject for empirical DCG-based p-value: " + json.get("empirical DCG-based p-value"));
						
						pvalue_DCG = String.valueOf(json.get("empirical DCG-based p-value"));
						pvalue = String.valueOf(json.get("empirical p-value without considering ranks"));
						
						
						/*JSONArray jarrEdges = (JSONArray) json2.get("edges");
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
						}*/
						
//						DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
//						taskmanager.execute(new TaskIterator(new DiamondCreateNetTask(app, false, diamondNodes, edges, scoreMap, pHyperMap, seeds_in_network, ggType, newNetName)));

//						resultPanel.activateFromDrugValidation(this);
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
	
	public String getPVal() {
		return pvalue;
	}
	
	public String getPValDCG() {
		return pvalue_DCG;
	}
	
	public Integer getPermutations() {
		return permutations.getValue();
	}
	
	public String getApproved() {
		String approved;
		if (only_approved) {
			approved = "only approved";
		}
		else {
			approved = "all";
		}		
		return approved;
	}
	
	public String getDescription() {
		return job_description;
	}

}
