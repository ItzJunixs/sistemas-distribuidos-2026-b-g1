package co.edu.corhuila.opti.workflow.observability;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

/**
 * Starts a fake opti-products-api, sends a place-order with a known traceparent and checks that
 * the trace_id that arrives downstream is the same one we sent. No framework: exit code 1 on failure.
 */
public final class TracePropagationTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String CALLER_SPAN = "00f067aa0ba902b7";
    private static final String FRAME = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
    private static int failures = 0;

    public static void main(String[] args) throws Exception {
        AtomicReference<String> receivedTraceparent = new AtomicReference<>();
        AtomicReference<String> receivedPath = new AtomicReference<>();
        HttpServer products = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        products.createContext("/api/v1/frames", ex -> {
            receivedTraceparent.set(ex.getRequestHeaders().getFirst("traceparent"));
            receivedPath.set(ex.getRequestURI().getPath());
            WorkflowServer.send(ex, 201, "{\"id\":\"5e2b0000-0000-4000-8000-000000000001\"}");
        });
        products.start();

        ByteArrayOutputStream logs = new ByteArrayOutputStream();
        StructuredLogger log = new StructuredLogger("workflow-api", "1.0.0", "test", new PrintStream(logs, true));
        WorkflowServer workflow = new WorkflowServer(0, "http://127.0.0.1:" + products.getAddress().getPort(),
                Duration.ofSeconds(2), log);
        workflow.start();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:" + workflow.port() + "/api/v1/sagas/place-order"))
                    .header("traceparent", "00-" + TRACE_ID + "-" + CALLER_SPAN + "-01")
                    .header("X-Correlation-Id", "place-order-0001")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"productType\":\"FRAME\",\"productId\":\"" + FRAME + "\",\"quantity\":1}"))
                    .build(), HttpResponse.BodyHandlers.ofString());

            String sentDownstream = receivedTraceparent.get();
            check("saga answers 201 COMPLETED", response.statusCode() == 201 && response.body().contains("COMPLETED"));
            check("products-api was called on the frame reservation path",
                    ("/api/v1/frames/" + FRAME + "/reservations").equals(receivedPath.get()));
            check("trace_id received by products-api == trace_id sent by the caller",
                    sentDownstream != null && TRACE_ID.equals(sentDownstream.split("-")[1]));
            check("downstream span is a new span, not the caller span", !CALLER_SPAN.equals(sentDownstream.split("-")[2]));
            check("response returns the same trace_id",
                    response.headers().firstValue("traceparent").orElse("").contains(TRACE_ID));
            check("every log line carries the trace_id", logs.toString().lines()
                    .allMatch(l -> l.contains("\"trace_id\":\"" + TRACE_ID + "\"")));

            String metrics = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + workflow.port() + "/metrics"))
                    .build(), HttpResponse.BodyHandlers.ofString()).body();
            check("counter by outcome is exposed", metrics.contains("workflow_place_order_requests_total{outcome=\"completed\"} 1"));
            check("latency histogram is exposed", metrics.contains("workflow_place_order_duration_seconds_count 1"));

            System.out.println("--- logs written during the test ---");
            System.out.print(logs);
        } finally {
            workflow.stop();
            products.stop(0);
        }
        System.out.println(failures == 0 ? "ALL TESTS PASSED" : failures + " TEST(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS " : "FAIL ") + name);
        if (!ok) {
            failures++;
        }
    }
}
