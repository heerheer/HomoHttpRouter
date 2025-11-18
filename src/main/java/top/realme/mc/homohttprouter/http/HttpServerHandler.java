package top.realme.mc.homohttprouter.http;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final RouterRegistry registry;

    public HttpServerHandler(RouterRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        String uri = req.uri();
        String path = uri;
        String query = "";

        int idx = uri.indexOf('?');
        if (idx >= 0) {
            path = uri.substring(0, idx);
            query = uri.substring(idx + 1);
        }

        byte[] bodyBytes = new byte[req.content().readableBytes()];
        req.content().readBytes(bodyBytes);

        Map<String, String> headers = new HashMap<>();
        req.headers().forEach(h -> headers.put(h.getKey(), h.getValue()));

        RestRequest restReq = new RestRequest(
                req.method().name(),
                path,
                query,
                headers,
                bodyBytes
        );

        RestResponse restResp;

        // docs.json
        if (path.equals("/docs.json")) {
            restResp = buildDocsJson();
        }
        // docs HTML
        else if (path.equals("/docs")) {
            restResp = buildDocsHtml();
        }
        // router registry
        else {
            RouterHandler handler = registry.findHandler(path);
            if (handler == null) {
                restResp = new RestResponse(404, "Not Found".getBytes());
                restResp.header("Content-Type", "text/plain");
            } else {
                try {
                    restResp = handler.handle(restReq);
                } catch (Exception e) {
                    e.printStackTrace();
                    restResp = new RestResponse(500, e.toString().getBytes());
                    restResp.header("Content-Type", "text/plain");
                }
            }
        }

        send(ctx, restResp);
    }

    private void send(ChannelHandlerContext ctx, RestResponse resp) {
        FullHttpResponse nettyResp = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(resp.status),
                Unpooled.wrappedBuffer(resp.body)
        );

        resp.headers.forEach((k, v) -> nettyResp.headers().add(k, v));
        nettyResp.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, resp.body.length);

        ctx.writeAndFlush(nettyResp).addListener(ChannelFutureListener.CLOSE);
    }

    // ===== docs.json =====
    private RestResponse buildDocsJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("routes", registry.listAll());

        byte[] json = JSON.toJSONString(map).getBytes(StandardCharsets.UTF_8);

        RestResponse r = new RestResponse(200, json);
        r.header("Content-Type", "application/json");

        return r;
    }

    // ===== docs HTML =====
    private RestResponse buildDocsHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>HomoHttpRouter API Docs</h1>");

        for (RouteInfo info : registry.listAll()) {
            sb.append("<h2>").append(info.prefix()).append(" — ").append(info.modId()).append("</h2>");
            sb.append("<p>").append(info.description()).append("</p>");

            for (RouteInfo.Endpoint ep : info.endpoints()) {
                sb.append("<div style='border:1px solid #aaa;padding:10px;margin:5px;'>");
                sb.append("<b>").append(ep.method()).append("</b> ");
                sb.append("<code>").append(info.prefix()).append(ep.path()).append("</code>");
                sb.append("<p>").append(ep.summary()).append("</p>");
                sb.append("</div>");
            }
        }

        sb.append("</body></html>");

        byte[] html = sb.toString().getBytes(StandardCharsets.UTF_8);

        RestResponse r = new RestResponse(200, html);
        r.header("Content-Type", "text/html; charset=utf-8");

        return r;
    }
}

