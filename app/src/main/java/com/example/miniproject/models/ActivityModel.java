package com.example.miniproject.models;

import java.util.List;

public class ActivityModel {
    private String id;
    private String dailySessionId;
    private String name;
    private Integer durationMinutes;
    private String description;
    private List<String> techniques;

    public ActivityModel(String id, String dailySessionId, String name, Integer durationMinutes, String description,
            List<String> techniques) {
        this.id = id;
        this.dailySessionId = dailySessionId;
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.description = description;
        this.techniques = techniques;
    }

    public String getId() {
        return id;
    }

    public String getDailySessionId() {
        return dailySessionId;
    }

    public String getName() {
        return name;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTechniques() {
        return techniques;
    }
}