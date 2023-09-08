package org.cytoscape.nedrex.internal.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
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
 * @author Andreas Maier
 */
public class RobustCreateNetTask extends AbstractTask {

    private RepoApplication app;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Set<String> robustNodes;
    private Set<List<String>> edges;
    private Map<String, Integer> occurenceMap;
    private Map<String, Double> significanceMap;
    private Set<String> seeds_in_network;
    private Boolean ggType;
    private String newNetName;
    private Boolean quick;

    public RobustCreateNetTask(RepoApplication app, Boolean quick, Set<String> robustNodes, Set<List<String>> edges, Map<String, Integer> occurenceMap,
                               Map<String, Double> significanceMap, Set<String> seeds_in_network, Boolean ggType, String newNetName) {
        this.app = app;
        this.setNedrexService(app.getNedrexService());
        this.quick = quick;
        this.robustNodes = robustNodes;
        this.edges = edges;
        this.occurenceMap = occurenceMap;
        this.significanceMap = significanceMap;
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
        String significance = "significance";
        String occurences = "occurences";
        String nodeDisplayNameCol = "displayName";
        String seedCol = "seed";
        List<String> genes = new ArrayList<String>(); // could be protein
        String geneEntity = "gene";
        String proteinEntity = "protein";

        CyNetwork diamondNet = cnf.createNetwork();
        diamondNet.getRow(diamondNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(newNetName));
        Map<String, CyNode> mapCyNode = new HashMap<String, CyNode>();

        diamondNet.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
        diamondNet.getDefaultNodeTable().createColumn(occurences, Integer.class, false);
        diamondNet.getDefaultNodeTable().createColumn(significance, Double.class, false);
        diamondNet.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
        diamondNet.getDefaultNodeTable().createColumn(seedCol, Boolean.class, false);

        String attrDispName = "displayName";
        JSONObject payload = new JSONObject();
        List<String> attributes = new ArrayList<String>();
        Map<String, String> geneDispNameMap = new HashMap<String, String>(); // could be protein

        if (ggType) {
            for (String node : robustNodes) {
                genes.add("entrez." + node);
            }
        } else {
            for (String node : robustNodes) {
                genes.add("uniprot." + node);
            }
        }

        payload = new JSONObject();
        attributes = new ArrayList<>();
        attributes.add(attrDispName);
        payload.put("attributes", attributes);
        payload.put("node_ids", new ArrayList<>(genes));

        String url = new String();
        if (ggType) {
            url = String.format(this.nedrexService.API_LINK + "%s/attributes/json", geneEntity);
        } else  {
            url = String.format(this.nedrexService.API_LINK + "%s/attributes/json", proteinEntity);
        }

        HttpGetWithEntity e;
        e = new HttpGetWithEntity();
        e.setURI(new URI(url));
        e.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

        try {
            HttpResponse response = nedrexService.send(e);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            String responseText = "";
            while ((line = rd.readLine()) != null) {
                responseText = line;
            }

            JSONParser parser = new JSONParser();
            JSONArray jarrGenes = (JSONArray) parser.parse(responseText);
            for (Object gene : jarrGenes) {
                JSONObject geneobj = (JSONObject) gene;
                String g = (String) geneobj.get("primaryDomainId");
                String dispName = (String) geneobj.get(attrDispName);
                if (genes.contains(g)) {
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


        for (String node : robustNodes) {
            CyNode cynode = diamondNet.addNode();
            if (ggType) {
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", "entrez." + node);
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Gene.toString());
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeDisplayNameCol, geneDispNameMap.getOrDefault("entrez." + node, ""));
            } else if (!ggType) {
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set("name", "uniprot." + node);
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeTypeCol, NodeType.Protein.toString());
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(nodeDisplayNameCol, geneDispNameMap.getOrDefault("uniprot." + node, ""));
            }
            diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(occurences, occurenceMap.getOrDefault(node, null));
            diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(significance, significanceMap.getOrDefault(node, null));
            if (seeds_in_network.contains(node)) {
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(seedCol, true);
            } else {
                diamondNet.getDefaultNodeTable().getRow(cynode.getSUID()).set(seedCol, false);
            }
            mapCyNode.put(node, cynode);
        }

        diamondNet.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);

        for (List<String> edge : edges) {
            CyEdge cyedge = diamondNet.addEdge(mapCyNode.get(edge.get(0)), mapCyNode.get(edge.get(1)), false);
            if (ggType) {
                diamondNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", "entrez." + edge.get(0) + " (-) " + "entrez." + edge.get(1));
                diamondNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.gene_gene.toString());
            } else if (!ggType) {
                diamondNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set("name", "uniprot." + edge.get(0) + " (-) " + "uniprot." + edge.get(1));
                diamondNet.getDefaultEdgeTable().getRow(cyedge.getSUID()).set(edgeTypeCol, InteractionType.protein_protein.toString());
            }
        }


        netMgr.addNetwork(diamondNet);

        Map<String, Object> args = new HashMap<>();
        args.put("network", "current");
        CommandExecuter cmdex = new CommandExecuter(app);
        cmdex.executeCommand("view", "create", args, null);

        String styleName = "NeDRex";

        Map<String, Object> argsAS = new HashMap<>();
        argsAS.put("styles", styleName);
        CommandExecuter cmdexAS = new CommandExecuter(app);
        cmdexAS.executeCommand("vizmap", "apply", argsAS, null);

        List<CyNode> seeds = CyTableUtil.getNodesInState(diamondNet, seedCol, true);
        for (CyNode n : seeds) {
            diamondNet.getRow(n).set("selected", true);
        }

        if (quick) {
            ClosenessAPI closeness = new ClosenessAPI(app.getNedrexService(), app.getApiRoutesUtil(), diamondNet, 100);
            Set<String> genes_diamonds = closeness.getGenes();
            List<List<String>> edgesGG = closeness.getGGEdges();
            List<List<String>> edgesDrP = closeness.getDrPEdges();
            Set<String> drugs = closeness.getDrugs();
            Map<String, Double> drugScoreMap = closeness.getDrugScore();
            Set<String> primary_seeds = closeness.getPrimarySeeds();

            DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
            String rankinFunction = "Closeness";
            taskmanager.execute(new TaskIterator(new CreateNetRankedDrugTask(app, true, genes_diamonds, drugs, edgesDrP, drugScoreMap, edgesGG, rankinFunction, newNetName, primary_seeds, true)));

        }

    }

}
