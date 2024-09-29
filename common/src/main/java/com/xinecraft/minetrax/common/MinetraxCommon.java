package com.xinecraft.minetrax.common;

import com.google.gson.Gson;
import com.xinecraft.minetrax.common.banwarden.BanWarden;
import com.xinecraft.minetrax.common.banwarden.hooks.AdvancedbanHook;
import com.xinecraft.minetrax.common.banwarden.hooks.LibertybansHook;
import com.xinecraft.minetrax.common.banwarden.hooks.LitebansHook;
import com.xinecraft.minetrax.common.banwarden.hooks.NullHook;
import com.xinecraft.minetrax.common.enums.BanWardenPluginType;
import com.xinecraft.minetrax.common.enums.PlatformType;
import com.xinecraft.minetrax.common.interfaces.MinetraxPlugin;
import com.xinecraft.minetrax.common.interfaces.banwarden.BanWardenHook;
import com.xinecraft.minetrax.common.interfaces.logging.CommonLogger;
import com.xinecraft.minetrax.common.interfaces.schedulers.CommonScheduler;
import com.xinecraft.minetrax.common.interfaces.webquery.CommonWebQuery;
import lombok.Getter;
import lombok.Setter;
import net.skinsrestorer.api.SkinsRestorer;

@Setter
@Getter
public class MinetraxCommon {
    @Getter
    private static MinetraxCommon instance;
    private CommonLogger logger;
    private CommonScheduler scheduler;
    private CommonWebQuery webQuery;
    private PlatformType platformType;
    private Gson gson;
    private MinetraxPlugin plugin;
    private SkinsRestorer skinsRestorerApi;
    private BanWardenPluginType banWardenPluginType;
    private BanWarden banWarden;
    public static String PLUGIN_MESSAGE_CHANNEL = "minetrax:main";

    public MinetraxCommon() {
        instance = this;
    }

    public void initBanWarden(BanWardenPluginType banWardenPluginType) {
        this.banWardenPluginType = banWardenPluginType;

        BanWardenHook hook = switch (banWardenPluginType) {
            case LITEBANS -> new LitebansHook();
            case LIBERTYBANS -> new LibertybansHook();
            case ADVANCEDBAN -> new AdvancedbanHook();
            default -> new NullHook();
        };

        banWarden = new BanWarden(hook);
        logger.info("[BanWarden] Hooked into " + banWardenPluginType.name());
    }
}
