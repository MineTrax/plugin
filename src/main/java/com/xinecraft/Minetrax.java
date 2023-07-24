package com.xinecraft;

import com.xinecraft.commands.AccountLinkCommand;
import com.xinecraft.commands.PlayerWhoisCommand;
import com.xinecraft.commands.WebSayCommand;
import com.xinecraft.data.PlayerData;
import com.xinecraft.data.PlayerSessionIntelData;
import com.xinecraft.hooks.chat.EpicCoreChatHook;
import com.xinecraft.hooks.chat.VentureChatHook;
import com.xinecraft.hooks.placeholderapi.MinetraxPlaceholderExpansion;
import com.xinecraft.listeners.*;
import com.xinecraft.log4j.ConsoleAppender;
import com.xinecraft.log4j.ConsoleMessage;
import com.xinecraft.tasks.AccountLinkReminderTask;
import com.xinecraft.tasks.PlayerAfkAndWorldIntelTrackerTask;
import com.xinecraft.tasks.PlayerIntelReportTask;
import com.xinecraft.tasks.ServerIntelReportTask;
import com.xinecraft.threads.ConsoleMessageQueueWorker;
import com.xinecraft.threads.webquery.NettyWebQueryServer;
import com.xinecraft.utils.PluginUtil;
import com.xinecraft.utils.UpdateChecker;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Minetrax extends JavaPlugin implements Listener {

    @Getter
    private ConsoleMessageQueueWorker consoleMessageQueueWorker;

    @Getter
    private NettyWebQueryServer webQuerySocketServer;

    // Console
    @Getter
    private final Deque<ConsoleMessage> consoleMessageQueue = new LinkedList<>();
    @Getter
    private ConsoleAppender consoleAppender;

    @Getter
    private Boolean isEnabled;
    @Getter
    private Boolean isDebugMode;
    @Getter
    private String apiKey;
    @Getter
    private String apiSecret;
    @Getter
    private String apiServerId;
    @Getter
    private String apiHost;
    @Getter
    private Boolean isChatLogEnabled;
    @Getter
    private Boolean isConsoleLogEnabled;
    @Getter
    private String webQueryHost;
    @Getter
    private int webQueryPort;
    @Getter
    private String webMessageFormat;
    @Getter
    private Boolean isWhoisOnPlayerJoinEnabled;
    @Getter
    private Boolean isWhoisOnCommandEnabled;
    @Getter
    private String whoisNoMatchFoundMessage;
    @Getter
    private List<String> whoisPlayerOnJoinMessage;
    @Getter
    private List<String> whoisPlayerOnFirstJoinMessage;
    @Getter
    private List<String> whoisPlayerOnCommandMessage;
    @Getter
    private Boolean isFireworkOnPlayerJoin;
    @Getter
    private Boolean isFireworkOnPlayerFirstJoin;
    @Getter
    private String fireworkSendAmount;
    @Getter
    private String whoisAdminPermissionName;
    @Getter
    private List<String> whoisPlayerOnAdminCommandMessage;
    @Getter
    private String whoisMultiplePlayersTitleMessage;
    @Getter
    private String whoisMultiplePlayersListMessage;
    @Getter
    private Boolean isRemindPlayerToLinkEnabled;
    @Getter
    private Boolean isServerIntelEnabled;
    @Getter
    private Boolean isPlayerIntelEnabled;
    @Getter
    private Long remindPlayerToLinkInterval;
    @Getter
    private List<String> remindPlayerToLinkMessage;
    @Getter
    private long afkThresholdInMs;
    @Getter
    public HashMap<String, PlayerData> playersDataMap;
    @Getter
    public HashMap<String, PlayerSessionIntelData> playerSessionIntelDataMap;
    @Getter
    public String serverSessionId;
    @Getter
    public Boolean isAllowOnlyWhitelistedCommandsFromWeb;
    @Getter
    public List<String> whitelistedCommandsFromWeb;
    @Getter
    public HashMap<String, String> joinAddressCache = new HashMap<String, String>();

    private static Permission perms = null;
    private static Economy economy = null;

    public static Minetrax getPlugin() {
        return getPlugin(Minetrax.class);
    }

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        getLogger().info("Minetrax Plugin Enabled!");

        // bStats Metric
        int pluginId = 15485;
        Metrics metrics = new Metrics(this, pluginId);

        playersDataMap = new HashMap<String, PlayerData>();
        playerSessionIntelDataMap = new HashMap<String, PlayerSessionIntelData>();

        // Config
        this.saveDefaultConfig();
        isEnabled = this.getConfig().getBoolean("enabled");
        // No need for anything if plugin is DISABLED
        if (!isEnabled) {
            getLogger().warning("Plugin disabled from config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        apiHost = this.getConfig().getString("api-host");
        if (apiHost != null) {
            apiHost = StringUtils.strip(apiHost, "/");
        }
        isDebugMode = this.getConfig().getBoolean("debug-mode");
        apiKey = this.getConfig().getString("api-key");
        apiSecret = this.getConfig().getString("api-secret");
        apiServerId = this.getConfig().getString("server-id");
        isChatLogEnabled = this.getConfig().getBoolean("enable-chatlog");
        isConsoleLogEnabled = this.getConfig().getBoolean("enable-consolelog");
        webQueryHost = this.getConfig().getString("webquery-host");
        webQueryPort = this.getConfig().getInt("webquery-port");
        webMessageFormat = this.getConfig().getString("web-message-format");
        isWhoisOnPlayerJoinEnabled = this.getConfig().getBoolean("enable-whois-on-player-join");
        isWhoisOnCommandEnabled = this.getConfig().getBoolean("enable-whois-on-command");
        whoisNoMatchFoundMessage = this.getConfig().getString("whois-no-match-found-message");
        whoisPlayerOnJoinMessage = this.getConfig().getStringList("whois-player-on-join-message");
        whoisPlayerOnFirstJoinMessage = this.getConfig().getStringList("whois-player-on-first-join-message");
        whoisPlayerOnCommandMessage = this.getConfig().getStringList("whois-player-on-command-message");
        whoisAdminPermissionName = this.getConfig().getString("whois-admin-permission-name");
        whoisPlayerOnAdminCommandMessage = this.getConfig().getStringList("whois-player-on-admin-command-message");
        whoisMultiplePlayersTitleMessage = this.getConfig().getString("whois-multiple-players-title-message");
        whoisMultiplePlayersListMessage = this.getConfig().getString("whois-multiple-players-list-message");
        isRemindPlayerToLinkEnabled = this.getConfig().getBoolean("remind-player-to-link");
        isServerIntelEnabled = this.getConfig().getBoolean("report-server-intel");
        isPlayerIntelEnabled = this.getConfig().getBoolean("report-player-intel");
        remindPlayerToLinkInterval = this.getConfig().getLong("remind-player-interval");
        remindPlayerToLinkMessage = this.getConfig().getStringList("remind-player-link-message");
        afkThresholdInMs = this.getConfig().getLong("afk-threshold-in-seconds", 300) * 1000;
        isFireworkOnPlayerJoin = this.getConfig().getBoolean("enable-firework-on-player-join");
        isFireworkOnPlayerFirstJoin = this.getConfig().getBoolean("enable-firework-on-player-first-join");
        fireworkSendAmount = this.getConfig().getString("join-fireworks-amount");
        isAllowOnlyWhitelistedCommandsFromWeb = this.getConfig().getBoolean("allow-only-whitelisted-commands-from-web");
        whitelistedCommandsFromWeb = this.getConfig().getStringList("whitelisted-commands-from-web");
        serverSessionId = UUID.randomUUID().toString();

        // Disable plugin if host, key, secret or server-id is not there
        if (
                apiHost == null || apiKey == null || apiSecret == null || apiServerId == null ||
                        apiHost.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty() || apiServerId.isEmpty()
        ) {
            getLogger().warning("Plugin disabled due to no API information");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register Commands
        getCommand("link-account").setExecutor(new AccountLinkCommand());
        getCommand("websay").setExecutor(new WebSayCommand());
        getCommand("ww").setExecutor(new PlayerWhoisCommand());

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerAdvancementDoneListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerKickBanListener(), this);
        getServer().getPluginManager().registerEvents(new ServerBroadcastListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDeathEventListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(), this);
        getServer().getPluginManager().registerEvents(new EntityPickupItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerItemBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerItemConsumeListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(), this);
        getServer().getPluginManager().registerEvents(new CraftItemListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantItemListener(), this);
        getServer().getPluginManager().registerEvents(new FishCatchListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerBedEnterListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);
        getServer().getPluginManager().registerEvents(new RaidFinishListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(), this);

        // Register Listeners only required for ChatLogs
        if (isChatLogEnabled) {
            // Register Plugin Hook Listeners only if its plugin exists
            if (PluginUtil.checkIfPluginEnabled("VentureChat")) {
                getLogger().info("Venture Chat is found! Adding Hook...");
                getServer().getPluginManager().registerEvents(new VentureChatHook(), this);
            }

            if (PluginUtil.checkIfPluginEnabled("EpicCore")) {
                getLogger().info("EpicCore is found! Adding Hook...");
                getServer().getPluginManager().registerEvents(new EpicCoreChatHook(), this);
            }
        }

        if (isConsoleLogEnabled) {
            // Attach appender to queue console messages
            consoleAppender = new ConsoleAppender();
            // start console message queue worker thread
            if (consoleMessageQueueWorker != null) {
                if (consoleMessageQueueWorker.getState() != Thread.State.NEW) {
                    consoleMessageQueueWorker.interrupt();
                    consoleMessageQueueWorker = new ConsoleMessageQueueWorker();
                }
            } else {
                consoleMessageQueueWorker = new ConsoleMessageQueueWorker();
            }
            consoleMessageQueueWorker.start();
        }

        // WebQuery Server
        webQuerySocketServer = new NettyWebQueryServer(webQueryHost, webQueryPort);
        webQuerySocketServer.runTaskAsynchronously(this);

        // Vault API Setup
        boolean hasVaultPermission = setupVaultPermission();
        if (!hasVaultPermission) {
            getLogger().info("No Vault supported permission plugin found.");
        } else {
            getLogger().info("Vault Permission Plugin: " + perms.getName());
        }
        boolean hasVaultEconomy = setupVaultEconomy();
        if (!hasVaultEconomy) {
            getLogger().info("No Vault supported economy plugin found.");
        } else {
            getLogger().info("Vault Economy Plugin: " + economy.getName());
        }

        // Setup Schedulers
        if (isRemindPlayerToLinkEnabled) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new AccountLinkReminderTask(), 20 * 20, remindPlayerToLinkInterval * 20L);
        }
        if (isServerIntelEnabled) {
             getServer().getScheduler().runTaskTimerAsynchronously(this, new ServerIntelReportTask(), 60 * 20L, 60 * 20L);   // every minute
        }
        if (isPlayerIntelEnabled) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, new PlayerIntelReportTask(), 5 * 60 * 20L, 5 * 60 * 20L);   // every 5 minutes
        }
        
        getServer().getScheduler().runTaskTimerAsynchronously(this, new PlayerAfkAndWorldIntelTrackerTask(), 20L, 20L);   // Run every seconds

        // Setup PlaceholderAPI
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Hooking into PlaceholderAPI.");
            new MinetraxPlaceholderExpansion(this).register();
        }

        // Update Checker
        checkForPluginUpdates();
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
        getLogger().info("Minetrax Plugin Disabled!");
        HandlerList.unregisterAll();
        if(webQuerySocketServer != null) {
            webQuerySocketServer.shutdown();
        }
    }

    private boolean setupVaultPermission() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean setupVaultEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void checkForPluginUpdates() {
        new UpdateChecker(this, 102635).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("You are currently running the latest version of MineTrax");
            } else {
                getLogger().info("There is a new update available. Please update to latest version.");
            }
        });
    }

    public static Permission getVaultPermission() {
        return perms;
    }

    public static Economy getVaultEconomy() {
        return economy;
    }
}
