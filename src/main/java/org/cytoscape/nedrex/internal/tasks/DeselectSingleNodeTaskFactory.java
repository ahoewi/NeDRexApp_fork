package org.cytoscape.nedrex.internal.tasks;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
/**
 * NeDRex App
 * @author Judith Bernett
 */
public class DeselectSingleNodeTaskFactory extends AbstractNodeViewTaskFactory {

    public DeselectSingleNodeTaskFactory(){
        super();
    }

    @Override
    public TaskIterator createTaskIterator(View<CyNode> view, CyNetworkView cyNetworkView) {
        Long suid = cyNetworkView.getModel().getRow(view.getModel()).get("SUID", Long.class);
        return new TaskIterator(new DeselectSingleNodeTask(cyNetworkView, suid));
    }
}
