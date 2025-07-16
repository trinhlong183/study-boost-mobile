package com.example.miniproject.models;

import java.util.List;

public class DailySession {
    private String id;
    private String sessionTitle;
    private Integer durationMinutes;
    private List<String> studyDays;
    private List<String> activitiesId;
    private String studyScheduleId;

    public DailySession(String id, String sessionTitle, Integer durationMinutes, List<String> studyDays,
            List<String> activitiesId, String studyScheduleId) {
        this.id = id;
        this.sessionTitle = sessionTitle;
        this.durationMinutes = durationMinutes;
        this.studyDays = studyDays;
        this.activitiesId = activitiesId;
        this.studyScheduleId = studyScheduleId;
    }

    public String getId() {
        return id;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public List<String> getStudyDays() {
        return studyDays;
    }

    public List<String> getActivitiesId() {
        return activitiesId;
    }

    public String getStudyScheduleId() {
        return studyScheduleId;
    }
}