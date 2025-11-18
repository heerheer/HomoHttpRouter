package top.realme.mc.homohttprouter.http;

import java.util.Map;

public record RestRequest(String method, String path, String query, Map<String, String> headers, byte[] body) {
}
