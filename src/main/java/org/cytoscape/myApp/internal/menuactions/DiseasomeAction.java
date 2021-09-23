package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.DiseasomeTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class DiseasomeAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InfoBox infoBox;
	
	public DiseasomeAction (RepoApplication app) {
		super("Diseasome");
		setPreferredMenu("Apps.NeDRex.Projections of Heterogeneous Network");
//		setMenuGravity(12.0f);
		setMenuGravity(45.0f);
		this.app = app;
		//TODO
		String message = "<html><body>" +
				"Creates a Disease-Disease projection of the heterogeneous network based on either shared drugs<br>" +
				"or shared genes between diseases.<br><br>" +
				"Required imported network from NeDRexDB:<br>" +
				"For the projected network based on shared drugs, a network with at least Drug-Disorder association type.<br>"
				+ "For the projected network based on shared genes, a network with at least Gene-Disorder association type.<br>"
				+ "<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#diseasome");
		putValue(SHORT_DESCRIPTION,"Creates a Disease-Disease projection of the heterogeneous network based on either shared drugs or shared genes between diseases.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new DiseasomeTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
		}else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new DiseasomeTask(app)));
		}
		
	}

}
