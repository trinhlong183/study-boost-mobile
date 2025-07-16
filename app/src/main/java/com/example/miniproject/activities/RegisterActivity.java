package com.example.miniproject.activities;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniproject.R;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.utils.CustomNotification;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

import io.appwrite.models.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView buttonBackToLogin;
    private AppwriteHelper appwrite;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        appwrite = AppwriteHelper.getInstance(getApplicationContext());

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextRegisterEmail);
        editTextPassword = findViewById(R.id.editTextRegisterPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegisterSubmit);
        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        passwordLayout.setEndIconOnClickListener(v -> togglePasswordVisibility());
        confirmPasswordLayout.setEndIconOnClickListener(v -> toggleConfirmPasswordVisibility());

        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                CustomNotification.showWarning(this, "Vui lòng điền đầy đủ thông tin");
                return;
            }

            if (!password.equals(confirmPassword)) {
                CustomNotification.showError(this, "Mật khẩu xác nhận không khớp");
                return;
            }

            if (password.length() < 8) {
                CustomNotification.showWarning(this, "Mật khẩu phải có ít nhất 8 ký tự");
                return;
            }

            appwrite.registerWithName(name, email, password, new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
                @Override
                public void onSuccess(User<Map<String, Object>> result) {
                    runOnUiThread(() -> {
                        CustomNotification.showSuccess(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.");
                        finish();
                    });
                }

                @Override
                public void onError(Exception error) {
                    runOnUiThread(() -> {
                        CustomNotification.showError(RegisterActivity.this, "Đăng ký thất bại: " + error.getMessage());
                    });
                    System.out.println(error.getMessage());
                }
            });
        });

        buttonBackToLogin.setOnClickListener(v -> finish());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_eye_open);
            isPasswordVisible = false;
        } else {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_eye_closed);
            isPasswordVisible = true;
        }
        editTextPassword.setSelection(editTextPassword.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPasswordLayout.setEndIconDrawable(R.drawable.ic_eye_open);
            isConfirmPasswordVisible = false;
        } else {
            editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPasswordLayout.setEndIconDrawable(R.drawable.ic_eye_closed);
            isConfirmPasswordVisible = true;
        }
        editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
    }
}

