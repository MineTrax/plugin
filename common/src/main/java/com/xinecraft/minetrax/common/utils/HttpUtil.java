package com.xinecraft.minetrax.common.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class HttpUtil {
    public static Response get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Build the URL with parameters
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                httpBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        String finalUrl = httpBuilder.build().toString();

        // Build the request with headers
        Request.Builder requestBuilder = new Request.Builder()
                .url(finalUrl);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        Request request = requestBuilder.build();

        // Send the request and return the response
        return client.newCall(request).execute();
    }

    public static Response post(String url, String json, Map<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Build the request with headers
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, MediaType.parse("application/json")));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        Request request = requestBuilder.build();

        // Send the request and return the response
        return client.newCall(request).execute();
    }
}
