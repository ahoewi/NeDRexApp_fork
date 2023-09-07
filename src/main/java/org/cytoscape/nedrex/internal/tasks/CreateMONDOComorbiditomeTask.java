package org.cytoscape.nedrex.internal.tasks;

import org.cytoscape.model.*;
import org.cytoscape.nedrex.internal.*;
import org.cytoscape.nedrex.internal.utils.FilterType;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * NeDRex App
 *
 * @author Andreas Maier
 */
public class CreateMONDOComorbiditomeTask extends AbstractTask {

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

    public CreateMONDOComorbiditomeTask(RepoApplication app, CyNetwork sourceNetwork, Map<String, HashSet<String>> id_map, Map<String, LinkedList<String>> edges_to_convert, boolean self_loops, CyNetwork targetNetwork, String newNetName) {
        this.app = app;
        this.sourceNetwork = sourceNetwork;
        this.id_map = id_map;
        this.edges_to_convert = edges_to_convert;
        this.newNetName = newNetName;
        this.self_loops = self_loops;
        this.targetNetwork = targetNetwork;
        System.out.println("Self loops: " + self_loops);
    }


    public CyNode createAndAddNode(CyNetwork currentNetwork, String node_id, String original_id, HashMap<String, CyNode> node_map){
        CyNode node;
        if (node_map.get(node_id) == null) {
            if (targetNetwork != null) {

                CyNode copy_node = ModelUtil.getNodeWithName(app, targetNetwork, node_id);
                node = currentNetwork.addNode();
                currentNetwork.getRow(node).set(CyNetwork.NAME, node_id);
                currentNetwork.getRow(node).set(nodeDisplayNameCol, node_id);
                currentNetwork.getRow(node).set(nodeTypeCol, NodeType.Disease.toString());
                currentNetwork.getRow(node).set("icd10", original_id);
                currentNetwork.getRow(node).set("mondo", node_id);
                currentNetwork.getRow(node).set("type", NodeType.Disease.toString());
                if (copy_node != null) {
                    for(String col: targetNetwork.getDefaultNodeTable().getColumns().stream().map(CyColumn::getName).toArray(String[]::new)){
                        currentNetwork.getRow(node).set(col, targetNetwork.getRow(copy_node).get(col, targetNetwork.getDefaultNodeTable().getColumn(col).getType()));
                    }
                    String icd10 = targetNetwork.getRow(copy_node).get("icd10", String.class);
                    HashSet<String> icd10s = new HashSet<>(Arrays.asList(icd10.split(",")));
                    icd10s.add(original_id);
                    currentNetwork.getRow(node).set("icd10", String.join(",", icd10s));
                }
            } else {
                node = currentNetwork.addNode();
                currentNetwork.getRow(node).set(CyNetwork.NAME, node_id);
                currentNetwork.getRow(node).set(nodeDisplayNameCol, node_id);
                currentNetwork.getRow(node).set(nodeTypeCol, NodeType.Disease.toString());
                currentNetwork.getRow(node).set("icd10", original_id);
                currentNetwork.getRow(node).set("mondo", node_id);
            }
            currentNetwork.getRow(node).set("primaryDomainId", node_id);
            node_map.put(node_id, node);
        } else {
            node = node_map.get(node_id);
        }

      return node;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        CyNetworkManager netMgr = app.getActivator().getService(CyNetworkManager.class);
        CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
        CyNetworkFactory cnf = app.getActivator().getService(CyNetworkFactory.class);

        CyNetworkViewFactory cnvf = app.getActivator().getService(CyNetworkViewFactory.class);
        CyNetworkViewManager nvm = app.getActivator().getService(CyNetworkViewManager.class);



        CyNetwork newProj = cnf.createNetwork();
        newProj.getRow(newProj).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(newNetName));

        newProj.getDefaultNodeTable().createColumn(nodeTypeCol, String.class, false);
        newProj.getDefaultNodeTable().createColumn(nodeDisplayNameCol, String.class, false);
        newProj.getDefaultNodeTable().createColumn("primaryDomainId", String.class, false);

        HashMap<String, CyNode> node_map = new HashMap<>();
        TreeMap<String, TreeSet<String>> existing_edges = new TreeMap<>();

        newProj.getDefaultNodeTable().createColumn("icd10", String.class, false);
        newProj.getDefaultNodeTable().createColumn("mondo", String.class, false);
        newProj.getDefaultEdgeTable().createColumn(edgeTypeCol, String.class, false);
        newProj.getDefaultEdgeTable().createColumn("sourceDomainId", String.class, false);
        newProj.getDefaultEdgeTable().createColumn("targetDomainId", String.class, false);

        if (this.targetNetwork != null) {
            this.targetNetwork.getDefaultNodeTable().getColumns().forEach(col -> {
                if (newProj.getDefaultNodeTable().getColumn(col.getName()) == null) {
                    newProj.getDefaultNodeTable().createColumn(col.getName(), col.getType(), col.isImmutable());
                }
            });
        }

        this.edges_to_convert.forEach((source, targets) -> {

            for (String mondo_source : id_map.get(source)) {

                CyNode source_node = this.createAndAddNode(newProj, mondo_source, source, node_map);

                for (String target : targets) {
                    for (String mondo_target : id_map.get(target)) {
                        if (!self_loops && mondo_source.equals(mondo_target))
                            continue;
                        if (existing_edges.get(mondo_source) != null && existing_edges.get(mondo_source).contains(mondo_target))
                            continue;
                        else {
                            existing_edges.computeIfAbsent(mondo_source, k -> new TreeSet<>());
                            existing_edges.get(mondo_source).add(mondo_target);
                            existing_edges.computeIfAbsent(mondo_target, k -> new TreeSet<>());
                            existing_edges.get(mondo_target).add(mondo_source);
                        }
                        CyNode target_node = this.createAndAddNode(newProj, mondo_target, target, node_map);
                        CyEdge new_edge = newProj.addEdge(source_node, target_node, false);
                        newProj.getDefaultEdgeTable().getRow(new_edge.getSUID()).set(edgeTypeCol, InteractionType.disease_comorbid_disease.toString());
                        newProj.getDefaultEdgeTable().getRow(new_edge.getSUID()).set("sourceDomainId", source);
                        newProj.getDefaultEdgeTable().getRow(new_edge.getSUID()).set("targetDomainId", target);
                    }
                }
            }
        });

        netMgr.addNetwork(newProj);

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
//            dialogTaskManager.execute(new TaskIterator(new AbstractTask() {
//                @Override
//                public void run(TaskMonitor taskMonitor) {
//                    taskMonitor.setTitle("Error");
//                    taskMonitor.setStatusMessage(e.getMessage());
//                    // Optionally, show the complete error trace in your logger.
//                    logger.error("An error occurred", e);
//                }
//            }));
            System.out.println("No visual style found");
        }
    }

}
