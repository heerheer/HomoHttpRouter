# HomoHttpRouter

> 70% of the code for this module was written by GTP-5.1, and the remaining 30% was written by the "artificially-unintelligent" Heer!(me).

![icon.png](src/main/resources/icon.png)

### A Shared HTTP Routing Framework for Minecraft Mods

HomoHttpRouter is a lightweight HTTP service aggregation framework designed for **Minecraft Forge mod developers**.
It allows multiple mods to **share the same HTTP port** and dynamically register routes through an event system, thereby avoiding the need for each mod to start an independent HTTP service individually.


It essentially is Minecraft server built-in:

* Route Center
* Automatic Documentation Generator

Based on mature **Netty** and **FastJSON2**, designed to be lightweight, stable, and easily extendable.

---

## ✨ Features

### 🔌 Shared HTTP Port

All mods are mounted on the same HTTP service to avoid port conflicts and duplicate network resource usage.

### ⚙️ Event-Driven Route Registration

The Mod listens to the `HttpServiceBuildEvent` when starting, and automatically registers route prefixes and handlers. There is no need to create a server by yourself.

### 📦 Netty Request/Response Standard

Your route handlers will receive:

* `RestRequest`
* You need to return `RestResponse`


### 📝 Auto-Generated Documentation

HomoHttpRouter comes with two built-in documentation endpoints:

* `/docs` → Automatically generated HTML API documentation
* `/docs.json` → JSON documentation output by FastJSON2 (useful for external generators / UIs)

The documentation is automatically generated based on the `RouteInfo` you register.

### 🔧 Configurable Port

The port number can be adjusted in the Forge configuration:

```toml
[http]
    # The port to listen on
    port = 11451
```

---

# 📦 Installation

Install HomoHttpRouter as a dependency Mod on the server side, and other Mods can automatically register routes.

To build from source code:

```
git clone https://github.com/yourname/HomoHttpRouter.git
./gradlew build
```

After the build is complete, you can find the jar in `build/libs/`.

---

# 🔧 Configuration

Main configuration file:

```
config/homohttprouter-server.toml
```

Content:

```toml
[http]
    # The port to listen on
    port = 11451
```

After modifying the configuration, restart the server for the changes to take effect.

---

# 🧩 Usage (Mod Developer Guide)


Other Mods can register their HTTP routes by listening to the `HttpServiceBuildEvent`.

## Step 1: Listen to the Event

```java
@EventBusSubscriber
public class AwhRoutes {
    @SubscribeEvent
    private static void onHttp(HttpServiceBuildEvent e){
        RouterRegistry registry = e.getRegistry();

        // Create RouteInfo
        RouteInfo info = new RouteInfo.Builder("mymod", "/mymod")
                .description("MyMod HTTP API")
                .route("GET", "/status", "Check service status", "", "OK")
                .build();

        // Register RouteInfo
        registry.register(info, restRequest -> RestResponse.ok());
    }
}
```

## Step 2: Get Parameters from Request

```java
@EventBusSubscriber
public class AwhRoutes {
    @SubscribeEvent
    private static void onHttp(HttpServiceBuildEvent e){
        RouterRegistry registry = e.getRegistry();

        // Create RouteInfo
        RouteInfo info = new RouteInfo.Builder("mymod", "/mymod")
                .description("MyMod HTTP API")
                .route("GET", "/status", "Check service status", "", "OK")
                .build();

        // Register RouteInfo
        registry.register(info, restRequest -> {
            // Get Parameters from Request like this: /status?id=1
            if(restRequest.matchTemplate("/status")){
                // you should ignore ?id=1 in template
                String id = restRequest.queryParam("id");
                return RestResponse.ok(id);
            }
            
            // or you can get path params like this: /status/1
            if(restRequest.matchTemplate("/status/<id>")){
                String id = restRequest.pathParam("id");
                return RestResponse.ok(id);
            }
            
           
        });
    }
}
```
---

# 📃 Automatic Documentation

Just visit:

```
http://your server address:[port]/docs
```

You can then see the automatically rendered HTML document:

Visit:
```
http://localhost:11451/docs.json
```
Will return:
```json
{
  "routes": [
    {
      "modId": "yourmodid",
      "prefix": "/mymod",
      "description": "MyMod HTTP API",
      "endpoints": [
        {
          "method": "GET",
          "path": "/status",
          "summary": "Check server status",
          "bodySchema": "",
          "returns": "{ok:true}"
        }
      ]
    }
  ]
}
```

---

# 🛠️ Development Environment

### Dependencies included:

```
com.squareup.okhttp3:okhttp:4.12.0
com.alibaba.fastjson2:fastjson2:2.x.x
```

Gradle example：

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.squareup.okhttp3:okhttp:4.12.0"
    implementation "com.alibaba.fastjson2:fastjson2:2.0.48"
}
```

---


# 🤝 Contributing

Welcome to submit PRs or issues to extend the functionality, such as:

* Middleware
* Authentication (token / API key)
* WebSocket support
* OpenAPI 3.0 export
* File upload (multipart)

---

# 📄 License

MIT License
