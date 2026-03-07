package com.salon.android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.salon.android.R;
import com.salon.android.adapters.StaffAdapter;
import com.salon.android.api.ApiClient;
import com.salon.android.api.ApiService;
import com.salon.android.models.ApiResponse;
import com.salon.android.utils.SessionManager;

import java.lang.reflect.Type;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookAppointmentActivity extends AppCompatActivity implements StaffAdapter.OnStaffSelectedListener {

    private TextView tvServiceName, tvServicePrice, tvServiceDuration, tvSelectedDateTime;
    private Button btnSelectDate, btnSelectTime, btnBookNow;
    private RecyclerView rvStaff;
    private ProgressBar progressBar;
    private EditText etNotes;

    private long serviceId;
    private Long selectedStaffId = null;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private boolean dateSelected = false, timeSelected = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Appointment");
        }

        tvServiceName = findViewById(R.id.tvServiceName);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        tvServiceDuration = findViewById(R.id.tvServiceDuration);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnBookNow = findViewById(R.id.btnBookNow);
        rvStaff = findViewById(R.id.rvStaff);
        progressBar = findViewById(R.id.progressBar);
        etNotes = findViewById(R.id.etNotes);

        // Get service details from intent
        serviceId = getIntent().getLongExtra("serviceId", -1);
        String serviceName = getIntent().getStringExtra("serviceName");
        String servicePrice = getIntent().getStringExtra("servicePrice");
        String serviceDuration = getIntent().getStringExtra("serviceDuration");
        String currencySymbol = getIntent().getStringExtra("currencySymbol");

        tvServiceName.setText(serviceName);
        tvServicePrice.setText(currencySymbol + " " + servicePrice);
        tvServiceDuration.setText(serviceDuration + " minutes");

        // Set up date/time pickers
        Calendar calendar = Calendar.getInstance();
        btnSelectDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                dateSelected = true;
                updateDateTimeDisplay();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSelectTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                timeSelected = true;
                updateDateTimeDisplay();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        btnBookNow.setOnClickListener(v -> bookAppointment());

        // Load available staff
        loadStaff();
    }

    private void updateDateTimeDisplay() {
        if (dateSelected && timeSelected) {
            String dateTime = String.format(Locale.getDefault(), "%02d/%02d/%d at %02d:%02d",
                    selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
            tvSelectedDateTime.setText("Selected: " + dateTime);
            tvSelectedDateTime.setVisibility(View.VISIBLE);
        }
    }

    private void loadStaff() {
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getAllStaff().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body().getData());
                    Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> staffList = gson.fromJson(json, type);

                    if (staffList != null && !staffList.isEmpty()) {
                        rvStaff.setLayoutManager(new LinearLayoutManager(BookAppointmentActivity.this,
                                LinearLayoutManager.HORIZONTAL, false));
                        rvStaff.setAdapter(new StaffAdapter(staffList, BookAppointmentActivity.this));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Staff loading is optional, continue without it
            }
        });
    }

    @Override
    public void onStaffSelected(Long staffId) {
        this.selectedStaffId = staffId;
    }

    private void bookAppointment() {
        if (!dateSelected || !timeSelected) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnBookNow.setEnabled(false);

        // Build ISO date-time string
        String dateTime = String.format(Locale.US, "%d-%02d-%02dT%02d:%02d:00",
                selectedYear, selectedMonth + 1, selectedDay, selectedHour, selectedMinute);

        Map<String, Object> request = new HashMap<>();
        request.put("serviceId", serviceId);
        request.put("appointmentDateTime", dateTime);
        request.put("notes", etNotes.getText().toString().trim());
        if (selectedStaffId != null) {
            request.put("staffId", selectedStaffId);
        }

        ApiService apiService = ApiClient.getApiService(this);
        apiService.createAppointment(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnBookNow.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BookAppointmentActivity.this, "Appointment booked successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Booking failed";
                    Toast.makeText(BookAppointmentActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnBookNow.setEnabled(true);
                Toast.makeText(BookAppointmentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
