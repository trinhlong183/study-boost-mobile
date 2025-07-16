package com.example.miniproject.models;

import java.util.Date;
import java.util.List;

public class SurveyQuestion {
    private String id;
    private String questionText;
    private String questionType;
    private String category;
    private Date createdAt;
    private Date updatedAt;
    private List<String> options;
    private String questionNo;
    private boolean required;

    public SurveyQuestion(String id, String questionText, boolean required, String questionType, String category,
            Date createdAt, Date updatedAt, List<String> options, String questionNo) {
        this.id = id;
        this.questionText = questionText;
        this.required = required;
        this.questionType = questionType;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.options = options;
        this.questionNo = questionNo;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(String questionNo) {
        this.questionNo = questionNo;
    }
}