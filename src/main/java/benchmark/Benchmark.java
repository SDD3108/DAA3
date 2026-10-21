package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public final class Benchmark {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final long[][] REFERENCE = {
        {147970, 27, 396274},
        {30363, 1, 252411},
        {468911, 137, 1271911},
        {97589, 1, 723157},
        {1486139, 373, 3941828},
        {309550, 1, 2381342},
        {4715515, 1188, 12551891},
        {968517, 1, 7376612},
        {15012094, 3923, 40096539},
        {3056681, 1, 23512087}
    };
    private record Measurement(long value,Metrics metrics,double timeMs){
    }

    private static Measurement measure(Function<Metrics, Long> algorithm){
        long[] nanos = new long[5];
        long value = 0;
        Metrics first = null;
        for(int run = 0; run < 5; run++){
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            long current = algorithm.apply(metrics);
            nanos[run] = System.nanoTime() - start;

            if(run == 0){
                value = current;
                first = metrics;
            }
            else if(current != value || !metrics.counters().equals(first.counters())){
                throw new IllegalStateException("Non-reproducible result or counters");
            }
        }

        Arrays.sort(nanos);

        return new Measurement(value, first, nanos[2] / 1_000_000.0);
    }
    private static void row(PrintWriter out,String name,String graph,int n,int m,Measurement result){
        out.printf(Locale.ROOT,"%s,%s,%d,%d,%.4f,%s,%d%n",name,graph,n,m,result.timeMs(),result.metrics().counters(),result.value());
        out.flush();
    }
    private static void expect(long actual,long expected,String description){
        if(actual != expected){
            throw new IllegalStateException(description + ": expected " + expected + ", got " + actual);
        }
    }
    public static void main(String[] args) throws Exception {
        boolean quick = Arrays.asList(args).contains("--quick");
        Path output = Path.of(quick ? "target/quick-results.csv" : "results/results.csv");
        Files.createDirectories(output.getParent());

        try(PrintWriter csv = new PrintWriter(Files.newBufferedWriter(output))){
            csv.println("algorithm,graph,n,m,time_ms,dsu_ops," + "edge_scans,comparisons,max_depth,result");
            int index = 0;
            for (int n : SIZES) {
                for (int factor : new int[]{4, 20}) {
                    int m = factor * n;
                    String density = factor == 4 ? "sparse" : "dense";

                    Graph undirected = GraphGenerator.generate(n, m, GraphGenerator.Kind.UNDIRECTED);
                    Measurement kruskal = measure(metric -> Kruskal.run(undirected, metric).weight());
                    Measurement prim = measure(metric -> Prim.run(undirected, metric).weight());

                    expect(kruskal.value(), prim.value(), "MST agreement");
                    expect(kruskal.value(),REFERENCE[index][0],"Reference MST");
                    row(csv, "Kruskal", "undirected_" + density, n, m, kruskal);
                    row(csv, "Prim", "undirected_" + density, n, m, prim);

                    Graph directed = GraphGenerator.generate(n, m, GraphGenerator.Kind.DIRECTED);

                    Measurement kosaraju = measure(metric -> (long) Kosaraju.run(directed, metric).count());
                    expect(kosaraju.value(),REFERENCE[index][1],"Reference SCC");
                    expect(kosaraju.metrics().edgeScans,2L * m,"Kosaraju scans");

                    row(csv, "Kosaraju", "directed_" + density,n, m, kosaraju);

                    Graph dag = GraphGenerator.generate(n, m, GraphGenerator.Kind.DAG);
                    Kahn.Result order = Kahn.run(dag, new Metrics());

                    if(order.hasCycle() || !Kahn.isValid(dag, order.order())){
                        throw new IllegalStateException("Invalid topological order");
                    }
                    Measurement kahn = measure(metric -> {
                        Kahn.Result result = Kahn.run(dag, metric);
                        if(result.hasCycle()){
                            throw new IllegalStateException("Generated graph has a cycle");
                        }
                        return (long) result.order().size();
                    });

                    Measurement paths = measure(metric -> DagShortestPath.run(dag, metric).sum());
                    expect(kahn.value(), n, "Kahn order size");
                    expect(kahn.metrics().edgeScans, m, "Kahn scans");
                    expect(paths.metrics().edgeScans,m,"DAG relaxation scans");
                    expect(paths.value(),REFERENCE[index][2],"Reference DAG distances");
                    row(csv, "Kahn", "dag_" + density, n, m, kahn);
                    row(csv, "DagShortestPath", "dag_" + density, n, m, paths);
                    System.out.printf("%d / %d: MST=%d SCC=%d DAG=%d%n",n, m,kruskal.value(),kosaraju.value(),paths.value());
                    index++;
                }

                if(quick){
                    break;
                }
            }
        }
        System.out.println("Validated benchmark written to " + output);
    }
}