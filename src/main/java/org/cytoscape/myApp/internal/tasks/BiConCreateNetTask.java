package org.cytoscape.myApp.internal.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.myApp.internal.CommandExecuter;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InteractionType;
import org.cytoscape.myApp.internal.NodeType;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.io.HttpGetWithEntity;
import org.cytoscape.myApp.internal.utils.ViewUtils;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class BiConCreateNetTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Set<String> nodes;
	private List<List<String>> edges;
	Map<String, Double> genesMap1;
	Map<String, Double> genesMap2;
	private String newNetName;
	
	public BiConCreateNetTask (RepoApplication app, Set<String> nodes, List<List<String>> edges, Map<String, Double> genesMap1, Map<String, Double> genesMap2, String newNetName) {
		this.app = app;
		this.nodes = nodes;
		this.edges = edges;
		this.genesMap1 = genesMap1;
		this.genesMap2 = genesMap2;
		this.newNetName = newNetName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetworkManager netMgr = app.getActivator().getService(CyNetworkManager.class);
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		CyNetworkFactory cnf = app.getActivator().getService(CyNetworkFactory.class);
		//CyNetworkViewFactory cnvf = app.getActivator().getService(CyNetworkViewFactory.class);
		//CyNetworkViewManager nvm = app.getActivator().getService(CyNetworkViewManager.class);
		
		String nodeTypeCol = "type";
		String edgeTypeCol = "type";
		String geneGroup = "gene_group";
		String mde = "mean_dif_exp";
		String nodeDisplayNameCol = "displayName";
		List<String> genes = new ArrayList<String>();

		CyNetwork biconNet = cnf.createNetwork();
//		biconNet.getRow(biconNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("BiCoN-network"));
		biconNet.getRow(biconNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(newNetName));
		Map <String, CyNode> mapCyNode = new HashMap<String, CyNode> ();
		
		biconNet.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
		biconNet.getDefaultNodeTable().createColumn(geneGroup, String.class, false);
		biconNet.getDefaultNodeTable().createColumn(mde, Double.class, false);
		biconNet.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
		
		String attrDispName = "displayName";
		JSONObject payload = new JSONObject();
		List<String> attributes = new ArrayList<String>();
		Map<String, String> geneDispNameMap = new HashMap <String, String>();
		
		for (String node: nodes) {
			genes.add("entrez."+node);
		}
		
		payload = new JSONObject();
		attributes = new ArrayList<String>();
		attributes.add(attrDispName);
		payload.put("attributes", attributes);
		payload.put("node_ids", new ArrayList<String>(genes));		
		
//		String url = String.format("https://api.repotrial.net/%s/attributes_v2/json", "gene");
		String url = String.format(Constant.API_LINK + "%s/attributes_v2/json", "gene");
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
		
		for (String node: nodes) {
			CyNode cynode = biconNet.addNode();
//			biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", node);
			biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", "entrez."+node);
			biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Gene.toString());
			biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeDisplayNameCol, geneDispNameMap.getOrDefault("entrez."+node, ""));
			if (genesMap1.keySet().contains(node)) {
				biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(geneGroup, "1");
				biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(mde, genesMap1.get(node));
			}
			else if (genesMap2.keySet().contains(node)) {
				biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(geneGroup, "2");
				biconNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(mde, genesMap2.get(node));
			}
			mapCyNode.put(node, cynode);
		}
		
		biconNet.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
		
		for (List<String> edge: edges) {
			CyEdge cyedge = biconNet.addEdge(mapCyNode.get(edge.get(0)), mapCyNode.get(edge.get(1)), false);
//			biconNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", edge.get(0) + " (-) " + edge.get(1));		
			biconNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", "entrez."+edge.get(0) + " (-) " + "entrez."+edge.get(1));				

			biconNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.gene_gene.toString());	
		}
		
		netMgr.addNetwork(biconNet);
		
		Map<String, Object> args = new HashMap<>();
		args.put("network", "current");
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("view", "create", args, null);
		
		//with the following code, it just view one node!!!
		/*CyNetworkView view = app.getActivator().getService(CyNetworkViewFactory.class).createNetworkView(biconNet);
		CyNetworkViewManager nvm = app.getActivator().getService(CyNetworkViewManager.class);
		nvm.addNetworkView(view);
		app.getActivator().getService(CyApplicationManager.class).setCurrentNetworkView(view);*/
		
		VisualStyle biconStyle = ViewUtils.createBiconStyle(app, biconNet);
		//String styleName = biconNet.getRow(biconNet).get(CyNetwork.NAME, String.class); // name of the network and the visual style is the same
		String styleName = biconStyle.getTitle();		
		logger.info("The name of the visual style to be applied: " + styleName);		

		Map<String, Object> argsAS = new HashMap<>();
		argsAS.put("styles", styleName);
		CommandExecuter cmdexAS = new CommandExecuter(app);
		cmdexAS.executeCommand("vizmap", "apply", argsAS, null);
		
	}
	

}
