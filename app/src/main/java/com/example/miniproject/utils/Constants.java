package com.example.miniproject.utils;

/**
 * Constants class chứa tất cả các hằng số được sử dụng trong ứng dụng
 */
public class Constants {

    // Appwrite Configuration
    public static final class Appwrite {
        public static final String DATABASE_ID = "683088550003da865176";
        public static final String CHAT_ROOMS_COLLECTION_ID = "68501467002d495cea64";
        public static final String CHAT_MESSAGES_COLLECTION_ID = "683d8d1e001f59b8c2b1";
        public static final String USERS_COLLECTION_ID = "6836f49600230e4fca23";
        public static final String SURVEY_QUESTIONS_COLLECTION_ID = "683d88e8000b8520879d";
        public static final String STUDY_SCHEDULE_COLLECTION_ID = "68442203003c48ed85ee";
        public static final String DAILY_SESSION_COLLECTION_ID = "6845befe002bbc1fdca2";
        public static final String ACTIVITY_COLLECTION_ID = "6846537f001264ab943d";
        public static final String MILESTONE_COLLECTION_ID = "6846543500114458341e";
        public static final String WEEKLY_PLAN_COLLECTION_ID = "6846523c000e3e1bf949";
    }

    // API Endpoints
    public static final class API {
        public static final String N8N_WEBHOOK_URL = "https://n8n.minhphuoc.io.vn/webhook/989c0ca7-17f0-4840-9f34-6b106d938570/chat";
    }

    // UI Constants
    public static final class UI {
        public static final int LOADING_ANIMATION_DURATION = 1500;
        public static final int CHAT_BUBBLE_CORNER_RADIUS = 24;
        public static final int DEFAULT_ANIMATION_DURATION = 300;
    }

    // Chat
    public static final class Chat {
        public static final String DEFAULT_CHAT_TITLE = "Đoạn chat mới";
        public static final String LOADING_MESSAGE = "";
        public static final String ERROR_MESSAGE = "Sorry, something went wrong. Please try again.";
        public static final String AI_UNAVAILABLE_MESSAGE = "AI service temporarily unavailable";
    }

    // Error Messages
    public static final class ErrorMessages {
        public static final String USER_NOT_LOADED = "User not loaded";
        public static final String FAILED_TO_CREATE_CHAT_ROOM = "Failed to create chat room";
        public static final String FAILED_TO_LOAD_MESSAGES = "Failed to load messages";
        public static final String FAILED_TO_SEND_MESSAGE = "Failed to send message";
        public static final String LOGIN_REQUIRED = "Error: Please login first";
        public static final String EMPTY_FIELDS = "Name, email and password cannot be empty";
    }

    // Success Messages
    public static final class SuccessMessages {
        public static final String NEW_CHAT_CREATED = "New chat created";
        public static final String LOGIN_SUCCESSFUL = "Login successful";
        public static final String REGISTER_SUCCESSFUL = "Registration successful";
    }
}
