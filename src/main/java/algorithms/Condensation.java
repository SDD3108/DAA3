package algorithms;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Condensation {
    public record CriticalPath(long weight, List<Integer> vertices){
        public CriticalPath{
            vertices = List.copyOf(vertices);
        }
    }
    private Condensation(){
    }
    public static Graph build(Graph graph, SccResult components){
        if(!graph.directed() || graph.size() != components.size()){
            throw new IllegalArgumentException("Invalid SCC mapping");
        }

        Map<Long, Integer> weights = new LinkedHashMap<>();

        for (Graph.Edge edge : graph.edges()) {
            int a = components.componentOf(edge.from());
            int b = components.componentOf(edge.to());
            if(a != b){
                long key = ((long) a << 32) | (b & 0xffffffffL);
                weights.merge(key, edge.weight(), Math::max);
            }
        }

        Graph dag = new Graph(components.count(), true);

        for (Map.Entry<Long, Integer> entry : weights.entrySet()) {
            long key = entry.getKey();
            dag.addEdge((int) (key >> 32),(int) key,entry.getValue());
        }

        return dag;
    }
    public static CriticalPath longestPath(Graph dag) {
        Kahn.Result topology = Kahn.run(dag, new Metrics());

        if(topology.hasCycle()){
            throw new IllegalArgumentException("Critical path needs a DAG");
        }
        if(dag.size() == 0){
            return new CriticalPath(0, List.of());
        }

        long[] negativeDistance = new long[dag.size()];
        Arrays.fill(negativeDistance, Long.MAX_VALUE);
        int[] parent = new int[dag.size()];
        Arrays.fill(parent, -1);

        for(int v = 0; v < dag.size(); v++){
            if(dag.indegree(v) == 0){
                negativeDistance[v] = 0;
            }
        }
        int best = topology.order().get(0);

        for(int v : topology.order()){
            if(negativeDistance[v] < negativeDistance[best]){
                best = v;
            }
            for (Graph.Edge edge : dag.adjacent(v)) {
                long candidate =
                        negativeDistance[v] - (long) edge.weight();

                if (candidate < negativeDistance[edge.to()]) {
                    negativeDistance[edge.to()] = candidate;
                    parent[edge.to()] = v;
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for(int v = best; v != -1; v = parent[v]){
            path.add(v);
        }

        Collections.reverse(path);
        return new CriticalPath(-negativeDistance[best], path);
    }
}