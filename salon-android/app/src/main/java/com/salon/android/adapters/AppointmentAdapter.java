package com.salon.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salon.android.R;

import java.util.List;
import java.util.Map;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<Map<String, Object>> appointments;
    private final OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onCancelAppointment(long appointmentId);
    }

    public AppointmentAdapter(List<Map<String, Object>> appointments, OnAppointmentActionListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> appointment = appointments.get(position);

        holder.tvServiceName.setText((String) appointment.get("serviceName"));
        holder.tvStaffName.setText("Staff: " + appointment.get("staffName"));
        holder.tvDateTime.setText("Date: " + appointment.get("appointmentDateTime"));

        String status = (String) appointment.get("status");
        holder.tvStatus.setText("Status: " + status);

        String price = appointment.get("currencySymbol") + " " + appointment.get("servicePrice");
        holder.tvPrice.setText(price);

        // Show cancel button only for pending/confirmed appointments
        boolean canCancel = "PENDING".equals(status) || "CONFIRMED".equals(status);
        holder.btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        holder.btnCancel.setOnClickListener(v -> {
            Object id = appointment.get("id");
            if (id instanceof Double) {
                listener.onCancelAppointment(((Double) id).longValue());
            }
        });

        // Set status color
        int statusColor;
        switch (status != null ? status : "") {
            case "CONFIRMED": statusColor = 0xFF4CAF50; break;
            case "PENDING": statusColor = 0xFFFF9800; break;
            case "CANCELLED": statusColor = 0xFFF44336; break;
            case "COMPLETED": statusColor = 0xFF2196F3; break;
            default: statusColor = 0xFF757575; break;
        }
        holder.tvStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public void updateData(List<Map<String, Object>> newData) {
        this.appointments = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvStaffName, tvDateTime, tvStatus, tvPrice;
        Button btnCancel;

        ViewHolder(View view) {
            super(view);
            tvServiceName = view.findViewById(R.id.tvAppointmentService);
            tvStaffName = view.findViewById(R.id.tvAppointmentStaff);
            tvDateTime = view.findViewById(R.id.tvAppointmentDateTime);
            tvStatus = view.findViewById(R.id.tvAppointmentStatus);
            tvPrice = view.findViewById(R.id.tvAppointmentPrice);
            btnCancel = view.findViewById(R.id.btnCancelAppointment);
        }
    }
}
