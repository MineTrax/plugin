package com.xinecraft.minetrax.bungee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xinecraft.minetrax.bungee.hooks.skinsrestorer.SkinsRestorerHook;
import com.xinecraft.minetrax.bungee.listeners.ServerConnectedListener;
import com.xinecraft.minetrax.bungee.logging.BungeeLogger;
import com.xinecraft.minetrax.bungee.schedulers.BungeeScheduler;
import com.xinecraft.minetrax.bungee.tasks.ServerIntelReportTask;
import com.xinecraft.minetrax.bungee.utils.PluginUtil;
import com.xinecraft.minetrax.bungee.webquery.BungeeWebQuery;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.interfaces.MinetraxPlugin;
import com.xinecraft.minetrax.common.enums.PlatformType;
import com.xinecraft.minetrax.common.webquery.WebQueryServer;
import lombok.AccessLevel;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
public final class MinetraxBungee extends Plugin implements MinetraxPlugin {
    @Getter
    private static MinetraxBungee plugin;

    private WebQueryServer webQueryServer;
    private Configuration config;

    private Boolean isEnabled;
    private Boolean isDebugMode;
    private String apiKey;
    private String apiSecret;
    private String apiServerId;
    private String apiHost;
    private Boolean isConsoleLogEnabled;
    private String webQueryHost;
    private int webQueryPort;
    private List<String> webQueryWhitelistedIps;
    private Boolean isServerIntelEnabled;
    public String serverSessionId;
    public Boolean isAllowOnlyWhitelistedCommandsFromWeb;
    public List<String> whitelistedCommandsFromWeb;
    public HashMap<String, String> joinAddressCache = new HashMap<>();
    public Boolean hasSkinsRestorer = false;
    public Boolean isSkinsRestorerHookEnabled;
    public SkinsRestorer skinsRestorerApi;
    public Gson gson = null;
    private MinetraxCommon common;

    @Override
    public void onEnable() {
        // GSON builder
        gson = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();

        // Setup Common
        common = new MinetraxCommon();
        common.setPlugin(this);
        common.setPlatformType(PlatformType.BUNGEE);
        common.setGson(gson);
        common.setLogger(new BungeeLogger(this));
        common.setScheduler(new BungeeScheduler(this));
        common.setWebQuery(new BungeeWebQuery(this));
        plugin = this;

        // Load configuration
        loadConfig();
        initVariables();
        if (!isEnabled) {
            getLogger().warning("Plugin disabled from config.yml");
            return;
        }
        // Disable plugin if host, key, secret or server-id is not there
        if (
                apiHost == null || apiKey == null || apiSecret == null || apiServerId == null ||
                        apiHost.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty() || apiServerId.isEmpty()
        ) {
            getLogger().severe("Plugin disabled due to no API information");
            return;
        }

        // init Bstats
        initBstats();

        // Start web query server
        startWebQueryServer();

        // Hook into plugins
        if (PluginUtil.checkIfPluginEnabled("SkinsRestorer")) {
            hasSkinsRestorer = setupSkinsRestorer();
        }

        // Register Channels
        getProxy().registerChannel(MinetraxCommon.PLUGIN_MESSAGE_CHANNEL);

        // Register Listeners
        getProxy().getPluginManager().registerListener(this, new ServerConnectedListener());

        // Register Tasks
        if (isServerIntelEnabled) {
            getProxy().getScheduler().schedule(this, new ServerIntelReportTask(), 60, 60, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
        webQueryServer.shutdown();

        // Unregister channels
        getProxy().unregisterChannel(MinetraxCommon.PLUGIN_MESSAGE_CHANNEL);
    }

    private void loadConfig() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                throw new RuntimeException("Unable to create the plugin data folder " + getDataFolder());
            }
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                getLogger().info("Please wait. Configuring MineTrax for the first time...");
                if (!configFile.createNewFile()) {
                    throw new IOException("Unable to create the config file at " + configFile);
                }

                Files.copy(getResourceAsStream("bungeeConfig.yml"), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load configuration", e);
        }
    }

    private void initVariables() {
        isEnabled = config.getBoolean("enabled", true);
        isDebugMode = config.getBoolean("debug-mode", false);
        apiKey = config.getString("api-key", null);
        apiSecret = config.getString("api-secret", null);
        apiServerId = String.valueOf(config.getInt("server-id", 0));
        apiHost = config.getString("api-host", null);
        webQueryHost = config.getString("webquery-host", null);
        webQueryPort = config.getInt("webquery-port", 25575);
        webQueryWhitelistedIps = config.getStringList("webquery-whitelisted-ips");
        isServerIntelEnabled = config.getBoolean("report-server-intel", false);
        isConsoleLogEnabled = config.getBoolean("enable-consolelog", true);
        isAllowOnlyWhitelistedCommandsFromWeb = config.getBoolean("allow-only-whitelisted-commands-from-web", false);
        whitelistedCommandsFromWeb = config.getStringList("whitelisted-commands-from-web");
        isSkinsRestorerHookEnabled = config.getBoolean("enable-skinsrestorer-hook", false);
        serverSessionId = UUID.randomUUID().toString();
    }

    private void initBstats() {
        int pluginId = 15485;
        Metrics metrics = new Metrics(this, pluginId);
    }

    private void startWebQueryServer() {
        webQueryServer = new WebQueryServer(webQueryHost, webQueryPort, webQueryWhitelistedIps);
        webQueryServer.start();
    }

    private Boolean setupSkinsRestorer() {
        if (!isSkinsRestorerHookEnabled) {
            getLogger().info("SkinsRestorer is found! But SkinsRestorer hook is disabled in config.");
            return false;
        }

        getLogger().info("Hooking into SkinsRestorer...");

        // Add SkinsRestorerHook
        try {
            skinsRestorerApi = SkinsRestorerProvider.get();
            skinsRestorerApi.getEventBus().subscribe(this, SkinApplyEvent.class, new SkinsRestorerHook());

            // Warn if SkinsRestorer is not compatible with v15
            if (!VersionProvider.isCompatibleWith("15")) {
                getLogger().warning("MineTrax supports SkinsRestorer v15, but " + VersionProvider.getVersionInfo() + " is installed. There may be errors!");
            }
            getLogger().info("Hooked into SkinsRestorer!");
            return true;
        } catch (Exception e) {
            getLogger().warning("MineTrax failed to hook into SkinsRestorer!");
            getLogger().warning("Error: " + e.getMessage());
            return false;
        }
    }
}
