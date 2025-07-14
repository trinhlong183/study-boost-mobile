package com.example.miniproject.adapters;

import com.example.miniproject.R;
import com.example.miniproject.models.Message;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AIChatAdapter extends RecyclerView.Adapter<AIChatAdapter.MessageViewHolder> {
    private List<Message> messages;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private static final int VIEW_TYPE_LOADING = 3;

    public AIChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
        } else if (viewType == VIEW_TYPE_LOADING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_loading, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_ai, parent, false);
        }
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.isLoading()) {
            return VIEW_TYPE_LOADING;
        }
        return message.isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTime;
        private LinearLayout layoutMessageBubble;
        private LinearLayout layoutLoadingDots;
        private View dotView1, dotView2, dotView3;
        private ValueAnimator loadingAnimator;
        private int viewType;

        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            
            textViewMessage = itemView.findViewById(R.id.textView_message);
            textViewTime = itemView.findViewById(R.id.textView_time);
            layoutMessageBubble = itemView.findViewById(R.id.layout_message_bubble);
            
            if (viewType == VIEW_TYPE_LOADING) {
                layoutLoadingDots = itemView.findViewById(R.id.layout_loading_dots);
                dotView1 = itemView.findViewById(R.id.dot1);
                dotView2 = itemView.findViewById(R.id.dot2);
                dotView3 = itemView.findViewById(R.id.dot3);
            }
        }

        public void bind(Message message) {
            if (viewType == VIEW_TYPE_LOADING) {
                startLoadingAnimation();
                return;
            }
            
            if (textViewMessage != null) {
                textViewMessage.setText(message.getMessageContent());
            }
            
            if (textViewTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                textViewTime.setText(sdf.format(message.getSentAt()));
            }
            
            // Customize bubble appearance based on message type
            if (layoutMessageBubble != null) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(24f);
                
                if (message.isFromUser()) {
                    drawable.setColor(ContextCompat.getColor(itemView.getContext(), R.color.user_message_bg));
                } else {
                    drawable.setColor(ContextCompat.getColor(itemView.getContext(), R.color.ai_message_bg));
                }
                
                layoutMessageBubble.setBackground(drawable);
            }
        }

        private void startLoadingAnimation() {
            if (loadingAnimator != null) {
                loadingAnimator.cancel();
            }
            
            loadingAnimator = ValueAnimator.ofFloat(0f, 1f);
            loadingAnimator.setDuration(1500);
            loadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
            loadingAnimator.setInterpolator(new LinearInterpolator());
            
            loadingAnimator.addUpdateListener(animation -> {
                float progress = animation.getAnimatedFraction();
                float offset = progress * 3; // 3 dots
                
                // Animate each dot with a phase offset
                animateDot(dotView1, offset);
                animateDot(dotView2, offset - 0.33f);
                animateDot(dotView3, offset - 0.66f);
            });
            
            loadingAnimator.start();
        }

        private void animateDot(View dot, float offset) {
            if (dot == null) return;
            
            float normalizedOffset = ((offset % 1f) + 1f) % 1f; // Normalize to 0-1
            float scale = 0.5f + 0.5f * (float) Math.sin(normalizedOffset * Math.PI * 2);
            float alpha = 0.3f + 0.7f * scale;
            
            dot.setScaleX(scale);
            dot.setScaleY(scale);
            dot.setAlpha(alpha);
        }

        public void stopLoadingAnimation() {
            if (loadingAnimator != null) {
                loadingAnimator.cancel();
                loadingAnimator = null;
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        super.onViewRecycled(holder);
        holder.stopLoadingAnimation();
    }
}
