import graph.Graph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void supportsLoopsParallelEdgesAndIsolatedVertices() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, -2);
        graph.addEdge(2, 2, 9);

        assertEquals(3, graph.edges().size());
        assertEquals(2, graph.adjacent(1).size());
        assertEquals(2, graph.adjacent(2).size());
        assertTrue(graph.adjacent(3).isEmpty());
        assertThrows(IllegalArgumentException.class,() -> graph.addEdge(4, 0, 1));
        assertThrows(UnsupportedOperationException.class,() -> graph.edges().clear());
    }

    @Test
    void parsesAndReverses(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("graph.txt");
        Files.writeString(file, "3 2 directed\n0 1 7\n1 2 -3\n");
        Graph graph = Graph.read(file);

        assertTrue(graph.directed());
        assertEquals(1, graph.indegree(2));
        assertEquals(1, graph.reversed().adjacent(2).get(0).to());

        Files.writeString(file, "3 2 directed\n0 1 7\n");
        assertThrows(IllegalArgumentException.class,() -> Graph.read(file));
    }
}