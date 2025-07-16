package com.example.miniproject.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.SurveyQuestion;
import com.example.miniproject.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;
import io.appwrite.services.Databases;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;

public class SurveyService {
    private static final String TAG = "SurveyService";
    private AppwriteHelper appwriteHelper;
    private Databases databases;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private Gson gson = new Gson();
    private static final String N8N_SURVEY_WEBHOOK_URL = "https://n8n.minhphuoc.io.vn/webhook/survey/start";

    public SurveyService(Context context) {
        appwriteHelper = AppwriteHelper.getInstance(context);
        databases = appwriteHelper.getDatabases();
    }

    public interface SurveyCallback<T> {
        void onSuccess(T result) throws AppwriteException;

        void onError(Exception error);
    }

    public void getSurveyQuestions(SurveyCallback<List<SurveyQuestion>> callback) {
        try {
            databases.listDocuments(
                    AppwriteHelper.DATABASE_ID,
                    Constants.Appwrite.SURVEY_QUESTIONS_COLLECTION_ID,
                    new ArrayList<>(),
                    new io.appwrite.coroutines.CoroutineCallback<>(
                            new io.appwrite.coroutines.Callback<io.appwrite.models.DocumentList<java.util.Map<String, Object>>>() {
                                @Override
                                public void onComplete(
                                        io.appwrite.models.DocumentList<java.util.Map<String, Object>> result,
                                        Throwable error) {
                                    mainHandler.post(() -> {
                                        if (error != null) {
                                            Log.e(TAG, "Error fetching survey questions", error);
                                            callback.onError(new Exception(error.getMessage()));
                                        } else {
                                            List<SurveyQuestion> questions = new ArrayList<>();
                                            for (io.appwrite.models.Document<java.util.Map<String, Object>> doc : result
                                                    .getDocuments()) {
                                                try {
                                                    SurveyQuestion question = documentToSurveyQuestion(doc);
                                                    questions.add(question);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error converting document to SurveyQuestion", e);
                                                }
                                            }
                                            try {
                                                callback.onSuccess(questions);
                                            } catch (io.appwrite.exceptions.AppwriteException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    });
                                }
                            }));
        } catch (io.appwrite.exceptions.AppwriteException e) {
            callback.onError(e);
        }
    }

    private SurveyQuestion documentToSurveyQuestion(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        String id = doc.getId();
        String questionText = (String) data.get("question_text");
        Boolean required = (Boolean) data.get("required");
        String questionType = (String) data.get("question_type");
        String category = (String) data.get("category");
        String createdAtStr = (String) data.get("created_at");
        String updatedAtStr = (String) data.get("updated_at");
        Object optionsObj = data.get("options");
        Log.d(TAG, "Options raw value for question '" + questionText + "': " + optionsObj + " (type: "
                + (optionsObj != null ? optionsObj.getClass().getName() : "null") + ")");
        List<String> options = new ArrayList<>();
        if (optionsObj instanceof List) {
            for (Object o : (List<?>) optionsObj) {
                if (o != null)
                    options.add(o.toString());
            }
        } else if (optionsObj instanceof String) {
            String optStr = (String) optionsObj;
            if (optStr.contains(",")) {
                for (String s : optStr.split(",")) {
                    options.add(s.trim());
                }
            } else if (!optStr.isEmpty()) {
                options.add(optStr.trim());
            }
        }
        String questionNo = (String) data.get("question_no");

        Date createdAt = parseAppwriteDate(createdAtStr);
        Date updatedAt = parseAppwriteDate(updatedAtStr);

        return new SurveyQuestion(
                id,
                questionText != null ? questionText : "",
                required != null ? required : false,
                questionType != null ? questionType : "",
                category != null ? category : "",
                createdAt,
                updatedAt,
                options,
                questionNo != null ? questionNo : "");
    }

    private Date parseAppwriteDate(String dateStr) {
        if (dateStr == null)
            return null;
        try {
            // Appwrite usually returns ISO 8601 format
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse date: " + dateStr, e);
            return null;
        }
    }

    public interface SurveySendCallback {
        void onSuccess(String n8nResponse);

        void onError(Exception error);
    }

    public static class SurveyResponse {
        public String questionId;
        public String response;

        public SurveyResponse(String questionId, String response) {
            this.questionId = questionId;
            this.response = response;
        }
    }

    public void sendSurveyResponseTon8n(String userId, List<SurveyResponse> responses, SurveySendCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        JsonArray responsesArray = new JsonArray();
        for (SurveyResponse r : responses) {
            JsonObject respObj = new JsonObject();
            respObj.addProperty("questionId", r.questionId);
            respObj.addProperty("response", r.response);
            responsesArray.add(respObj);
        }
        body.add("responses", responsesArray);

        // Only send the body object, not the full requestJson
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(N8N_SURVEY_WEBHOOK_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String resp = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    mainHandler.post(() -> callback.onSuccess(resp));
                } else {
                    mainHandler.post(() -> callback.onError(new Exception("n8n error: " + resp)));
                }
            }
        });
    }
}