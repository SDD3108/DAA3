package algorithms;

import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;

public final class Tarjan {
    private Tarjan() {}

    public static SccResult run(Graph graph, Metrics metrics){
        if(!graph.directed()){
            throw new IllegalArgumentException("SCC needs a directed graph");
        }

        int n = graph.size();
        int clock = 0;
        int count = 0;
        int activeSize = 0;
        int[] index = new int[n];
        int[] low = new int[n];
        int[] parent = new int[n];
        int[] next = new int[n];
        int[] frames = new int[n];
        int[] active = new int[n];
        int[] component = new int[n];
        boolean[] onStack = new boolean[n];
        Arrays.fill(index, -1);
        Arrays.fill(parent, -1);

        for(int start = 0; start < n; start++){
            if(index[start] != -1){
                continue;
            }

            int depth = 1;
            frames[0] = start;
            index[start] = low[start] = clock++;
            active[activeSize++] = start;
            onStack[start] = true;
            metrics.depth(depth);
            while (depth > 0) {
                int v = frames[depth - 1];
                if(next[v] < graph.adjacent(v).size()){
                    int to = graph.adjacent(v).get(next[v]++).to();
                    metrics.edgeScans++;
                    if(index[to] == -1){
                        parent[to] = v;
                        index[to] = low[to] = clock++;
                        frames[depth++] = to;
                        active[activeSize++] = to;
                        onStack[to] = true;
                        metrics.depth(depth);
                    }
                    else if(onStack[to]){
                        low[v] = Math.min(low[v], index[to]);
                    }
                }
                else{
                    depth--;
                    if(parent[v] != -1){
                        low[parent[v]] = Math.min(low[parent[v]], low[v]);
                    }
                    if(low[v] == index[v]){
                        int member;
                        do{
                            member = active[--activeSize];
                            onStack[member] = false;
                            component[member] = count;
                        } while (member != v);

                        count++;
                    }
                }
            }
        }

        return new SccResult(component, count);
    }
}