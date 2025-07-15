package com.example.miniproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.R;
import com.example.miniproject.models.Activity;
import java.util.List;
import java.util.Map;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {
    private final List<String> dayNames;
    private final Map<String, List<Activity>> activitiesByDay;
    private final OnActivityClickListener activityClickListener;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public CalendarDayAdapter(List<String> dayNames, Map<String, List<Activity>> activitiesByDay,
            OnActivityClickListener listener) {
        this.dayNames = dayNames;
        this.activitiesByDay = activitiesByDay;
        this.activityClickListener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        String day = dayNames.get(position);
        holder.tvDayName.setText(day);
        List<Activity> activities = activitiesByDay.get(day);

        if (activities == null || activities.isEmpty()) {
            holder.recyclerViewActivities.setVisibility(View.GONE);
            holder.tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            holder.recyclerViewActivities.setVisibility(View.VISIBLE);
            holder.tvEmptyState.setVisibility(View.GONE);

            ActivityAdapter activityAdapter = new ActivityAdapter(activities, activityClickListener);
            LinearLayoutManager layoutManager = new LinearLayoutManager(holder.itemView.getContext());
            // Reduce item spacing for better fit
            holder.recyclerViewActivities.setLayoutManager(layoutManager);
            holder.recyclerViewActivities.setAdapter(activityAdapter);

            // Add item decoration for consistent spacing
            if (holder.recyclerViewActivities.getItemDecorationCount() == 0) {
                holder.recyclerViewActivities
                        .addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                            @Override
                            public void getItemOffsets(@androidx.annotation.NonNull android.graphics.Rect outRect,
                                    @androidx.annotation.NonNull android.view.View view,
                                    @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView parent,
                                    @androidx.annotation.NonNull androidx.recyclerview.widget.RecyclerView.State state) {
                                outRect.bottom = 2; // Minimal spacing between items
                            }
                        });
            }
        }
    }

    @Override
    public int getItemCount() {
        return dayNames.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        TextView tvEmptyState;
        RecyclerView recyclerViewActivities;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvEmptyState = itemView.findViewById(R.id.tvEmptyState);
            recyclerViewActivities = itemView.findViewById(R.id.recyclerViewActivities);
        }
    }
}