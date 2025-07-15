package com.example.miniproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.R;
import com.example.miniproject.models.Milestone;
import java.util.List;

public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder> {
    private List<Milestone> milestones;
    private OnMilestoneClickListener clickListener;

    public interface OnMilestoneClickListener {
        void onMilestoneClick(Milestone milestone);
    }

    public MilestoneAdapter(List<Milestone> milestones, OnMilestoneClickListener clickListener) {
        this.milestones = milestones;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_milestone_card, parent, false);
        return new MilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
        Milestone milestone = milestones.get(position);
        holder.bind(milestone, clickListener);
    }

    @Override
    public int getItemCount() {
        android.util.Log.d("MilestoneAdapter", "getItemCount: " + milestones.size());
        return milestones.size();
    }

    public void updateMilestones(List<Milestone> newMilestones) {
        android.util.Log.d("MilestoneAdapter", "updateMilestones called with " + newMilestones.size() + " milestones");
        this.milestones = newMilestones;
        notifyDataSetChanged();
    }

    static class MilestoneViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMilestoneNumber;
        private TextView tvMilestoneDescription;
        private TextView tvMilestoneCompletion;

        public MilestoneViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMilestoneNumber = itemView.findViewById(R.id.tvMilestoneNumber);
            tvMilestoneDescription = itemView.findViewById(R.id.tvMilestoneDescription);
            tvMilestoneCompletion = itemView.findViewById(R.id.tvMilestoneCompletion);
        }

        public void bind(Milestone milestone, OnMilestoneClickListener clickListener) {
            android.util.Log.d("MilestoneAdapter",
                    "Binding milestone: " + milestone.id + ", Description: " + milestone.description);

            tvMilestoneNumber.setText("CỘT MỐC " + milestone.id);
            tvMilestoneDescription.setText(milestone.description != null ? milestone.description : "Không có mô tả");
            tvMilestoneCompletion
                    .setText(milestone.target_completion != null ? milestone.target_completion : "Chưa xác định");

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMilestoneClick(milestone);
                }
            });
        }
    }
}
