package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.exceptions.HttpException;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.responses.PlayerWhoisApiResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

public class WhoisQuery {
    public static PlayerWhoisApiResponse playerSync(String uuid, String username, String ipAddress, Boolean exact) throws Exception {
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
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.FETCH_PLAYER_WHOIS_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "WhoisQuery.playerSync");
        }
        if (body == null) {
            throw new HttpException(resp, "WhoisQuery.playerSync");
        }

        PlayerWhoisApiResponse response = MinetraxCommon.getInstance().getGson().fromJson(body, PlayerWhoisApiResponse.class);
        response.setCode(resp.code());

        return response;
    }
}
