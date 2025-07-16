package com.example.miniproject.fragments;

import com.example.miniproject.R;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class PomodoroFragment extends Fragment {

    private TextView timerDisplay;
    private Button btnPlayPause, btnReset;
    private ImageButton btnSettings;
    private NumberPicker minutePicker;
    private RelativeLayout pomodoroBackground;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 25 * 60 * 1000; // 25 phút mặc định
    private long selectedTimeInMillis = 25 * 60 * 1000;
    
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PomodoroSettings";
    private static final String BACKGROUND_KEY = "selected_background";
    private int selectedBackground = R.drawable.background_1; // Mặc định background_1

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pomodoro, container, false);
        
        // Khởi tạo SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Ánh xạ views
        timerDisplay = view.findViewById(R.id.timer_display);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnReset = view.findViewById(R.id.btn_reset);
        btnSettings = view.findViewById(R.id.btn_settings);
        minutePicker = view.findViewById(R.id.minute_picker);
        pomodoroBackground = view.findViewById(R.id.pomodoro_background);
        
        // Load background đã lưu
        loadSavedBackground();
        
        // Thiết lập NumberPicker cho phút (5-60 phút)
        minutePicker.setMinValue(1);
        minutePicker.setMaxValue(60);
        minutePicker.setValue(25); // Giá trị mặc định 25 phút
        
        // Listener cho NumberPicker
        minutePicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (!isTimerRunning) {
                selectedTimeInMillis = newVal * 60 * 1000;
                timeLeftInMillis = selectedTimeInMillis;
                updateTimerDisplay();
            }
        });
        
        // Thiết lập button listeners
        btnPlayPause.setOnClickListener(v -> toggleTimer());
        btnReset.setOnClickListener(v -> resetTimer());
        btnSettings.setOnClickListener(v -> showBackgroundSelector());
        
        // Hiển thị thời gian ban đầu
        updateTimerDisplay();
        
        return view;
    }
    
    private void toggleTimer() {
        if (!isTimerRunning) {
            // Bắt đầu timer
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateTimerDisplay();
                }
                
                @Override
                public void onFinish() {
                    timerDisplay.setText("Hoàn thành!");
                    isTimerRunning = false;
                    btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play, 0, 0);
                    minutePicker.setEnabled(true);
                }
            }.start();
            
            isTimerRunning = true;
            btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_pause, 0, 0);
            minutePicker.setEnabled(false); // Không cho thay đổi thời gian khi đang chạy
        } else {
            // Tạm dừng timer
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            isTimerRunning = false;
            btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play, 0, 0);
            minutePicker.setEnabled(false);
        }
    }
    
    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        isTimerRunning = false;
        timeLeftInMillis = selectedTimeInMillis;
        updateTimerDisplay();
        btnPlayPause.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_play, 0, 0);
        minutePicker.setEnabled(true);
    }
    
    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerDisplay.setText(timeFormatted);
    }
    
    private void showBackgroundSelector() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_background_selector);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        ImageView bg1 = dialog.findViewById(R.id.background_1_preview);
        ImageView bg2 = dialog.findViewById(R.id.background_2_preview);
        ImageView bg3 = dialog.findViewById(R.id.background_3_preview);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        
        bg1.setOnClickListener(v -> {
            setBackground(R.drawable.background_1);
            dialog.dismiss();
        });
        
        bg2.setOnClickListener(v -> {
            setBackground(R.drawable.background_2);
            dialog.dismiss();
        });
        
        bg3.setOnClickListener(v -> {
            setBackground(R.drawable.background_3);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void setBackground(int backgroundResource) {
        selectedBackground = backgroundResource;
        pomodoroBackground.setBackgroundResource(backgroundResource);
        
        // Lưu vào SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(BACKGROUND_KEY, backgroundResource);
        editor.apply();
    }
    
    private void loadSavedBackground() {
        selectedBackground = sharedPreferences.getInt(BACKGROUND_KEY, R.drawable.background_1);
        pomodoroBackground.setBackgroundResource(selectedBackground);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
