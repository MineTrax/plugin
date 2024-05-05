package com.xinecraft.minetrax.common.actions;

import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.exceptions.HttpException;
import com.xinecraft.minetrax.common.responses.GenericApiResponse;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

public class FetchPlayerData {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static GenericApiResponse getSync(String username, String uuid) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("username", username);
        payload.addProperty("uuid", uuid);
        payload.addProperty("server_id", common.getPlugin().getApiServerId());
        String payloadString = payload.toString();
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.FETCH_PLAYER_DATA_ROUTE, payloadString, null);
        String body = resp.body();
        if (!resp.isSuccessful()) {
            throw new HttpException(resp, "FetchPlayerData.getSync");
        }
        if (body == null) {
            throw new HttpException(resp, "FetchPlayerData.getSync");
        }

        GenericApiResponse response = MinetraxCommon.getInstance().getGson().fromJson(body, GenericApiResponse.class);
        response.setCode(resp.code());
        return response;
    }
}
