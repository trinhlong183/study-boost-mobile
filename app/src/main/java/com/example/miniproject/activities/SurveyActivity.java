package com.example.miniproject.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.models.SurveyQuestion;
import com.example.miniproject.services.SurveyService;

import java.util.List;
import java.util.HashMap;

public class SurveyActivity extends AppCompatActivity {
    private LinearLayout questionsContainer;
    private ProgressBar progressBarSurveyTop;
    private TextView questionProgress;
    private SurveyService surveyService;
    private List<SurveyQuestion> questions;
    private int currentQuestionIndex = 0;
    private HashMap<String, String> answers = new HashMap<>();
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        questionsContainer = findViewById(R.id.questions_container);
        progressBarSurveyTop = findViewById(R.id.progressBarSurveyTop);
        questionProgress = findViewById(R.id.questionProgress);
        surveyService = new SurveyService(this);

        loadSurveyQuestions();
    }

    private void loadSurveyQuestions() {
        // No progress bar for loading, just show questions when ready
        surveyService.getSurveyQuestions(new SurveyService.SurveyCallback<List<SurveyQuestion>>() {
            @Override
            public void onSuccess(List<SurveyQuestion> loadedQuestions) {
                if (loadedQuestions == null || loadedQuestions.isEmpty()) {
                    Toast.makeText(SurveyActivity.this, "No survey questions found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                questions = loadedQuestions;
                currentQuestionIndex = 0;
                showCurrentQuestion();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(SurveyActivity.this, "Failed to load survey questions.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCurrentQuestion() {
        questionsContainer.removeAllViews();
        if (questions == null || currentQuestionIndex >= questions.size())
            return;
        SurveyQuestion question = questions.get(currentQuestionIndex);

        // Update progress bar and question number
        if (progressBarSurveyTop != null && questions != null && questions.size() > 0) {
            int percent = (int) (((currentQuestionIndex + 1) * 100.0f) / questions.size());
            progressBarSurveyTop.setProgress(percent);
        }
        if (questionProgress != null && questions != null && questions.size() > 0) {
            questionProgress.setText((currentQuestionIndex + 1) + " of " + questions.size());
        }

        // Create question title
        TextView questionLabel = new TextView(this);
        questionLabel.setText("Question " + (currentQuestionIndex + 1));
        questionLabel.setTextColor(getResources().getColor(R.color.accent_purple, null));
        questionLabel.setTextSize(16);
        questionLabel.setPadding(0, 0, 0, 16);
        questionsContainer.addView(questionLabel);

        // Create question text
        TextView surveyTitle = findViewById(R.id.surveyTitle);
        surveyTitle.setText(question.getQuestionText());
        surveyTitle.setTextColor(getResources().getColor(R.color.text_dark, null));
        surveyTitle.setTextSize(20);
        surveyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        surveyTitle.setPadding(0, 0, 0, 32);

        String type = question.getQuestionType();
        if (type != null && type.equalsIgnoreCase("text")) {
            // Create modern text input design
            EditText answerBox = new EditText(this);
            answerBox.setHint("Enter your answer here...");
            answerBox.setMinLines(3);
            answerBox.setMaxLines(5);
            answerBox.setPadding(24, 24, 24, 24);
            answerBox.setBackgroundResource(R.drawable.modern_text_input);
            answerBox.setTextColor(getResources().getColor(R.color.text_dark, null));
            answerBox.setHintTextColor(getResources().getColor(R.color.text_secondary, null));
            answerBox.setTextSize(16);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0, 0, 0, 32);
            answerBox.setLayoutParams(textParams);
            questionsContainer.addView(answerBox);

            // Add character count
            TextView charCount = new TextView(this);
            charCount.setText("0/200");
            charCount.setTextColor(getResources().getColor(R.color.text_secondary, null));
            charCount.setTextSize(12);
            charCount.setPadding(16, 0, 0, 24);
            questionsContainer.addView(charCount);

            // Create submit button
            Button submitBtn = new Button(this);
            submitBtn.setText(currentQuestionIndex == questions.size() - 1 ? "Submit ✈" : "Next →");
            submitBtn.setBackgroundResource(R.drawable.modern_button);
            submitBtn.setTextColor(getResources().getColor(android.R.color.white, null));
            submitBtn.setTextSize(16);
            submitBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            submitBtn.setPadding(32, 20, 32, 20);

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(0, 16, 0, 0);
            submitBtn.setLayoutParams(btnParams);

            submitBtn.setOnClickListener(v -> {
                answers.put(question.getId(), answerBox.getText().toString());
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    showCurrentQuestion();
                } else {
                    submitSurvey();
                }
            });
            questionsContainer.addView(submitBtn);

        } else if (type != null && type.toLowerCase().contains("multiple") && question.getOptions() != null
                && !question.getOptions().isEmpty()) {
            // Create modern multiple choice buttons
            for (String option : question.getOptions()) {
                Button optionBtn = new Button(this);
                optionBtn.setText(option + " →");
                optionBtn.setBackgroundResource(R.drawable.choice_button);
                optionBtn.setTextColor(getResources().getColor(android.R.color.white, null));
                optionBtn.setTextSize(16);
                optionBtn.setPadding(32, 24, 32, 50);
                optionBtn.setAllCaps(false);
                optionBtn.setTypeface(null, android.graphics.Typeface.BOLD);

                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                btnParams.setMargins(0, 0, 0, 24); // Increased gap from 16dp to 24dp
                optionBtn.setLayoutParams(btnParams);

                optionBtn.setOnClickListener(v -> {
                    answers.put(question.getId(), option);
                    goToNextOrSubmit();
                });
                questionsContainer.addView(optionBtn);
            }
        }
    }

    private void goToNextOrSubmit() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            showCurrentQuestion();
        } else {
            submitSurvey();
        }
    }

    private void submitSurvey() {
        showProcessingPage();
        // Prepare answers for sending
        List<com.example.miniproject.services.SurveyService.SurveyResponse> responseList = new java.util.ArrayList<>();
        for (SurveyQuestion q : questions) {
            String answer = answers.get(q.getId());
            if (answer != null) {
                responseList.add(new com.example.miniproject.services.SurveyService.SurveyResponse(q.getId(), answer));
            }
        }
        // Get userId from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) {
            runOnUiThread(() -> {
                Toast.makeText(SurveyActivity.this, "User not logged in", Toast.LENGTH_LONG).show();
                // Optionally, return to last question
                showCurrentQuestion();
            });
            return;
        }
        surveyService.sendSurveyResponseTon8n(userId, responseList,
                new com.example.miniproject.services.SurveyService.SurveySendCallback() {
                    @Override
                    public void onSuccess(String n8nResponse) {
                        runOnUiThread(() -> showSuccessPage());
                    }

                    @Override
                    public void onError(Exception error) {
                        runOnUiThread(() -> {
                            Toast.makeText(SurveyActivity.this, "Failed to submit survey: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            // Optionally, return to last question
                            showCurrentQuestion();
                        });
                    }
                });
    }

    private void showProcessingPage() {
        setContentView(R.layout.activity_survey_processing);
        // Optionally, animate or update steps here
    }

    private void showSuccessPage() {
        setContentView(R.layout.activity_survey_success);
        Button btnViewSchedule = findViewById(R.id.btnViewSchedule);
        btnViewSchedule.setOnClickListener(v -> {
            // Navigate to home page (BottomTabActivity)
            Intent intent = new Intent(SurveyActivity.this, BottomTabActivity.class);
            startActivity(intent);
            finish();
        });
    }
}