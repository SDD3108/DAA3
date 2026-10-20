import algorithms.DSU;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MstTest {
    @Test
    void algorithmsAgreeOnOneHundredConnectedGraphs(){
        Random random = new Random(123);
        for (int trial = 0; trial < 100; trial++){
            int n = 2 + random.nextInt(50);
            Graph graph = new Graph(n, false);
            for(int i = 1; i < n; i++){
                graph.addEdge(i,random.nextInt(i),random.nextInt(101) - 50);
            }
            for(int i = 0; i < 4 * n; i++){
                graph.addEdge(random.nextInt(n),random.nextInt(n),random.nextInt(101) - 50);
            }

            MstResult kruskal = Kruskal.run(graph, new Metrics());
            MstResult prim = Prim.run(graph, new Metrics());

            assertEquals(kruskal.weight(), prim.weight());
            assertEquals(n - 1, kruskal.edges().size());
            assertEquals(n - 1, prim.edges().size());
            assertEquals(1, kruskal.components());
            assertEquals(1, prim.components());
        }
    }

    @Test
    void disconnectedFixtureReturnsForest() throws Exception {
        Graph graph = Graph.read(Path.of("data/disconnected.txt"));
        MstResult[] results = {
            Kruskal.run(graph, new Metrics()),
            Prim.run(graph, new Metrics())
        };

        for(MstResult result : results){
            assertEquals(8, result.weight());
            assertEquals(3, result.components());
            assertEquals(3, result.edges().size());
        }
    }

    @Test
    void agreesWithExhaustiveSpanningTrees(){
        Random random = new Random(44);

        for (int trial = 0; trial < 30; trial++) {
            Graph graph = new Graph(5, false);
            for(int v = 1; v < 5; v++){
                graph.addEdge(v - 1, v, random.nextInt(15) - 7);
            }
            for(int e = 0; e < 4; e++){
                graph.addEdge(random.nextInt(5),random.nextInt(5),random.nextInt(15) - 7);
            }

            long best = Long.MAX_VALUE;
            int edgeCount = graph.edges().size();

            for(int mask = 0; mask < (1 << edgeCount); mask++){
                if(Integer.bitCount(mask) != 4){
                    continue;
                }

                DSU dsu = new DSU(5);
                long sum = 0;

                for(int e = 0; e < edgeCount; e++){
                    if((mask & (1 << e)) != 0){
                        Graph.Edge edge = graph.edges().get(e);
                        dsu.union(edge.from(),edge.to());
                        sum += edge.weight();
                    }
                }

                if(dsu.components() == 1){
                    best = Math.min(best, sum);
                }
            }

            assertEquals(best, Kruskal.run(graph, new Metrics()).weight());
            assertEquals(best, Prim.run(graph, new Metrics()).weight());
        }
    }

    @Test
    void emptyGraphsAndLongWeights(){
        assertEquals(0,Kruskal.run(new Graph(0, false), new Metrics()).components());
        assertEquals(0,Prim.run(new Graph(0, false), new Metrics()).weight());

        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1, Integer.MAX_VALUE);
        graph.addEdge(1, 2, Integer.MAX_VALUE);
        long expected = 2L * Integer.MAX_VALUE;

        assertEquals(expected, Kruskal.run(graph, new Metrics()).weight());
        assertEquals(expected, Prim.run(graph, new Metrics()).weight());
        assertThrows(IllegalArgumentException.class,() -> Kruskal.run(new Graph(2, true), new Metrics()));
        assertThrows(IllegalArgumentException.class,() -> Prim.run(new Graph(2, true), new Metrics()));
    }
}