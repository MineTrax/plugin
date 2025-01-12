package com.xinecraft.minetrax.common.interfaces.webquery;

import com.xinecraft.minetrax.common.enums.BanWardenPunishmentType;

public interface CommonWebQuery {
    String handleStatus() throws Exception;
    String handlePing() throws Exception;
    String handleUserSay(String user, String message) throws Exception;
    String handleBroadcast(String message) throws Exception;
    String handleCommand(String command) throws Exception;
    String handleSetPlayerSkin(String playerUuid, String commandType, String value) throws Exception;
    String handleAccountLinkSuccess(String playerUuid, String userId) throws Exception;
    String handleCheckPlayerOnline(String playerUuid) throws Exception;
    String handleBanwardenPardon(BanWardenPunishmentType type, String victim, String reason, String admin) throws Exception;
}
