package com.example.miniproject.activities;

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

            appwrite.registerWithName(name, email, password, new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
                @Override
                public void onSuccess(User<Map<String, Object>> result) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration successful. Please login.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Exception error) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    System.out.println(error.getMessage());
                }
            });
        });

        buttonBackToLogin.setOnClickListener(v -> finish());
    }
}
