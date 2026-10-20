package algorithms;

import graph.Graph.Edge;
import java.util.List;

public record MstResult(long weight, List<Edge> edges, int components) {
    public MstResult {
        edges = List.copyOf(edges);
    }
}