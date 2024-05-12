package com.xinecraft.minetrax.common.interfaces.webquery;

public interface CommonWebQuery {
    String handleStatus() throws Exception;
    String handleUserSay(String user, String message) throws Exception;
    String handleBroadcast(String message) throws Exception;
    String handleCommand(String command) throws Exception;
    String handleSetPlayerSkin(String playerUuid, String commandType, String value) throws Exception;
    String handleAccountLinkSuccess(String playerUuid, String userId) throws Exception;
}
