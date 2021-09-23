package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.SelNodesFromFileTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class SelectNodeFileAction extends AbstractCyAction{
	private RepoApplication app;
	
	public SelectNodeFileAction (RepoApplication app) {
		super("From File");
		setPreferredMenu("Apps.NeDRex.Select Nodes");
		setMenuGravity(10.0f);
		this.app = app;		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
		taskmanager.execute(new TaskIterator(new SelNodesFromFileTask(app)));
		
	}

}
