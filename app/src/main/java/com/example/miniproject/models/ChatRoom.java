package com.example.miniproject.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatRoom {
    private String id;
    private String userId;
    private String title;
    private Date createdAt;
    private Date updatedAt;
    
    public ChatRoom() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    public ChatRoom(String userId, String title) {
        this.userId = userId;
        this.title = title;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    public ChatRoom(String id, String userId, String title, Date createdAt, Date updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = new Date();
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getFormattedCreatedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(createdAt);
    }
    
    public String getFormattedUpdatedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(updatedAt);
    }
} 