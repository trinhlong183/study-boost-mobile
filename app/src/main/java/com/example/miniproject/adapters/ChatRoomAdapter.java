package com.example.miniproject.adapters;

import android.util.Log;
import com.example.miniproject.R;
import com.example.miniproject.models.ChatRoom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.appwrite.exceptions.AppwriteException;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {
    private static final String TAG = "ChatRoomAdapter";
    private List<ChatRoom> chatRooms;
    private OnChatRoomClickListener listener;
    private String selectedChatRoomId;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom) throws AppwriteException;
    }

    public ChatRoomAdapter(List<ChatRoom> chatRooms, OnChatRoomClickListener listener) {
        this.chatRooms = chatRooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        Log.d(TAG, "Binding chat room at position " + position + ": " + chatRoom.getTitle());
        holder.bind(chatRoom, chatRoom.getId().equals(selectedChatRoomId));
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount() returning: " + chatRooms.size());
        return chatRooms.size();
    }

    public void setSelectedChatRoom(String chatRoomId) {
        this.selectedChatRoomId = chatRoomId;
        notifyDataSetChanged();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textViewTitle;
        private TextView textViewDate;
        private View indicatorView;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView_chat_room);
            textViewTitle = itemView.findViewById(R.id.textView_chat_title);
            textViewDate = itemView.findViewById(R.id.textView_chat_date);
            indicatorView = itemView.findViewById(R.id.view_indicator);

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ChatRoom chatRoom = chatRooms.get(position);
                        Log.d(TAG, "Chat room clicked: " + chatRoom.getTitle() + " (ID: " + chatRoom.getId() + ")");
                        selectedChatRoomId = chatRoom.getId();
                        notifyDataSetChanged();
                        try {
                            listener.onChatRoomClick(chatRoom);
                        } catch (AppwriteException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        public void bind(ChatRoom chatRoom, boolean isSelected) {
            textViewTitle.setText(chatRoom.getTitle());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            textViewDate.setText(sdf.format(chatRoom.getCreatedAt()));

            // Update selection state
            if (isSelected) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.selected_chat_room_bg)
                );
                indicatorView.setVisibility(View.VISIBLE);
                indicatorView.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.primary_color)
                );
            } else {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.default_chat_room_bg)
                );
                indicatorView.setVisibility(View.GONE);
            }
        }
    }
}
