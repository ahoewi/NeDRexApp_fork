package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.tasks.MuSTapiTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class MuSTapiAction extends AbstractCyAction{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	InfoBox infoBox;
	
	public MuSTapiAction(RepoApplication app) {
		super("Run MuST");
		setPreferredMenu("Apps.NeDRex.Disease Module Identification");
		setMenuGravity(33.0f);
		this.app = app;
		String message = "<html><body>" +
				"By selecting genes/proteins associated with a disease under study (seeds), MuST (Multi-Steiner trees) <br>" +
				"extracts a connected subnetwork involved in the disease pathways based on the aggregation of several <br>" +
				"non-unique approximates of Steiner trees.<br><br>" +
				"Before continuing with this function, make sure you have: <br>" +
				"a) selected a set of genes or proteins (seeds) in your network or;<br>" +
				"b) a custom seed list ready to give to the function.<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, "Sadegh et al.", "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC7360763/", Constant.TUTORIAL_LINK+"availableFunctions.html#run-must");
		putValue(SHORT_DESCRIPTION, "By selecting genes/proteins associated with a disease under study (seeds), MuST (Multi-Steiner trees) extracts a connected subnetwork involved in the disease pathways based on the aggregation of several non-unique approximates of Steiner trees. Sadegh et al. (2020)");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new MuSTapiTask(app)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}

		}else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new MuSTapiTask(app)));
		}
		
	}

}
