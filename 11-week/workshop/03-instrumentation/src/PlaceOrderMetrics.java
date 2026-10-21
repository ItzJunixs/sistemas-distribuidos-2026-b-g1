package co.edu.corhuila.opti.workflow.observability;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * The two metrics of the place-order saga, rendered in Prometheus text format on /metrics:
 * a counter by outcome and a latency histogram. Names and buckets come from 04-metrics.md.
 */
public final class PlaceOrderMetrics {

    /** Seconds. A seller waits at the counter with the patient: 1 s is fine, 5 s is the participant timeout. */
    static final double[] BUCKETS = {0.1, 0.25, 0.5, 1, 2, 5};

    /** outcome is a closed list: completed, compensated, running, failed. */
    private final Map<String, AtomicLong> requests = new ConcurrentHashMap<>();
    private final AtomicLong[] bucketCounts = new AtomicLong[BUCKETS.length];
    private final AtomicLong count = new AtomicLong();
    private final DoubleAdder sum = new DoubleAdder();

    public PlaceOrderMetrics() {
        for (int i = 0; i < BUCKETS.length; i++) {
            bucketCounts[i] = new AtomicLong();
        }
    }

    public void record(String outcome, double seconds) {
        requests.computeIfAbsent(outcome, k -> new AtomicLong()).incrementAndGet();
        for (int i = 0; i < BUCKETS.length; i++) {
            if (seconds <= BUCKETS[i]) {
                bucketCounts[i].incrementAndGet();
            }
        }
        count.incrementAndGet();
        sum.add(seconds);
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP workflow_place_order_requests_total Place-order sagas by final outcome.\n");
        sb.append("# TYPE workflow_place_order_requests_total counter\n");
        new TreeMap<>(requests).forEach((outcome, n) -> sb.append("workflow_place_order_requests_total{outcome=\"")
                .append(outcome).append("\"} ").append(n.get()).append('\n'));
        sb.append("# HELP workflow_place_order_duration_seconds Time to answer POST /api/v1/sagas/place-order.\n");
        sb.append("# TYPE workflow_place_order_duration_seconds histogram\n");
        for (int i = 0; i < BUCKETS.length; i++) {
            sb.append("workflow_place_order_duration_seconds_bucket{le=\"").append(BUCKETS[i]).append("\"} ")
                    .append(bucketCounts[i].get()).append('\n');
        }
        sb.append("workflow_place_order_duration_seconds_bucket{le=\"+Inf\"} ").append(count.get()).append('\n');
        sb.append("workflow_place_order_duration_seconds_sum ").append(sum.sum()).append('\n');
        sb.append("workflow_place_order_duration_seconds_count ").append(count.get()).append('\n');
        return sb.toString();
    }
}
