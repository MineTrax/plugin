package com.xinecraft.threads.webquery;

import com.xinecraft.threads.WebQueryProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress() + "-> Channel Active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " -> " + s);
        String output;
        try {
            output = WebQueryProtocol.processInput(s);
        } catch (Exception e) {
            output = "err";
        }
        ctx.channel().writeAndFlush(output);
        ctx.close();
    }
}