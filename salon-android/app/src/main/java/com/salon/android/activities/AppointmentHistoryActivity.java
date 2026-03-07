package com.salon.android.activities;

import android.os.Bundle;
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
import com.salon.android.adapters.AppointmentAdapter;
import com.salon.android.api.ApiClient;
import com.salon.android.api.ApiService;
import com.salon.android.models.ApiResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentHistoryActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentActionListener {

    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private TextView tvNoAppointments;
    private SwipeRefreshLayout swipeRefresh;
    private AppointmentAdapter adapter;
    private List<Map<String, Object>> appointmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Appointments");
        }

        rvAppointments = findViewById(R.id.rvAppointments);
        progressBar = findViewById(R.id.progressBar);
        tvNoAppointments = findViewById(R.id.tvNoAppointments);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(appointmentList, this);
        rvAppointments.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadAppointments);
        loadAppointments();
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoAppointments.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.getMyAppointments().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body().getData());
                    Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    appointmentList.clear();
                    List<Map<String, Object>> data = gson.fromJson(json, type);
                    if (data != null) {
                        appointmentList.addAll(data);
                    }
                    adapter.updateData(appointmentList);
                    tvNoAppointments.setVisibility(appointmentList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvNoAppointments.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                tvNoAppointments.setVisibility(View.VISIBLE);
                tvNoAppointments.setText("Network error. Pull to retry.");
            }
        });
    }

    @Override
    public void onCancelAppointment(long appointmentId) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Appointment")
                .setMessage("Are you sure you want to cancel this appointment?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ApiService apiService = ApiClient.getApiService(this);
                    apiService.cancelAppointment(appointmentId).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(AppointmentHistoryActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                                loadAppointments();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Toast.makeText(AppointmentHistoryActivity.this, "Failed to cancel", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
