package com.example.miniproject.fragments;

import com.example.miniproject.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import com.example.miniproject.api.AppwriteHelper;
import io.appwrite.models.User;
import java.util.Map;
import io.appwrite.Query;
import io.appwrite.coroutines.Callback;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.models.DocumentList;
import io.appwrite.models.Document;
import com.example.miniproject.models.DailySession;
import com.example.miniproject.models.Activity;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.adapters.CalendarDayAdapter;
import com.example.miniproject.adapters.WeeklyPlanAdapter;
import com.example.miniproject.adapters.MilestoneAdapter;
import com.example.miniproject.models.WeeklyPlan;
import com.example.miniproject.models.Milestone;
import com.example.miniproject.services.WeeklyPlanService;
import com.example.miniproject.services.MilestoneService;
import io.appwrite.exceptions.AppwriteException;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.widget.ImageButton;
import java.util.Collections;
import java.util.Comparator;

public class HomeFragment extends Fragment
        implements CalendarDayAdapter.OnActivityClickListener, WeeklyPlanAdapter.OnWeeklyPlanClickListener,
        MilestoneAdapter.OnMilestoneClickListener {

    private RecyclerView recyclerViewCalendar;
    private RecyclerView recyclerViewWeeklyPlans;
    private RecyclerView recyclerViewMilestones;
    private CalendarDayAdapter calendarDayAdapter;
    private WeeklyPlanAdapter weeklyPlanAdapter;
    private MilestoneAdapter milestoneAdapter;
    private WeeklyPlanService weeklyPlanService;
    private MilestoneService milestoneService;
    private List<WeeklyPlan> weeklyPlanList;
    private List<WeeklyPlan> allWeeklyPlans = new ArrayList<>();
    private List<Milestone> milestoneList;
    private List<Milestone> allMilestones = new ArrayList<>();
    private final List<String> weekDays = java.util.Arrays.asList("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7",
            "Chủ nhật");
    private final java.util.Map<String, java.util.List<Activity>> activitiesByDay = new java.util.HashMap<>();
    private final Map<String, String> dayEnToVi = new java.util.HashMap<String, String>() {
        {
            put("Monday", "Thứ 2");
            put("Tuesday", "Thứ 3");
            put("Wednesday", "Thứ 4");
            put("Thursday", "Thứ 5");
            put("Friday", "Thứ 6");
            put("Saturday", "Thứ 7");
            put("Sunday", "Chủ nhật");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        android.util.Log.d("Calendar", "HomeFragment onCreateView called");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        recyclerViewCalendar = view.findViewById(R.id.recyclerViewCalendar);
        recyclerViewWeeklyPlans = view.findViewById(R.id.recyclerViewWeeklyPlans);
        recyclerViewMilestones = view.findViewById(R.id.recyclerViewMilestones);

        // Initialize services
        weeklyPlanService = new WeeklyPlanService(getContext());
        milestoneService = new MilestoneService(getContext());
        weeklyPlanList = new ArrayList<>();
        allWeeklyPlans = new ArrayList<>();
        milestoneList = new ArrayList<>();
        allMilestones = new ArrayList<>();

        // Setup weekly plans adapter with click listener
        weeklyPlanAdapter = new WeeklyPlanAdapter(weeklyPlanList, this);
        recyclerViewWeeklyPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewWeeklyPlans.setAdapter(weeklyPlanAdapter);

        // Setup milestones adapter
        milestoneAdapter = new MilestoneAdapter(milestoneList, this);
        recyclerViewMilestones.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMilestones.setAdapter(milestoneAdapter);

        // Add test data initially
        addTestWeeklyPlans();
        addTestMilestones();

        // Setup calendar adapter
        calendarDayAdapter = new CalendarDayAdapter(weekDays, activitiesByDay, this);
        recyclerViewCalendar
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCalendar.setAdapter(calendarDayAdapter);

        // Fetch current user
        AppwriteHelper.getInstance(getContext())
                .getCurrentUser(new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(User<Map<String, Object>> user) {
                        String userId = user.getId();
                        android.util.Log.d("Calendar", "userId: " + userId);
                        fetchStudySchedules(userId);
                    }

                    @Override
                    public void onError(Exception error) {
                        android.util.Log.e("Calendar", "getCurrentUser error: " + error.getMessage(), error);
                    }
                });
        return view;
    }

    private void addTestWeeklyPlans() {
        // Add multiple test weekly plans
        WeeklyPlan testPlan1 = new WeeklyPlan();
        testPlan1.week = 1;
        testPlan1.focus = "Nền tảng Ngữ pháp và Từ vựng Sơ cấp";
        testPlan1.objective = "Nắm vững các cấu trúc cơ bản và xây dựng nền tảng vững chắc để học hiệu quả";

        WeeklyPlan testPlan2 = new WeeklyPlan();
        testPlan2.week = 2;
        testPlan2.focus = "Giao tiếp và Thực hành";
        testPlan2.objective = "Phát triển kỹ năng giao tiếp cơ bản và thực hành sử dụng ngôn ngữ";

        WeeklyPlan testPlan3 = new WeeklyPlan();
        testPlan3.week = 3;
        testPlan3.focus = "Cấu trúc ngữ liệu";
        testPlan3.objective = "Nắm vững cấu trúc văn phòng và kỹ năng tự nhiên";

        WeeklyPlan testPlan4 = new WeeklyPlan();
        testPlan4.week = 4;
        testPlan4.focus = "Hệ và Module";
        testPlan4.objective = "Tối ưu hóa quá trình học tập và rèn luyện kỹ năng nghề nghiệp";

        allWeeklyPlans.clear();
        allWeeklyPlans.add(testPlan1);
        allWeeklyPlans.add(testPlan2);
        allWeeklyPlans.add(testPlan3);
        allWeeklyPlans.add(testPlan4);

        // Sort by week number
        Collections.sort(allWeeklyPlans, new Comparator<WeeklyPlan>() {
            @Override
            public int compare(WeeklyPlan w1, WeeklyPlan w2) {
                return Integer.compare(w1.week, w2.week);
            }
        });

        updateWeeklyPlanDisplay();

        android.util.Log.d("Calendar", "Added " + allWeeklyPlans.size() + " test weekly plans");
    }

    private void updateWeeklyPlanDisplay() {
        weeklyPlanList.clear();

        // Always show only Week 1 in the main view
        for (WeeklyPlan plan : allWeeklyPlans) {
            if (plan.week == 1) {
                weeklyPlanList.add(plan);
                break;
            }
        }

        weeklyPlanAdapter.updatePlans(weeklyPlanList);
        android.util.Log.d("Calendar", "Updated weekly plan display. Count: " + weeklyPlanList.size());
    }

    private void addTestMilestones() {
        Milestone milestone1 = new Milestone();
        milestone1.id = 1;
        milestone1.description = "Viết và chạy thành chương trình thành công";
        milestone1.target_completion = "Kết thúc Tuần 1";

        Milestone milestone2 = new Milestone();
        milestone2.id = 2;
        milestone2.description = "Xây dựng được một ứng dụng";
        milestone2.target_completion = "Kết thúc Tuần 2";

        Milestone milestone3 = new Milestone();
        milestone3.id = 3;
        milestone3.description = "Tạo một chương trình quản lý";
        milestone3.target_completion = "Kết thúc Tuần 3";

        Milestone milestone4 = new Milestone();
        milestone4.id = 4;
        milestone4.description = "Viết một hàm tự định nghĩa";
        milestone4.target_completion = "Kết thúc Tuần 4";

        allMilestones.clear();
        allMilestones.add(milestone1);
        allMilestones.add(milestone2);
        allMilestones.add(milestone3);
        allMilestones.add(milestone4);

        updateMilestoneDisplay();

        android.util.Log.d("Calendar", "Added " + allMilestones.size() + " test milestones");
    }

    private void updateMilestoneDisplay() {
        milestoneList.clear();

        // Always show only Milestone 1 in the main view
        for (Milestone milestone : allMilestones) {
            if (milestone.id == 1) {
                milestoneList.add(milestone);
                break;
            }
        }

        milestoneAdapter.updateMilestones(milestoneList);
        android.util.Log.d("Calendar", "Updated milestone display. Count: " + milestoneList.size());
    }

    @Override
    public void onWeeklyPlanClick(WeeklyPlan weeklyPlan) {
        // Show modal with all weekly plans
        showWeeklyPlansModal();
    }

    private void showWeeklyPlansModal() {
        if (getContext() == null)
            return;

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_weekly_plans);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Setup close button
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseDialog);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Setup RecyclerView with all weekly plans
        RecyclerView recyclerViewDialog = dialog.findViewById(R.id.recyclerViewDialogWeeklyPlans);
        WeeklyPlanAdapter dialogAdapter = new WeeklyPlanAdapter(allWeeklyPlans, plan -> {
            // Handle click on individual plan in modal
            android.widget.Toast.makeText(getContext(),
                    "Tuần " + plan.week + ": " + plan.focus,
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        recyclerViewDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDialog.setAdapter(dialogAdapter);

        dialog.show();
    }

    @Override
    public void onMilestoneClick(Milestone milestone) {
        showMilestonesModal();
    }

    private void showMilestonesModal() {
        if (getContext() == null)
            return;

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_milestones);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Setup close button
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseMilestoneDialog);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Setup RecyclerView with all milestones
        RecyclerView recyclerViewDialog = dialog.findViewById(R.id.recyclerViewDialogMilestones);
        MilestoneAdapter dialogAdapter = new MilestoneAdapter(allMilestones, milestone -> {
            android.widget.Toast.makeText(getContext(),
                    "Cột mốc " + milestone.id + ": " + milestone.description,
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        recyclerViewDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDialog.setAdapter(dialogAdapter);

        dialog.show();
    }

    private void fetchStudySchedules(String userId) {
        try {
            AppwriteHelper.getInstance(getContext()).getDatabases().listDocuments(
                    com.example.miniproject.utils.Constants.Appwrite.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES_SCHEDULE_ID,
                    java.util.Collections.singletonList(Query.Companion.equal("user_id", userId)),
                    new CoroutineCallback<DocumentList<Map<String, Object>>>(
                            new Callback<DocumentList<Map<String, Object>>>() {
                                @Override
                                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                                    if (error == null && result != null) {
                                        List<String> dailySessionIds = new java.util.ArrayList<>();
                                        List<String> weeklyPlanIds = new java.util.ArrayList<>();
                                        List<String> milestoneIds = new java.util.ArrayList<>();

                                        for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                            String scheduleId = doc.getId();
                                            Object dailySessionId = doc.getData().get("daily_session_id");
                                            Object weeklyPlanIdList = doc.getData().get("weekly_plan_id");
                                            Object milestoneIdList = doc.getData().get("milestones_id");

                                            android.util.Log.d("Calendar", "study_schedule_id: " + scheduleId
                                                    + ", daily_session_id: " + dailySessionId);

                                            if (dailySessionId != null) {
                                                dailySessionIds.add(dailySessionId.toString());
                                            }

                                            // Collect weekly plan IDs
                                            if (weeklyPlanIdList instanceof List) {
                                                for (Object id : (List<?>) weeklyPlanIdList) {
                                                    weeklyPlanIds.add(id.toString());
                                                }
                                            }

                                            // Collect milestone IDs
                                            if (milestoneIdList instanceof List) {
                                                for (Object id : (List<?>) milestoneIdList) {
                                                    milestoneIds.add(id.toString());
                                                }
                                            }
                                        }

                                        // Fetch weekly plans
                                        if (!weeklyPlanIds.isEmpty()) {
                                            fetchWeeklyPlans(weeklyPlanIds);
                                        }

                                        // Fetch milestones
                                        if (!milestoneIds.isEmpty()) {
                                            fetchMilestones(milestoneIds);
                                        }

                                        // Fetch daily sessions
                                        if (!dailySessionIds.isEmpty()) {
                                            fetchDailySessions(dailySessionIds);
                                        }
                                    } else {
                                        android.util.Log.e("Calendar", "fetchStudySchedules error: "
                                                + (error != null ? error.getMessage() : "null"), error);
                                    }
                                }
                            }));
        } catch (AppwriteException e) {
            e.printStackTrace();
            android.util.Log.e("Calendar", "fetchStudySchedules AppwriteException: " + e.getMessage(), e);
        }
    }

    private void fetchWeeklyPlans(List<String> weeklyPlanIds) {
        android.util.Log.d("Calendar", "fetchWeeklyPlans called with " + weeklyPlanIds.size() + " IDs");
        android.util.Log.d("Calendar", "Using collection ID: "
                + com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES_WEEKLY_PLAN_ID);

        weeklyPlanService.getWeeklyPlansByIds(weeklyPlanIds, new WeeklyPlanService.WeeklyPlanCallback() {
            @Override
            public void onSuccess(List<WeeklyPlan> weeklyPlans) {
                android.util.Log.d("Calendar", "Successfully fetched " + weeklyPlans.size() + " weekly plans");

                requireActivity().runOnUiThread(() -> {
                    allWeeklyPlans.clear();
                    allWeeklyPlans.addAll(weeklyPlans);

                    // Sort by week number again to ensure proper order
                    Collections.sort(allWeeklyPlans, new Comparator<WeeklyPlan>() {
                        @Override
                        public int compare(WeeklyPlan w1, WeeklyPlan w2) {
                            return Integer.compare(w1.week, w2.week);
                        }
                    });

                    updateWeeklyPlanDisplay();

                    android.util.Log.d("Calendar", "Updated weekly plans: " + allWeeklyPlans.size());
                });
            }

            @Override
            public void onError(Exception error) {
                android.util.Log.e("Calendar", "Error fetching weekly plans: " + error.getMessage(), error);
            }
        });
    }

    private void fetchMilestones(List<String> milestoneIds) {
        android.util.Log.d("Calendar", "fetchMilestones called with " + milestoneIds.size() + " IDs");

        milestoneService.getMilestonesByIds(milestoneIds, new MilestoneService.MilestoneCallback() {
            @Override
            public void onSuccess(List<Milestone> milestones) {
                android.util.Log.d("Calendar", "Successfully fetched " + milestones.size() + " milestones");

                requireActivity().runOnUiThread(() -> {
                    allMilestones.clear();
                    allMilestones.addAll(milestones);

                    updateMilestoneDisplay();

                    android.util.Log.d("Calendar", "Updated milestones: " + allMilestones.size());
                });
            }

            @Override
            public void onError(Exception error) {
                android.util.Log.e("Calendar", "Error fetching milestones: " + error.getMessage(), error);
            }
        });
    }

    private void fetchDailySessions(List<String> dailySessionIds) {
        try {
            AppwriteHelper.getInstance(getContext()).getDatabases().listDocuments(
                    com.example.miniproject.utils.Constants.Appwrite.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES_SESSION_ID,
                    java.util.Collections.singletonList(Query.Companion.equal("$id", dailySessionIds)),
                    new CoroutineCallback<DocumentList<Map<String, Object>>>(
                            new Callback<DocumentList<Map<String, Object>>>() {
                                @Override
                                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                                    if (error == null && result != null) {
                                        java.util.Map<String, List<String>> activitiesIdByDay = new java.util.HashMap<>();
                                        java.util.Map<String, List<Activity>> tempActivitiesByDay = new java.util.HashMap<>();
                                        for (String day : weekDays)
                                            tempActivitiesByDay.put(day, new java.util.ArrayList<>());
                                        List<String> activityIds = new java.util.ArrayList<>();
                                        for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                            String dailySessionId = doc.getId();
                                            Object acts = doc.getData().get("activities_id");
                                            Object studyDays = doc.getData().get("study_days");
                                            android.util.Log.d("Calendar",
                                                    "daily_session_id: " + dailySessionId + ", activities_id: " + acts);
                                            if (acts instanceof List && studyDays instanceof List) {
                                                for (Object actId : (List<?>) acts) {
                                                    activityIds.add(actId.toString());
                                                }
                                                for (Object day : (List<?>) studyDays) {
                                                    String dayStr = day.toString();
                                                    String viDay = dayEnToVi.get(dayStr);
                                                    if (viDay != null && !activitiesIdByDay.containsKey(viDay))
                                                        activitiesIdByDay.put(viDay, new java.util.ArrayList<>());
                                                    for (Object actId : (List<?>) acts) {
                                                        if (viDay != null)
                                                            activitiesIdByDay.get(viDay).add(actId.toString());
                                                    }
                                                }
                                            }
                                        }
                                        if (!activityIds.isEmpty()) {
                                            fetchActivities(activityIds, activitiesIdByDay, tempActivitiesByDay);
                                        }
                                    } else {
                                        android.util.Log.e("Calendar", "fetchDailySessions error: "
                                                + (error != null ? error.getMessage() : "null"), error);
                                    }
                                }
                            }));
        } catch (AppwriteException e) {
            e.printStackTrace();
            android.util.Log.e("Calendar", "fetchDailySessions AppwriteException: " + e.getMessage(), e);
        }
    }

    private void fetchActivities(List<String> activityIds, java.util.Map<String, List<String>> activitiesIdByDay,
            java.util.Map<String, List<Activity>> tempActivitiesByDay) {
        try {
            AppwriteHelper.getInstance(getContext()).getDatabases().listDocuments(
                    com.example.miniproject.utils.Constants.Appwrite.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES_ACTIVITIES_ID,
                    java.util.Collections.singletonList(Query.Companion.equal("$id", activityIds)),
                    new CoroutineCallback<DocumentList<Map<String, Object>>>(
                            new Callback<DocumentList<Map<String, Object>>>() {
                                @Override
                                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                                    if (error == null && result != null) {
                                        java.util.Map<String, Activity> activityMap = new java.util.HashMap<>();
                                        for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                            Activity activity = new Activity();
                                            activity.id = doc.getId();
                                            activity.name = (String) doc.getData().get("name");
                                            Object duration = doc.getData().get("duration_minutes");
                                            activity.duration_minutes = duration != null
                                                    ? Integer.parseInt(duration.toString())
                                                    : 0;
                                            activity.description = (String) doc.getData().get("description");
                                            android.util.Log.d("Calendar",
                                                    "activity_id: " + activity.id + ", name: " + activity.name);
                                            activityMap.put(activity.id, activity);
                                        }
                                        // Gán activity vào từng ngày
                                        for (String day : weekDays) {
                                            List<String> ids = activitiesIdByDay.get(day);
                                            if (ids != null) {
                                                for (String id : ids) {
                                                    Activity act = activityMap.get(id);
                                                    if (act != null)
                                                        tempActivitiesByDay.get(day).add(act);
                                                }
                                            }
                                        }
                                        requireActivity().runOnUiThread(() -> {
                                            activitiesByDay.clear();
                                            activitiesByDay.putAll(tempActivitiesByDay);
                                            for (String day : weekDays) {
                                                android.util.Log.d("Calendar", day + ": "
                                                        + tempActivitiesByDay.get(day).size() + " activities");
                                            }
                                            if (calendarDayAdapter != null)
                                                calendarDayAdapter.notifyDataSetChanged();
                                        });
                                    } else {
                                        android.util.Log.e("Calendar", "fetchActivities error: "
                                                + (error != null ? error.getMessage() : "null"), error);
                                    }
                                }
                            }));
        } catch (AppwriteException e) {
            e.printStackTrace();
            android.util.Log.e("Calendar", "fetchActivities AppwriteException: " + e.getMessage(), e);
        }
    }

    @Override
    public void onActivityClick(Activity activity) {
        showActivityDetailDialog(activity);
    }

    private void showActivityDetailDialog(Activity activity) {
        if (getContext() == null)
            return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_activity_detail, null);

        TextView tvName = dialogView.findViewById(R.id.tvDialogActivityName);
        TextView tvDuration = dialogView.findViewById(R.id.tvDialogActivityDuration);
        TextView tvDescription = dialogView.findViewById(R.id.tvDialogActivityDescription);

        tvName.setText(activity.name);
        tvDuration.setText(activity.duration_minutes + " phút");
        tvDescription.setText(activity.description != null && !activity.description.isEmpty()
                ? activity.description
                : "Không có mô tả");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnDialogClose).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
