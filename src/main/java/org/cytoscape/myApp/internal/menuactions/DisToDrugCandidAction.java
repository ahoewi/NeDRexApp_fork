package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.DisToDrugCandidTask;
import org.cytoscape.myApp.internal.utils.WarningMessages;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class DisToDrugCandidAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InfoBox infoBox;
	
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
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#start-with-disease-drugs", true);
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
