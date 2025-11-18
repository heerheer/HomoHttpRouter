package top.realme.mc.homohttprouter.http;

import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
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


    /**
     * 通过 JSON 文本构建响应
     */
    public static RestResponse json(int status, String jsonText) {
        RestResponse resp = new RestResponse(status, jsonText.getBytes(StandardCharsets.UTF_8));
        resp.header("Content-Type", "application/json;charset=UTF-8");
        return resp;
    }

    /**
     * 通过任意对象创建 JSON 响应
     */
    public static RestResponse json(int status, Object obj) {
        String json = JSON.toJSONString(obj);
        return json(status, json);
    }

    /**
     * 常用 OK 响应
     */
    public static RestResponse ok(Object obj) {
        return json(200, obj);
    }

    /**
     * 常用错误响应
     */
    public static RestResponse error(int status, Object obj) {
        return json(status, obj);
    }
}
