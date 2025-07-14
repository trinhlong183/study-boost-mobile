package com.example.miniproject.fragments;

import com.example.miniproject.R;
import com.example.miniproject.adapters.ChatAdapter;
import com.example.miniproject.adapters.ChatRoomAdapter;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.ChatRoom;
import com.example.miniproject.models.Message;
import com.example.miniproject.services.ChatService;
import com.example.miniproject.utils.Constants;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appwrite.exceptions.AppwriteException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AIFragment extends Fragment {

    private RecyclerView recyclerViewMessages;
    private RecyclerView recyclerViewChatRooms;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonMenu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabNewChat;
    private ChatAdapter chatAdapter;
    private ChatRoomAdapter chatRoomAdapter;
    private List<Message> messageList;
    private List<ChatRoom> chatRoomList;
    private ExecutorService executorService;
    private Handler mainHandler;
    private OkHttpClient httpClient = new OkHttpClient();
    private Gson gson = new Gson();
    private ChatService chatService;
    private AppwriteHelper appwriteHelper;

    // Chat room management
    private ChatRoom currentChatRoom;
    private String currentUserId = "user_001"; // TODO: Get from AppwriteHelper or session
    private String currentChatRoomTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        // Init views
        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        executorService = Executors.newCachedThreadPool();
        mainHandler = new Handler(Looper.getMainLooper());
        appwriteHelper = AppwriteHelper.getInstance(requireContext());
        chatService = new ChatService(requireContext());

        // Setup chat room list
        chatRoomList = new ArrayList<>();
        setupChatRoomRecyclerView(view);

        // Fetch chat rooms and create new chat room
        fetchCurrentUserAndInitChat();

        return view;
    }

    private void initViews(View view) {
        drawerLayout = view.findViewById(R.id.drawer_layout);
        navigationView = view.findViewById(R.id.navigation_view);
        recyclerViewMessages = view.findViewById(R.id.recyclerView_messages);
        recyclerViewChatRooms = view.findViewById(R.id.recyclerView_chat_rooms);
        editTextMessage = view.findViewById(R.id.editText_message);
        buttonSend = view.findViewById(R.id.button_send);
        buttonMenu = view.findViewById(R.id.button_menu);
        fabNewChat = view.findViewById(R.id.fab_new_chat);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Start from bottom

        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(chatAdapter);
    }

    private void setupChatRoomRecyclerView(View view) {
        chatRoomAdapter = new ChatRoomAdapter(chatRoomList, chatRoom -> {
            selectChatRoom(chatRoom);
        });
        recyclerViewChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChatRooms.setAdapter(chatRoomAdapter);
    }

    private void setupClickListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
        buttonMenu.setOnClickListener(v -> {
            fetchChatRoomsOnMenuOpen();
            drawerLayout.openDrawer(navigationView);
        });
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
        fabNewChat.setOnClickListener(v -> createNewChatRoomAndSwitch());
    }

    private void fetchChatRoomsOnMenuOpen() {
        if (currentUserId == null) return;
        try {
            chatService.getUserChatRooms(currentUserId, new ChatService.ChatCallback<List<ChatRoom>>() {
                @Override
                public void onSuccess(List<ChatRoom> chatRooms) {
                    chatRoomList.clear();
                    chatRoomList.addAll(chatRooms);
                    chatRoomAdapter.notifyDataSetChanged();
                }
                @Override
                public void onError(Exception error) {
                    Toast.makeText(getContext(), "Không thể tải danh sách đoạn chat", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể tải danh sách đoạn chat", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCurrentUserAndInitChat() {
        appwriteHelper.getCurrentUser(new AppwriteHelper.AuthCallback<io.appwrite.models.User<java.util.Map<String, Object>>>() {
            @Override
            public void onSuccess(io.appwrite.models.User<java.util.Map<String, Object>> user) {
                currentUserId = user.getId();
                fetchChatRoomsAndCreateNew();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(getContext(), "Error: Please login first", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchChatRoomsAndCreateNew() {
        try {
            chatService.getUserChatRooms(currentUserId, new ChatService.ChatCallback<List<ChatRoom>>() {
                @Override
                public void onSuccess(List<ChatRoom> chatRooms) {
                    chatRoomList.clear();
                    chatRoomList.addAll(chatRooms);
                    chatRoomAdapter.notifyDataSetChanged();
                    createNewChatRoom();
                }

                @Override
                public void onError(Exception error) {
                    createNewChatRoom();
                }
            });
        } catch (Exception e) {
            createNewChatRoom();
        }
    }

    private void createNewChatRoom() {
        String chatTitle = "Chat " + new Date().toString().substring(11, 16);
        currentChatRoomTitle = chatTitle;
        try {
            chatService.createChatRoom(currentUserId, chatTitle, new ChatService.ChatCallback<String>() {
                @Override
                public void onSuccess(String chatRoomId) {
                    currentChatRoom = new ChatRoom(currentUserId, chatTitle);
                    currentChatRoom.setId(chatRoomId);
                    addWelcomeMessage();
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(getContext(), "Failed to create chat room", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to create chat room", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewChatRoomAndSwitch() {
        String chatTitle = "Chat " + new Date().toString().substring(11, 16);
        currentChatRoomTitle = chatTitle;
        try {
            chatService.createChatRoom(currentUserId, chatTitle, new ChatService.ChatCallback<String>() {
                @Override
                public void onSuccess(String chatRoomId) {
                    currentChatRoom = new ChatRoom(currentUserId, chatTitle);
                    currentChatRoom.setId(chatRoomId);
                    // Add to chat room list and update adapter
                    chatRoomList.add(0, currentChatRoom);
                    chatRoomAdapter.notifyDataSetChanged();
                    // Switch to new chat room
                    messageList.clear();
                    chatAdapter.notifyDataSetChanged();
                    addWelcomeMessage();
                    drawerLayout.closeDrawer(navigationView);
                }
                @Override
                public void onError(Exception error) {
                    Toast.makeText(getContext(), "Failed to create chat room", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to create chat room", Toast.LENGTH_SHORT).show();
        }
    }

    private void addWelcomeMessage() {
        Message welcomeMessage = new Message(
                "Xin chào! Tôi là trợ lý AI của bạn. Hãy hỏi tôi bất cứ điều gì về học tập nhé! 😊",
                false
        );
        welcomeMessage.setChatRoomId(currentChatRoom.getId());
        chatAdapter.addMessage(welcomeMessage);
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Add user message to chat
        Message userMessage = new Message(messageText, true);
        userMessage.setChatRoomId(currentChatRoom.getId());
        chatAdapter.addMessage(userMessage);
        scrollToBottom();

        // Clear input
        editTextMessage.setText("");

        // Disable send button temporarily
        buttonSend.setEnabled(false);

        // Send message to AI
        sendMessageToAI(userMessage);
    }

    private void sendMessageToAI(Message userMessage) {
        executorService.execute(() -> {
            try {
                // Prepare JSON body
                JsonObject requestData = new JsonObject();
                requestData.addProperty("message_content", userMessage.getMessageContent());
                requestData.addProperty("user_id", currentUserId);
                requestData.addProperty("chat_room_id", currentChatRoom.getId());
                requestData.addProperty("is_from_user", true);
                String jsonBody = gson.toJson(requestData);

                RequestBody body = RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        jsonBody
                );
                Request request = new Request.Builder()
                        .url(Constants.API.N8N_WEBHOOK_URL)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                        String aiResponse = responseJson.has("message_content") ? responseJson.get("message_content").getAsString() : "AI không trả lời";
                        mainHandler.post(() -> {
                            Message aiMessage = new Message(
                                    currentChatRoom.getId(),
                                    aiResponse,
                                    false,
                                    new Date()
                            );
                            chatAdapter.addMessage(aiMessage);
                            scrollToBottom();
                            buttonSend.setEnabled(true);
                        });
                    } else {
                        mainHandler.post(() -> {
                            Toast.makeText(getContext(), "AI service temporarily unavailable", Toast.LENGTH_SHORT).show();
                            buttonSend.setEnabled(true);
                        });
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), "Có lỗi xảy ra khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                    buttonSend.setEnabled(true);
                });
            }
        });
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            recyclerViewMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private void selectChatRoom(ChatRoom chatRoom) throws AppwriteException {
        currentChatRoom = chatRoom;
        currentChatRoomTitle = chatRoom.getTitle();
        drawerLayout.closeDrawer(navigationView);
        // Load message history for selected chat room
        messageList.clear();
        chatAdapter.notifyDataSetChanged();
        buttonSend.setEnabled(false);
        chatService.getChatMessages(chatRoom.getId(), new ChatService.ChatCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                messageList.clear();
                messageList.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                scrollToBottom();
                buttonSend.setEnabled(true);
            }
            @Override
            public void onError(Exception error) {
                Toast.makeText(getContext(), "Không thể tải lịch sử đoạn chat", Toast.LENGTH_SHORT).show();
                buttonSend.setEnabled(true);
            }
        });
    }

    // Getter methods for external access
    public ChatRoom getCurrentChatRoom() {
        return currentChatRoom;
    }

    public List<Message> getMessageHistory() {
        return new ArrayList<>(messageList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
