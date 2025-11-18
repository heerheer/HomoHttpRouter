package top.realme.mc.homohttprouter.http;

import java.util.*;
import java.util.stream.Collectors;

public class RouterRegistry {

    // 路由表中的一个条目，包含模块ID、处理程序和路由信息
    private static class Entry {
        final String modId;
        final RouterHandler handler;
        final RouteInfo info;

        Entry(String modId, RouterHandler handler, RouteInfo info) {
            this.modId = modId;
            this.handler = handler;
            this.info = info;
        }
    }

    // 路由表，键为路由前缀，值为对应的处理程序和路由信息
    private final Map<String, Entry> routes = new HashMap<>();

    /**
     * Register a router handler for a specific route prefix.
     * 注册一个特定路由前缀的路由器处理程序。
     *
     * @param info     The route information.
     * @param handler  The router handler.
     */
    public synchronized void register(RouteInfo info, RouterHandler handler) {
        if (routes.containsKey(info.prefix())) {
            throw new RuntimeException("Duplicate prefix: " + info.prefix());
        }
        routes.put(info.prefix(), new Entry(info.modId(), handler, info));
    }

    /**
     * Find the router handler for a specific path.
     * 寻找路径匹配的路由器处理程序。
     *
     * @param path  The request path.
     * @return      The router handler, or null if not found.
     */
    public synchronized RouterHandler findHandler(String path) {
        return routes.entrySet().stream()
                .filter(e -> path.startsWith(e.getKey()))
                .sorted((a, b) -> b.getKey().length() - a.getKey().length())
                .map(e -> e.getValue().handler)
                .findFirst()
                .orElse(null);
    }

    /**
     * List all registered routes.
     * 列出所有已注册的路由。
     *
     * @return  A list of route information.
     */
    public synchronized List<RouteInfo> listAll() {
        return routes.values()
                .stream()
                .map(e -> e.info)
                .collect(Collectors.toList());
    }
}