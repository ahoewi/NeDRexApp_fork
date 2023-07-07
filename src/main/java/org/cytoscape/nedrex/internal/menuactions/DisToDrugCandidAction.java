package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.DisToDrugCandidTask;
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
public class DisToDrugCandidAction extends AbstractCyAction{
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


	public DisToDrugCandidAction(RepoApplication app) {
		super("Start with Disease -> Drugs");
		setPreferredMenu("Apps.NeDRex.Exploratory Functions");
		setMenuGravity(27.1f);
		this.app = app;
		//TODO: How to write this message correctly?
		String message = "<html><body>" +
				"Starting with a set of selected diseases, Disease->Gene->Protein->Drug paths in the network will be returned. <br>" +
				"There's an option to run Steiner tree on the intermediate genes/proteins to expand the exploration.<br><br>" +
				"Required imported network from NeDRexDB:<br>" +
				"A network with at least Gene-Disorder, Gene-Protein, Protein-Protein and Drug-Protein association types.<br>"
				+ "Starting point: <br>"
				+ "Selected disorder(s) in the network."
				+ "<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#start-with-disease-drugs", true);
		putValue(SHORT_DESCRIPTION,"Starting with a set of selected diseases, Disease->Gene->Protein->Drug paths in the network will be returned. There's an option to run Steiner tree on the intermediate genes/proteins to expand the exploration.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0 && infoBox.getLicensbox().isSelected()) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new DisToDrugCandidTask(app)));
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
			taskmanager.execute(new TaskIterator(new DisToDrugCandidTask(app)));
		}
		
	}

}
