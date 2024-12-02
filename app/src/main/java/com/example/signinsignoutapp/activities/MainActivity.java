package com.example.signinsignoutapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.signinsignoutapp.R;
import com.example.signinsignoutapp.activities.SignUpActivity;
import com.example.signinsignoutapp.databinding.ActivityMainBinding;
import com.example.signinsignoutapp.databinding.ActivitySignInBinding;
import com.example.signinsignoutapp.models.User;
import com.example.signinsignoutapp.utilities.Constants;
import com.example.signinsignoutapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

// MainActivity class extends AppCompatActivity
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        // invoke the helper functions
        loadUserDetails();
        getToken();
        setListener();
    }

    /**
     * setListener method for user on click actions
     */
    private void setListener() {
        // invoke the signOut function to sign the user out
        binding.imagesSignOut.setOnClickListener(v -> signOut());
        // continue to the userActivity class to choose which account to chat with
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UserActivity.class)));
    }

    /**
     * loadUserDetails method to display the user name and profile image
     */
    private void loadUserDetails() {
        // set user name
        binding.textName.setText(preferenceManager.getString(Constants.KEY_FIRST_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap); // set user profile image
        binding.progressBar.setVisibility(View.GONE); // make progress bar invisible
    }

    /**
     * showToast method to prompt the user in a Toast pop up format
     *
     * @param message - the message in the Toast
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * getToken method to get the user firebase token on successful login
     */
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    /**
     * updateToken method to update the user token in firebase database on real-time
     *
     * @param token - the user firebase token
     */
    private void updateToken(String token) {
        // initialize the firebase database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // initialize a new DocumentReference connecting the database
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                // prompt the user with a Toast pop up message
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> showToast("Unable to update Token"));
    }

    /**
     * signOut method when the user clicks the sign out button
     */
    private void signOut() {
        showToast("Signing out ..."); // prompt the user
        // initialize the firebase database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        // initialize a new DocumentReference connecting the database
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        // initialize a new HashMap for the updates
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete()); // add the user token to updates
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                }).addOnFailureListener(e -> showToast("Unable to sign out")); // prompt the user
    }
}