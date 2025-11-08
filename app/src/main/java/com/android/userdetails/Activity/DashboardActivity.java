package com.android.userdetails.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.android.userdetails.model.User;

public class DashboardActivity extends AppCompatActivity {

    // UI Components
    private TextView tvUserName, tvUserEmail, tvUserPhone, tvUserGender;
    private CardView cardViewProfile, cardEditProfile, cardAddProduct, cardViewProduct,
            cardViewOrders, cardSettings, cardLogout;

    // Database Helper
    private DatabaseHelper databaseHelper;

    // User Data
    private String userEmail;
    private User currentUser;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Database
        databaseHelper = new DatabaseHelper(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Get User Email from Intent or SharedPreferences
        getUserEmail();

        // Initialize Views
        initViews();

        // Load User Data
        loadUserData();

        // Set Click Listeners
        setClickListeners();
    }

    private void getUserEmail() {
        // Try to get email from Intent first
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("email");

        // If not in Intent, get from SharedPreferences
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = sharedPreferences.getString("email", "");
        }

        // If still empty, redirect to login
        if (userEmail.isEmpty()) {
            redirectToLogin();
        }
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserGender = findViewById(R.id.tvUserGender);

        cardViewProfile = findViewById(R.id.cardViewProfile);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardAddProduct = findViewById(R.id.cardAddProduct);
        cardViewProduct = findViewById(R.id.cardViewProduct);
        cardViewOrders = findViewById(R.id.cardViewOrders);
        cardSettings = findViewById(R.id.cardSettings);
        cardLogout = findViewById(R.id.cardLogout);
    }

    private void loadUserData() {
        // Get User from Database
        currentUser = databaseHelper.getUserByEmail(userEmail);

        if (currentUser != null) {
            // Display User Information
            tvUserName.setText(currentUser.getName());
            tvUserEmail.setText(currentUser.getEmail());
            tvUserPhone.setText(currentUser.getPhone());
            tvUserGender.setText(currentUser.getGender());
        } else {
            redirectToLogin();
        }
    }

    private void setClickListeners() {
        // View Profile Card Click
        cardViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                intent.putExtra("email", userEmail);
                startActivity(intent);
            }
        });

        // Edit Profile Card Click
        cardEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                intent.putExtra("email", userEmail);
                intent.putExtra("editMode", true);
                startActivity(intent);
            }
        });

        // Add Product Card Click
        cardAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

        // View Product Card Click
        cardViewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ViewProductActivity.class);
                startActivity(intent);
            }
        });

        // View Orders Card Click
        cardViewOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });

        // Settings Card Click
        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Logout Card Click
        cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                performLogout();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performLogout() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to GuestShoppingActivity instead of LoginActivity
        redirectToGuestShopping();
    }

    // ✅ NEW METHOD: Redirect to GuestShoppingActivity after logout
    private void redirectToGuestShopping() {
        Intent intent = new Intent(DashboardActivity.this, GuestShoppingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Keep this method for other redirects (like when user data is not found)
    private void redirectToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning from Profile Activity
        if (userEmail != null && !userEmail.isEmpty()) {
            loadUserData();
        }
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App");
        builder.setMessage("Do you want to exit the app?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}