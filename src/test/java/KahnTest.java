import algorithms.Kahn;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KahnTest {

    @Test
    void validatesDagFixtureAndRejectsCycle() throws Exception {
        Graph dag = Graph.read(Path.of("data/dag.txt"));
        Metrics metrics = new Metrics();
        Kahn.Result result = Kahn.run(dag, metrics);

        assertFalse(result.hasCycle());
        assertTrue(Kahn.isValid(dag, result.order()));
        assertEquals(6, result.order().size());
        assertEquals(6, metrics.edgeScans);

        Graph cyclic = Graph.read(Path.of("data/cyclic.txt"));
        Kahn.Result cycle = Kahn.run(cyclic, new Metrics());

        assertTrue(cycle.hasCycle());
        assertTrue(cycle.order().isEmpty());
    }

    @Test
    void loopsParallelEdgesEmptyAndInvalidOrders() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 1, 2);

        assertEquals(List.of(0, 1),Kahn.run(graph, new Metrics()).order());

        assertFalse(Kahn.isValid(graph, List.of(0, 0)));
        assertFalse(Kahn.isValid(graph, List.of(1, 0)));
        assertFalse(Kahn.isValid(graph, List.of(0)));
        assertFalse(Kahn.isValid(graph, List.of(0, 2)));
        graph.addEdge(1, 1, 0);


        assertTrue(Kahn.run(graph, new Metrics()).hasCycle());

        Graph empty = new Graph(0, true);
        Kahn.Result emptyResult = Kahn.run(empty, new Metrics());

        assertFalse(emptyResult.hasCycle());
        assertTrue(emptyResult.order().isEmpty());
        assertTrue(Kahn.isValid(empty, emptyResult.order()));
        assertThrows(IllegalArgumentException.class,() -> Kahn.run(new Graph(2, false), new Metrics()));
    }
}