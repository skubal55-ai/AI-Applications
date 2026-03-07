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
import com.salon.android.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.login(loginRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body().getData());
                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                    Map<String, Object> data = gson.fromJson(json, type);

                    String accessToken = (String) data.get("accessToken");
                    String refreshToken = (String) data.get("refreshToken");
                    double userId = (Double) data.get("userId");
                    String userEmail = (String) data.get("email");
                    String fullName = (String) data.get("fullName");
                    String role = (String) data.get("role");
                    String currencyCode = (String) data.get("currencyCode");
                    String currencySymbol = (String) data.get("currencySymbol");

                    sessionManager.saveAuthData(accessToken, refreshToken, (long) userId,
                            userEmail, fullName, role, currencyCode, currencySymbol);
                    ApiClient.saveToken(LoginActivity.this, accessToken);

                    Toast.makeText(LoginActivity.this, "Welcome back, " + fullName + "!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Check your credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
