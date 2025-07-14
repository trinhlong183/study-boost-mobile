package com.example.miniproject.fragments;

import com.example.miniproject.R;
import com.example.miniproject.activities.MainActivity;
import com.example.miniproject.api.AppwriteHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import java.util.Map;
import io.appwrite.models.User;

public class ProfileFragment extends Fragment {

    private TextView userEmail;
    private TextView userName;
    private Button btnLogout;
    private AppwriteHelper appwrite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Appwrite
        appwrite = AppwriteHelper.getInstance(getContext());

        // Initialize UI components
        userEmail = view.findViewById(R.id.user_email);
        userName = view.findViewById(R.id.user_name);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Load current user information
        loadUserInfo();

        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void loadUserInfo() {
        appwrite.getCurrentUser(new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
            @Override
            public void onSuccess(User<Map<String, Object>> user) {
                getActivity().runOnUiThread(() -> {
                    userEmail.setText("Email: " + user.getEmail());
                    userName.setText("Name: " + (user.getName() != null ? user.getName() : "N/A"));
                });
            }


            @Override
            public void onError(Exception error) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load user info: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    // Fallback to intent data
                    String email = getActivity().getIntent().getStringExtra("user_email");
                    if (email != null) {
                        userEmail.setText("Email: " + email);
                    }
                });
            }
        });
    }

    private void logout() {
        appwrite.logout(new AppwriteHelper.AuthCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(Exception error) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Đăng xuất thất bại: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
