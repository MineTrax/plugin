package com.xinecraft.minetrax.bukkit.threads.webquery;

import com.xinecraft.minetrax.bukkit.utils.LoggingUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String output;
        try {
            output = WebQueryProtocol.processInput(s);
        } catch (Exception e) {
            LoggingUtil.warning("Error processing input: " + e.getMessage());
            output = "err";
        }
        if (output == null) {
            output = "err";
        }
        ctx.channel().writeAndFlush(output);
        ctx.close();
    }
}