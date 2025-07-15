package com.example.miniproject.models;

import java.util.List;

public class WeeklyPlan {
    public String id;
    public int week;
    public String focus;
    public List<String> topics;
    public String objective;
    public String study_schedule_id;

    public WeeklyPlan() {
    }

    public WeeklyPlan(String id, int week, String focus, List<String> topics, String objective,
            String study_schedule_id) {
        this.id = id;
        this.week = week;
        this.focus = focus;
        this.topics = topics;
        this.objective = objective;
        this.study_schedule_id = study_schedule_id;
    }

    public String getTopicsAsString() {
        if (topics == null || topics.isEmpty()) {
            return "Không có chủ đề";
        }
        return String.join(", ", topics);
    }
}
