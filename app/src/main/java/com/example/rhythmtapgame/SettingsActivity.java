package com.example.rhythmtapgame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private SeekBar musicSeekBar;
    private SeekBar soundEffectsSeekBar;
    private CheckBox musicCheckBox;
    private CheckBox soundEffectsCheckBox;
    private View saveButton;
    private View exitButton;
    private TextView settingsSavedMessage;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Initialize Views
        musicSeekBar = findViewById(R.id.music_volume);
        soundEffectsSeekBar = findViewById(R.id.sound_effects_volume);
        musicCheckBox = findViewById(R.id.musicCheckBox);
        soundEffectsCheckBox = findViewById(R.id.soundEffectCheckBox);
        saveButton = findViewById(R.id.saveButton);
        exitButton = findViewById(R.id.exitButton);

        settingsSavedMessage = findViewById(R.id.save_success_text);


        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load saved settings
        loadSettings();

        // Set Listeners
        setListeners();
    }

    private void loadSettings() {
        // Load saved values and set them to the UI components
        int musicVolume = sharedPreferences.getInt("musicVolume", 100);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
        boolean isMusicOn = sharedPreferences.getBoolean("isMusicOn", true);
        boolean isSoundEffectsOn = sharedPreferences.getBoolean("isSoundEffectsOn", true);

        // Set UI components
        musicSeekBar.setProgress(musicVolume);
        soundEffectsSeekBar.setProgress(soundEffectsVolume);
        musicCheckBox.setChecked(isMusicOn);
        soundEffectsCheckBox.setChecked(isSoundEffectsOn);

        // Update SeekBar enabled state
        musicSeekBar.setEnabled(isMusicOn);
        soundEffectsSeekBar.setEnabled(isSoundEffectsOn);
    }

    private void setListeners() {
        musicCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            musicSeekBar.setEnabled(isChecked);
            if (!isChecked) {
                // If unchecked, set music volume to zero
                musicSeekBar.setProgress(0);
            } else {
                // If checked, restore to last saved volume or default
                int musicVolume = sharedPreferences.getInt("musicVolume", 100);
                musicSeekBar.setProgress(musicVolume);
            }
        });

        soundEffectsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEffectsSeekBar.setEnabled(isChecked);
            if (!isChecked) {
                // If unchecked, set sound effects volume to zero
                soundEffectsSeekBar.setProgress(0);
            } else {
                // If checked, restore to last saved volume or default
                int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
                soundEffectsSeekBar.setProgress(soundEffectsVolume);
            }
        });

        saveButton.setOnClickListener(view -> saveSettings());
        exitButton.setOnClickListener(view -> finish());
    }

    private void saveSettings() {
        int musicVolume = musicSeekBar.getProgress();
        int soundEffectsVolume = soundEffectsSeekBar.getProgress();
        boolean isMusicOn = musicCheckBox.isChecked();
        boolean isSoundEffectsOn = soundEffectsCheckBox.isChecked();

        settingsSavedMessage.setVisibility(View.VISIBLE);

        // Hide the message after 2 seconds
        new Handler().postDelayed(() -> settingsSavedMessage.setVisibility(View.GONE), 2000);


        // Save settings
        editor.putInt("musicVolume", musicVolume);
        editor.putInt("soundEffectsVolume", soundEffectsVolume);
        editor.putBoolean("isMusicOn", isMusicOn);
        editor.putBoolean("isSoundEffectsOn", isSoundEffectsOn);
        editor.apply();
    }
}
