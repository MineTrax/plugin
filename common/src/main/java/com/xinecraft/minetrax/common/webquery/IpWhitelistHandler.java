package com.xinecraft.minetrax.common.webquery;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

public class IpWhitelistHandler extends ChannelInboundHandlerAdapter {
    private final List<String> whitelist;

    public IpWhitelistHandler(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ipAddress = remoteAddress.getAddress().getHostAddress();

        // Normalize IPv6 address to compressed form
        if (ipAddress.contains(":")) {
            ipAddress = normalizeIPv6Address(ipAddress);
        }

        if (!whitelist.contains(ipAddress)) {
            ctx.close(); // Reject connection
        } else {
            super.channelActive(ctx); // Continue in the pipeline
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        MinetraxCommon.getInstance().getLogger().error("[WebQuery] Exception caught: " + cause.getMessage());
        if (MinetraxCommon.getInstance().getPlugin().getIsDebugMode()) {
            cause.printStackTrace();
        }
        ctx.close();
    }

    // Normalize IPv6 address to compressed form
    private String normalizeIPv6Address(String ipAddress) {
        try {
            return java.net.InetAddress.getByName(ipAddress).getHostAddress();
        } catch (Exception e) {
            LoggingUtil.warntrace(e);
            return ipAddress;
        }
    }
}
