package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.responses.PlayerWhoisApiResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;
import okhttp3.Response;

import java.util.HashMap;

public class WhoisQuery {
    public static PlayerWhoisApiResponse player(String uuid, String username, String ipAddress, Boolean exact) throws Exception {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        if (ipAddress != null) {
            data.addProperty("ip_address", ipAddress);
        }
        if (uuid != null) {
            data.addProperty("uuid", uuid);
        }
        if (exact) {
            data.addProperty("only_exact_result", true);
        }

        String payloadString = data.toString();
        Response resp = MinetraxHttpUtil.post(MinetraxHttpUtil.FETCH_PLAYER_WHOIS_ROUTE, payloadString, null);
        if (resp.body() == null) {
            throw new Exception("Empty response body");
        }

        PlayerWhoisApiResponse response = MinetraxCommon.getInstance().getGson().fromJson(resp.body().string(), PlayerWhoisApiResponse.class);
        response.setCode(resp.code());

        return response;
    }
}
