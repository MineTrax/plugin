package com.xinecraft.minetrax.common.utils;

import java.util.HashMap;

public class DebugUtil {
    private static final HashMap<String, Long> debugTimeMap = new HashMap<>();

    public static void startTimer(String key) {
        debugTimeMap.put(key, System.nanoTime());
    }

    public static void endTimer(String key) {
        if (!debugTimeMap.containsKey(key)) {
            System.out.println("Error: start time for key \"" + key + "\" was not set.");
            return;
        }

        long endTime = System.nanoTime();
        long startTime = debugTimeMap.get(key);
        long elapsedTime = endTime - startTime;

        // Convert nanoseconds to milliseconds for easier reading
        double elapsedTimeInMillis = elapsedTime / 1_000_000.0;

        System.out.println("[DebugTimer] Time taken for \"" + key + "\": " + elapsedTimeInMillis + " ms");
        debugTimeMap.remove(key);  // Remove the flag after calculating the time
    }
}
