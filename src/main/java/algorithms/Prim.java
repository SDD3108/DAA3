package algorithms;

import graph.Graph;
import graph.Graph.Edge;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Prim {
    private Prim() {}
    public static MstResult run(Graph graph, Metrics metrics){
        if(graph.directed()){
            throw new IllegalArgumentException("MST needs an undirected graph");
        }

        boolean[] visited = new boolean[graph.size()];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        List<Edge> chosen = new ArrayList<>();
        long weight = 0;
        int components = 0;

        for (int start = 0; start < graph.size(); start++){
            if(visited[start]){
                continue;
            }
            components++;
            queue.add(new Edge(-1, start, 0));

            while(!queue.isEmpty()){
                Edge best = queue.remove();
                int v = best.to();
                if(visited[v]){
                    continue;
                }
                visited[v] = true;

                if(best.from() != -1){
                    chosen.add(best);
                    weight += best.weight();
                }
                for(Edge edge : graph.adjacent(v)){
                    metrics.edgeScans++;

                    if(!visited[edge.to()]){
                        queue.add(edge);
                    }
                }
            }
        }

        return new MstResult(weight, chosen, components);
    }
}