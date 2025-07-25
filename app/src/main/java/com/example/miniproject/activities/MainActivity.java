package com.example.miniproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.miniproject.utils.CustomNotification;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.services.StudyScheduleService;
import com.example.miniproject.activities.SurveyActivity;

import java.util.Map;

import io.appwrite.models.Session;
import io.appwrite.models.User;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextInputLayout passwordLayout;
    private Button buttonLogin;
    private TextView buttonRegister;
    private TextView textViewStatus;
    private Button buttonLogout;
    private AppwriteHelper appwrite;
    private StudyScheduleService studyScheduleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Appwrite
        appwrite = AppwriteHelper.getInstance(getApplicationContext());
        studyScheduleService = new StudyScheduleService(getApplicationContext());

        setContentView(R.layout.activity_main);

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        passwordLayout = findViewById(R.id.passwordLayout);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewStatus = findViewById(R.id.textViewStatus);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Set up click listeners
        buttonLogin.setOnClickListener(v -> login());
        buttonRegister.setOnClickListener(v -> openRegisterActivity());

        // Check if user is already logged in
        checkExistingSession();
    }

    private void checkExistingSession() {
        appwrite.getCurrentSession(new AppwriteHelper.AuthCallback<Session>() {
            @Override
            public void onSuccess(Session result) {
                // User is already logged in, get user info and redirect
                appwrite.getCurrentUser(new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(User<Map<String, Object>> user) {
                        runOnUiThread(() -> {
                            String email = user.getEmail();
                            showLoggedInUI(email);
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        // If can't get user info, show login UI
                        runOnUiThread(() -> showLoginUI());
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                // No active session, show login UI
                runOnUiThread(() -> showLoginUI());
            }
        });
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            CustomNotification.showWarning(this, "Vui lòng nhập email và mật khẩu");
            return;
        }

        appwrite.login(email, password, new AppwriteHelper.AuthCallback<Session>() {
            @Override
            public void onSuccess(Session result) {
                appwrite.getCurrentUser(new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(User<Map<String, Object>> user) {
                        String userId = user.getId();
                        // Store userId in SharedPreferences
                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit().putString("user_id", userId).apply();
                        runOnUiThread(() -> {
                            CustomNotification.showSuccess(MainActivity.this, "Đăng nhập thành công");
                            checkStudyScheduleAndNavigate(userId, user.getEmail());
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Failed to get user after login", error);
                            CustomNotification.showError(MainActivity.this,
                                    "Đăng nhập thất bại: " + error.getMessage());
                        });
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Login failed", error);
                    CustomNotification.showError(MainActivity.this, "Đăng nhập thất bại: " + error.getMessage());
                });
            }
        });
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showLoggedInUI(String email) {
        Intent intent = new Intent(MainActivity.this, BottomTabActivity.class);
        intent.putExtra("user_email", email);
        startActivity(intent);
        finish(); // Đóng MainActivity để người dùng không thể quay lại
    }

    private void showLoginUI() {
        editTextEmail.setVisibility(View.VISIBLE);
        editTextPassword.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);

        textViewStatus.setVisibility(View.GONE);
        buttonLogout.setVisibility(View.GONE);
    }

    private void checkStudyScheduleAndNavigate(String userId, String email) {
        studyScheduleService.getStudyScheduleByUserId(userId,
                new StudyScheduleService.StudyScheduleCallback<java.util.List<com.example.miniproject.models.StudySchedule>>() {
                    @Override
                    public void onSuccess(java.util.List<com.example.miniproject.models.StudySchedule> schedules) {
                        runOnUiThread(() -> {
                            if (schedules == null || schedules.isEmpty()) {
                                Intent intent = new Intent(MainActivity.this, SurveyActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                showLoggedInUI(email);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error checking study schedule", error);
                            // Fallback: go to survey page if error
                            Intent intent = new Intent(MainActivity.this, SurveyActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
    }
}
