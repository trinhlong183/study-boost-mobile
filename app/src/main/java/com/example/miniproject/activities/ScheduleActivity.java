package com.example.miniproject.activities;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.miniproject.R;
import android.widget.TextView;
import com.example.miniproject.api.AppwriteHelper;
import io.appwrite.models.User;
import java.util.Map;
import io.appwrite.Query;
import io.appwrite.coroutines.Callback;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.models.DocumentList;
import io.appwrite.models.Document;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.miniproject.adapters.WeeklyPlanAdapter;
import com.example.miniproject.models.WeeklyPlan;
import com.example.miniproject.services.WeeklyPlanService;
import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity implements WeeklyPlanAdapter.OnWeeklyPlanClickListener {
    private RecyclerView recyclerViewWeeklyPlans;
    private WeeklyPlanAdapter weeklyPlanAdapter;
    private WeeklyPlanService weeklyPlanService;
    private List<WeeklyPlan> weeklyPlanList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        initViews();
        initServices();
        setupRecyclerView();

        TextView tvSchedulePlaceholder = findViewById(R.id.tvSchedulePlaceholder);

        // Fetch current user
        AppwriteHelper.getInstance(this).getCurrentUser(new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
            @Override
            public void onSuccess(User<Map<String, Object>> user) {
                String userId = user.getId();
                fetchStudySchedules(userId, tvSchedulePlaceholder);
            }

            @Override
            public void onError(Exception error) {
                runOnUiThread(() -> tvSchedulePlaceholder.setText("Failed to get user info"));
            }
        });
    }

    private void initViews() {
        recyclerViewWeeklyPlans = findViewById(R.id.recyclerViewWeeklyPlans);
    }

    private void initServices() {
        weeklyPlanService = new WeeklyPlanService(this);
        weeklyPlanList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        weeklyPlanAdapter = new WeeklyPlanAdapter(weeklyPlanList, this);
        recyclerViewWeeklyPlans.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewWeeklyPlans.setAdapter(weeklyPlanAdapter);
    }

    private void fetchStudySchedules(String userId, TextView tv) {
        try {
            AppwriteHelper.getInstance(this).getDatabases().listDocuments(
                    com.example.miniproject.utils.Constants.Appwrite.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES_SCHEDULE_ID,
                    java.util.Collections.singletonList(Query.Companion.equal("user_id", userId)),
                    new CoroutineCallback<DocumentList<Map<String, Object>>>(
                            new Callback<DocumentList<Map<String, Object>>>() {
                                @Override
                                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                                    if (error != null) {
                                        runOnUiThread(() -> tv.setText("Failed to fetch study schedules"));
                                    } else if (result != null) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("Lịch học của bạn:\n\n");

                                        List<String> allWeeklyPlanIds = new ArrayList<>();

                                        for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                            String title = (String) doc.getData().get("title");
                                            String subject = (String) doc.getData().get("subject");
                                            sb.append("📚 ").append(title).append("\n");
                                            if (subject != null && !subject.isEmpty()) {
                                                sb.append("   Môn học: ").append(subject).append("\n");
                                            }
                                            sb.append("\n");

                                            // Collect weekly plan IDs
                                            Object weeklyPlanIds = doc.getData().get("weekly_plan_id");
                                            if (weeklyPlanIds instanceof List) {
                                                for (Object id : (List<?>) weeklyPlanIds) {
                                                    allWeeklyPlanIds.add(id.toString());
                                                }
                                            }
                                        }

                                        runOnUiThread(() -> tv.setText(sb.toString()));

                                        // Fetch weekly plans
                                        if (!allWeeklyPlanIds.isEmpty()) {
                                            fetchWeeklyPlans(allWeeklyPlanIds);
                                        }
                                    }
                                }
                            }));
        } catch (Exception e) {
            runOnUiThread(() -> tv.setText("Error: " + e.getMessage()));
        }
    }

    private void fetchWeeklyPlans(List<String> weeklyPlanIds) {
        weeklyPlanService.getWeeklyPlansByIds(weeklyPlanIds, new WeeklyPlanService.WeeklyPlanCallback() {
            @Override
            public void onSuccess(List<WeeklyPlan> weeklyPlans) {
                runOnUiThread(() -> {
                    weeklyPlanList.clear();
                    weeklyPlanList.addAll(weeklyPlans);
                    weeklyPlanAdapter.updatePlans(weeklyPlanList);
                });
            }

            @Override
            public void onError(Exception error) {
                Log.e("ScheduleActivity", "Error fetching weekly plans", error);
            }
        });
    }

    @Override
    public void onWeeklyPlanClick(WeeklyPlan weeklyPlan) {
        // Handle weekly plan click in ScheduleActivity
        // For now, just show a toast with the week info
        android.widget.Toast.makeText(this,
                "Đã chọn Tuần " + weeklyPlan.week + ": " + weeklyPlan.focus,
                android.widget.Toast.LENGTH_SHORT).show();
    }
}