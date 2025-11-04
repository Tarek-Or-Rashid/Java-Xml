package com.android.userdetails.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.android.userdetails.model.User;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText etLoginEmail, etLoginPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    // Database Helper
    private DatabaseHelper databaseHelper;

    // SharedPreferences for Remember Me
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        checkLoginStatus();

        // Initialize Views
        initViews();

        // Load Saved Credentials
        loadSavedCredentials();

        // Set Click Listeners
        setClickListeners();
    }

    private void initViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void checkLoginStatus() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            // User is already logged in, go to Dashboard
            String email = sharedPreferences.getString(KEY_EMAIL, "");
            goToDashboard(email);
        }
    }

    private void loadSavedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (rememberMe) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

            etLoginEmail.setText(savedEmail);
            etLoginPassword.setText(savedPassword);
            cbRememberMe.setChecked(true);
        }
    }

    private void setClickListeners() {
        // Login Button Click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Register Text Click
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to Register Activity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Forgot Password Click
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        // Get Input Values
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError("Email is required");
            etLoginEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etLoginEmail.setError("Please enter a valid email");
            etLoginEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Password is required");
            etLoginPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etLoginPassword.setError("Password must be at least 6 characters");
            etLoginPassword.requestFocus();
            return;
        }

        // Check User Credentials
        boolean isValidUser = databaseHelper.checkUser(email, password);

        if (isValidUser) {
            // Save Login Session
            saveLoginSession(email, password);

            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

            // Go to Dashboard
            goToDashboard(email);
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show();
            etLoginPassword.setError("Incorrect password");
            etLoginPassword.requestFocus();
        }
    }

    private void saveLoginSession(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save logged in status
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);

        // Save credentials if Remember Me is checked
        if (cbRememberMe.isChecked()) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_PASSWORD, password);
        } else {
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_PASSWORD);
        }

        editor.apply();
    }

    private void goToDashboard(String email) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Exit app on back press from login screen
        finishAffinity();
        super.onBackPressed();
    }
}