package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.SelectAllNodeOfType;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class SelectAllNodeOfTypeAction extends AbstractCyAction{
	
	private RepoApplication app;
	
	public SelectAllNodeOfTypeAction(RepoApplication app) {
		super("All nodes of specific type");
		setPreferredMenu("Apps.NeDRex.Select Nodes");
		setMenuGravity(11.0f);
		this.app = app;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
		taskmanager.execute(new TaskIterator(new SelectAllNodeOfType(app)));
		
	}
	
	
}
