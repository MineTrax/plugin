package com.xinecraft.minetrax.common.actions;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.exceptions.HttpException;
import com.xinecraft.minetrax.common.responses.GenericApiResponse;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import com.xinecraft.minetrax.common.utils.MinetraxHttpUtil;

import java.util.HashMap;

public class LinkAccount {
    public static GenericApiResponse link(String playerUuid, String otpCode, String serverId) throws Exception {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("uuid", playerUuid);
        payload.put("server_id", serverId);
        payload.put("otp", otpCode);
        String payloadString = MinetraxCommon.getInstance().getGson().toJson(payload);
        HttpResponse resp = MinetraxHttpUtil.post(MinetraxHttpUtil.VERIFY_ACCOUNT_LINK_ROUTE, payloadString, null);
        String body = resp.body();
        if (body == null) {
            throw new HttpException(resp, "LinkAccount.link");
        }

        GenericApiResponse response = MinetraxCommon.getInstance().getGson().fromJson(body, GenericApiResponse.class);
        response.setCode(resp.code());

        return response;
    }
}
