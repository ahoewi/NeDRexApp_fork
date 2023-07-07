package org.cytoscape.nedrex.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.graph.WeightedPseudograph;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CySteinerTree {
	
	//UndirectedGraph<Vertex, Link> graph;
	SimpleWeightedGraph<Vertex, Link> graph;
	WeightedMultigraph<Vertex, Link> tree;
	List<Vertex> steinerNodes;
	
	public CySteinerTree(SimpleWeightedGraph<Vertex, Link> graph, List<Vertex> steinerNodes) {		
		this.graph = graph;
		this.steinerNodes = steinerNodes;
		
		runAlgorithm();
		
	}
	
	/**
	 * Construct the complete undirected distance graph G1=(V1,E1,d1) from G and S.
	 * @return
	 */
	// try later with SimpleWeightedGraph instead of WeightedPseudograph, it's better since it doesn't have selfloop and multiple edges
	private WeightedPseudograph<Vertex, Link> step1(Map <Link, List<Link>> spMap) {
		
		//logger.debug("<enter");

		WeightedPseudograph<Vertex, Link> g = 
			new WeightedPseudograph<Vertex, Link>(Link.class);
		
		int suid = 0;
		
		for (Vertex n : this.steinerNodes) {
			g.addVertex(n);
		}
		
		//BellmanFordShortestPath<Vertex, Link> path;
		
		//Map <Link, List<Link>> spMap = new HashMap<Link, List<Link>>();
		
		for (Vertex n1 : this.steinerNodes) {
			//path = new BellmanFordShortestPath<Vertex, Link>(this.graph, n1);
			
			for (Vertex n2 : this.steinerNodes) {
				if (g.containsEdge(n1, n2) || n1.equals(n2))
					continue;
				if (!n1.equals(n2)) {
					//GraphPath<Vertex, Link> gpath = BellmanFordShortestPath.findPathBetween(this.graph, n1, n2);
					GraphPath<Vertex, Link> gpath = DijkstraShortestPath.findPathBetween(this.graph, n1, n2);
					/*if (n1.equals(n2))
						continue;*/
					//System.out.println("This is the shortest path between " + n1 + " and " + n2 + ": " + gpath);
					
					//Link e = new Link(null, null, null, null);
					Link e = new Link(n1, n2, Long.valueOf(suid), gpath.getWeight());
					//Link e = new Link(null);
					g.addEdge(n1, n2, e);
					//g.setEdgeWeight(e, path.getCost(n2));
					g.setEdgeWeight(e, gpath.getWeight());
					suid ++;
					
					// Keep the shortest path between steiner nodes to avoid computing them again at step 3, Attention: not all of these shortest paths will be used
					spMap.put(e, gpath.getEdgeList());
				}
								
			}

		}
		
		//logger.debug("exit>");
		//System.out.println("This is the g:" + g);

		return g;

	}
	
	/**
	 * Find the minimal spanning tree, T1, of G1. (If there are several minimal spanning trees, pick an arbitrary one.)
	 * @param g1
	 * @return
	 */
	private WeightedMultigraph<Vertex, Link> step2(WeightedPseudograph<Vertex, Link> g1) {

		//logger.debug("<enter");

		KruskalMinimumSpanningTree<Vertex, Link> mst =
            new KruskalMinimumSpanningTree<Vertex, Link>(g1);
		SpanningTreeAlgorithm.SpanningTree<Link> krst= mst.getSpanningTree();

//    	System.out.println("Total MST Cost: " + mst.getSpanningTreeCost());

//        Set<CyEdge> edges = mst.getEdgeSet();
		//Set<Link> edges = mst.getMinimumSpanningTreeEdgeSet();
		Set<Link> edges = krst.getEdges();
        

		WeightedMultigraph<Vertex, Link> g2 = 
			new WeightedMultigraph<Vertex, Link>(Link.class);
		
/*		List<CyEdge> edgesSortedById = new ArrayList<CyEdge>();
		
		for (CyEdge e : edges) 
			edgesSortedById.add(e);
		
		Collections.sort(edgesSortedById);
		
		for (CyEdge edge : edgesSortedById) {
			g2.addVertex(edge.getSource());
			g2.addVertex(edge.getTarget());
			g2.addEdge( edge.getSource(), edge.getTarget(), edge); 
		}*/
		
		for (Link edge : edges) {
			g2.addVertex(edge.getSource());
			g2.addVertex(edge.getTarget());
			g2.addEdge( edge.getSource(), edge.getTarget(), edge); 
		}
		
		//logger.debug("exit>");
		//System.out.println("This is the g2:" + g2);

		return g2;
	}
	
	/**
	 * Construct the subgraph, Gs, of G by replacing each edge in T1 by its corresponding shortest path in G. 
	 * (If there are several shortest paths, pick an arbitrary one.)
	 * @param g2
	 * @return
	 */
	private WeightedMultigraph<Vertex, Link> step3(WeightedMultigraph<Vertex, Link> g2, Map <Link, List<Link>> spMap) {
		
		//logger.debug("<enter");

		WeightedMultigraph<Vertex, Link> g3 = 
			new WeightedMultigraph<Vertex, Link>(Link.class);
		
		Set<Link> edges = g2.edgeSet();
		//DijkstraShortestPath<Vertex, Link> path;
		
		Vertex source, target;
		
		for (Link edge : edges) {
			/*source = edge.getSource();
			target = edge.getTarget();
			
			GraphPath<Vertex, Link> dijpath = DijkstraShortestPath.findPathBetween(this.graph, source, target);
			//path = new DijkstraShortestPath<Vertex, Link>(this.graph, source, target);
			//List<Link> pathEdges = path.getPathEdgeList();
			
			List<Link> pathEdges = dijpath.getEdgeList()*/;
			List<Link> pathEdges = spMap.get(edge);
			if (pathEdges == null)
				continue;
			
			for (int i = 0; i < pathEdges.size(); i++) {
				
				if (g3.edgeSet().contains(pathEdges.get(i)))
					continue;
				
				source = pathEdges.get(i).getSource();
				target = pathEdges.get(i).getTarget();
				
				if (!g3.vertexSet().contains(source) )
					g3.addVertex(source);

				if (!g3.vertexSet().contains(target) )
					g3.addVertex(target);

				g3.addEdge(source, target, pathEdges.get(i));
			}
		}

		//logger.debug("exit>");
		//System.out.println("This is the g3:" + g3);

		return g3;
	}
	
	/**
	 * Find the minimal spanning tree, Ts, of Gs. (If there are several minimal spanning trees, pick an arbitrary one.)
	 * @param g3
	 * @return
	 */
	private WeightedMultigraph<Vertex, Link> step4(WeightedMultigraph<Vertex, Link> g3) {

		//logger.debug("<enter");

		KruskalMinimumSpanningTree<Vertex, Link> mst =
            new KruskalMinimumSpanningTree<Vertex, Link>(g3);
		
		SpanningTreeAlgorithm.SpanningTree<Link> krst= mst.getSpanningTree();

//    	System.out.println("Total MST Cost: " + mst.getSpanningTreeCost());


//      Set<CyEdge> edges = mst.getEdgeSet();
		//Set<Link> edges = mst.getMinimumSpanningTreeEdgeSet();
		Set<Link> edges = krst.getEdges();

		WeightedMultigraph<Vertex, Link> g4 = 
			new WeightedMultigraph<Vertex, Link>(Link.class);
		
/*		List<CyEdge> edgesSortedById = new ArrayList<CyEdge>();
		
		for (CyEdge e : edges) 
			edgesSortedById.add(e);
		
		Collections.sort(edgesSortedById);
		
		for (CyEdge edge : edgesSortedById) {
			g4.addVertex(edge.getSource());
			g4.addVertex(edge.getTarget());
			g4.addEdge( edge.getSource(), edge.getTarget(), edge); 
		}*/
		
		for (Link edge : edges) {
			g4.addVertex(edge.getSource());
			g4.addVertex(edge.getTarget());
			g4.addEdge( edge.getSource(), edge.getTarget(), edge); 
		}
		
		//logger.debug("exit>");
		//System.out.println("This is the g4:" + g4);

		return g4;
	}
	
	/**
	 * Construct a Steiner tree, Th, from Ts by deleting edges in Ts,if necessary, 
	 * so that all the leaves in Th are Steiner points.
	 * @param g4
	 * @return
	 */
	private WeightedMultigraph<Vertex, Link> step5(WeightedMultigraph<Vertex, Link> g4) {
		
		//logger.debug("<enter");

		WeightedMultigraph<Vertex, Link> g5 = g4; 

		List<Vertex> nonSteinerLeaves = new ArrayList<Vertex>();
		
		Set<Vertex> vertexSet = g4.vertexSet();
		for (Vertex vertex : vertexSet) {
			if (g5.degreeOf(vertex) == 1 && steinerNodes.indexOf(vertex) == -1) {
				nonSteinerLeaves.add(vertex);
			}
		}
		
		Vertex source, target;
		for (int i = 0; i < nonSteinerLeaves.size(); i++) {
			source = nonSteinerLeaves.get(i);
			do {
				Link e = g5.edgesOf(source).toArray(new Link[0])[0];
				target = this.graph.getEdgeTarget(e);
				
				// this should not happen, but just in case of ...
				if (target.equals(source)) 
					target = e.getSource();
				
				g5.removeVertex(source);
				source = target;
			} while(g5.degreeOf(source) == 1 && steinerNodes.indexOf(source) == -1);
			
		}
		
		//logger.debug("exit>");
		//System.out.println("This is the g5:" + g5);

		return g5;
	}
	
	private void runAlgorithm() {
		
		Map <Link, List<Link>> spMap = new HashMap<Link, List<Link>>();
		//logger.debug("<enter");
		//logger.debug("step1 ...");
		WeightedPseudograph<Vertex, Link> g1 = step1(spMap);
		
		if (g1.vertexSet().size() < 2) {
			this.tree = new WeightedMultigraph<Vertex, Link>(Link.class);
			for (Vertex n : g1.vertexSet()) this.tree.addVertex(n);
			return;
		}
		
		//logger.debug("step2 ...");
		WeightedMultigraph<Vertex, Link> g2 = step2(g1);
		
		//logger.debug("step3 ...");
		WeightedMultigraph<Vertex, Link> g3 = step3(g2, spMap);
		
		//logger.debug("step4 ...");
		WeightedMultigraph<Vertex, Link> g4 = step4(g3);
		
		//logger.debug("step5 ...");
		WeightedMultigraph<Vertex, Link> g5 = step5(g4);
		
		this.tree = g5;
		//logger.debug("exit>");

	}
	
	public WeightedMultigraph<Vertex, Link> getSteinerTree() {
		return this.tree;
	}
	
	

}
