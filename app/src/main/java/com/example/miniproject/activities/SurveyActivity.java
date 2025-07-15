package com.example.miniproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.miniproject.R;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.SurveyQuestion;
import com.example.miniproject.services.SurveyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.appwrite.models.Session;
import io.appwrite.models.User;
import com.google.gson.JsonObject;

public class SurveyActivity extends AppCompatActivity {
    private static final String TAG = "SurveyActivity";

    // UI Components
    private TextView tvProgress;
    private ProgressBar progressBar;
    private TextView tvQuestionTitle;
    private TextView tvQuestionText;
    private LinearLayout layoutOptions;
    private Button btnNext;
    private CardView cardQuestion;
    private TextView tvLoadingText;

    // Data
    private List<SurveyQuestion> questions;
    private Map<Integer, String> userAnswers;
    private int currentQuestionIndex = 0;
    private String selectedAnswer = "";
    private String currentUserId;
    private SurveyService surveyService;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        initViews();
        initServices();
        getCurrentUser();
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tv_progress);
        progressBar = findViewById(R.id.progress_bar);
        tvQuestionTitle = findViewById(R.id.tv_question_title);
        tvQuestionText = findViewById(R.id.tv_question_text);
        layoutOptions = findViewById(R.id.layout_options);
        btnNext = findViewById(R.id.btn_next);
        cardQuestion = findViewById(R.id.card_question);
        tvLoadingText = findViewById(R.id.tv_loading_text);

        btnNext.setOnClickListener(v -> handleNextButton());
    }

    private void initServices() {
        surveyService = new SurveyService(this);
        questions = new ArrayList<>();
        userAnswers = new HashMap<>();
    }

    private void getCurrentUser() {
        AppwriteHelper.getInstance(this).getCurrentUser(new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
            @Override
            public void onSuccess(User<Map<String, Object>> user) {
                currentUserId = user.getId();
                Log.d(TAG, "Current user ID: " + currentUserId);
                loadSurveyQuestions();
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error getting current user", error);
                Toast.makeText(SurveyActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadSurveyQuestions() {
        // Show loading while fetching questions
        tvLoadingText.setText("Đang tải câu hỏi khảo sát...");
        cardQuestion.setVisibility(View.GONE);

        surveyService.getSurveyQuestions(new SurveyService.SurveyCallback() {
            @Override
            public void onSuccess(List<SurveyQuestion> fetchedQuestions) {
                runOnUiThread(() -> {
                    questions.clear();
                    questions.addAll(fetchedQuestions);
                    isDataLoaded = true;

                    cardQuestion.setVisibility(View.VISIBLE);
                    displayCurrentQuestion();
                });
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading survey questions", error);
                runOnUiThread(() -> {
                    // Fallback to mock data if database fails
                    initMockData();
                    isDataLoaded = true;
                    cardQuestion.setVisibility(View.VISIBLE);
                    displayCurrentQuestion();
                });
            }
        });
    }

    private void initMockData() {
        // Fallback mock data (your existing hardcoded questions)
        questions.add(new SurveyQuestion(1,
                "Trong 1 tháng tới, mục tiêu học tập chính của bạn là gì?",
                new String[] {
                        "Cải thiện điểm của một môn học cụ thể",
                        "Nắm vững một chủ đề mới",
                        "Ôn tập cho kỳ thi",
                        "Hoàn thành một dự án học tập",
                        "Nghiên cứu chuyên sâu một lĩnh vực"
                }));

        questions.add(new SurveyQuestion(2,
                "Thời gian học tập mỗi ngày của bạn thường là bao lâu?",
                new String[] {
                        "Dưới 1 giờ",
                        "1-2 giờ",
                        "2-4 giờ",
                        "4-6 giờ",
                        "Trên 6 giờ"
                }));

        questions.add(new SurveyQuestion(3,
                "Môi trường học tập yêu thích của bạn là gì?",
                new String[] {
                        "Tại nhà, yên tĩnh",
                        "Thư viện",
                        "Quán cà phê",
                        "Phòng học nhóm",
                        "Ngoài trời"
                }));

        questions.add(new SurveyQuestion(4,
                "Phương pháp học tập hiệu quả nhất với bạn là?",
                new String[] {
                        "Đọc sách và ghi chú",
                        "Xem video giảng dạy",
                        "Thực hành và làm bài tập",
                        "Thảo luận nhóm",
                        "Tự nghiên cứu"
                }));

        questions.add(new SurveyQuestion(5,
                "Khi gặp khó khăn trong học tập, bạn thường làm gì?",
                new String[] {
                        "Tìm kiếm trên internet",
                        "Hỏi giáo viên/bạn bè",
                        "Đọc thêm tài liệu",
                        "Nghỉ ngơi rồi quay lại",
                        "Bỏ qua phần khó"
                }));
    }

    private void displayCurrentQuestion() {
        if (!isDataLoaded || currentQuestionIndex >= questions.size()) {
            if (!isDataLoaded) {
                return; // Wait for data to load
            }
            completeSurvey();
            return;
        }

        SurveyQuestion question = questions.get(currentQuestionIndex);

        // Update progress
        int progress = (int) (((float) (currentQuestionIndex + 1) / questions.size()) * 100);
        progressBar.setProgress(progress);
        tvProgress.setText((currentQuestionIndex + 1) + " of " + questions.size());

        // Update question content
        tvQuestionTitle.setText("Question " + (currentQuestionIndex + 1));
        tvQuestionText.setText(question.getQuestionText());

        // Clear previous options
        layoutOptions.removeAllViews();
        selectedAnswer = "";

        // Add new options
        for (String option : question.getOptions()) {
            addOptionButton(option);
        }

        // Reset next button
        btnNext.setText("Next");
        btnNext.setEnabled(false);

        // Animate card appearance
        cardQuestion.setAlpha(0f);
        cardQuestion.animate().alpha(1f).setDuration(300).start();
    }

    private void addOptionButton(String optionText) {
        Button optionButton = new Button(this);
        optionButton.setText(optionText);
        optionButton.setBackgroundResource(R.drawable.option_button_background);
        optionButton.setTextColor(getResources().getColor(android.R.color.black));
        optionButton.setPadding(32, 24, 32, 24);
        optionButton.setTextSize(14);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        optionButton.setLayoutParams(params);

        optionButton.setOnClickListener(v -> {
            // Clear previous selections
            for (int i = 0; i < layoutOptions.getChildCount(); i++) {
                View child = layoutOptions.getChildAt(i);
                if (child instanceof Button) {
                    child.setBackgroundResource(R.drawable.option_button_background);
                    ((Button) child).setTextColor(getResources().getColor(android.R.color.black));
                }
            }

            // Highlight selected option
            optionButton.setBackgroundResource(R.drawable.option_button_selected);
            optionButton.setTextColor(getResources().getColor(android.R.color.white));

            selectedAnswer = optionText;
            btnNext.setEnabled(true);
        });

        layoutOptions.addView(optionButton);
    }

    private void handleNextButton() {
        if (selectedAnswer.isEmpty()) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save current answer
        userAnswers.put(currentQuestionIndex + 1, selectedAnswer);

        currentQuestionIndex++;

        if (currentQuestionIndex >= questions.size()) {
            btnNext.setText("Complete Survey");
            completeSurvey();
        } else {
            displayCurrentQuestion();
        }
    }

    private void completeSurvey() {
        // Convert survey responses to JSON
        String surveyJson = convertAnswersToJson();

        Log.d(TAG, "Survey completed with " + userAnswers.size() + " answers");
        for (Map.Entry<Integer, String> entry : userAnswers.entrySet()) {
            Log.d(TAG, "Q" + entry.getKey() + ": " + entry.getValue());
        }

        // Show completion message
        Toast.makeText(this, "Khảo sát hoàn tất! Đang tạo kế hoạch học tập...", Toast.LENGTH_SHORT).show();

        // Navigate to loading screen
        Intent intent = new Intent(this, SurveyLoadingActivity.class);
        intent.putExtra("survey_data", surveyJson);
        intent.putExtra("user_id", currentUserId);
        startActivity(intent);
        finish();
    }

    private String convertAnswersToJson() {
        JsonObject surveyData = new JsonObject();

        for (Map.Entry<Integer, String> entry : userAnswers.entrySet()) {
            surveyData.addProperty("question_" + entry.getKey(), entry.getValue());
        }

        return surveyData.toString();
    }

    @Override
    public void onBackPressed() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayCurrentQuestion();
        } else {
            super.onBackPressed();
        }
    }
}