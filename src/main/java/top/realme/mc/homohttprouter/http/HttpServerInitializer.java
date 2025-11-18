package top.realme.mc.homohttprouter.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

class HttpServerInitializer extends ChannelInitializer<Channel> {

    private final RouterRegistry registry;

    public HttpServerInitializer(RouterRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(1024 * 1024));
        p.addLast(new HttpServerHandler(registry));
    }
}
