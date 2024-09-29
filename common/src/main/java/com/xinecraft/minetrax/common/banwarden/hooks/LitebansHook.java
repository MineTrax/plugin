package com.xinecraft.minetrax.common.banwarden.hooks;

import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;
import com.xinecraft.minetrax.common.interfaces.banwarden.BanWardenHook;

public class LitebansHook implements BanWardenHook {
    @Override
    public void upsert(PunishmentData punishmentData) {

    }

    @Override
    public void remove(String punishmentId) {

    }

    @Override
    public PunishmentData getPunishment(String punishmentId) {
        return null;
    }

    @Override
    public void sync(BanWardenSyncType type) {
        System.out.println("Syncing with Litebans from LitebansHook");
    }
}
