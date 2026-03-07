package com.salon.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Helper class to manage app-wide locale/language changes.
 */
public class LocaleHelper {

    /**
     * Apply the saved language preference to the given context.
     */
    public static Context applyLanguage(Context context) {
        SessionManager session = new SessionManager(context);
        String langCode = session.getLanguage();
        return updateResources(context, langCode);
    }

    /**
     * Set a new language and apply it to the context.
     */
    public static Context setLanguage(Context context, String languageCode) {
        SessionManager session = new SessionManager(context);
        session.saveLanguage(languageCode);
        return updateResources(context, languageCode);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    /**
     * Returns display name for a language code.
     */
    public static String getLanguageDisplayName(String code) {
        switch (code) {
            case "hi": return "Hindi - \u0939\u093F\u0928\u094D\u0926\u0940";
            case "es": return "Spanish - Espa\u00F1ol";
            case "fr": return "French - Fran\u00E7ais";
            case "ja": return "Japanese - \u65E5\u672C\u8A9E";
            case "zh": return "Chinese - \u4E2D\u6587";
            case "ar": return "Arabic - \u0627\u0644\u0639\u0631\u0628\u064A\u0629";
            case "pt": return "Portuguese - Portugu\u00EAs";
            case "de": return "German - Deutsch";
            case "ko": return "Korean - \uD55C\uAD6D\uC5B4";
            default: return "English";
        }
    }
}
