package com.salon.android.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to detect user's country from device location or locale.
 * Used for automatic currency selection.
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";

    /**
     * Get country code from device locale as fallback.
     */
    public static String getCountryCodeFromLocale() {
        String country = Locale.getDefault().getCountry();
        return country.isEmpty() ? "US" : country;
    }

    /**
     * Get country code from a Location object using Geocoder.
     */
    public static String getCountryCodeFromLocation(Context context, Location location) {
        if (location == null) {
            return getCountryCodeFromLocale();
        }

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                String countryCode = addresses.get(0).getCountryCode();
                if (countryCode != null && !countryCode.isEmpty()) {
                    Log.d(TAG, "Country detected from GPS: " + countryCode);
                    return countryCode;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed: " + e.getMessage());
        }

        return getCountryCodeFromLocale();
    }

    /**
     * Get country name from country code.
     */
    public static String getCountryName(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }
}
