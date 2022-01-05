package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.QuickStartTask;
import org.cytoscape.myApp.internal.utils.WarningMessages;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class QuickStartAction extends AbstractCyAction{
	private RepoApplication app;
	private InfoBox infoBox;
		
	public QuickStartAction(RepoApplication app) {
		super("Quick Start with Drug Repurposing");
		setPreferredMenu("Apps.NeDRex");
		setMenuGravity(0.9f);
		this.app = app;
		String message = "<html><body>" +
				"To quickly find potential repurposable drugs for the selected diseases in the network<br>" +
				"with one click, Quick Start performs all main steps of the drug repurposing workflow: <br>" +
				"<ul><li> Get disease associated genes</li>" +
				"<li> Run Disease Module Identification</li>" +
				"<li> Run Drug Prioritization</li></ul>" +
				"Required imported network from NeDRexDB:<br>" +
				"A network with at least Disorder-Disorder and Gene-Disorder associations.<br>"+
				"Starting point: <br>" +
				"Selected disorder(s) in the network." +
				"<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#quick-start-with-drug-repurposing", true);
		putValue(SHORT_DESCRIPTION, "To quickly find potential repurposable drugs for the selected diseases in the network, with one click Quick Start performs all main steps of the workflow (1. Get disease associated genes; 2. Run Disease Module Identification; 3. Run Drug Prioritization");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0 && infoBox.getLicensbox().isSelected()) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new QuickStartTask(app)));
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
			taskmanager.execute(new TaskIterator(new QuickStartTask(app)));
		}
		
	}

}
