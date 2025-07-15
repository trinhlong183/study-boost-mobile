package com.example.miniproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.api.AppwriteHelper;

import java.util.Map;

import io.appwrite.models.User;
import io.appwrite.models.Session;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonRegister;
    private Button buttonBackToLogin;
    private AppwriteHelper appwrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        appwrite = AppwriteHelper.getInstance(getApplicationContext());

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextRegisterEmail);
        editTextPassword = findViewById(R.id.editTextRegisterPassword);
        buttonRegister = findViewById(R.id.buttonRegisterSubmit);
        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);

        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Name, email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent multiple submissions
            buttonRegister.setEnabled(false);

            appwrite.registerWithName(name, email, password,
                    new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
                        @Override
                        public void onSuccess(User<Map<String, Object>> result) {
                            // After successful registration, automatically log in the user
                            appwrite.login(email, password, new AppwriteHelper.AuthCallback<Session>() {
                                @Override
                                public void onSuccess(Session session) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(RegisterActivity.this, "Registration and login successful!",
                                                Toast.LENGTH_SHORT).show();

                                        // Now navigate to survey with logged-in user
                                        Intent intent = new Intent(RegisterActivity.this, SurveyActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                }

                                @Override
                                public void onError(Exception loginError) {
                                    runOnUiThread(() -> {
                                        buttonRegister.setEnabled(true);
                                        Toast.makeText(RegisterActivity.this,
                                                "Registration successful but login failed. Please login manually.",
                                                Toast.LENGTH_LONG).show();
                                        finish(); // Go back to login screen
                                    });
                                }
                            });
                        }

                        @Override
                        public void onError(Exception error) {
                            runOnUiThread(() -> {
                                buttonRegister.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + error.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                            System.out.println(error.getMessage());
                        }
                    });
        });

        buttonBackToLogin.setOnClickListener(v -> finish());
    }
}
