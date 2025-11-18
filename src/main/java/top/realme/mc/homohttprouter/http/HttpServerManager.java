package top.realme.mc.homohttprouter.http;


import com.alibaba.fastjson2.JSON;
import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;

public class HttpServerManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int port;
    private final RouterRegistry registry;

    private HttpServer server;

    public HttpServerManager(int port, RouterRegistry registry) {
        this.port = port;
        this.registry = registry;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.createContext("/", this::handleExchange);
        server.start();

        LOGGER.info("HTTP server started at port {}", port);
    }

    public void stop() {
        if (server != null) server.stop(0);
        LOGGER.info("HTTP server stopped");
    }

    private void handleExchange(HttpExchange ex) throws IOException {
        byte[] rawBody = ex.getRequestBody().readAllBytes();

        Request okreq = toOkHttpRequest(ex, rawBody);
        Response okresp;

        String path = ex.getRequestURI().getPath();

        // /docs.json
        if (path.equals("/docs.json")) {
            okresp = buildDocsJsonResponse(okreq);
        }
        // /docs
        else if (path.equals("/docs")) {
            okresp = buildDocsHtmlResponse(okreq);
        }
        // 普通路由
        else {
            RouterHandler handler = registry.findHandler(path);

            if (handler == null) {
                okresp = new Response.Builder()
                        .request(okreq)
                        .protocol(Protocol.HTTP_1_1)
                        .code(404)
                        .message("Not Found")
                        .body(ResponseBody.create("Not Found", MediaType.parse("text/plain")))
                        .build();
            } else {
                try {
                    okresp = handler.handle(okreq);
                } catch (Exception e) {
                    e.printStackTrace();
                    okresp = new Response.Builder()
                            .request(okreq)
                            .protocol(Protocol.HTTP_1_1)
                            .code(500)
                            .message("Internal Error")
                            .body(ResponseBody.create(e.toString(), MediaType.parse("text/plain")))
                            .build();
                }
            }
        }

        writeOkHttpResponse(ex, okresp);
    }

    private Request toOkHttpRequest(HttpExchange ex, byte[] body) {
        URI uri = ex.getRequestURI();

        Request.Builder builder = new Request.Builder()
                .url("http://localhost:" + port + uri.toString());

        boolean hasBody = !(ex.getRequestMethod().equals("GET") || ex.getRequestMethod().equals("HEAD"));
        RequestBody requestBody = hasBody ? RequestBody.create(body) : null;

        builder.method(ex.getRequestMethod(), requestBody);

        ex.getRequestHeaders().forEach((name, values) -> {
            for (String v : values) builder.addHeader(name, v);
        });

        return builder.build();
    }

    private void writeOkHttpResponse(HttpExchange ex, Response resp) throws IOException {
        // headers
        for (String name : resp.headers().names()) {
            for (String value : resp.headers(name)) {
                ex.getResponseHeaders().add(name, value);
            }
        }

        byte[] data = resp.body() != null ? resp.body().bytes() : new byte[0];
        ex.sendResponseHeaders(resp.code(), data.length);

        OutputStream os = ex.getResponseBody();
        os.write(data);
        os.close();
    }

    // 🔥 fastjson 格式的 /docs.json
    private Response buildDocsJsonResponse(Request req) {
        Map<String, Object> map = new HashMap<>();
        map.put("routes", registry.listAll());

        String json = JSON.toJSONString(map);

        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .addHeader("Content-Type", "application/json")
                .body(ResponseBody.create(json.getBytes(), MediaType.parse("application/json")))
                .build();
    }

    // 🔥 自动生成 HTML 的文档, AI真的太好用啦(
    private Response buildDocsHtmlResponse(Request req) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>HomoHttpRouter API Docs</h1>");

        for (RouteInfo info : registry.listAll()) {
            sb.append("<h2>")
                    .append(info.prefix).append(" — ").append(info.modId)
                    .append("</h2>");
            sb.append("<p>").append(info.description).append("</p>");

            for (RouteInfo.Endpoint ep : info.endpoints) {
                sb.append("<div style='border:1px solid #aaa;padding:10px;margin:5px;'>");
                sb.append("<b>").append(ep.method).append("</b> ");
                sb.append("<code>").append(info.prefix).append(ep.path).append("</code>");
                sb.append("<p>").append(ep.summary).append("</p>");
                sb.append("</div>");
            }
        }

        sb.append("</body></html>");

        return new Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .addHeader("Content-Type", "text/html")
                .body(ResponseBody.create(sb.toString(), MediaType.parse("text/html")))
                .build();
    }
}
