package org.cytoscape.myApp.internal.algorithms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.utils.ApiRoutesUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ClosenessAPI {
	CyNetwork network;
//	Set<CyNode> seeds;
	Integer result_size;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	Set<String> genes = new HashSet<String> ();
	List<List<String>> edgesGG = new ArrayList<List<String>> ();
	List<List<String>> edgesDrP = new ArrayList<List<String>> ();
	Set<String> drugs = new HashSet<String>();
	Map<String, Double> drugScoreMap = new HashMap <String, Double>();
	Set<String> primary_seeds = new HashSet<String>();
	Boolean Success;
	
	public ClosenessAPI(CyNetwork network, Integer result_size) throws URISyntaxException, ParseException {
		this.network = network;
		this.result_size = result_size;
		
		runAlgorithm();
	}
	
	public void runAlgorithm() throws URISyntaxException, ParseException {	
		String submit_url = Constant.API_LINK + "closeness/submit";
		String status_url = Constant.API_LINK + "closeness/status";
		
		JSONObject payload = new JSONObject();
		int sleep_time = 3; //in seconds
		String seedCol = "seed";
		
		Map<CyNode, Set<String>> geneProteinsMap = new HashMap<CyNode, Set<String>>();
		Map<String, Set<String>> proteinGenesMap = new HashMap<String, Set<String>>();
			
		List<CyNode> allNodes = network.getNodeList();		
		List<String> allNodeNames = new ArrayList<String>();

		// find all proteins encoded by all the genes first
		Set<CyNode> allGenes = new HashSet<CyNode>(allNodes);
		geneProteinsMap = ApiRoutesUtil.getEncodedProteins(network, allGenes);
		logger.info("This is the gene protein map obtained via API: "+ geneProteinsMap);
		System.out.println("This is the gene protein map obtained via API: "+ geneProteinsMap);
		for (Entry<CyNode, Set<String>> entry: geneProteinsMap.entrySet()) {
			allNodeNames.addAll(entry.getValue());
			for (String p:entry.getValue()) {
				if (!proteinGenesMap.keySet().contains(p)) {
					proteinGenesMap.put(p, new HashSet<String>());
				}
				proteinGenesMap.get(p).add(network.getRow(entry.getKey()).get(CyNetwork.NAME, String.class));
			}
		}
		for (CyNode g: allGenes) {
			String nodeName = network.getRow(g).get(CyNetwork.NAME, String.class);
			if (network.getRow(g).isSet(seedCol) && network.getRow(g).get(seedCol, Boolean.class)) {
				primary_seeds.add(nodeName);
			}
		}		
		
		payload.put("seeds", allNodeNames);
		payload.put("only_direct_drugs", true);
		payload.put("only_approved_drugs", true);
		payload.put("N", result_size);
		
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
		if(!failedSubmit) {
			String uid = uidd.replace("\"", "");
			HttpGet request = new HttpGet(status_url);
			URI uri = new URIBuilder(request.getURI()).addParameter("uid", uid).build();
			((HttpRequestBase) request).setURI(uri);
			logger.info("The uid: " + uid);					
			Success = false;
			try {
				HttpResponse response = client.execute(request);
				boolean Failed = false;			  
				  // we're letting it build for t*3 seconds
				for (int t=0; t<200; t++) {
					String  responseText = "";
					BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
//						System.out.println(line);
//						logger.info("The response entity of the status: " +line);
						if (line.contains("completed"))
							Success=true;
							responseText = line;
						if (line.contains("failed")) {
							Failed=true;
						}
						if (responseText.length() < 3) {
							Failed = true;
						}
					}
					if(Success) {
						logger.info("The run is successfully completed! This is the response: " + response.getParams());						
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(responseText);
						JSONObject json2 = (JSONObject) json.get("results");
						JSONArray jarrEdges = (JSONArray) json2.get("edges");
						JSONArray jarrDrugs = (JSONArray) json2.get("drugs");
						
						for (Object e: jarrEdges) {
							List<String> nn = (ArrayList<String>) e;						
							Set<String> gs= proteinGenesMap.get(nn.get(1));
							for (String g: gs) {
								nn.remove(1);
								nn.add(g);
								edgesDrP.add(nn);
								genes.add(g);
							}							
							drugs.add(nn.get(0));									
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
//						DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
//						String rankinFunction = "Closeness";
//						taskmanager.execute(new TaskIterator(new CreateNetRankedDrugTask(app, genes, drugs, edgesDrP, drugScoreMap, edgesGG, rankinFunction, newNetName, primary_seeds, ggType)));						
						break;
					}
					if (Failed) {
						logger.info("The run is failed!");
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
							  
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}	
	}
	
	public Set<String> getGenes(){
		return this.genes;
	}
	
	public Set<String> getDrugs(){
		return this.drugs;
	}
	
	public List<List<String>> getGGEdges(){
		return this.edgesGG;
	}
	
	public List<List<String>> getDrPEdges(){
		return this.edgesDrP;
	}
	
	public Set<String> getPrimarySeeds(){
		return this.primary_seeds;
	}
	
	public Map<String, Double> getDrugScore(){
		return this.drugScoreMap;
	}

	public Boolean getSuccess() {
		return this.Success;
	}

}
