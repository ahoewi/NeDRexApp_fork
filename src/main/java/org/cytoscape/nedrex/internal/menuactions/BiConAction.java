package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.RepoResultPanel;
import org.cytoscape.nedrex.internal.tasks.BiConTask;
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
public class BiConAction extends AbstractCyAction{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private RepoResultPanel resultPanel;
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


	public BiConAction(RepoApplication app, RepoResultPanel resultPanel) {
		super("Run BiCoN");
		setPreferredMenu("Apps.NeDRex.Disease Module Identification");
		setMenuGravity(31.0f);
		this.app = app;
		this.resultPanel = resultPanel;
		String message = "<html><body>" +
				"BiCoN (Biclustering Constrained by Networks) is an unsupervised approach that performs <br>" +
				"simultaneous patient and gene clustering on patients' gene expression profiles such that <br>" +
				"the genes that provide the best possible clustering are also connected in the PPI network. <br><br>" +
				"Before continuing with this function, make sure you have a gene expression / methylation file (.csv) in this format: <br><br> " +
				"<table style=\"undefined;table-layout: fixed; width: 400px\"><colgroup><col style=\"width: 100px\"><col style=\"width: 100px\"><col style=\"width: 100px\"><col style=\"width: 100px\"></colgroup><thead><tr><th>Gene ID</th><th>Patient 1</th><th>Patient 2</th><th>Patient 3</th></tr></thead><tbody><tr style=\"text-align:center\"><td>6311</td><td>7.3487</td><td>6.9464</td><td>7.2675</td></tr><tr style=\"text-align:center\"><td>133584</td><td>6.5562</td><td>5.7931</td><td>6.3360</td></tr><tr style=\"text-align:center\"><td>283165</td><td>3.8878</td><td>3.9588</td><td>4.0173</td></tr></tbody></table><br>"+
				"<br></body></html>";
		this.infoBox = new InfoBox(app, message, "Lazareva et al. (2020)", "https://academic.oup.com/bioinformatics/advance-article-abstract/doi/10.1093/bioinformatics/btaa1076/6050718?redirectedFrom=fulltext",
				this.nedrexService.TUTORIAL_LINK+"availableFunctions.html#run-bicon");

		putValue(SHORT_DESCRIPTION, "BiCoN (Biclustering Constrained by Networks) is an unsupervised approach that performs simultaneous patient and gene clustering on patients' gene expression profiles such that the genes that provide the best possible clustering are also connected in the PPI network. Lazareva et al. (2021)");		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!infoBox.isHide()) {
			int returnedValue = infoBox.showMessage();
			if (returnedValue == 0) {
				//Continue
				DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
				taskmanager.execute(new TaskIterator(new BiConTask(app, resultPanel)));
				if (infoBox.getCheckbox().isSelected()) {
					//Don't show this again
					infoBox.setHide(true);
				}
			}

		}else {
			DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
			taskmanager.execute(new TaskIterator(new BiConTask(app, resultPanel)));
		}
		
	}

}
