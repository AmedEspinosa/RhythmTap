package com.example.rhythmtapgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.games.PlayGames;

import android.os.Handler;
import android.util.Log;


import android.text.TextPaint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private CurrencyManager currencyManager;
    private TextView beatCoinsTextView;
    private TextView beatCoinsTextView1;
    private TextView beatCoinsTextView2;
    private FrameLayout settingOverlay;
    private View settingButton;


    private SharedPreferences sharedPreferences;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);





        TextView title = findViewById(R.id.title);

        currencyManager = new CurrencyManager(this);

        settingOverlay = findViewById(R.id.settingsOverlay);

        settingButton = findViewById(R.id.settingButton);

        PlayGamesSdk.initialize(this);

        signInSilently();





        title.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (title.getWidth() > 0 && title.getHeight() > 0) {
                TextPaint paint = title.getPaint();
                float width = paint.measureText(title.getText().toString());

                LinearGradient shader = new LinearGradient(
                        0, 0, width, 0,
                        new int[]{
                                Color.parseColor("#D93232"),  // Red color at 0%
                                Color.parseColor("#9504AC"),  // Purple color at 34%
                                Color.parseColor("#FFFFFF")   // White color at 100%
                        },
                        new float[]{0f, 0.34f, 1f},
                        Shader.TileMode.CLAMP);

                paint.setShader(shader);
                title.setTextColor(Color.WHITE); // Set a base color
                title.invalidate(); // Force redraw
            }
        });


        FrameLayout leaderboardButton = findViewById(R.id.leaderboardButton);
        FrameLayout playButton = findViewById(R.id.playButton);
        FrameLayout storeButton = findViewById(R.id.shopButton);
        FrameLayout signInButton = findViewById(R.id.signInButtonLayout);




        beatCoinsTextView = findViewById(R.id.beatCoinBalanceText);
        beatCoinsTextView1 = findViewById(R.id.beatCoinBalanceTextShadow1);
        beatCoinsTextView2 = findViewById(R.id.beatCoinBalanceTextShadow2);


       updateBeatCoinsDisplay();

        signInButton.setOnClickListener(v -> startSignInIntent());


        applySettings();


        settingButton.setOnClickListener(v -> showSettingsMenu());

        leaderboardButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        playButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        storeButton.setOnClickListener(view -> {
            // Handle shop button click
        });
    }


    private void showSettingsMenu() {
        // Inflate the settings menu layout if it hasn't been done yet
        if (settingOverlay.getChildCount() == 0) {
            View settingsMenuContent = getLayoutInflater().inflate(R.layout.settings_activity, settingOverlay, false);
            settingOverlay.addView(settingsMenuContent);

            // Initialize settings UI components
            setupSettingsMenu(settingsMenuContent);
        }

        settingOverlay.setVisibility(View.VISIBLE);

    }



    private void setupSettingsMenu(View settingsMenuContent) {
        SeekBar musicSeekBar = settingsMenuContent.findViewById(R.id.music_volume);
        SeekBar soundEffectsSeekBar = settingsMenuContent.findViewById(R.id.sound_effects_volume);
        CheckBox musicCheckBox = settingsMenuContent.findViewById(R.id.musicCheckBox);
        CheckBox soundEffectsCheckBox = settingsMenuContent.findViewById(R.id.soundEffectCheckBox);
        View saveButton = settingsMenuContent.findViewById(R.id.saveButton);
        View exitButton = settingsMenuContent.findViewById(R.id.exitButton);
        TextView settingsSavedMessage = settingsMenuContent.findViewById(R.id.save_success_text);

        TextView soundEffectPercent = settingsMenuContent.findViewById(R.id.sound_effects_percentage);
        TextView musicPercent = settingsMenuContent.findViewById(R.id.music_percentage);

        // Load and apply settings
        loadSettings(musicSeekBar, soundEffectsSeekBar, musicCheckBox, soundEffectsCheckBox, soundEffectPercent, musicPercent);

        saveButton.setOnClickListener(view -> {
            saveSettings(musicSeekBar, soundEffectsSeekBar, musicCheckBox, soundEffectsCheckBox,
                    settingsSavedMessage, soundEffectPercent, musicPercent);
        });

        exitButton.setOnClickListener(view -> settingOverlay.setVisibility(View.GONE));
    }

    private void loadSettings(SeekBar musicSeekBar, SeekBar soundEffectsSeekBar, CheckBox musicCheckBox, CheckBox soundEffectsCheckBox, TextView soundEffectPercent, TextView musicPercent) {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        int musicVolume = sharedPreferences.getInt("musicVolume", 100);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
        boolean isMusicOn = sharedPreferences.getBoolean("isMusicOn", true);
        boolean isSoundEffectsOn = sharedPreferences.getBoolean("isSoundEffectsOn", true);

        musicSeekBar.setProgress(musicVolume);
        soundEffectsSeekBar.setProgress(soundEffectsVolume);
        musicCheckBox.setChecked(isMusicOn);
        soundEffectsCheckBox.setChecked(isSoundEffectsOn);


        String soundEffectText = soundEffectsVolume + "%";
        String musicText = musicVolume + "%";

        soundEffectPercent.setText(soundEffectText);
        musicPercent.setText(musicText);




        musicSeekBar.setEnabled(isMusicOn);
        soundEffectsSeekBar.setEnabled(isSoundEffectsOn);
    }

    private void saveSettings(SeekBar musicSeekBar, SeekBar soundEffectsSeekBar,
                              CheckBox musicCheckBox, CheckBox soundEffectsCheckBox,
                              TextView settingsSavedMessage, TextView soundEffectPercent, TextView musicPercent) {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int musicVolume = musicCheckBox.isChecked() ? musicSeekBar.getProgress() : 0;
        int soundEffectsVolume = soundEffectsCheckBox.isChecked() ? soundEffectsSeekBar.getProgress() : 0;
        boolean isMusicOn = musicCheckBox.isChecked();
        boolean isSoundEffectsOn = soundEffectsCheckBox.isChecked();


        String soundEffectText = soundEffectsVolume + "%";
        String musicText = musicVolume + "%";

        soundEffectPercent.setText(soundEffectText);
        musicPercent.setText(musicText);

        settingsSavedMessage.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> settingsSavedMessage.setVisibility(View.GONE), 2000);


        editor.putInt("musicVolume", musicVolume);
        editor.putInt("soundEffectsVolume", soundEffectsVolume);
        editor.putBoolean("isMusicOn", isMusicOn);
        editor.putBoolean("isSoundEffectsOn", isSoundEffectsOn);
        editor.putString("soundEffectPercent", soundEffectText);
        editor.putString("musicPercent",musicText);
        editor.apply();

        applySettings();  // Apply settings immediately after saving
    }

    private void applySettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);

        // Assuming you have methods to update volume in your SoundPool and MediaPlayer
        BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
        if (bouncingSquaresView != null) {
            bouncingSquaresView.updateVolume(soundEffectsVolume / 100f);
        }

        // Apply to MediaPlayer or other sound-related components
        // Example: mediaPlayer.setVolume(musicVolume / 100f, musicVolume / 100f);
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Reapply settings in case they were changed
        applySettings();
    }



    private void updateBeatCoinsDisplay(){
        int currentCoins = currencyManager.getBeatCoins();

        beatCoinsTextView.setText(String.valueOf(currentCoins));
        beatCoinsTextView1.setText(String.valueOf(currentCoins));
        beatCoinsTextView2.setText(String.valueOf(currentCoins));
    }

    private void signInSilently() {
        Log.d("SignIn", "Attempting silent sign-in");
        PlayGames.getGamesSignInClient(this).isAuthenticated().addOnCompleteListener(task -> {
            boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
            if (isAuthenticated) {
                // Player is already authenticated, you can start your game
                onConnected();
            } else {
                // Player is not authenticated, show sign-in button
                showSignInButton();
            }
        });
    }

    private void startSignInIntent() {
        Log.d("SignIn", "Starting explicit sign-in");
        PlayGames.getGamesSignInClient(this).signIn().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onConnected();
            } else {
                String message = task.getException().getMessage();
                // Show an error message to the user
            }
        });
    }

    private void onConnected() {
        Log.d("SignIn", "Sign-in successful");
        // The player is signed in. You can now start your game logic or enable Play Games features.
        // For example, you might hide the sign-in button and show the play button
        findViewById(R.id.signInButtonLayout).setVisibility(View.GONE);
    }

    private void showSignInButton() {
        Log.d("SignIn", "Sign-in failed or not signed in");


        findViewById(R.id.signInButtonLayout).setVisibility(View.VISIBLE);
    }
}
