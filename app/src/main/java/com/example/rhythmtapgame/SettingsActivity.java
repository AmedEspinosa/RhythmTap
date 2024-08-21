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

        musicSeekBar = findViewById(R.id.music_volume);
        soundEffectsSeekBar = findViewById(R.id.sound_effects_volume);
        musicCheckBox = findViewById(R.id.musicCheckBox);
        soundEffectsCheckBox = findViewById(R.id.soundEffectCheckBox);
        saveButton = findViewById(R.id.saveButton);
        exitButton = findViewById(R.id.exitButton);

        settingsSavedMessage = findViewById(R.id.save_success_text);

        sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        loadSettings();

        setListeners();
    }

    private void loadSettings() {

        int musicVolume = sharedPreferences.getInt("musicVolume", 100);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
        boolean isMusicOn = sharedPreferences.getBoolean("isMusicOn", true);
        boolean isSoundEffectsOn = sharedPreferences.getBoolean("isSoundEffectsOn", true);

        musicSeekBar.setProgress(musicVolume);
        soundEffectsSeekBar.setProgress(soundEffectsVolume);
        musicCheckBox.setChecked(isMusicOn);
        soundEffectsCheckBox.setChecked(isSoundEffectsOn);

        musicSeekBar.setEnabled(isMusicOn);
        soundEffectsSeekBar.setEnabled(isSoundEffectsOn);
    }

    private void setListeners() {
        musicCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            musicSeekBar.setEnabled(isChecked);
            if (!isChecked) {

                musicSeekBar.setProgress(0);
            } else {

                int musicVolume = sharedPreferences.getInt("musicVolume", 100);
                musicSeekBar.setProgress(musicVolume);
            }
        });

        soundEffectsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEffectsSeekBar.setEnabled(isChecked);
            if (!isChecked) {

                soundEffectsSeekBar.setProgress(0);
            } else {

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

        new Handler().postDelayed(() -> settingsSavedMessage.setVisibility(View.GONE), 2000);

        editor.putInt("musicVolume", musicVolume);
        editor.putInt("soundEffectsVolume", soundEffectsVolume);
        editor.putBoolean("isMusicOn", isMusicOn);
        editor.putBoolean("isSoundEffectsOn", isSoundEffectsOn);
        editor.apply();
    }
}
