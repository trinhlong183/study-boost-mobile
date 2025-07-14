package com.example.miniproject.adapters;

import com.example.miniproject.R;
import com.example.miniproject.models.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_USER_MESSAGE = 1;
    private static final int TYPE_AI_MESSAGE = 2;
    
    private List<Message> messages;
    
    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).isFromUser()) {
            return TYPE_USER_MESSAGE;
        } else {
            return TYPE_AI_MESSAGE;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == TYPE_USER_MESSAGE) {
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_ai, parent, false);
            return new AIMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
    
    // ViewHolder cho tin nhắn từ user
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTimestamp;
        
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textView_message);
            textViewTimestamp = itemView.findViewById(R.id.textView_timestamp);
        }
        
        public void bind(Message message) {
            textViewMessage.setText(message.getContent());
            textViewTimestamp.setText(message.getFormattedTime());
        }
    }
    
    // ViewHolder cho tin nhắn từ AI
    static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTimestamp;
        
        public AIMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textView_message);
            textViewTimestamp = itemView.findViewById(R.id.textView_timestamp);
        }
        
        public void bind(Message message) {
            textViewMessage.setText(message.getContent());
            textViewTimestamp.setText(message.getFormattedTime());
        }
    }
} 