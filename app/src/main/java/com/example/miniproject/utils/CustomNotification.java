package com.example.miniproject.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.miniproject.R;

public class CustomNotification {

    public enum Type {
        SUCCESS(android.R.drawable.ic_dialog_info, "#4CAF50"),
        ERROR(android.R.drawable.ic_dialog_alert, "#F44336"),
        WARNING(android.R.drawable.ic_dialog_info, "#FF9800"),
        INFO(android.R.drawable.ic_dialog_info, "#2196F3");

        private final int iconRes;
        private final String color;

        Type(int iconRes, String color) {
            this.iconRes = iconRes;
            this.color = color;
        }

        public int getIconRes() {
            return iconRes;
        }

        public String getColor() {
            return color;
        }
    }

    private static final int DEFAULT_DURATION = 3000; // 3 seconds
    private static View currentNotificationView;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable dismissRunnable;

    public static void show(Context context, String message) {
        show(context, "Thông báo", message, Type.INFO, DEFAULT_DURATION);
    }

    public static void show(Context context, String message, Type type) {
        show(context, "Thông báo", message, type, DEFAULT_DURATION);
    }

    public static void show(Context context, String title, String message, Type type) {
        show(context, title, message, type, DEFAULT_DURATION);
    }

    public static void show(Context context, String title, String message, Type type, int duration) {
        if (!(context instanceof Activity)) {
            return;
        }

        Activity activity = (Activity) context;
        
        // Dismiss current notification if exists
        dismissCurrent();

        // Inflate the custom notification layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View notificationView = inflater.inflate(R.layout.custom_notification, null);

        // Set up the notification content
        TextView titleTextView = notificationView.findViewById(R.id.notification_title);
        TextView messageTextView = notificationView.findViewById(R.id.notification_message);
        ImageView iconImageView = notificationView.findViewById(R.id.notification_icon);
        ImageView closeImageView = notificationView.findViewById(R.id.notification_close);

        titleTextView.setText(title);
        messageTextView.setText(message);
        iconImageView.setImageResource(type.getIconRes());

        // Set up close button
        closeImageView.setOnClickListener(v -> dismissCurrent());

        // Get the root view of the activity
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        
        // Create layout parameters for positioning at the top
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP;
        params.topMargin = getStatusBarHeight(context);

        // Add the notification to the root view
        rootView.addView(notificationView, params);
        currentNotificationView = notificationView;

        // Start slide-in animation
        Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.notification_slide_in);
        notificationView.startAnimation(slideIn);

        // Auto-dismiss after duration
        dismissRunnable = () -> dismissCurrent();
        handler.postDelayed(dismissRunnable, duration);
    }

    public static void dismissCurrent() {
        if (currentNotificationView != null && currentNotificationView.getParent() != null) {
            // Cancel auto-dismiss
            if (dismissRunnable != null) {
                handler.removeCallbacks(dismissRunnable);
            }

            // Start slide-out animation
            Animation slideOut = AnimationUtils.loadAnimation(
                    currentNotificationView.getContext(), 
                    R.anim.notification_slide_out
            );
            
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (currentNotificationView != null && currentNotificationView.getParent() != null) {
                        ((ViewGroup) currentNotificationView.getParent()).removeView(currentNotificationView);
                        currentNotificationView = null;
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            currentNotificationView.startAnimation(slideOut);
        }
    }

    private static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    // Convenient methods for different types
    public static void showSuccess(Context context, String message) {
        show(context, "Thành công", message, Type.SUCCESS);
    }

    public static void showError(Context context, String message) {
        show(context, "Lỗi", message, Type.ERROR);
    }

    public static void showWarning(Context context, String message) {
        show(context, "Cảnh báo", message, Type.WARNING);
    }

    public static void showInfo(Context context, String message) {
        show(context, "Thông tin", message, Type.INFO);
    }
}
