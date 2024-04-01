package com.xinecraft.minetrax.threads.webquery;

import com.xinecraft.minetrax.Minetrax;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.bukkit.scheduler.BukkitRunnable;

public class NettyWebQueryServer extends BukkitRunnable {
    private int port;
    private String host;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private ChannelFuture serverChannelFuture;

    public NettyWebQueryServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    @Override
    public void run() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SimpleTCPChannelInitializer())
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            if (host != null && !host.isEmpty())
                serverChannelFuture = b.bind(host, port).sync();
            else
                serverChannelFuture = b.bind(port).sync();

            if (serverChannelFuture.isSuccess()) {
                Minetrax.getPlugin().getLogger().info("WebQuery Server started successfully on port " + port);
            }

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            serverChannelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Minetrax.getPlugin().getLogger().warning("WebQuery Server interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        if (serverChannelFuture != null) {
            serverChannelFuture.channel().close().syncUninterruptibly();
        }
        workerGroup.shutdownGracefully().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
    }
}
