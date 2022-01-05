package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.RepoResultPanel;
import org.cytoscape.myApp.internal.tasks.JointValidationTask;
import org.cytoscape.myApp.internal.tasks.MechBasedValidTask;
import org.cytoscape.myApp.internal.utils.WarningMessages;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class JointValidationAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private RepoResultPanel resultPanel;
	private InfoBox infoBox;
	
	public JointValidationAction(RepoApplication app, RepoResultPanel resultPanel) {
		super("Joint Module & Drugs");
		setPreferredMenu("Apps.NeDRex.Validation");
		setMenuGravity(40.0f);
		this.app = app;
		this.resultPanel = resultPanel;
		String message = "<html><body>This joint validation method is one of the three validation methods that can be used to<br>" + 
				"evaluate the statistical significance of the results returned by NeDRex repurposing pipeline.<br>"+
				"This method is based on empirical P-value and takes into account both steps of drug<br>" +
				"repurposing pipeline, i.e. disease module identification and drug ranking, as a whole in the<br>" +
				"final validation of results.<br><br>" +
				"Before continuing with this function, make sure you have:<br>" +
				"a) run one of the disease module identification and one of the drug prioritization functions,<br> " +
				"   in a sequence, and the returned subnetwork (including the module and drugs) is open;<br>" +
				"b) a list of drugs indicated for the treatment of the disease to be used as reference true drugs.<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#joint-module-drugs", true);
		putValue(SHORT_DESCRIPTION, "A statistical method to jointly validate the disease module and the drug candidates returned by NeDRex repurposing approach.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0 && infoBox.getLicensbox().isSelected()) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new JointValidationTask(app, resultPanel)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}
			else if (returnedValue == 0 && !infoBox.getLicensbox().isSelected()) {
				WarningMessages.showAgreementWarning();
			}

		}else{
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new JointValidationTask(app, resultPanel)));
		}
		
	}

}
