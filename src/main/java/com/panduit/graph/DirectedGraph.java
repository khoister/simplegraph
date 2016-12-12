package com.panduit.graph;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.collections4.MapUtils;


public class DirectedGraph<Node> implements Graph<Node> {

    // Keeps track of neighbor vertices being pointed to by the key vertex
    // Edge information e.g. label, weight, etc., is kept as a payload attached
    // to each destination vertex that forms an edge.
    private final Map<Node, Map<Node, Edge>> outgoing = new HashMap<>();

    // Keeps track of vertices pointing to the key vertex
    private final Map<Node, Map<Node, Edge>> incoming = new HashMap<>();

    // Try to make this thread-safe
    private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock readLock = rwlock.readLock();
    private final Lock writeLock = rwlock.writeLock();

    /**
     * Checks for the existence of a vertex in the graph
     *
     * @param node vertex to check for existence
     * @return true if node exists in the graph, false otherwise
     */
    public boolean containsNode(Node node) {
        readLock.lock();
        try {
            return outgoing.containsKey(node);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Checks for the existence of an edge in the graph
     *
     * @param src
     * @param dest
     * @return true if edge exists in the graph, false otherwise
     */
    public boolean containsEdge(Node src, Node dest) {
        if (src == null || dest == null) {
            return false;
        }

        readLock.lock();
        try {
            if (!outgoing.containsKey(src)) {
                return false;
            }

            final Map<Node, Edge> neighbors = outgoing.get(src);
            return neighbors.containsKey(dest);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Retrieves information about the edge
     *
     * @param src
     * @param dest
     * @return object with edge information
     */
    public Edge getEdge(Node src, Node dest) {
        if (src == null || dest == null) {
            return new Edge();
        }

        readLock.lock();
        try {
            if (!outgoing.containsKey(src)) {
                return new Edge();
            }

            final Map<Node, Edge> neighbors = outgoing.get(src);
            return neighbors.get(dest);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if two vertices are connected through a path,
     * regardless of the direction of the edges.
     * @implNote Uses depth first search traversal of the graph
     *
     * @param u
     * @param v
     * @return true if vertices are connected in the graph
     */
    public boolean isConnected(Node u, Node v) {
        if (u == null || v == null) {
            return false;
        }

        readLock.lock();
        try {
            // Nodes are not connected if one or both
            // of the nodes do not exist in the graph
            if (!outgoing.containsKey(u) || !outgoing.containsKey(v)) {
                return false;
            }

            // There exists a loop at this node so it's
            // connected to itself
            if (u == v && containsEdge(u, v)) {
                return true;
            }

            // Keeps track of nodes that have been visited
            final Set<Node> visited = new HashSet<>();
            final Stack<Node> stack = new Stack<>();

            stack.push(u);
            while (!stack.isEmpty()) {
                u = stack.pop();
                if (!visited.contains(u)) {
                    visited.add(u);
                    for (final Node outNeighbor : outgoing.get(u).keySet()) {
                        if (outNeighbor == v) {
                            return true;
                        }
                        stack.push(outNeighbor);
                    }

                    // Vertices C and A are connected even if the graph is A --> B --> C
                    // It is obvious starting from A, we can see that C is a connection.
                    // Starting from C, we should be able to find a connection to A as well.
                    for (final Node inNeighbor : incoming.get(u).keySet()) {
                        if (inNeighbor == v) {
                            return true;
                        }
                        stack.push(inNeighbor);
                    }
                }
            }
            return false;
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Find the shortest path between two nodes. This is a direct
     * implementation of Dijkstra's algorithm.
     * @link https://en.wikipedia.org/wiki/Dijkstra's_algorithm
     *
     * @param src
     * @param dest
     * @return shortest path of nodes from src to dest
     */
    public List<Node> findShortestPath(Node src, Node dest) {
        final Map<Node, Node> prevMap = new HashMap<>();

        readLock.lock();
        try {
            if (!containsNode(src) || !containsNode(dest)) {
                return new ArrayList<>();
            }

            // Initialize the maps with the starting node
            final Map<Node, Double> distance = new HashMap<>();
            distance.put(src, new Double(0));
            prevMap.put(src, null);

            for (final Node node : outgoing.keySet()) {
                if (node != src) {
                    distance.put(node, Double.POSITIVE_INFINITY);
                }
            }

            final PriorityQueue<Node> pq = new PriorityQueue<>();
            pq.add(src);
            while (!pq.isEmpty()) {
                final Node u = pq.remove();

                // Done. Found the destination node.
                if (u == dest) {
                    break;
                }

                // For every neighbor v of u
                for (final Node v : outgoing.get(u).keySet()) {
                    double altDistance = distance.get(u) + getEdge(u, v).getWeight();

                    // Shorter distance found. Add to the path.
                    if (altDistance < distance.get(v)) {
                        distance.put(v, altDistance);
                        prevMap.put(v, u);
                        pq.add(v);
                    }
                }
            }
        }
        finally {
            readLock.unlock();
        }
        return buildShortestPath(prevMap, dest);
    }

    private List<Node> buildShortestPath(Map<Node, Node> prevMap, Node dest) {
        if (MapUtils.isEmpty(prevMap)) {
            return new ArrayList<>();
        }

        // Construct the shortest path in reverse, starting
        // with the destination node and working its way back
        // to the starting node using the previous map (prevMap).
        // Use a deque to reverse the order of the nodes.
        final Deque<Node> deque = new LinkedList<>();
        while (prevMap.containsKey(dest)) {
            deque.addFirst(dest);
            dest = prevMap.get(dest);
        }
        return new ArrayList<>(deque);
    }

    /**
     * Add a new, unconnected vertex to the graph
     *
     * @param node Vertex to add to the graph
     * @return true if vertex was added successfully to graph, false otherwise
     */
    public boolean addNode(Node node) {
        if (node == null) {
            return false;
        }

        writeLock.lock();
        try {
            if (outgoing.containsKey(node)) {
                return false;
            }

            // Add the node to the graph but it has no connections
            outgoing.put(node, new HashMap<>());
            incoming.put(node, new HashMap<>());
            return true;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Remove a vertex from the graph
     *
     * @param node to remove from the graph
     */
    public void removeNode(Node node) {
        if (node == null) {
            return;
        }

        writeLock.lock();
        try {
            if (!outgoing.containsKey(node)) {
                return;
            }

            // Remove the edges that connect the neighbors to this node
            // It's important to iterate using a copy of incoming edges
            // since we're removing edges while iterating.
            final Map<Node, Edge> neighbors = getIncomingEdges(node);
            for (final Node neighbor : neighbors.keySet()) {
                removeEdge(neighbor, node);
            }
            outgoing.remove(node);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Connect two vertices in the graph, if no current connection exists.
     * This method could be used to create two vertices and the edge between them.
     *
     * @param src the starting node
     * @param dest the ending node that completes the edge
     * @param weight value given to this edge
     * @return true if an edge was established between two vertices, false otherwise.
     */
    public boolean addEdge(Node src, Node dest, String label, double weight) {
        if (src == null || dest == null) {
            return false;
        }

        writeLock.lock();
        try {
            // Vertices need to be in the graph for an edge to exist between them
            if (!outgoing.containsKey(src)) {
                addNode(src);
            }
            if (!outgoing.containsKey(dest)) {
                addNode(dest);
            }

            // This is the edge information
            final Edge edge = new Edge(label, weight);

            // Keep track of the edge
            outgoing.get(src).put(dest, edge);

            // Also keep track of the vertex that is pointing to the 'src' vertex.
            // This is so we can easily tell what vertices directly point to a certain vertex.
            incoming.get(dest).put(src, edge);
            return true;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes an edge from the graph
     *
     * @param src starting vertex of edge
     * @param dest ending vertex of edge
     */
    public void removeEdge(Node src, Node dest) {
        if (src == null || dest == null) {
            return;
        }

        writeLock.lock();
        try {
            // Vertices need to be in the graph for an edge to exist between them
            if (!outgoing.containsKey(src) || !outgoing.containsKey(dest)) {
                return;
            }
            outgoing.get(src).remove(dest);
            incoming.get(dest).remove(src);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets all the vertices in the graph
     *
     * @return a copy of the set of vertices
     */
    public Set<Node> getNodes() {
        readLock.lock();
        try {
            return new HashSet<>(outgoing.keySet());
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Find all the adjacent outgoing edges from 'node'
     *
     * @param node vertex to find outgoing edges for
     * @return a copy of the set of vertices
     */
    public Map<Node, Edge> getOutgoingEdges(Node node) {
        readLock.lock();
        try {
            return new HashMap<>(outgoing.get(node));
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Find all the incoming edges pointing to vertex 'node'
     *
     * @param node vertex to find incoming edges for
     * @return a copy of the set of vertices
     */
    public Map<Node, Edge> getIncomingEdges(Node node) {
        readLock.lock();
        try {
            return new HashMap<>(incoming.get(node));
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * For debugging
     */
    public void printNodes() {
        readLock.lock();
        try {
            for (Map.Entry<Node, Map<Node, Edge>> vertex : outgoing.entrySet()) {
                System.out.println("Vertex " + vertex.getKey() + " points to:");
                for (Node v : vertex.getValue().keySet()) {
                    System.out.print(v + " ");
                }
                System.out.println();
            }
        }
        finally {
            readLock.unlock();
        }
    }

    public void printEdges() {
        readLock.lock();
        try {
            for (Map.Entry<Node, Map<Node, Edge>> vertex : outgoing.entrySet()) {
                System.out.println("Vertex " + vertex.getKey() + " has edges:");
                for (Map.Entry<Node, Edge> v : vertex.getValue().entrySet()) {
                    System.out.println(v.getValue().getLabel() + " (weight: " + v.getValue().getWeight() + ")");
                }
                System.out.println();
            }
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Write graph to file
     *
     * This is implemented to work with Graph<Integer> only.
     * Notice that it is not part of the parent Graph interface.
     *
     * @param graph
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void write(Graph<Integer> graph, final String file) throws FileNotFoundException, IOException {
        GraphProtos.Graph.Builder graphBuilder = GraphProtos.Graph.newBuilder();
        Set<Integer> nodes = graph.getNodes();
        for (Integer u : nodes) {
            Map<Integer, Edge> neighbors = graph.getOutgoingEdges(u);
            for (Map.Entry<Integer, Edge> v : neighbors.entrySet()) {
                GraphProtos.Edge.Builder edgeBuilder = GraphProtos.Edge.newBuilder();
                edgeBuilder.setFrom(u);
                edgeBuilder.setTo(v.getKey());
                edgeBuilder.setWeight(v.getValue().getWeight());
                edgeBuilder.setLabel(v.getValue().getLabel());
                graphBuilder.addEdge(edgeBuilder.build());
            }
        }
        FileOutputStream output = new FileOutputStream(file);
        graphBuilder.build().writeTo(output);
    }

    /**
     * Read graph from file
     *
     * This is implemented to work with Graph<Integer> only.
     * Notice that it is not part of the parent Graph interface.
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Graph<Integer> read(final String file) throws FileNotFoundException, IOException {
        Graph<Integer> graph = new DirectedGraph<>();
        GraphProtos.Graph g = GraphProtos.Graph.parseFrom(new FileInputStream(file));
        for (GraphProtos.Edge edge : g.getEdgeList()) {
            graph.addEdge(edge.getFrom(), edge.getTo(), edge.getLabel(), edge.getWeight());
        }
        return graph;
    }
}
