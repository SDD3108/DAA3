package graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    public record Edge(int from, int to, int weight){
    }
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final int[] indegrees;

    public Graph(int n, boolean directed){
        if(n < 0){
            throw new IllegalArgumentException("Negative vertex count");
        }

        this.directed = directed;
        indegrees = new int[n];
        adjacency = new ArrayList<>(n);
        for(int i = 0; i < n; i++){
            adjacency.add(new ArrayList<>());
        }
    }
    public int size(){
        return adjacency.size();
    }
    public boolean directed(){
        return directed;
    }
    public List<Edge> edges(){
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> adjacent(int v){
        checkVertex(v);
        return Collections.unmodifiableList(adjacency.get(v));
    }

    public int indegree(int v){
        checkVertex(v);
        return indegrees[v];
    }

    public void checkVertex(int v){
        if(v < 0 || v >= size()){
            throw new IllegalArgumentException("Invalid vertex: " + v);
        }
    }

    public void addEdge(int u, int v, int weight) {
        checkVertex(u);
        checkVertex(v);

        Edge edge = new Edge(u, v, weight);
        edges.add(edge);
        adjacency.get(u).add(edge);
        indegrees[v]++;

        if(!directed){
            adjacency.get(v).add(new Edge(v, u, weight));
            indegrees[u]++;
        }
    }

    public Graph reversed() {
        Graph result = new Graph(size(), directed);
        for(Edge edge : edges){
            result.addEdge(edge.to(), edge.from(), edge.weight());
        }

        return result;
    }

    public static Graph read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path).stream().filter(line -> !line.isBlank()).toList();

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Empty graph file");
        }

        String[] header = lines.get(0).trim().split("\\s+");
        if(header.length != 3 || (!header[2].equals("directed") && !header[2].equals("undirected"))){
            throw new IllegalArgumentException("expected n m directed|undirected");
        }

        int n = Integer.parseInt(header[0]);
        int m = Integer.parseInt(header[1]);
        if(m < 0 || lines.size() != m + 1){
            throw new IllegalArgumentException("Edge count mismatch");
        }

        Graph graph = new Graph(n, header[2].equals("directed"));
        for(int i = 1; i <= m; i++){
            String[] edge = lines.get(i).trim().split("\\s+");

            if(edge.length != 3){
                throw new IllegalArgumentException("Expected u v w");
            }
            graph.addEdge(Integer.parseInt(edge[0]),Integer.parseInt(edge[1]),Integer.parseInt(edge[2]));
        }

        return graph;
    }
}