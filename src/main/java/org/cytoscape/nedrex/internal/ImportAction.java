package org.cytoscape.nedrex.internal;

import org.cytoscape.nedrex.internal.tasks.ImportTask;
import org.cytoscape.nedrex.internal.ui.SearchOptionPanel;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * NeDRex App
 * @author Sepideh Sadegh, Judith Bernett
 */
public class ImportAction extends AbstractAction {
    private RepoApplication app;
    private SearchOptionPanel optionsPanel;

    public ImportAction(RepoApplication app, SearchOptionPanel optionsPanel) {
        super("Import");
        this.app = app;
        this.optionsPanel = optionsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
        ((Window)optionsPanel.getRootPane().getParent()).dispose();
        taskmanager.execute(new TaskIterator(new ImportTask(app, optionsPanel)));

    }
}
