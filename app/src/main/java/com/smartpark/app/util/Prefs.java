package com.smartpark.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class for managing app preferences via SharedPreferences.
 */
public class Prefs {
    private static final String PREF_NAME = "smartpark_prefs";

    private static final String KEY_IS_GUEST = "is_guest";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_AIRPLANE_ALERT = "airplane_alert";
    private static final String KEY_OFFLINE_CACHE = "offline_cache";

    private final SharedPreferences sp;

    public Prefs(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isGuest() { return sp.getBoolean(KEY_IS_GUEST, false); }
    public void setGuest(boolean isGuest) { sp.edit().putBoolean(KEY_IS_GUEST, isGuest).apply(); }

    public boolean isRememberMe() { return sp.getBoolean(KEY_REMEMBER_ME, false); }
    public void setRememberMe(boolean rememberMe) { sp.edit().putBoolean(KEY_REMEMBER_ME, rememberMe).apply(); }

    public String getSavedEmail() { return sp.getString(KEY_SAVED_EMAIL, ""); }
    public void setSavedEmail(String email) { sp.edit().putString(KEY_SAVED_EMAIL, email).apply(); }

    public boolean isDarkTheme() { return sp.getBoolean(KEY_DARK_THEME, false); }
    public void setDarkTheme(boolean darkTheme) { sp.edit().putBoolean(KEY_DARK_THEME, darkTheme).apply(); }

    public boolean isNotificationsEnabled() { return sp.getBoolean(KEY_NOTIFICATIONS, true); }
    public void setNotificationsEnabled(boolean enabled) { sp.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply(); }

    public boolean isAirplaneAlertEnabled() { return sp.getBoolean(KEY_AIRPLANE_ALERT, true); }
    public void setAirplaneAlertEnabled(boolean enabled) { sp.edit().putBoolean(KEY_AIRPLANE_ALERT, enabled).apply(); }

    public boolean isOfflineCacheEnabled() { return sp.getBoolean(KEY_OFFLINE_CACHE, true); }
    public void setOfflineCacheEnabled(boolean enabled) { sp.edit().putBoolean(KEY_OFFLINE_CACHE, enabled).apply(); }

    public void clearSession() {
        sp.edit().remove(KEY_IS_GUEST).apply();
    }
}
