package com.example.miniproject.services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.StudySchedule;
import com.example.miniproject.models.DailySession;
import com.example.miniproject.models.ActivityModel;
import com.example.miniproject.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.appwrite.Query;
import io.appwrite.coroutines.Callback;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;
import io.appwrite.services.Databases;

public class StudyScheduleService {
    private static final String TAG = "StudyScheduleService";
    private AppwriteHelper appwriteHelper;
    private Databases databases;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public StudyScheduleService(Context context) {
        appwriteHelper = AppwriteHelper.getInstance(context);
        databases = appwriteHelper.getDatabases();
    }

    public interface StudyScheduleCallback<T> {
        void onSuccess(T result);

        void onError(Exception error);
    }

    public void getStudyScheduleByUserId(String userId, StudyScheduleCallback<List<StudySchedule>> callback) {
        List<String> queries = new ArrayList<>();
        queries.add(Query.Companion.equal("user_id", userId));
        try {
            databases.listDocuments(
                    AppwriteHelper.DATABASE_ID,
                    Constants.Appwrite.STUDY_SCHEDULE_COLLECTION_ID,
                    queries,
                    new CoroutineCallback<>(new Callback<DocumentList<Map<String, Object>>>() {
                        @Override
                        public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                            mainHandler.post(() -> {
                                if (error != null) {
                                    Log.e(TAG, "Error fetching study schedules by userId", error);
                                    callback.onError(new Exception(error.getMessage()));
                                } else {
                                    List<StudySchedule> schedules = new ArrayList<>();
                                    for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                        try {
                                            StudySchedule schedule = documentToStudySchedule(doc);
                                            schedules.add(schedule);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error converting document to StudySchedule", e);
                                        }
                                    }
                                    try {
                                        callback.onSuccess(schedules);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                    }));
        } catch (AppwriteException e) {
            callback.onError(e);
        }
    }

    public void getStudyScheduleById(String id, StudyScheduleCallback<StudySchedule> callback) {
        try {
            databases.getDocument(
                    AppwriteHelper.DATABASE_ID,
                    Constants.Appwrite.STUDY_SCHEDULE_COLLECTION_ID,
                    id,
                    new CoroutineCallback<>(new Callback<Document<Map<String, Object>>>() {
                        @Override
                        public void onComplete(Document<Map<String, Object>> result, Throwable error) {
                            mainHandler.post(() -> {
                                if (error != null) {
                                    Log.e(TAG, "Error fetching study schedule by id", error);
                                    callback.onError(new Exception(error.getMessage()));
                                } else {
                                    try {
                                        StudySchedule schedule = documentToStudySchedule(result);
                                        callback.onSuccess(schedule);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error converting document to StudySchedule", e);
                                        callback.onError(e);
                                    }
                                }
                            });
                        }
                    }));
        } catch (AppwriteException e) {
            callback.onError(e);
        }
    }

    private StudySchedule documentToStudySchedule(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        String id = doc.getId();
        String title = (String) data.get("title");
        String userId = (String) data.get("user_id");
        String status = (String) data.get("status");
        String createdAtStr = (String) data.get("created_at");
        String startDateStr = (String) data.get("start_date");
        String endDateStr = (String) data.get("end_date");
        String subject = (String) data.get("subject");
        List<String> weeklyPlanId = (List<String>) data.get("weekly_plan_id");
        String dailySessionId = (String) data.get("daily_session_id");
        List<String> milestonesId = (List<String>) data.get("milestones_id");

        Date createdAt = parseAppwriteDate(createdAtStr);
        Date startDate = parseAppwriteDate(startDateStr);
        Date endDate = parseAppwriteDate(endDateStr);

        return new StudySchedule(
                id,
                title != null ? title : "",
                userId != null ? userId : "",
                status != null ? status : "",
                createdAt,
                startDate,
                endDate,
                subject != null ? subject : "",
                weeklyPlanId != null ? weeklyPlanId : new ArrayList<>(),
                dailySessionId != null ? dailySessionId : "",
                milestonesId != null ? milestonesId : new ArrayList<>());
    }

    private Date parseAppwriteDate(String dateStr) {
        if (dateStr == null)
            return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse date: " + dateStr, e);
            return null;
        }
    }

    // --- DailySession ---
    public interface DailySessionCallback {
        void onSuccess(DailySession session);

        void onError(Exception error);
    }

    public void getDailySessionById(String id, DailySessionCallback callback) {
        try {
            databases.getDocument(
                    AppwriteHelper.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.DAILY_SESSION_COLLECTION_ID,
                    id,
                    new CoroutineCallback<>(new Callback<Document<Map<String, Object>>>() {
                        @Override
                        public void onComplete(Document<Map<String, Object>> result, Throwable error) {
                            mainHandler.post(() -> {
                                if (error != null) {
                                    callback.onError(new Exception(error.getMessage()));
                                } else {
                                    try {
                                        DailySession session = documentToDailySession(result);
                                        callback.onSuccess(session);
                                    } catch (Exception e) {
                                        callback.onError(e);
                                    }
                                }
                            });
                        }
                    }));
        } catch (AppwriteException e) {
            callback.onError(e);
        }
    }

    private DailySession documentToDailySession(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        String id = doc.getId();
        String sessionTitle = (String) data.get("session_title");
        Integer durationMinutes = data.get("duration_minutes") instanceof Number
                ? ((Number) data.get("duration_minutes")).intValue()
                : null;
        List<String> studyDays = new ArrayList<>();
        Object studyDaysObj = data.get("study_days");
        if (studyDaysObj instanceof List) {
            for (Object o : (List<?>) studyDaysObj) {
                if (o != null)
                    studyDays.add(o.toString());
            }
        }
        List<String> activitiesId = new ArrayList<>();
        Object activitiesIdObj = data.get("activities_id");
        if (activitiesIdObj instanceof List) {
            for (Object o : (List<?>) activitiesIdObj) {
                if (o != null)
                    activitiesId.add(o.toString());
            }
        }
        String studyScheduleId = (String) data.get("study_schedule_id");
        return new DailySession(id, sessionTitle, durationMinutes, studyDays, activitiesId, studyScheduleId);
    }

    // --- Activity ---
    public interface ActivityCallback {
        void onSuccess(ActivityModel activity);

        void onError(Exception error);
    }

    public void getActivityById(String id, ActivityCallback callback) {
        try {
            databases.getDocument(
                    AppwriteHelper.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.ACTIVITY_COLLECTION_ID,
                    id,
                    new CoroutineCallback<>(new Callback<Document<Map<String, Object>>>() {
                        @Override
                        public void onComplete(Document<Map<String, Object>> result, Throwable error) {
                            mainHandler.post(() -> {
                                if (error != null) {
                                    callback.onError(new Exception(error.getMessage()));
                                } else {
                                    try {
                                        ActivityModel activity = documentToActivity(result);
                                        callback.onSuccess(activity);
                                    } catch (Exception e) {
                                        callback.onError(e);
                                    }
                                }
                            });
                        }
                    }));
        } catch (AppwriteException e) {
            callback.onError(e);
        }
    }

    private ActivityModel documentToActivity(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        String id = doc.getId();
        String dailySessionId = (String) data.get("daily_session_id");
        String name = (String) data.get("name");
        Integer durationMinutes = data.get("duration_minutes") instanceof Number
                ? ((Number) data.get("duration_minutes")).intValue()
                : null;
        String description = (String) data.get("description");
        List<String> techniques = new ArrayList<>();
        Object techniquesObj = data.get("techniques");
        if (techniquesObj instanceof List) {
            for (Object o : (List<?>) techniquesObj) {
                if (o != null)
                    techniques.add(o.toString());
            }
        }
        return new ActivityModel(id, dailySessionId, name, durationMinutes, description, techniques);
    }

    // --- Milestone ---
    public interface MilestoneCallback {
        void onSuccess(com.example.miniproject.models.Milestone milestone);

        void onError(Exception error);
    }

    public void getMilestoneById(String id, MilestoneCallback callback) {
        try {
            databases.getDocument(
                    AppwriteHelper.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.MILESTONE_COLLECTION_ID,
                    id,
                    new CoroutineCallback<>(new Callback<Document<Map<String, Object>>>() {
                        @Override
                        public void onComplete(Document<Map<String, Object>> result, Throwable error) {
                            mainHandler.post(() -> {
                                if (error != null) {
                                    callback.onError(new Exception(error.getMessage()));
                                } else {
                                    try {
                                        com.example.miniproject.models.Milestone milestone = documentToMilestone(
                                                result);
                                        callback.onSuccess(milestone);
                                    } catch (Exception e) {
                                        callback.onError(e);
                                    }
                                }
                            });
                        }
                    }));
        } catch (AppwriteException e) {
            callback.onError(e);
        }
    }

    private com.example.miniproject.models.Milestone documentToMilestone(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        String id = doc.getId();
        int integerId = data.get("id") instanceof Number ? ((Number) data.get("id")).intValue() : 0;
        String description = (String) data.get("description");
        String targetCompletion = (String) data.get("target_completion");
        String studyScheduleId = (String) data.get("study_schedule_id");
        return new com.example.miniproject.models.Milestone(id, integerId, description, targetCompletion,
                studyScheduleId);
    }

    // --- WeeklyPlan ---
    public interface WeeklyPlanCallback {
        void onSuccess(com.example.miniproject.models.WeeklyPlan weeklyPlan);

        void onError(Exception error);
    }

    public void getWeeklyPlanById(String id, WeeklyPlanCallback callback) {
        try {
            databases.getDocument(
                    AppwriteHelper.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.WEEKLY_PLAN_COLLECTION_ID,
                    id,
                    new CoroutineCallback<>(new Callback<Document<Map<String, Object>>>() {
                        @Override
                        public void onComplete(Document<Map<String, Object>> result, Throwable error) {
                            mainHandler.post(() -> {
                                if (error != null) {
                                    callback.onError(new Exception(error.getMessage()));
                                } else {
                                    try {
                                        com.example.miniproject.models.WeeklyPlan weeklyPlan = documentToWeeklyPlan(
                                                result);
                                        callback.onSuccess(weeklyPlan);
                                    } catch (Exception e) {
                                        callback.onError(e);
                                    }
                                }
                            });
                        }
                    }));
        } catch (AppwriteException e) {
            callback.onError(e);
        }
    }

    private com.example.miniproject.models.WeeklyPlan documentToWeeklyPlan(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        String id = doc.getId();
        int week = data.get("week") instanceof Number ? ((Number) data.get("week")).intValue() : 0;
        String focus = (String) data.get("focus");
        Object topicsObj = data.get("topics");
        java.util.List<String> topics = new java.util.ArrayList<>();
        if (topicsObj instanceof java.util.List) {
            for (Object o : (java.util.List<?>) topicsObj) {
                if (o != null)
                    topics.add(o.toString());
            }
        }
        String objective = (String) data.get("objective");
        String studyScheduleId = (String) data.get("study_schedule_id");
        return new com.example.miniproject.models.WeeklyPlan(id, week, focus, topics, objective, studyScheduleId);
    }
}