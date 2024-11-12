package com.example.signinsignoutapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ShowableListMenu;

import com.example.signinsignoutapp.databinding.ActivitySignUpBinding;
import com.example.signinsignoutapp.utilities.Constants;
import com.example.signinsignoutapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

// SignUpActivity class
public class SignUpActivity extends AppCompatActivity {

    // the binding for this activity
    private ActivitySignUpBinding binding;
    // the preferenceManager for this activity
    private PreferenceManager preferenceManager;
    // the encodeImage for this activity
    private String encodeImage;

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
        binding = ActivitySignUpBinding.inflate(getLayoutInflater()); // inflate binding
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners(); // set listeners for user click
    }

    /**
     * setListeners method for changing between the different activities
     */
    private void setListeners() {
        // switch back to the previous activity (SignInActivity) when user clicks "Sign In" text view
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        // if user successfully selects an image and inputs a first name, last name, email, and password
        // call the SignUp method when user clicks "Sign Up" button
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidateSignUpDetails()) {
                SignUp();
            }
        });

        // switch to the image choice display when the user clicks "Add Image" text view
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
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
     * SignUp method to post the user information to the database storage
     * Information includes the user profile picture, first name, last name, email, and password
     * Successful post of user information will change to new activity
     * Unsuccessful post will result in a exception Toast pop up message
     */
    private void SignUp() {
        loading(true); // signal the user SignUp is loading
        FirebaseFirestore database = FirebaseFirestore.getInstance(); // connect to FireBase

        // add user information into a hashmap to store key value pairs
        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,binding.inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME,binding.inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodeImage);

        // post the user hashmap to the database collection
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                // this code executes if successful post to database
                .addOnSuccessListener(documentReference -> {
                    loading(false); // signal the user SignUp is not loading

                    // signify the user is signed in
                    // put the user first name, last name, and profile picture in preferenceManager
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_FIRST_NAME, binding.inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME, binding.inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);

                    // declare a new intent for switching to MainActivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent); // switch to MainActivity

                    // this code executes if unsuccessful post to database
                }).addOnFailureListener(exception -> {
                    loading(false); // signal the user SignUp is not loading
                    showToast(exception.getMessage()); // display a Toast exception pop up message
        });
    }

    /**
     * encodeImage method converts a bitmap into a Base64 string to be displayed in the databse
     *
     * @param bitmap the bitmap that will be converted
     * @return the Base64 string of the bitmap
     */
    private String encodeImage(Bitmap bitmap) {
        // find width and height of new bitmap
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight()*previewWidth / bitmap.getHeight();

        // creates a new bitmap
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);

        // declare a new ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // the new bitmap compressed to JPEG format
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT); // return the Base64 string of the new bitmap
    }

    /**
     * ActivityResultLauncher class to set the user profile picture to the account
     * Either update the user profile picture or print an exception
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // set the user profile picture with the new bitmap
                        binding.imageProfile.setImageBitmap(bitmap);
                        // hide the "Add Image" text view
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage = encodeImage(bitmap); // encode the new bitmap
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * isValidateSignUpDetails method to check if user selected a profile picture and inputted
     *  first name, last name, email, password, and password confirmation
     * User email must be a real existing email
     * Password and password confirmation must be same
     *
     * @return
     */
    private Boolean isValidateSignUpDetails() {
        // check if user did not select a profile picture
        if (encodeImage == null) {
            showToast("Please Select Your Image"); // prompt the user to try again
            return false;
        }
        // check if user did not enter a first name
        else if (binding.inputFirstName.getText().toString().trim().isEmpty()) {
            showToast("Please Enter Your First Name"); // prompt the user to try again
            return false;
        }
        // check if user did not enter a last name
        else if (binding.inputLastName.getText().toString().trim().isEmpty()) {
                showToast("Please Enter Your Last Name"); // prompt the user to try again
                return false;
        }
        // check if user did not enter an email
        else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
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
        }
        // check if user did not enter a confirmation password
        else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please Confirm Your Password"); // prompt the user to try again
            return false;
        }
        // check if user password and confirmation password do not match
        else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Passwords Must be the Same"); // prompt the user to try again
            return false;
        }
        else {
            return true; // passed all checks, return true
        }
    }

    /**
     * loading method to signal to the user that the sign in is working
     *
     * @param isLoading the boolean value to signify if sign in is loading
     */
    private void loading(Boolean isLoading) {
        // if sign up is loading (true), make the sign in button invisible and display the progress bar
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        // if sign up is not loading (false), make the sign in button visible and un-display the progress bar
        else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}