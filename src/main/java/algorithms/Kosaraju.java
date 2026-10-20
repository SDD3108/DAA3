package algorithms;

import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;

public final class Kosaraju {
    private Kosaraju(){
    }

    public static SccResult run(Graph graph, Metrics metrics){
        if(!graph.directed()){
            throw new IllegalArgumentException("SCC needs a directed graph");
        }

        int n = graph.size();
        int finished = 0;
        boolean[] seen = new boolean[n];
        int[] order = new int[n];
        int[] stack = new int[n];
        int[] next = new int[n];

        for(int start = 0; start < n; start++){
            if(seen[start]){
                continue;
            }
            int depth = 1;
            stack[0] = start;
            seen[start] = true;
            metrics.depth(depth);

            while (depth > 0) {
                int v = stack[depth - 1];
                if(next[v] < graph.adjacent(v).size()){
                    int to = graph.adjacent(v).get(next[v]++).to();
                    metrics.edgeScans++;
                    if(!seen[to]){
                        seen[to] = true;
                        stack[depth++] = to;
                        metrics.depth(depth);
                    }
                }
                else{
                    depth--;
                    order[finished++] = v;
                }
            }
        }

        Graph reverse = graph.reversed();
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int count = 0;

        for(int i = n - 1; i >= 0; i--){
            int start = order[i];

            if (component[start] != -1){
                continue;
            }
            int depth = 1;
            stack[0] = start;
            component[start] = count;
            metrics.depth(depth);

            while(depth > 0){
                int v = stack[--depth];
                for(Graph.Edge edge : reverse.adjacent(v)){
                    metrics.edgeScans++;
                    if(component[edge.to()] == -1){
                        component[edge.to()] = count;
                        stack[depth++] = edge.to();
                        metrics.depth(depth);
                    }
                }
            }
            count++;
        }

        return new SccResult(component, count);
    }
}