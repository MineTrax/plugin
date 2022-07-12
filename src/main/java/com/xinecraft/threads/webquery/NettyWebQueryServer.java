package com.xinecraft.threads.webquery;

import com.xinecraft.Minetrax;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.bukkit.scheduler.BukkitRunnable;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

public class NettyWebQueryServer extends BukkitRunnable {
    private int port;
    private String host;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyWebQueryServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SimpleTCPChannelInitializer())
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f;
            if (host != null && !host.isEmpty())
                f = b.bind(host, port).sync();
            else
                f = b.bind(port).sync();

            if(f.isSuccess()) {
                Minetrax.getPlugin().getLogger().info("WebQuery Server started successfully on port " + port);
                serverChannel = f.channel();
            }

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        if (serverChannel != null) {
            try {
                serverChannel.close().syncUninterruptibly();
            } catch (Exception e) {
                Minetrax.getPlugin().getLogger().warning("Unable to shutdown webquery channel! " + e.getMessage());
            }
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();

        try {
            bossGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            workerGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
