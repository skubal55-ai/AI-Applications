package com.salon.android.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String PREF_NAME = "salon_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static Retrofit retrofit = null;
    private static String baseUrl = "http://10.0.2.2:8080/api/";

    public static void setBaseUrl(String url) {
        baseUrl = url;
        retrofit = null; // Force recreation
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String token = prefs.getString(KEY_TOKEN, "");

                Request.Builder builder = original.newBuilder();
                if (token != null && !token.isEmpty()) {
                    builder.header("Authorization", "Bearer " + token);
                }
                return chain.proceed(builder.build());
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }

    public static void saveToken(Context context, String token) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, "");
    }

    public static void clearToken(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_TOKEN)
                .apply();
    }

    public static void saveUserData(Context context, String email, String name, String role,
                                     String currencyCode, String currencySymbol, long userId) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("email", email)
                .putString("full_name", name)
                .putString("role", role)
                .putString("currency_code", currencyCode)
                .putString("currency_symbol", currencySymbol)
                .putLong("user_id", userId)
                .apply();
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
