package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.NeighborModuleTask;
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
public class NeighborModuleAction extends AbstractCyAction{
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


	public NeighborModuleAction(RepoApplication app) {
		super("Neighbor Module from Seed Proteins");
		setPreferredMenu("Apps.NeDRex.Exploratory Functions");
		setMenuGravity(27.0f);
		this.app = app;
		String message = "<html><body>" +
				"This function returns the direct neighborhood of selected proteins in the network<br>" +
				"along with their Subnetwork Participation Degree (SPD). There's also an option to<br>" +
				"return the associated diseases. <br><br>" +
				"Required imported network from NeDRexDB:<br>" +
				"A network with at least Protein-Protein associations. For full function, <br>" +
				"a network with Gene-Disorder, Gene-Protein and Protein-Protein associations.<br>" +
				"Starting point: <br>"
				+ "Selected protein(s) in the network."
				+ "<br><br><br></body></html>";
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#neighbor-module-from-seed-proteins");
		putValue(SHORT_DESCRIPTION,"This function returns the direct neighborhood of selected proteins in the network along with their Subnetwork Participation Degree (SPD). There's also an option to return the associated diseases. ");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new NeighborModuleTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
		}else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new NeighborModuleTask(app)));
		}
		
	}

}
