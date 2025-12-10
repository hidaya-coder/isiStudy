package com.example.fittness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AuthHelper {
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "user_email";

    public static void setLoggedIn(Context context, String email) {
        android.util.Log.d("AuthHelper", "Saving login state for: " + email);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean success = prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_EMAIL, email)
                .commit(); // Use commit() for synchronous saving
        android.util.Log.d("AuthHelper", "Login state saved: " + success);
    }

    public static void setLoggedOut(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_EMAIL)
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        android.util.Log.d("AuthHelper", "isLoggedIn check: " + loggedIn);
        return loggedIn;
    }

    public static String getLoggedInEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, null);
        android.util.Log.d("AuthHelper", "getLoggedInEmail: " + email);
        return email;
    }

    public static void requireLogin(Context context) {
        if (!isLoggedIn(context)) {
            Intent intent = new Intent(context, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).finish();
            }
        }
    }
}

