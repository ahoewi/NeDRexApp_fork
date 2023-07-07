package org.cytoscape.nedrex.internal;

import org.jgrapht.graph.SimpleWeightedGraph;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
// According to docs, SimpleWeightedGraph is an undirected weighted graph without self-loops and multiple edges
public class UndirectedNetwork extends SimpleWeightedGraph<Vertex, Link>{
	public UndirectedNetwork() {
        super(Link.class);
    }

}
