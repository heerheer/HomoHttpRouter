package top.realme.mc.homohttprouter.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record RouteInfo(String modId, String prefix, String description, List<Endpoint> endpoints) {
    public RouteInfo(String modId, String prefix, String description, List<Endpoint> endpoints) {
        this.modId = modId;
        this.prefix = prefix;
        this.description = description;
        this.endpoints = Collections.unmodifiableList(endpoints);
    }

    public record Endpoint(String method, String path, String summary, String bodySchema, String returns) {
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