package org.cytoscape.nedrex.internal.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.tasks.ImportComorbidTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ImportComorbidityAction extends AbstractAction{
	private RepoApplication app;
    private ComorbOptionPanel optionsPanel;

    public ImportComorbidityAction(RepoApplication app, ComorbOptionPanel optionsPanel) {
        super("Import");
        this.app = app;
        this.optionsPanel = optionsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
        ((Window)optionsPanel.getRootPane().getParent()).dispose();
        taskmanager.execute(new TaskIterator(new ImportComorbidTask(app, optionsPanel)));

    }

}
