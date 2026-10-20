import algorithms.Kosaraju;
import algorithms.SccResult;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class SccTest {
    static boolean[][] reachability(Graph graph){
        boolean[][] reach = new boolean[graph.size()][graph.size()];
        for(int start = 0; start < graph.size(); start++){
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            reach[start][start] = true;
            while(!queue.isEmpty()){
                int v = queue.remove();
                for(Graph.Edge edge : graph.adjacent(v)){
                    if(!reach[start][edge.to()]){
                        reach[start][edge.to()] = true;
                        queue.add(edge.to());
                    }
                }
            }
        }

        return reach;
    }

    @Test
    void agreesWithBruteForceReachability(){
        Random random = new Random(88);
        for (int trial = 0; trial < 50; trial++){
            int n = 1 + random.nextInt(200);
            Graph graph = new Graph(n, true);
            for(int i = 0; i < n * 3; i++){
                graph.addEdge(random.nextInt(n), random.nextInt(n), 1);
            }

            Metrics metrics = new Metrics();
            SccResult result = Kosaraju.run(graph, metrics);
            boolean[][] reach = reachability(graph);

            int count = 0;
            for(int u = 0; u < n; u++){
                boolean first = true;
                for(int v = 0; v < n; v++){
                    boolean mutuallyReachable = reach[u][v] && reach[v][u];
                    assertEquals(mutuallyReachable,result.componentOf(u) == result.componentOf(v));
                    if(v < u && mutuallyReachable){
                        first = false;
                    }
                }
                if(first){
                    count++;
                }
            }
            assertEquals(count, result.count());
            assertEquals(2L * graph.edges().size(), metrics.edgeScans);
        }
    }

    @Test
    void pathOfOneHundredThousandIsStackSafe(){
        int n = 100_000;
        Graph graph = new Graph(n, true);
        for(int v = 1; v < n; v++){
            graph.addEdge(v - 1, v, 1);
        }
        Metrics metrics = new Metrics();
        SccResult result = Kosaraju.run(graph, metrics);
        assertEquals(n, result.count());
        assertEquals(n, metrics.maxDepth);
        assertEquals(2L * (n - 1), metrics.edgeScans);
    }

    @Test
    void cyclicFixtureAndEmptyGraph() throws Exception{
        Graph graph = Graph.read(Path.of("data/cyclic.txt"));
        SccResult result = Kosaraju.run(graph, new Metrics());

        assertEquals(3, result.count());
        assertEquals(result.componentOf(0), result.componentOf(1));
        assertEquals(result.componentOf(1), result.componentOf(2));
        assertEquals(result.componentOf(3), result.componentOf(4));
        assertNotEquals(result.componentOf(2), result.componentOf(3));
        assertNotEquals(result.componentOf(5), result.componentOf(0));
        assertNotEquals(result.componentOf(5), result.componentOf(3));
        assertEquals(0,Kosaraju.run(new Graph(0, true), new Metrics()).count());
        assertThrows(IllegalArgumentException.class,() -> Kosaraju.run(new Graph(2, false), new Metrics()));
    }
}