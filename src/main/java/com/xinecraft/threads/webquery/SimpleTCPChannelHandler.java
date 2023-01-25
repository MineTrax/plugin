package com.xinecraft.threads.webquery;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String output;
        try {
            output = WebQueryProtocol.processInput(s);
        } catch (Exception e) {
            output = "err";
        }
        if (output == null) {
            output = "err";
        }
        ctx.channel().writeAndFlush(output);
        ctx.close();
    }
}