package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.exceptions.HttpException;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

import java.io.IOException;
import java.util.List;

public class ReportPlayerPunishment {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static void syncSync(List<PunishmentData> data) throws HttpException, IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("server_id", common.getPlugin().getApiServerId());
        payload.add("punishments", common.getGson().toJsonTree(data));

        HttpResponse response = MinetraxHttpUtil.post(MinetraxHttpUtil.BANWARDEN_SYNC_PUNISHMENT_ROUTE, payload.toString(), null);

        if (!response.isSuccessful()) {
            throw new HttpException(response, "ReportServerChat.reportAsync");
        }
    }

    public static void upsertSync(PunishmentData data) throws HttpException, IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("type", data.getType());
        // todo
        payload.addProperty("server_id", common.getPlugin().getApiServerId());

        HttpResponse response = MinetraxHttpUtil.post(MinetraxHttpUtil.BANWARDEN_REPORT_PUNISHMENT_ROUTE, payload.toString(), null);

        if (!response.isSuccessful()) {
            throw new HttpException(response, "ReportServerChat.reportAsync");
        }
    }
}
