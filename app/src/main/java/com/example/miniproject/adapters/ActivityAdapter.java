package com.example.miniproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.R;
import com.example.miniproject.models.Activity;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private final List<Activity> activities;
    private final CalendarDayAdapter.OnActivityClickListener clickListener;

    public ActivityAdapter(List<Activity> activities, CalendarDayAdapter.OnActivityClickListener clickListener) {
        this.activities = activities;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);

        // Show full activity name with better wrapping
        holder.tvActivityName.setText(activity.name);
        holder.tvActivityDuration.setText(activity.duration_minutes + " phút");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onActivityClick(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivityName;
        TextView tvActivityDuration;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityName = itemView.findViewById(R.id.tvActivityName);
            tvActivityDuration = itemView.findViewById(R.id.tvActivityDuration);
        }
    }
}