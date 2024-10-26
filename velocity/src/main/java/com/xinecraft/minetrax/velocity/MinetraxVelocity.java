package com.xinecraft.minetrax.velocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.enums.BanWardenPluginType;
import com.xinecraft.minetrax.common.enums.PlatformType;
import com.xinecraft.minetrax.common.interfaces.MinetraxPlugin;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.webquery.WebQueryServer;
import com.xinecraft.minetrax.velocity.hooks.skinsrestorer.SkinsRestorerHook;
import com.xinecraft.minetrax.velocity.listeners.ServerConnectedListener;
import com.xinecraft.minetrax.velocity.logging.VelocityLogger;
import com.xinecraft.minetrax.velocity.schedulers.VelocityScheduler;
import com.xinecraft.minetrax.velocity.tasks.ServerIntelReportTask;
import com.xinecraft.minetrax.velocity.utils.PluginUtil;
import com.xinecraft.minetrax.velocity.webquery.VelocityWebQuery;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.VersionProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
@Plugin(
        id = "minetrax",
        name = "Minetrax",
        authors = {"Xinecraft"},
        version = BuildConstants.VERSION,
        dependencies = {
                @Dependency(id = "skinsrestorer", optional = true)
        }
)
public class MinetraxVelocity implements MinetraxPlugin {
    @Inject
    private Logger logger;
    @Inject
    @DataDirectory
    private Path dataPath;
    @Inject
    private ProxyServer proxyServer;
    @Inject
    private Metrics.Factory metricsFactory;
    @Inject
    private PluginContainer pluginContainer;
    @Getter
    private static MinetraxVelocity plugin;

    private WebQueryServer webQueryServer;
    private Gson gson = null;
    private MinetraxCommon common;
    private YamlDocument config;

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
    public ConcurrentHashMap<String, String> joinAddressCache = new ConcurrentHashMap<>();
    public Boolean hasSkinsRestorer = false;
    public Boolean isSkinsRestorerHookEnabled;
    public Boolean isBanWardenEnabled = false;
    public static final MinecraftChannelIdentifier PLUGIN_MESSAGE_CHANNEL = MinecraftChannelIdentifier.from(MinetraxCommon.PLUGIN_MESSAGE_CHANNEL);

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        plugin = this;

        // Load config
        loadConfig();
        initVariables();
        if (!isEnabled) {
            logger.warn("Plugin disabled from config.yml");
            return;
        }
        // Disable plugin if host, key, secret or server-id is not there
        if (
                apiHost == null || apiKey == null || apiSecret == null || apiServerId == null ||
                        apiHost.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty() || apiServerId.isEmpty()
        ) {
            logger.error("Plugin disabled due to no API information");
            return;
        }

        // GSON builder
        gson = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();

        // Setup Common
        common = new MinetraxCommon();
        common.setPlugin(this);
        common.setPlatformType(PlatformType.VELOCITY);
        common.setGson(gson);
        common.setLogger(new VelocityLogger(this));
        common.setScheduler(new VelocityScheduler(this));
        common.setWebQuery(new VelocityWebQuery(this));
        initBanWarden(common);

        // init Bstats
        initBstats();

        // Start web query server
        startWebQueryServer();

        // Hook into plugins
        if (PluginUtil.checkIfPluginEnabled("skinsrestorer")) {
            hasSkinsRestorer = setupSkinsRestorer();
        }

        // Register Channels
        proxyServer.getChannelRegistrar().register(PLUGIN_MESSAGE_CHANNEL);

        // Register Listeners
        proxyServer.getEventManager().register(this, new ServerConnectedListener());

        // Register Tasks
        if (isServerIntelEnabled) {
            proxyServer.getScheduler()
                    .buildTask(plugin, new ServerIntelReportTask())
                    .delay(60L, TimeUnit.SECONDS)
                    .repeat(60L, TimeUnit.SECONDS)
                    .schedule();
        }
    }

    private void startWebQueryServer() {
        webQueryServer = new WebQueryServer(webQueryHost, webQueryPort, webQueryWhitelistedIps);
        webQueryServer.start();
    }

    private void loadConfig() {
        // Create and update the file
        try {
            config = YamlDocument.create(new File(getDataPath().toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/velocityConfig.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());
        } catch (IOException ex) {
            LoggingUtil.warntrace(ex);
        }
    }

    private void initVariables() {
        isEnabled = config.getBoolean("enabled");
        isDebugMode = config.getBoolean("debug-mode");
        apiKey = config.getString("api-key");
        isEnabled = config.getBoolean("enabled", true);
        isDebugMode = config.getBoolean("debug-mode", false);
        apiKey = config.getString("api-key", null);
        apiSecret = config.getString("api-secret", null);
        apiServerId = config.getString("server-id", null);
        apiHost = config.getString("api-host", null);
        webQueryHost = config.getString("webquery-host", null);
        webQueryPort = config.getInt("webquery-port", 25575);
        webQueryWhitelistedIps = config.getStringList("webquery-whitelisted-ips");
        isServerIntelEnabled = config.getBoolean("report-server-intel", false);
        isConsoleLogEnabled = config.getBoolean("enable-consolelog", false);
        isAllowOnlyWhitelistedCommandsFromWeb = config.getBoolean("allow-only-whitelisted-commands-from-web", false);
        whitelistedCommandsFromWeb = config.getStringList("whitelisted-commands-from-web");
        isSkinsRestorerHookEnabled = config.getBoolean("enable-skinsrestorer-hook", false);
        isBanWardenEnabled = config.getBoolean("enable-banwarden", false);
        serverSessionId = UUID.randomUUID().toString();
    }

    private void initBstats() {
        int pluginId = 15485;
        Metrics metrics = metricsFactory.make(this, pluginId);
    }

    private Boolean setupSkinsRestorer() {
        if (!isSkinsRestorerHookEnabled) {
            logger.info("SkinsRestorer is found! But SkinsRestorer hook is disabled in config.");
            return false;
        }

        logger.info("Hooking into SkinsRestorer...");

        // Add SkinsRestorerHook
        try {
            common.setSkinsRestorerApi(SkinsRestorerProvider.get());
            common.getSkinsRestorerApi().getEventBus().subscribe(pluginContainer, SkinApplyEvent.class, new SkinsRestorerHook());

            // Warn if SkinsRestorer is not compatible with v15
            if (!VersionProvider.isCompatibleWith("15")) {
                logger.warn("MineTrax supports SkinsRestorer v15, but " + VersionProvider.getVersionInfo() + " is installed. There may be errors!");
            }
            logger.info("Hooked into SkinsRestorer!");
            return true;
        } catch (Exception e) {
            logger.warn("MineTrax failed to hook into SkinsRestorer!");
            logger.warn("Error: " + e.getMessage());
            return false;
        }
    }


    private void initBanWarden(MinetraxCommon common) {
        if (!isBanWardenEnabled) {
            logger.warn("[BanWarden] BanWarden is disabled in config.yml");
            return;
        }

        // set which ban plugin is enabled.
        if (PluginUtil.checkIfPluginEnabled("litebans")) {
            common.initBanWarden(BanWardenPluginType.LITEBANS);
        } else if (PluginUtil.checkIfPluginEnabled("libertybans")) {
            common.initBanWarden(BanWardenPluginType.LIBERTYBANS);
        } else if (PluginUtil.checkIfPluginEnabled("advancedban")) {
            common.initBanWarden(BanWardenPluginType.ADVANCEDBAN);
        } else {
            isBanWardenEnabled = false;
            logger.warn("[BanWarden] No supported BanWarden plugin found.");
            return;
        }
    }
}
