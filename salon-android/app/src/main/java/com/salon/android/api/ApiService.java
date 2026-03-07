package com.salon.android.api;

import com.salon.android.models.ApiResponse;
import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // Auth endpoints
    @POST("auth/register")
    Call<ApiResponse> register(@Body Map<String, Object> request);

    @POST("auth/login")
    Call<ApiResponse> login(@Body Map<String, String> request);

    @POST("auth/refresh")
    Call<ApiResponse> refreshToken(@Body Map<String, String> request);

    // Services endpoints
    @GET("services")
    Call<ApiResponse> getAllServices(@Query("countryCode") String countryCode);

    @GET("services/category/{category}")
    Call<ApiResponse> getServicesByCategory(@Path("category") String category,
                                            @Query("countryCode") String countryCode);

    @GET("services/{id}")
    Call<ApiResponse> getServiceById(@Path("id") long id, @Query("countryCode") String countryCode);

    // Appointment endpoints
    @POST("appointments")
    Call<ApiResponse> createAppointment(@Body Map<String, Object> request);

    @GET("appointments/my")
    Call<ApiResponse> getMyAppointments();

    @GET("appointments/upcoming")
    Call<ApiResponse> getUpcomingAppointments();

    @POST("appointments/{id}/cancel")
    Call<ApiResponse> cancelAppointment(@Path("id") long id);

    // Subscription endpoints
    @GET("subscriptions/plans")
    Call<ApiResponse> getSubscriptionPlans(@Query("countryCode") String countryCode);

    @POST("subscriptions/subscribe")
    Call<ApiResponse> subscribe(@Body Map<String, Object> request);

    @GET("subscriptions/active")
    Call<ApiResponse> getActiveSubscription();

    @GET("subscriptions/history")
    Call<ApiResponse> getSubscriptionHistory();

    @POST("subscriptions/cancel")
    Call<ApiResponse> cancelSubscription();

    // Staff endpoints
    @GET("staff")
    Call<ApiResponse> getAllStaff();

    @GET("staff/{id}")
    Call<ApiResponse> getStaffById(@Path("id") long id);

    // Currency endpoints
    @GET("currency/detect")
    Call<ApiResponse> detectCurrency();

    @GET("currency/{countryCode}")
    Call<ApiResponse> getCurrencyByCountry(@Path("countryCode") String countryCode);

    @GET("currency/convert")
    Call<ApiResponse> convertCurrency(@Query("amount") double amount,
                                      @Query("countryCode") String countryCode);
}
