package com.example.miniproject.models;

import java.util.Date;
import java.util.List;

public class StudySchedule {
    private String id;
    private String title;
    private String userId;
    private String status;
    private Date createdAt;
    private Date startDate;
    private Date endDate;
    private String subject;
    private List<String> weeklyPlanId;
    private String dailySessionId;
    private List<String> milestonesId;

    public StudySchedule(String id, String title, String userId, String status, Date createdAt, Date startDate,
            Date endDate, String subject, List<String> weeklyPlanId, String dailySessionId, List<String> milestonesId) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.startDate = startDate;
        this.endDate = endDate;
        this.subject = subject;
        this.weeklyPlanId = weeklyPlanId;
        this.dailySessionId = dailySessionId;
        this.milestonesId = milestonesId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getWeeklyPlanId() {
        return weeklyPlanId;
    }

    public void setWeeklyPlanId(List<String> weeklyPlanId) {
        this.weeklyPlanId = weeklyPlanId;
    }

    public String getDailySessionId() {
        return dailySessionId;
    }

    public void setDailySessionId(String dailySessionId) {
        this.dailySessionId = dailySessionId;
    }

    public List<String> getMilestonesId() {
        return milestonesId;
    }

    public void setMilestonesId(List<String> milestonesId) {
        this.milestonesId = milestonesId;
    }
}