package com.xinecraft.minetrax.common.webquery;

import com.xinecraft.minetrax.common.utils.LoggingUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.FastThreadLocalThread;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class WebQueryServer {
    private static final boolean USE_EPOLL = Epoll.isAvailable();

    private final String host;
    private final int port;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;

    public WebQueryServer(String host, int port) {
        this.host = host;
        this.port = port;
        if (USE_EPOLL) {
            bossGroup = new EpollEventLoopGroup(1, createThreadFactory("WebQueryServer epoll boss"));
            workerGroup = new EpollEventLoopGroup(3, createThreadFactory("WebQueryServer epoll worker"));
        } else {
            bossGroup = new NioEventLoopGroup(1, createThreadFactory("WebQueryServer nio boss"));
            workerGroup = new NioEventLoopGroup(3, createThreadFactory("WebQueryServer nio worker"));
        }
    }

    private static ThreadFactory createThreadFactory(String name) {
        return runnable -> {
            FastThreadLocalThread thread = new FastThreadLocalThread(runnable, name);
            thread.setDaemon(true);
            return thread;
        };
    }

    public void start() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.channel(USE_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .group(bossGroup, workerGroup)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebQueryServerInitializer());

            ChannelFuture serverChannelFuture;
            if (host != null && !host.isEmpty()) {
                serverChannelFuture = b.bind(host, port);
            } else {
                serverChannelFuture = b.bind(port);
            }

            serverChannelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    serverChannel = future.channel();
                    LoggingUtil.info("WebQuery Server started on port " + ((InetSocketAddress) future.channel().localAddress()).getPort());
                } else {
                    LoggingUtil.error("WebQuery Server failed to start: " + future.cause());
                }
            });
        } catch (Exception e) {
            LoggingUtil.error("WebQuery Server interrupted: " + e.getMessage());
            LoggingUtil.trace(e);
        }
    }

    public void shutdown() {
        if (serverChannel != null) {
            try {
                serverChannel.close().syncUninterruptibly();
            } catch (Exception e) {
                LoggingUtil.warning("Unable to shutdown server channel: " + e);
            }
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        try {
            bossGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            workerGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
