package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.RepoResultPanel;
import org.cytoscape.myApp.internal.tasks.ModuleValidationTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ModuleValidationAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private RepoResultPanel resultPanel;
	private InfoBox infoBox;
	
	public ModuleValidationAction(RepoApplication app, RepoResultPanel resultPanel) {
		super("Disease Module");
		setPreferredMenu("Apps.NeDRex.Validation");
		setMenuGravity(40.0f);
		this.app = app;
		this.resultPanel = resultPanel;
		String message = "<html><body>This disease module validation method is one of the three validation methods that can be used<br>" + 
				"to evaluate the statistical significance of the results returned by NeDRex repurposing pipeline.<br>"+
				"This method takes into account the role of disease module identification step in the NeDRex drug<br>" +
				"repurposing pipeline. We define precision as the number of reference drugs targeting the module<br>" + 
				"divided by the overall number of drugs targeting the module. The significance of the result<br>" +
				"disease module is estimated by calculating an empirical P-value by counting the number of<br>" +
				"mock modules with higher precision values than the disease module computed by NeDRex. In a<br>"+
				"simplified version, we compare intersection sizes with the reference drugs instead of<br>" +
				" precision values. <br><br>"+
				"Before continuing with this function, make sure you have:<br>" +
				"a) run one of the disease module identification functions and the returned subnetwork is open;<br> " +
				"b) a list of drugs indicated for the treatment of the disease to be used as reference true drugs.<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#disease-module");
		putValue(SHORT_DESCRIPTION, "A statistical method to jointly validate the disease module returned by NeDRex repurposing approach.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new ModuleValidationTask(app, resultPanel)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}

		}else{
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new ModuleValidationTask(app, resultPanel)));
		}		
	}
	

}
