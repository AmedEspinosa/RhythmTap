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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private CurrencyManager currencyManager;
    private TextView beatCoinsTextView;
    private TextView beatCoinsTextView1;
    private TextView beatCoinsTextView2;
    private FrameLayout settingOverlay;
    private PlayerProgressManager progressManager;
    private ProgressBar xpProgressBar;
    private int progress;
    private TextView rankTextView;
    private TextView rankTextView1;
    private TextView rankTextView2;
    private int currentRank;
    private FrameLayout rankUpOverLay;
    private FrameLayout rankViewOverlay;
    private FrameLayout objectivesOverlay;
    private ObjectiveManager objectiveManager;
    private LinearLayout dailyObjectivesContainer;
    private boolean hasRankedUp = false;
    private SharedPreferences sharedPreferences;
    private ImageView objectivesButton;
    private String countText;
    private TextView countdownTextView;
    private Handler countdownHandler;
    private Runnable countdownRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressManager = new PlayerProgressManager(this);

        currentRank = progressManager.getRank();

        sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);

        xpProgressBar = findViewById(R.id.customProgressBar);
        FrameLayout rankButton = findViewById(R.id.rankButton);
        rankTextView = findViewById(R.id.rankText);
        rankTextView1 = findViewById(R.id.shadow1rankText);
        rankTextView2 = findViewById(R.id.shadow2rankText);

        TextView title = findViewById(R.id.title);

        currencyManager = new CurrencyManager(this);

        settingOverlay = findViewById(R.id.settingsOverlay);

        rankUpOverLay = findViewById(R.id.rankUpOverlay);

        rankViewOverlay = findViewById(R.id.rankViewOverlay);

        objectivesButton = findViewById(R.id.objectives_button);

        objectivesOverlay = findViewById(R.id.objectivesOverlay);

        objectiveManager = new ObjectiveManager(this);

        //objectiveManager.resetObjectives();

        View settingButton = findViewById(R.id.settingButton);

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


        TextView completedCount = findViewById(R.id.objectiveCount);

        countText = String.valueOf(objectiveManager.getCompletedObjectives());


        completedCount.setText(countText);

        objectivesButton.setOnClickListener(v -> showObjectives());

        xpProgressBar.setOnClickListener(v -> showRankView());

        rankButton.setOnClickListener(v -> showRankView());

        settingButton.setOnClickListener(v -> showSettingsMenu());

        leaderboardButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        playButton.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("originalRank", currentRank);
            editor.apply();

            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        storeButton.setOnClickListener(view -> {
            // Handle shop button click
        });
        updateUI();
    }

    private void showObjectives() {
        BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
        if (bouncingSquaresView != null) {
            bouncingSquaresView.pauseBouncing(); // Pause the BouncingSquaresView
        }

        if (objectivesOverlay.getChildCount() == 0) {
            View objectivesContent = getLayoutInflater().inflate(R.layout.objectives_activity, objectivesOverlay, false);
            objectivesOverlay.addView(objectivesContent);


            setUpObjectives(objectivesContent);

            startDynamicCountdown();


        }

        objectivesOverlay.setVisibility(View.VISIBLE);
        Log.d("ObjectivesDebug", "Objectives Overlay should now be visible.");
    }

    private void startDynamicCountdown() {
        countdownHandler = new Handler();
        countdownRunnable = new Runnable() {
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

    private void stopDynamicCountdown() {
        if (countdownHandler != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }



    private void showRankView() {
        BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
        if (bouncingSquaresView != null) {
            bouncingSquaresView.pauseBouncing(); // Pause the BouncingSquaresView
        }
        if (rankViewOverlay.getChildCount() == 0) {
            View rankMenuContent = getLayoutInflater().inflate(R.layout.activity_rank_up, rankViewOverlay, false);
            rankViewOverlay.addView(rankMenuContent);

            setUpRankMenu(rankMenuContent);
        }

        rankViewOverlay.setVisibility(View.VISIBLE);
        Log.d("RankDebug", "Rank Up Overlay should now be visible.");
    }

    private void showRankUpMenu() {
        BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
        if (bouncingSquaresView != null) {
            bouncingSquaresView.pauseBouncing();
        }
        if (rankUpOverLay.getChildCount() == 0) {
            View rankUpMenuContent = getLayoutInflater().inflate(R.layout.activity_rank_up, rankUpOverLay, false);
            rankUpOverLay.addView(rankUpMenuContent);

            setUpRankUpMenu(rankUpMenuContent);
        }

        rankUpOverLay.setVisibility(View.VISIBLE);
        Log.d("RankDebug", "Rank Up Overlay should now be visible.");
    }

    private void showSettingsMenu() {

        BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
        if (bouncingSquaresView != null) {
            bouncingSquaresView.pauseBouncing();
        }

        if (settingOverlay.getChildCount() == 0) {
            View settingsMenuContent = getLayoutInflater().inflate(R.layout.settings_activity, settingOverlay, false);
            settingOverlay.addView(settingsMenuContent);

            setupSettingsMenu(settingsMenuContent);
        }
        settingOverlay.setVisibility(View.VISIBLE);
    }


    private void setUpObjectives(View objectivesContent) {
        LinearLayout objectivesContainer = objectivesContent.findViewById(R.id.objectives_list);
        dailyObjectivesContainer = objectivesContent.findViewById(R.id.daily_objectives_list);



        TextView compObj = objectivesContent.findViewById(R.id.completeObjectiveText);
        TextView compObj1 = objectivesContent.findViewById(R.id.completeObjectiveText1);
        TextView compObj2 = objectivesContent.findViewById(R.id.completeObjectiveText2);

        countdownTextView = objectivesContent.findViewById(R.id.timeLeftObj);

        updateCountdown(countdownTextView);



        Objective rankObj = objectiveManager.getObjectiveByType(ObjectiveType.REACH_RANK,"regular");
        Objective rankObjDaily = objectiveManager.getObjectiveByType(ObjectiveType.REACH_RANK,"daily");


        if (rankObj.getType() != null) {
            if (currentRank >= rankObj.getTargetAmount()) {
                objectiveManager.updateObjectiveProgress(ObjectiveType.REACH_RANK,currentRank,"regular");
            }
        }

        if (rankObjDaily.getType() != null) {
            if (currentRank >= rankObjDaily.getTargetAmount()) {
                objectiveManager.updateObjectiveProgress(ObjectiveType.REACH_RANK,currentRank,"daily");
            }
        }

        if (currencyManager.getBeatCoins() == objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS,"regular").getTargetAmount()) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.COLLECT_COINS, objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS,"regular").getTargetAmount(),"regular");
        }

        if (currencyManager.getBeatCoins() == objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS,"daily").getTargetAmount()) {
            objectiveManager.updateObjectiveProgress(ObjectiveType.COLLECT_COINS, objectiveManager.getObjectiveByType(ObjectiveType.COLLECT_COINS,"daily").getTargetAmount(),"daily");
        }

        int countNeeded = objectiveManager.getCountNeededForTier();

        String compObjString = "COMPLETE " + countNeeded  + " OBJECTIVES TO UNLOCK NEW CHALLENGES";

        compObj.setText(compObjString);
        compObj1.setText(compObjString);
        compObj2.setText(compObjString);


        List<Objective> regularObjectives = objectiveManager.getRegularObjectives();
        for (Objective obj : regularObjectives) {
            Log.d("ObjectiveManager", "Adding objective to view: " + obj.getDescription());

            addObjectiveToView(objectivesContainer, obj);
        }

        List<Objective> dailyObjectives = objectiveManager.getDailyObjectives();
        for (Objective obj: dailyObjectives) {
            addObjectiveToView(dailyObjectivesContainer,obj);
        }

        ImageView exitButton = objectivesContent.findViewById(R.id.exit_ButtonObj);

        exitButton.setOnClickListener(view -> {
            BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
            if (bouncingSquaresView != null) {
                bouncingSquaresView.resumeBouncing();
            }

            TextView completedCount = findViewById(R.id.objectiveCount);

            objectivesOverlay.setVisibility(View.GONE);
            objectivesOverlay.removeAllViews();
            countText = String.valueOf(objectiveManager.getCompletedObjectives());
            completedCount.setText(countText);

        });

    }

    private void updateCountdown(TextView countdownTextView) {
        long millisUntilReset = objectiveManager.getMillisUntilNextReset();

        if (millisUntilReset < 0) {
            // If time is negative, force it to 0 and set the next reset time
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
            int xp = objectiveManager.claimObjectiveReward(objective,objective.getClassification());
            progressManager.addXP(xp);
            progressManager.saveProgress();
            objectiveManager.saveObjectives();

            objectiveCheckbox.setChecked(true);
            objective.setClaimed(true);
            objectiveReward.setText("Reward Claimed");
            objectiveReward1.setText("Reward Claimed");
            objectiveReward2.setText("Reward Claimed");

            TextView compObj = findViewById(R.id.completeObjectiveText);
            TextView compObj1 = findViewById(R.id.completeObjectiveText1);
            TextView compObj2 = findViewById(R.id.completeObjectiveText2);

            int countNeeded = objectiveManager.getCountNeededForTier();

            String compObjString = "COMPLETE " + countNeeded  + " OBJECTIVES TO UNLOCK NEW CHALLENGES";

            compObj.setText(compObjString);
            compObj1.setText(compObjString);
            compObj2.setText(compObjString);

        }
    });

        objectivesContainer.addView(objectivesContent);


}

    private void setUpRankMenu(View rankUpMenuContent) {
        ProgressBar bar = rankUpMenuContent.findViewById(R.id.customProgressBarRankUp);

        TextView xpProgress = rankUpMenuContent.findViewById(R.id.xpProgress);
        TextView xpProgress1 = rankUpMenuContent.findViewById(R.id.xpProgress1);
        TextView xpProgress2 = rankUpMenuContent.findViewById(R.id.xpProgress2);

        TextView rankUpText = rankUpMenuContent.findViewById(R.id.rankTextMenu);
        TextView rankUpText1 = rankUpMenuContent.findViewById(R.id.rankTextMenu1);
        TextView rankUpText2 = rankUpMenuContent.findViewById(R.id.rankTextMenu2);

        FrameLayout rankUpTextLayout = rankUpMenuContent.findViewById(R.id.rankUpTextLayout);

        FrameLayout rewardsTextLayout = rankUpMenuContent.findViewById(R.id.rewardsTextLayout);

        View resumeButton = rankUpMenuContent.findViewById(R.id.resumeButtonLayout);

        resumeButton.setVisibility(View.GONE);

        rankUpTextLayout.setVisibility(View.GONE);

        rewardsTextLayout.setVisibility(View.GONE);


        FrameLayout rankViewUpcomingText = rankUpMenuContent.findViewById(R.id.rankViewUpcomingText);
        TextView nextRank = rankUpMenuContent.findViewById(R.id.nextRank);
        TextView nextRank1 = rankUpMenuContent.findViewById(R.id.nextRank1);
        TextView nextRank2 = rankUpMenuContent.findViewById(R.id.nextRank2);

        String nextRankText = String.valueOf(progressManager.getRank() + 1);

        nextRank.setText(nextRankText);
        nextRank1.setText(nextRankText);
        nextRank2.setText(nextRankText);

        rankViewUpcomingText.setVisibility(View.VISIBLE);

        FrameLayout rankViewTitleLayout = rankUpMenuContent.findViewById(R.id.rankViewTitleLayout);

        FrameLayout rankViewUpcoming = rankUpMenuContent.findViewById(R.id.rankViewUpcoming);

        rankViewTitleLayout.setVisibility(View.VISIBLE);

        rankViewUpcoming.setVisibility(View.VISIBLE);

        View continueButton = rankUpMenuContent.findViewById(R.id.exit_ButtonRank);

        continueButton.setVisibility(View.VISIBLE);

        bar.setProgress(progress);

        String progressText = progress + "/" + progressManager.getNextLevelXP();

        xpProgress.setText(progressText);
        xpProgress1.setText(progressText);
        xpProgress2.setText(progressText);

        String rankString = String.valueOf(progressManager.getRank());

        rankUpText.setText(rankString);
        rankUpText1.setText(rankString);
        rankUpText2.setText(rankString);

        continueButton.setOnClickListener(view -> {
            BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
            if (bouncingSquaresView != null) {
                bouncingSquaresView.resumeBouncing(); // Resume the BouncingSquaresView
            }

            rankViewOverlay.setVisibility(View.GONE);
        });
    }

    private void setUpRankUpMenu(View rankUpMenuContent) {
        ProgressBar bar = rankUpMenuContent.findViewById(R.id.customProgressBarRankUp);

        TextView xpProgress = rankUpMenuContent.findViewById(R.id.xpProgress);
        TextView xpProgress1 = rankUpMenuContent.findViewById(R.id.xpProgress1);
        TextView xpProgress2 = rankUpMenuContent.findViewById(R.id.xpProgress2);

        TextView rankUpText = rankUpMenuContent.findViewById(R.id.rankTextMenu);
        TextView rankUpText1 = rankUpMenuContent.findViewById(R.id.rankTextMenu1);
        TextView rankUpText2 = rankUpMenuContent.findViewById(R.id.rankTextMenu2);

        View continueButton = rankUpMenuContent.findViewById(R.id.resumeButtonRank);

        bar.setProgress(progress);

        String progressText = progress + "/" + progressManager.getNextLevelXP();

        xpProgress.setText(progressText);
        xpProgress1.setText(progressText);
        xpProgress2.setText(progressText);

        String rankString = String.valueOf(progressManager.getRank());

        rankUpText.setText(rankString);
        rankUpText1.setText(rankString);
        rankUpText2.setText(rankString);

        continueButton.setOnClickListener(view -> {
            BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
            if (bouncingSquaresView != null) {
                bouncingSquaresView.resumeBouncing();
            }

            rankUpOverLay.setVisibility(View.GONE);
        });
    }



    private void setupSettingsMenu(View settingsMenuContent) {
        SeekBar musicSeekBar = settingsMenuContent.findViewById(R.id.music_volume);
        SeekBar soundEffectsSeekBar = settingsMenuContent.findViewById(R.id.sound_effects_volume);
        CheckBox musicCheckBox = settingsMenuContent.findViewById(R.id.musicCheckBox);
        CheckBox soundEffectsCheckBox = settingsMenuContent.findViewById(R.id.soundEffectCheckBox);
        View saveButton = settingsMenuContent.findViewById(R.id.saveButton);
        ImageView exitButton = settingsMenuContent.findViewById(R.id.exit_ButtonSettings);
        View closeAppButton = settingsMenuContent.findViewById(R.id.exitButton);
        TextView settingsSavedMessage = settingsMenuContent.findViewById(R.id.save_success_text);
        TextView soundEffectPercent = settingsMenuContent.findViewById(R.id.sound_effects_percentage);
        TextView musicPercent = settingsMenuContent.findViewById(R.id.music_percentage);
        TextView ioSoundEffect = settingsMenuContent.findViewById(R.id.IOSoundEffect);
        TextView ioMusic = settingsMenuContent.findViewById(R.id.IOMusic);


        loadSettings(musicSeekBar, soundEffectsSeekBar, musicCheckBox, soundEffectsCheckBox, soundEffectPercent, musicPercent, ioSoundEffect, ioMusic);

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
                String musicPercentText = musicVolume + "%";
                musicPercent.setText(musicPercentText);
                ioMusic.setText("ON");
            } else {
                musicSeekBar.setProgress(0);
                musicPercent.setText("0%");
                ioMusic.setText("OFF");

            }
        });

        soundEffectsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEffectsSeekBar.setEnabled(isChecked);
            if (isChecked) {
                int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);
                soundEffectsSeekBar.setProgress(soundEffectsVolume);
                String soundEffectPercentText = soundEffectsVolume + "%";
                soundEffectPercent.setText(soundEffectPercentText);
                ioSoundEffect.setText("ON");
            } else {
                soundEffectsSeekBar.setProgress(0);
                soundEffectPercent.setText("0%");
                ioSoundEffect.setText("OFF");

            }
        });

        saveButton.setOnClickListener(view -> saveSettings(musicSeekBar, soundEffectsSeekBar, musicCheckBox, soundEffectsCheckBox,
                settingsSavedMessage, soundEffectPercent, musicPercent, ioSoundEffect, ioMusic));

        exitButton.setOnClickListener(view -> {
            BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
            if (bouncingSquaresView != null) {
                bouncingSquaresView.resumeBouncing(); // Resume the BouncingSquaresView
            }

            settingOverlay.setVisibility(View.GONE);
        });

        closeAppButton.setOnClickListener(view -> finishAffinity());
    }


    private void loadSettings(SeekBar musicSeekBar, SeekBar soundEffectsSeekBar, CheckBox musicCheckBox, CheckBox soundEffectsCheckBox, TextView soundEffectPercent, TextView musicPercent, TextView ioSoundEffect, TextView ioMusic) {
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
        ioSoundEffect.setText(isSoundEffectsOn ? "ON" : "OFF");
        ioMusic.setText(isMusicOn ? "ON" : "OFF");


    }

    private void saveSettings(SeekBar musicSeekBar, SeekBar soundEffectsSeekBar,
                              CheckBox musicCheckBox, CheckBox soundEffectsCheckBox,
                              TextView settingsSavedMessage, TextView soundEffectPercent, TextView musicPercent, TextView ioSoundEffect, TextView ioMusic) {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int musicVolume = musicCheckBox.isChecked() ? musicSeekBar.getProgress() : 0;
        int soundEffectsVolume = soundEffectsCheckBox.isChecked() ? soundEffectsSeekBar.getProgress() : 0;
        boolean isMusicOn = musicCheckBox.isChecked();
        boolean isSoundEffectsOn = soundEffectsCheckBox.isChecked();
        String onOff = musicCheckBox.isChecked() ? "ON" : "OFF";

        editor.putInt("musicVolume", musicVolume);
        editor.putInt("soundEffectsVolume", soundEffectsVolume);
        editor.putBoolean("isMusicOn", isMusicOn);
        editor.putBoolean("isSoundEffectsOn", isSoundEffectsOn);
        editor.apply();

        String soundEffectText = soundEffectsVolume + "%";
        String musicText = musicVolume + "%";
        soundEffectPercent.setText(soundEffectText);
        musicPercent.setText(musicText);
        ioSoundEffect.setText(onOff);
        ioMusic.setText(onOff);

        settingsSavedMessage.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> settingsSavedMessage.setVisibility(View.GONE), 2000);

        applySettings();
    }

    private void applySettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        int soundEffectsVolume = sharedPreferences.getInt("soundEffectsVolume", 100);

        BouncingSquaresView bouncingSquaresView = findViewById(R.id.bouncingSquareView);
        if (bouncingSquaresView != null) {
            bouncingSquaresView.updateVolume(soundEffectsVolume / 100f);
        }
    }

    private void updateUI() {
        String rankTextString = "RANK " + progressManager.getRank();
        rankTextView.setText(rankTextString);
        rankTextView1.setText(rankTextString);
        rankTextView2.setText(rankTextString);

        progress = (int) ((progressManager.getCurrentXP() / (float) progressManager.getNextLevelXP()) * 100);
        xpProgressBar.setProgress(progress);
        objectiveManager.loadObjectives();
    }


    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
        updateUI();

        SharedPreferences sharedPreferences = getSharedPreferences("GameSettings", MODE_PRIVATE);
        int originalRank = sharedPreferences.getInt("originalRank", currentRank);

        int updatedRank = progressManager.getRank();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        Log.d("RankDebug", "Original Rank: " + originalRank + " | Updated Rank: " + updatedRank);


        if (updatedRank > originalRank && !hasRankedUp) {
            Log.d("RankDebug", "Rank Up Detected! Showing Rank Up Menu.");

            showRankUpMenu();
            editor.putInt("originalRank", currentRank);
            editor.apply();
            hasRankedUp = true;
        } else {
            hasRankedUp = false;
            Log.d("RankDebug", "No Rank Up Detected.");
        }
    }

    private void updateBeatCoinsDisplay() {
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

                onConnected();
            } else {

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
                String message = Objects.requireNonNull(task.getException()).getMessage();
                assert message != null;
                Log.e("Sign IN", message);

            }
        });
    }

    private void onConnected() {
        Log.d("SignIn", "Sign-in successful");

        findViewById(R.id.signInButtonLayout).setVisibility(View.GONE);
    }

    private void showSignInButton() {
        Log.d("SignIn", "Sign-in failed or not signed in");


        findViewById(R.id.signInButtonLayout).setVisibility(View.VISIBLE);
    }
    }


