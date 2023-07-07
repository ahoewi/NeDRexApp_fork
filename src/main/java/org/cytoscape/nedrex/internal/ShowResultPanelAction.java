package org.cytoscape.nedrex.internal;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ShowResultPanelAction extends AbstractCyAction{
	
	private RepoResultPanel resultPanel;
	
	public ShowResultPanelAction (RepoResultPanel resultPanel) {
		super("Show NeDRex Results Panel");
		setPreferredMenu("Apps.NeDRex");
		setMenuGravity(30.0f);
		this.resultPanel = resultPanel;		
		putValue(SHORT_DESCRIPTION, "Activates the result panel for the functions, such as BiCoN and Validation methods, where results are not integrated into the node/edge table of the returned networks");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this.resultPanel != null)
			this.resultPanel.activate();
		
	}

}
