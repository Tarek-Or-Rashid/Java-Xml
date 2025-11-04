package com.android.userdetails.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.android.userdetails.R;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private Spinner spinnerTheme, spinnerLanguage, spinnerFontSize;
    private SeekBar seekBarBrightness;
    private TextView tvBrightnessValue, tvSettingsTitle;
    private ImageView imgBack;
    private CardView cardTheme, cardLanguage, cardFontSize, cardBrightness;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SettingsPrefs";
    private static final String KEY_THEME = "theme";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_BRIGHTNESS = "brightness";

    // Flag to prevent multiple recreations
    private boolean isInitialLoad = true;

    // Theme Options
    private String[] themeOptions = {"Light", "Dark", "Auto"};
    private String[] languageOptions = {"English", "বাংলা"};
    private String[] fontSizeOptions = {"Small", "Medium", "Large"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load saved settings first
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadSavedSettings();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Views
        initViews();

        // Setup Spinners
        setupSpinners();

        // Setup Brightness Control
        setupBrightnessControl();

        // Set Click Listeners
        setClickListeners();
    }

    private void initViews() {
        tvSettingsTitle = findViewById(R.id.tvSettingsTitle);
        imgBack = findViewById(R.id.imgBack);

        cardTheme = findViewById(R.id.cardTheme);
        cardLanguage = findViewById(R.id.cardLanguage);
        cardFontSize = findViewById(R.id.cardFontSize);
        cardBrightness = findViewById(R.id.cardBrightness);

        spinnerTheme = findViewById(R.id.spinnerTheme);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerFontSize = findViewById(R.id.spinnerFontSize);

        seekBarBrightness = findViewById(R.id.seekBarBrightness);
        tvBrightnessValue = findViewById(R.id.tvBrightnessValue);
    }

    private void setupSpinners() {
        // Theme Spinner
        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, themeOptions);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);

        // Set saved theme selection
        String savedTheme = sharedPreferences.getString(KEY_THEME, "Auto");
        spinnerTheme.setSelection(getThemePosition(savedTheme));

        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTheme = themeOptions[position];
                saveTheme(selectedTheme);
                applyTheme(selectedTheme);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Language Spinner
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languageOptions);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);

        // Set saved language selection
        String savedLanguage = sharedPreferences.getString(KEY_LANGUAGE, "English");
        spinnerLanguage.setSelection(getLanguagePosition(savedLanguage));

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isInitialLoad) {
                    String selectedLanguage = languageOptions[position];
                    String currentLanguage = sharedPreferences.getString(KEY_LANGUAGE, "English");

                    if (!selectedLanguage.equals(currentLanguage)) {
                        saveLanguage(selectedLanguage);
                        applyLanguage(selectedLanguage);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Font Size Spinner
        ArrayAdapter<String> fontSizeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fontSizeOptions);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFontSize.setAdapter(fontSizeAdapter);

        // Set saved font size selection
        String savedFontSize = sharedPreferences.getString(KEY_FONT_SIZE, "Medium");
        spinnerFontSize.setSelection(getFontSizePosition(savedFontSize));

        spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isInitialLoad) {
                    String selectedFontSize = fontSizeOptions[position];
                    String currentFontSize = sharedPreferences.getString(KEY_FONT_SIZE, "Medium");

                    if (!selectedFontSize.equals(currentFontSize)) {
                        saveFontSize(selectedFontSize);
                        applyFontSize(selectedFontSize);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupBrightnessControl() {
        // Get saved brightness
        int savedBrightness = sharedPreferences.getInt(KEY_BRIGHTNESS, 50);
        seekBarBrightness.setProgress(savedBrightness);
        tvBrightnessValue.setText(savedBrightness + "%");

        // Set flag to false after initial setup
        seekBarBrightness.post(new Runnable() {
            @Override
            public void run() {
                isInitialLoad = false;
            }
        });

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBrightnessValue.setText(progress + "%");
                applyBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveBrightness(seekBar.getProgress());
            }
        });
    }

    private void setClickListeners() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Theme Methods
    private void saveTheme(String theme) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }

    private void applyTheme(String theme) {
        switch (theme) {
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Auto":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private int getThemePosition(String theme) {
        for (int i = 0; i < themeOptions.length; i++) {
            if (themeOptions[i].equals(theme)) {
                return i;
            }
        }
        return 2; // Default Auto
    }

    // Language Methods
    private void saveLanguage(String language) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    private void applyLanguage(String language) {
        String languageCode = language.equals("বাংলা") ? "bn" : "en";
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Restart activity to apply changes
        recreate();
    }

    private int getLanguagePosition(String language) {
        for (int i = 0; i < languageOptions.length; i++) {
            if (languageOptions[i].equals(language)) {
                return i;
            }
        }
        return 0; // Default English
    }

    // Font Size Methods
    private void saveFontSize(String fontSize) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FONT_SIZE, fontSize);
        editor.apply();
    }

    private void applyFontSize(String fontSize) {
        float scale = 1.0f;
        switch (fontSize) {
            case "Small":
                scale = 0.85f;
                break;
            case "Medium":
                scale = 1.0f;
                break;
            case "Large":
                scale = 1.15f;
                break;
        }

        Configuration config = new Configuration();
        config.fontScale = scale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Restart activity to apply changes
        recreate();
    }

    private int getFontSizePosition(String fontSize) {
        for (int i = 0; i < fontSizeOptions.length; i++) {
            if (fontSizeOptions[i].equals(fontSize)) {
                return i;
            }
        }
        return 1; // Default Medium
    }

    // Brightness Methods
    private void saveBrightness(int brightness) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_BRIGHTNESS, brightness);
        editor.apply();
    }

    private void applyBrightness(int brightness) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness / 100.0f;
        getWindow().setAttributes(layoutParams);
    }

    // Load all saved settings
    private void loadSavedSettings() {
        String savedTheme = sharedPreferences.getString(KEY_THEME, "Auto");
        applyTheme(savedTheme);

        String savedLanguage = sharedPreferences.getString(KEY_LANGUAGE, "English");
        if (!savedLanguage.equals("English")) {
            String languageCode = savedLanguage.equals("বাংলা") ? "bn" : "en";
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }

        String savedFontSize = sharedPreferences.getString(KEY_FONT_SIZE, "Medium");
        float scale = 1.0f;
        switch (savedFontSize) {
            case "Small":
                scale = 0.85f;
                break;
            case "Large":
                scale = 1.15f;
                break;
        }
        if (scale != 1.0f) {
            Configuration config = new Configuration();
            config.fontScale = scale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }

        int savedBrightness = sharedPreferences.getInt(KEY_BRIGHTNESS, 50);
        applyBrightness(savedBrightness);
    }
}