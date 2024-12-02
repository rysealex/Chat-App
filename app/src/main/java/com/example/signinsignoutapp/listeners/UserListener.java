package com.example.signinsignoutapp.listeners;

import com.example.signinsignoutapp.models.User;

// UserListener interface
public interface UserListener {
    /**
     * onUserClicked method for when the user clicks a new user account
     *
     * @param user - the user account that was clicked by the user
     */
    void onUserClicked(User user);
}
