package org.cytoscape.nedrex.internal;

import java.net.URL;

import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class LoadNetworkTask extends AbstractTask{
//public class LoadNetworkTask extends AbstractTask implements ObservableTask{	
	//private RepoApplication app;
	private RepoApplication app;
	String urlp;
	//RepoManager manager;

//	@Tunable(description="Path:" , groups="Network to laod")
//	public File f;
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public LoadNetworkTask(RepoApplication app, String urlp) {
		this.app = app;
		this.urlp = urlp;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Loading the network");
		
				
	    /*String option = app.getPanel().getProjectionOption();
	    String path = "";
	    String urlp = "";
	    boolean ns = false;
	    if (option.equals("DGPP network")) {
	    	path = "/Users/sepideh/Documents/TUM/REPO-TRIAL/WP1/PGD-files/protein-gene-disorder-Dec12/mine/protein-gene-disorder-labeled.cys";
	    	urlp = "http://repotrial.bioswarm.net/datasets/Projections-sample/protein-gene-disorder-labeled.cys";
	    	//urlp = "http://repotrial.bioswarm.net/datasets/Projections-sample/toy-session.cys";
	    	ns = true;
	    }
	    else if (option.equals("hdn.jaccard network")) {
	    	path = "/Users/sepideh/Documents/TUM/REPO-TRIAL/WP1/PGD-files/diseasome files/hdn.jaccard.graphml";
	    	urlp = "http://repotrial.bioswarm.net/datasets/Diseasome-2.1/hdn.jaccard.graphml";
	    	ns = false;
		}
	    else if (option.equals("hdn.ppisp network")) {
	    	path = "/Users/sepideh/Documents/TUM/REPO-TRIAL/WP1/PGD-files/diseasome files/hdn.ppisp_avg1.graphml";
	    	urlp = "http://repotrial.bioswarm.net/datasets/Diseasome-2.1/hdn.ppisp_avg1.graphml";
	    	ns = false;
		}*/
	    
	    /// ---Load network from a local file---
/*		File f = new File (path);		
		if (f == null) {
			JOptionPane.showMessageDialog(null, "file not selected!");
			logger.info("file not selected");
		}
		else {
			LoadNetworkFileTaskFactory NFile = app.getActivator().getService(LoadNetworkFileTaskFactory.class);
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(NFile.createTaskIterator(f));
		}*/
		
		/// ---Load network from a URL---
		/*URL u = new URL (urlp);
		if (f == null) {
			JOptionPane.showMessageDialog(null, "file not selected!");
			logger.info("file not selected");
		}
		else {
			LoadNetworkURLTaskFactory NFile = app.getActivator().getService(LoadNetworkURLTaskFactory.class);
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(NFile.loadCyNetworks(u));
		}*/
		//String urlp = "http://repotrial.bioswarm.net:5000/download/b644369d-675e-46f8-9635-55b68fd13666.graphml";
		taskMonitor.setProgress(0.1);
		taskMonitor.setStatusMessage("Loading your network into Cytoscape...");
		URL u = new URL (urlp);
		LoadNetworkURLTaskFactory NFile = app.getActivator().getService(LoadNetworkURLTaskFactory.class);
		taskMonitor.setProgress(0.3);
		DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
		taskMonitor.setProgress(0.5);
		taskmanager.execute(NFile.loadCyNetworks(u));
		//manager.execute(NFile.loadCyNetworks(u));

		/// ---Load network by command from a local file---
/*		Map<String, Object> args = new HashMap<>();
		//network import file file="/Users/sepideh/Documents/TUM/Cytoscape/input-data/P1.graphml"
		args.put("file", "/Users/sepideh/Documents/TUM/Cytoscape/input-data/P1.graphml");
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("network", "import file", args, null);*/
		
		/// ---Load session file by command from a local file---
/*		Map<String, Object> args1 = new HashMap<>();
		args1.put("file", "/Users/sepideh/Documents/TUM/Cytoscape/toy-session.cys");
		CommandExecuter cmdex1 = new CommandExecuter(app);
		cmdex1.executeCommand("session", "open", args1, null);*/
		
/*		/// ---Load session file by command from a URL---
		Map<String, Object> args2 = new HashMap<>();
		args2.put("url", "http://repotrial.bioswarm.net/datasets/Projections-sample/toy-session.cys");
		CommandExecuter cmdex2 = new CommandExecuter(app);
		cmdex2.executeCommand("session", "open", args2, null);*/
		
		/*
		 * Load any file (network/session) from url
		 */
		
		/*String path = "";
		//String urlp = "http://repotrial.bioswarm.net:5000/download/ef7c976b-c3cb-4257-9005-db27901972a8.graphml";
		String urlp = "http://repotrial.bioswarm.net:5000/download/b644369d-675e-46f8-9635-55b68fd13666.graphml";
	    boolean ns = false;
	    
	    
		Map<String, Object> argsL = new HashMap<>();
		argsL.put("url", urlp);
		CommandExecuter cmdexL = new CommandExecuter(app);
		if (ns == true) {
			cmdexL.executeCommand("session", "open", argsL, null);
		}
		else if (ns == false) {
			cmdexL.executeCommand("network", "load url", argsL, null);
		}*/
		taskMonitor.setProgress(1.0);
		logger.info("Loading file done");


		
	}

//	@Override
	/*public <R> R getResults(Class<? extends R> type) {
		// TODO Auto-generated method stub
		return null;
	}*/
	/*public <T> T getResults(Class<? extends T> type) {
		return null;
	}*/
}
