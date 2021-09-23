package org.cytoscape.myApp.internal.menuactions;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.InfoBox;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.RepoResultPanel;
import org.cytoscape.myApp.internal.tasks.DrugBasedValidTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class DrugBasedValidAction extends AbstractCyAction{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private RepoResultPanel resultPanel;
	private InfoBox infoBox;
	
	public DrugBasedValidAction(RepoApplication app, RepoResultPanel resultPanel) {
		super("Drug-centric");
		setPreferredMenu("Apps.NeDRex.Validation");
		setMenuGravity(40.0f);
		this.app = app;
		this.resultPanel = resultPanel;
		String message = "<html><body>Drug-centric validation method is one of the two validation methods that can be used to<br>" + 
				"evaluate the statistical significance of the top-ranked list of drugs returned by NeDRex<br>" +
				"drug repurposing pipeline. The significance of the result drugs is estimated by calculating<br>" +
				"an empirical P-value by counting the number of randomly generated drug lists having larger<br>" +
				" overlap with the reference list of drugs than that of the NeDRex result list. In a variation<br>" +
				"of this method the ranks of the reference drugs in the output are also considered.<br><br>" + 
				"Before continuing with this function, make sure you have:<br>" +
				"a) already run one of the drug prioritization functions and the returned subnetwork is open;<br> " +
				"b) a list of drugs indicated for the treatment of the disease to be used as reference true drugs.<br><br></body></html>";
		this.infoBox = new InfoBox(app, message, Constant.TUTORIAL_LINK+"availableFunctions.html#drug-centric-method");
		putValue(SHORT_DESCRIPTION, "A statistical drug-centric method to validate the drug repurposing results returned by NeDRex.");
		}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new DrugBasedValidTask(app, resultPanel)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}

		}else{
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new DrugBasedValidTask(app, resultPanel)));
		}
		
	}

}
