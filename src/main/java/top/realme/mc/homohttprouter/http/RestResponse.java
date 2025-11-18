package top.realme.mc.homohttprouter.http;

import java.util.HashMap;
import java.util.Map;

public class RestResponse {
    public final int status;
    public final Map<String, String> headers = new HashMap<>();
    public final byte[] body;

    public RestResponse(int status, byte[] body) {
        this.status = status;
        this.body = body;
    }

    public RestResponse header(String key, String value) {
        headers.put(key, value);
        return this;
    }
}
