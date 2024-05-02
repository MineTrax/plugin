package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.data.ServerIntelData;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

public class ReportServerIntel {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static void reportSync(ServerIntelData data) {
        try {
            String payload = common.getGson().toJson(data);
            HttpResponse response = MinetraxHttpUtil.post(MinetraxHttpUtil.SERVER_INTEL_REPORT_ROUTE, payload, null);

            if (!response.isSuccessful()) {
                common.getLogger().warning("Failed to report server intel: " + response.body());
            }
        } catch (Exception e) {
            common.getLogger().warning(e.getMessage());
        }
    }
}
