package top.realme.mc.homohttprouter.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteInfo {
    public final String modId;
    public final String prefix;
    public final String description;
    public final List<Endpoint> endpoints;

    public RouteInfo(String modId, String prefix, String description, List<Endpoint> endpoints) {
        this.modId = modId;
        this.prefix = prefix;
        this.description = description;
        this.endpoints = Collections.unmodifiableList(endpoints);
    }

    public static class Endpoint {
        public final String method;
        public final String path;
        public final String summary;
        public final String bodySchema;
        public final String returns;

        public Endpoint(String method, String path, String summary,
                        String bodySchema, String returns) {
            this.method = method;
            this.path = path;
            this.summary = summary;
            this.bodySchema = bodySchema;
            this.returns = returns;
        }
    }

    public static class Builder {
        private final String modId;
        private final String prefix;
        private String description = "";
        private final List<Endpoint> endpoints = new ArrayList<>();

        public Builder(String modId, String prefix) {
            this.modId = modId;
            this.prefix = prefix;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder route(String method, String path, String summary,
                             String bodySchema, String returns) {
            endpoints.add(new Endpoint(method, path, summary, bodySchema, returns));
            return this;
        }

        public RouteInfo build() {
            return new RouteInfo(modId, prefix, description, endpoints);
        }
    }
}