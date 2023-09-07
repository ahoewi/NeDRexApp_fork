package org.cytoscape.nedrex.internal.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.nedrex.internal.ModelUtil;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.NodeType;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.utils.ApiRoutesUtil;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.*;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * NeDRex App
 *
 * @author Andreas Maier
 */
public class ICD10toMONDOTask extends AbstractTask {
    private RepoApplication app;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private NeDRexService nedrexService;
    private ApiRoutesUtil apiUtils;

    @ProvidesTitle
    public String getTitle() {
        return "Set Parameters for mapping selected ICD10 comorbiditome nodes to MONDO space";
    }



    @Tunable(description = "Include self references of nodes", groups = "Parameters",
            tooltip = "Select, if you would like to include self references of nodes in the result network",
            gravity = 5.0)
    public Boolean self_loops = false;


    @Tunable(description = "Map by using existing disease network", groups = "Result network",
            tooltip = "Select, if you have another network with MONDO IDs already loaded.",
            gravity = 6.0)
    public Boolean use_mapping_network = false;

    private ListSingleSelection<String> allNets;
    @Tunable(description = "Name of the target network to map the selection to:", groups = "Result network", params = "displayState=uncollapsed",
            longDescription = "If no network is used, select ```--NONE---```",
            dependsOn = "use_mapping_network=true",
            gravity = 7.0)
    public ListSingleSelection<String> getallNets() {
        //attribute = new ListSingleSelection<>("Current collection", "--Create new network collection--");
        allNets = ModelUtil.updateNetworksList(app, allNets);
        System.out.println("Loading networks for icd10mondotask");
        return allNets;
    }
    public void setallNets(ListSingleSelection<String> nets) { }


    @Tunable(description = "Use custom name for the result network", groups = "Result network",
            tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name based on the selected algorithm parameters will be assigned",
            gravity = 5.0)
    public Boolean set_net_name = false;

    @Tunable(description = "Name of the result network",
            groups = "Result network",
            dependsOn = "set_net_name=true",
            tooltip = "Enter the name you would like to have assigned to the result network",
            gravity = 5.0)
    public String new_net_name = new String();

    public ICD10toMONDOTask(RepoApplication app) {
        this.app = app;
        this.setNedrexService(app.getNedrexService());
        this.apiUtils = app.getApiRoutesUtil();
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

        CyNetwork targetNet = null;
        if (use_mapping_network)
            targetNet = ModelUtil.getNetworkWithName(app, allNets.getSelectedValue());

        String newNetName = "";
        if (!set_net_name) {
            String netName = "MODNO_Comorbiditome";
            newNetName = namingUtil.getSuggestedNetworkTitle(netName);
        } else {
            newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
        }

        String submit_url = this.nedrexService.API_LINK + "comorbiditome/icd10_to_mondo";

        JSONObject payload = new JSONObject();
        List<String> icd10_disorders = new ArrayList<>();
        Set<CyEdge> edgesBetwDisorders = new HashSet<>();
        int sleep_time = 3; //in seconds

        List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
        List<String> selectedNodeNames = new ArrayList<>();

        for (CyNode n : selectedNodes) {
            String nodeName = network.getRow(n).get(CyNetwork.NAME, String.class);
            selectedNodeNames.add(nodeName);
            if (network.getRow(n).get("type", String.class).equals(NodeType.Disease.toString())) {
                icd10_disorders.add(nodeName);
            }
        }

        for (CyNode s1 : selectedNodes) {
            for (CyNode s2 : selectedNodes) {
                if (!s1.equals(s2)) {
                    edgesBetwDisorders.addAll(network.getConnectingEdgeList(s1, s2, CyEdge.Type.ANY));
                    edgesBetwDisorders.addAll(network.getConnectingEdgeList(s2, s1, CyEdge.Type.ANY));
                }
            }
        }

        logger.info("The list of selected nodes or disorders: " + icd10_disorders);

        payload.put("icd10", icd10_disorders);

        logger.info("The post JSON converted to string: " + payload);

        HttpPost post = new HttpPost(submit_url);
        post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        Boolean failedSubmit = false;

        try {
            HttpResponse response = nedrexService.send(post);
            JSONParser parser = new JSONParser();
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JSONObject json = (JSONObject) parser.parse(rd.readLine());

            HashMap<String, LinkedList<String>> new_edges = new HashMap<>();

            HashMap<String, HashSet<String>> icd10_to_mondo = new HashMap<>();

            json.forEach((icd10, mondo_ids) -> icd10_to_mondo.put((String) icd10, new HashSet<String>((JSONArray) mondo_ids)));

            edgesBetwDisorders.forEach(edge -> {
                String source_name = network.getRow(edge.getSource()).get(CyNetwork.NAME, String.class);
                String target_name = network.getRow(edge.getTarget()).get(CyNetwork.NAME, String.class);

                new_edges.putIfAbsent(source_name, new LinkedList<>());
                new_edges.get(source_name).add(target_name);
            });

            DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
            taskmanager.execute(new TaskIterator(new CreateMONDOComorbiditomeTask(app, network, icd10_to_mondo, new_edges, self_loops, targetNet, newNetName)));

        } catch (ClientProtocolException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

    }

}
