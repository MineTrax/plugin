package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.data.PlayerDeathData;
import com.xinecraft.minetrax.common.data.PlayerPvpKillData;
import com.xinecraft.minetrax.common.data.PlayerSessionIntelData;
import com.xinecraft.minetrax.common.exceptions.HttpException;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

public class ReportPlayerIntel {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static void initSessionSync(PlayerSessionIntelData data) throws Exception {
        LoggingUtil.info("--- STARTING SESSION FOR A PLAYER ---");
        JsonObject payload = new JsonObject();
        String payloadString = common.getGson().toJson(data);
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.PLAYER_INTEL_SESSION_INIT_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "ReportPlayerIntel.initSessionSync");
        }
        if (body == null) {
            throw new HttpException(resp, "ReportPlayerIntel.initSessionSync");
        }
    }

    public static void reportEventSync(PlayerSessionIntelData data) throws Exception {
        LoggingUtil.debug("---SENDING PLAYER EVENT REPORT---");
        String payloadString = common.getGson().toJson(data);
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.PLAYER_INTEL_EVENT_REPORT_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "ReportPlayerIntel.reportEventSync");
        }
        if (body == null) {
            throw new HttpException(resp, "ReportPlayerIntel.reportEventSync");
        }
    }

    public static void reportPvpKillSync(PlayerPvpKillData data) throws Exception {
        LoggingUtil.debug("---SENDING PLAYER PVP KILL REPORT---");
        JsonObject payload = new JsonObject();
        String payloadString = common.getGson().toJson(data);
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.PLAYER_INTEL_REPORT_PVP_KILL_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "ReportPlayerIntel.reportPvpKillSync");
        }
        if (body == null) {
            throw new HttpException(resp, "ReportPlayerIntel.reportPvpKillSync");
        }
    }

    public static void reportDeathSync(PlayerDeathData data) throws Exception {
        LoggingUtil.debug("---SENDING PLAYER DEATH REPORT---");
        JsonObject payload = new JsonObject();
        String payloadString = common.getGson().toJson(data);
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.PLAYER_INTEL_REPORT_DEATH_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "ReportPlayerIntel.reportDeathSync");
        }
        if (body == null) {
            throw new HttpException(resp, "ReportPlayerIntel.reportDeathSync");
        }
    }
}
