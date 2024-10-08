package com.example.rhythmtapgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.gridlayout.widget.GridLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private GridLayout gridLayout;
    private List<BeatTile> beatTiles;
    private TextView timerTextView;
    private TextView shadow1Timer;
    private TextView shadow2Timer;
    private CountDownTimer gameTimer;
    private long timeLeftInMillis = 10000;
    private Random random;
    private MediaPlayer mediaPlayer;
    private TextView levelTextView;
    private TextView shadow1Level;
    private TextView shadow2Level;
    private TextView scoreTextView;
    private TextView shadow1Score;
    private TextView shadow2Score;
    private SoundPool soundPool;
    private int freezeSoundId, clearSoundId, addTimeSoundId;
    private int tilePressSoundId1, tilePressSoundId2, lowTimeSoundID;
    private int totalTaps = 0;
    private int correctTaps = 0;
    private int currentLevel = 1;
    private int powerUpUse = 0;
    private int currentVariation;
    private int totalScore;
    private static final int[] ROW_COLORS = {
            Color.parseColor("#7D60CE"), // Purple
            Color.parseColor("#D93232"), // Red
            Color.parseColor("#F9DA66"), // Yellow
            Color.parseColor("#55E5BA"), // Cyan
            Color.parseColor("#4C49DD"), // Blue
            Color.parseColor("#59B937")  // Green
    };
    private CSVProcessor csvProcessor;
    private String currentSong;
    private int freezeCount;
    private int clearCount;
    private int addTimeCount;
    private boolean isTimerFrozen = false;
    private final Handler freezeHandler = new Handler();
    private boolean isPaused = false;
    private FrameLayout pauseMenuOverlay;
    private FrameLayout gameOverMenuOverlay;
    private long timeRemainingOnPause;
    private CurrencyManager currencyManager;
    private SharedPreferences sharedPreferences;
    private List<Integer> soundIDs;
    private FrameLayout settingOverlay;
    private View pauseMenuContent;
    private PlayerProgressManager progressManager;
    private int previousRank;
    private ObjectiveManager objectiveManager;
    private int comboCounter = 0;
    private FrameLayout objectivesOverlay;
    private String countText;
    private TextView objCount;
    private TextView liveCount;
    private ImageView objIcon;
    private ImageView livesIcon;
    private TextView countdownTextView;
    private Handler countdownHandler;
    private boolean soundPlayed = false;
    private int countDownId;
    private InventoryManager inventoryManager;
    private long remainingFreezeTime = 0;
    private long freezeStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> Log.e("Leaderboard", "Uncaught exception", throwable));

        PlayGamesSdk.initialize(this);

        progressManager = new PlayerProgressManager(this);
        previousRank = progressManager.getRank();

        sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);

        currencyManager = new CurrencyManager(this);

        objectiveManager = new ObjectiveManager(this);

        inventoryManager = new InventoryManager(this);
        
        inventoryManager.saveInventory();

        freezeCount = inventoryManager.getItemQuantity("powerups", "freeze");

        clearCount = inventoryManager.getItemQuantity("powerups", "clear");

        addTimeCount = inventoryManager.getItemQuantity("powerups", "addTime");

        objectiveManager.startSession();

        objCount = findViewById(R.id.objectiveCountGame);

        countText = String.valueOf(objectiveManager.getCompletedObjectives());

        objCount.setText(countText);

        liveCount = findViewById(R.id.livesCountGame);

        objIcon = findViewById(R.id.objectives_button_game);
        livesIcon = findViewById(R.id.lives_button);

        objIcon.setOnClickListener(v -> showObjectives());

        objectivesOverlay = findViewById(R.id.objectiveOverlayGame);

        pauseMenuOverlay = findViewById(R.id.pauseMenuOverlay);
        gameOverMenuOverlay = findViewById(R.id.gameOverMenuOverlay);
        ImageButton pauseButton = findViewById(R.id.pauseButton);

        pauseMenuContent = getLayoutInflater().inflate(R.layout.activity_pause_menu, pauseMenuOverlay, false);

        pauseButton.setOnClickListener(v -> {
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        checkSignInStatus();
        csvProcessor = new CSVProcessor();
        setupPowerUpButtons();

        gridLayout = findViewById(R.id.gridLayout);

        timerTextView = findViewById(R.id.timer);
        shadow1Timer = findViewById(R.id.shadow1Timer);
        shadow2Timer = findViewById(R.id.shadow2Timer);

        scoreTextView = findViewById(R.id.score);
        shadow1Score = findViewById(R.id.shadow1Score);
        shadow2Score = findViewById(R.id.shadow2Score);

        levelTextView = findViewById(R.id.levelTextView);
        shadow1Level = findViewById(R.id.shadow1Level);
        shadow2Level = findViewById(R.id.shadow2Level);

        beatTiles = new ArrayList<>();
        random = new Random();

        soundPool = new SoundPool.Builder().setMaxStreams(10).build();
        freezeSoundId = soundPool.load(this, R.raw.freeze_sound, 1);
        clearSoundId = soundPool.load(this, R.raw.clear_sound, 1);
        addTimeSoundId = soundPool.load(this, R.raw.add_time_sound, 1);
        tilePressSoundId1 = soundPool.load(this, R.raw.bubble_1, 0);
        tilePressSoundId2 = soundPool.load(this, R.raw.bubble_2, 0);
        lowTimeSoundID = soundPool.load(this, R.raw.clock_ticking, 0);

        soundIDs = new ArrayList<>();

        soundIDs.add(R.raw.freeze_sound);
        soundIDs.add(R.raw.clear_sound);
        soundIDs.add(R.raw.add_time_sound);
        soundIDs.add(R.raw.bubble_1);
        soundIDs.add(R.raw.bubble_2);

        startNewLevel();
    }

    private void showObjectives() {
        setPaused();

        if (objectivesOverlay.getChildCount() == 0) {
            View objectivesContent = getLayoutInflater().inflate(R.layout.objectives_activity, objectivesOverlay, false);
            objectivesOverlay.addView(objectivesContent);

            setUpObjectives(objectivesContent);

            startDynamicCountdown();
        }
        objectivesOverlay.setVisibility(View.VISIBLE);
    }

    private void setUpObjectives(View objectivesContent) {
        LinearLayout objectivesContainer = objectivesContent.findViewById(R.id.objectives_list);
        LinearLayout dailyObjectivesContainer = objectivesContent.findViewById(R.id.daily_objectives_list);

        TextView compObj = objectivesContent.findViewById(R.id.completeObjectiveText);
        TextView compObj1 = objectivesContent.findViewById(R.id.completeObjectiveText1);
        TextView compObj2 = objectivesContent.findViewById(R.id.completeObjectiveText2);

        countdownTextView = objectivesContent.findViewById(R.id.timeLeftObj);

        updateCountdown(countdownTextView);

        objIcon.setVisibility(View.GONE);
        livesIcon.setVisibility(View.GONE);
        objCount.setVisibility(View.GONE);
        liveCount.setVisibility(View.GONE);

        int countNeeded = objectiveManager.getObjectivesRemainingForTierUp();

        String compObjString = "COMPLETE " + countNeeded + " OBJECTIVES TO UNLOCK NEW CHALLENGES";

        compObj.setText(compObjString);
        compObj1.setText(compObjString);
        compObj2.setText(compObjString);

        List<Objective> regularObjectives = objectiveManager.getRegularObjectives();
        for (Objective obj : regularObjectives) {

            addObjectiveToView(objectivesContainer, obj);
        }

        List<Objective> dailyObjectives = objectiveManager.getDailyObjectives();
        for (Objective obj : dailyObjectives) {

            addObjectiveToView(dailyObjectivesContainer, obj);
        }

        ImageView exitButton = objectivesContent.findViewById(R.id.exit_ButtonObj);

        exitButton.setOnClickListener(view -> {
            setResume();

            TextView completedCount = findViewById(R.id.objectiveCountGame);

            objIcon.setVisibility(View.VISIBLE);
            livesIcon.setVisibility(View.VISIBLE);
            objCount.setVisibility(View.VISIBLE);
            liveCount.setVisibility(View.VISIBLE);

            objectivesOverlay.setVisibility(View.GONE);
            objectivesOverlay.removeAllViews();
            countText = String.valueOf(objectiveManager.getCompletedObjectives());
            completedCount.setText(countText);
        });
    }

    private final Runnable unfreezeRunnable = () -> {
        isTimerFrozen = false;
        startGameTimer(0);
        if (soundPlayed) {
            soundPool.resume(countDownId);
        }
    };

    private void startDynamicCountdown() {
        countdownHandler = new Handler();
        Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                countdownHandler.postDelayed(this, 1000); // Update every second
            }
        };
        countdownHandler.post(countdownRunnable); // Start the handler
    }

    private void updateCountdown() {
        long millisUntilReset = objectiveManager.getMillisUntilNextReset();

        if (millisUntilReset < 0) {
            millisUntilReset = 0;
            objectiveManager.startSession(); // Reschedule the next reset if necessary
        }

        int hours = (int) (millisUntilReset / (1000 * 60 * 60));
        int minutes = (int) ((millisUntilReset / (1000 * 60)) % 60);
        int seconds = (int) ((millisUntilReset / 1000) % 60);

        String timeRemaining = String.format("Next Reset: %02d:%02d:%02d", hours, minutes, seconds);
        countdownTextView.setText(timeRemaining);
    }

    private void updateCountdown(TextView countdownTextView) {
        long millisUntilReset = objectiveManager.getMillisUntilNextReset();

        if (millisUntilReset < 0) {
            millisUntilReset = 0;
            objectiveManager.startSession(); // Reschedule the next reset
        }

        int hours = (int) (millisUntilReset / (1000 * 60 * 60));
        int minutes = (int) ((millisUntilReset / (1000 * 60)) % 60);
        int seconds = (int) ((millisUntilReset / 1000) % 60);

        String timeRemaining = String.format("TIME LEFT : %02d:%02d:%02d", hours, minutes, seconds);
        countdownTextView.setText(timeRemaining);
    }

    private void addObjectiveToView(LinearLayout objectivesContainer, Objective objective) {

        View objectivesContent = getLayoutInflater().inflate(R.layout.objectives_entry, objectivesContainer, false);

        CheckBox objectiveCheckbox = objectivesContent.findViewById(R.id.objCheckbox);

        TextView objectiveDescription = objectivesContent.findViewById(R.id.descriptionObj);
        TextView objectiveDescription1 = objectivesContent.findViewById(R.id.descriptionObj1);
        TextView objectiveDescription2 = objectivesContent.findViewById(R.id.descriptionObj2);

        TextView objectiveProgress = objectivesContent.findViewById(R.id.progressTextObj);
        TextView objectiveProgress1 = objectivesContent.findViewById(R.id.progressTextObj1);
        TextView objectiveProgress2 = objectivesContent.findViewById(R.id.progressTextObj2);

        TextView objectiveReward = objectivesContent.findViewById(R.id.rewardTextObj);
        TextView objectiveReward1 = objectivesContent.findViewById(R.id.rewardTextObj1);
        TextView objectiveReward2 = objectivesContent.findViewById(R.id.rewardTextObj2);

        if (objective.getType().equals(ObjectiveType.NO_MISS)) {
            objectiveDescription.setText(objective.getDescription());
            objectiveDescription1.setText(objective.getDescription());
            objectiveDescription2.setText(objective.getDescription());

            objectiveDescription.setTextSize(15);
            objectiveDescription1.setTextSize(15);
            objectiveDescription2.setTextSize(15);
        }

        objectiveDescription.setText(objective.getDescription());
        objectiveDescription1.setText(objective.getDescription());
        objectiveDescription2.setText(objective.getDescription());

        String progressString = objective.getCurrentProgress() + "/" + objective.getTargetAmount() + " " + objective.getTargetDescriptor();

        objectiveProgress.setText(progressString);
        objectiveProgress1.setText(progressString);
        objectiveProgress2.setText(progressString);

        String rewardString = "Reward: " + objective.getRewardXP() + " XP";

        objectiveReward.setText(rewardString);
        objectiveReward1.setText(rewardString);
        objectiveReward2.setText(rewardString);

        objectiveCheckbox.setChecked(objective.isCompleted());

        objectivesContent.setOnClickListener(v ->
        {
            if (objective.isCompleted() && !objective.isClaimed()) {
                int xp = objectiveManager.claimObjectiveReward(objective);
                progressManager.addXP(xp);
                progressManager.saveProgress();
                objectiveManager.saveObjectives();

                objectiveCheckbox.setChecked(true);
                objective.setClaimed(true);
                objectiveReward.setText(R.string.reward_claimed);
                objectiveReward1.setText(R.string.reward_claimed);
                objectiveReward2.setText(R.string.reward_claimed);

                objectiveReward.setTextColor(Color.parseColor("#F9DA65"));

                TextView compObj = findViewById(R.id.completeObjectiveText);
                TextView compObj1 = findViewById(R.id.completeObjectiveText1);
                TextView compObj2 = findViewById(R.id.completeObjectiveText2);

                int countNeeded = objectiveManager.getObjectivesRemainingForTierUp();

                String compObjString = "COMPLETE " + countNeeded + " OBJECTIVES TO UNLOCK NEW CHALLENGES";

                compObj.setText(compObjString);
                compObj1.setText(compObjString);
                compObj2.setText(compObjString);
            }
        });
        objectivesContainer.addView(objectivesContent);
    }

    private void setPaused() {
        isPaused = true;

        if (gameTimer != null) {
            gameTimer.cancel();
            timeRemainingOnPause = timeLeftInMillis;
        }

        if (isTimerFrozen) {
            freezeHandler.removeCallbacks(unfreezeRunnable); // pause freeze timer
            remainingFreezeTime -= (System.currentTimeMillis() - freezeStartTime);
        }


        soundPool.autoPause();
        mediaPlayer.pause();

        gridLayout.setEnabled(false);
        findViewById(R.id.freezePowerUp).setEnabled(false);
        findViewById(R.id.clearPowerUp).setEnabled(false);
        findViewById(R.id.timePowerUp).setEnabled(false);
    }

    private void setResume() {
        isPaused = false;
        soundPool.autoResume();
        mediaPlayer.start();
        timeLeftInMillis = timeRemainingOnPause;

        if (isTimerFrozen && remainingFreezeTime > 0) {
            freezeHandler.postDelayed(unfreezeRunnable, remainingFreezeTime);
        } else {
            startGameTimer(0);
        }

        findViewById(R.id.freezePowerUp).setEnabled(true);
        findViewById(R.id.clearPowerUp).setEnabled(true);
        findViewById(R.id.timePowerUp).setEnabled(true);
    }

    private void pauseGame() {
        isPaused = true;

        objIcon.setVisibility(View.GONE);
        livesIcon.setVisibility(View.GONE);
        objCount.setVisibility(View.GONE);
        liveCount.setVisibility(View.GONE);

        if (pauseMenuOverlay.getChildCount() == 0) {
            pauseMenuOverlay.addView(pauseMenuContent);

            TextView title = pauseMenuContent.findViewById(R.id.pauseTitle);
            applyGradient(title);

            settingOverlay = pauseMenuContent.findViewById(R.id.settingsMenuOverlay);

            pauseMenuContent.findViewById(R.id.resumeButton).setOnClickListener(v -> resumeGame());
            pauseMenuContent.findViewById(R.id.settingsMenuButton).setOnClickListener(v -> {
                pauseMenuContent.findViewById(R.id.resumeButton).setEnabled(false);
                pauseMenuContent.findViewById(R.id.settingsMenuButton).setEnabled(false);
                pauseMenuContent.findViewById(R.id.mainMenuButtonPause).setEnabled(false);

                showSettingsMenu();
            });

            pauseMenuContent.findViewById(R.id.mainMenuButtonPause).setOnClickListener(v -> {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
        if (gameTimer != null) {
            gameTimer.cancel();
            timeRemainingOnPause = timeLeftInMillis;
        }

        if (isTimerFrozen) {
            freezeHandler.removeCallbacks(unfreezeRunnable); // pause freeze timer
            remainingFreezeTime -= (System.currentTimeMillis() - freezeStartTime);
        }

        pauseMenuOverlay.setVisibility(View.VISIBLE);

        soundPool.autoPause();
        mediaPlayer.pause();

        gridLayout.setEnabled(false);
        findViewById(R.id.freezePowerUp).setEnabled(false);
        findViewById(R.id.clearPowerUp).setEnabled(false);
        findViewById(R.id.timePowerUp).setEnabled(false);
    }

    private void resumeGame() {
        isPaused = false;

        objIcon.setVisibility(View.VISIBLE);
        livesIcon.setVisibility(View.VISIBLE);
        objCount.setVisibility(View.VISIBLE);
        liveCount.setVisibility(View.VISIBLE);

        pauseMenuOverlay.setVisibility(View.GONE);
        soundPool.autoResume();
        mediaPlayer.start();
        timeLeftInMillis = timeRemainingOnPause;

        if (isTimerFrozen && remainingFreezeTime > 0) {
            freezeHandler.postDelayed(unfreezeRunnable, remainingFreezeTime);
        } else {
            startGameTimer(0);
        }

        findViewById(R.id.freezePowerUp).setEnabled(true);
        findViewById(R.id.clearPowerUp).setEnabled(true);
        findViewById(R.id.timePowerUp).setEnabled(true);
    }

    private void showSettingsMenu() {
        if (settingOverlay.getChildCount() == 0) {
            View settingsMenuContent = getLayoutInflater().inflate(R.layout.settings_activity, settingOverlay, false);
            settingOverlay.addView(settingsMenuContent);

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
        ImageView exitButton = settingsMenuContent.findViewById(R.id.exit_ButtonSettings);
        TextView settingsSavedMessage = settingsMenuContent.findViewById(R.id.save_success_text);
        TextView soundEffectPercent = settingsMenuContent.findViewById(R.id.sound_effects_percentage);
        TextView musicPercent = settingsMenuContent.findViewById(R.id.music_percentage);

        View oldExit = settingsMenuContent.findViewById(R.id.exitLayout);

        oldExit.setVisibility(View.GONE);

        loadSettings(musicSeekBar, soundEffectsSeekBar, musicCheckBox, soundEffectsCheckBox, soundEffectPercent, musicPercent);

        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String musicText = progress + "%";
                musicPercent.setText(musicText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        soundEffectsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String soundEffectText = progress + "%";
                soundEffectPercent.setText(soundEffectText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        musicCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            musicSeekBar.setEnabled(isChecked);
            if (isChecked) {
                int musicVolume = sharedPreferences.getInt("musicVolume", 100);
                musicSeekBar.setProgress(musicVolume);
                String musicPercentString = musicVolume + "%";
                musicPercent.setText(musicPercentString);
            } else {
                musicSeekBar.setProgress(0);
                musicPercent.setText("0%");
            }
        });

        soundEffectsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEffectsSeekBar.setEnabled(isChecked);
            if (isChecked) {
                int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
                soundEffectsSeekBar.setProgress(soundEffectsVolume);
                String soundEffectPercentString = soundEffectsVolume + "%";
                soundEffectPercent.setText(soundEffectPercentString);
            } else {
                soundEffectsSeekBar.setProgress(0);
                soundEffectPercent.setText("0%");
            }
        });

        saveButton.setOnClickListener(view -> saveSettings(musicSeekBar, soundEffectsSeekBar, musicCheckBox, soundEffectsCheckBox,
                settingsSavedMessage, soundEffectPercent, musicPercent));

        exitButton.setOnClickListener(view -> {

            pauseMenuContent.findViewById(R.id.resumeButton).setEnabled(true);
            pauseMenuContent.findViewById(R.id.settingsMenuButton).setEnabled(true);
            pauseMenuContent.findViewById(R.id.mainMenuButtonPause).setEnabled(true);
            settingOverlay.setVisibility(View.GONE);
        });
    }

    private void loadSettings(SeekBar musicSeekBar, SeekBar soundEffectsSeekBar, CheckBox musicCheckBox, CheckBox soundEffectsCheckBox, TextView soundEffectPercent, TextView musicPercent) {
        int musicVolume = sharedPreferences.getInt("musicVolume", 100);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
        boolean isMusicOn = sharedPreferences.getBoolean("isMusicOn", true);
        boolean isSoundEffectsOn = sharedPreferences.getBoolean("isSoundEffectsOn", true);

        musicSeekBar.setProgress(isMusicOn ? musicVolume : 0);
        soundEffectsSeekBar.setProgress(isSoundEffectsOn ? soundEffectsVolume : 0);
        musicCheckBox.setChecked(isMusicOn);
        soundEffectsCheckBox.setChecked(isSoundEffectsOn);

        musicSeekBar.setEnabled(isMusicOn);
        soundEffectsSeekBar.setEnabled(isSoundEffectsOn);

        String soundEffectText = soundEffectsVolume + "%";
        String musicText = musicVolume + "%";
        soundEffectPercent.setText(isSoundEffectsOn ? soundEffectText : "0%");
        musicPercent.setText(isMusicOn ? musicText : "0%");
    }

    private void saveSettings(SeekBar musicSeekBar, SeekBar soundEffectsSeekBar, CheckBox musicCheckBox, CheckBox soundEffectsCheckBox, TextView settingsSavedMessage, TextView soundEffectPercent, TextView musicPercent) {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int musicVolume = musicCheckBox.isChecked() ? musicSeekBar.getProgress() : 0;
        int soundEffectsVolume = soundEffectsCheckBox.isChecked() ? soundEffectsSeekBar.getProgress() : 0;
        boolean isMusicOn = musicCheckBox.isChecked();
        boolean isSoundEffectsOn = soundEffectsCheckBox.isChecked();

        editor.putInt("musicVolume", musicVolume);
        editor.putInt("soundEffectsVolume", soundEffectsVolume);
        editor.putBoolean("isMusicOn", isMusicOn);
        editor.putBoolean("isSoundEffectsOn", isSoundEffectsOn);
        editor.apply();

        String soundEffectText = soundEffectsVolume + "%";
        String musicText = musicVolume + "%";
        soundEffectPercent.setText(soundEffectText);
        musicPercent.setText(musicText);

        settingsSavedMessage.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> settingsSavedMessage.setVisibility(View.GONE), 2000);

        applySettings();
    }

    private void applySettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        int musicVolume = sharedPreferences.getInt("musicVolume", 100);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);

        for (Integer ids : soundIDs) {
            soundPool.setVolume(ids, soundEffectsVolume, soundEffectsVolume);
        }
        mediaPlayer.setVolume(musicVolume / 100f, musicVolume / 100f);
    }

    private int calculateScore(int level, long remainingTimeInMillis, int accuracy) {
        int basePoints = level * 10;
        int speedBonus = (int) (remainingTimeInMillis / 1000) * 2;

        float accuracyMultiplier;
        if (accuracy >= 95) {
            accuracyMultiplier = 2.0f;
        } else if (accuracy >= 85) {
            accuracyMultiplier = 1.5f;
        } else if (accuracy >= 70) {
            accuracyMultiplier = 1.2f;
        } else {
            accuracyMultiplier = 1.0f;
        }
        return (int) ((basePoints + speedBonus) * accuracyMultiplier);
    }

    private void startNewLevel() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        csvProcessor.loadCSV(this, R.raw.tile_positions);
        getSong();
        currentVariation = getVariationForLevel(currentLevel, currentSong);

        beatTiles.clear();
        gridLayout.removeAllViews();

        Log.e("Correct Taps", "Taps for round: " + correctTaps);

        int accuracy = (int) (((double) correctTaps / totalTaps) * 100);

        int levelScore = calculateScore(currentLevel, timeLeftInMillis, accuracy);

        totalScore += levelScore;

        int targetDaily = objectiveManager.getObjectiveByType(ObjectiveType.ACHIEVE_COMBO, "daily").getTargetAmount();

        if (comboCounter >= targetDaily) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.ACHIEVE_COMBO, targetDaily, "daily");
        }

        if (accuracy == 100) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.NO_MISS, 1, "daily");
        }

        if (currencyManager.getBeatCoins() == objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS, "daily").getTargetAmount()) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.COLLECT_COINS, objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS, "daily").getTargetAmount(), "daily");
        }

        int target = objectiveManager.getObjectiveByType(ObjectiveType.ACHIEVE_COMBO, "regular").getTargetAmount();

        if (comboCounter >= target) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.ACHIEVE_COMBO, target, "regular");
        }

        if (accuracy == 100) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.NO_MISS, 1, "regular");
        }

        if (currencyManager.getBeatCoins() == objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS, "regular").getTargetAmount()) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.COLLECT_COINS, objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS, "regular").getTargetAmount(), "regular");
        }

        if (isTimerFrozen) {
            freezeHandler.removeCallbacks(unfreezeRunnable);
            isTimerFrozen = false;
        }

        setupGrid();
        startGameTimer(5000);
        updatePowerUpCounts();

        String level = "LVL " + currentLevel;
        levelTextView.setText(level);
        shadow1Level.setText(level);
        shadow2Level.setText(level);

        String scoreText = "SCORE: " + totalScore;
        scoreTextView.setText(scoreText);
        shadow1Score.setText(scoreText);
        shadow2Score.setText(scoreText);

        playBackgroundTrack();

        applySettings();

        objectiveManager.updateObjectiveProgress(ObjectiveType.TAP_TILES, correctTaps, "regular");
        objectiveManager.updateObjectiveProgress(ObjectiveType.USE_POWERUPS, powerUpUse, "regular");
        objectiveManager.updateObjectiveProgress(ObjectiveType.CLEAR_LEVELS, 1, "regular");

        objectiveManager.updateObjectiveProgress(ObjectiveType.TAP_TILES, correctTaps, "daily");
        objectiveManager.updateObjectiveProgress(ObjectiveType.USE_POWERUPS, powerUpUse, "daily");
        objectiveManager.updateObjectiveProgress(ObjectiveType.CLEAR_LEVELS, 1, "daily");

        TextView completedCount = findViewById(R.id.objectiveCountGame);

        objectivesOverlay.setVisibility(View.GONE);
        objectivesOverlay.removeAllViews();
        countText = String.valueOf(objectiveManager.getCompletedObjectives());
        completedCount.setText(countText);

        correctTaps = 0;
        totalTaps = 0;
        powerUpUse = 0;


        soundPool.stop(countDownId);
        soundPlayed = false;
    }

    private void applyGradient(TextView title) {
        title.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (title.getWidth() > 0 && title.getHeight() > 0) {
                TextPaint paint = title.getPaint();
                float width = paint.measureText(title.getText().toString());

                LinearGradient shader = new LinearGradient(
                        0, 0, width, 0,
                        new int[]{
                                Color.parseColor("#D93232"),
                                Color.parseColor("#9504AC"),
                                Color.parseColor("#FFFFFF")
                        },
                        new float[]{0f, 0.34f, 1f},
                        Shader.TileMode.CLAMP);

                paint.setShader(shader);
                title.setTextColor(Color.WHITE); // Set a base color
                title.invalidate();
            }
        });
    }

    private void setupGrid() {
        int gridRows = 16;
        gridLayout.setRowCount(gridRows);

        int gridColumns = 6;
        gridLayout.setColumnCount(gridColumns);

        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridColumns; j++) {
                Button tileButton = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                tileButton.setLayoutParams(params);

                int finalI = i;
                int finalJ = j;
                tileButton.setOnClickListener(view -> onTileTapped(tileButton, finalI, finalJ));

                BeatTile tile = new BeatTile(i, j);

                beatTiles.add(tile);

                gridLayout.addView(tileButton);
            }
        }

        String songId;
        if (currentSong.equalsIgnoreCase("string_dance")) {
            songId = "sd";
        } else songId = "am";
        List<Integer> currentTilePositions = csvProcessor.getTilePositions(songId, currentLevel, currentVariation);

        if (currentTilePositions != null) {
            for (int index : currentTilePositions) {
                BeatTile tile = beatTiles.get(index);
                tile.setInitiallyToggled(true);
                tile.toggle();
                int row = index / (gridColumns);
                int col = index % (gridColumns);
                View view = gridLayout.getChildAt(row * gridColumns + col);
                if (view instanceof Button) {
                    Button tileButton = (Button) view;
                    onTileTapped(tileButton, tile.getX(), tile.getY());
                }
            }
        } else {
            Log.e("Tile Positions", "Error: currentTilePositions is null for songId: " + songId + ", currentLevel: " + currentLevel + ", currentVariation: " + currentVariation);
        }
    }

    private void playBackgroundTrack() {
        int level = currentLevel;

        if (currentLevel > 14 && !(currentLevel % 6 == 0)) {
            level = 14;
        } else if (currentLevel % 6 == 0) {
            level = 1;
        }

        String trackName = currentSong + "_level_" + level + "_v" + currentVariation;
        @SuppressLint("DiscouragedApi") int trackResId = getResources().getIdentifier(trackName, "raw", getPackageName());

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, trackResId);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private int getVariationForLevel(int level, String currentSong) {
        if (currentSong.equalsIgnoreCase("angelic_melody") && level % 6 == 0) {
            return random.nextInt((2 - 1) + 1) + 1;
        } else if (currentSong.equalsIgnoreCase("string_dance") && level % 6 == 0) {
            return random.nextInt((3 - 1) + 1) + 1;
        } else if (currentSong.equalsIgnoreCase("angelic_melody") && level < 14) {
            return random.nextInt((2 - 1) + 1) + 1;
        } else if (currentSong.equalsIgnoreCase("angelic_melody") ||
                currentSong.equalsIgnoreCase("string_dance") && currentLevel >= 14 && !(level % 6 == 0)) {
            return random.nextInt((7 - 1) + 1) + 1;
        } else return random.nextInt((3 - 1) + 1) + 1;
    }

    private void startGameTimer(int timeToAdd) {
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        timeLeftInMillis += timeToAdd;

        gameTimer = new CountDownTimer(timeLeftInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                submitScoreToLeaderboard(totalScore);
                inventoryManager.saveInventory();
                showGameOver();
            }
        }.start();
    }

    private void updateTimerText() {
        int minutes = (int) ((timeLeftInMillis) / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        @SuppressLint("DefaultLocale") String timeFormatted = String.format("%2d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
        shadow1Timer.setText(timeFormatted);
        shadow2Timer.setText(timeFormatted);

        if (timeLeftInMillis <= 5999) {
            timerTextView.setTextColor(Color.parseColor("#D93232"));
            if (!soundPlayed) {
                int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
                countDownId = soundPool.play(lowTimeSoundID, soundEffectsVolume / 100f, soundEffectsVolume / 100f, 0, 0, 1);
                soundPlayed = true;
            }
        } else {
            timerTextView.setTextColor(Color.WHITE);
            if (soundPlayed) {
                soundPool.stop(countDownId);
                soundPlayed = false;
            }
        }
    }

    private void getSong() {
        boolean result = random.nextBoolean();
        if (result) {
            currentSong = "angelic_melody";
        } else {
            currentSong = "string_dance";
        }
    }

    private void playSound(int id) {
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
        soundPool.play(id, soundEffectsVolume / 100f, soundEffectsVolume / 100f, 0, 0, 1);
    }

    private void onTileTapped(Button tileButton, int x, int y) {
        for (BeatTile tile : beatTiles) {
            if (tile.getX() == x && tile.getY() == y) {
                if (!tile.isInitiallyToggled()) {
                    totalTaps++;
                    comboCounter = 0;
                    return;
                }
                if (tile.isActive()) {
                    tile.toggle();
                    tileButton.setBackgroundColor(Color.TRANSPARENT);
                    int soundToPlay = random.nextBoolean() ? tilePressSoundId1 : tilePressSoundId2;
                    playSound(soundToPlay);
                    correctTaps++;
                    comboCounter++;
                    totalTaps++;

                } else {
                    tile.toggle();
                    tileButton.setBackgroundColor(ROW_COLORS[y]);
                }
            }
        }

        if (areAllTilesInactive()) {
            gameTimer.cancel();
            nextLevel();
        }
    }

    private boolean areAllTilesInactive() {
        for (BeatTile tile : beatTiles) {
            if (tile.isActive() && tile.isInitiallyToggled()) {
                return false;
            }
        }
        return true;
    }


    private void showGameOver() {
        objIcon.setVisibility(View.GONE);
        livesIcon.setVisibility(View.GONE);
        objCount.setVisibility(View.GONE);
        liveCount.setVisibility(View.GONE);

        objectiveManager.endSession();

        if (gameOverMenuOverlay.getChildCount() == 0) {
            View gameOverMenuContent = getLayoutInflater().inflate(R.layout.activity_game_over, gameOverMenuOverlay, false);
            gameOverMenuOverlay.addView(gameOverMenuContent);

            int xpGained = totalScore / 10;
            progressManager.addXP(xpGained);

            if (progressManager.getRank() > previousRank) {
                showRankUpNotification();
            }
            progressManager.saveProgress();

            TextView title = gameOverMenuContent.findViewById(R.id.gameOverTitle);

            TextView levelReached = findViewById(R.id.gameOverLevel);
            TextView levelReached1 = findViewById(R.id.gameOverLevel1);
            TextView levelReached2 = findViewById(R.id.gameOverLevel2);

            TextView lastScore = findViewById(R.id.gameOverScore);
            TextView lastScore1 = findViewById(R.id.gameOverScore1);
            TextView lastScore2 = findViewById(R.id.gameOverScore2);

            TextView rankView = findViewById(R.id.rankTextOver);
            TextView rankView1 = findViewById(R.id.gameOverRank1);
            TextView rankView2 = findViewById(R.id.gameOverRank2);

            TextView xpView = findViewById(R.id.xpText);
            TextView xpView1 = findViewById(R.id.xpText1);
            TextView xpView2 = findViewById(R.id.xpText2);

            String level = "LEVEL " + currentLevel;

            levelReached.setText(level);
            levelReached1.setText(level);
            levelReached2.setText(level);

            String score = "SCORE: " + totalScore;

            lastScore.setText(score);
            lastScore1.setText(score);
            lastScore2.setText(score);

            String rank = "RANK " + progressManager.getRank();

            rankView.setText(rank);
            rankView1.setText(rank);
            rankView2.setText(rank);

            String xp = "XP " + xpGained;

            xpView.setText(xp);
            xpView1.setText(xp);
            xpView2.setText(xp);

            applyGradient(title);

            new Handler().postDelayed(() -> {
                gameOverMenuContent.findViewById(R.id.playAgainButton).setOnClickListener(v -> {
                    Intent intent = new Intent(GameActivity.this, GameActivity.class);
                    startActivity(intent);
                    finish();
                });
                gameOverMenuContent.findViewById(R.id.mainMenuButtonOver).setOnClickListener(v -> {
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }, 1000);
        }

        gameOverMenuOverlay.setVisibility(View.VISIBLE);

        soundPool.release();
        mediaPlayer.stop();
        gameTimer.cancel();

        gridLayout.setEnabled(false);
        findViewById(R.id.freezePowerUp).setEnabled(false);
        findViewById(R.id.clearPowerUp).setEnabled(false);
        findViewById(R.id.timePowerUp).setEnabled(false);
    }

    private void showRankUpNotification() {
        TextView rankUp = findViewById(R.id.rankUpText);
        TextView rankUp1 = findViewById(R.id.gameOverRankUp1);
        TextView rankUp2 = findViewById(R.id.gameOverRankUp2);

        rankUp.setVisibility(View.VISIBLE);
        rankUp1.setVisibility(View.VISIBLE);
        rankUp2.setVisibility(View.VISIBLE);
    }


    private void submitScoreToLeaderboard(int score) {
        if (PlayGames.getGamesSignInClient(this).isAuthenticated().isSuccessful()) {
            PlayGames.getLeaderboardsClient(this)
                    .submitScore(getString(R.string.leaderboard_id), score);
        } else {
            Log.e("Leaderboard", "User is not signed in, cannot submit score.");
        }
    }

    private void checkSignInStatus() {
        PlayGames.getGamesSignInClient(this).isAuthenticated()
                .addOnCompleteListener(task -> {
                    boolean isSignedIn = task.isSuccessful() && task.getResult().isAuthenticated();
                    if (isSignedIn) {
                        Log.d("Leaderboard", "User is signed in");

                    } else {
                        Log.e("Leaderboard", "User is not signed in");
                    }
                });
    }

    private void nextLevel() {
        currentLevel++;
        startNewLevel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private void setupPowerUpButtons() {
        findViewById(R.id.freezePowerUp).setOnClickListener(v -> useFreezePowerUp());

        findViewById(R.id.clearPowerUp).setOnClickListener(v -> useClearPowerUp());

        findViewById(R.id.timePowerUp).setOnClickListener(v -> useAddTimePowerUp());
    }

    private void useFreezePowerUp() {
        if (freezeCount > 0 && !isTimerFrozen) {
            soundPool.pause(countDownId);
            freezeCount--;
            isTimerFrozen = true;
            gameTimer.cancel();
            playSound(freezeSoundId);

            freezeStartTime = System.currentTimeMillis();
            remainingFreezeTime = 10000;
            freezeHandler.postDelayed(unfreezeRunnable, remainingFreezeTime);

            updatePowerUpCounts();
            powerUpUse++;

        }
    }

    private void useClearPowerUp() {
        if (clearCount > 0) {
            clearCount--;
            currentLevel++;
            gameTimer.cancel();
            playSound(clearSoundId);
            startNewLevel();
            updatePowerUpCounts();
            powerUpUse++;
        }
    }

    private void useAddTimePowerUp() {
        if (addTimeCount > 0) {
            addTimeCount--;
            gameTimer.cancel();
            playSound(addTimeSoundId);
            startGameTimer(15000);
            updatePowerUpCounts();
            powerUpUse++;
        }
    }

    private void updatePowerUpCounts() {
        ((TextView) findViewById(R.id.freezeCount)).setText(String.valueOf(freezeCount));
        inventoryManager.updateItemQuantity("powerups", "freeze", freezeCount);
        ((TextView) findViewById(R.id.clearCount)).setText(String.valueOf(clearCount));
        inventoryManager.updateItemQuantity("powerups", "clear", clearCount);
        ((TextView) findViewById(R.id.addCount)).setText(String.valueOf(addTimeCount));
        inventoryManager.updateItemQuantity("powerups", "addTime", addTimeCount);
    }
}





