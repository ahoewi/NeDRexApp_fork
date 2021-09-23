package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.DiamondTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class DiamondAction extends AbstractCyAction{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private InfoBox infoBox;


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
				Constant.TUTORIAL_LINK+"availableFunctions.html#run-diamond");
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
