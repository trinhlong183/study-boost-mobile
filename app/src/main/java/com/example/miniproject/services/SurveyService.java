package com.example.miniproject.services;

import android.content.Context;
import android.util.Log;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.SurveyQuestion;
import io.appwrite.Query;
import io.appwrite.coroutines.Callback;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SurveyService {
    private static final String TAG = "SurveyService";
    private AppwriteHelper appwriteHelper;

    public SurveyService(Context context) {
        appwriteHelper = AppwriteHelper.getInstance(context);
    }

    public interface SurveyCallback {
        void onSuccess(List<SurveyQuestion> questions);

        void onError(Exception error);
    }

    public void getSurveyQuestions(SurveyCallback callback) {
        try {
            appwriteHelper.getDatabases().listDocuments(
                    com.example.miniproject.utils.Constants.Appwrite.DATABASE_ID,
                    com.example.miniproject.utils.Constants.Appwrite.SURVEY_QUESTIONS_COLLECTION_ID,
                    new ArrayList<>(),
                    new CoroutineCallback<DocumentList<Map<String, Object>>>(
                            new Callback<DocumentList<Map<String, Object>>>() {
                                @Override
                                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                                    if (error != null) {
                                        Log.e(TAG, "Error fetching survey questions", error);
                                        // Create fallback questions
                                        List<SurveyQuestion> fallbackQuestions = createFallbackQuestions();
                                        callback.onSuccess(fallbackQuestions);
                                    } else if (result != null) {
                                        List<SurveyQuestion> questions = new ArrayList<>();
                                        for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                            SurveyQuestion question = documentToSurveyQuestion(doc);
                                            if (question != null) {
                                                questions.add(question);
                                            }
                                        }

                                        if (questions.isEmpty()) {
                                            questions = createFallbackQuestions();
                                        }

                                        Log.d(TAG, "Successfully fetched " + questions.size() + " survey questions");
                                        callback.onSuccess(questions);
                                    }
                                }
                            }));
        } catch (AppwriteException e) {
            Log.e(TAG, "AppwriteException when fetching survey questions", e);
            List<SurveyQuestion> fallbackQuestions = createFallbackQuestions();
            callback.onSuccess(fallbackQuestions);
        }
    }

    private SurveyQuestion documentToSurveyQuestion(Document<Map<String, Object>> doc) {
        try {
            Map<String, Object> data = doc.getData();

            // Extract basic info
            Object idObj = data.get("question_no");
            int id = idObj != null ? Integer.parseInt(idObj.toString()) : 1;

            String questionText = (String) data.get("question_text");
            if (questionText == null || questionText.isEmpty()) {
                return null;
            }

            // Extract options
            Object optionsObj = data.get("options");
            String[] options = null;
            if (optionsObj instanceof List) {
                List<?> optionsList = (List<?>) optionsObj;
                options = new String[optionsList.size()];
                for (int i = 0; i < optionsList.size(); i++) {
                    options[i] = optionsList.get(i).toString();
                }
            }

            return new SurveyQuestion(id, questionText, options);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing survey question document", e);
            return null;
        }
    }

    private List<SurveyQuestion> createFallbackQuestions() {
        List<SurveyQuestion> questions = new ArrayList<>();

        questions.add(new SurveyQuestion(1,
                "Trong 1 tháng tới, mục tiêu học tập chính của bạn là gì?",
                new String[] {
                        "Cải thiện điểm của một môn học cụ thể",
                        "Nắm vững một chủ đề mới",
                        "Ôn tập cho kỳ thi",
                        "Hoàn thành một dự án học tập",
                        "Nghiên cứu chuyên sâu một lĩnh vực"
                }));

        questions.add(new SurveyQuestion(2,
                "Thời gian học tập mỗi ngày của bạn thường là bao lâu?",
                new String[] {
                        "Dưới 1 giờ",
                        "1-2 giờ",
                        "2-4 giờ",
                        "4-6 giờ",
                        "Trên 6 giờ"
                }));

        questions.add(new SurveyQuestion(3,
                "Môi trường học tập yêu thích của bạn là gì?",
                new String[] {
                        "Tại nhà, yên tĩnh",
                        "Thư viện",
                        "Quán cà phê",
                        "Phòng học nhóm",
                        "Ngoài trời"
                }));

        questions.add(new SurveyQuestion(4,
                "Phương pháp học tập hiệu quả nhất với bạn là?",
                new String[] {
                        "Đọc sách và ghi chú",
                        "Xem video giảng dạy",
                        "Thực hành và làm bài tập",
                        "Thảo luận nhóm",
                        "Tự nghiên cứu"
                }));

        questions.add(new SurveyQuestion(5,
                "Khi gặp khó khăn trong học tập, bạn thường làm gì?",
                new String[] {
                        "Tìm kiếm trên internet",
                        "Hỏi giáo viên/bạn bè",
                        "Đọc thêm tài liệu",
                        "Nghỉ ngơi rồi quay lại",
                        "Bỏ qua phần khó"
                }));

        Log.d(TAG, "Created " + questions.size() + " fallback survey questions");
        return questions;
    }
}