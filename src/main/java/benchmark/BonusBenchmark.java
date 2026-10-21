package benchmark;
import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class BonusBenchmark {

    public static void main(String[] args) throws Exception{
        Files.createDirectories(Path.of("results"));
        try(PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of("results/bonus.csv")))){
            out.println("n,m,kosaraju_scans,tarjan_scans,components," + "condensation_edges,critical_path_weight");

            for(int n : new int[]{1000, 3162, 10000, 31623, 100000}){
                for(int factor : new int[]{4, 20}){
                    int m = n * factor;

                    Graph graph = GraphGenerator.generate(n, m, GraphGenerator.Kind.DIRECTED);

                    Metrics km = new Metrics();
                    Metrics tm = new Metrics();

                    SccResult k = Kosaraju.run(graph, km);
                    SccResult t = Tarjan.run(graph, tm);

                    if (k.count() != t.count()) {
                        throw new IllegalStateException("SCC counts differ");
                    }

                    int[] map = new int[k.count()];
                    int[] reverse = new int[t.count()];

                    Arrays.fill(map, -1);
                    Arrays.fill(reverse, -1);

                    for(int v = 0; v < n; v++){
                        int a = k.componentOf(v);
                        int b = t.componentOf(v);

                        if((map[a] != -1 && map[a] != b) || (reverse[b] != -1 && reverse[b] != a)){
                            throw new IllegalStateException("SCC partitions differ");
                        }

                        map[a] = b;
                        reverse[b] = a;
                    }

                    if(km.edgeScans != 2L * m || tm.edgeScans != m){
                        throw new IllegalStateException("SCC scan count mismatch");
                    }

                    Graph dag = Condensation.build(graph, k);
                    Kahn.Result topology = Kahn.run(dag, new Metrics());

                    if(topology.hasCycle() || !Kahn.isValid(dag, topology.order())){
                        throw new IllegalStateException("Condensation has a cycle");
                    }
                    out.printf("%d,%d,%d,%d,%d,%d,%d%n",n,m,km.edgeScans,tm.edgeScans,k.count(),dag.edges().size(),Condensation.longestPath(dag).weight());
                }
            }
        }

        System.out.println("Validated Tarjan partitions and condensation graphs " + "for all 10 datasets");
    }
}