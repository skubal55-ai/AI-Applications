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

public class SubscriptionPlanAdapter extends RecyclerView.Adapter<SubscriptionPlanAdapter.ViewHolder> {

    private final List<Map<String, Object>> plans;
    private final OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onPlanClick(Map<String, Object> plan);
    }

    public SubscriptionPlanAdapter(List<Map<String, Object>> plans, OnPlanClickListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> plan = plans.get(position);

        holder.tvPlanName.setText((String) plan.get("plan"));
        holder.tvPrice.setText(plan.get("currencySymbol") + " " + plan.get("priceLocal") + "/month");
        holder.tvFeatures.setText((String) plan.get("features"));
        holder.tvBillingCycle.setText((String) plan.get("billingCycle"));

        holder.btnSubscribe.setOnClickListener(v -> listener.onPlanClick(plan));
    }

    @Override
    public int getItemCount() {
        return plans != null ? plans.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanName, tvPrice, tvFeatures, tvBillingCycle;
        Button btnSubscribe;

        ViewHolder(View view) {
            super(view);
            tvPlanName = view.findViewById(R.id.tvPlanName);
            tvPrice = view.findViewById(R.id.tvPlanPrice);
            tvFeatures = view.findViewById(R.id.tvPlanFeatures);
            tvBillingCycle = view.findViewById(R.id.tvBillingCycle);
            btnSubscribe = view.findViewById(R.id.btnSubscribePlan);
        }
    }
}
