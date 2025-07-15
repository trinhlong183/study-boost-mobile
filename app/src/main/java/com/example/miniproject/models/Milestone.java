package com.example.miniproject.models;

public class Milestone {
    public String documentId;
    public int id;
    public String description;
    public String target_completion;
    public String study_schedule_id;

    public Milestone() {
    }

    public Milestone(String documentId, int id, String description, String target_completion,
            String study_schedule_id) {
        this.documentId = documentId;
        this.id = id;
        this.description = description;
        this.target_completion = target_completion;
        this.study_schedule_id = study_schedule_id;
    }
}
