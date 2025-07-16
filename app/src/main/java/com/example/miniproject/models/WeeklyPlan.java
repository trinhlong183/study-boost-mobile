package com.example.miniproject.models;

import java.util.List;

public class WeeklyPlan {
    private String id;
    private int week;
    private String focus;
    private List<String> topics;
    private String objective;
    private String studyScheduleId;

    public WeeklyPlan(String id, int week, String focus, List<String> topics, String objective,
            String studyScheduleId) {
        this.id = id;
        this.week = week;
        this.focus = focus;
        this.topics = topics;
        this.objective = objective;
        this.studyScheduleId = studyScheduleId;
    }

    public String getId() {
        return id;
    }

    public int getWeek() {
        return week;
    }

    public String getFocus() {
        return focus;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getObjective() {
        return objective;
    }

    public String getStudyScheduleId() {
        return studyScheduleId;
    }
}