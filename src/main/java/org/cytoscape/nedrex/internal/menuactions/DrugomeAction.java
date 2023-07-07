package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.DrugomeTask;
import org.cytoscape.nedrex.internal.utils.WarningMessages;
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
public class DrugomeAction extends AbstractCyAction{
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


	public DrugomeAction (RepoApplication app) {
		super("Drugome");
		setPreferredMenu("Apps.NeDRex.Projections of Heterogeneous Network");
//		setMenuGravity(11.0f);
		setMenuGravity(45.0f);
		this.app = app;
		String message = "<html><body>" +
				"Creates a Drug-Drug projection of the heterogeneous network based on either shared indications (diseases)<br>" +
				"or shared targets between drugs.<br><br>" +
				"Required imported network from NeDRexDB:<br>" +
				"For the projected network based on shared indication, a network with at least Drug-Disorder association type.<br>"
				+ "For the projected network based on shared targets, a network with at least Drug-Protein association type.<br>"			
				+ "<br></body></html>";
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#drugome", true);
		putValue(SHORT_DESCRIPTION,"Creates a Drug-Drug projection of the heterogeneous network based on either shared indications (diseases) or shared targets between drugs.");
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0 && infoBox.getLicensbox().isSelected()) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new DrugomeTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
			else if (returnedValue == 0 && !infoBox.getLicensbox().isSelected()) {
				WarningMessages.showAgreementWarning();
			}
		}else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new DrugomeTask(app)));
		}
		
	}

}
