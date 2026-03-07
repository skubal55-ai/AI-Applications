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

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private List<Map<String, Object>> services;
    private final String currencySymbol;
    private final OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Map<String, Object> service);
    }

    public ServiceAdapter(List<Map<String, Object>> services, String currencySymbol, OnServiceClickListener listener) {
        this.services = services;
        this.currencySymbol = currencySymbol;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> service = services.get(position);

        holder.tvName.setText((String) service.get("name"));
        holder.tvDescription.setText((String) service.get("description"));

        String symbol = service.get("currencySymbol") != null ? (String) service.get("currencySymbol") : currencySymbol;
        holder.tvPrice.setText(symbol + " " + service.get("price"));

        Object duration = service.get("durationMinutes");
        if (duration instanceof Double) {
            holder.tvDuration.setText(((Double) duration).intValue() + " min");
        } else {
            holder.tvDuration.setText(duration + " min");
        }

        String category = (String) service.get("category");
        holder.tvCategory.setText(category != null ? category : "General");

        holder.btnBook.setOnClickListener(v -> listener.onServiceClick(service));
        holder.itemView.setOnClickListener(v -> listener.onServiceClick(service));
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    public void updateData(List<Map<String, Object>> newData) {
        this.services = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice, tvDuration, tvCategory;
        Button btnBook;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvServiceName);
            tvDescription = view.findViewById(R.id.tvServiceDescription);
            tvPrice = view.findViewById(R.id.tvServicePrice);
            tvDuration = view.findViewById(R.id.tvServiceDuration);
            tvCategory = view.findViewById(R.id.tvServiceCategory);
            btnBook = view.findViewById(R.id.btnBookService);
        }
    }
}
