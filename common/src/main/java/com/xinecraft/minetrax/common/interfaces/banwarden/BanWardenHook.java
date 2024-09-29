package com.xinecraft.minetrax.common.interfaces.banwarden;

import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;

public interface BanWardenHook {
    void upsert(PunishmentData punishmentData);
    void remove(String punishmentId);
    PunishmentData getPunishment(String punishmentId);
    void sync(BanWardenSyncType type);
}
