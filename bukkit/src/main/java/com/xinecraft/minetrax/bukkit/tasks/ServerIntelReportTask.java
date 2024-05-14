package com.xinecraft.minetrax.bukkit.tasks;

import com.sun.management.OperatingSystemMXBean;
import com.xinecraft.minetrax.bukkit.MinetraxBukkit;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.SystemUtil;
import com.xinecraft.minetrax.bukkit.utils.TpsUtil;
import com.xinecraft.minetrax.common.actions.ReportServerIntel;
import com.xinecraft.minetrax.common.data.ServerIntelData;
import com.xinecraft.minetrax.common.data.WorldData;
import org.bukkit.World;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServerIntelReportTask implements Runnable {
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @Override
    public void run() {
        LoggingUtil.info("--- SENDING SERVER INTEL ---");
        ServerIntelData serverIntelData = new ServerIntelData();

        serverIntelData.max_players = MinetraxBukkit.getPlugin().getServer().getMaxPlayers();
        serverIntelData.online_players = MinetraxBukkit.getPlugin().getServer().getOnlinePlayers().size();
        serverIntelData.max_memory = Runtime.getRuntime().maxMemory() / 1024;
        serverIntelData.total_memory = Runtime.getRuntime().totalMemory() / 1024;
        serverIntelData.free_memory = Runtime.getRuntime().freeMemory() / 1024;

        double tps = 0.0;
        try {
            tps = TpsUtil.getTPS();
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            DecimalFormat format = new DecimalFormat("##.##", decimalFormatSymbols);
            tps = Double.parseDouble(format.format(tps).replace(",", "."));
        } catch (Exception e) {
            tps = 00.00;
        }

        serverIntelData.tps = tps;
        serverIntelData.available_cpu_count = osBean.getAvailableProcessors();
        serverIntelData.cpu_load = SystemUtil.getAverageCpuLoad();
        serverIntelData.uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        serverIntelData.motd = MinetraxBukkit.getPlugin().getServer().getMotd();
        serverIntelData.server_version = MinetraxBukkit.getPlugin().getServer().getVersion();
        serverIntelData.server_session_id = MinetraxBukkit.getPlugin().serverSessionId;

        serverIntelData.free_disk_in_kb = SystemUtil.getFreeDiskSpaceInKiloBytes();
        serverIntelData.chunks_loaded = 0;

        ArrayList<WorldData> worldDataList = new ArrayList<>();
        final List<World> worlds = MinetraxBukkit.getPlugin().getServer().getWorlds();
        for (final World w : worlds) {
            WorldData worldData = new WorldData();
            worldData.world_name = w.getName();
            worldData.environment = w.getEnvironment().toString();
            worldData.world_border = w.getWorldBorder().getSize();
            worldData.chunks_loaded = w.getLoadedChunks().length;
            // Quick Hack for 1.12 as getGameTime() doesn't exist there.
            try {
                worldData.game_time = w.getGameTime();
            } catch (NoSuchMethodError e) {
                worldData.game_time = 0;
            }
            worldData.online_players = w.getPlayers().size();
            worldDataList.add(worldData);
            serverIntelData.chunks_loaded = serverIntelData.chunks_loaded + worldData.chunks_loaded;
        }
        serverIntelData.world_data = worldDataList;
        serverIntelData.server_id = MinetraxBukkit.getPlugin().getApiServerId();

        // Perform the API request
        try {
            ReportServerIntel.reportSync(serverIntelData);
        } catch (Exception e) {
            LoggingUtil.warning("Failed to report server intel: " + e.getMessage());
        }
    }
}
