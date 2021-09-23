package org.cytoscape.myApp.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.myApp.internal.utils.FilterType;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CyNetToGraphConverter {
	
	private RepoApplication app;	
	private Logger logger = LoggerFactory.getLogger(getClass());
	//private WeightedMultigraph<Vertex, Link> g;
	
	public CyNetToGraphConverter (RepoApplication app) {
		this.app = app;
	}
	
	
	/**
	 * This methods returns a graph consisting only PPI part of the cynetwork without isolated proteins.
	 */
	public UndirectedNetwork cyNetworkToPPIgraph(CyNetwork network) {
		UndirectedNetwork graph = new UndirectedNetwork();
		
		Map<Long,Vertex> nodeMap = new HashMap<>();
		for(CyNode cynode : network.getNodeList()) {
			if (network.getRow(cynode).get("type", String.class).equals(NodeType.Protein.toString()) &&
					FilterType.adjacentEdgesOfType(network, cynode, InteractionType.protein_protein).size()!=0) {
				Vertex node = new Vertex(Long.toString(cynode.getSUID()));
				nodeMap.put(cynode.getSUID(), node);
				graph.addVertex(node);
			}			
		}
		boolean weighted = true;
		if (network.getDefaultEdgeTable().getColumn("weight") == null) {
			weighted = false;
		}
		double weight = 0.0;
		
		Set<CyEdge> ppiEdges = FilterType.edgesOfType(network, InteractionType.protein_protein);
		for (CyEdge e: ppiEdges) {
			if (!e.getSource().equals(e.getTarget())) {
				Vertex source = nodeMap.get(e.getSource().getSUID());
				Vertex target = nodeMap.get(e.getTarget().getSUID());
				if (weighted) {
					weight = network.getRow(e).get("weight", Double.class);
				}
				else weight = 1.0;
				Link edge = new Link(source, target, e.getSUID(), weight);
				graph.addEdge(source, target, edge);
				graph.setEdgeWeight(edge, weight);
			}			
		}
		return graph;
	}
	
	/**
	 * This methods returns a graph consisting only PPI part of the cynetwork without isolated proteins.
	 * It also fill the map for CyNode SUIDs as key and Vertex as values. 
	 */
	public Map<Long, Vertex> cyNetToPPIgraph(CyNetwork network, UndirectedNetwork out) {
		Map<Long,Vertex> nodeMap = new HashMap<>();
		for(CyNode cynode : network.getNodeList()) {
			if (network.getRow(cynode).get("type", String.class).equals(NodeType.Protein.toString()) &&
					FilterType.adjacentEdgesOfType(network, cynode, InteractionType.protein_protein).size()!=0) {
				Vertex node = new Vertex(Long.toString(cynode.getSUID()));
				nodeMap.put(cynode.getSUID(), node);
				out.addVertex(node);
			}			
		}
		boolean weighted = true;
		if (network.getDefaultEdgeTable().getColumn("weight") == null) {
			weighted = false;
		}
		double weight = 0.0;
		
		Set<CyEdge> ppiEdges = FilterType.edgesOfType(network, InteractionType.protein_protein);
		for (CyEdge e: ppiEdges) {
			if (!e.getSource().equals(e.getTarget())) {
				Vertex source = nodeMap.get(e.getSource().getSUID());
				Vertex target = nodeMap.get(e.getTarget().getSUID());
				if (weighted) {
					weight = network.getRow(e).get("weight", Double.class);
				}
				else weight = 1.0;
				Link edge = new Link(source, target, e.getSUID(), weight);
				out.addEdge(source, target, edge);
				out.setEdgeWeight(edge, weight);
			}			
		}
		return nodeMap;
	}
	
	/**
	 * This creates the PPI network + all isolated proteins from other databases like DrugBank, REACTOME,...
	 */
	public List <Vertex> cyNetworkToPPINet(Graph<Vertex,Link> out, CyNetwork network, Set<CyNode> terminalCyNodes) {
		List <Vertex> terminalNodes = new ArrayList<Vertex> ();
		Map<Long,Vertex> nodeMap = new HashMap<>();
		for(CyNode cynode : network.getNodeList()) {
			if (network.getRow(cynode).get("type", String.class).equals(NodeType.Protein.toString())) {
				Vertex node = new Vertex(Long.toString(cynode.getSUID()));
				if (terminalCyNodes.contains(cynode)) {
					terminalNodes.add(node);
				}
				nodeMap.put(cynode.getSUID(), node);
				out.addVertex(node);
			}			
		}
		boolean weighted = true;
		if (network.getDefaultEdgeTable().getColumn("weight") == null) {
			weighted = false;
		}
		double weight = 0.0;

		for(CyEdge cyedge : network.getEdgeList()) {
			// to exclude selfLoops from PPI
			boolean selfLoop = false;
			if (cyedge.getSource().equals(cyedge.getTarget())) {
				selfLoop = true;
			}
			if (network.getRow(cyedge).get("type", String.class).equals(InteractionType.protein_protein.toString()) && !selfLoop) {
				Vertex source = nodeMap.get(cyedge.getSource().getSUID());
				Vertex target = nodeMap.get(cyedge.getTarget().getSUID());
				//String name = network.getRow(cyedge).get(CyNetwork.NAME, String.class);
				//logger.info("The weight for edge: " + cyedge.getSUID() + " is: " + network.getRow(cyedge).get("weight", Double.class));
				//network.getRow(cyedge).get("weight", Double.class);
				if (weighted) {
					weight = network.getRow(cyedge).get("weight", Double.class);
				}
				else weight = 1.0;
				Link edge = new Link(source, target, cyedge.getSUID(), weight);
				out.addEdge(source, target, edge);
				out.setEdgeWeight(edge, weight);
			}			
		}
		return terminalNodes;
	}
	
	/// Note: this creates the PPI network without isolated proteins (proteins without any neighbors),...
	/**
     * This method gets the terminal/seed CyNodes and the CyNetwork and returns the seeds as vertices and also
     * convert CyNetwork to graph without isolated node, only includes the one participating in p-p interacrtions. 
     * Specifically we use this function when we have PPI network as part of a larger heterogeneous network. 
     * Set of isolated proteins will be updated as well. Also return the seeds vertex set which are not isolated.
     */
	public List <Vertex> cyNetworkToPPINetNoIsolate(Graph<Vertex,Link> out, CyNetwork network, Set<CyNode> terminalCyNodes, Set<CyNode> isolatedProts) {
		List <Vertex> terminalNodes = new ArrayList<Vertex> ();
		Map<Long,Vertex> nodeMap = new HashMap<>();
		// to test sorted nodeList:
		//List<CyNode> nlist = new ArrayList<CyNode>(network.getNodeList());
		List<CyNode> proteins = FilterType.nodesOfTypeList(network, NodeType.Protein);
		//Collections.shuffle(proteins);
		for(CyNode cynode : proteins) {
			if (FilterType.adjacentEdgesOfType(network, cynode, InteractionType.protein_protein).size()!=0) {
				Vertex node = new Vertex(Long.toString(cynode.getSUID()));
				if (terminalCyNodes.contains(cynode)) {
					terminalNodes.add(node);
				}
				nodeMap.put(cynode.getSUID(), node);
				out.addVertex(node);
			}
			else if (FilterType.adjacentEdgesOfType(network, cynode, InteractionType.protein_protein).size()==0){
				isolatedProts.add(cynode);
			}
		}
		// end of test
		/*for(CyNode cynode : network.getNodeList()) {
			String suid = cynode.SUID;
			Long suID = cynode.getSUID();
			if (network.getRow(cynode).get("type", String.class).equals(NodeType.Protein.toString()) &&
					FilterType.adjacentEdgesOfType(network, cynode, InteractionType.protein_protein).size()!=0) {
				Vertex node = new Vertex(Long.toString(cynode.getSUID()));
				if (terminalCyNodes.contains(cynode)) {
					terminalNodes.add(node);
				}
				nodeMap.put(cynode.getSUID(), node);
				out.addVertex(node);
			}
			else if (network.getRow(cynode).get("type", String.class).equals(NodeType.Protein.toString()) &&
					FilterType.adjacentEdgesOfType(network, cynode, InteractionType.protein_protein).size()==0){
				isolatedProts.add(cynode);
			}
		}*/
		
		boolean weighted = true;
		if (network.getDefaultEdgeTable().getColumn("weight") == null) {
			weighted = false;
		}
		double weight = 0.0;

		for(CyEdge cyedge : network.getEdgeList()) {
			// to exclude selfLoops from PPI
			boolean selfLoop = false;
			if (cyedge.getSource().equals(cyedge.getTarget())) {
				selfLoop = true;
			}
			if (network.getRow(cyedge).get("type", String.class).equals(InteractionType.protein_protein.toString()) && !selfLoop) {
				Vertex source = nodeMap.get(cyedge.getSource().getSUID());
				Vertex target = nodeMap.get(cyedge.getTarget().getSUID());
				//String name = network.getRow(cyedge).get(CyNetwork.NAME, String.class);
				//logger.info("The weight for edge: " + cyedge.getSUID() + " is: " + network.getRow(cyedge).get("weight", Double.class));
				//network.getRow(cyedge).get("weight", Double.class);
				if (weighted) {
					weight = network.getRow(cyedge).get("weight", Double.class);
				}
				else weight = 1.0;
				Link edge = new Link(source, target, cyedge.getSUID(), weight);
				out.addEdge(source, target, edge);
				out.setEdgeWeight(edge, weight);
			}			
		}
		logger.info("This is the first vertex from the NodeList of the graph: " + out.vertexSet().iterator().next());
		//logger.info("This is the last vertex from the NodeList of the graph: " + network.getNodeList().get(network.getNodeList().size()-1));
		return terminalNodes;
	}
		/**
	     * This method gets the terminal/seed CyNodes and the CyNetwork and returns the seeds as vertices and also
	     * convert CyNetwork to graph the graph. We use this function when our network is simply a PPI network and 
	     * not a heterogeneous network
	     */
	public List <Vertex> cyNetworkToNetwork(Graph<Vertex,Link> out, CyNetwork network, Set<CyNode> terminalCyNodes) {
		List <Vertex> terminalNodes = new ArrayList<Vertex> ();
        Map<Long,Vertex> nodeMap = new HashMap<>();
        for(CyNode cynode : network.getNodeList()) {
            //Vertex node = new Vertex(Long.toString(cynode.getSUID()), cynode.getSUID());
        	Vertex node = new Vertex(Long.toString(cynode.getSUID()));
        	if (terminalCyNodes.contains(cynode)) {
        		terminalNodes.add(node);
        	}
            nodeMap.put(cynode.getSUID(), node);
            out.addVertex(node);
        }
        boolean weighted = true;
        if (network.getDefaultEdgeTable().getColumn("weight") == null) {
        	weighted = false;
        }
        double weight = 0.0;

        for(CyEdge cyedge : network.getEdgeList()) {
        	// to exclude selfLoops from PPI
        	boolean selfLoop = false;
        	if (cyedge.getSource().equals(cyedge.getTarget())) {
        		selfLoop = true;
        	}        	
            if (!selfLoop) {
            	Vertex source = nodeMap.get(cyedge.getSource().getSUID());
                Vertex target = nodeMap.get(cyedge.getTarget().getSUID());
                //String name = network.getRow(cyedge).get(CyNetwork.NAME, String.class);
                //logger.info("The weight for edge: " + cyedge.getSUID() + " is: " + network.getRow(cyedge).get("weight", Double.class));
                //network.getRow(cyedge).get("weight", Double.class);
                if (weighted) {
                	weight = network.getRow(cyedge).get("weight", Double.class);
                }
                else weight = 1.0;
                Link edge = new Link(source, target, cyedge.getSUID(), weight);
                out.addEdge(source, target, edge);
                out.setEdgeWeight(edge, weight);
            }
        }
        return terminalNodes;
    }
	
	/**
     * This method gets the terminal/seed CyNodes and the CyNetwork and returns the seeds as vertices and also
     * convert CyNetwork to a graph without isolated nodes. We use this function when our network is simply a 
     * PPI network and not a heterogeneous network. Set of isolated nodes will be updated as well
     */
	public List <Vertex> cyNetworkToNetNoIsolate(Graph<Vertex,Link> out, CyNetwork network, Set<CyNode> terminalCyNodes, Set<CyNode> isolatedNodes) {
		List <Vertex> terminalNodes = new ArrayList<Vertex> ();
		Map <Long,Vertex> nodeMap = new HashMap<>();
		for(CyNode cynode : network.getNodeList()) {
			if (!network.getNeighborList(cynode, CyEdge.Type.ANY).isEmpty()) {
				Vertex node = new Vertex(Long.toString(cynode.getSUID()));
				if (terminalCyNodes.contains(cynode)) {
					terminalNodes.add(node);
				}
				nodeMap.put(cynode.getSUID(), node);
				out.addVertex(node);
			}			
		}
		boolean weighted = true;
		if (network.getDefaultEdgeTable().getColumn("weight") == null) {
			weighted = false;
		}
		double weight = 0.0;

		for(CyEdge cyedge : network.getEdgeList()) {
			// to exclude selfLoops from PPI
			boolean selfLoop = false;
			if (cyedge.getSource().equals(cyedge.getTarget())) {
				selfLoop = true;
			}        	
			if (!selfLoop) {
				Vertex source = nodeMap.get(cyedge.getSource().getSUID());
				Vertex target = nodeMap.get(cyedge.getTarget().getSUID());
				//String name = network.getRow(cyedge).get(CyNetwork.NAME, String.class);
				//logger.info("The weight for edge: " + cyedge.getSUID() + " is: " + network.getRow(cyedge).get("weight", Double.class));
				//network.getRow(cyedge).get("weight", Double.class);
				if (weighted) {
					weight = network.getRow(cyedge).get("weight", Double.class);
				}
				else weight = 1.0;
				Link edge = new Link(source, target, cyedge.getSUID(), weight);
				out.addEdge(source, target, edge);
				out.setEdgeWeight(edge, weight);
			}
		}
		return terminalNodes;
	}
	
    /**
     * This method gets the terminal/seed nodes and the graph and returns those seeds that are not part of 
     * largest connected component in the graph. Moreover, it updates the terminal/seed nodes and only
     * keeps the ones in LCC
     */
	
	public List<Vertex> inConnectedComponent (List <Vertex> terminalNodes, UndirectedNetwork net) {
		List<Vertex> terminalsNotConnected = new ArrayList<Vertex>(terminalNodes);
		ConnectivityInspector<Vertex, Link> cIns = new ConnectivityInspector<Vertex, Link>(net);
	    List<Set<Vertex>> cnctdcomps = cIns.connectedSets();
	    System.out.println("The number of connected components: " + cnctdcomps.size());
	    logger.info("The number of connected components: " + cnctdcomps.size());
	    int maxcopmSize = 0;
	    Set<Vertex> maxCnctdComp = new HashSet<Vertex>();
	    for (Set<Vertex> s: cnctdcomps) {
	    	if (s.size() > maxcopmSize) {
	    		maxcopmSize = s.size();
	    		maxCnctdComp = s;
	    	}
	    }
	    System.out.println("The largest connected component size: " + maxCnctdComp.size());
	    logger.info("The largest connected component size: " + maxCnctdComp.size());
	   // List<Vertex> allTerminals = new ArrayList<Vertex>(terminalNodes);
	    terminalNodes.retainAll(maxCnctdComp);
	    terminalsNotConnected.removeAll(terminalNodes);
	    return terminalsNotConnected;
	}
	
	
	
	/**
     * This method gets the graph and returns those nodes that are not part of 
     * the largest connected component in the graph.
     */
	public Set<Vertex> notInConnectedComponent (UndirectedNetwork net) {
		Set<Vertex> nodesNotInLCC = new HashSet<Vertex>();
		ConnectivityInspector<Vertex, Link> cIns = new ConnectivityInspector<Vertex, Link>(net);
	    List<Set<Vertex>> cnctdcomps = cIns.connectedSets();
	    System.out.println("The number of connected components: " + cnctdcomps.size());
	    logger.info("The number of connected components: " + cnctdcomps.size());
	    int maxcopmSize = 0;
	    Set<Vertex> maxCnctdComp = new HashSet<Vertex>();
	    for (Set<Vertex> s: cnctdcomps) {
	    	if (s.size() > maxcopmSize) {
	    		maxcopmSize = s.size();
	    		//maxCnctdComp = s;
	    		maxCnctdComp = new HashSet<Vertex>(s);
	    	}
	    }
	    logger.info("The largest connected component size: " + maxCnctdComp.size());
	    nodesNotInLCC = new HashSet<Vertex> (net.vertexSet());
	    nodesNotInLCC.removeAll(maxCnctdComp);
	    //logger.info("The nodes not in LCC: " + nodesNotInLCC);
	    return nodesNotInLCC;
	}
	
	public Set<Vertex> getLargestConnectedComponent (UndirectedNetwork network){
		Set<Vertex> largestCnctdComp = new HashSet<Vertex>();
		ConnectivityInspector<Vertex, Link> cIns = new ConnectivityInspector<Vertex, Link>(network);
	    List<Set<Vertex>> cnctdcomps = cIns.connectedSets();
	    logger.info("The number of connected components: " + cnctdcomps.size());
	    int maxcopmSize = 0;
	    for (Set<Vertex> s: cnctdcomps) {
	    	if (s.size() > maxcopmSize) {
	    		maxcopmSize = s.size();
	    		largestCnctdComp = new HashSet<Vertex>(s);
	    	}
	    }
		return largestCnctdComp;
	}


}
