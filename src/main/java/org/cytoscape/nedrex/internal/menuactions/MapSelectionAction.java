package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.MapSelectionToNetTask;
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
public class MapSelectionAction extends AbstractCyAction{
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


	public MapSelectionAction (RepoApplication app) {
		super("Map Selection to another Network");
		setPreferredMenu("Apps.NeDRex.Supplementary Functions");
		setMenuGravity(38.0f);
		this.app = app;
		String message = "<html><body>" +
				"The nodes and edges selected in the current network will be selected <br>" +
				"in another network, if they exist.<br><br>" +
				"Starting point:<br>" +
				"A selection of nodes in the network.<br><br><br></body></html>";
		this.infoBox = new InfoBox(app, message, this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#map-selection-to-another-network");
		putValue(SHORT_DESCRIPTION,"The nodes & edges selected in the current network will be selected in the specified target network, if they exist.");
		//this.menuGravity = 4;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new MapSelectionToNetTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
		} else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new MapSelectionToNetTask(app)));
		}
		
	}

}
