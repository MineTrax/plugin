package com.xinecraft.minetrax.common.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.responses.HttpResponse;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MinetraxHttpUtil {
    public static final MinetraxCommon common = MinetraxCommon.getInstance();
    public static final String ACCOUNT_LINK_ROUTE = "/user/linked-players";
    public static final String VERIFY_ACCOUNT_LINK_ROUTE = "/api/v1/account-link/verify";
    public static final String REPORT_SERVER_CHAT_ROUTE = "/api/v1/server/chat";
    public static final String REPORT_SERVER_CONSOLE_ROUTE = "/api/v1/server/console";
    public static final String FETCH_PLAYER_WHOIS_ROUTE = "/api/v1/player/whois";
    public static final String FETCH_PLAYER_DATA_ROUTE = "/api/v1/player/data";
    public static final String SERVER_INTEL_REPORT_ROUTE = "/api/v1/intel/server/report";
    public static final String PLAYER_INTEL_SESSION_INIT_ROUTE = "/api/v1/intel/player/session-init";
    public static final String PLAYER_INTEL_EVENT_REPORT_ROUTE = "/api/v1/intel/player/report/event";
    public static final String PLAYER_INTEL_REPORT_PVP_KILL_ROUTE = "/api/v1/intel/player/report/pvp-kill";
    public static final String PLAYER_INTEL_REPORT_DEATH_ROUTE = "/api/v1/intel/player/report/death";

    public static HttpResponse get(String path, Map<String, String> params, Map<String, String> headers) throws IOException {
        String minetraxApiHost = common.getPlugin().getApiHost();
        if (minetraxApiHost != null) {
            minetraxApiHost = StringUtils.stripEnd(minetraxApiHost, "/");
        }
        String minetraxApiKey = common.getPlugin().getApiKey();
        String minetraxApiSecret = common.getPlugin().getApiSecret();

        // Add current timestamp in milliseconds to the params
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String uri = minetraxApiHost + path;
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(uri)).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            httpBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String fullUriToSign = httpBuilder.build().toString();
        headers = generateHeaders(headers, minetraxApiKey, minetraxApiSecret, fullUriToSign);

        return HttpUtil.get(uri, params, headers);
    }

    public static HttpResponse post(String path, String payload, Map<String, String> headers) throws IOException {
        String minetraxApiHost = common.getPlugin().getApiHost();
        if (minetraxApiHost != null) {
            minetraxApiHost = StringUtils.stripEnd(minetraxApiHost, "/");
        }
        String minetraxApiKey = common.getPlugin().getApiKey();
        String minetraxApiSecret = common.getPlugin().getApiSecret();

        // Add current timestamp in milliseconds to the payload
        JsonElement payloadJson = common.getGson().fromJson(payload, JsonElement.class);
        JsonObject finalPayload = new JsonObject();
        finalPayload.addProperty("timestamp", System.currentTimeMillis());
        finalPayload.add("data", payloadJson);
        String payloadString = finalPayload.toString();

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String uri = minetraxApiHost + path;
        headers = generateHeaders(headers, minetraxApiKey, minetraxApiSecret, payloadString);

        return HttpUtil.post(uri, payloadString, headers);
    }

    @NotNull
    private static Map<String, String> generateHeaders(Map<String, String> headers, String minetraxApiKey, String minetraxApiSecret, String payloadString) {
        String signature = CryptoUtil.getHmacSignature(minetraxApiSecret, payloadString);
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("x-api-key", minetraxApiKey);
        headers.put("x-signature", signature);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }

    public static String getUrl(String route) {
        String minetraxApiHost = common.getPlugin().getApiHost();
        if (minetraxApiHost != null) {
            minetraxApiHost = StringUtils.stripEnd(minetraxApiHost, "/");
        }
        return minetraxApiHost + route;
    }
}
