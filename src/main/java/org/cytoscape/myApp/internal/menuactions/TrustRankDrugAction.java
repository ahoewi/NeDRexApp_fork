package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.TrustRankDrugTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class TrustRankDrugAction extends AbstractCyAction{
	private RepoApplication app;
	private InfoBox infoBox;
	
	public TrustRankDrugAction(RepoApplication app) {
		super("Rank drugs with TrustRank");
		setPreferredMenu("Apps.NeDRex.Drug Prioritization");
		setMenuGravity(20.6f);
		this.app = app;
		String message = "<html><body>" +
				"TrustRank ranks nodes in a network based on how well they are connected to a <br>" +
				"(trusted) set of seed nodes. It is a modification of Google’s PageRank algorithm, <br>" +
				"where “trust” is iteratively propagated from seed nodes to adjacent nodes using <br>" +
				"the network topology.<br><br>" +
				"Before continuing with this function, make sure you have:<br>" +
				"a) selected either all genes/proteins in a returned disease module or a subset of them or;<br>"
				+"b) selected genes/proteins which you are interested to rank drugs targeting them."
				+ "<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, "Gyöngyi et al.", "https://www.vldb.org/conf/2004/RS15P3.PDF", Constant.TUTORIAL_LINK+"availableFunctions.html#rank-drugs-with-trustrank");
		putValue(SHORT_DESCRIPTION,"TrustRank ranks nodes in a network based on how well they are connected to a (trusted) set of seed nodes. It is a modification of Google’s PageRank algorithm, where “trust” is iteratively propagated from seed nodes to adjacent nodes using the network topology.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new TrustRankDrugTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
		} else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new TrustRankDrugTask(app)));
		}
		
	}

}
