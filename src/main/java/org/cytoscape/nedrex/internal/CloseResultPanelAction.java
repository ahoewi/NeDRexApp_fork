package org.cytoscape.nedrex.internal;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CloseResultPanelAction extends AbstractCyAction{
	
	private RepoResultPanel resultPanel;
	
	public CloseResultPanelAction(RepoResultPanel resultPanel) {
		super("Close");
		this.resultPanel = resultPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (this.resultPanel != null)
			this.resultPanel.deactivate();
		
	}

}
