package com.panduit.graph;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class DirectedGraphTests {
    private Graph<Integer> graph;

    private void buildBasicGraph() {
        assertTrue(graph.addEdge(1, 2, "1 -> 2", 3.0));
        assertTrue(graph.addEdge(2, 3, "2 -> 3", 1.0));
        assertTrue(graph.addEdge(3, 1, "3 -> 1", 2.0));
    }

    private void buildComplexGraph() {
        // Add edges. Each node points to each other
        assertTrue(graph.addEdge(1, 2, "1 -> 2", 3.0));
        assertTrue(graph.addEdge(2, 1, "2 -> 1", 4.0));
        assertTrue(graph.addEdge(2, 3, "2 -> 3", 1.0));
        assertTrue(graph.addEdge(3, 2, "3 -> 2", 2.0));
        assertTrue(graph.addEdge(3, 1, "3 -> 1", 2.0));
        assertTrue(graph.addEdge(1, 3, "1 -> 3", 2.0));
    }

    private void buildDAG() {
        for (int i = 1; i <= 9; ++i) {
            assertTrue(graph.addNode(i));
        }

        graph.addEdge(1, 7, "1 -> 7", 9);
        graph.addEdge(3, 4, "3 -> 4", 2);
        graph.addEdge(4, 2, "4 -> 2", 5);
        graph.addEdge(5, 7, "5 -> 7", 1);
        graph.addEdge(6, 5, "6 -> 5", 3);
        graph.addEdge(7, 2, "7 -> 2", 5);

        // Nodes 8 and 9 are disconnected
        graph.addEdge(8, 9, "8 -> 9", 8);
    }

    private void buildGraphForShortestPath() {
        graph.addEdge(1, 2, "label1", 4);
        graph.addEdge(1, 3, "label2", 2);
        graph.addEdge(2, 3, "label3", 5);
        graph.addEdge(2, 4, "label4", 10);
        graph.addEdge(3, 5, "label5", 3);
        graph.addEdge(5, 4, "label6", 4);
        graph.addEdge(4, 6, "label7", 11);
    }

    @BeforeMethod
    public void beforeTest() {
        // Create an empty graph before every test
        graph = new DirectedGraph<Integer>();
    }

    @Test
    public void testAddNode() {
        // Add even values
        for (int i = 0; i <= 10; i += 2) {
            graph.addNode(i);
        }
        // Contains evens
        for (int i = 0; i <= 10; i += 2) {
            assertTrue(graph.containsNode(i));
        }
        // Does not contain odds
        for (int i = 1; i <= 10; i += 2) {
            assertFalse(graph.containsNode(i));
        }
    }

    @Test
    public void testAddNodeDuplicateNode() {
        assertTrue(graph.addNode(17));
        assertTrue(graph.containsNode(17));
        assertFalse(graph.addNode(17));
    }

    @Test
    public void testAddNodeNullNode() {
        graph.addNode(null);

        // null not added to the graph
        assertFalse(graph.containsNode(null));
    }

    @Test
    public void testRemoveNode() {
        for (int i = 0; i <= 10; ++i) {
            graph.addNode(i);
        }
        // Remove evens
        for (int i = 0; i <= 10; i += 2) {
            graph.removeNode(i);
        }
        // Does not contain evens
        for (int i = 0; i <= 10; i += 2) {
            assertFalse(graph.containsNode(i));
        }
        // Contain odds
        for (int i = 1; i <= 10; i += 2) {
            assertTrue(graph.containsNode(i));
        }
    }

    @Test
    public void testRemoveNodeNullNode() {
        assertFalse(graph.addNode(null));
        assertFalse(graph.containsNode(null));

        graph.removeNode(null);
        assertFalse(graph.containsNode(null));
    }

    @Test
    public void testRemoveNodeNonExistent() {
        assertFalse(graph.containsNode(15));

        // Remove a non-existent vertex
        graph.removeNode(15);

        // Still doesn't exist
        assertFalse(graph.containsNode(15));
    }

    @Test
    public void testAddEdge() {
        buildBasicGraph();
    }

    @Test
    public void testRemoveEdge() {
        buildBasicGraph();

        // Remove an edge
        graph.removeEdge(1, 2);
        assertFalse(graph.containsEdge(1, 2));
        assertTrue(graph.containsEdge(2, 3));
        assertTrue(graph.containsEdge(3, 1));

        // Remove the remaining edges
        graph.removeEdge(2, 3);
        graph.removeEdge(3, 1);

        assertFalse(graph.containsEdge(1, 2));
        assertFalse(graph.containsEdge(2, 3));
        assertFalse(graph.containsEdge(3, 1));
    }

    @Test
    public void testRemoveEdgesByDeletingVertices() {
        buildBasicGraph();
        graph.removeNode(1);

        assertFalse(graph.containsEdge(1, 2));
        assertTrue(graph.containsEdge(2, 3));
        assertFalse(graph.containsEdge(3, 1));
    }

    @Test
    public void testRemoveEdgesInvalidInput() {
        buildBasicGraph();
        graph.removeEdge(1, 5);
        graph.removeEdge(null, null);
        graph.removeEdge(2, null);
        graph.removeEdge(null, 3);

        // Nothing should have been affected in the graph
        assertTrue(graph.containsEdge(1, 2));
        assertTrue(graph.containsEdge(2, 3));
        assertTrue(graph.containsEdge(3, 1));
    }

    @Test
    public void testRemoveNodeWithInOutEdges() {
        buildComplexGraph();
        graph.removeNode(2);

        // Check vertices
        assertTrue(graph.containsNode(1));
        assertFalse(graph.containsNode(2));
        assertTrue(graph.containsNode(3));

        // Check for existing edges
        assertTrue(graph.containsEdge(1, 3));
        assertTrue(graph.containsEdge(3, 1));

        // Verify edges to/from the deleted vertex no longer exist
        assertFalse(graph.containsEdge(1, 2));
        assertFalse(graph.containsEdge(2, 1));
        assertFalse(graph.containsEdge(2, 3));
        assertFalse(graph.containsEdge(3, 2));
    }

    @Test
    public void testGetNodes() {
        buildComplexGraph();
        Set<Integer> vertices = graph.getNodes();
        for (Integer vertex : vertices) {
            assertTrue(graph.containsNode(vertex));
        }
    }

    @Test
    public void testGetOutgoingEdges() {
        buildComplexGraph();
        Map<Integer, Edge> vertices = graph.getOutgoingEdges(2);
        for (Integer vertex : vertices.keySet()) {
            assertTrue(graph.containsEdge(2, vertex));
        }
    }

    @Test
    public void testGetIncomingEdges() {
        buildBasicGraph();
        Map<Integer, Edge> vertices = graph.getIncomingEdges(3);
        for (Integer vertex : vertices.keySet()) {
            assertTrue(graph.containsEdge(vertex, 3));
        }
    }

    @Test
    public void testVerifyEdgesAfterRemovingEdge() {
        buildBasicGraph();
        graph.removeEdge(3, 1);

        // Check the incoming edges
        Map<Integer, Edge> incomingVertices = graph.getIncomingEdges(1);
        assertTrue(incomingVertices.isEmpty());

        // Check the outgoing edges
        Map<Integer, Edge> outgoingVertices = graph.getOutgoingEdges(3);
        assertTrue(outgoingVertices.isEmpty());
    }

    @Test
    public void testIsConnectedAdjacent() {
        buildDAG();
        // Check connection of adjacent nodes
        assertTrue(graph.isConnected(1, 7));
        assertTrue(graph.isConnected(3, 4));
        assertTrue(graph.isConnected(4, 2));
        assertTrue(graph.isConnected(5, 7));
        assertTrue(graph.isConnected(6, 5));
        assertTrue(graph.isConnected(7, 2));
        assertTrue(graph.isConnected(8, 9));
    }

    @Test
    public void testIsConnectedAdjacentReverseDirection() {
        buildDAG();
        // Check adjacent connection in reverse direction
        assertTrue(graph.isConnected(7, 1));
        assertTrue(graph.isConnected(4, 3));
        assertTrue(graph.isConnected(2, 4));
        assertTrue(graph.isConnected(7, 5));
        assertTrue(graph.isConnected(5, 6));
        assertTrue(graph.isConnected(2, 7));
        assertTrue(graph.isConnected(9, 8));
    }

    @Test
    public void testIsConnectedTwoDegreesSeparation() {
        buildDAG();
        // Check connections with two degrees of separation
        assertTrue(graph.isConnected(6, 7));
        assertTrue(graph.isConnected(1, 2));
        assertTrue(graph.isConnected(5, 2));
        assertTrue(graph.isConnected(3, 2));

        assertTrue(graph.isConnected(7, 6));
        assertTrue(graph.isConnected(2, 1));
        assertTrue(graph.isConnected(2, 5));
        assertTrue(graph.isConnected(2, 3));
    }

    @Test
    public void testIsConnectedUndirected() {
        buildDAG();
        // Check general connections
        assertTrue(graph.isConnected(6, 1));
        assertTrue(graph.isConnected(3, 1));
        assertTrue(graph.isConnected(5, 4));
        assertTrue(graph.isConnected(3, 6));
        assertTrue(graph.isConnected(8, 9));
    }

    @Test
    public void testIsConnectedNotConnected() {
        buildDAG();
        // Check general connections
        for (int i = 1; i <= 7; ++i) {
            assertFalse(graph.isConnected(i, 8));
            assertFalse(graph.isConnected(8, i));
            assertFalse(graph.isConnected(i, 9));
            assertFalse(graph.isConnected(9, i));
        }
        System.out.println(graph.toString());
    }

    @Test
    public void testFindShortestPath() {
        buildGraphForShortestPath();
        List<Integer> path = graph.findShortestPath(1, 6);
        assertEquals(path, ImmutableList.of(1, 3, 5, 4, 6));

        // Invalid path. No such directed path from node 6 to node 1
        assertEquals(graph.findShortestPath(6, 1), ImmutableList.of());
        System.out.println(graph.toString());
    }

    @Test
    public void testGraphSerialization() throws IOException {
        buildGraphForShortestPath();

        final String fileName = "graph.data";

        // Write to file
        DirectedGraph.write(graph, fileName);

        // Read from file
        Graph<Integer> g = DirectedGraph.read(fileName);

        // Verify graph
        assertEquals(g.findShortestPath(1, 6), ImmutableList.of(1, 3, 5, 4, 6));
    }
}
