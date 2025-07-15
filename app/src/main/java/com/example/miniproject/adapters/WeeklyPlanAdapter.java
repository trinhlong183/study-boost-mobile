package com.example.miniproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.R;
import com.example.miniproject.models.WeeklyPlan;
import java.util.List;

public class WeeklyPlanAdapter extends RecyclerView.Adapter<WeeklyPlanAdapter.WeeklyPlanViewHolder> {
    private List<WeeklyPlan> weeklyPlans;
    private OnWeeklyPlanClickListener clickListener;

    public interface OnWeeklyPlanClickListener {
        void onWeeklyPlanClick(WeeklyPlan weeklyPlan);
    }

    public WeeklyPlanAdapter(List<WeeklyPlan> weeklyPlans, OnWeeklyPlanClickListener clickListener) {
        this.weeklyPlans = weeklyPlans;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public WeeklyPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_plan_card, parent, false);
        return new WeeklyPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyPlanViewHolder holder, int position) {
        WeeklyPlan plan = weeklyPlans.get(position);
        holder.bind(plan, clickListener);
    }

    @Override
    public int getItemCount() {
        android.util.Log.d("WeeklyPlanAdapter", "getItemCount: " + weeklyPlans.size());
        return weeklyPlans.size();
    }

    public void updatePlans(List<WeeklyPlan> newPlans) {
        android.util.Log.d("WeeklyPlanAdapter", "updatePlans called with " + newPlans.size() + " plans");
        this.weeklyPlans = newPlans;
        notifyDataSetChanged();
    }

    static class WeeklyPlanViewHolder extends RecyclerView.ViewHolder {
        private TextView tvWeekNumber;
        private TextView tvFocus;
        private TextView tvObjective;

        public WeeklyPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekNumber = itemView.findViewById(R.id.tvWeekNumber);
            tvFocus = itemView.findViewById(R.id.tvFocus);
            tvObjective = itemView.findViewById(R.id.tvObjective);
        }

        public void bind(WeeklyPlan plan, OnWeeklyPlanClickListener clickListener) {
            android.util.Log.d("WeeklyPlanAdapter", "Binding plan: Week " + plan.week + ", Focus: " + plan.focus);

            tvWeekNumber.setText("TUẦN " + plan.week);
            tvFocus.setText(plan.focus != null ? plan.focus : "Nền tảng ngữ pháp và từ vựng sơ cấp");

            String objective = plan.objective != null ? plan.objective
                    : "Nắm vững các cấu trúc cơ bản và xây dựng nền tảng vững chắc để học hiệu quả ở mức 50-70 từ từ vựng cơ bản";
            tvObjective.setText(objective);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onWeeklyPlanClick(plan);
                }
            });
        }

    }
}
