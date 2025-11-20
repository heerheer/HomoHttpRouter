package top.realme.mc.homohttprouter.http;

import io.netty.handler.codec.http.QueryStringDecoder;
import top.realme.mc.homohttprouter.PathTemplateParser;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * @param template path template, like /test/<id> or /test/<id>/[a]
     * @return true if matches, false otherwise
     */
    public boolean matchTemplate(String template) {
        if (template == null || path == null) return false;

        String[] tParts = template.split("/");
        String[] pParts = path.split("/");

        // 必须段数一致，否则直接不匹配
        if (tParts.length != pParts.length) return false;

        for (int i = 0; i < tParts.length; i++) {
            String t = tParts[i];
            String p = pParts[i];

            // 模板中的 {xxx} 表示通配符，可以匹配任何非空 segment
            if (t.startsWith("{") && t.endsWith("}")) {
                if (p.isEmpty()) {
                    return false; // 占位符不能匹配空值
                }
                continue;
            }

            // 普通字符串必须完全相等
            if (!t.equals(p)) {
                return false;
            }
        }

        return true;
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

