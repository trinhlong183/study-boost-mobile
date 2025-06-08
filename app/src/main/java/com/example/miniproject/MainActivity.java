package com.example.miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import io.appwrite.models.Session;
import io.appwrite.models.User;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonRegister;
    private TextView textViewStatus;
    private Button buttonLogout;
    private AppwriteHelper appwrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Appwrite
        appwrite = AppwriteHelper.getInstance(getApplicationContext());

        setContentView(R.layout.activity_main);

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewStatus = findViewById(R.id.textViewStatus);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Set up click listeners
        buttonLogin.setOnClickListener(v -> login());
        buttonRegister.setOnClickListener(v -> openRegisterActivity());
        buttonLogout.setOnClickListener(v -> logout());
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        appwrite.login(email, password, new AppwriteHelper.AuthCallback<Session>() {
            @Override
            public void onSuccess(Session result) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    showLoggedInUI(email);
                });
            }

            @Override
            public void onError(Exception error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Login failed", error);
                    Toast.makeText(MainActivity.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void logout() {
        appwrite.logout(new AppwriteHelper.AuthCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
                    showLoginUI();
                });
            }

            @Override
            public void onError(Exception error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Logout failed", error);
                    Toast.makeText(MainActivity.this, "Logout failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoggedInUI(String email) {
        editTextEmail.setVisibility(View.GONE);
        editTextPassword.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.GONE);
        buttonRegister.setVisibility(View.GONE);

        textViewStatus.setVisibility(View.VISIBLE);
        buttonLogout.setVisibility(View.VISIBLE);

        textViewStatus.setText("Logged in as " + email);
    }

    private void showLoginUI() {
        editTextEmail.setVisibility(View.VISIBLE);
        editTextPassword.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);

        textViewStatus.setVisibility(View.GONE);
        buttonLogout.setVisibility(View.GONE);
    }
}
