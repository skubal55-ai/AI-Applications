package com.salon.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session data including authentication tokens and user preferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "salon_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_COUNTRY_CODE = "country_code";
    private static final String KEY_CURRENCY_CODE = "currency_code";
    private static final String KEY_CURRENCY_SYMBOL = "currency_symbol";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveAuthData(String accessToken, String refreshToken, long userId,
                              String email, String name, String role,
                              String currencyCode, String currencySymbol) {
        editor.putString(KEY_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_CURRENCY_CODE, currencyCode);
        editor.putString(KEY_CURRENCY_SYMBOL, currencySymbol);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveCountryCode(String countryCode) {
        editor.putString(KEY_COUNTRY_CODE, countryCode);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, ""); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH_TOKEN, ""); }
    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getName() { return prefs.getString(KEY_NAME, ""); }
    public String getRole() { return prefs.getString(KEY_ROLE, "CUSTOMER"); }
    public String getCountryCode() { return prefs.getString(KEY_COUNTRY_CODE, "US"); }
    public String getCurrencyCode() { return prefs.getString(KEY_CURRENCY_CODE, "USD"); }
    public String getCurrencySymbol() { return prefs.getString(KEY_CURRENCY_SYMBOL, "$"); }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
