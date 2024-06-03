package com.xinecraft.minetrax.bungee.tasks;

import com.sun.management.OperatingSystemMXBean;
import com.xinecraft.minetrax.bungee.MinetraxBungee;
import com.xinecraft.minetrax.common.actions.ReportServerIntel;
import com.xinecraft.minetrax.common.data.ServerIntelData;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.SystemUtil;
import net.md_5.bungee.api.ProxyServer;

import java.lang.management.ManagementFactory;

public class ServerIntelReportTask implements Runnable {
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @Override
    public void run() {
        LoggingUtil.debug("--- SENDING SERVER INTEL ---");
        ServerIntelData serverIntelData = new ServerIntelData();
        ProxyServer proxyServer = MinetraxBungee.getPlugin().getProxy();

        serverIntelData.max_players = proxyServer.getConfig().getPlayerLimit();
        serverIntelData.online_players = proxyServer.getOnlineCount();
        serverIntelData.max_memory = Runtime.getRuntime().maxMemory() / 1024;
        serverIntelData.total_memory = Runtime.getRuntime().totalMemory() / 1024;
        serverIntelData.free_memory = Runtime.getRuntime().freeMemory() / 1024;

        serverIntelData.available_cpu_count = osBean.getAvailableProcessors();
        serverIntelData.cpu_load = SystemUtil.getAverageCpuLoad();
        serverIntelData.uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        serverIntelData.server_version = MinetraxBungee.getPlugin().getProxy().getVersion();
        serverIntelData.server_session_id = MinetraxBungee.getPlugin().getServerSessionId();
        serverIntelData.free_disk_in_kb = SystemUtil.getFreeDiskSpaceInKiloBytes();
        serverIntelData.server_id = MinetraxBungee.getPlugin().getApiServerId();
        try {
            ReportServerIntel.reportSync(serverIntelData);
        } catch (Exception e) {
            LoggingUtil.warning("Failed to report server intel: " + e.getMessage());
        }
    }
}
