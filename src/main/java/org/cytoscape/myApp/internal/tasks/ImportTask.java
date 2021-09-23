package org.cytoscape.myApp.internal.tasks;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.LoadNetworkTask;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.ui.SearchOptionPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
/**
 * NeDRex App
 * @author Sepideh Sadegh, Judith Bernett
 */
public class ImportTask extends AbstractTask {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private RepoApplication app;
    private SearchOptionPanel optionsPanel;

    public ImportTask(RepoApplication app, SearchOptionPanel optionsPanel){
        this.app = app;
        this.optionsPanel = optionsPanel;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Importing the network");
        taskMonitor.setProgress(0.0);

        JSONObject payload = new JSONObject();
        List<String> nodes = new ArrayList<String>();
        List<String> edges = new ArrayList<String>();

        taskMonitor.setProgress(0.1);
        taskMonitor.setStatusMessage("Processing your request...");

        edges = optionsPanel.getSelectedEdgeTypes();
        List<String> iidEvids = optionsPanel.getIIDevidence();
        List<String> drugGroups = optionsPanel.getSelectedDrugGroups();
        Boolean ppiSL = optionsPanel.getSelfLoop();
        List<Integer> taxIDs = new ArrayList<Integer>();
        taxIDs.add(9606);
        if (optionsPanel.allTaxIDSelected()) {
            taxIDs.add(-1);
        }
        System.out.println("This is the selected threshold: " + optionsPanel.getThreshold());
        String networkName = optionsPanel.getNetworkName();
        logger.info("The entered name of the new network by user: " + networkName);

//        Boolean concise = false;
        Boolean concise = optionsPanel.conciseVersion();
        logger.info("The option selected for concise: " + concise);
        payload.put("nodes", nodes);
        payload.put("edges", edges);
        payload.put("iid_evidence", iidEvids);
        payload.put("ppi_self_loops", ppiSL);
        payload.put("taxid", taxIDs);
        payload.put("concise", concise);
        if(optionsPanel.includeDisGeNet()) {
        	payload.put("disgenet_threshold", optionsPanel.getThreshold());
        }
        else if (!optionsPanel.includeDisGeNet()) {
        	payload.put("disgenet_threshold", 2D);
        }
        payload.put("include_omim", optionsPanel.includeOMIM());
        payload.put("drug_groups", drugGroups);

        logger.info("The post JSON converted to string: " + payload.toString());


//        HttpPost post = new HttpPost("https://api.repotrial.net/graph_builder");
        HttpPost post = new HttpPost(Constant.API_LINK+ "graph_builder");
        HttpClient client = new DefaultHttpClient();

        post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        String uidd = new String();

        taskMonitor.setProgress(0.2);
        taskMonitor.setStatusMessage("Sending your request to our server...");

        try {

            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line = "";
            logger.info("Response entity: ");
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
        String uid = uidd.replace("\"", "");
        HttpGet request = new HttpGet(Constant.API_LINK+"graph_details/"+uid);        

        taskMonitor.setProgress(0.3);
        taskMonitor.setStatusMessage("Your network is being built...");
        double progress = 0.3;
        try {
            HttpResponse response = client.execute(request);
            boolean Success = false;
            boolean Failed = false;

            // we're letting it build for t*10 seconds
            for (int t=0; t<60; t++) {
                BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                    logger.info(line);
                    if (line.contains("completed"))
                        Success=true;
                    if (line.contains("failed")) {
                        Failed=true;
                    }
                }
                if(Success) {
                    taskMonitor.setProgress(1.0);
                    taskMonitor.setStatusMessage("Successfully built the network!");
                    System.out.println("The built was successful!!!");
                    logger.info("The built was successful!");
                    String urlp = "";
                    if (!networkName.equals("")) {
                        urlp = Constant.API_LINK+"graph_download_v2/"+uid+"/"+networkName+".graphml";
                        
                    }
                    else {
                        urlp = Constant.API_LINK+"graph_download/"+uid+".graphml";
                        
                    }

                    DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
                    taskmanager.execute(new TaskIterator(new LoadNetworkTask(app,urlp)));
                    break;
                }
                if (Failed) {
                    logger.info("The build is failed!");
                    break;
                }
                response = client.execute(request);
                try {
                    if(progress < 0.9) {
                        progress += 0.1;
                    }
                    taskMonitor.setProgress(progress);
                    logger.info("Waiting for build to complete, sleeping for 10 seconds...");
                    Thread.sleep(10000);
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
