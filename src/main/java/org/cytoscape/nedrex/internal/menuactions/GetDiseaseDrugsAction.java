package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.GetDiseaseDrugsTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class GetDiseaseDrugsAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InfoBox infoBox;

	private NeDRexService nedrexService;
	@Reference
	public void setNedrexService(NeDRexService nedrexService) {
		this.nedrexService = nedrexService;
	}

	public void unsetNedrexService(NeDRexService nedrexService) {
		if (this.nedrexService == nedrexService)
			this.nedrexService = null;
	}
	
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
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#get-drugs-indicated-in-disease");
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
