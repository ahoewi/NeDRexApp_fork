package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.DiseasomeTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @modified by: Andreas Maier
 */
public class DiseasomeAction extends AbstractCyAction{
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
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#diseasome");
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
