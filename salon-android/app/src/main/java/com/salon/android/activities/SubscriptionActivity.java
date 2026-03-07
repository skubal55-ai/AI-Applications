package com.salon.android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.salon.android.R;
import com.salon.android.adapters.SubscriptionPlanAdapter;
import com.salon.android.api.ApiClient;
import com.salon.android.api.ApiService;
import com.salon.android.models.ApiResponse;
import com.salon.android.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionActivity extends AppCompatActivity implements SubscriptionPlanAdapter.OnPlanClickListener {

    private RecyclerView rvPlans;
    private LinearLayout activeSubscriptionLayout;
    private TextView tvActivePlan, tvActivePrice, tvActiveExpiry, tvActiveFeatures;
    private Button btnCancelSubscription;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Subscriptions");
        }

        rvPlans = findViewById(R.id.rvPlans);
        activeSubscriptionLayout = findViewById(R.id.activeSubscriptionLayout);
        tvActivePlan = findViewById(R.id.tvActivePlan);
        tvActivePrice = findViewById(R.id.tvActivePrice);
        tvActiveExpiry = findViewById(R.id.tvActiveExpiry);
        tvActiveFeatures = findViewById(R.id.tvActiveFeatures);
        btnCancelSubscription = findViewById(R.id.btnCancelSubscription);
        progressBar = findViewById(R.id.progressBar);

        btnCancelSubscription.setOnClickListener(v -> showCancelDialog());

        rvPlans.setLayoutManager(new LinearLayoutManager(this));

        loadActiveSubscription();
        loadAvailablePlans();
    }

    private void loadActiveSubscription() {
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getActiveSubscription().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body().getData());
                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> data = gson.fromJson(json, type);

                    activeSubscriptionLayout.setVisibility(View.VISIBLE);
                    tvActivePlan.setText("Plan: " + data.get("plan"));
                    tvActivePrice.setText("Price: " + data.get("currencySymbol") + " " + data.get("price") + "/month");
                    tvActiveExpiry.setText("Expires: " + data.get("endDate"));
                    tvActiveFeatures.setText("Features: " + data.get("features"));
                } else {
                    activeSubscriptionLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                activeSubscriptionLayout.setVisibility(View.GONE);
            }
        });
    }

    private void loadAvailablePlans() {
        progressBar.setVisibility(View.VISIBLE);
        String countryCode = sessionManager.getCountryCode();
        if (countryCode.isEmpty()) countryCode = "US";

        ApiService apiService = ApiClient.getApiService(this);
        apiService.getSubscriptionPlans(countryCode).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body().getData());
                    Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> plans = gson.fromJson(json, type);

                    if (plans != null) {
                        rvPlans.setAdapter(new SubscriptionPlanAdapter(plans, SubscriptionActivity.this));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SubscriptionActivity.this, "Failed to load plans", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPlanClick(Map<String, Object> plan) {
        String planName = (String) plan.get("plan");
        String price = plan.get("currencySymbol") + " " + plan.get("priceLocal");

        new AlertDialog.Builder(this)
                .setTitle("Subscribe to " + planName)
                .setMessage("Price: " + price + "/month\n\nFeatures:\n" + plan.get("features"))
                .setPositiveButton("Subscribe", (dialog, which) -> subscribeToPlan(planName))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void subscribeToPlan(String planName) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> request = new HashMap<>();
        request.put("plan", planName);
        request.put("autoRenew", true);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.subscribe(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SubscriptionActivity.this, "Subscribed to " + planName + " plan!", Toast.LENGTH_LONG).show();
                    loadActiveSubscription();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Subscription failed";
                    Toast.makeText(SubscriptionActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SubscriptionActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Subscription")
                .setMessage("Are you sure you want to cancel your subscription?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelSubscription())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelSubscription() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.cancelSubscription().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SubscriptionActivity.this, "Subscription cancelled", Toast.LENGTH_SHORT).show();
                    activeSubscriptionLayout.setVisibility(View.GONE);
                } else {
                    Toast.makeText(SubscriptionActivity.this, "Failed to cancel subscription", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SubscriptionActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
