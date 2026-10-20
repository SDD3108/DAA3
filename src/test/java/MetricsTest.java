import metrics.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsTest {

    @Test
    void startsWithZeroCounters() {
        Metrics metrics = new Metrics();

        assertEquals(0, metrics.dsuOps());
        assertEquals("0,0,0,0", metrics.counters());
    }

    @Test
    void combinesDsuOperationsAndFormatsCounters() {
        Metrics metrics = new Metrics();

        metrics.findCalls = 8;
        metrics.successfulUnions = 3;
        metrics.edgeScans = 15;
        metrics.comparisons = 12;
        metrics.depth(4);
        assertEquals(11, metrics.dsuOps());
        assertEquals("11,15,12,4", metrics.counters());
    }

    @Test
    void keepsMaximumDepthWhenTraversalReturns() {
        Metrics metrics = new Metrics();

        metrics.depth(1);
        metrics.depth(5);
        metrics.depth(2);
        metrics.depth(0);
        assertEquals(5, metrics.maxDepth);
        metrics.depth(7);
        assertEquals(7, metrics.maxDepth);
    }
}