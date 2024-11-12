package com.example.signinsignoutapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.signinsignoutapp.R;
import com.example.signinsignoutapp.databinding.ActivitySignInBinding;
import com.example.signinsignoutapp.utilities.Constants;
import com.example.signinsignoutapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

// SignInActivity class extends AppCompatActivity
public class SignInActivity extends AppCompatActivity {

    // the binding for this activity
    private ActivitySignInBinding binding;
    // the preferenceManager for this activity
    private PreferenceManager preferenceManager;

    /**
     * onCreate method overridden, initialize binding and preferenceManager
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater()); // inflate binding
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners(); // set listeners for user click
    }

    /**
     * setListeners method for changing between the different activities
     */
    private void setListeners() {
        // switch to SignUpActivity when user clicks "Create New Account" text view
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));

        // if user successfully inputs an email and password, call the SignIn method when user clicks "Sign In" button
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidateSignInDetails()) {
                SignIn();
            }
        });
    }

    /**
     * showToast method for displaying a Toast pop up message in activity
     *
     * @param message the Toast pop up message to display
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * SignIn method to check if user can successfully sign in with the inputted email and password
     * User email and password will be searched in the database
     * Successfully found email and password will change to new activity
     * Unsuccessful search will result in a error Toast pop up message
     */
    private void SignIn() {
        loading(true); // signal the user SignIn is loading
        FirebaseFirestore database = FirebaseFirestore.getInstance(); // connect to FireBase

        // get access to the user email and password from the database
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    // checking if successfully found the user email and password in the database
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        // create a DocumentSnapshot for the user id and profile picture
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        // put user id and profile picture in the preferenceManager editor
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        // declare a new intent for switching to MainActivity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        showToast("Successful Sign In"); // display a Toast pop up message for successful sign in
                        startActivity(intent); // switch to MainActivity
                    }
                    else {
                        loading(false); // signal the user SignIn is not loading
                        showToast("Unable to Sign In"); // display a Toast pop up message for unsuccessful sign in
                    }
                });
    }

    /**
     * loading method to signal to the user that the sign in is working
     *
     * @param isLoading the boolean value to signify if sign in is loading
     */
    private void loading(Boolean isLoading) {
        // if sign in is loading (true), make the sign in button invisible and display the progress bar
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        // if sign in is not loading (false), make the sign in button visible and un-display the progress bar
        else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * isValidateSignInDetails method to check if user input an email and password
     * User email must be a real existing email
     *
     * @return the boolean value to signify if user input is correct
     */
    private boolean isValidateSignInDetails() {
        // check if user did not enter an email
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please Enter Your Email"); // prompt the user to try again
            return false;
        }
        // check if user email is not a real existing email
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please Enter Valid Email"); // prompt the user to try again
            return false;
        }
        // check if user did not enter a password
        else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please Enter Your Password"); // prompt the user to try again
            return false;
        } else {
            return true; // passed all checks, return true
        }
    }
}