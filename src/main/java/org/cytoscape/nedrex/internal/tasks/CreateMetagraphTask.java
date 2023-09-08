package org.cytoscape.nedrex.internal.tasks;

import org.cytoscape.model.*;
import org.cytoscape.nedrex.internal.*;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * NeDRex App
 *
 * @author Andreas Maier
 */
public class CreateMetagraphTask extends AbstractTask {

    private RepoApplication app;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private CyNetwork sourceNetwork;
    private Map<String, HashSet<String>> id_map;
    //private Set<List<CyNode>> edges_to_add;
    private Map<String, LinkedList<String>> edges_to_convert;

    private boolean self_loops;

    private String newNetName;

    private CyNetwork targetNetwork;


    String nodeTypeCol = "type";
    String edgeTypeCol = "type";
    String nodeDisplayNameCol = "displayName";

    public CreateMetagraphTask(RepoApplication app, String newNetName) {
        this.app = app;
        this.newNetName = newNetName;
    }



    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        CyNetworkManager netMgr = app.getActivator().getService(CyNetworkManager.class);
        CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
        CyNetworkFactory cnf = app.getActivator().getService(CyNetworkFactory.class);

        CyNetworkViewFactory cnvf = app.getActivator().getService(CyNetworkViewFactory.class);
        CyNetworkViewManager nvm = app.getActivator().getService(CyNetworkViewManager.class);



        CyNetwork network = cnf.createNetwork();

        network.getRow(network).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(newNetName));

        network.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
        network.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);

        network.getDefaultEdgeTable().createColumn(nodeTypeCol, String.class, false);
        network.getDefaultEdgeTable().createColumn(nodeDisplayNameCol, String.class, false);

        CyNode disorder = network.addNode();
        network.getRow(disorder).set(CyNetwork.NAME, "disorder");
        network.getRow(disorder).set(nodeTypeCol, NodeType.Disease.toString());
        network.getRow(disorder).set(nodeDisplayNameCol, "Disorder");

        CyNode drug = network.addNode();
        network.getRow(drug).set(CyNetwork.NAME, "dug");
        network.getRow(drug).set(nodeTypeCol, NodeType.Drug.toString());
        network.getRow(drug).set(nodeDisplayNameCol, "Drug");

        CyNode gene = network.addNode();
        network.getRow(gene).set(CyNetwork.NAME, "gene");
        network.getRow(gene).set(nodeTypeCol, NodeType.Gene.toString());
        network.getRow(gene).set(nodeDisplayNameCol, "Gene");

        CyNode protein = network.addNode();
        network.getRow(protein).set(CyNetwork.NAME, "protein");
        network.getRow(protein).set(nodeTypeCol, NodeType.Protein.toString());
        network.getRow(protein).set(nodeDisplayNameCol, "Protein");


        CyEdge drug_has_indication = network.addEdge(drug, disorder, true);
        network.getRow(drug_has_indication).set(CyNetwork.NAME, "(drug) - (disorder)");
        network.getRow(drug_has_indication).set(edgeTypeCol, InteractionType.drug_disease.toString());
        network.getRow(drug_has_indication).set("displayName", "drug_has_indication");

        CyEdge drug_has_target = network.addEdge(drug, protein, true);
        network.getRow(drug_has_target).set(CyNetwork.NAME, "(drug) - (protein)");
        network.getRow(drug_has_target).set(edgeTypeCol, InteractionType.drug_protein.toString());
        network.getRow(drug_has_target).set("displayName", "drug_has_target");

        CyEdge gene_associated_with = network.addEdge(gene, disorder, true);
        network.getRow(gene_associated_with).set(CyNetwork.NAME, "(gene) - (disorder)");
        network.getRow(gene_associated_with).set(edgeTypeCol, InteractionType.gene_disease.toString());
        network.getRow(gene_associated_with).set("displayName", "gene_associated_with_disorder");

        CyEdge protein_encoded_by = network.addEdge(protein, gene, true);
        network.getRow(protein_encoded_by).set(CyNetwork.NAME, "(protein) - (gene)");
        network.getRow(protein_encoded_by).set(edgeTypeCol, InteractionType.gene_protein.toString());
        network.getRow(protein_encoded_by).set("displayName", "protein_encoded_by");

        CyEdge protein_interacts_with_protein = network.addEdge(protein, protein, false);
        network.getRow(protein_interacts_with_protein).set(CyNetwork.NAME, "(protein) - (protein)");
        network.getRow(protein_interacts_with_protein).set(edgeTypeCol, InteractionType.protein_protein.toString());
        network.getRow(protein_interacts_with_protein).set("displayName", "protein_interacts_with_protein");

        CyEdge disorder_comorbid_with_disorder = network.addEdge(disorder, disorder, false);
        network.getRow(disorder_comorbid_with_disorder).set(CyNetwork.NAME, "(disorder) - (disorder)");
        network.getRow(disorder_comorbid_with_disorder).set(edgeTypeCol, InteractionType.disease_comorbid_disease.toString());
        network.getRow(disorder_comorbid_with_disorder).set("displayName", "disorder_comorbid_with_disorder");


        netMgr.addNetwork(network);

        Map<String, Object> args = new HashMap<>();
        args.put("network", "current");
        CommandExecuter cmdex = new CommandExecuter(app);
        cmdex.executeCommand("view", "create", args, null);

        VisualMappingManager visualMappingManager = app.getActivator().getService(VisualMappingManager.class);
        Optional<VisualStyle> visualStyle = visualMappingManager.getAllVisualStyles()
                .stream()
                .filter(style -> style.getTitle().equals("NeDRex"))
                .findFirst();
        try {
            Map<String, Object> argsAS = new HashMap<>();
            argsAS.put("styles", visualStyle.get());
            CommandExecuter cmdexAS = new CommandExecuter(app);
            cmdexAS.executeCommand("vizmap", "apply", argsAS, null);
        } catch (Exception e) {
            System.out.println("No visual style found");
        }
    }

}
