package org.cytoscape.nedrex.internal;

import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * NeDRex App
 * @author Andreas Maier
 */
public class ImportNetworkTask extends AbstractTask{
//public class LoadNetworkTask extends AbstractTask implements ObservableTask{
	//private RepoApplication app;
	private RepoApplication app;
	File file;
	//RepoManager manager;

//	@DiablTunable(description="Path:" , groups="Network to laod")
//	public File f;


	private Logger logger = LoggerFactory.getLogger(getClass());

	public ImportNetworkTask(RepoApplication app, File file) {
		this.app = app;
		this.file = file;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Importing the network");
		
		taskMonitor.setProgress(0.55);
		taskMonitor.setStatusMessage("Loading your network into Cytoscape...");
		LoadNetworkFileTaskFactory NFile = app.getActivator().getService(LoadNetworkFileTaskFactory.class);
		taskMonitor.setProgress(0.6);
		DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
//		taskMonitor.setProgress(0.5);
		taskmanager.execute(NFile.createTaskIterator(file));
		taskMonitor.setProgress(0.9);
		taskMonitor.setStatusMessage("Deleting temporary network file...");
		file.delete();
		taskMonitor.setProgress(1.0);
		logger.info("Loading file done");


	}

}
