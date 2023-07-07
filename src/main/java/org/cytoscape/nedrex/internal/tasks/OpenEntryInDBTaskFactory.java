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

public class OpenEntryInDBTaskFactory extends AbstractNodeViewTaskFactory {

    public OpenEntryInDBTaskFactory(){
        super();
    }

    @Override
    public TaskIterator createTaskIterator(View<CyNode> view, CyNetworkView cyNetworkView) {
        String dbID = cyNetworkView.getModel().getRow(view.getModel()).get("name", String.class);
        String[] dbArray = dbID.split("\\.");
        String uriDatabase = "";
        String database = "";
        switch (dbArray[0]){
            case "drugbank":
                uriDatabase = "https://go.drugbank.com/drugs/";
                database = "DrugBank";
                break;
            case "uniprot":
                uriDatabase = "https://www.uniprot.org/uniprot/";
                database = "Uniprot";
                break;
            case "mondo":
                uriDatabase = "https://monarchinitiative.org/disease/MONDO:";
                database = "Monarch Initiative Explorer";
                break;
            case "entrez":
                uriDatabase = "https://www.ncbi.nlm.nih.gov/gene/";
                database = "NCBI";
                break;
            case "reactome":
                uriDatabase = "https://reactome.org/content/detail/";
                database = "Reactome";
                break;
        }

        String id = dbArray[1];
        if(id == null){
            throw new NullPointerException("Could not get ID");
        }
        //String uriDrugBank = "https://go.drugbank.com/drugs/";
        return new TaskIterator(new OpenEntryInDBTask(uriDatabase, id, database));
    }
}
