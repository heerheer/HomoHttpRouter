package top.realme.mc.homohttprouter.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public interface RouterHandler {
    RestResponse handle(RestRequest request) throws Exception;
}