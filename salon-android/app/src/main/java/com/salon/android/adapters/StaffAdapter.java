package com.salon.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.salon.android.R;

import java.util.List;
import java.util.Map;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {

    private final List<Map<String, Object>> staffList;
    private final OnStaffSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnStaffSelectedListener {
        void onStaffSelected(Long staffId);
    }

    public StaffAdapter(List<Map<String, Object>> staffList, OnStaffSelectedListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> staff = staffList.get(position);

        holder.tvName.setText((String) staff.get("name"));
        holder.tvSpecialization.setText((String) staff.get("specialization"));

        Object rating = staff.get("rating");
        if (rating instanceof Double) {
            holder.tvRating.setText(String.format("%.1f", (Double) rating) + " stars");
        }

        holder.cardView.setSelected(position == selectedPosition);
        holder.cardView.setCardBackgroundColor(position == selectedPosition ?
                0xFFE8F5E9 : 0xFFFFFFFF);

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);

            Object id = staff.get("id");
            if (id instanceof Double) {
                listener.onStaffSelected(((Double) id).longValue());
            }
        });
    }

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName, tvSpecialization, tvRating;

        ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.staffCard);
            tvName = view.findViewById(R.id.tvStaffName);
            tvSpecialization = view.findViewById(R.id.tvStaffSpecialization);
            tvRating = view.findViewById(R.id.tvStaffRating);
        }
    }
}
