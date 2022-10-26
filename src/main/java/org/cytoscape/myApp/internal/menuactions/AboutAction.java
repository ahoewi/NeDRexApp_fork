package org.cytoscape.myApp.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.myApp.internal.AboutPanel;
import org.cytoscape.myApp.internal.RepoApplication;


import java.awt.event.ActionEvent;

/**
 * NeDRex App
 * @author Judith Bernett, Sepideh Sadegh
 */
public class AboutAction extends AbstractCyAction {
    RepoApplication app;
    AboutPanel aboutPanel;

    public AboutAction(RepoApplication app, AboutPanel aboutPanel){
        super("About");
        setPreferredMenu("Apps.NeDRex");
        setMenuGravity(100.0f);
        this.app = app;
        this.aboutPanel = aboutPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(this.aboutPanel != null){
            this.aboutPanel.activate();
        }
    }

}
