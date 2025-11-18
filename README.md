# HomoHttpRouter

> 此模组由GTP-5.1编写90%的代码，剩下10%由人 工♂不智能小赫编写。

![icon.png](src/main/resources/icon.png)

### A Shared HTTP Routing Framework for Minecraft Mods

HomoHttpRouter 是一个为 **Minecraft Forge 模组开发者**设计的轻量级 HTTP 服务聚合框架。
它让多个 Mod **共享同一个 HTTP 端口**，并通过事件系统动态注册路由，从而避免每个 Mod 都各自启动独立的 HTTP 服务。


它本质上是 Minecraft 服务端内置的：

* API Gateway
* 路由中心
* 自动文档生成器

基于成熟的 **OkHttp** 和 **FastJSON2**，设计轻量、稳定、易扩展。

---

## ✨ Features

### 🔌 Shared HTTP Port

所有 Mod 挂在同一个 HTTP 服务上，避免端口冲突，也避免重复占用网络资源。

### ⚙️ Event-Driven Route Registration

Mod 在启动时监听 `HttpServiceBuildEvent`，自动注册路由前缀和处理器。不需要自己创建服务器。

### 📦 OkHttp Request/Response Standard

你的路由处理器将获得：

* `okhttp3.Request`
* 返回 `okhttp3.Response`

无需设计自定义结构，直接享用 OkHttp 的完整 Request/Response API。

### 📝 Swagger-like Documentation

内建两个文档端点：

* `/docs` → 自动生成 HTML API 文档
* `/docs.json` → FastJSON2 输出的 JSON 文档（可用于外部生成器 / UI）

文档基于你注册的 `RouteInfo` 自动生成。

### 🔧 Configurable Port

端口号可在 Forge 配置中调整：

```toml
[http]
    # The port to listen on
    port = 11451
```

> 注意：新版配置中仅保留 `PORT` 一个字段。

### 🚀 Lightweight & Stable

使用 Java HttpServer + OkHttp + FastJSON2，避免大型网络框架的高负担。

---

# 📦 Installation

将 HomoHttpRouter 作为依赖 Mod 安装至服务器端，然后其他 Mod 可自动注册路由。

如要从源码构建：

```
git clone https://github.com/yourname/HomoHttpRouter.git
./gradlew build
```

构建完成后即可在 `build/libs/` 中找到 jar。

---

# 🔧 Configuration

主配置文件：

```
config/homohttprouter-server.toml
```

内容：

```toml
[http]
    # The port to listen on
    port = 11451
```

修改后重启服务器生效。

---

# 🧩 Usage (Mod Developer Guide)

其他 Mod 可以通过监听 `HttpServiceBuildEvent` 注册自己的 HTTP 路由。

## Step 1: Listen to the Event

```java
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AwhRoutes {

    @SubscribeEvent
    public static void onHttp(HttpServiceBuildEvent e) {
        RouterRegistry registry = e.getRegistry();

        // Create RouteInfo
        RouteInfo info = new RouteInfo.Builder("awh", "/awh")
                .description("AWH module HTTP API")
                .route("GET", "/status", "Check server status", "", "{ok:true}")
                .route("POST", "/user/create", "Create user", "{name,age}", "{id}")
                .build();

        // Register route
        registry.register(info, request -> {
            if (request.url().encodedPath().equals("/awh/status")) {

                String json = "{\"ok\":true}";

                return new Response.Builder()
                        .request(request)
                        .code(200)
                        .protocol(Protocol.HTTP_1_1)
                        .addHeader("Content-Type", "application/json")
                        .body(ResponseBody.create(json.getBytes()))
                        .build();
            }

            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(404)
                    .message("Not Found")
                    .body(ResponseBody.create("Not Found".getBytes()))
                    .build();
        });
    }
}
```

---

# 📃 Automatic Documentation

访问：

```
http://localhost:11451/docs
```

即可看到自动渲染的 HTML 文档：

* 路由前缀（如 `/awh`）
* 方法（GET/POST/PUT/DELETE）
* Summary
* Body Schema
* Return Schema

而：

```
http://localhost:11451/docs.json
```

会返回 FastJSON2 序列化的 JSON：

```json
{
  "routes": [
    {
      "modId": "awh",
      "prefix": "/awh",
      "description": "AWH module HTTP API",
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

# 📚 Architecture Overview

```
HomoHttpRouter
 ├── HttpServerManager     ← 启动 Java HttpServer, 处理请求路由
 ├── RouterRegistry        ← 路由前缀 → Handler 映射，支持查询
 ├── RouterHandler         ← Mod 处理器接口 (OkHttp Request/Response)
 ├── RouteInfo             ← Swagger-like 描述结构
 ├── HttpServiceBuildEvent ← Mod 监听此事件注册路由
 └── Config                ← Forge config 管理端口
```

轻量、解耦、易扩展。

---

# 🤝 Contributing

欢迎提交 PR 或 issue 来扩展功能，例如：

* 中间件（Middleware）
* 鉴权（token / API key）
* WebSocket 支持
* OpenAPI 3.0 导出
* 上传文件（multipart）

---

# 📄 License

MIT License
