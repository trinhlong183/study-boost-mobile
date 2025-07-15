package com.example.miniproject.services;

import android.content.Context;
import android.util.Log;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.Milestone;
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

public class MilestoneService {
    private static final String TAG = "MilestoneService";
    private AppwriteHelper appwriteHelper;

    public MilestoneService(Context context) {
        appwriteHelper = AppwriteHelper.getInstance(context);
    }

    public interface MilestoneCallback {
        void onSuccess(List<Milestone> milestones);

        void onError(Exception error);
    }

    public void getMilestonesByIds(List<String> milestoneIds, MilestoneCallback callback) {
        if (milestoneIds == null || milestoneIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        try {
            appwriteHelper.getDatabases().listDocuments(
                    com.example.miniproject.utils.Constants.Appwrite.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.SURVEY_RESPONSES__MILESTONES_ID,
                    java.util.Collections.singletonList(Query.Companion.equal("$id", milestoneIds)),
                    new CoroutineCallback<DocumentList<Map<String, Object>>>(
                            new Callback<DocumentList<Map<String, Object>>>() {
                                @Override
                                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                                    if (error != null) {
                                        Log.e(TAG, "Error fetching milestones", error);
                                        // Create fallback data
                                        List<Milestone> fallbackMilestones = createFallbackMilestones();
                                        callback.onSuccess(fallbackMilestones);
                                    } else if (result != null) {
                                        List<Milestone> milestones = new ArrayList<>();
                                        for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                            Milestone milestone = documentToMilestone(doc);
                                            milestones.add(milestone);
                                        }

                                        if (milestones.isEmpty()) {
                                            milestones = createFallbackMilestones();
                                        }

                                        // Sort by milestone id
                                        Collections.sort(milestones, new Comparator<Milestone>() {
                                            @Override
                                            public int compare(Milestone m1, Milestone m2) {
                                                return Integer.compare(m1.id, m2.id);
                                            }
                                        });

                                        Log.d(TAG, "Successfully fetched " + milestones.size() + " milestones");
                                        callback.onSuccess(milestones);
                                    }
                                }
                            }));
        } catch (AppwriteException e) {
            Log.e(TAG, "AppwriteException when fetching milestones", e);
            List<Milestone> fallbackMilestones = createFallbackMilestones();
            callback.onSuccess(fallbackMilestones);
        }
    }

    private Milestone documentToMilestone(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();

        Milestone milestone = new Milestone();
        milestone.documentId = doc.getId();

        Object idObj = data.get("id");
        milestone.id = idObj != null ? Integer.parseInt(idObj.toString()) : 0;

        milestone.description = (String) data.get("description");
        milestone.target_completion = (String) data.get("target_completion");
        milestone.study_schedule_id = (String) data.get("study_schedule_id");

        return milestone;
    }

    private List<Milestone> createFallbackMilestones() {
        List<Milestone> fallbackMilestones = new ArrayList<>();

        Milestone milestone1 = new Milestone();
        milestone1.documentId = "fallback_milestone_1";
        milestone1.id = 1;
        milestone1.description = "Viết và chạy thành chương trình thành công";
        milestone1.target_completion = "Kết thúc Tuần 1";

        Milestone milestone2 = new Milestone();
        milestone2.documentId = "fallback_milestone_2";
        milestone2.id = 2;
        milestone2.description = "Xây dựng được một ứng dụng";
        milestone2.target_completion = "Kết thúc Tuần 2";

        Milestone milestone3 = new Milestone();
        milestone3.documentId = "fallback_milestone_3";
        milestone3.id = 3;
        milestone3.description = "Tạo một chương trình quản lý";
        milestone3.target_completion = "Kết thúc Tuần 3";

        Milestone milestone4 = new Milestone();
        milestone4.documentId = "fallback_milestone_4";
        milestone4.id = 4;
        milestone4.description = "Viết một hàm tự định nghĩa";
        milestone4.target_completion = "Kết thúc Tuần 4";

        fallbackMilestones.add(milestone1);
        fallbackMilestones.add(milestone2);
        fallbackMilestones.add(milestone3);
        fallbackMilestones.add(milestone4);

        Log.d(TAG, "Created " + fallbackMilestones.size() + " fallback milestones");
        return fallbackMilestones;
    }
}
