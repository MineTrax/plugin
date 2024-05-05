package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.exceptions.HttpException;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

import java.io.IOException;

public class ReportServerConsole {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static void reportSync(String log) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("log", log);
        payload.addProperty("server_id", common.getPlugin().getApiServerId());

        HttpResponse response = MinetraxHttpUtil.post(MinetraxHttpUtil.REPORT_SERVER_CONSOLE_ROUTE, payload.toString(), null);

        if (!response.isSuccessful()) {
            throw new HttpException(response, "ReportServerConsole.reportSync");
        }
    }
}
