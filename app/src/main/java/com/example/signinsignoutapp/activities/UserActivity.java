package com.example.signinsignoutapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.signinsignoutapp.R;
import com.example.signinsignoutapp.adapters.UsersAdapter;
import com.example.signinsignoutapp.databinding.ActivityUserBinding;
import com.example.signinsignoutapp.listeners.UserListener;
import com.example.signinsignoutapp.models.User;
import com.example.signinsignoutapp.utilities.Constants;
import com.example.signinsignoutapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// UserActivity class extends AppCompatActivity and implements UserListener
public class UserActivity extends AppCompatActivity implements UserListener {
    private ActivityUserBinding binding;
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
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        // invoke the helper functions
        setListeners();
        getUsers();
    }

    /**
     * setListener method for user on click actions
     */
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * getUsers method to collect all information and data about all user accounts in the database
     */
    private void getUsers() {
        loading(true); // set loading to true
        // initialize the firebase database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task -> {
                    loading(false); // set loading to false
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users  = new ArrayList<>(); // users list for holding all user accounts
                        // for each query get the results
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User(); // create a new User
                            // set all the user information accordingly from the database
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user); // add the users list
                        }
                        // check if no users added
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.userRecyclerView.setAdapter(usersAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage(); // prompt error message
                        }
                    } else {
                        showErrorMessage(); // prompt error message
                    }
                });
    }

    /**
     * showErrorMessage method to display error message to user
     */
    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * loading method to display and un-display the progress bar
     *
     * @param isLoading - the flag to signify if progress bar is loading or not
     */
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE); // display progress bar
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE); // un-display progress bar
        }
    }

    /**
     * onUserClicked method for when the user clicks a new user account
     *
     * @param user - the user account that was clicked by the user
     */
    @Override
    public void onUserClicked(User user) {
        // initialize a new intent to switch to the ChatActivity class
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}