package top.realme.mc.homohttprouter.http;

import com.alibaba.fastjson2.JSON;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpServerManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int port;
    private final RouterRegistry registry;

    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private Channel serverChannel;

    public HttpServerManager(int port, RouterRegistry registry) {
        this.port = port;
        this.registry = registry;
    }

    public void start() {
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpServerInitializer(registry));

        try {
            serverChannel = b.bind(port).sync().channel();
            LOGGER.info("Netty HTTP server started at http://localhost:{}/", port);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Netty HTTP server", e);
        }
    }

    public void stop() {
        try {
            if (serverChannel != null) serverChannel.close().sync();
        } catch (Exception ignored) {}

        if (boss != null) boss.shutdownGracefully();
        if (worker != null) worker.shutdownGracefully();

        LOGGER.info("Netty HTTP server stopped");
    }
}
