package com.xinecraft.minetrax.common.webquery;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class WebQueryServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final int MAX_FRAME_LENGTH = 1024 * 1024; // 1MB
    private final List<String> whitelistedIps;

    public WebQueryServerInitializer(List<String> whitelistedIps) {
        this.whitelistedIps = whitelistedIps;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        if (whitelistedIps != null && !whitelistedIps.isEmpty()) {
            socketChannel.pipeline().addLast(new IpWhitelistHandler(whitelistedIps));
        }
        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(MAX_FRAME_LENGTH));
        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast(new WebQueryChannelHandler());
    }
}
