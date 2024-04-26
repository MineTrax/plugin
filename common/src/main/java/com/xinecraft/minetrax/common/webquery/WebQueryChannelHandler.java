package com.xinecraft.minetrax.common.webquery;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.webquery.protocol.WebQueryProtocol;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class WebQueryChannelHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String output;
        try {
            output = WebQueryProtocol.processInput(s);
            System.out.println("Received: " + s);
            System.out.println("Length: " + s.length());
        } catch (Exception e) {
            System.out.println("Error processing input: " + e.getMessage());
            output = "err";
        }
        if (output == null) {
            output = "err";
        }
        JsonObject object = new JsonObject();
        object.addProperty("status", "ok");
        ctx.channel().writeAndFlush(MinetraxCommon.getInstance().getGson().toJson(object) + "\r\n").addListener(ChannelFutureListener.CLOSE);
        ctx.close();
    }
}
