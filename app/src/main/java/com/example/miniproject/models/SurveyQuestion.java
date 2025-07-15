package com.example.miniproject.models;

public class SurveyQuestion {
    private int id;
    private String questionText;
    private String[] options;

    public SurveyQuestion(int id, String questionText, String[] options) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
    }

    public int getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }
}