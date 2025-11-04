package com.android.userdetails;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.android.userdetails.Activity.DashboardActivity;
import com.android.userdetails.Activity.GuestShoppingActivity;

public class MainActivity extends AppCompatActivity {

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";

    // Splash Screen Duration (1 second)
    private static final int SPLASH_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check Login Status and Navigate
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginAndNavigate();
            }
        }, SPLASH_DURATION);
    }

    private void checkLoginAndNavigate() {
        // Check if user is already logged in
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

        Intent intent;

        if (isLoggedIn) {
            // User is logged in - Go to Dashboard (Admin)
            String email = sharedPreferences.getString(KEY_EMAIL, "");
            intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("email", email);
        } else {
            // User is not logged in - Go to Guest Shopping
            intent = new Intent(MainActivity.this, GuestShoppingActivity.class);
        }

        startActivity(intent);
        finish();
    }
}