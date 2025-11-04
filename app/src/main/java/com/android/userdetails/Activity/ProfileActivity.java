package com.android.userdetails.Activity;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.userdetails.R;
import com.android.userdetails.db.DatabaseHelper;
import com.android.userdetails.model.User;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    // View Mode Components
    private CardView viewModeCard;
    private TextView tvViewName, tvViewEmail, tvViewPhone, tvViewGender, tvViewAddress, tvViewDOB;

    // Edit Mode Components
    private CardView editModeCard;
    private TextInputEditText etName, etPhone, etGender, etAddress, etDOB;

    private TextView tvTitle;
    private Button btnSaveAll, btnBack;

    private DatabaseHelper db;
    private SharedPreferences sharedPreferences;
    private String userEmail;

    private boolean isEditMode = false;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("email", "");

        // Initialize Views
        tvTitle = findViewById(R.id.tvTitle);
        btnSaveAll = findViewById(R.id.btnSaveAll);
        btnBack = findViewById(R.id.btnBack);

        // View Mode Views
        viewModeCard = findViewById(R.id.viewModeCard);
        tvViewName = findViewById(R.id.tvViewName);
        tvViewEmail = findViewById(R.id.tvViewEmail);
        tvViewPhone = findViewById(R.id.tvViewPhone);
        tvViewGender = findViewById(R.id.tvViewGender);
        tvViewAddress = findViewById(R.id.tvViewAddress);
        tvViewDOB = findViewById(R.id.tvViewDOB);

        // Edit Mode Views
        editModeCard = findViewById(R.id.editModeCard);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etGender = findViewById(R.id.etGender);
        etAddress = findViewById(R.id.etAddress);
        etDOB = findViewById(R.id.etDOB);

        // Detect mode from Intent (default: view mode)
        isEditMode = getIntent().getBooleanExtra("editMode", false);

        if (isEditMode) {
            enableEditMode();
        } else {
            enableViewMode();
        }

        loadUserData();

        // DatePicker for DOB in Edit Mode
        if (isEditMode) {
            etDOB.setKeyListener(null);
            etDOB.setOnClickListener(v -> showDatePicker());
        }

        btnSaveAll.setOnClickListener(v -> saveAllData());
        btnBack.setOnClickListener(v -> finish());
    }

    private void enableViewMode() {
        tvTitle.setText("View Profile");
        viewModeCard.setVisibility(View.VISIBLE);
        editModeCard.setVisibility(View.GONE);
        btnSaveAll.setVisibility(View.GONE);
    }

    private void enableEditMode() {
        tvTitle.setText("Edit Profile");
        viewModeCard.setVisibility(View.GONE);
        editModeCard.setVisibility(View.VISIBLE);
        btnSaveAll.setVisibility(View.VISIBLE);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        String dobStr = etDOB.getText().toString().trim();
        if (!dobStr.isEmpty()) {
            try {
                Date date = sdf.parse(dobStr);
                if (date != null) calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            etDOB.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void loadUserData() {
        User user = db.getUserByEmail(userEmail);

        if (user != null) {
            Log.d(TAG, "Loading user data: " + user.toString());

            if (isEditMode) {
                // Edit Mode - Load data into edit fields
                etName.setText(user.getName());
                etPhone.setText(user.getPhone());
                etGender.setText(user.getGender());
                etAddress.setText(user.getAddress());
                etDOB.setText(user.getDateOfBirth());
            } else {
                // View Mode - Display data in TextViews
                tvViewName.setText(user.getName());
                tvViewEmail.setText(user.getEmail());
                tvViewPhone.setText(user.getPhone());
                tvViewGender.setText(user.getGender());
                tvViewAddress.setText(user.getAddress() == null || user.getAddress().isEmpty()
                        ? "Not provided" : user.getAddress());
                tvViewDOB.setText(user.getDateOfBirth() == null || user.getDateOfBirth().isEmpty()
                        ? "Not provided" : user.getDateOfBirth());
            }
        } else {
            Log.e(TAG, "User not found for email: " + userEmail);
            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAllData() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        }
        if (gender.isEmpty()) {
            etGender.setError("Gender is required");
            etGender.requestFocus();
            return;
        }

        if (!dob.isEmpty() && !isValidDate(dob)) {
            etDOB.setError("Invalid date format. Use dd/MM/yyyy");
            etDOB.requestFocus();
            return;
        }

        boolean basicUpdated = db.updateBasicInfo(userEmail, name, phone, gender);
        boolean profileUpdated = db.updateUserProfile(userEmail, address, dob, null, null, null);

        if (basicUpdated || profileUpdated) {
            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save profile. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}