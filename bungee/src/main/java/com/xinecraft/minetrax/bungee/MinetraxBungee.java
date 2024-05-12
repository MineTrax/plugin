package com.xinecraft.minetrax.bungee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xinecraft.minetrax.bungee.logging.BungeeLogger;
import com.xinecraft.minetrax.bungee.schedulers.BungeeScheduler;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.interfaces.MinetraxPlugin;
import com.xinecraft.minetrax.common.enums.PlatformType;
import com.xinecraft.minetrax.common.webquery.WebQueryServer;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

@Getter
public final class MinetraxBungee extends Plugin implements MinetraxPlugin {
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
    private Boolean isServerIntelEnabled;
    private Boolean isPlayerIntelEnabled;
    public String serverSessionId;
    public Boolean isAllowOnlyWhitelistedCommandsFromWeb;
    public List<String> whitelistedCommandsFromWeb;
    public HashMap<String, String> joinAddressCache = new HashMap<>();
    public Boolean hasSkinRestorer = false;
    public Boolean isSkinsRestorerHookEnabled;
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

        // Load configuration
        loadConfig();
        isEnabled = config.getBoolean("enabled", true);
        isDebugMode = config.getBoolean("debug-mode", false);
        apiKey = config.getString("api-key", null);
        apiSecret = config.getString("api-secret", null);
        apiServerId = config.getString("server-id", null);
        apiHost = config.getString("api-host", null);
        webQueryHost = config.getString("webquery-host", null);
        webQueryPort = config.getInt("webquery-port", 25575);

        // Start web query server
        startWebQueryServer();
    }

    @Override
    public void onDisable() {
        webQueryServer.shutdown();
    }

    private void loadConfig() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                throw new RuntimeException("Unable to create the plugin data folder " + getDataFolder());
            }
        }

        File configFile = new File(getDataFolder() , "config.yml");
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

    private void startWebQueryServer() {
        webQueryServer = new WebQueryServer(webQueryHost, webQueryPort);
        webQueryServer.start();
    }
}
