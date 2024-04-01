package com.xinecraft.minetrax.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

    private static final Date date = new Date();
    private static final SimpleDateFormat timestampFormat;
    private static final SimpleDateFormat dateFormat;
    private static final TimeZone zone;

    static {
        timestampFormat = new SimpleDateFormat("EEE, d. MMM yyyy HH:mm:ss z");
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        zone = TimeZone.getTimeZone("UTC");
        timestampFormat.setTimeZone(zone);
        dateFormat.setTimeZone(zone);
    }

    public static String format(String format) {
        return format(new SimpleDateFormat(format));
    }

    public static String format(SimpleDateFormat format) {
        date.setTime(System.currentTimeMillis());
        return format.format(date);
    }

    public static String date() {
        return format(dateFormat);
    }

    public static String timeStamp() {
        return format(timestampFormat);
    }
}
