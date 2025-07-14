package com.example.miniproject.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Utility class cho các helper methods thông dụng
 */
public class Utils {
    
    /**
     * Hiển thị Toast message
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Hiển thị Toast message dài
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Kiểm tra string có empty không
     */
    public static boolean isStringEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Tạo chat room title mới
     */
    public static String generateChatRoomTitle(int existingChatCount) {
        if (existingChatCount == 0) {
            return Constants.Chat.DEFAULT_CHAT_TITLE;
        }
        return Constants.Chat.DEFAULT_CHAT_TITLE + " (" + (existingChatCount + 1) + ")";
    }
    
    /**
     * Validation cho email
     */
    public static boolean isValidEmail(String email) {
        return !isStringEmpty(email) && 
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Validation cho password (tối thiểu 6 ký tự)
     */
    public static boolean isValidPassword(String password) {
        return !isStringEmpty(password) && password.length() >= 6;
    }
}
