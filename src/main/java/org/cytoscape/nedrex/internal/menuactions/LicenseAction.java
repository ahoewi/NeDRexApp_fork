package org.cytoscape.nedrex.internal.menuactions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.LicensePanel;
import org.cytoscape.nedrex.internal.RepoApplication;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */

public class LicenseAction extends AbstractCyAction{
	RepoApplication app;
    LicensePanel licensePanel;

    public LicenseAction(RepoApplication app, LicensePanel licensePanel){
        super("Terms of Use");
        setPreferredMenu("Apps.NeDRex");
        setMenuGravity(200.0f);
        this.app = app;
        this.licensePanel = licensePanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(this.licensePanel != null){
            try {
				this.licensePanel.activate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

}
