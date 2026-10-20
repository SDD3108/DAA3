import algorithms.DagShortestPath;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathTest {

    @Test
    void comparesWithBellmanFordOnRandomNegativeDags(){
        Random random = new Random(90);
        for(int trial = 0; trial < 100; trial++){
            int n = 2 + random.nextInt(40);
            Graph graph = new Graph(n, true);

            for(int e = 0; e < n * 4; e++){
                int a = random.nextInt(n);
                int b = random.nextInt(n);
                if(a == b){
                    continue;
                }
                graph.addEdge(Math.min(a, b),Math.max(a, b),random.nextInt(41) - 20);
            }

            long[] reference = new long[n];
            Arrays.fill(reference, Long.MAX_VALUE);

            for(int v = 0; v < n; v++){
                if(graph.indegree(v) == 0){
                    reference[v] = 0;
                }
            }
            for(int pass = 1; pass < n; pass++){
                for(Graph.Edge edge : graph.edges()){
                    if(reference[edge.from()] != Long.MAX_VALUE){
                        reference[edge.to()] = Math.min(reference[edge.to()],reference[edge.from()] + edge.weight());
                    }
                }
            }

            Metrics metrics = new Metrics();
            DagShortestPath.Result result = DagShortestPath.run(graph, metrics);
            for (int v = 0; v < n; v++) {
                assertEquals(reference[v], result.distanceTo(v));
                List<Integer> path = result.pathTo(v);
                assertEquals(0, graph.indegree(path.get(0)));
                assertEquals(v, path.get(path.size() - 1).intValue());
                long sum = 0;

                for(int i = 1; i < path.size(); i++){
                    int from = path.get(i - 1);
                    int to = path.get(i);
                    int weight = graph.adjacent(from).stream().filter(edge -> edge.to() == to).mapToInt(Graph.Edge::weight).min().orElseThrow();
                    sum += weight;
                }

                assertEquals(reference[v], sum);
            }

            assertEquals(graph.edges().size(), metrics.edgeScans);
        }
    }

    @Test
    void restoresFixturePathAndAllSources() throws Exception {
        Graph graph = Graph.read(Path.of("data/dag.txt"));

        DagShortestPath.Result result = DagShortestPath.run(graph, new Metrics());
        assertEquals(0, result.distanceTo(0));
        assertEquals(0, result.distanceTo(1));
        assertEquals(-2, result.distanceTo(2));
        assertEquals(1, result.distanceTo(3));
        assertEquals(5, result.distanceTo(4));
        assertEquals(0, result.distanceTo(5));
        assertEquals(List.of(1, 2, 3), result.pathTo(3));
        assertEquals(List.of(5), result.pathTo(5));
        assertEquals(4, result.sum());
    }

    @Test
    void rejectsCyclesAndAvoidsIntOverflow() {
        Graph cycle = new Graph(1, true);
        cycle.addEdge(0, 0, -1);

        assertThrows(IllegalArgumentException.class,() -> DagShortestPath.run(cycle, new Metrics()));
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, Integer.MIN_VALUE);
        graph.addEdge(1, 2, Integer.MIN_VALUE);
        assertEquals(2L * Integer.MIN_VALUE,DagShortestPath.run(graph, new Metrics()).distanceTo(2));
        assertEquals(0,DagShortestPath.run(new Graph(0, true), new Metrics()).sum());
    }
}