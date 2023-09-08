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
public class MetagraphTask extends AbstractTask {
    private RepoApplication app;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private NeDRexService nedrexService;
    private ApiRoutesUtil apiUtils;

    @ProvidesTitle
    public String getTitle() {
        return "Set Parameters for mapping selected ICD10 comorbiditome nodes to MONDO space";
    }

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

    public MetagraphTask(RepoApplication app) {
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

        String newNetName = "";
        if (!set_net_name) {
            String netName = "NeDRex Metagraph";
            newNetName = namingUtil.getSuggestedNetworkTitle(netName);
        } else {
            newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
        }


        DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
        taskmanager.execute(new TaskIterator(new CreateMetagraphTask(app, newNetName)));

    }

}
