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

