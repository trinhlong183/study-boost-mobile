package com.example.miniproject.services;

import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.ChatRoom;
import com.example.miniproject.models.Message;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.Instant;

import io.appwrite.ID;
import io.appwrite.Query;
import io.appwrite.coroutines.Callback;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.models.DocumentList;
import io.appwrite.services.Databases;

public class ChatService {
    private static final String TAG = "ChatService";
    private AppwriteHelper appwriteHelper;
    private Databases databases;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ChatService(Context context) {
        appwriteHelper = AppwriteHelper.getInstance(context);
        databases = appwriteHelper.getDatabases();
    }

    public interface ChatCallback<T> {
        void onSuccess(T result) throws AppwriteException;
        void onError(Exception error);
    }

    // Create new chat room
    public void createChatRoom(String userId, String title, ChatCallback<String> callback) throws AppwriteException {
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("title", title);

        databases.createDocument(
            AppwriteHelper.DATABASE_ID,
            AppwriteHelper.CHAT_ROOMS_COLLECTION_ID,
                ID.Companion.unique(7),
            data,
            new CoroutineCallback<>(new Callback<Document<Map<String, Object>>>() {
                @Override
                public void onComplete(Document<Map<String, Object>> result, Throwable error) {
                    mainHandler.post(() -> {
                        if (error != null) {
                            Log.e(TAG, "Error creating chat room", error);
                            callback.onError(new Exception(error.getMessage()));
                        } else {
                            Log.d(TAG, "Chat room created: " + result.getId());
                            try {
                                callback.onSuccess(result.getId());
                            } catch (AppwriteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            })
        );
    }

    // Send message to chat room
    public void sendMessage(String userId, String messageContent, String chatRoomId, boolean isFromUser, ChatCallback<String> callback) throws AppwriteException {
        Log.d(TAG, "Sending message to chat room: " + chatRoomId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("chat_room_id", chatRoomId);
        data.put("message_content", messageContent);
        data.put("is_from_user", isFromUser);
        
        // Store timestamp in ISO format for consistency
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data.put("sent_at", Instant.now().toString());
        } else {
            // Fallback for older Android versions
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            data.put("sent_at", isoFormat.format(new Date()));
        }

        Log.d(TAG, "Message data: " + data.toString());

        try {
            databases.createDocument(
                AppwriteHelper.DATABASE_ID,
                AppwriteHelper.CHAT_MESSAGES_COLLECTION_ID,
                    ID.Companion.unique(7),
                data,
                new CoroutineCallback<>(new Callback<Document<Map<String, Object>>>() {
                    @Override
                    public void onComplete(Document<Map<String, Object>> result, Throwable error) {
                        mainHandler.post(() -> {
                            if (error != null) {
                                Log.e(TAG, "Error sending message", error);
                                callback.onError(new Exception(error.getMessage()));
                            } else {
                                Log.d(TAG, "Message sent: " + result.getId());
                                Log.d(TAG, "Sent message data: " + result.getData().toString());

                                // TODO: Call n8n webhook here if needed
                                // Make HTTP request to n8n endpoint

                                try {
                                    callback.onSuccess(messageContent); // Return the message content for now
                                } catch (AppwriteException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                })
            );
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }

    // Get messages for a chat room (with alternative query methods)
    public void getChatMessages(String chatRoomId, ChatCallback<List<Message>> callback) throws AppwriteException {
        Log.d(TAG, "============ GET CHAT MESSAGES DEBUG ============");
        Log.d(TAG, "Getting messages for chat room: " + chatRoomId);
        Log.d(TAG, "DATABASE_ID: " + AppwriteHelper.DATABASE_ID);
        Log.d(TAG, "CHAT_MESSAGES_COLLECTION_ID: " + AppwriteHelper.CHAT_MESSAGES_COLLECTION_ID);
        
        // Try different query approaches
        List<String> queries = new ArrayList<>();
        
        // Method 1: Direct equal query
        try {
            queries.add(Query.Companion.equal("chat_room_id", chatRoomId));
            Log.d(TAG, "Using equal query for chat_room_id: " + chatRoomId);
        } catch (Exception e) {
            Log.e(TAG, "Error creating equal query", e);
            // Try alternative
            queries.clear();
            queries.add("chat_room_id=" + chatRoomId); // Manual query string
        }
        
        // Add ordering - but make it optional in case it causes issues
        try {
            queries.add(Query.Companion.orderAsc("sent_at"));
        } catch (Exception e) {
            Log.w(TAG, "Could not add ordering, continuing without it", e);
        }

        Log.d(TAG, "Final queries: " + queries.toString());
        Log.d(TAG, "About to call databases.listDocuments...");

        databases.listDocuments(
            AppwriteHelper.DATABASE_ID,
            AppwriteHelper.CHAT_MESSAGES_COLLECTION_ID,
            queries,
            new CoroutineCallback<>(new Callback<DocumentList<Map<String, Object>>>() {
                @Override
                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                    Log.d(TAG, "★★★ listDocuments onComplete called ★★★");
                    mainHandler.post(() -> {
                        if (error != null) {
                            Log.e(TAG, "★★★ ERROR in listDocuments ★★★");
                            Log.e(TAG, "Error type: " + error.getClass().getSimpleName());
                            Log.e(TAG, "Error message: " + error.getMessage());
                            if (error.getCause() != null) {
                                Log.e(TAG, "Error cause: " + error.getCause().getMessage());
                            }
                            error.printStackTrace();
                            Log.e(TAG, "Error getting messages for chat room " + chatRoomId, error);
                            
                            // Fallback: try to get all messages and filter manually
                            Log.d(TAG, "Fallback: trying to get all messages and filter manually");
                            try {
                                getAllMessagesAndFilter(chatRoomId, callback);
                            } catch (AppwriteException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Log.d(TAG, "★★★ SUCCESS in listDocuments ★★★");
                            Log.d(TAG, "Total documents returned: " + result.getTotal());
                            Log.d(TAG, "Documents in this page: " + result.getDocuments().size());
                            Log.d(TAG, "Found " + result.getDocuments().size() + " messages for chat room " + chatRoomId);
                            
                            List<Message> messages = new ArrayList<>();
                            for (int i = 0; i < result.getDocuments().size(); i++) {
                                Document<Map<String, Object>> doc = result.getDocuments().get(i);
                                try {
                                    Log.d(TAG, "Processing document " + i + ": " + doc.getId());
                                    Log.d(TAG, "Document data: " + doc.getData().toString());
                                    Message message = documentToMessage(doc);
                                    messages.add(message);
                                    Log.d(TAG, "Converted message " + i + ": " + message.getMessageContent());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document " + i + " to message", e);
                                    e.printStackTrace();
                                }
                            }
                            Log.d(TAG, "Total messages converted: " + messages.size());
                            Log.d(TAG, "About to call callback.onSuccess with " + messages.size() + " messages");
                            try {
                                callback.onSuccess(messages);
                            } catch (AppwriteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            })
        );
    }
    
    // Fallback method: get all messages and filter manually
    private void getAllMessagesAndFilter(String chatRoomId, ChatCallback<List<Message>> callback) throws AppwriteException {
        Log.d(TAG, "============ FALLBACK METHOD DEBUG ============");
        Log.d(TAG, "Getting ALL messages and filtering for chat room: " + chatRoomId);
        
        databases.listDocuments(
            AppwriteHelper.DATABASE_ID,
            AppwriteHelper.CHAT_MESSAGES_COLLECTION_ID,
            new ArrayList<>(), // No queries
            new CoroutineCallback<>(new Callback<DocumentList<Map<String, Object>>>() {
                @Override
                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                    Log.d(TAG, "★★★ Fallback onComplete called ★★★");
                    mainHandler.post(() -> {
                        if (error != null) {
                            Log.e(TAG, "★★★ ERROR in fallback method ★★★");
                            Log.e(TAG, "Fallback error: " + error.getMessage());
                            error.printStackTrace();
                            callback.onError(new Exception(error.getMessage()));
                        } else {
                            Log.d(TAG, "★★★ SUCCESS in fallback method ★★★");
                            Log.d(TAG, "Fallback: Found " + result.getDocuments().size() + " total messages, filtering for chat room " + chatRoomId);
                            
                            List<Message> messages = new ArrayList<>();
                            int matchCount = 0;
                            for (int i = 0; i < result.getDocuments().size(); i++) {
                                Document<Map<String, Object>> doc = result.getDocuments().get(i);
                                try {
                                    Map<String, Object> data = doc.getData();
                                    String docChatRoomId = (String) data.get("chat_room_id");
                                    Log.d(TAG, "Document " + i + " chat_room_id: '" + docChatRoomId + "', looking for: '" + chatRoomId + "'");
                                    
                                    if (chatRoomId.equals(docChatRoomId)) {
                                        matchCount++;
                                        Message message = documentToMessage(doc);
                                        messages.add(message);
                                        Log.d(TAG, "★ MATCH " + matchCount + ": " + message.getMessageContent());
                                    } else {
                                        Log.d(TAG, "No match for document " + i);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error in fallback filtering for document " + i, e);
                                    e.printStackTrace();
                                }
                            }
                            Log.d(TAG, "Fallback: Found " + messages.size() + " matching messages out of " + result.getDocuments().size() + " total");
                            try {
                                callback.onSuccess(messages);
                            } catch (AppwriteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            })
        );
    }

    // Get user's chat rooms
    public void getUserChatRooms(String userId, ChatCallback<List<ChatRoom>> callback) throws AppwriteException {
        List<String> queries = new ArrayList<>();
        queries.add(Query.Companion.equal("user_id", userId));
        queries.add(Query.Companion.orderDesc("$createdAt"));

        databases.listDocuments(
            AppwriteHelper.DATABASE_ID,
            AppwriteHelper.CHAT_ROOMS_COLLECTION_ID,
            queries,
            new CoroutineCallback<>(new Callback<DocumentList<Map<String, Object>>>() {
                @Override
                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                    mainHandler.post(() -> {
                        if (error != null) {
                            Log.e(TAG, "Error getting chat rooms", error);
                            callback.onError(new Exception(error.getMessage()));
                        } else {
                            List<ChatRoom> chatRooms = new ArrayList<>();
                            for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                try {
                                    ChatRoom chatRoom = documentToChatRoom(doc);
                                    chatRooms.add(chatRoom);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to chat room", e);
                                }
                            }
                            try {
                                callback.onSuccess(chatRooms);
                            } catch (AppwriteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            })
        );
    }

    // Debug method: Get all messages (no filter)
    public void getAllMessages(ChatCallback<List<Message>> callback) throws AppwriteException {
        Log.d(TAG, "Getting ALL messages for debugging");

        databases.listDocuments(
            AppwriteHelper.DATABASE_ID,
            AppwriteHelper.CHAT_MESSAGES_COLLECTION_ID,
            new ArrayList<>(), // No queries = get all
            new CoroutineCallback<>(new Callback<DocumentList<Map<String, Object>>>() {
                @Override
                public void onComplete(DocumentList<Map<String, Object>> result, Throwable error) {
                    mainHandler.post(() -> {
                        if (error != null) {
                            Log.e(TAG, "Error getting all messages", error);
                            callback.onError(new Exception(error.getMessage()));
                        } else {
                            Log.d(TAG, "Found " + result.getDocuments().size() + " total messages");
                            List<Message> messages = new ArrayList<>();
                            for (Document<Map<String, Object>> doc : result.getDocuments()) {
                                try {
                                    Log.d(TAG, "Document ID: " + doc.getId());
                                    Log.d(TAG, "Document data: " + doc.getData().toString());
                                    Message message = documentToMessage(doc);
                                    messages.add(message);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document to message", e);
                                }
                            }
                            try {
                                callback.onSuccess(messages);
                            } catch (AppwriteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            })
        );
    }

    // Helper method to convert Appwrite document to Message
    private Message documentToMessage(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        
        String chatRoomId = (String) data.get("chat_room_id");
        String messageContent = (String) data.get("message_content");
        Boolean isFromUser = (Boolean) data.get("is_from_user");
        String sentAtStr = (String) data.get("sent_at");
        
        // Parse date safely
        Date sentAt = parseAppwriteDate(sentAtStr);
        
        return new Message(
            chatRoomId != null ? chatRoomId : "",
            messageContent != null ? messageContent : "",
            isFromUser != null ? isFromUser : false,
            sentAt
        );
    }

    // Helper method to convert Appwrite document to ChatRoom
    private ChatRoom documentToChatRoom(Document<Map<String, Object>> doc) {
        Map<String, Object> data = doc.getData();
        
        String id = doc.getId();
        String userId = (String) data.get("user_id");
        String title = (String) data.get("title");
        
        // Parse dates safely from Appwrite timestamps
        Date createdAt = parseAppwriteDate(doc.getCreatedAt());
        Date updatedAt = parseAppwriteDate(doc.getUpdatedAt());

        return new ChatRoom(
            id,
            userId != null ? userId : "",
            title != null ? title : "",
            createdAt,
            updatedAt
        );
    }
    
    // Helper method to safely parse Appwrite date strings
    private Date parseAppwriteDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return new Date(); // Return current date as fallback
        }
        
        try {
            // Try parsing as ISO 8601 format first (most common for Appwrite)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Date.from(Instant.parse(dateString));
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse date as ISO 8601: " + dateString, e);
        }
        
        // Try different date formats
        String[] dateFormats = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        };
        
        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setLenient(false);
                return sdf.parse(dateString);
            } catch (Exception e) {
                // Continue to next format
            }
        }
        
        Log.w(TAG, "Unable to parse date string: " + dateString + ", using current date");
        return new Date(); // Fallback to current date
    }
}

