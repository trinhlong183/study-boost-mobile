package com.example.miniproject.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String chatRoomId;
    private String messageContent;
    private boolean isFromUser;
    private Date sentAt;
    private boolean isLoading = false; // Add loading state
    
    public Message(String messageContent, boolean isFromUser) {
        this.messageContent = messageContent;
        this.isFromUser = isFromUser;
        this.sentAt = new Date();
        this.chatRoomId = ""; // Will be set when chat room is created
    }
    
    public Message(String chatRoomId, String messageContent, boolean isFromUser, Date sentAt) {
        this.chatRoomId = chatRoomId;
        this.messageContent = messageContent;
        this.isFromUser = isFromUser;
        this.sentAt = sentAt;
    }
    
    public String getChatRoomId() {
        return chatRoomId;
    }
    
    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
    
    public String getMessageContent() {
        return messageContent;
    }
    
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    
    public boolean isFromUser() {
        return isFromUser;
    }
    
    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }
    
    public Date getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }
    
    // Convenience method to get content (for backward compatibility)
    public String getContent() {
        return messageContent;
    }
    
    // Convenience method to get timestamp as long
    public long getTimestamp() {
        return sentAt.getTime();
    }
    
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(sentAt);
    }
    
    // Loading state methods
    public boolean isLoading() {
        return isLoading;
    }
    
    public void setLoading(boolean loading) {
        isLoading = loading;
    }
} 