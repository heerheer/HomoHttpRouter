package top.realme.mc.homohttprouter.event;

import net.neoforged.bus.api.Event;
import top.realme.mc.homohttprouter.http.RouterRegistry;

public class HttpServiceBuildEvent extends Event {
    private final RouterRegistry registry;

    public HttpServiceBuildEvent(RouterRegistry registry) {
        this.registry = registry;
    }

    public RouterRegistry getRegistry() {
        return registry;
    }
}
