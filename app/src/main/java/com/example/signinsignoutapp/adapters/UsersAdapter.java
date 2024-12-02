package com.example.signinsignoutapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.signinsignoutapp.databinding.ItemContainerUserBinding;
import com.example.signinsignoutapp.listeners.UserListener;
import com.example.signinsignoutapp.models.User;

import java.util.List;

// UsersAdapter class extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private final List<User> users;
    private final UserListener userListener;

    /**
     * UsersAdapter constructor to initialize the users list and userListeners
     *
     * @param users - the list of Users that are in the chat application
     *
     * @param userListener - the userListener in the chat application
     */
    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    /**
     * onCreateViewHolder method overridden
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return - the UserViewHolder of the itemContainerUserBinding
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // initialize the itemContainerUserBinding
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding
                .inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(itemContainerUserBinding); // return new UserViewHolder here
    }

    /**
     * onBindViewHolder method overridden
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    /**
     * getItemCount method overridden
     *
     * @return - the number of Users in the chat application
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    // UserViewHolder class extends RecyclerView.ViewHolder
    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        /**
         * UserViewHolder constructor to initialize the binding as the itemContainerUserBinding
         *
         * @param itemContainerUserBinding
         */
        public UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding; // initialize binding here
        }

        /**
         * setUserData method to display the current user's name, email, and profile picture information
         * allows for the user to visualize the different users in the chat application
         *
         * @param user - the current user to be displayed and information to be displayed
         */
        void setUserData(User user) {
            binding.textName.setText(user.name); // current user name
            binding.textEmail.setText(user.email); // current user email
            binding.imageProfile.setImageBitmap(getUserImage(user.image)); // current user profile picture
            // enable on click listener for clicking on a user
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    /**
     * getUserImage method to convert the encodedImage to a bitmap
     *
     * @param encodeImage - the encodedImage that is being converted
     *
     * @return - the converted encodedImage in bitmap form
     */
    private Bitmap getUserImage(String encodeImage) {
        byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT); // the bytes of the encodedImage
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length); // newly converted bitmap
    }
}
