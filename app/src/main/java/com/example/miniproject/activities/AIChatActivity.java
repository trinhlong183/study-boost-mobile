package com.example.miniproject.activities;

import com.example.miniproject.R;
import com.example.miniproject.adapters.AIChatAdapter;
import com.example.miniproject.adapters.ChatRoomAdapter;
import com.example.miniproject.api.AppwriteHelper;
import com.example.miniproject.models.ChatRoom;
import com.example.miniproject.models.Message;
import com.example.miniproject.services.ChatService;
import com.example.miniproject.services.HttpService;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.User;

public class AIChatActivity extends AppCompatActivity {
    private static final String TAG = "AIChatActivity";
    
    // UI Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerViewMessages;
    private RecyclerView recyclerViewChatRooms;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ImageButton buttonMenu;
    private TextView textViewChatTitle;
    private TextView textViewWelcome;
    private LinearLayout layoutWelcome;
    private LinearLayout layoutChatHeader;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabNewChat;
    private ProgressBar progressBarLoading;
    
    // Adapters
    private AIChatAdapter chatAdapter;
    private ChatRoomAdapter chatRoomAdapter;
    
    // Data
    private List<Message> messageList;
    private List<ChatRoom> chatRoomList;
    private String currentChatRoomId;
    private String currentUserId;
    private String currentChatRoomTitle;
    
    // Services
    private AppwriteHelper appwriteHelper;
    private ChatService chatService;
    private HttpService httpService;
    
