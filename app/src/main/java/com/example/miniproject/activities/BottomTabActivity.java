package com.example.miniproject.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.miniproject.R;
import com.example.miniproject.fragments.AIFragment;
import com.example.miniproject.fragments.HomeFragment;
import com.example.miniproject.fragments.PomodoroFragment;
import com.example.miniproject.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomTabActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_tab);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set mặc định fragment đầu tiên (Home )
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            // Set selected item trong bottom navigation
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_ai) {
                selectedFragment = new AIFragment();
            } else if (itemId == R.id.nav_pomodoro) {
                selectedFragment = new PomodoroFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}
