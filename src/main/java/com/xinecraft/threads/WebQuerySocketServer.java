package com.xinecraft.threads;

import com.xinecraft.Minetrax;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class WebQuerySocketServer extends BukkitRunnable {
    static ServerSocket socket;
    private boolean running = true;

    public WebQuerySocketServer(String host, int port)
    {
        try {
            socket = new ServerSocket();
            socket.bind(new InetSocketAddress(host, port));
            Minetrax.getPlugin().getLogger().info("WebQuery successfully started on " + host + ":" + port);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            PrintWriter out = null;
            try {
                Socket sock;
                sock = socket.accept();
                sock.setSoTimeout(5000);

                BufferedReader br;
                br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintWriter(sock.getOutputStream(), true);

                String output;
                String str;
                str = br.readLine();

                if (str == null || str.isEmpty())
                {
                    output = "err";
                }
                else {
                    output = WebQueryProtocol.processInput(str);
                }

                out.println(output);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    public void close() {
        try {
            cancel();
            socket.close();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
