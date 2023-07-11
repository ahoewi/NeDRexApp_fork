package org.cytoscape.nedrex.internal.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.cytoscape.model.*;
import org.cytoscape.nedrex.internal.*;
import org.cytoscape.nedrex.internal.io.HttpGetWithEntity;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class CreateNetRankedDrugTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Set<String> proteins;
	private Set<String> drugs;
	private List<List<String>> edgesDrP;
	private Map<String, Double> drugScoreMap;
	private List<List<String>> edgesPP;
	private String rankinFunction;
	private String newNetName;
	private Set<String> primary_seeds;
	private Boolean ggType;
	private Boolean quick;
	
	public CreateNetRankedDrugTask(RepoApplication app, Boolean quick, Set<String> proteins, Set<String> drugs, List<List<String>> edgesDrP, 
			Map<String, Double> drugScoreMap, List<List<String>> edgesPP, String rankinFunction, String newNetName, Set<String> primary_seeds, Boolean ggType) {
		this.app = app;
		this.setNedrexService(app.getNedrexService());
		this.quick = quick;
		this.proteins = proteins;
		this.drugs = drugs;
		this.edgesDrP = edgesDrP;
		this.drugScoreMap = drugScoreMap;
		this.edgesPP = edgesPP;
		this.rankinFunction = rankinFunction;
		this.newNetName = newNetName;
		this.primary_seeds = primary_seeds;
		this.ggType = ggType;
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
		
		CyNetworkManager netMgr = app.getActivator().getService(CyNetworkManager.class);
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		CyNetworkFactory cnf = app.getActivator().getService(CyNetworkFactory.class);
		
		String nodeTypeCol = "type";
		String edgeTypeCol = "type";
		String score = "score";
		String rank = "rank";
		String nodeDisplayNameCol = "displayName";
		String drugGroupCol = "drugGroups";
		String seedCol = "primary_seed";
		
		CyNetwork newNet;
		if(!quick) {
			newNet = cnf.createNetwork();
			newNet.getRow(newNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(newNetName));
			newNet.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
			newNet.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
			newNet.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
		}
		else {
			newNet = app.getCurrentNetwork();
		}
//		CyNetwork newNet = cnf.createNetwork();
//		newNet.getRow(newNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(newNetName));
		Map <String, CyNode> mapCyNode = new HashMap<String, CyNode> ();
		Map<String, Double> sortedDrugScoreMap = new LinkedHashMap<String, Double>();
		Map<String, Integer> drugRankMap = new HashMap<String, Integer>();
		List<Entry<String, Double>> rankedDrugs = new ArrayList<Map.Entry<String, Double>>();
		
//		newNet.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
		if (!drugScoreMap.keySet().isEmpty()) {
			newNet.getDefaultNodeTable().createColumn(score, Double.class, false);
			if(newNet.getDefaultNodeTable().getColumn(rank)==null) {
				newNet.getDefaultNodeTable().createColumn(rank, Integer.class, false);
			}
			
			// ranking drugs based on their score. Notice that drugs with the exact same score should get also the same rank
			sortedDrugScoreMap = drugScoreMap.entrySet().stream()
	                .sorted(Entry.comparingByValue())
	                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
			rankedDrugs = new ArrayList<Map.Entry<String, Double>>(sortedDrugScoreMap.entrySet());

			// Standard competiotion ranking 1224
			/*int r = 1;
			int jump = 0;
			int n = rankedDrugs.size();
			drugRankMap.put(rankedDrugs.get(n-1).getKey(), r);
			for (int i = 2; i < n+1; i++) {
				if(Double.compare(rankedDrugs.get(n-i+1).getValue(), rankedDrugs.get(n-i).getValue())!= 0) {
					r +=1;
					r += jump;
					drugRankMap.put(rankedDrugs.get(n-i).getKey(), r);
					jump = 0;
				}
				else if(Double.compare(rankedDrugs.get(n-i+1).getValue(), rankedDrugs.get(n-i).getValue())== 0) {
					drugRankMap.put(rankedDrugs.get(n-i).getKey(), r);
					jump +=1;
				}
				
			}*/
			
			// Dense ranking 1223
			int r = 1;
			int n = rankedDrugs.size();
			drugRankMap.put(rankedDrugs.get(n-1).getKey(), r);
			for (int i = 2; i < n+1; i++) {
				if(Double.compare(rankedDrugs.get(n-i+1).getValue(), rankedDrugs.get(n-i).getValue())!= 0) {
					r +=1;
					drugRankMap.put(rankedDrugs.get(n-i).getKey(), r);
				}
				else if(Double.compare(rankedDrugs.get(n-i+1).getValue(), rankedDrugs.get(n-i).getValue())== 0) {
					drugRankMap.put(rankedDrugs.get(n-i).getKey(), r);
				}
				
			}
		}		
//		newNet.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
		newNet.getDefaultNodeTable().createColumn(drugGroupCol, String.class, false);
		if (!primary_seeds.isEmpty() && !quick) {
			newNet.getDefaultNodeTable().createColumn(seedCol, Boolean.class, false);
		}
		
		
		String drugEntity = "drug";
		String attrDispName = "displayName";
		Map<String, String> drugDispNameMap = new HashMap <String, String>();
		String attrDrugGroups = "drugGroups";
		Map<String, List<String>> drugGroupMap = new HashMap <String, List<String>>();
		String proteinEntity = "protein";
		String geneEntity = "gene";
		Map<String, String> proteinDispNameMap = new HashMap <String, String>();		
		
		// TODO: Replace with the utility function from ApiRoutesUtil
		JSONObject payload = new JSONObject();
		List<String> attributes = new ArrayList<String>();
		attributes.add(attrDrugGroups);
		attributes.add(attrDispName);
		payload.put("attributes", attributes);
		payload.put("node_ids", new ArrayList<String>(drugs));		
		
//		String url = String.format("https://api.repotrial.net/%s/attributes_v2/json", drugEntity);
		String url = String.format(this.nedrexService.API_LINK + "%s/attributes/json", drugEntity);
		HttpGetWithEntity e = new HttpGetWithEntity();
		e.setURI(new URI(url));
		e.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
		
		try {
			HttpResponse response = nedrexService.send(e);
			BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			String  responseText = "";
			while ((line = rd.readLine()) != null) {
				responseText = line;
			}
			
			JSONParser parser = new JSONParser();
			JSONArray jarrDrugs = (JSONArray) parser.parse(responseText);			
			for (Object drug: jarrDrugs) {
				System.out.println(drug);
				 JSONObject drugobj = (JSONObject) drug;
				 String dr = (String) drugobj.get("primaryDomainId");
				 String dispName = (String) drugobj.get(attrDispName);					 
				 drugDispNameMap.put(dr, dispName);
				 JSONArray drugGroups = (JSONArray) drugobj.get(attrDrugGroups);
				 List<String> groups = new ArrayList<String>();
				 for (Object groupObj: drugGroups) {
					 String group = (String) groupObj;
					 groups.add(group);
				}					 
				 drugGroupMap.put(dr, groups);				 
			}
//			logger.info("drugDisplayName map: " + drugDispNameMap);
			logger.info("drugGroups map: " + drugGroupMap);				  		  
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		payload = new JSONObject();
		attributes = new ArrayList<String>();
		attributes.add(attrDispName);
		payload.put("attributes", attributes);
		payload.put("node_ids", new ArrayList<String>(proteins));		
		
		if (!ggType && !quick) {
//			url = String.format("https://api.repotrial.net/%s/attributes_v2/json", proteinEntity);
			url = String.format(this.nedrexService.API_LINK + "%s/attributes/json", proteinEntity);
		}
		else if (ggType && !quick) {
//			url = String.format("https://api.repotrial.net/%s/attributes_v2/json", geneEntity);
			url = String.format(this.nedrexService.API_LINK + "%s/attributes/json", geneEntity);
		}
		
		e = new HttpGetWithEntity();
		e.setURI(new URI(url));
		e.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
		
		try {
			HttpResponse response = nedrexService.send(e);
			BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			String  responseText = "";
			while ((line = rd.readLine()) != null) {
				responseText = line;
			}
			
			JSONParser parser = new JSONParser();
			JSONArray jarrProts = (JSONArray) parser.parse(responseText);			
			for (Object prot: jarrProts) {
				 JSONObject proteinobj = (JSONObject) prot;
				 String pr = (String) proteinobj.get("primaryDomainId");
				 String dispName = (String) proteinobj.get(attrDispName);					 
				 if (proteins.contains(pr) ) {
					 proteinDispNameMap.put(pr, dispName);
				 }
			}
//			logger.info("proteinsDisplayName map: " + proteinDispNameMap);			  		  
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if (!quick) {
			for (String p: proteins) {	// could be also genes
				CyNode cynode = newNet.addNode();
				newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", p);
				newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeDisplayNameCol, proteinDispNameMap.get(p));
				if (primary_seeds.contains(p)) {
					newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(seedCol, true);
				}
				if(!ggType) {
					newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Protein.toString());
				}
				else if (ggType) {
					newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Gene.toString());
				}
				mapCyNode.put(p, cynode);
			}
		}
		else {
			for (String p: proteins) {	// could be also genes
				CyNode cynode = ModelUtil.getNodeWithName(app, newNet, p);
				mapCyNode.put(p, cynode);
			}
		}

		System.out.println(drugGroupMap);
		for (String d: drugs) {
			System.out.println(d);
			System.out.println(drugGroupMap.containsKey(d));
			CyNode cynode = newNet.addNode();
			newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", d);
			newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeDisplayNameCol, drugDispNameMap.get(d));
			newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(drugGroupCol, drugGroupMap.containsKey(d) ? String.join(", ", drugGroupMap.get(d)): "");
			newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Drug.toString());			
