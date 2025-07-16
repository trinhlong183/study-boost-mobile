package com.example.miniproject.services;

import com.example.miniproject.utils.Constants;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appwrite.exceptions.AppwriteException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpService {
    private static final String TAG = "HttpService";
    private static final String N8N_WEBHOOK_URL = Constants.API.N8N_WEBHOOK_URL;

    private OkHttpClient client;
    private Gson gson;
    private ExecutorService executorService;
    private Handler mainHandler;

    public HttpService() {
        client = new OkHttpClient.Builder()
                .build();
        gson = new Gson();
        executorService = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface HttpCallback {
        void onSuccess(String aiResponse) throws AppwriteException;

        void onError(Exception error);
    }

    public void sendMessageToN8n(String userId, String messageContent, String chatRoomId, boolean isFromUser,
            HttpCallback callback) {
        executorService.execute(() -> {
            try {
                // Create request body
                JsonObject requestData = new JsonObject();
                requestData.addProperty("message_content", messageContent);
                requestData.addProperty("user_id", userId);
                requestData.addProperty("chat_room_id", chatRoomId);
                requestData.addProperty("is_from_user", isFromUser);

                String jsonBody = gson.toJson(requestData);
                Log.d(TAG, "Sending request to n8n: " + jsonBody);

                RequestBody body = RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        jsonBody);

                Request request = new Request.Builder()
                        .url(N8N_WEBHOOK_URL)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "n8n response: " + responseBody);

                        // Parse the response to get AI message
                        JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                        String aiMessage = responseJson.get("message_content").getAsString();

                        // Return result on main thread
                        mainHandler.post(() -> {
                            try {
                                callback.onSuccess(aiMessage);
                            } catch (AppwriteException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        Log.e(TAG, "n8n request failed: " + response.code() + " - " + response.message());
                        mainHandler.post(() -> callback.onError(new Exception("AI service temporarily unavailable")));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending message to n8n", e);
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
