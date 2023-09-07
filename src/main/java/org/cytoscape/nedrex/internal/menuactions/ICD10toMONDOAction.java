package org.cytoscape.nedrex.internal.menuactions;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.nedrex.internal.InfoBox;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.ICD10toMONDOTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;

/**
 * NeDRex App
 *
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class ICD10toMONDOAction extends AbstractCyAction {
    private RepoApplication app;
    private Logger logger = LoggerFactory.getLogger(getClass());
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


    public ICD10toMONDOAction(RepoApplication app) {
        super("ICD10 to MONDO");
        setPreferredMenu("Apps.NeDRex.Comorbidity Functions");
        setMenuGravity(10.0f);
        this.app = app;
        String message = "<html><body>Convert ICD10 diseases to MONDO diseases to for further analysis on the general NeDRex knowledge graph.<br><br>Before continuing with this function, make sure you have:<br><b>selected a list of diseases from the comorbiditome</b></body></html>";

        this.infoBox = new InfoBox(app, message, null, null, null, true);
        putValue(SHORT_DESCRIPTION, "Convert ICD10 diseases to MONDO diseases to for further analysis on the general NeDRex knowledge graph."
        );
    }

    @Override
    public boolean insertSeparatorBefore() {
        return true;
    }

    @Override
    public boolean insertSeparatorAfter() {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!infoBox.isHide()) {
            int returnedValue = infoBox.showMessage();
            if (returnedValue == 0) {
                //Continue
                DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
                taskmanager.execute(new TaskIterator(new ICD10toMONDOTask(app)));
                if (infoBox.getCheckbox().isSelected()) {
                    //Don't show this again
                    infoBox.setHide(true);
                }
            }
        } else {
            DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
            taskmanager.execute(new TaskIterator(new ICD10toMONDOTask(app)));
        }

    }

}
