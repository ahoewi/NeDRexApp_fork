package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.TrustRankDrugTask;
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
public class TrustRankDrugAction extends AbstractCyAction{
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
		this.infoBox = new InfoBox(app, message, "Gyöngyi et al.", "https://www.vldb.org/conf/2004/RS15P3.PDF", this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#rank-drugs-with-trustrank", true);
		putValue(SHORT_DESCRIPTION,"TrustRank ranks nodes in a network based on how well they are connected to a (trusted) set of seed nodes. It is a modification of Google’s PageRank algorithm, where “trust” is iteratively propagated from seed nodes to adjacent nodes using the network topology.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0 && infoBox.getLicensbox().isSelected()) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new TrustRankDrugTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
			else if (returnedValue == 0 && !infoBox.getLicensbox().isSelected()) {
				WarningMessages.showAgreementWarning();
			}
		} else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new TrustRankDrugTask(app)));
		}
		
	}

}
