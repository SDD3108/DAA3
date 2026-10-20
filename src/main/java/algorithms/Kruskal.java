package algorithms;

import graph.Graph;
import graph.Graph.Edge;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

public final class Kruskal {
    private Kruskal() {}

    public static MstResult run(Graph graph, Metrics metrics) {
        if(graph.directed()){
            throw new IllegalArgumentException("MST needs an undirected graph");
        }

        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        DSU dsu = new DSU(graph.size(), metrics);
        List<Edge> chosen = new ArrayList<>();
        long weight = 0;

        for(Edge edge : sorted){
            if (dsu.components() <= 1){
                break;
            }
            metrics.edgeScans++;

            if(dsu.union(edge.from(),edge.to())){
                chosen.add(edge);
                weight += edge.weight();
            }
        }

        return new MstResult(weight, chosen, dsu.components());
    }
}