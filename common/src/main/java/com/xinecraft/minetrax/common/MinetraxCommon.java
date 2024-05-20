package com.xinecraft.minetrax.common;

import com.google.gson.Gson;
import com.xinecraft.minetrax.common.enums.PlatformType;
import com.xinecraft.minetrax.common.interfaces.MinetraxPlugin;
import com.xinecraft.minetrax.common.interfaces.logging.CommonLogger;
import com.xinecraft.minetrax.common.interfaces.schedulers.CommonScheduler;
import com.xinecraft.minetrax.common.interfaces.webquery.CommonWebQuery;
import lombok.Getter;
import lombok.Setter;

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
    public static String PLUGIN_MESSAGE_CHANNEL = "minetrax:main";

    public MinetraxCommon() {
        instance = this;
    }
}
