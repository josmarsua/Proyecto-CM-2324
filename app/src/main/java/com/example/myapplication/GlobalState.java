package com.example.myapplication;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.example.myapplication.database.entities.User;

public class GlobalState {
    private static final String TAG = "GLOBAL STATE";
    public enum keys{
        USERNAME,
        USER_ID,
        USER_EMAIL,
        USER_PROFILE,
        USER_BIO,
        USER_FIRST_NAME,
        USER_LAST_NAME,
        USER_PHONE
    }

    private static SharedPreferences preferences;

    public static void init(Context context) {
        preferences = context.getSharedPreferences("globalStateFile", MODE_PRIVATE);
    }

    public static void saveString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void saveImage(byte[] image)
    {
        SharedPreferences.Editor editor = preferences.edit();
        String byteString = Base64.encodeToString(image, Base64.DEFAULT);
        editor.putString(keys.USER_PROFILE.name(), byteString);
        editor.apply();
    }

    public static byte[] getImage()
    {
        String byteString = preferences.getString(keys.USER_PROFILE.name(), null);
        byte[] byteArray= null;
        if (byteString != null) {
          byteArray = Base64.decode(byteString, Base64.DEFAULT);
        }
        return byteArray;
    }

    public static String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public static void saveLoginState(User user)
    {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(keys.USERNAME.name(), user.username);
        editor.putInt(keys.USER_ID.name(), user.uid);
        editor.putString(keys.USER_EMAIL.name(), user.email);
        editor.putString(keys.USER_BIO.name(), user.bio);
        editor.putString(keys.USER_PHONE.name(), user.phoneNumber);
        editor.putString(keys.USER_FIRST_NAME.name(), user.firstName);
        editor.putString(keys.USER_LAST_NAME.name(), user.lastName);
        if (user.image != null) {
            saveImage(user.image);
        }
        editor.apply();
        Log.e(TAG, "Saving State for User: " + user);
    }

    public static String getLoggedInUser()
    {
        return preferences.getString((keys.USERNAME.name()), "");
    }

    public static String getLoggedInUserEmail()
    {
        return preferences.getString((keys.USER_EMAIL.name()), "");
    }

    public static String getLoggedInUserFirstname()
    {
        return preferences.getString((keys.USER_FIRST_NAME.name()), "");
    }

    public static String getLoggedInUserLastname()
    {
        return preferences.getString((keys.USER_LAST_NAME.name()), "");
    }

    public static String getLoggedInUserPhone()
    {
        return preferences.getString((keys.USER_PHONE.name()), "");
    }


    public static boolean isLoggedIn()
    {
        return !preferences.getString(keys.USERNAME.name(), "").isEmpty();
    }

    public static void removeLoginState()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(keys.USERNAME.name());
        editor.remove(keys.USER_ID.name());
        editor.remove(keys.USER_EMAIL.name());
        editor.remove(keys.USER_PHONE.name());
        editor.remove(keys.USER_FIRST_NAME.name());
        editor.remove(keys.USER_LAST_NAME.name());
        editor.apply();
    }

    public static void destroy()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void printLoginState() {
            Log.d("Preferences", "Logged in user: " + getLoggedInUser());
            Log.d("Preferences", "User ID: " + getInt(keys.USER_ID.name(), -1));
            Log.d("Preferences", "User Email: " + getString(keys.USER_EMAIL.name(), ""));
        }
    }


