import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BonusTest {
    @Test
    void tarjanMatchesKosarajuAndCondensationIsADag(){
        Random random = new Random(91);

        for(int trial = 0; trial < 100; trial++){
            int n = 1 + random.nextInt(100);
            Graph graph = new Graph(n, true);

            for(int i = 0; i < 3 * n; i++){
                graph.addEdge(random.nextInt(n),random.nextInt(n),random.nextInt(50));
            }

            Metrics metrics = new Metrics();
            SccResult tarjan = Tarjan.run(graph, metrics);
            SccResult kosaraju = Kosaraju.run(graph, new Metrics());

            assertEquals(kosaraju.count(), tarjan.count());
            assertEquals(graph.edges().size(), metrics.edgeScans);

            for(int a = 0; a < n; a++){
                for(int b = 0; b < n; b++){
                    assertEquals(kosaraju.componentOf(a) == kosaraju.componentOf(b),tarjan.componentOf(a) == tarjan.componentOf(b));
                }
            }

            Graph dag = Condensation.build(graph, tarjan);
            Kahn.Result order = Kahn.run(dag, new Metrics());

            assertFalse(order.hasCycle());
            assertTrue(Kahn.isValid(dag, order.order()));
        }
    }

    @Test
    void tarjanHandlesDeepPathAndCycle() {
        Graph graph = new Graph(100000, true);
        for(int v = 1; v < graph.size(); v++){
            graph.addEdge(v - 1, v, 1);
        }

        Metrics metrics = new Metrics();
        assertEquals(100000, Tarjan.run(graph, metrics).count());
        assertEquals(100000, metrics.maxDepth);

        graph.addEdge(99999, 0, 1);
        assertEquals(1, Tarjan.run(graph, new Metrics()).count());
    }

    @Test
    void fixtureCondensationAndCriticalPath() throws Exception {
        Graph graph = Graph.read(Path.of("data/cyclic.txt"));
        Graph dag = Condensation.build(graph,Kosaraju.run(graph, new Metrics()));

        assertEquals(3, dag.size());
        assertEquals(1, dag.edges().size());
        assertEquals(4, Condensation.longestPath(dag).weight());

        Graph jobs = new Graph(4, true);
        jobs.addEdge(0, 1, 5);
        jobs.addEdge(0, 2, 2);
        jobs.addEdge(1, 3, 7);
        jobs.addEdge(2, 3, 20);
        assertEquals(new Condensation.CriticalPath(22, List.of(0, 2, 3)),Condensation.longestPath(jobs));

        jobs.addEdge(3, 0, 1);
        assertThrows(IllegalArgumentException.class,() -> Condensation.longestPath(jobs));
    }

    @Test
    void condensationKeepsMaximumParallelWeightAndLongNegation() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 1, 9);
        graph.addEdge(1, 2, Integer.MAX_VALUE);

        Graph condensed = Condensation.build(graph,Tarjan.run(graph, new Metrics()));
        assertEquals(2, condensed.edges().size());
        assertEquals(9L + Integer.MAX_VALUE,Condensation.longestPath(condensed).weight());

        Graph negative = new Graph(2, true);
        negative.addEdge(0, 1, Integer.MIN_VALUE);
        assertEquals(0, Condensation.longestPath(negative).weight());
        assertEquals(0,Condensation.longestPath(new Graph(0, true)).weight());
    }
}