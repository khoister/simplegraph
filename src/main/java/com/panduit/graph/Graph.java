package com.panduit.graph;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public interface Graph<Node> {

    // Mutable operations
    boolean addNode(Node node);
    void removeNode(Node node);
    boolean addEdge(Node src, Node dest, String label, double weight);
    void removeEdge(Node src, Node dest);

    // Query operations
    boolean containsNode(Node node);
    boolean containsEdge(Node u, Node v);
    Optional<Edge> getEdge(Node u, Node v);
    boolean isConnected(Node u, Node v);
    List<Node> findShortestPath(Node src, Node dest);

    Set<Node> getNodes();
    Map<Node, Edge> getOutgoingEdges(Node node);
    Map<Node, Edge> getIncomingEdges(Node node);

    // Informational
    void printNodes();
    void printEdges();
    String printDotFormat();
}
