package org.cytoscape.myApp.internal.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.myApp.internal.Link;
import org.cytoscape.myApp.internal.Vertex;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class SteinerTree {
	
	SimpleWeightedGraph<Vertex, Link> graph;
	SimpleWeightedGraph<Vertex, Link> tree;
	List<Vertex> steinerNodes;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public SteinerTree(SimpleWeightedGraph<Vertex, Link> graph, List<Vertex> steinerNodes) {
		this.graph = graph;
		this.steinerNodes = steinerNodes;
		
		runAlgorithm();
	}
	
	/**
	 * Construct the complete undirected distance graph G1=(V1,E1,d1) from G and S.
	 * @return
	 */
	private SimpleWeightedGraph<Vertex, Link> step1(Map <Link, List<Link>> spMap) {

		SimpleWeightedGraph<Vertex, Link> g = 
			new SimpleWeightedGraph<Vertex, Link>(Link.class);
		
		int suid = 0;		
		for (Vertex n : this.steinerNodes) {
			g.addVertex(n);
		}		
		for (Vertex n1 : this.steinerNodes) {

			for (Vertex n2 : this.steinerNodes) {
				if (g.containsEdge(n1, n2) || g.containsEdge(n2, n1) || n1.equals(n2))
					continue;
				if (!n1.equals(n2)) {
					GraphPath<Vertex, Link> gpath = DijkstraShortestPath.findPathBetween(this.graph, n1, n2);
					
					Link e = new Link(n1, n2, Long.valueOf(suid), gpath.getWeight());
					g.addEdge(n1, n2, e);
					g.setEdgeWeight(e, gpath.getWeight());
					//logger.info("The path weight between node " + n1 + " and " + n2 + " is: " + gpath.getWeight());
					suid ++;
					spMap.put(e, gpath.getEdgeList());
				}					
			}
		}
		//logger.info("This is G1, including edges: " + g);
		return g;
	}
	
	/**
	 * Find the minimal spanning tree, T1, of G1. (If there are several minimal spanning trees, pick an arbitrary one.)
	 * @param g1
	 * @return
	 */
	private SimpleWeightedGraph<Vertex, Link> step2(SimpleWeightedGraph<Vertex, Link> g1) {
		
		KruskalMinimumSpanningTree<Vertex, Link> mst = new KruskalMinimumSpanningTree<Vertex, Link>(g1);
		SpanningTreeAlgorithm.SpanningTree<Link> krst= mst.getSpanningTree();
		Set<Link> edges = krst.getEdges();
       
		SimpleWeightedGraph<Vertex, Link> g2 = new SimpleWeightedGraph<Vertex, Link>(Link.class);

///////// Can be source of randomness!!!!!! for deterministic iteration maybe needs to be sorted and kept as list 		
		for (Link edge : edges) {
			g2.addVertex(edge.getSource());
			g2.addVertex(edge.getTarget());
			g2.addEdge(edge.getSource(), edge.getTarget(), edge); 
		}
		//logger.info("This is G2, including edges: " + g2);	
		return g2;
	}
	
	
	/**
	 * Construct the subgraph, Gs, of G by replacing each edge in T1 by its corresponding shortest path in G. 
	 * (If there are several shortest paths, pick an arbitrary one.)
	 * @param g2
	 * @return
	 */
	private SimpleWeightedGraph<Vertex, Link> step3(SimpleWeightedGraph<Vertex, Link> g2, Map <Link, List<Link>> spMap) {

		SimpleWeightedGraph<Vertex, Link> g3 = new SimpleWeightedGraph<Vertex, Link>(Link.class);

///////// Can be source of randomness!!!!!! for deterministic iteration maybe needs to be sorted and kept as list 
		Set<Link> edges = g2.edgeSet();		
		Vertex source, target;
		
		for (Link edge : edges) {
			List<Link> pathEdges = spMap.get(edge);
			if (pathEdges == null)
				continue;			
			for (int i = 0; i < pathEdges.size(); i++) {				
				/*if (g3.edgeSet().contains(pathEdges.get(i)))
					continue;*/
				
				source = pathEdges.get(i).getSource();
				target = pathEdges.get(i).getTarget();

				//if (!g3.vertexSet().contains(source) )
				g3.addVertex(source);

				//if (!g3.vertexSet().contains(target) )
				g3.addVertex(target);
				
				g3.addEdge(source, target, pathEdges.get(i));
				//g3.setEdgeWeight(edge, weight);
			}
		}
		//logger.info("This is G3, including edges: " + g3);	
		return g3;
	}
	
	
	/**
	 * Find the minimal spanning tree, Ts, of Gs. (If there are several minimal spanning trees, pick an arbitrary one.)
	 * @param g3
	 * @return
	 */
	private SimpleWeightedGraph<Vertex, Link> step4(SimpleWeightedGraph<Vertex, Link> g3) {

		KruskalMinimumSpanningTree<Vertex, Link> mst = new KruskalMinimumSpanningTree<Vertex, Link>(g3);
		
		SpanningTreeAlgorithm.SpanningTree<Link> krst= mst.getSpanningTree();
		Set<Link> edges = krst.getEdges();

		SimpleWeightedGraph<Vertex, Link> g4 = new SimpleWeightedGraph<Vertex, Link>(Link.class);

		for (Link edge : edges) {
			g4.addVertex(edge.getSource());
			g4.addVertex(edge.getTarget());
			g4.addEdge( edge.getSource(), edge.getTarget(), edge); 
		}
		//logger.info("This is G4, including edges: " + g4);	
		return g4;
	}
	
	
	/**
	 * Construct a Steiner tree, Th, from Ts by deleting edges in Ts, if necessary, 
	 * so that all the leaves in Th are Steiner points.
	 * @param g4
	 * @return
	 */
	private SimpleWeightedGraph<Vertex, Link> step5(SimpleWeightedGraph<Vertex, Link> g4) {
		
		SimpleWeightedGraph<Vertex, Link> g5 = g4; 
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
		//logger.info("This is G5, including edges: " + g5);	
		return g5;
	}
	
	
	private void runAlgorithm() {
		
		Map <Link, List<Link>> spMap = new HashMap<Link, List<Link>>();
		SimpleWeightedGraph<Vertex, Link> g1 = step1(spMap);
		
		if (g1.vertexSet().size() < 2) {
			this.tree = new SimpleWeightedGraph<Vertex, Link>(Link.class);
			for (Vertex n : g1.vertexSet()) this.tree.addVertex(n);
			return;
		}
		
		SimpleWeightedGraph<Vertex, Link> g2 = step2(g1);
		SimpleWeightedGraph<Vertex, Link> g3 = step3(g2, spMap);
		SimpleWeightedGraph<Vertex, Link> g4 = step4(g3);
		SimpleWeightedGraph<Vertex, Link> g5 = step5(g4);		
		this.tree = g5;
	}
	
	
	public SimpleWeightedGraph<Vertex, Link> getSteinerTree() {
		return this.tree;
	}
	
	public Double getSteinerTreeWeight() {
		Double weight = 0D;
		for (Link l: this.tree.edgeSet()) {
			weight += l.getWeight();
		}
		return weight;
	}
	
	// testing pagerank
	private void getRank() {
		double dampfact= 0.85;
		PageRank pgrnk = new PageRank<>(graph, dampfact);
	}
}
