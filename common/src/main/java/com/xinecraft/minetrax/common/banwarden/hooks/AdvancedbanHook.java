package com.xinecraft.minetrax.common.banwarden.hooks;

import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.enums.BanWardenPunishmentType;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;
import com.xinecraft.minetrax.common.interfaces.banwarden.BanWardenHook;

public class AdvancedbanHook implements BanWardenHook {

    @Override
    public String punish(BanWardenPunishmentType type, String punishmentString) {
        return "";
    }

    @Override
    public boolean pardon(BanWardenPunishmentType type, String victim, String reason, String admin) {
        return false;
    }

    @Override
    public PunishmentData getPunishment(String punishmentId) {
        return null;
    }

    @Override
    public void sync(BanWardenSyncType type) {

    }
}
