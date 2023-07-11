package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.DiamondTask;
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
public class DiamondAction extends AbstractCyAction{
	
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

	public DiamondAction (RepoApplication app) {
		super("Run DIAMOnD");
		setPreferredMenu("Apps.NeDRex.Disease Module Identification");
		setMenuGravity(32.0f);
		this.app = app;
		String message = "<html><body>DIAMOnD (DIseAse MOdule Detection) algorithm identifies the disease module around <br>" +
				"a set of known disease genes/proteins (seeds) based on the principle that the connectivity significance<br>" +
				"is highly distinctive for known disease proteins. <br><br>" +
				"Before continuing with this function, make sure you have:<br>" +
				"a) selected a set of genes or proteins (seeds) in your network or;<br> " +
				"b) a custom seed list ready to use as input for the function.<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, "Ghiassian et al. (2015)", "https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1004120",
				this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#run-diamond");
		putValue(SHORT_DESCRIPTION, "DIAMOnD (DIseAse MOdule Detection) algorithm identifies the disease module around a set of known disease genes/proteins (seeds) based on the principle that the connectivity significance is highly distinctive for known disease proteins. Ghiassian et al. (2015)");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new DiamondTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}

		}else{
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new DiamondTask(app)));
		}
	}

}
