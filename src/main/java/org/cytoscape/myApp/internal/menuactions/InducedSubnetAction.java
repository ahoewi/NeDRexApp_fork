package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.FindEdgeBtwNodesTask;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class InducedSubnetAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InfoBox infoBox;
	
	public InducedSubnetAction (RepoApplication app) {
		super("Induced Subnetwork of selected nodes");
		setPreferredMenu("Apps.NeDRex.Supplementary Functions");
		setMenuGravity(37.0f);
		this.app = app;
		String message = "<html><body>" +
				"The induced subnetwork is formed from a subset of the nodes in network and all of <br>" +
				"the edges connecting node pairs in that subset.<br><br>" +
				"Starting point:<br>" +
				"A selection of nodes in the network. <br><br><br> </body></html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#induced-subnetwork-of-selected-nodes");
		putValue(SHORT_DESCRIPTION,"The induced subnetwork is formed from a subset of the nodes in network and all of the edges connecting node pairs in that subset.");
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new FindEdgeBtwNodesTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
		} else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new FindEdgeBtwNodesTask(app)));
		}
		
	}

}
