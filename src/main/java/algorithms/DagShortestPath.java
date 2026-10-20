package algorithms;

import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DagShortestPath {
    public static final long INF = Long.MAX_VALUE;

    public static final class Result{
        private final long[] distance;
        private final int[] parent;

        private Result(long[] distance, int[] parent){
            this.distance = distance;
            this.parent = parent;
        }
        public long distanceTo(int v){
            return distance[v];
        }
        public long sum(){
            long sum = 0;
            for (long value : distance) {
                sum += value;
            }
            return sum;
        }
        public List<Integer> pathTo(int v) {
            List<Integer> path = new ArrayList<>();
            for(int current = v; current != -1; current = parent[current]){
                path.add(current);
            }
            Collections.reverse(path);
            return List.copyOf(path);
        }
    }
    private DagShortestPath(){
    }
    public static Result run(Graph graph, Metrics metrics) {
        Kahn.Result topology = Kahn.run(graph, new Metrics());

        if(topology.hasCycle()){
            throw new IllegalArgumentException("Shortest paths require a DAG");
        }

        long[] distance = new long[graph.size()];
        Arrays.fill(distance, INF);
        int[] parent = new int[graph.size()];
        Arrays.fill(parent, -1);

        for(int v = 0; v < graph.size(); v++){
            if(graph.indegree(v) == 0){
                distance[v] = 0;
            }
        }

        for(int v : topology.order()){
            for(Graph.Edge edge : graph.adjacent(v)){
                metrics.edgeScans++;
                long candidate = distance[v] + edge.weight();
                if(candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = v;
                }
            }
        }

        return new Result(distance, parent);
    }
}