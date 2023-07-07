package org.cytoscape.nedrex.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.task.select.DeselectAllTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class DeselectAll extends AbstractTask {
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private CyNetwork network;
	
	public DeselectAll (RepoApplication app, CyNetwork network) {
		this.app = app;
		this.network = network;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		DeselectAllTaskFactory datf = app.getActivator().getService(DeselectAllTaskFactory.class);
		DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
		TaskIterator ti = datf.createTaskIterator(network);
		taskmanager.execute(ti);
		
	}


}
