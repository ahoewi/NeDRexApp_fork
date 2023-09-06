package org.cytoscape.nedrex.internal.algorithms;

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
import org.cytoscape.nedrex.internal.NeDRexService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class DiamondAPI {
	CyNetwork network;
	Set<CyNode> seeds;
	Integer iter;
	Integer alpha;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	Set<String> seeds_in_network= new HashSet<String>();
	Set<List<String>> edges = new HashSet<List<String>>();
	Set<String> diamondNodes = new HashSet<String>();
	Map<String, Integer> scoreMap = new HashMap <String, Integer>();
	Map<String, Double> pHyperMap = new HashMap <String, Double>();
	Boolean Success = false;
	
	public DiamondAPI(NeDRexService nedrexService, CyNetwork network, Set<CyNode> seeds, Integer iter, Integer alpha) throws ParseException, URISyntaxException {
		this.nedrexService = nedrexService;
		this.network = network;
		this.seeds = seeds;
		this.iter = iter;
		this.alpha = alpha;
		
		runAlgorithm();		
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
	
	public void runAlgorithm() throws ParseException, URISyntaxException {
		String submit_url = this.nedrexService.API_LINK + "diamond/submit";
		String status_url = this.nedrexService.API_LINK + "diamond/status";
		
		JSONObject payload = new JSONObject();
		int sleep_time = 3; //in seconds
		List<String> geneNames = new ArrayList<String>();	

		for (CyNode n: seeds) {
			geneNames.add(network.getRow(n).get(CyNetwork.NAME, String.class).replaceFirst("entrez.", ""));

		}
				
		payload.put("seeds", geneNames);
		payload.put("alpha", alpha);
		payload.put("n", iter);
		payload.put("network", "DEFAULT");
		payload.put("edges", "limited");
		
		logger.info("The post JSON converted to string: " + payload.toString());
		
		HttpPost post = new HttpPost(submit_url);
		post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
		String uidd = new String();
		Boolean failedSubmit = false;
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
				logger.info("The uri of the response to the post: "+line + "\n");
				uidd = line;
				if (line.contains("No seeds submitted")) {
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
			String uid = uidd.replace("\"", "");
			HttpGet request = new HttpGet(status_url);
			URI uri = new URIBuilder(request.getURI()).addParameter("uid", uid).build();
			((HttpRequestBase) request).setURI(uri);
			logger.info("The uid: " + uid);		
			
			Success = false;
			try {
				HttpResponse response = nedrexService.send(request);
//				boolean Success = false;
				boolean Failed = false;
				  
				  // we're letting it to run for t*3 seconds
				for (int t=0; t<200; t++) {
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
						
						JSONParser parser = new JSONParser();
						JSONObject json = (JSONObject) parser.parse(responseText);
						logger.info("The result values of the response json onbject: " + json.get("results"));
						JSONObject json2 = (JSONObject) json.get("results");
						
						JSONArray jarrEdges = (JSONArray) json2.get("edges");
						JSONArray jarrDiamondNodes = (JSONArray) json2.get("diamond_nodes");
						JSONArray jarrSeeds = (JSONArray) json2.get("seeds_in_network");
						
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
						
						break;
					}
					if (Failed) {
						logger.info("The run is failed!");
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
							  
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}			
		}
	}
	
	public Set<String> getDiamondNodes(){
		return this.diamondNodes;
	}
	
	public Set<List<String>> getDiamondEdges(){
		return this.edges;
	}
	
	public Set<String> getSeedsInNetwork(){
		return this.seeds_in_network;
	}
	
	public Map<String, Integer> getScore(){
		return this.scoreMap;
	}
	
	public Map<String, Double> getPHyper(){
		return this.pHyperMap;
	}
	
	public Boolean getSuccess() {
		return this.Success;
	}

}
