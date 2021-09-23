package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.GetDiseaseDrugsTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class GetDiseaseDrugsAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InfoBox infoBox;
	
	public GetDiseaseDrugsAction(RepoApplication app) {
		super("Get Drugs indicated in disease");
		setPreferredMenu("Apps.NeDRex.Exploratory Functions");
		setMenuGravity(1.2f);
		this.app = app;
		String message = "<html><body>" +
				"Get drugs indicated for the treatment of the selected disorders (based on DrugCentral database)<br><br>" +
				"Required imported network from NeDRexDB:<br>" +
				"A network with at least Drug-Disorder association type.<br>"+
				"Starting point: <br>" +
				"Selected disorder(s) in the network." +
				"<br><br><br> </body><html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#get-drugs-indicated-in-disease");
		putValue(SHORT_DESCRIPTION, "Get drugs indicated for the treatment of the selected disorders (based on DrugCentral database)");		

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new GetDiseaseDrugsTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
		}else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new GetDiseaseDrugsTask(app)));
		}
		
	}

}
