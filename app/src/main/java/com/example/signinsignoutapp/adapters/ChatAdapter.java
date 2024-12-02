package com.example.signinsignoutapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.signinsignoutapp.databinding.ItemContainerRecievedMessageBinding;
import com.example.signinsignoutapp.databinding.ItemContainerSentMessageBinding;
import com.example.signinsignoutapp.models.ChatMessage;

import java.util.List;

// ChatAdapter class extends RecyclerView.Adapter<RecyclerView.ViewHolder>
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Bitmap receiverProfileBitmap;
    private final List<ChatMessage> chatMessages;
    private final String sendId;
    public static final int VIEW_TYPE_SENT = 1; // 1 resembles the sent view type
    public static final int VIEW_TYPE_RECEIVED = 2; // 2 resembles the sent view type

    /**
     * ChatAdapter constructor to initialize chatMessages, receiverProfileBitmap, and sendId
     *
     * @param chatMessages - the list of chat messages that were sent by the user
     *
     * @param receiverProfileBitmap - the receiver user profile picture in a bitmap format
     *
     * @param sendId - the sendId for the chat message
     */
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileBitmap, String sendId) {
        this.chatMessages = chatMessages;
        this.receiverProfileBitmap = receiverProfileBitmap;
        this.sendId = sendId;
    }

    /**
     * onCreateViewHolder method overridden
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return - the RecyclerView.ViewHolder of the current view type, either sent or received
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // for sent view types, return a new SentMessageViewHolder
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(ItemContainerSentMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else { // for received view types, return a new ReceiverMessageViewHolder
            return new ReceiverMessageViewHolder(ItemContainerRecievedMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    /**
     * onBindViewHolder method overridden
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // for sent view types
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        } else { // for received view types
            ((ReceiverMessageViewHolder)holder).setData(chatMessages.get(position), receiverProfileBitmap);
        }
    }

    /**
     * getItemCount method overridden
     *
     * @return - the number of chatMessages
     */
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    /**
     * getItemViewType method overridden
     *
     * @param position position to query
     *
     * @return - the current view type, either 1 or 2
     */
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(sendId)) {
            return VIEW_TYPE_SENT; // returns a 1 for sent view type
        } else {
            return VIEW_TYPE_RECEIVED; // returns a 2 for received view type
        }
    }

    // SentMessageViewHolder class extends RecyclerView.ViewHolder
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        /**
         * SentMessageViewHolder constructor to initialize the binding as the itemContainerSentMessageBinding
         *
         * @param itemContainerSentMessageBinding - the item container binding for the sent message
         */
        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding; // initialize binding here
        }

        /**
         * setData method to update the chat message text and date time
         *
         * @param chatMessage - the chat message that needs to be updated
         */
        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message); // the text of the chat message
            binding.textDateTime.setText(chatMessage.dateTime); // the date time of the chat message
        }
    }

    // ReceiverMessageViewHolder class extends RecyclerView.ViewHolder
    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerRecievedMessageBinding binding;

        /**
         * ReceiverMessageViewHolder constructor to initialize the binding as the itemContainerReceiverMessageBinding
         *
         * @param itemContainerRecievedMessageBinding - the item container binding for the received message
         */
        public ReceiverMessageViewHolder(ItemContainerRecievedMessageBinding itemContainerRecievedMessageBinding) {
            super(itemContainerRecievedMessageBinding.getRoot());
            binding = itemContainerRecievedMessageBinding; // initialize binding here
        }

        /**
         * setData method to update the chat message text and date time
         *
         * @param chatMessage - the chat message that needs to be updated
         *
         * @param receiverProfileBitmap - the receiver profile picture in a bitmap format
         */
        void setData(ChatMessage chatMessage, Bitmap receiverProfileBitmap) {
            binding.textMessage.setText(chatMessage.message); // the text of the chat message
            binding.textDateTime.setText(chatMessage.dateTime); // the date time of the chat message

            binding.imageProfile.setImageBitmap(receiverProfileBitmap); // the image profile picture of the receiver profile
        }
    }
}
