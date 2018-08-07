# Simple graph library
A little graph library that allows for
- Adding and removing vertices
- Adding and removing edges
- Query for existence of vertices
- Query for existance of edges
- Get all the adjacent vertices to a certain vertex
- Get all vertices that a vertex is adjacent to
- Check to see if two vertices are connected
- Find the shortest path between two vertices (Dijkstra's Algorithm)
- Synchronization using ReadWriteLock. Untested :(
- Serialization/deserialization using Google protocol buffer library.
- Unit tests. Run `mvn test`


### To build
Make sure Maven is installed and from the command line, run:
```
mvn clean install -U
```
Otherwise, import as Maven project using your favorite IDE, e.g. IntelliJ or Eclipse


### Generating a graph image

##### Creating a graph and writing to file in DOT format
```
  final Graph<Integer> graph = new DirectedGraph<>();
  graph.addEdge(1, 2, "label1", 4);
  graph.addEdge(1, 3, "label2", 2);
  graph.addEdge(2, 3, "label3", 5);
  graph.addEdge(2, 4, "label4", 10);
  graph.addEdge(3, 5, "label5", 3);
  graph.addEdge(5, 4, "label6", 4);
  graph.addEdge(4, 6, "label7", 11);

  try (BufferedWriter br = new BufferedWriter(new FileWriter(new File("graph.dot")))) {
    br.write(graph.toString());
  }
```

##### Generate an image from the DOT file
```
dot -Tpng graph.dot -o graph.png
```

![Graph image](/images/graph.png)


### Install Graphviz
In order to be able to convert a DOT text file to some image format such as PNG, Graphviz needs to be installed. **This is optional**.
```
brew install -v graphviz
```

