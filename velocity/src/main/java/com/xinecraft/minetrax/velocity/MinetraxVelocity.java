package com.xinecraft.minetrax.velocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.interfaces.MinetraxPlugin;
import com.xinecraft.minetrax.common.enums.PlatformType;
import com.xinecraft.minetrax.common.webquery.WebQueryServer;
import com.xinecraft.minetrax.velocity.logging.VelocityLogger;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import net.skinsrestorer.api.SkinsRestorer;
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
        version = BuildConstants.VERSION
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
    private Boolean isServerIntelEnabled;
    private Boolean isPlayerIntelEnabled;
    public String serverSessionId;
    public Boolean isAllowOnlyWhitelistedCommandsFromWeb;
    public List<String> whitelistedCommandsFromWeb;
    public HashMap<String, String> joinAddressCache = new HashMap<>();
    public Boolean hasSkinRestorer = false;
    public SkinsRestorer skinsRestorerApi;
    public Boolean isSkinsRestorerHookEnabled;
    public HashMap<String, String> skinRestorerValueCache = new HashMap<>();

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

        // Setup Common
        common = new MinetraxCommon();
        common.setPlatformType(PlatformType.VELOCITY);
        common.setGson(gson);
        common.setLogger(new VelocityLogger(this));
        common.setPlugin(this);

        // Start web query server
        startWebQueryServer();
    }

    private void startWebQueryServer() {
        webQueryServer = new WebQueryServer(webQueryHost, webQueryPort);
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
