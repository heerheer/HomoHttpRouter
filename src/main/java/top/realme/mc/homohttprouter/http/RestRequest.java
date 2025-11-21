package top.realme.mc.homohttprouter.http;

import io.netty.handler.codec.http.QueryStringDecoder;
import top.realme.mc.homohttprouter.PathTemplateParser;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * RESTful request
 * @param method HTTP method, like GET, POST, PUT, DELETE, etc.
 * @param path URL path, like /my-mod-api/test/123 or /my-mod-api/test/123/abc
 * @param query URL query string, like a=1&b=2
 * @param headers Map of HTTP headers, key is header name, value is list of header values
 * @param body Request body as byte array
 */
public record RestRequest(
        String method,
        String path,
        String query,
        Map<String, String> headers,
        byte[] body
) {

    /**
     * Get URL parameters
     * @return Map of URL parameters, key is parameter name, value is list of parameter values
     */
    public Map<String, List<String>> queryParams() {
        return new QueryStringDecoder(url()).parameters();
    }

    /**
     * Get URL parameter by name (Optional)
     * @param name parameter name
     * @return Optional parameter value
     */
    public Optional<String> queryParam(String name) {
        return Optional.ofNullable(queryParams().get(name))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    /**
     * Get path parameters
     * @param template path template, like /test/<id> or /test/<id>/[a]
     * @return Map of path parameters, key is parameter name, value is parameter value
     */
    public Map<String, String> pathParams(String template) {
        template  = fixTemplate(template);
        return PathTemplateParser.parse(template, path);
    }

    /**
     * Get path parameter by name (Optional)
     * @param template path template, like /test/<id>
     * @param name parameter name
     * @return Optional parameter value
     */
    public Optional<String> pathParam(String template, String name) {

        return Optional.ofNullable(pathParams(template).get(name));
    }



    /**
     * Check if path matches template
     * @param template path template, like /my-mod-api/test/<id> or /my-mod-api/test/<id>/[a], method will automatically trim prefix
     * @return true if matches, false otherwise
     */
    public boolean matchTemplate(String template) {
        if (template == null || path == null) return false;

        template  = fixTemplate(template);


        String[] tParts = template.split("/"); // template can be /abc/1
        String[] pParts = path.split("/"); // path is full like /my-mod/abc/1


        if (tParts.length != pParts.length) return false;

        for (int i = 0; i < tParts.length; i++) {
            String t = tParts[i];
            String p = pParts[i];

            // 1. 匹配 <xxx> 格式
            if (isAnglePlaceholder(t)) {
                if (p.isEmpty()) return false;
                continue;
            }

            // 2. 匹配 [xxx] 格式
            if (isBracketPlaceholder(t)) {
                if (p.isEmpty()) return false;
                continue;
            }

            // 3. 普通字符串必须精确相等
            if (!t.equals(p)) {
                return false;
            }
        }

        return true;
    }

    private boolean isAnglePlaceholder(String s) {
        return s.startsWith("<") && s.endsWith(">");
    }

    private boolean isBracketPlaceholder(String s) {
        return s.startsWith("[") && s.endsWith("]");
    }

    // fix the maybe-not-full-template, like /abc/1 to /my-mod/abc/1
    private String fixTemplate(String template) {
        String[] tParts = template.split("/"); // template can be /abc/1
        String[] pParts = path.split("/"); // path is full like /my-mod/abc/1

        if (tParts.length <= 1 || pParts.length <= 1)
            return template;


        if (!tParts[1].equals(pParts[1]))
            return "/" + pParts[1] + String.join("/", tParts);

        return template;

    }


    /**
     * Get body as String
     * @return body as String
     */
    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /**
     * Get full URL
     * @return full URL
     */
    public String url() {
        return path + (query == null || query.isEmpty() ? "" : "?" + query);
    }


}

