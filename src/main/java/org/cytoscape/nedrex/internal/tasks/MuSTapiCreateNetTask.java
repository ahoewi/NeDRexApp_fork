package org.cytoscape.nedrex.internal.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cytoscape.model.*;
import org.cytoscape.nedrex.internal.*;
import org.cytoscape.nedrex.internal.algorithms.ClosenessAPI;
import org.cytoscape.nedrex.internal.io.HttpGetWithEntity;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
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
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @modified by: Andreas Maier
 */
public class MuSTapiCreateNetTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Set<String> mustNodes;
	private Set<List<String>> edges;
	private Map<String, Integer> nodeParticipationMap;
	private Map<List<String>, Integer> edgeParticipationMap;
	private Set<String> seeds_in_network;
	private Boolean ggType;
	private String newNetName;
	private Boolean quick;
	
	public MuSTapiCreateNetTask(RepoApplication app, Boolean quick, Set<String> mustNodes, Set<List<String>> edges, Map<String, Integer> nodeParticipationMap,
			Map<List<String>, Integer> edgeParticipationMap, Set<String> seeds_in_network, Boolean ggType, String newNetName) {
		this.app = app;
		this.quick = quick;
		this.mustNodes = mustNodes;
		this.edges = edges;
		this.nodeParticipationMap = nodeParticipationMap;
		this.edgeParticipationMap = edgeParticipationMap;
		this.seeds_in_network = seeds_in_network;
		this.ggType = ggType;
		this.newNetName = newNetName;
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
		String particNumb = "participation_number";
		String nodeDisplayNameCol = "displayName";
		String seedCol = "seed";
		List<String> genes = new ArrayList<String>();	// could be protein
		String geneEntity = "gene";
		String proteinEntity = "protein";

		CyNetwork mustNet = cnf.createNetwork();
		mustNet.getRow(mustNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(newNetName));
		Map <String, CyNode> mapCyNode = new HashMap<String, CyNode> ();
		
		mustNet.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
		mustNet.getDefaultNodeTable().createColumn(particNumb, Integer.class, false);
		mustNet.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
		mustNet.getDefaultNodeTable().createColumn(seedCol, Boolean.class, false);
		
		String attrDispName = "displayName";
		JSONObject payload = new JSONObject();
		List<String> attributes = new ArrayList<String>();
		Map<String, String> geneDispNameMap = new HashMap <String, String>();
		
		if (ggType) {
			for (String node: mustNodes) {
				genes.add("entrez."+node);
			}
		}
		else if (!ggType) {
			for (String node: mustNodes) {
				genes.add("uniprot."+node);
			}
		}
		
		payload = new JSONObject();
		attributes = new ArrayList<String>();
		attributes.add(attrDispName);
		payload.put("attributes", attributes);
		payload.put("node_ids", new ArrayList<String>(genes));

		String url = new String();
		if (ggType) {
			url = String.format(this.nedrexService.API_LINK + "%s/attributes/json", geneEntity);
		}
		else if (!ggType) {
			url = String.format(this.nedrexService.API_LINK + "%s/attributes/json", proteinEntity);
		}

		HttpClient httpClient = new DefaultHttpClient();
		HttpGetWithEntity e = new HttpGetWithEntity();
		e = new HttpGetWithEntity();
		e.setURI(new URI(url));
		e.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
		
		try {
			HttpResponse response = httpClient.execute(e);
			BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			String  responseText = "";
			while ((line = rd.readLine()) != null) {
				responseText = line;
			}
			
			JSONParser parser = new JSONParser();
			JSONArray jarrGenes = (JSONArray) parser.parse(responseText);
			for (Object gene: jarrGenes) {
				 JSONObject geneobj = (JSONObject) gene;
				 String g = (String) geneobj.get("primaryDomainId");
				 String dispName = (String) geneobj.get(attrDispName);					 
				 if (genes.contains(g) ) {
					 geneDispNameMap.put(g, dispName);
				 }
			}
			logger.info("geneDisplayName map: " + geneDispNameMap);			  		  
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		for (String node: mustNodes) {
			CyNode cynode = mustNet.addNode();
			if (ggType) {
				mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", "entrez."+node);
				mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Gene.toString());
				mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeDisplayNameCol, geneDispNameMap.getOrDefault("entrez."+node, ""));
			}
			else if (!ggType) {
				mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", "uniprot."+node);
				mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Protein.toString());	
				mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeDisplayNameCol, geneDispNameMap.getOrDefault("uniprot."+node, ""));
			}			
			mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(particNumb, nodeParticipationMap.getOrDefault(node, null));			
			if (seeds_in_network.contains(node)) {
				mustNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(seedCol, true);
			}
			mapCyNode.put(node, cynode);
		}
		
		mustNet.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
		mustNet.getDefaultEdgeTable().createColumn(particNumb, Integer.class, false);
		
		for (List<String> edge: edges) {
			CyEdge cyedge = mustNet.addEdge(mapCyNode.get(edge.get(0)), mapCyNode.get(edge.get(1)), false);
			mustNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(particNumb, edgeParticipationMap.getOrDefault(edge, null));
			if (ggType) {
				mustNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", "entrez."+edge.get(0) + " (-) " + "entrez."+edge.get(1));				
				mustNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.gene_gene.toString());
			}
			else if (!ggType) {
				mustNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", "uniprot."+edge.get(0) + " (-) " + "uniprot."+edge.get(1));				
				mustNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.protein_protein.toString());
			}
		}
		
		
		
		netMgr.addNetwork(mustNet);
		
		Map<String, Object> args = new HashMap<>();
		args.put("network", "current");
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("view", "create", args, null);
		
		String styleName = "NeDRex";		

		Map<String, Object> argsAS = new HashMap<>();
		argsAS.put("styles", styleName);
		CommandExecuter cmdexAS = new CommandExecuter(app);
		cmdexAS.executeCommand("vizmap", "apply", argsAS, null);
		
		List<CyNode> seeds = CyTableUtil.getNodesInState(mustNet,seedCol,true);
		for (CyNode n: seeds) {
			mustNet.getRow(n).set("selected", true);
		}
		
		if(quick) {
			ClosenessAPI closeness = new ClosenessAPI(mustNet, 100);
			Set<String> genes_must = closeness.getGenes();
			List<List<String>> edgesGG = closeness.getGGEdges();
			List<List<String>> edgesDrP = closeness.getDrPEdges();
			Set<String> drugs = closeness.getDrugs();
			Map<String, Double> drugScoreMap = closeness.getDrugScore();
			Set<String> primary_seeds = closeness.getPrimarySeeds();
			
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			String rankinFunction = "Closeness";
			taskmanager.execute(new TaskIterator(new CreateNetRankedDrugTask(app, true, genes_must, drugs, edgesDrP, drugScoreMap, edgesGG, rankinFunction, newNetName, primary_seeds, true)));						
			
		}
		
		
	}

}
