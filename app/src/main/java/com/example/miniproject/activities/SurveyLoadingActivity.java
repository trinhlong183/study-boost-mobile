package com.example.miniproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SurveyLoadingActivity extends AppCompatActivity {
    private static final String TAG = "SurveyLoadingActivity";

    private ProgressBar progressBar;
    private TextView tvLoadingText;
    private TextView tvProgressText;

    private ExecutorService executorService;
    private Handler mainHandler;
    private OkHttpClient httpClient;
    private Gson gson;

    private String[] loadingMessages = {
            "Đang phân tích câu trả lời của bạn...",
            "Đang tạo kế hoạch học tập cá nhân...",
            "Đang thiết lập lịch học theo tuần...",
            "Đang tạo các cột mốc học tập...",
            "Hoàn thiện kế hoạch học tập..."
    };

    private int currentStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_loading);

        initViews();
        initServices();
        startLoadingProcess();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress_bar_loading);
        tvLoadingText = findViewById(R.id.tv_loading_text);
        tvProgressText = findViewById(R.id.tv_progress_text);

        progressBar.setMax(100);
        progressBar.setProgress(0);
    }

    private void initServices() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        httpClient = new OkHttpClient();
        gson = new Gson();
    }

    private void startLoadingProcess() {
        // Get survey data from intent
        String surveyData = getIntent().getStringExtra("survey_data");
        String userId = getIntent().getStringExtra("user_id");

        if (surveyData == null || userId == null) {
            Log.e(TAG, "Missing survey data or user ID");
            showErrorAndFinish("Dữ liệu khảo sát bị thiếu");
            return;
        }

        // Start animated progress
        animateProgress();

        // Process survey data
        processSurveyData(surveyData, userId);
    }

    private void animateProgress() {
        Handler progressHandler = new Handler(Looper.getMainLooper());

        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentStep < loadingMessages.length) {
                    // Update loading message
                    tvLoadingText.setText(loadingMessages[currentStep]);

                    // Update progress
                    int targetProgress = (currentStep + 1) * 20;
                    animateProgressBar(targetProgress);

                    // Update progress text
                    tvProgressText.setText((currentStep + 1) + " / " + loadingMessages.length);

                    currentStep++;

                    // Schedule next update
                    progressHandler.postDelayed(this, 2000); // 2 seconds per step
                } else {
                    // All steps completed
                    completeLoading();
                }
            }
        };

        progressHandler.post(progressRunnable);
    }

    private void animateProgressBar(int targetProgress) {
        int currentProgress = progressBar.getProgress();

        Handler animationHandler = new Handler(Looper.getMainLooper());
        Runnable animationRunnable = new Runnable() {
            int progress = currentProgress;

            @Override
            public void run() {
                if (progress < targetProgress) {
                    progress += 2;
                    progressBar.setProgress(Math.min(progress, targetProgress));
                    animationHandler.postDelayed(this, 50);
                }
            }
        };

        animationHandler.post(animationRunnable);
    }

    private void processSurveyData(String surveyData, String userId) {
        executorService.execute(() -> {
            try {
                // Prepare request to N8N webhook for survey processing
                JsonObject requestData = new JsonObject();
                requestData.addProperty("action", "process_survey");
                requestData.addProperty("user_id", userId);
                requestData.addProperty("survey_responses", surveyData);

                String jsonBody = gson.toJson(requestData);

                RequestBody body = RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        jsonBody);

                Request request = new Request.Builder()
                        .url(Constants.API.N8N_WEBHOOK_URL)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Survey processing successful: " + responseBody);

                        // Wait for animation to complete before proceeding
                        mainHandler.postDelayed(() -> {
                            navigateToHome();
                        }, 3000);

                    } else {
                        Log.e(TAG, "Survey processing failed: " + response.code());
                        mainHandler.post(() -> {
                            showErrorAndFinish("Không thể xử lý dữ liệu khảo sát");
                        });
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error processing survey data", e);
                mainHandler.post(() -> {
                    showErrorAndFinish("Có lỗi xảy ra khi xử lý dữ liệu");
                });
            }
        });
    }

    private void completeLoading() {
        progressBar.setProgress(100);
        tvLoadingText.setText("Hoàn tất! Đang chuyển đến trang chủ...");
        tvProgressText.setText("Completed");

        // Wait a moment before navigating
        mainHandler.postDelayed(() -> {
            navigateToHome();
        }, 1500);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, BottomTabActivity.class);
        intent.putExtra("survey_completed", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showErrorAndFinish(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

        // Navigate back to survey or home
        Intent intent = new Intent(this, BottomTabActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back navigation during loading
        Toast.makeText(this, "Vui lòng đợi quá trình hoàn tất...", Toast.LENGTH_SHORT).show();
    }
}
