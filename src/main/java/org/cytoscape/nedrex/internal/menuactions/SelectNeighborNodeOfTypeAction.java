package org.cytoscape.nedrex.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.SelectNeighborNodeOfType;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class SelectNeighborNodeOfTypeAction extends AbstractCyAction{
	private RepoApplication app;
	
	public SelectNeighborNodeOfTypeAction (RepoApplication app) {
		super("Neighbors of specific type");
		setPreferredMenu("Apps.NeDRex.Select Nodes");
		setMenuGravity(11.0f);
		this.app = app;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
		taskmanager.execute(new TaskIterator(new SelectNeighborNodeOfType(app)));
		
	}

}
