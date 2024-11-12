package com.example.signinsignoutapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

// PreferenceManager class
public class PreferenceManager {

    // final SharedPreferences for accessing and modifying preference data
    private final SharedPreferences sharedPreferences;

    /**
     * PreferenceManager constructor
     *
     * @param context the current state of this application
     */
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME,context.MODE_PRIVATE);
    }

    /**
     * putBoolean method for adding key value pair in the SharedPreferences object
     *
     * @param key the String key to add
     * @param value the Boolean value to add
     */
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply(); // commit the changes in the editor
    }

    /**
     * getBoolean method for getting key value pair in the SharedPreferences object
     *
     * @param key the String key to add
     * @return the current sharedPreferences
     */
    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * putString method for adding key value pair in the SharedPreferences object
     *
     * @param key the String key to add
     * @param value the String value to add
     */
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply(); // commit the changes in the editor
    }

    /**
     * getString method for getting key value pair in the SharedPreferences object
     *
     * @param key the String key to add
     * @return the current sharedPreferences
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    /**
     * clear method to clear the editor
     */
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // clear the editor
        editor.apply(); // commit changes in the editor
    }
}
