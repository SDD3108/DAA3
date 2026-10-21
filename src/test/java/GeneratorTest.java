import algorithms.*;
import benchmark.GraphGenerator;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    @Test
    void reproducesSmallReferenceValuesAndCounters() {
        int[] factors = {4, 20};
        long[] mst = {147970, 30363};
        long[] scc = {27, 1};
        long[] distances = {396274, 252411};

        for(int i = 0; i < factors.length; i++){
            int m = 1000 * factors[i];
            Graph undirected = GraphGenerator.generate(1000, m, GraphGenerator.Kind.UNDIRECTED);

            assertEquals(mst[i],Kruskal.run(undirected, new Metrics()).weight());
            assertEquals(mst[i],Prim.run(undirected, new Metrics()).weight());

            Graph directed = GraphGenerator.generate(1000, m, GraphGenerator.Kind.DIRECTED);
            Metrics sm = new Metrics();

            assertEquals(scc[i], Kosaraju.run(directed, sm).count());
            assertEquals(2L * m, sm.edgeScans);

            Graph dag = GraphGenerator.generate(1000, m, GraphGenerator.Kind.DAG);

            Metrics pm = new Metrics();
            Metrics km = new Metrics();

            assertEquals(distances[i],DagShortestPath.run(dag, pm).sum());
            assertEquals(m, pm.edgeScans);
            assertTrue(Kahn.isValid(dag, Kahn.run(dag, km).order()));
            assertEquals(m, km.edgeScans);
        }
    }
}