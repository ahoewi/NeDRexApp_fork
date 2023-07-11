package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.ClosenessDrugTask;
import org.cytoscape.nedrex.internal.utils.WarningMessages;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.service.component.annotations.Reference;

import java.awt.event.ActionEvent;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class ClosenessDrugAction extends AbstractCyAction{
	
	private RepoApplication app;
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


	public ClosenessDrugAction(RepoApplication app) {
		super("Rank drugs with Closeness Centrality");
		setPreferredMenu("Apps.NeDRex.Drug Prioritization");
		setMenuGravity(20.4f);
		this.app = app;
		String message = "<html><body>" +
				"Closeness is a node centrality measure that ranks the nodes in a network<br>" +
				"based on the lengths of their shortest paths to all other nodes in the network.<br>" +
				"Here, a modified version is implemented, where closeness is calculated with <br>" +
				"respect to only the selected seeds.<br><br>" +
				"Before continuing with this function, make sure you have:<br>" +
				"a) selected either all genes/proteins in a returned disease module or a subset of them or;<br>"
				+"b) selected genes/proteins which you are interested to rank drugs targeting them."
				+ "<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#rank-drugs-with-closeness-centrality", true);
		putValue(SHORT_DESCRIPTION,"Closeness is a node centrality measure that ranks the nodes in a network based on the lengths of their shortest paths to all other nodes in the network. Here, a modified version is implemented, where closeness is calculated with respect to only the selected seeds.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0 && infoBox.getLicensbox().isSelected()) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new ClosenessDrugTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
			else if(returnedValue == 0 && !infoBox.getLicensbox().isSelected()) {
				WarningMessages.showAgreementWarning();
			}

		} else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new ClosenessDrugTask(app)));
		}
		
	}

}
