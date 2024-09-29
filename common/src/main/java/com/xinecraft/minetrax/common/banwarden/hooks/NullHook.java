package com.xinecraft.minetrax.common.banwarden.hooks;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;
import com.xinecraft.minetrax.common.interfaces.banwarden.BanWardenHook;

public class NullHook implements BanWardenHook {
    public static final MinetraxCommon common = MinetraxCommon.getInstance();

    @Override
    public void upsert(PunishmentData punishmentData) {
        warn();
    }

    @Override
    public void remove(String punishmentId) {
        warn();
    }

    @Override
    public PunishmentData getPunishment(String punishmentId) {
        warn();
        return null;
    }

    @Override
    public void sync(BanWardenSyncType type) {
        warn();
    }

    private void warn() {
        common.getLogger().warning("[NullHook] No ban plugin found. Please contact plugin developer if you see this message.");
    }
}
