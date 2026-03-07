package com.salon.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.salon.android.R;
import com.salon.android.adapters.ServiceAdapter;
import com.salon.android.api.ApiClient;
import com.salon.android.api.ApiService;
import com.salon.android.models.ApiResponse;
import com.salon.android.utils.LocaleHelper;
import com.salon.android.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ServiceAdapter.OnServiceClickListener {

    private RecyclerView rvServices;
    private ProgressBar progressBar;
    private TextView tvWelcome, tvCurrency, tvNoServices;
    private SwipeRefreshLayout swipeRefresh;
    private ServiceAdapter serviceAdapter;
    private SessionManager sessionManager;
    private List<Map<String, Object>> serviceList = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvCurrency = findViewById(R.id.tvCurrency);
        tvNoServices = findViewById(R.id.tvNoServices);
        rvServices = findViewById(R.id.rvServices);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        tvWelcome.setText("Welcome, " + sessionManager.getName() + "!");
        tvCurrency.setText("Currency: " + sessionManager.getCurrencySymbol() + " (" + sessionManager.getCurrencyCode() + ")");

        rvServices.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceAdapter(serviceList, sessionManager.getCurrencySymbol(), this);
        rvServices.setAdapter(serviceAdapter);

        swipeRefresh.setOnRefreshListener(this::loadServices);

        loadServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServices();
    }

    private void loadServices() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoServices.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getApiService(this);
        String countryCode = sessionManager.getCountryCode();
        if (countryCode.isEmpty()) countryCode = "US";

        apiService.getAllServices(countryCode).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body().getData());
                    Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    serviceList.clear();
                    List<Map<String, Object>> data = gson.fromJson(json, type);
                    if (data != null) {
                        serviceList.addAll(data);
                    }
                    serviceAdapter.updateData(serviceList);

                    tvNoServices.setVisibility(serviceList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvNoServices.setVisibility(View.VISIBLE);
                    tvNoServices.setText("Failed to load services");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvNoServices.setVisibility(View.VISIBLE);
                tvNoServices.setText("Network error. Pull to retry.");
            }
        });
    }

    @Override
    public void onServiceClick(Map<String, Object> service) {
        Intent intent = new Intent(this, BookAppointmentActivity.class);
        intent.putExtra("serviceId", ((Double) service.get("id")).longValue());
        intent.putExtra("serviceName", (String) service.get("name"));
        intent.putExtra("servicePrice", String.valueOf(service.get("price")));
        intent.putExtra("serviceDuration", String.valueOf(((Double) service.get("durationMinutes")).intValue()));
        intent.putExtra("currencySymbol", (String) service.get("currencySymbol"));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_appointments) {
            startActivity(new Intent(this, AppointmentHistoryActivity.class));
            return true;
        } else if (id == R.id.action_subscription) {
            startActivity(new Intent(this, SubscriptionActivity.class));
            return true;
        } else if (id == R.id.action_language) {
            showLanguageDialog();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguageDialog() {
        String[] langCodes = {"en", "hi", "es", "fr", "ja", "zh", "ar", "pt", "de", "ko"};
        String[] langNames = {
            "English", "Hindi - \u0939\u093F\u0928\u094D\u0926\u0940", "Spanish - Espa\u00F1ol",
            "French - Fran\u00E7ais", "Japanese - \u65E5\u672C\u8A9E", "Chinese - \u4E2D\u6587",
            "Arabic - \u0627\u0644\u0639\u0631\u0628\u064A\u0629", "Portuguese - Portugu\u00EAs",
            "German - Deutsch", "Korean - \uD55C\uAD6D\uC5B4"
        };

        String currentLang = sessionManager.getLanguage();
        int checkedItem = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(langNames, checkedItem, (dialog, which) -> {
                    LocaleHelper.setLanguage(this, langCodes[which]);
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logout();
                    ApiClient.clearToken(this);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
