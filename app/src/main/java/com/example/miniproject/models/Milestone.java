package com.example.miniproject.models;

public class Milestone {
    private String id;
    private int integerId;
    private String description;
    private String targetCompletion;
    private String studyScheduleId;

    public Milestone(String id, int integerId, String description, String targetCompletion, String studyScheduleId) {
        this.id = id;
        this.integerId = integerId;
        this.description = description;
        this.targetCompletion = targetCompletion;
        this.studyScheduleId = studyScheduleId;
    }

    public String getId() {
        return id;
    }

    public int getIntegerId() {
        return integerId;
    }

    public String getDescription() {
        return description;
    }

    public String getTargetCompletion() {
        return targetCompletion;
    }

    public String getStudyScheduleId() {
        return studyScheduleId;
    }
}