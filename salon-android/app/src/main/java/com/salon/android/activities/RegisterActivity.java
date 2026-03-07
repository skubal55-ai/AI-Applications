package com.salon.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.salon.android.R;
import com.salon.android.api.ApiClient;
import com.salon.android.api.ApiService;
import com.salon.android.models.ApiResponse;
import com.salon.android.utils.LocationHelper;
import com.salon.android.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etPhone;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> attemptRegister());

        tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Auto-detect country code from device locale
        String countryCode = LocationHelper.getCountryCodeFromLocale();

        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("fullName", fullName);
        registerRequest.put("email", email);
        registerRequest.put("password", password);
        registerRequest.put("phone", phone);
        registerRequest.put("countryCode", countryCode);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.register(registerRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body().getData());
                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> data = gson.fromJson(json, type);

                    String accessToken = (String) data.get("accessToken");
                    String refreshToken = (String) data.get("refreshToken");
                    double userId = (Double) data.get("userId");
                    String userEmail = (String) data.get("email");
                    String userName = (String) data.get("fullName");
                    String role = (String) data.get("role");
                    String currencyCode = (String) data.get("currencyCode");
                    String currencySymbol = (String) data.get("currencySymbol");

                    sessionManager.saveAuthData(accessToken, refreshToken, (long) userId,
                            userEmail, userName, role, currencyCode, currencySymbol);
                    sessionManager.saveCountryCode(countryCode);
                    ApiClient.saveToken(RegisterActivity.this, accessToken);

                    Toast.makeText(RegisterActivity.this, "Welcome, " + userName + "!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Registration failed";
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