//			newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(score, drugScoreMap.get(d));
			if (!drugScoreMap.keySet().isEmpty()) {
				newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(score, drugScoreMap.get(d));
				newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(rank, drugRankMap.get(d));
			}
//			newNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(score, drugScoreMap.getOrDefault(d, 1D));			
			mapCyNode.put(d, cynode);
		}
		
//		newNet.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
		Set<List<String>> edgesToAdd = new HashSet<List<String>>(edgesDrP);
		for (List<String> edge: edgesToAdd) {
			CyEdge cyedge = newNet.addEdge(mapCyNode.get(edge.get(0)), mapCyNode.get(edge.get(1)), false);
			newNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", edge.get(0) + " (-) " + edge.get(1));
			if (!ggType) {
				newNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.drug_protein.toString());
			}
			else if (ggType) {
				newNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.drug_gene.toString());
			}				
		}
		
		if(!quick) {
			for (List<String> edge: edgesPP) {
				CyEdge cyedge = newNet.addEdge(mapCyNode.get(edge.get(0)), mapCyNode.get(edge.get(1)), false);
				newNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", edge.get(0) + " (-) " + edge.get(1));
				if (!ggType) {
					newNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.protein_protein.toString());	
				}
				else if (ggType) {
					newNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.gene_gene.toString());	
				}			
			}
			
			netMgr.addNetwork(newNet);
		}
		
			
		Map<String, Object> args = new HashMap<>();
		args.put("network", "current");
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("view", "create", args, null);

		String styleName = "NeDRex";		

		Map<String, Object> argsAS = new HashMap<>();
		argsAS.put("styles", styleName);
		CommandExecuter cmdexAS = new CommandExecuter(app);
		cmdexAS.executeCommand("vizmap", "apply", argsAS, null);
		
		if (!primary_seeds.isEmpty() && !quick) {
			List<CyNode> seeds_toSelect = CyTableUtil.getNodesInState(newNet,seedCol,true);
			for (CyNode n: seeds_toSelect) {
				newNet.getRow(n).set("selected", true);
			}
		}
		
		if (quick) {
			newNet.getDefaultNodeTable().deleteColumn("seeds");
			// Update the layout as following:
			CyLayoutAlgorithmManager layAlgMan = app.getCyLayoutAlgorithmManager();
			CyLayoutAlgorithm layAlg = layAlgMan.getLayout("force-directed");
			TaskIterator itr = layAlg.createTaskIterator(app.getCurrentNetworkView(),layAlg.createLayoutContext(),CyLayoutAlgorithm.ALL_NODE_VIEWS,null);
			app.getTaskManager().execute(itr);
		}
		
	}

}
