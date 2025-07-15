package com.example.miniproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject.R;
import com.example.miniproject.models.SurveyQuestion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyQuestionAdapter extends RecyclerView.Adapter<SurveyQuestionAdapter.QuestionViewHolder> {
    private final List<SurveyQuestion> questions;
    private final Map<Integer, Object> answers = new HashMap<>();

    public SurveyQuestionAdapter(List<SurveyQuestion> questions) {
        this.questions = questions;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_survey_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        SurveyQuestion question = questions.get(position);
        holder.questionText.setText(question.getQuestionText());
        holder.inputContainer.removeAllViews();

        // Since our SurveyQuestion model is simplified for the one-by-one flow,
        // we'll create multiple choice options for all questions
        RadioGroup optionsGroup = new RadioGroup(holder.itemView.getContext());
        optionsGroup.setOrientation(RadioGroup.VERTICAL);

        if (question.getOptions() != null) {
            for (String option : question.getOptions()) {
                RadioButton rb = new RadioButton(holder.itemView.getContext());
                rb.setText(option);
                rb.setPadding(16, 16, 16, 16);
                optionsGroup.addView(rb);
                if (answers.get(position) != null && answers.get(position).toString().equals(option)) {
                    rb.setChecked(true);
                }
            }
        }

        optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checked = group.findViewById(checkedId);
            if (checked != null) {
                answers.put(position, checked.getText().toString());
            }
        });

        holder.inputContainer.addView(optionsGroup);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public Map<String, Object> collectAnswers() {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < questions.size(); i++) {
            SurveyQuestion q = questions.get(i);
            Object answer = answers.get(i);
            result.put(q.getQuestionText(), answer != null ? answer : "");
        }
        return result;
    }

    public static class SurveyResponse {
        public String questionId;
        public String response;

        public SurveyResponse(String questionId, String response) {
            this.questionId = questionId;
            this.response = response;
        }
    }

    public List<SurveyResponse> collectSurveyResponses(List<SurveyQuestion> questions) {
        List<SurveyResponse> result = new java.util.ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            SurveyQuestion q = questions.get(i);
            Object answer = answers.get(i);
            result.add(new SurveyResponse(String.valueOf(q.getId()), answer != null ? answer.toString() : ""));
        }
        return result;
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        LinearLayout inputContainer;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.tvQuestionText);
            inputContainer = itemView.findViewById(R.id.inputContainer);
        }
    }
}