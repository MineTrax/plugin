package com.xinecraft.minetrax.velocity.tasks;

import com.sun.management.OperatingSystemMXBean;
import com.velocitypowered.api.proxy.ProxyServer;
import com.xinecraft.minetrax.common.actions.ReportServerIntel;
import com.xinecraft.minetrax.common.data.ServerIntelData;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.SystemUtil;
import com.xinecraft.minetrax.velocity.MinetraxVelocity;

import java.lang.management.ManagementFactory;

public class ServerIntelReportTask implements Runnable {
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @Override
    public void run() {
        LoggingUtil.debug("--- SENDING SERVER INTEL ---");
        ServerIntelData serverIntelData = new ServerIntelData();
        ProxyServer proxyServer = MinetraxVelocity.getPlugin().getProxyServer();

        serverIntelData.max_players = proxyServer.getConfiguration().getShowMaxPlayers();
        serverIntelData.online_players = proxyServer.getPlayerCount();
        serverIntelData.max_memory = Runtime.getRuntime().maxMemory() / 1024;
        serverIntelData.total_memory = Runtime.getRuntime().totalMemory() / 1024;
        serverIntelData.free_memory = Runtime.getRuntime().freeMemory() / 1024;

        serverIntelData.available_cpu_count = osBean.getAvailableProcessors();
        serverIntelData.cpu_load = SystemUtil.getAverageCpuLoad();
        serverIntelData.uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        serverIntelData.server_version = proxyServer.getVersion().getVersion();
        serverIntelData.server_session_id = MinetraxVelocity.getPlugin().getServerSessionId();
        serverIntelData.free_disk_in_kb = SystemUtil.getFreeDiskSpaceInKiloBytes();
        serverIntelData.server_id = MinetraxVelocity.getPlugin().getApiServerId();
        try {
            ReportServerIntel.reportSync(serverIntelData);
        } catch (Exception e) {
            LoggingUtil.warning("Failed to report server intel: " + e.getMessage());
        }
    }
}
