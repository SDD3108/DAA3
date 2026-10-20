package algorithms;
import graph.Graph;
import metrics.Metrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Kahn {
    public record Result(List<Integer> order, boolean hasCycle){
        public Result{
            order = List.copyOf(order);
        }
    }
    private Kahn(){
    }
    public static Result run(Graph graph, Metrics metrics){
        if(!graph.directed()){
            throw new IllegalArgumentException("Topological order needs a directed graph");
        }

        int[] indegree = new int[graph.size()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for(int v = 0; v < graph.size(); v++){
            indegree[v] = graph.indegree(v);
            if(indegree[v] == 0){
                queue.add(v);
            }
        }

        List<Integer> order = new ArrayList<>(graph.size());
        while(!queue.isEmpty()){
            int v = queue.remove();
            order.add(v);
            for(Graph.Edge edge : graph.adjacent(v)){
                metrics.edgeScans++;
                indegree[edge.to()]--;
                if(indegree[edge.to()] == 0){
                    queue.add(edge.to());
                }
            }
        }
        boolean cycle = order.size() != graph.size();
        return new Result(cycle ? List.of() : order, cycle);
    }

    public static boolean isValid(Graph graph, List<Integer> order){
        if(!graph.directed() || order.size() != graph.size()){
            return false;
        }

        int[] position = new int[graph.size()];
        boolean[] seen = new boolean[graph.size()];
        for(int i = 0; i < order.size(); i++){
            int v = order.get(i);
            if(v < 0 || v >= graph.size() || seen[v]){
                return false;
            }

            position[v] = i;
            seen[v] = true;
        }
        for(Graph.Edge edge : graph.edges()){
            if(position[edge.from()] >= position[edge.to()]){
                return false;
            }
        }

        return true;
    }
}