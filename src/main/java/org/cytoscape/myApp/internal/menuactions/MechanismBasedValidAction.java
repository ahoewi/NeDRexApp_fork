package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.RepoResultPanel;
import org.cytoscape.myApp.internal.tasks.MechBasedValidTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class MechanismBasedValidAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private RepoResultPanel resultPanel;
	private InfoBox infoBox;
	
	public MechanismBasedValidAction(RepoApplication app, RepoResultPanel resultPanel) {
		super("Mechanism-centric");
		setPreferredMenu("Apps.NeDRex.Validation");
		setMenuGravity(40.0f);
		this.app = app;
		this.resultPanel = resultPanel;
		String message = "<html><body>Mechanism-centric validation method is a validation method that can be used to evaluate <br>" + 
				"the statistical significance of the list of drugs (as repurposing candidates) and disease<br>" +
				"mechanisms returned by NeDRex. This method is based on empirical P-values and takes into<br>" +
				"account the role of disease module identification step in the NeDRex drug repurposing pipeline.<br><br>" +
				"Before continuing with this function, make sure you have:<br>" +
				"a) already run one of the drug prioritization functions and the returned subnetwork is open;<br> " +
				"b) a list of drugs indicated for the treatment of the disease to be used as reference true drugs.<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#mechanism-centric-method");
		putValue(SHORT_DESCRIPTION, "A statistical mechanism-centric method to validate the drug repurposing results returned by NeDRex.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new MechBasedValidTask(app, resultPanel)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}

		}else{
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new MechBasedValidTask(app, resultPanel)));
		}
		
	}

}
