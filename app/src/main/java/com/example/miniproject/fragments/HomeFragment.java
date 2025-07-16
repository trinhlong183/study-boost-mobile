package com.example.miniproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject.R;
import com.example.miniproject.models.ActivityModel;
import com.example.miniproject.models.DailySession;
import com.example.miniproject.models.StudySchedule;
import com.example.miniproject.models.Milestone;
import com.example.miniproject.models.WeeklyPlan;
import com.example.miniproject.services.StudyScheduleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewCalendar;
    private CalendarAdapter calendarAdapter;
    private StudyScheduleService studyScheduleService;
    private List<String> weekDays = new ArrayList<>();
    private Map<String, List<ActivityModel>> activitiesByDay = new HashMap<>();
    private LinearLayout milestoneCard;
    private TextView milestoneIdView, milestoneDescriptionView, milestoneTargetCompletionView,
            milestoneStudyScheduleIdView;
    private List<Milestone> milestones = new ArrayList<>();
    private LinearLayout weeklyPlanCard;
    private TextView weeklyPlanWeekView, weeklyPlanFocusView, weeklyPlanTopicsView, weeklyPlanObjectiveView,
            weeklyPlanStudyScheduleIdView;
    private List<WeeklyPlan> weeklyPlans = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Add top padding to move content down
        int topPaddingDp = 64; // Adjust this value as needed
        float density = getResources().getDisplayMetrics().density;
        int topPaddingPx = (int) (topPaddingDp * density);
        view.setPadding(view.getPaddingLeft(), topPaddingPx, view.getPaddingRight(), view.getPaddingBottom());

        recyclerViewCalendar = view.findViewById(R.id.recyclerViewCalendar);
        milestoneCard = view.findViewById(R.id.milestoneCard);
        milestoneIdView = view.findViewById(R.id.milestoneId);
        milestoneDescriptionView = view.findViewById(R.id.milestoneDescription);
        milestoneTargetCompletionView = view.findViewById(R.id.milestoneTargetCompletion);
        // milestoneStudyScheduleIdView =
        // view.findViewById(R.id.milestoneStudyScheduleId);
        weeklyPlanCard = view.findViewById(R.id.weeklyPlanCard);
        weeklyPlanWeekView = view.findViewById(R.id.weeklyPlanWeek);
        weeklyPlanFocusView = view.findViewById(R.id.weeklyPlanFocus);
        weeklyPlanTopicsView = view.findViewById(R.id.weeklyPlanTopics);
        weeklyPlanObjectiveView = view.findViewById(R.id.weeklyPlanObjective);
        // weeklyPlanStudyScheduleIdView =
        // view.findViewById(R.id.weeklyPlanStudyScheduleId);
        studyScheduleService = new StudyScheduleService(requireContext());

        setupWeekDays();
        setupRecyclerView();
        fetchStudySchedule();
        setupMilestoneCardClick();
        setupWeeklyPlanCardClick();
        return view;
    }

    private void setupWeekDays() {
        weekDays.clear();
        weekDays.add("Thứ 2");
        weekDays.add("Thứ 3");
        weekDays.add("Thứ 4");
        weekDays.add("Thứ 5");
        weekDays.add("Thứ 6");
        weekDays.add("Thứ 7");
        weekDays.add("Chủ nhật");
    }

    private void setupRecyclerView() {
        calendarAdapter = new CalendarAdapter(weekDays, activitiesByDay);
        recyclerViewCalendar
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCalendar.setAdapter(calendarAdapter);
    }

    private void setupMilestoneCardClick() {
        milestoneCard.setOnClickListener(v -> {
            if (milestones.isEmpty())
                return;
            // Sort milestones by integerId
            java.util.List<Milestone> sortedMilestones = new java.util.ArrayList<>(milestones);
            java.util.Collections.sort(sortedMilestones, java.util.Comparator.comparingInt(Milestone::getIntegerId));
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_milestones, null);
            LinearLayout container = dialogView.findViewById(R.id.milestonesContainer);
            for (Milestone m : sortedMilestones) {
                View card = LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_milestone, container, false);
                TextView id = card.findViewById(R.id.milestoneId);
                TextView desc = card.findViewById(R.id.description);
                TextView target = card.findViewById(R.id.target);
                id.setText("Milestone: " + m.getIntegerId());
                desc.setText("Description: " + m.getDescription());
                target.setText("Target: " + m.getTargetCompletion());
                container.addView(card);
            }
            new AlertDialog.Builder(getContext())
                    .setTitle("All Milestones")
                    .setView(dialogView)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void setupWeeklyPlanCardClick() {
        weeklyPlanCard.setOnClickListener(v -> {
            if (weeklyPlans.isEmpty())
                return;
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_weekly_plans, null);
            LinearLayout container = dialogView.findViewById(R.id.weeklyPlansContainer);
            for (WeeklyPlan w : weeklyPlans) {
                View card = LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_weekly_plan, container,
                        false);
                TextView weekTitle = card.findViewById(R.id.weekTitle);
                TextView focus = card.findViewById(R.id.focus);
                TextView topics = card.findViewById(R.id.topics);
                TextView objective = card.findViewById(R.id.objective);
                weekTitle.setText("Week " + w.getWeek());
                focus.setText("Focus: " + w.getFocus());
                String topicsText = "Topics:";
                if (w.getTopics() != null && !w.getTopics().isEmpty()) {
                    topicsText += "\n• " + android.text.TextUtils.join("\n• ", w.getTopics());
                }
                topics.setText(topicsText);
                objective.setText("Objective: " + w.getObjective());
                container.addView(card);
            }
            new AlertDialog.Builder(getContext())
                    .setTitle("All Weekly Plans")
                    .setView(dialogView)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void fetchStudySchedule() {
        // Retrieve userId from SharedPreferences
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs",
                android.content.Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) {
            showToast("User not logged in");
            return;
        }
        Log.d("HomeFragment", "Fetching study schedule for userId: " + userId);
        studyScheduleService.getStudyScheduleByUserId(userId,
                new StudyScheduleService.StudyScheduleCallback<List<StudySchedule>>() {
                    @Override
                    public void onSuccess(List<StudySchedule> schedules) {
                        Log.d("HomeFragment",
                                "Study schedules fetched: " + (schedules != null ? schedules.size() : "null"));
                        if (schedules == null || schedules.isEmpty()) {
                            showToast("No study schedule found.");
                            return;
                        }
                        StudySchedule schedule = schedules.get(0);
                        Log.d("HomeFragment", "Using schedule: " + schedule.getId());
                        fetchMilestones(schedule.getMilestonesId());
                        fetchWeeklyPlans(schedule.getWeeklyPlanId());
                        fetchDailySession(schedule.getDailySessionId());
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e("HomeFragment", "Failed to load study schedule", error);
                        showToast("Failed to load study schedule: " + error.getMessage());
                    }
                });
    }

    private void fetchMilestones(List<String> milestoneIds) {
        milestones.clear();
        if (milestoneIds == null || milestoneIds.isEmpty()) {
            updateMilestoneCard(null);
            return;
        }
        final int[] remaining = { milestoneIds.size() };
        for (String milestoneId : milestoneIds) {
            studyScheduleService.getMilestoneById(milestoneId, new StudyScheduleService.MilestoneCallback() {
                @Override
                public void onSuccess(Milestone milestone) {
                    milestones.add(milestone);
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Milestone first = null;
                        for (Milestone m : milestones) {
                            if (m.getIntegerId() == 1) {
                                first = m;
                                break;
                            }
                        }
                        updateMilestoneCard(first);
                    }
                }

                @Override
                public void onError(Exception error) {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Milestone first = null;
                        for (Milestone m : milestones) {
                            if (m.getIntegerId() == 1) {
                                first = m;
                                break;
                            }
                        }
                        updateMilestoneCard(first);
                    }
                }
            });
        }
    }

    private void updateMilestoneCard(Milestone milestone) {
        if (milestone == null) {
            milestoneCard.setVisibility(View.GONE);
        } else {
            milestoneCard.setVisibility(View.VISIBLE);
            milestoneIdView.setText("Milestone: " + milestone.getIntegerId());
            milestoneIdView.setTextColor(getResources().getColor(R.color.primary_blue, null));
            milestoneIdView.setTypeface(null, Typeface.BOLD);

            milestoneDescriptionView.setText(milestone.getDescription());
            milestoneDescriptionView.setTextColor(getResources().getColor(R.color.text_dark, null));

            milestoneTargetCompletionView.setText("Target: " + milestone.getTargetCompletion());
            milestoneTargetCompletionView.setTextColor(getResources().getColor(R.color.accent_purple, null));
            milestoneTargetCompletionView.setTypeface(null, Typeface.BOLD);
        }
    }

    private void fetchWeeklyPlans(List<String> weeklyPlanIds) {
        weeklyPlans.clear();
        if (weeklyPlanIds == null || weeklyPlanIds.isEmpty()) {
            updateWeeklyPlanCard(null);
            return;
        }
        final int[] remaining = { weeklyPlanIds.size() };
        for (String weeklyPlanId : weeklyPlanIds) {
            studyScheduleService.getWeeklyPlanById(weeklyPlanId, new StudyScheduleService.WeeklyPlanCallback() {
                @Override
                public void onSuccess(WeeklyPlan weeklyPlan) {
                    weeklyPlans.add(weeklyPlan);
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        WeeklyPlan first = null;
                        for (WeeklyPlan w : weeklyPlans) {
                            if (w.getWeek() == 1) {
                                first = w;
                                break;
                            }
                        }
                        updateWeeklyPlanCard(first);
                    }
                }

                @Override
                public void onError(Exception error) {
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        WeeklyPlan first = null;
                        for (WeeklyPlan w : weeklyPlans) {
                            if (w.getWeek() == 1) {
                                first = w;
                                break;
                            }
                        }
                        updateWeeklyPlanCard(first);
                    }
                }
            });
        }
    }

    private void updateWeeklyPlanCard(WeeklyPlan weeklyPlan) {
        if (weeklyPlan == null) {
            weeklyPlanCard.setVisibility(View.GONE);
        } else {
            weeklyPlanCard.setVisibility(View.VISIBLE);

            weeklyPlanWeekView.setText("Week: " + weeklyPlan.getWeek());
            weeklyPlanWeekView.setTextColor(getResources().getColor(R.color.primary_blue, null));
            weeklyPlanWeekView.setTypeface(null, Typeface.BOLD);

            weeklyPlanFocusView.setText("Nền tảng và Cú pháp cơ bản");
            weeklyPlanFocusView.setTextColor(getResources().getColor(R.color.text_dark, null));
            weeklyPlanFocusView.setTypeface(null, Typeface.BOLD);

            // Format topics as bullet points
            String topicsText = "Topics: [Biến và các kiểu dữ liệu (Số, Chuỗi, Boolean), Các toán tử cơ bản (Số học, So sánh, Logic), Cấu trúc điều khiển (if-elif-else, Vòng lặp: for và while)]";
            if (weeklyPlan.getTopics() != null && !weeklyPlan.getTopics().isEmpty()) {
                topicsText = "Topics: [" + String.join(", ", weeklyPlan.getTopics()) + "]";
            }
            weeklyPlanTopicsView.setText(topicsText);
            weeklyPlanTopicsView.setTextColor(getResources().getColor(R.color.text_secondary, null));

            weeklyPlanObjectiveView.setText("Objective: " + weeklyPlan.getObjective());
            weeklyPlanObjectiveView.setTextColor(getResources().getColor(R.color.accent_purple, null));
        }
    }

    private void fetchDailySession(String dailySessionId) {
        Log.d("HomeFragment", "Fetching daily session with id: " + dailySessionId);
        studyScheduleService.getDailySessionById(dailySessionId, new StudyScheduleService.DailySessionCallback() {
            @Override
            public void onSuccess(DailySession session) {
                Log.d("HomeFragment", "Daily session fetched: " + (session != null ? session.getId() : "null"));
                if (session == null) {
                    showToast("No daily session found.");
                    return;
                }
                Log.d("HomeFragment", "Session study days: " + session.getStudyDays());
                Log.d("HomeFragment", "Session activitiesId: " + session.getActivitiesId());
                fetchActivitiesForSession(session);
            }

            @Override
            public void onError(Exception error) {
                Log.e("HomeFragment", "Failed to load daily session", error);
                showToast("Failed to load daily session: " + error.getMessage());
            }
        });
    }

    private void fetchActivitiesForSession(DailySession session) {
        activitiesByDay.clear();
        // Initialize empty lists for each study day using English day names as keys
        for (String day : session.getStudyDays()) {
            activitiesByDay.put(getEnglishDay(day), new ArrayList<>());
        }
        if (session.getActivitiesId() == null || session.getActivitiesId().isEmpty()) {
            Log.d("HomeFragment", "No activitiesId in session");
            updateActivitiesForSelectedDay();
            return;
        }
        Log.d("HomeFragment", "Fetching activities: " + session.getActivitiesId());
        // Fetch all activities, then assign to days
        final int[] remaining = { session.getActivitiesId().size() };
        for (String activityId : session.getActivitiesId()) {
            studyScheduleService.getActivityById(activityId, new StudyScheduleService.ActivityCallback() {
                @Override
                public void onSuccess(ActivityModel activity) {
                    Log.d("HomeFragment", "Fetched activity: " + (activity != null ? activity.getId() : "null"));
                    // Assign all activities to all study days (by English day name)
                    for (String day : session.getStudyDays()) {
                        List<ActivityModel> list = activitiesByDay.get(getEnglishDay(day));
                        if (list != null)
                            list.add(activity);
                    }
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        Log.d("HomeFragment", "All activities fetched, updating UI");
                        updateActivitiesForSelectedDay();
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e("HomeFragment", "Failed to fetch activity: " + activityId, error);
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        updateActivitiesForSelectedDay();
                    }
                }
            });
        }
    }

    private void updateActivitiesForSelectedDay() {
        // Update all days instead of just selected day
        calendarAdapter.notifyDataSetChanged();
    }

    private void showToast(String msg) {
        if (getContext() != null)
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // Helper to map Vietnamese day names to English
    private String getEnglishDay(String vietnameseDay) {
        switch (vietnameseDay) {
            case "Thứ 2":
                return "Monday";
            case "Thứ 3":
                return "Tuesday";
            case "Thứ 4":
                return "Wednesday";
            case "Thứ 5":
                return "Thursday";
            case "Thứ 6":
                return "Friday";
            case "Thứ 7":
                return "Saturday";
            case "Chủ nhật":
                return "Sunday";
            default:
                return vietnameseDay;
        }
    }

    // --- Calendar Adapter ---
    private static class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
        private final List<String> days;
        private final Map<String, List<ActivityModel>> activitiesByDay;

        CalendarAdapter(List<String> days, Map<String, List<ActivityModel>> activitiesByDay) {
            this.days = days;
            this.activitiesByDay = activitiesByDay;
        }

        @NonNull
        @Override
        public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
            return new CalendarViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
            String day = days.get(position);
            holder.textDayName.setText(day);

            String englishDay = getEnglishDay(day);
            List<ActivityModel> activities = activitiesByDay.get(englishDay);
            if (activities == null)
                activities = new ArrayList<>();

            holder.layoutActivities.removeAllViews();

            for (ActivityModel activity : activities) {
                View activityView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_activity_small, holder.layoutActivities, false);

                TextView textActivityName = activityView.findViewById(R.id.textActivityName);
                TextView textDuration = activityView.findViewById(R.id.textDuration);
                TextView textMethod = activityView.findViewById(R.id.textMethod);

                textActivityName.setText(activity.getName());
                textDuration.setText("⏱ " + activity.getDurationMinutes() + "p");
                textMethod.setText("Phương pháp...");

                // Add click listener to show details dialog
                activityView.setOnClickListener(v -> {
                    // Inflate custom dialog layout
                    View dialogView = LayoutInflater.from(holder.itemView.getContext())
                            .inflate(R.layout.dialog_activity_detail, null);

                    TextView titleView = dialogView.findViewById(R.id.dialogActivityTitle);
                    TextView durationView = dialogView.findViewById(R.id.dialogActivityDuration);
                    TextView descriptionView = dialogView.findViewById(R.id.dialogActivityDescription);
                    TextView techniquesView = dialogView.findViewById(R.id.dialogActivityTechniques);

                    // Title styling
                    titleView.setText(activity.getName());
                    titleView.setTypeface(null, Typeface.BOLD);
                    titleView.setTextSize(18);

                    // Duration
                    durationView.setText("Thời lượng: " + activity.getDurationMinutes() + " phút");

                    // Description
                    if (activity.getDescription() != null && !activity.getDescription().isEmpty()) {
                        descriptionView.setText("Mô tả: " + activity.getDescription());
                        descriptionView.setVisibility(View.VISIBLE);
                    } else {
                        descriptionView.setVisibility(View.GONE);
                    }

                    // Techniques
                    if (activity.getTechniques() != null && !activity.getTechniques().isEmpty()) {
                        techniquesView
                                .setText("Kỹ thuật: " + android.text.TextUtils.join(", ", activity.getTechniques()));
                        techniquesView.setVisibility(View.VISIBLE);
                    } else {
                        techniquesView.setVisibility(View.GONE);
                    }

                    AlertDialog dialog = new AlertDialog.Builder(holder.itemView.getContext())
                            .setView(dialogView)
                            .setPositiveButton("ĐÓNG", null)
                            .create();

                    // Optional: Style dialog background (rounded corners, padding)
                    if (dialog.getWindow() != null) {
                        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
                    }

                    dialog.show();
                });

                holder.layoutActivities.addView(activityView);
            }
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        private String getEnglishDay(String vietnameseDay) {
            switch (vietnameseDay) {
                case "Thứ 2":
                    return "Monday";
                case "Thứ 3":
                    return "Tuesday";
                case "Thứ 4":
                    return "Wednesday";
                case "Thứ 5":
                    return "Thursday";
                case "Thứ 6":
                    return "Friday";
                case "Thứ 7":
                    return "Saturday";
                case "Chủ nhật":
                    return "Sunday";
                default:
                    return vietnameseDay;
            }
        }

        static class CalendarViewHolder extends RecyclerView.ViewHolder {
            TextView textDayName;
            LinearLayout layoutActivities;

            CalendarViewHolder(View itemView) {
                super(itemView);
                textDayName = itemView.findViewById(R.id.textDayName);
                layoutActivities = itemView.findViewById(R.id.layoutActivities);
            }
        }
    }
}
