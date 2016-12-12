package com.panduit.graph;


/**
 * Builder for building graph.
 *
 * Its use is optional although it provides convenient chaining syntax.
 *
 * @param <Node>
 */
public class DirectedGraphBuilder<Node> {
    private Graph graph = new DirectedGraph();

    public DirectedGraphBuilder addNode(Node node) {
        graph.addNode(node);
        return this;
    }

    public DirectedGraphBuilder addEdge(Node u, Node v, String label, double weight) {
        graph.addEdge(u, v, label, weight);
        return this;
    }

    public Graph build() {
        return graph;
    }
}
