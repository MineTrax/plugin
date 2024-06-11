package com.xinecraft.minetrax.common.webquery.protocol;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.data.WebQueryRequestData;
import com.xinecraft.minetrax.common.utils.CryptoUtil;
import com.xinecraft.minetrax.common.utils.LoggingUtil;

public class WebQueryProtocol {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static String processInput(String input) throws Exception {
        // Decrypt the input to plain text and map in JSON object.
        WebQueryRequestData requestData = decryptRequest(input);

        LoggingUtil.debug("Received webquery request: " + requestData.toString());

        // Validate signature.
        if (!validateSignature(requestData)) {
            throw new Exception("Invalid signature");
        }

        String payload = requestData.getPayload();
        JsonObject payloadJson = common.getGson().fromJson(payload, JsonObject.class);
        String type = payloadJson.get("type").getAsString();

        String response;
        switch (type) {
            case "status":
               response = common.getWebQuery().handleStatus();
               break;
            case "ping":
                response = common.getWebQuery().handlePing();
                break;
            case "user-say":
                String username = payloadJson.get("username").getAsString();
                String message = payloadJson.get("message").getAsString();
                response = common.getWebQuery().handleUserSay(username, message);
                break;
            case "broadcast":
                String broadcastMessage = payloadJson.get("message").getAsString();
                response = common.getWebQuery().handleBroadcast(broadcastMessage);
                break;
            case "command":
                String command = payloadJson.get("command").getAsString();
                response = common.getWebQuery().handleCommand(command);
                break;
            case "set-player-skin":
                String playerUuid = payloadJson.get("player_uuid").getAsString();
                String changeCommandType = payloadJson.get("change_command_type").getAsString();
                String value = payloadJson.get("value").isJsonNull() ? null : payloadJson.get("value").getAsString();
                response = common.getWebQuery().handleSetPlayerSkin(playerUuid, changeCommandType, value);
                break;
            case "account-link-success":
                String pUuid = payloadJson.get("player_uuid").getAsString();
                String userId = payloadJson.get("user_id").getAsString();
                response = common.getWebQuery().handleAccountLinkSuccess(pUuid, userId);
                break;
            case "check-player-online":
                String playerUuidForCheck = payloadJson.get("player_uuid").getAsString();
                response = common.getWebQuery().handleCheckPlayerOnline(playerUuidForCheck);
                break;
            default:
                throw new Exception("Invalid webquery command type");
        }

        // Encrypt the response and return.
        return encryptResponse(response);
    }

    private static WebQueryRequestData decryptRequest(String input) {
        String apiKey = common.getPlugin().getApiKey();
        String decrypted = CryptoUtil.getDecryptedString(apiKey, input);

        return common.getGson().fromJson(decrypted, WebQueryRequestData.class);
    }

    private static String encryptResponse(String response) {
        return CryptoUtil.getEncryptedString(common.getPlugin().getApiKey(), response);
    }

    private static boolean validateSignature(WebQueryRequestData requestData) {
        String secretKey = common.getPlugin().getApiSecret().substring(0, 32);
        String signature = requestData.getSignature();
        String payload = requestData.getPayload();

        return CryptoUtil.verifyHmacSignature(secretKey, payload, signature);
    }
}
