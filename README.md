### Simple graph library
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


### To run
Make sure Maven is installed and run `mvn clean install -U` from the command line. Otherwise, import as Maven project using your favorite IDE, e.g. IntelliJ or Eclipse
