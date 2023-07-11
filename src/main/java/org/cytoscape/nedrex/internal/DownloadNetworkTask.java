package org.cytoscape.nedrex.internal;

import java.io.*;
import java.net.URL;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeDRex App
 *
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class DownloadNetworkTask extends AbstractTask {
    //public class LoadNetworkTask extends AbstractTask implements ObservableTask{
    //private RepoApplication app;
    private RepoApplication app;
    String urlp;

    File file;
    //RepoManager manager;

    NeDRexService nedrexService;

//	@DiablTunable(description="Path:" , groups="Network to laod")
//	public File f;


    private Logger logger = LoggerFactory.getLogger(getClass());

    public DownloadNetworkTask(RepoApplication app, String urlp, File file, NeDRexService neDRexService) {
        this.app = app;
        this.urlp = urlp;
        this.file = file;
        this.nedrexService = neDRexService;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setTitle("Downloading the network");
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.file));
        double p = 0.1;
        taskMonitor.setProgress(p);
        taskMonitor.setStatusMessage("Downloading your network into Cytoscape. Depending on the network and your internet speed, this may take some minutes...");
        HttpGet request = new HttpGet(this.urlp);
        HttpResponse response = nedrexService.send(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "";
        int lines = 0;
        boolean progress = true;
        while ((line = rd.readLine()) != null) {
            bw.write(line);
            bw.newLine();
            lines++;
            if ( progress && lines == 1000) {
                lines = 0;
                p += 0.0001;
                taskMonitor.setProgress(p);
                if(p>=0.5)
                    progress = false;
            }
        }
        bw.close();
        logger.info("Downloading file done");
    }

}
