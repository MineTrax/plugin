package com.xinecraft.minetrax.velocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.interfaces.MinetraxPlugin;
import com.xinecraft.minetrax.common.enums.PlatformType;
import com.xinecraft.minetrax.common.webquery.WebQueryServer;
import com.xinecraft.minetrax.velocity.logging.VelocityLogger;
import com.xinecraft.minetrax.velocity.schedulers.VelocityScheduler;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Getter
@Plugin(
        id = "minetrax",
        name = "Minetrax",
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
    private ProxyServer server;

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
    public HashMap<String, String> joinAddressCache = new HashMap<>();
    public Boolean hasSkinRestorer = false;
    public Boolean isSkinsRestorerHookEnabled;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Hello, Velocity!, This is a MineTrax plugin");
        // GSON builder
        gson = new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();

        // Load config
        loadConfig();
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

        // Setup Common
        common = new MinetraxCommon();
        common.setPlugin(this);
        common.setPlatformType(PlatformType.VELOCITY);
        common.setGson(gson);
        common.setLogger(new VelocityLogger(this));
        common.setScheduler(new VelocityScheduler(this));

        // Start web query server
        startWebQueryServer();
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
            ex.printStackTrace();
        }
    }
}
