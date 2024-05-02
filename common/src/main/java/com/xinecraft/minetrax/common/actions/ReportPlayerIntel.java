package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.common.responses.GenericApiResponse;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

public class ReportPlayerIntel {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static GenericApiResponse initSessionSync(PlayerSessionIntelData data) throws Exception {
        JsonObject payload = new JsonObject();
        String payloadString = common.getGson().toJson(data);
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.PLAYER_INTEL_SESSION_INIT_ROUTE, payloadString, null);
        String body = resp.body();
        if (body == null) {
            throw new Exception("Empty response body");
        }

        GenericApiResponse response = MinetraxCommon.getInstance().getGson().fromJson(body, GenericApiResponse.class);
        response.setCode(resp.code());
        return response;
    }

    public static GenericApiResponse reportEventSync(PlayerSessionIntelData data) throws Exception {
        JsonObject payload = new JsonObject();
        String payloadString = common.getGson().toJson(data);
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.PLAYER_INTEL_EVENT_REPORT_ROUTE, payloadString, null);
        String body = resp.body();
        if (body == null) {
            throw new Exception("Empty response body");
        }

        GenericApiResponse response = MinetraxCommon.getInstance().getGson().fromJson(body, GenericApiResponse.class);
        response.setCode(resp.code());
        return response;
    }
}
