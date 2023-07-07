package org.cytoscape.nedrex.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.CreateClusterTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
// not used in the current version
public class ViewClusterAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InfoBox infoBox;
	
	public ViewClusterAction(RepoApplication app) {
		super("Create a view for Clusters");
		setPreferredMenu("Apps.NeDRex");
		setMenuGravity(5.0f);
		this.app = app;
		//TODO
		String message = "<html><body>" +
				"**TODO**<br><br><br></body></html>";
		this.infoBox = new InfoBox(app, message);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new CreateClusterTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
		}else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new CreateClusterTask(app)));
		}
		
	}

}
