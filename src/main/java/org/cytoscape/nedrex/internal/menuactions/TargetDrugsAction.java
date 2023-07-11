package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.AllDrugsTargetTask;
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
 * @author Andreas Maier
 */
public class TargetDrugsAction extends AbstractCyAction{
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

	public TargetDrugsAction (RepoApplication app) {
		super("All drugs targeting the selection");
		setPreferredMenu("Apps.NeDRex.Drug Prioritization");
		setMenuGravity(20.2f);
		String message = "<html><body>" +
				"Returns all drugs targeting the selection.<br><br>" +
				"Before continuing with this function, make sure you have:<br>" +
				"a) selected either all genes/proteins in a returned disease module or a subset of them or;<br>"
				+"b) selected genes/proteins which you are interested to rank drugs targeting them."
				+ "<br><br></body></html>";
		this.app = app;
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#all-drugs-targeting-the-selection", true);
		putValue(SHORT_DESCRIPTION, "Returns all drugs targeting the selection");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0 && infoBox.getLicensbox().isSelected()) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new AllDrugsTargetTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
			else if (returnedValue == 0 && !infoBox.getLicensbox().isSelected()) {
				WarningMessages.showAgreementWarning();
			}

		} else{
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new AllDrugsTargetTask(app)));
		}
		
	}

}
