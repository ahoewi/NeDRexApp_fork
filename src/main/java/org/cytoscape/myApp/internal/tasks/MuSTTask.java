package org.cytoscape.myApp.internal.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.myApp.internal.CommandExecuter;
import org.cytoscape.myApp.internal.Link;
import org.cytoscape.myApp.internal.ModelUtil;
import org.cytoscape.myApp.internal.NodeType;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.UndirectedNetwork;
import org.cytoscape.myApp.internal.Vertex;
import org.cytoscape.myApp.internal.algorithms.SteinerTree;
import org.cytoscape.myApp.internal.utils.FilterType;
import org.cytoscape.myApp.internal.utils.GraphUtils;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedInteger;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class MuSTTask extends AbstractTask{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Set Parameters for Multi-Steiner Trees Algorithm (current network)";}
	
	@Tunable(description="Return multiple Steiner trees", groups="Algorithm settings",
			gravity = 1.0)
    public Boolean multiple = false;
	
	@Tunable(description="The number of Steiner trees", groups="Algorithm settings",
			params="slider=true",
			dependsOn="multiple=true",
			tooltip="The number of Steiner trees to be returned",
			gravity = 2.0)
    public BoundedInteger treeNumber = new BoundedInteger(2, 10, 50, false, false);
	
	@Tunable(description="Max number of iterations", groups="Algorithm settings",
			params="slider=true",
			dependsOn="multiple=true",
			tooltip="The maximum number of iterations that the algorithm runs to find dissimilar Steiner trees of the selected number",
			gravity = 2.5)
    public BoundedInteger iterNumber = new BoundedInteger(0, 5, 20, false, false);
	
	@Tunable(description="Penalize hub nodes", groups="Algorithm settings",
			tooltip="Penalize high degree nodes by incorporating the degree of neighboring nodes as edge weights",
			gravity = 3.0)
    public Boolean penalized = false;
	
	@Tunable(description="Hub penalty", 
	         groups="Algorithm settings", 
	         params="slider=true",
	         dependsOn="penalized=true",
	         tooltip="Penalty parameter for hubs. Sets edge weight to (1 - hub_penalty) * AveDeg(G) + (hub_penalty / 2) * (Deg(source) + Deg(target))",
	         gravity = 5.0)
	public BoundedDouble hubPenalty = new BoundedDouble(0.0, 0.5, 1.0, false, false);
	
	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name based on the selected algorithm parameters will be assigned",
			gravity = 5.0)
    public Boolean set_net_name = false;
	
	@Tunable(description="Name of the result network", 
	         groups="Result network", 
	         dependsOn="set_net_name=true",
	         tooltip="Enter the name you would like to have assigned to the result network",
	         gravity = 5.0)
	public String new_net_name = new String();
	
	public MuSTTask(RepoApplication app) {
		this.app = app;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);	
		String newNetName = new String();
		if (!set_net_name) {
					
			String netName = "MuST";
			if (multiple) {
				netName = netName + String.format("_trees%d", treeNumber.getValue());
			}
			if (penalized) {
				netName = netName + String.format("_HP%.2f", hubPenalty.getValue());
			}
			newNetName = namingUtil.getSuggestedNetworkTitle(netName);
		}
		else if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
		UndirectedNetwork graph = new UndirectedNetwork();
		List <Vertex> terminalNodes = new ArrayList<Vertex>();
		List<CyNode> terminalCyNodes = new ArrayList<CyNode>();
		terminalCyNodes = CyTableUtil.getNodesInState(network,CyNetwork.SELECTED,true);
		for (CyNode n: terminalCyNodes) {
			network.getRow(n).set("selected", false);
		}
		List<CyNode> proteins = new ArrayList<CyNode>();
		Boolean mixedTypeNetwork = false;
		Boolean weightsSet = false;
		
		if (network.getDefaultNodeTable().getColumn("type")== null) {
			proteins = network.getNodeList();
			// to sort the proteins list based on the name so we have a deterministic result always
			Collections.sort(proteins, new Comparator<CyNode>() {
				 @Override
				 public int compare(CyNode cyNode, CyNode t1) {
				  String id1 = network.getRow(cyNode).get("name", String.class);
				  String id2 = network.getRow(t1).get("name", String.class);
				  return id1.compareTo(id2);
				 }
				});
			///
			terminalNodes = GraphUtils.cyNetworkToNetwork(graph, network, proteins, terminalCyNodes, weightsSet, penalized);
			GraphUtils.inLCC(terminalNodes, graph);						
		}
		else if (network.getDefaultNodeTable().getColumn("type").getValues(String.class).contains(NodeType.Protein.toString())) {
			proteins = FilterType.nodesInPPI(network);
			//// to sort the proteins list based on the name so we have a deterministic result always
			Collections.sort(proteins, new Comparator<CyNode>() {
				@Override
				public int compare(CyNode cyNode, CyNode t1) {
				String id1 = network.getRow(cyNode).get("name", String.class);
				String id2 = network.getRow(t1).get("name", String.class);
				return id1.compareTo(id2);
				}
				});

			terminalNodes = GraphUtils.cyNetworkToPPINetNoIsolate(graph, network, proteins, terminalCyNodes, weightsSet, penalized);
			GraphUtils.inLCC(terminalNodes, graph);
			mixedTypeNetwork = true;
		}
		else {
			throw new RuntimeException("The network should contain PROTEIN nodes!");
		}
		
		
		if (penalized) {
			double totalAvDeg = GraphUtils.getAvDeg(graph);
			GraphUtils.setEdgeWeight(graph, network, hubPenalty.getValue(), totalAvDeg);
			logger.info("The average degree of nodes in the graph is: " + totalAvDeg);
			weightsSet = true;
		}
		
		SteinerTree st = new SteinerTree(graph, terminalNodes);
		logger.info("The total weight of the first Steiner tree: " + st.getSteinerTreeWeight());
		SimpleWeightedGraph<Vertex, Link> steiner = st.getSteinerTree();				
		Set<Link> stEdges = steiner.edgeSet();
		Map<CyNode, Integer> participationNumber = new HashMap<CyNode, Integer>();
		Map<Integer, Set<Link>> stEdgesMap = new HashMap<Integer, Set<Link>>();
		stEdgesMap.put(1, stEdges);
		for (Vertex v: steiner.vertexSet()) {
			participationNumber.put(network.getNode(Long.parseLong(v.toString())), 1);
		}

		if (multiple) {
			terminalCyNodes = new ArrayList<CyNode>();
			for (Vertex v: terminalNodes) {
				Long nSUID = Long.parseLong(v.toString());
				terminalCyNodes.add(network.getNode(nSUID));
			}
			
			// give the shuffle a random seed to start with, so we always get deterministic results.
			Random rnd;
			rnd = new Random(42);
			
			int tn = 2;
			int trial = 1;
			while (tn<= treeNumber.getValue() && trial < treeNumber.getValue()+iterNumber.getValue()) {
				
				Collections.shuffle(proteins, rnd);
				
				if(!mixedTypeNetwork) {
					terminalNodes = GraphUtils.cyNetworkToNetwork(graph, network, proteins, terminalCyNodes, weightsSet, penalized);
				}
				else if (mixedTypeNetwork) {
					terminalNodes = GraphUtils.cyNetworkToPPINetNoIsolate(graph, network, proteins, terminalCyNodes, weightsSet, penalized);
				}
				st = new SteinerTree(graph, terminalNodes);				
				// only keep non-identical trees
				int notIdentical = 0;
				for (Entry<Integer, Set<Link>> entry: stEdgesMap.entrySet()) {
					if (st.getSteinerTree().edgeSet().equals(entry.getValue())) {
						//logger.info("Found one identical Steiner tree!");
						break;
					}						
					else if (!st.getSteinerTree().edgeSet().equals(entry.getValue())) {
						notIdentical ++;
					}
				}
				if (notIdentical == tn-1) {
					logger.info("The total weight of the next Steiner tree: " + st.getSteinerTreeWeight());
					stEdgesMap.put(tn, st.getSteinerTree().edgeSet());
					tn ++;
					for (Vertex v: st.getSteinerTree().vertexSet()) {
						participationNumber.put(network.getNode(Long.parseLong(v.toString())), participationNumber.getOrDefault(network.getNode(Long.parseLong(v.toString())), 0)+1);
					}
				}
				trial ++;
				
			}
			logger.info(String.format("After %s number of trials we reached the %s number of Steiner trees!", trial, tn-1));
			
		}
		
		for (Entry<Integer, Set<Link>> entry: stEdgesMap.entrySet()) {
			for (Link l: entry.getValue()) {
				Long snSUID = Long.parseLong(l.getSource().toString());
				Long tnSUID = Long.parseLong(l.getTarget().toString());
				List<CyEdge> lce = network.getConnectingEdgeList(network.getNode(snSUID), network.getNode(tnSUID), CyEdge.Type.ANY);
				network.getRow(lce.get(0)).set("selected", true);
			}
//			logger.info("The edge set of the steiner tree number " + entry.getKey() + " is: " + entry.getValue());
		}
		
		Map<String, Object> args = new HashMap<>();
		//network create edgeList=selected, nodeList=selected, source=current
		args.put("edgeList", "selected");
		args.put("nodeList", "selected");
		//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
		args.put("excludeEdges", true);
		args.put("source", "current");
		args.put("networkName", newNetName);
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("network", "create", args, null);
		//netMgr.addNetwork(newNet);
		
		CyNetwork newNet = ModelUtil.getNetworkWithName(app, newNetName);		
		CyTable localNewNodeTable = newNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		if (localNewNodeTable.getColumn("#participation") == null) {
			localNewNodeTable.createColumn("#participation", Integer.class, false);
			for (Entry<CyNode, Integer> entry: participationNumber.entrySet()) {
				//Long nSUID = Long.parseLong(entry.getKey().toString());
				newNet.getRow(entry.getKey()).set("#participation", entry.getValue());
			}
		}

		insertTasksAfterCurrentTask(new DeselectAll(app, network));
		
	}

}
