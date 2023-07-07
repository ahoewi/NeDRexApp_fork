package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.QuickSelectPanel2;

import java.awt.event.ActionEvent;

/**
 * NeDRex App
 * @author Judith Bernett
 */

public class QuickSelectAction2 extends AbstractCyAction{

    private QuickSelectPanel2 disordersPanel;

    public QuickSelectAction2(QuickSelectPanel2 disordersPanel){
    	super("Quick Select");
        setPreferredMenu("Apps.NeDRex");
        setMenuGravity(1.0f);
        this.disordersPanel = disordersPanel;
    }
    
    @Override
    public boolean insertSeparatorBefore() {
		return true;
	}

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(this.disordersPanel != null){
            this.disordersPanel.activate();
        }
    }
}
