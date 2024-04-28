package com.xinecraft.minetrax.common.interfaces;

import com.xinecraft.minetrax.common.MinetraxCommon;

public interface MinetraxPlugin {
    MinetraxCommon getCommon();
    String getApiKey();
    String getApiSecret();
    String getApiServerId();
    String getApiHost();

    String getServerSessionId();
}