    // Handler for main thread updates
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ai);
        
        initViews();
        initServices();
        setupRecyclerViews();
        setupClickListeners();
        setupSwipeRefresh();
        
        // Fetch current user and chat rooms
        fetchCurrentUser();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        recyclerViewMessages = findViewById(R.id.recyclerView_messages);
        recyclerViewChatRooms = findViewById(R.id.recyclerView_chat_rooms);
        editTextMessage = findViewById(R.id.editText_message);
        buttonSend = findViewById(R.id.button_send);
        buttonMenu = findViewById(R.id.button_menu);
        textViewChatTitle = findViewById(R.id.textView_chat_title);
        textViewWelcome = findViewById(R.id.textView_welcome);
        layoutWelcome = findViewById(R.id.layout_welcome);
        layoutChatHeader = findViewById(R.id.layout_chat_header);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        fabNewChat = findViewById(R.id.fab_new_chat);
        progressBarLoading = findViewById(R.id.progress_loading);
    }

    private void initServices() {
        appwriteHelper = AppwriteHelper.getInstance(this);
        chatService = new ChatService(this);
        httpService = new HttpService();
    }

    private void setupRecyclerViews() {
        // Chat messages RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new AIChatAdapter(messageList);
        
        LinearLayoutManager messagesLayoutManager = new LinearLayoutManager(this);
        messagesLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(messagesLayoutManager);
        recyclerViewMessages.setAdapter(chatAdapter);
        
        // Chat rooms RecyclerView in drawer
        chatRoomList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(chatRoomList, this::selectChatRoom);
        
        LinearLayoutManager chatRoomsLayoutManager = new LinearLayoutManager(this);
        recyclerViewChatRooms.setLayoutManager(chatRoomsLayoutManager);
        recyclerViewChatRooms.setAdapter(chatRoomAdapter);
    }

    private void setupClickListeners() {
        buttonMenu.setOnClickListener(v -> {
            Log.d(TAG, "Menu button clicked, opening drawer");
            Log.d(TAG, "Current chat rooms in list: " + chatRoomList.size());
            drawerLayout.openDrawer(navigationView);
        });
        
        buttonSend.setOnClickListener(v -> sendMessage());
        
        fabNewChat.setOnClickListener(v -> {
            try {
                createNewChatRoom();
            } catch (AppwriteException e) {
                throw new RuntimeException(e);
            }
        });
        
        // Enable/disable send button based on input
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setEnabled(s.toString().trim().length() > 0 && currentChatRoomId != null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentChatRoomId != null) {
                try {
                    refreshMessages();
                } catch (AppwriteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void fetchCurrentUser() {
        showLoading(true);
        appwriteHelper.getCurrentUser(new AppwriteHelper.AuthCallback<User<Map<String, Object>>>() {
            @Override
            public void onSuccess(User<Map<String, Object>> user) throws AppwriteException {
                currentUserId = user.getId();
                Log.d(TAG, "Current user ID: " + currentUserId);
                fetchChatRooms();
            }

            @Override
            public void onError(Exception error) {
                showLoading(false);
                Log.e(TAG, "Error fetching current user", error);
                Toast.makeText(AIChatActivity.this, "Error: Please login first", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void fetchChatRooms() throws AppwriteException {
        Log.d(TAG, "Fetching chat rooms for user: " + currentUserId);
        
        chatService.getUserChatRooms(currentUserId, new ChatService.ChatCallback<List<ChatRoom>>() {
            @Override
            public void onSuccess(List<ChatRoom> chatRooms) {
                showLoading(false);
                Log.d(TAG, "Successfully fetched " + chatRooms.size() + " chat rooms");
                
                for (int i = 0; i < chatRooms.size(); i++) {
                    ChatRoom room = chatRooms.get(i);
                    Log.d(TAG, "Chat Room " + i + ": ID=" + room.getId() + ", Title=" + room.getTitle());
                }
                
                chatRoomList.clear();
                chatRoomList.addAll(chatRooms);
                chatRoomAdapter.notifyDataSetChanged();
                
                Log.d(TAG, "Updated adapter with " + chatRoomList.size() + " chat rooms");
                
                // Show welcome screen if no chat rooms
                if (chatRooms.isEmpty()) {
                    Log.d(TAG, "No chat rooms found, showing welcome screen");
                    showWelcomeScreen();
                } else {
                    Log.d(TAG, "Chat rooms available, sidebar will show them");
                }
            }

            @Override
            public void onError(Exception error) {
                showLoading(false);
                Log.e(TAG, "Error fetching chat rooms", error);
                showWelcomeScreen();
            }
        });
    }

    private void createNewChatRoom() throws AppwriteException {
        if (currentUserId == null) {
            Toast.makeText(this, "User not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Generate chat room title
        int chatCount = chatRoomList.size();
        String newTitle = chatCount == 0 ? "Đoạn chat mới" : "Đoạn chat mới (" + (chatCount + 1) + ")";

        try {
            chatService.createChatRoom(currentUserId, newTitle, new ChatService.ChatCallback<String>() {
                @Override
                public void onSuccess(String chatRoomId) throws AppwriteException {
                    currentChatRoomId = chatRoomId;
                    currentChatRoomTitle = newTitle;

                    // Clear messages and update UI
                    messageList.clear();
                    chatAdapter.notifyDataSetChanged();

                    // Refresh chat rooms list
                    fetchChatRooms();

                    // Update UI
                    showChatInterface();
                    drawerLayout.closeDrawer(navigationView);

                    showLoading(false);
                    Toast.makeText(AIChatActivity.this, "New chat created", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception error) {
                    showLoading(false);
                    Log.e(TAG, "Error creating chat room", error);
                    Toast.makeText(AIChatActivity.this, "Failed to create chat room", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (AppwriteException e) {
            throw new RuntimeException(e);
        }
    }

    private void selectChatRoom(ChatRoom chatRoom) {
        Log.d(TAG, "============ CHAT ROOM SELECTION DEBUG ============");
        Log.d(TAG, "Selecting chat room: " + chatRoom.getId() + " - " + chatRoom.getTitle());
        Log.d(TAG, "Chat room object: " + chatRoom.toString());
        Log.d(TAG, "Previous currentChatRoomId: " + currentChatRoomId);
        
        currentChatRoomId = chatRoom.getId();
        currentChatRoomTitle = chatRoom.getTitle();
        
        Log.d(TAG, "New currentChatRoomId: " + currentChatRoomId);
        Log.d(TAG, "New currentChatRoomTitle: " + currentChatRoomTitle);
        
        // Debug the selection
        debugChatRoomSelection(currentChatRoomId);
        
        showLoading(true);
        drawerLayout.closeDrawer(navigationView);
        
        // Update selected chat room in adapter
        chatRoomAdapter.setSelectedChatRoom(currentChatRoomId);
        
        // Fetch messages for this chat room
        Log.d(TAG, "About to call getChatMessages for chat room: " + currentChatRoomId);
        try {
            chatService.getChatMessages(currentChatRoomId, new ChatService.ChatCallback<List<Message>>() {
                @Override
                public void onSuccess(List<Message> messages) {
                    Log.d(TAG, "★★★ getChatMessages SUCCESS ★★★");
                    Log.d(TAG, "Messages received: " + messages.size());
                    for (int i = 0; i < messages.size(); i++) {
                        Message msg = messages.get(i);
                        Log.d(TAG, "Message " + i + ": " + msg.getMessageContent() + " (from user: " + msg.isFromUser() + ")");
                    }
                    
                    showLoading(false);
                    Log.d(TAG, "Successfully loaded " + messages.size() + " messages for chat room " + currentChatRoomId);
                    
                    messageList.clear();
                    messageList.addAll(messages);
                    Log.d(TAG, "MessageList size after adding: " + messageList.size());
                    chatAdapter.notifyDataSetChanged();
                    
                    showChatInterface();
                    scrollToBottom();
                    Log.d(TAG, "Chat interface shown and scrolled to bottom");
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "★★★ getChatMessages ERROR ★★★");
                    Log.e(TAG, "Error details: " + error.getClass().getSimpleName());
                    Log.e(TAG, "Error message: " + error.getMessage());
                    if (error.getCause() != null) {
                        Log.e(TAG, "Error cause: " + error.getCause().getMessage());
                    }
                    error.printStackTrace();
                    
                    showLoading(false);
                    Log.e(TAG, "Error fetching messages for chat room " + currentChatRoomId, error);
                    Toast.makeText(AIChatActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    // Still show chat interface even if no messages loaded
                    messageList.clear();
                    chatAdapter.notifyDataSetChanged();
                    showChatInterface();
                }
            });
        } catch (AppwriteException e) {
            Log.e(TAG, "★★★ APPWRITE EXCEPTION when calling getChatMessages ★★★");
            Log.e(TAG, "AppwriteException details: " + e.getMessage());
            e.printStackTrace();
            
            showLoading(false);
            Log.e(TAG, "Exception when trying to get chat messages", e);
            Toast.makeText(this, "Error loading chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            
            // Still show chat interface
            messageList.clear();
            chatAdapter.notifyDataSetChanged();
            showChatInterface();
        }
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        
        if (messageText.isEmpty() || currentChatRoomId == null || currentUserId == null) {
            return;
        }
        
        // Clear input immediately
        editTextMessage.setText("");
        
        // Add user message to UI
        Message userMessage = new Message(currentChatRoomId, messageText, true, new Date());
        messageList.add(userMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
        
        // Add loading message for AI response
        Message loadingMessage = new Message(currentChatRoomId, "", false, new Date());
        loadingMessage.setLoading(true);
        messageList.add(loadingMessage);
        int loadingPosition = messageList.size() - 1;
        chatAdapter.notifyItemInserted(loadingPosition);
        scrollToBottom();
        
        // Disable send button temporarily
        buttonSend.setEnabled(false);
        
        // Send message to n8n webhook
        httpService.sendMessageToN8n(currentUserId, messageText, currentChatRoomId, true, new HttpService.HttpCallback() {
            @Override
            public void onSuccess(String aiResponse) throws AppwriteException {
                // Remove loading message and add AI response
                messageList.remove(loadingPosition);
                chatAdapter.notifyItemRemoved(loadingPosition);
                
                Message aiMessage = new Message(currentChatRoomId, aiResponse, false, new Date());
                messageList.add(aiMessage);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                scrollToBottom();
                
                // Re-enable send button
                buttonSend.setEnabled(editTextMessage.getText().toString().trim().length() > 0);
                
                // Refresh messages from database to ensure sync
                refreshMessages();
            }

            @Override
            public void onError(Exception error) {
                // Replace loading message with error message
                messageList.get(loadingPosition).setMessageContent("Sorry, something went wrong. Please try again.");
                messageList.get(loadingPosition).setLoading(false);
                chatAdapter.notifyItemChanged(loadingPosition);
                
                // Re-enable send button
                buttonSend.setEnabled(editTextMessage.getText().toString().trim().length() > 0);
                
                Log.e(TAG, "Error sending message to AI", error);
                Toast.makeText(AIChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshMessages() throws AppwriteException {
        if (currentChatRoomId == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        
        chatService.getChatMessages(currentChatRoomId, new ChatService.ChatCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                swipeRefreshLayout.setRefreshing(false);
                messageList.clear();
                messageList.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                scrollToBottom();
            }

            @Override
            public void onError(Exception error) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Error refreshing messages", error);
            }
        });
    }

    private void showWelcomeScreen() {
        layoutWelcome.setVisibility(View.VISIBLE);
        layoutChatHeader.setVisibility(View.GONE);
        recyclerViewMessages.setVisibility(View.GONE);
        
        // Animate welcome screen
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        layoutWelcome.startAnimation(fadeIn);
    }

    private void showChatInterface() {
        layoutWelcome.setVisibility(View.GONE);
        layoutChatHeader.setVisibility(View.VISIBLE);
        recyclerViewMessages.setVisibility(View.VISIBLE);
        
        // Update chat title
        textViewChatTitle.setText(currentChatRoomTitle != null ? currentChatRoomTitle : "AI Assistant");
        
        // Enable input
        editTextMessage.setEnabled(true);
        buttonSend.setEnabled(editTextMessage.getText().toString().trim().length() > 0);
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private void showLoading(boolean show) {
        progressBarLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpService != null) {
            httpService.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }

    // Debug method - thêm vào cuối class trước dấu }
    private void debugChatRoomSelection(String chatRoomId) {
        Log.d(TAG, "=== DEBUG CHAT ROOM SELECTION ===");
        Log.d(TAG, "Selected chat room ID: " + chatRoomId);
        Log.d(TAG, "Current user ID: " + currentUserId);
        
        // Test method to get all messages first
        try {
            chatService.getAllMessages(new ChatService.ChatCallback<List<Message>>() {
                @Override
                public void onSuccess(List<Message> allMessages) {
                    Log.d(TAG, "DEBUG: Total messages in database: " + allMessages.size());
                    for (Message msg : allMessages) {
                        Log.d(TAG, "DEBUG: Message - ChatRoomId: " + msg.getChatRoomId() + 
                              ", Content: " + msg.getMessageContent() + 
                              ", IsFromUser: " + msg.isFromUser());
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "DEBUG: Error getting all messages", error);
                }
            });
        } catch (AppwriteException e) {
            Log.e(TAG, "DEBUG: Exception getting all messages", e);
        }
    }
}
