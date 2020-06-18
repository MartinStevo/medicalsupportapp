package com.google.medicalsupportapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.medicalsupportapp.Chat;
import com.google.medicalsupportapp.MessageActivity;
import com.google.medicalsupportapp.R;
import com.google.medicalsupportapp.User;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> mChat;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> mChat) {
        this.mChat = mChat;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = mChat.get(position);

        holder.message.setText(chat.getMessage());

        if (position == mChat.size() - 1) {
            if (chat.isSeen()) {
                holder.seenMessage.setText("Prečítané");
            } else {
                holder.seenMessage.setText("Doručené");
            }
        } else {
            holder.seenMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView message;
        public TextView seenMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.chat_message);
            seenMessage = itemView.findViewById(R.id.seen);
        }
    }

    @Override
    public int getItemViewType(int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }
}
