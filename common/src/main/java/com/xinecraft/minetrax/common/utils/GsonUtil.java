package com.xinecraft.minetrax.common.utils;

import com.google.gson.JsonElement;

public class GsonUtil {
    public static String getAsString(JsonElement element, String defaultValue) {
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }
        return element.getAsString();
    }
}
