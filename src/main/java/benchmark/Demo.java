package benchmark;
import algorithms.*;
import graph.Graph;
import metrics.Metrics;
import java.nio.file.Path;

public final class Demo {
    public static void main(String[] args) throws Exception {
        Path input = Path.of(args.length == 0 ? "data/dag.txt" : args[0]);
        Graph graph = Graph.read(input);

        if(!graph.directed()){
            System.out.println("Kruskal: " + Kruskal.run(graph, new Metrics()));
            System.out.println("Prim: " + Prim.run(graph, new Metrics()));
            return;
        }
        SccResult components = Kosaraju.run(graph, new Metrics());
        System.out.println("SCC count: " + components.count());

        for(int v = 0; v < graph.size(); v++){
            System.out.println(v + " -> component " + components.componentOf(v));
        }
        Kahn.Result topology = Kahn.run(graph, new Metrics());

        if(topology.hasCycle()) {
            System.out.println("Cycle detected: no topological order or DAG shortest paths");
        }
        else{
            System.out.println("Topological order: " + topology.order());
            DagShortestPath.Result paths = DagShortestPath.run(graph, new Metrics());
            for(int v = 0; v < graph.size(); v++){
                System.out.println(v + ": distance=" + paths.distanceTo(v) + ", path=" + paths.pathTo(v));
            }
        }
        Graph condensed = Condensation.build(graph, components);

        System.out.println("Condensation critical path: " + Condensation.longestPath(condensed));
    }
}