package com.example.rhythmtapgame;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;


import androidx.appcompat.app.AppCompatActivity;


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
    private int tilePressSoundId1, tilePressSoundId2;
    private int totalTaps = 0;
    private int correctTaps = 0;
    private int currentLevel = 1; // Track the current level
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
    private int freezeCount = 3;
    private int clearCount = 3;
    private int addTimeCount = 3;
    private boolean isTimerFrozen = false;
    private boolean isPaused = false;
    private FrameLayout pauseMenuOverlay;
    private FrameLayout gameOverMenuOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        pauseMenuOverlay = findViewById(R.id.pauseMenuOverlay);
        gameOverMenuOverlay = findViewById(R.id.gameOverMenuOverlay);
        ImageButton pauseButton = findViewById(R.id.pauseButton);

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




        // Initialize the level TextView
        beatTiles = new ArrayList<>();
        random = new Random();

        soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        freezeSoundId = soundPool.load(this, R.raw.freeze_sound, 1);
        clearSoundId = soundPool.load(this, R.raw.clear_sound, 1);
        addTimeSoundId = soundPool.load(this, R.raw.add_time_sound, 1);
        tilePressSoundId1 = soundPool.load(this,R.raw.bubble_1,1);
        tilePressSoundId2 = soundPool.load(this,R.raw.bubble_2,1);

        startNewLevel();


    }

    private void pauseGame() {
        isPaused = true;

        // Inflate the pause menu layout if it hasn't been done yet
        if (pauseMenuOverlay.getChildCount() == 0) {
            View pauseMenuContent = getLayoutInflater().inflate(R.layout.activity_pause_menu, pauseMenuOverlay, false);
            pauseMenuOverlay.addView(pauseMenuContent);

            TextView title = pauseMenuContent.findViewById(R.id.pauseTitle);
            applyGradient(title);

            // Set up button listeners for the pause menu
            pauseMenuContent.findViewById(R.id.resumeButton).setOnClickListener(v -> resumeGame());
            pauseMenuContent.findViewById(R.id.mainMenuButtonPause).setOnClickListener(v -> {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });



        }

        pauseMenuOverlay.setVisibility(View.VISIBLE);

        // Pause game logic
        soundPool.autoPause();
        mediaPlayer.pause();
        gameTimer.cancel();

        gridLayout.setEnabled(false);
        findViewById(R.id.freezePowerUp).setEnabled(false);
        findViewById(R.id.clearPowerUp).setEnabled(false);
        findViewById(R.id.timePowerUp).setEnabled(false);
    }


    private void resumeGame() {
        isPaused = false;
        pauseMenuOverlay.setVisibility(View.GONE);
        soundPool.autoResume();
        mediaPlayer.start();
        gameTimer.start();
        findViewById(R.id.freezePowerUp).setEnabled(true);
        findViewById(R.id.clearPowerUp).setEnabled(true);
        findViewById(R.id.timePowerUp).setEnabled(true);
    }


    private int calculateScore(int level, long remainingTimeInMillis, int accuracy) {
        int basePoints = level * 100;
        int speedBonus = (int) (remainingTimeInMillis / 1000) * 5;

        // Define multipliers based on accuracy
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

        beatTiles.clear(); // Clear previous tiles
        gridLayout.removeAllViews();


        int accuracy = (int) (((double) correctTaps / totalTaps) * 100);

        // Calculate score
        int levelScore = calculateScore(currentLevel, timeLeftInMillis, accuracy);

        totalScore += levelScore;



        // Remove previous views
        setupGrid();
        startGameTimer(7000);
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


    }


    private void applyGradient(TextView title) {
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
    }



    private void setupGrid() {
        // Grid rows
        int gridRows = 16;
        gridLayout.setRowCount(gridRows);
        // Grid columns
        int gridColumns = 6;
        gridLayout.setColumnCount(gridColumns);


        for (int i = 0; i < gridRows; i++) {
            // Add the icon at the beginning of the row
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
                int row = index / (gridColumns); // Calculate row
                int col = index % (gridColumns); // Calculate column
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
        Log.e("Track", "Track Name: " + trackName);
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
    }

    private void getSong() {
        boolean result = random.nextBoolean();
        if (result) {
            currentSong = "angelic_melody";
        } else {
            currentSong = "string_dance";
        }


    }

    private void onTileTapped(Button tileButton, int x, int y) {
        for (BeatTile tile : beatTiles) {
            if (tile.getX() == x && tile.getY() == y) {
                if (!tile.isInitiallyToggled()) {
                    totalTaps++;
                    return;
                }
                tile.toggle();
                if (tile.isActive()) {
                    tileButton.setBackgroundColor(ROW_COLORS[y]);
                } else {
                    tileButton.setBackgroundColor(Color.TRANSPARENT);
                    int soundToPlay = random.nextBoolean() ? tilePressSoundId1 : tilePressSoundId2;
                    soundPool.play(soundToPlay, 0.25f, 0.25f, 0, 0, 1);
                    correctTaps++;
                    totalTaps++;
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
        if (gameOverMenuOverlay.getChildCount() == 0) {
            View gameOverMenuContent = getLayoutInflater().inflate(R.layout.activity_game_over, gameOverMenuOverlay, false);
            gameOverMenuOverlay.addView(gameOverMenuContent);

            TextView title = gameOverMenuContent.findViewById(R.id.gameOverTitle);

            TextView levelReached = findViewById(R.id.gameOverLevel);
            TextView levelReached1 = findViewById(R.id.gameOverLevel1);
            TextView levelReached2 = findViewById(R.id.gameOverLevel2);


            TextView lastScore = findViewById(R.id.gameOverScore);
            TextView lastScore1 = findViewById(R.id.gameOverScore1);
            TextView lastScore2 = findViewById(R.id.gameOverScore2);

            String level = "LEVEL " + currentLevel;

            levelReached.setText(level);
            levelReached1.setText(level);
            levelReached2.setText(level);


            String score = "SCORE: " + totalScore;

            lastScore.setText(score);
            lastScore1.setText(score);
            lastScore2.setText(score);


            applyGradient(title);

            // Set up button listeners for the pause menu
            gameOverMenuContent.findViewById(R.id.playAgainButton).setOnClickListener(v ->{
                Intent intent = new Intent(GameActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            });
            gameOverMenuContent.findViewById(R.id.mainMenuButtonOver).setOnClickListener(v -> {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });



        }

        gameOverMenuOverlay.setVisibility(View.VISIBLE);

        // Pause game logic
        soundPool.release();
        mediaPlayer.stop();
        gameTimer.cancel();

        gridLayout.setEnabled(false);
        findViewById(R.id.freezePowerUp).setEnabled(false);
        findViewById(R.id.clearPowerUp).setEnabled(false);
        findViewById(R.id.timePowerUp).setEnabled(false);
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
            freezeCount--;
            isTimerFrozen = true;
            gameTimer.cancel();
            soundPool.play(freezeSoundId,1, 1, 0, 0, 1);
            long freezeDuration = 10000;
            new Handler().postDelayed(() -> {
                isTimerFrozen = false;
                startGameTimer(0);
            }, freezeDuration);
            updatePowerUpCounts();
        }
    }

    private void useClearPowerUp() {
        if (clearCount > 0) {
            clearCount--;
            currentLevel++;
            gameTimer.cancel();
            soundPool.play(clearSoundId,1, 1, 0, 0, 1);
            startNewLevel();
            updatePowerUpCounts();
        }
    }

    private void useAddTimePowerUp() {
        if (addTimeCount > 0) {
            addTimeCount--;
            gameTimer.cancel();
            soundPool.play(addTimeSoundId,0.25f, 0.25f, 0, 0, 1);
            startGameTimer(15000);
            updatePowerUpCounts();
        }
    }

    private void updatePowerUpCounts() {
        ((TextView) findViewById(R.id.freezeCount)).setText(String.valueOf(freezeCount));
        ((TextView) findViewById(R.id.clearCount)).setText(String.valueOf(clearCount));
        ((TextView) findViewById(R.id.addCount)).setText(String.valueOf(addTimeCount));
    }


}





