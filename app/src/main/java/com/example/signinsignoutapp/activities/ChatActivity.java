package com.example.signinsignoutapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.signinsignoutapp.R;
import com.example.signinsignoutapp.adapters.ChatAdapter;
import com.example.signinsignoutapp.databinding.ActivityChatBinding;
import com.example.signinsignoutapp.models.ChatMessage;
import com.example.signinsignoutapp.models.User;
import com.example.signinsignoutapp.utilities.Constants;
import com.example.signinsignoutapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// ChatActivity class extends AppCompatActivity
public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    /**
     * onCreate method overridden
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // invoke the helper functions
        loadReceiverDetails();
        setListeners();
        init();
        listenMessage();
    }

    /**
     * init method to initialize the chatMessages, the chatAdapter, and the firebase database
     */
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>(); // initialize chatMessages array list here
        // initialize chatAdapter here with chatMessages, receiverUser image, and user ID
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance(); // initialize the firebase database here
    }

    /**
     * sendMessages method to control the messages being sent from the user
     * the messages will then be directed to the firebase database in real-time
     */
    private void sendMessages() {
        // message made up of a String, Object key-pair
        HashMap<String, Object> message = new HashMap<>();

        // add the sender and receiver id's to the message
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);

        // add the actual message and time stamp of the message
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());

        // add this message to the firebase database
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        binding.inputMessage.setText(null); // reset the input message edit text box for the next message
    }

    /**
     * listenMessage method to differentiate between the sender and receiver messages
     * understands what message comes from who
     */
    private void listenMessage() {
        // for messages coming from the sender
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);

        // for messages coming from the receiver
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,
                        receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,
                        preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    /**
     * EventListener for getting the query information from the current message in the firebase database
     * initialize the chat message sender id, receiver id, message, date time, and date object
     * added to the chatMessages list
     */
    private final EventListener <QuerySnapshot> eventListener = ((value, error) -> {
        // if error occur, end function
        if (error != null) {
            return;
        }
        // continue if value is present
        if  (value != null) {
            int count = chatMessages.size();
            // for each DocumentChange in the value document changes
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                // if message has been added, we need to initialize the information here
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage(); // create new chatMessage
                    // initialize sender id, receiver id, message, date time, and date object from firebase database
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(
                            documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));

                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            // sort the chatMessages based on the date object
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged(); // if no chat messages
            } else {
                chatAdapter.notifyItemRangeChanged(chatMessages.size(), chatMessages.size());

                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE); // inflate the chatRecyclerView here
        }
        binding.progressBar.setVisibility(View.GONE); // hide the progress bar
    });

    /**
     * getBitmapFromEncodedString method to convert the encodedImage to a bitmap
     *
     * @param encodedImage - the encodedImage that is being converted
     *
     * @return - the converted encodedImage in bitmap form
     */
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT); // the bytes of the encodedImage
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length); // newly converted bitmap
    }

    /**
     * loadReceiverDetails method to initialize the receiverUser and their user name
     */
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name); // user name here
    }

    /**
     * setListeners method for the user on click listeners
     * either go back on imageBack click, or sendMessages invoked on layoutSend click
     */
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());

        binding.layoutSend.setOnClickListener(v -> sendMessages());
    }

    /**
     * getReadableDateTime method for getting the current local time of the chat message
     *
     * @param date - the current local time in month, day, year, and time format
     * @return - the new formatted local time
     */
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy - hh:mm a",
                Locale.getDefault()).format(date);
    }
}