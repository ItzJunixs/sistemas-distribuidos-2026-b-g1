package co.edu.corhuila.opti.workflow.observability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Minimal opti-workflow: POST /api/v1/sagas/place-order behind the trace-context filter, step
 * reserve-stock against opti-products-api, and GET /metrics for Prometheus.
 */
public final class WorkflowServer {

    private static final Pattern PRODUCT_ID = Pattern.compile("\"productId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private final HttpServer server;
    private final StructuredLogger log;
    private final StockHttpClient stock;
    private final PlaceOrderMetrics metrics = new PlaceOrderMetrics();

    public WorkflowServer(int port, String productsUrl, Duration timeout, StructuredLogger log) throws IOException {
        this.log = log;
        this.stock = new StockHttpClient(productsUrl, timeout, log);
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/v1/sagas/place-order", new TraceContextFilter(this::placeOrder, log));
        server.createContext("/metrics", ex -> sendText(ex, metrics.render()));
    }

    public int port() { return server.getAddress().getPort(); }
    public void start() { server.start(); }
    public void stop() { server.stop(0); }

    private void placeOrder(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            send(ex, 405, "{\"code\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }
        Matcher m = PRODUCT_ID.matcher(readBody(ex));
        if (!m.find()) {
            send(ex, 400, "{\"code\":\"VALIDATION\",\"field\":\"productId\"}");
            return;
        }
        String sagaId = UUID.randomUUID().toString();
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("saga_id", sagaId);
        fields.put("product_type", "FRAME");
        fields.put("product_id", m.group(1));
        fields.put("attempt", 1);
        log.info("saga.started", "place-order saga started", fields);
        long started = System.nanoTime();

        StockHttpClient.Result result = stock.reserveFrame(sagaId, m.group(1), 1);
        String status;
        int http;
        switch (result) {
            case RESERVED: status = "COMPLETED"; http = 201; break;
            case INSUFFICIENT_STOCK: status = "COMPENSATED"; http = 201; break;
            default: status = "RUNNING"; http = 202;
        }
        metrics.record(status.toLowerCase(), (System.nanoTime() - started) / 1e9);
        send(ex, http, "{\"id\":\"" + sagaId + "\",\"type\":\"PLACE_ORDER\",\"status\":\"" + status + "\"}");
    }

    static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }

    static void sendText(HttpExchange ex, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4");
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8085"));
        StructuredLogger log = StructuredLogger.fromEnv(System.out);
        String productsUrl = System.getenv().getOrDefault("PRODUCTS_URL", "http://127.0.0.1:8082");
        Duration timeout = Duration.parse(System.getenv().getOrDefault("HTTP_TIMEOUT", "PT5S"));
        WorkflowServer app = new WorkflowServer(port, productsUrl, timeout, log);
        app.start();
        log.info("app.started", "listening on http://127.0.0.1:" + app.port(), null);
    }
}
