package com.smartpark.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class DateTimeUtil {

    private static final SimpleDateFormat DB_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US);
    private static final SimpleDateFormat TIME_ONLY_FORMAT = new SimpleDateFormat("hh:mm a", Locale.US);

    public static String getCurrentDateTimeString() {
        return DB_FORMAT.format(new Date());
    }

    public static String formatDisplayDate(long timeMillis) {
        return DISPLAY_FORMAT.format(new Date(timeMillis));
    }

    public static String formatTimeOnly(long timeMillis) {
        return TIME_ONLY_FORMAT.format(new Date(timeMillis));
    }
}
