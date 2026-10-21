package benchmark;
import graph.Graph;
import java.util.Random;

public final class GraphGenerator{
    public enum Kind{
        UNDIRECTED, DIRECTED, DAG
    }
    private GraphGenerator(){
    }
    public static Graph generate(int n, int m, Kind kind){
        if(n < 2 || m < 0 || (kind == Kind.UNDIRECTED && m < n - 1)){
            throw new IllegalArgumentException("Invalid generator dimensions");
        }

        Random random = new Random(42);
        Graph graph = new Graph(n, kind != Kind.UNDIRECTED);
        int[] permutation = new int[n];

        for(int i = 0; i < n; i++){
            permutation[i] = i;
        }
        if(kind != Kind.DIRECTED){
            for(int i = n - 1; i > 0; i--){
                int j = random.nextInt(i + 1);
                int temp = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = temp;
            }
        }
        if(kind == Kind.UNDIRECTED){
            for(int i = 1; i < n; i++){
                graph.addEdge(permutation[random.nextInt(i)],permutation[i],1 + random.nextInt(1000));
            }
        }

        while(graph.edges().size() < m){
            int a = random.nextInt(n);
            int b = random.nextInt(n);

            while(a == b){
                b = random.nextInt(n);
            }
            if(kind == Kind.DAG && permutation[a] > permutation[b]){
                int temp = a;
                a = b;
                b = temp;
            }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return graph;
    }
}