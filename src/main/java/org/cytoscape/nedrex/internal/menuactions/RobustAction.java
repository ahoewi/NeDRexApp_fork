package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.DiamondTask;
import org.cytoscape.nedrex.internal.tasks.RobustTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;

/**
 * NeDRex App
 * @author Andreas Maier
 */
public class RobustAction extends AbstractCyAction{

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

	public RobustAction(RepoApplication app) {
		super("Run ROBUST");
		setPreferredMenu("Apps.NeDRex.Disease Module Identification");
		setMenuGravity(32.0f);
		this.app = app;
		String message = "<html><body>ROBUST (robust disease module mining via enumeration prize collecting Steiner Trees) identifies the disease module by selecting the most robust genes/proteins connecting all the seeds. <br>" +
				"This method is recommended in cases, where seed sets might already be big or in the scope of a condition that is not well characterized on a molecular bases. <br><br>" +
				"Before continuing with this function, make sure you have:<br>" +
				"a) selected a set of genes or proteins (seeds) in your network or;<br> " +
				"b) a custom seed list ready to use as input for the function.<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, "Bernett et al. (2022)", "https://academic.oup.com/bioinformatics/article/38/6/1600/6497106?login=true",
				null);
		putValue(SHORT_DESCRIPTION, "ROBUST (robust disease module mining via enumeration prize collecting Steiner Trees) identifies the disease module by selecting the most robust genes/proteins connecting all the seeds (Bernett et al. 2022).");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new RobustTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}

		}else{
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new RobustTask(app)));
		}
	}

}
