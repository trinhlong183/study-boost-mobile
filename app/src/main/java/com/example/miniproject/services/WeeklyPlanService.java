package com.example.miniproject.services;

import android.content.Context;
import android.util.Log;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.WeeklyPlan;
import io.appwrite.Query;
import io.appwrite.coroutines.Callback;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class WeeklyPlanService {
    private static final String TAG = "WeeklyPlanService";
    private AppwriteHelper appwriteHelper;

    public WeeklyPlanService(Context context) {
        appwriteHelper = AppwriteHelper.getInstance(context);
    }

    public interface WeeklyPlanCallback {
        void onSuccess(List<WeeklyPlan> weeklyPlans);

        void onError(Exception error);
    }

    public void getWeeklyPlansByIds(List<String> weeklyPlanIds, WeeklyPlanCallback callback) {
        if (weeklyPlanIds == null || weeklyPlanIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        try {
            appwriteHelper.getDatabases().listDocuments(
                    com.example.miniproject.utils.Constants.Appwrite.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES_WEEKLY_PLAN_ID,
                    java.util.Collections.singletonList(Query.Companion.equal("$id", weeklyPlanIds)),
                    new CoroutineCallback<DocumentList<Map<String, Object>>>(
                            new Callback<DocumentList<Map<String, Object>>>() {
                                @Override
                                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                                    if (error != null) {
                                        Log.e(TAG, "Error fetching weekly plans", error);
                                        Log.e(TAG, "Collection ID used: "
                                                + com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES_WEEKLY_PLAN_ID);

                                        // Create fallback data
                                        List<WeeklyPlan> fallbackPlans = createFallbackWeeklyPlans();
                                        callback.onSuccess(fallbackPlans);
                                    } else if (result != null) {
                                        List<WeeklyPlan> weeklyPlans = new ArrayList<>();
                                        for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                            WeeklyPlan plan = documentToWeeklyPlan(doc);
                                            weeklyPlans.add(plan);
                                        }

                                        if (weeklyPlans.isEmpty()) {
                                            // If no real data found, use fallback
                                            weeklyPlans = createFallbackWeeklyPlans();
                                        }

                                        // Sort by week number
                                        Collections.sort(weeklyPlans, new Comparator<WeeklyPlan>() {
                                            @Override
                                            public int compare(WeeklyPlan w1, WeeklyPlan w2) {
                                                return Integer.compare(w1.week, w2.week);
                                            }
                                        });

                                        Log.d(TAG, "Successfully fetched " + weeklyPlans.size() + " weekly plans");
                                        callback.onSuccess(weeklyPlans);
                                    }
                                }
                            }));
        } catch (AppwriteException e) {
            Log.e(TAG, "AppwriteException when fetching weekly plans", e);

            // Create fallback data on exception
            List<WeeklyPlan> fallbackPlans = createFallbackWeeklyPlans();
            callback.onSuccess(fallbackPlans);
        }
    }

    private WeeklyPlan documentToWeeklyPlan(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();

        WeeklyPlan plan = new WeeklyPlan();
        plan.id = doc.getId();

        Object weekObj = data.get("week");
        plan.week = weekObj != null ? Integer.parseInt(weekObj.toString()) : 0;

        plan.focus = (String) data.get("focus");
        plan.objective = (String) data.get("objective");
        plan.study_schedule_id = (String) data.get("study_schedule_id");

        Object topicsObj = data.get("topics");
        if (topicsObj instanceof List) {
            plan.topics = new ArrayList<>();
            for (Object topic : (List<?>) topicsObj) {
                plan.topics.add(topic.toString());
            }
        }

        return plan;
    }

    private List<WeeklyPlan> createFallbackWeeklyPlans() {
        List<WeeklyPlan> fallbackPlans = new ArrayList<>();

        // Create sample weekly plans
        WeeklyPlan week1 = new WeeklyPlan();
        week1.id = "fallback_week_1";
        week1.week = 1;
        week1.focus = "Nền tảng Ngữ pháp và Từ vựng Sơ cấp";
        week1.objective = "Nắm vững các cấu trúc cơ bản và xây dựng nền tảng vững chắc để học hiệu quả ở mức 50-70 từ vựng cơ bản";
        week1.topics = java.util.Arrays.asList("Ngữ pháp cơ bản", "Từ vựng hàng ngày", "Phát âm");

        WeeklyPlan week2 = new WeeklyPlan();
        week2.id = "fallback_week_2";
        week2.week = 2;
        week2.focus = "Giao tiếp và Thực hành";
        week2.objective = "Phát triển kỹ năng giao tiếp cơ bản và thực hành sử dụng ngôn ngữ trong các tình huống thường ngày";
        week2.topics = java.util.Arrays.asList("Hội thoại cơ bản", "Nghe hiểu", "Viết câu đơn giản");

        fallbackPlans.add(week1);
        fallbackPlans.add(week2);

        // Sort fallback plans as well
        Collections.sort(fallbackPlans, new Comparator<WeeklyPlan>() {
            @Override
            public int compare(WeeklyPlan w1, WeeklyPlan w2) {
                return Integer.compare(w1.week, w2.week);
            }
        });

        Log.d(TAG, "Created " + fallbackPlans.size() + " fallback weekly plans");
        return fallbackPlans;
    }
}
