package com.xinecraft.minetrax.common.actions;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.responses.GenericApiResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;
import okhttp3.Response;

import java.util.HashMap;

public class AccountLinker {
    public static GenericApiResponse linkAccount(String playerUuid, String otpCode, String serverId) throws Exception {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("uuid", playerUuid);
        payload.put("server_id", serverId);
        payload.put("otp", otpCode);
        String payloadString = MinetraxCommon.getInstance().getGson().toJson(payload);
        Response resp = MinetraxHttpUtil.post(MinetraxHttpUtil.VERIFY_ACCOUNT_LINK_ROUTE, payloadString, null);
        if (resp.body() == null) {
            throw new Exception("Empty response body");
        }

        GenericApiResponse response = MinetraxCommon.getInstance().getGson().fromJson(resp.body().string(), GenericApiResponse.class);
        response.setCode(resp.code());

        return response;
    }
}
