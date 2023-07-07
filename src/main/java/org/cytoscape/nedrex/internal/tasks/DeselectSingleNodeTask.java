package org.cytoscape.nedrex.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
/**
 * NeDRex App
 * @author Judith Bernett
 */
public class DeselectSingleNodeTask extends AbstractTask {

    CyNetworkView cyNetworkView;
    Long suid;

    public DeselectSingleNodeTask(CyNetworkView cyNetworkView, Long suid){
        this.cyNetworkView = cyNetworkView;
        this.suid = suid;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        cyNetworkView.getModel().getDefaultNodeTable().getRow(suid).set(CyNetwork.SELECTED, false);
    }
}
