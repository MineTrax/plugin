package com.xinecraft.minetrax.common.interfaces.banwarden;

import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.enums.BanWardenPunishmentType;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;

public interface BanWardenHook {
    String punish(BanWardenPunishmentType type, String punishmentString);
    boolean pardon(BanWardenPunishmentType type, String victim, String reason);
    PunishmentData getPunishment(String punishmentId);
    void sync(BanWardenSyncType type);
}
