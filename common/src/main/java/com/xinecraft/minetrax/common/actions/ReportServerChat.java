package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

public class ReportServerChat {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static void reportAsync(String type, String message, String causerUsername, String causerUuid) {
        common.getScheduler().runAsync(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("type", type);
                payload.addProperty("chat", message);
                payload.addProperty("causer_username", causerUsername);
                payload.addProperty("causer_uuid", causerUuid);
                payload.addProperty("server_id", common.getPlugin().getApiServerId());

                HttpResponse response = MinetraxHttpUtil.post(MinetraxHttpUtil.REPORT_SERVER_CHAT_ROUTE, payload.toString(), null);

                if (!response.isSuccessful()) {
                    common.getLogger().warning("Failed to report chat message: " + response.body());
                }
            } catch (Exception e) {
                common.getLogger().warning(e.getMessage());
            }
        });
    }
}
