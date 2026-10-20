import algorithms.DSU;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DSUTest {

    @Test
    void agreesWithReferenceAfterRandomUnions(){
        int n = 60;
        DSU dsu = new DSU(n);
        int[] labels = new int[n];

        for (int i = 0; i < n; i++) {
            labels[i] = i;
        }
        Random random = new Random(42);
        for (int step = 0; step < 500; step++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            int old = labels[b];
            int replacement = labels[a];

            assertEquals(old != replacement, dsu.union(a, b));
            for(int i = 0; i < n; i++){
                if(labels[i] == old){
                    labels[i] = replacement;
                }
            }

            for(int i = 0; i < n; i++){
                for(int j = 0; j < n; j++){
                    assertEquals(labels[i] == labels[j],dsu.find(i) == dsu.find(j));
                }
            }

            assertEquals(Arrays.stream(labels).distinct().count(),dsu.components());
        }
    }
    @Test
    void countsAndResets() {
        DSU dsu = new DSU(3);

        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        dsu.find(2);
        assertEquals(2, dsu.components());
        assertEquals(5, dsu.metrics().findCalls);
        assertEquals(1, dsu.metrics().successfulUnions);
        assertEquals(6, dsu.metrics().dsuOps());
        dsu.makeSet(0);
        assertEquals(0, dsu.components());
        assertEquals(0, dsu.metrics().dsuOps());
        assertThrows(IllegalArgumentException.class,() -> dsu.find(0));
    }
}