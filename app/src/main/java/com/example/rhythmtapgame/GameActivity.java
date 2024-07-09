package com.example.rhythmtapgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    private CountDownTimer gameTimer;
    private long timeLeftInMillis = 10000;
    private Random random;
    private MediaPlayer mediaPlayer;
    private TextView levelTextView; // New TextView for the level
    private SoundPool soundPool;
    private int totalTaps = 0;
    private int correctTaps = 0;
    private int currentLevel = 1; // Track the current level
    private static final int[] ROW_COLORS = {
            Color.parseColor("#800080"), // Purple
            Color.parseColor("#FF0000"), // Red
            Color.parseColor("#FFFF00"), // Yellow
            Color.parseColor("#00FFFF"), // Cyan
            Color.parseColor("#0000FF"), // Blue
            Color.parseColor("#008000")  // Green
    };
    CSVProcessor csvProcessor;
    private String currentSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        csvProcessor = new CSVProcessor(this, R.raw.tile_positions);


        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        levelTextView = findViewById(R.id.levelTextView); // Initialize the level TextView
        beatTiles = new ArrayList<>();
        random = new Random();

        startNewLevel();


    }

    private void startNewLevel() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        beatTiles.clear(); // Clear previous tiles
        gridLayout.removeAllViews();


        // Remove previous views
        setupGrid();
        startGameTimer();


        String level = "Level: " + currentLevel;
        levelTextView.setText(level); // Update the level TextView


        playBackgroundTrack();


    }


    private void setupGrid() {
        //Log.d(TAG, "setupGrid: Setting up grid");


        int[] rowIcons = {
                R.drawable.bass,
                R.drawable.reddrum,
                R.drawable.yellowhands,
                R.drawable.cymbalcyan,
                R.drawable.hihatblue,
                R.drawable.tomdrumgreen
        };

        // Grid rows
        int gridRows = 6;
        gridLayout.setRowCount(gridRows);
        // Grid columns
        int gridColumns = 17;
        gridLayout.setColumnCount(gridColumns);


        for (int i = 0; i < gridRows; i++) {
            // Add the icon at the beginning of the row
            ImageView rowIcon = new ImageView(this);
            GridLayout.LayoutParams iconParams = new GridLayout.LayoutParams();
            iconParams.width = 0;
            iconParams.height = 0;
            iconParams.rowSpec = GridLayout.spec(i, 1f);
            iconParams.columnSpec = GridLayout.spec(0, 1f);
            rowIcon.setLayoutParams(iconParams);
            rowIcon.setImageResource(rowIcons[i]);
            rowIcon.setTag("icon");
            rowIcon.setBackgroundColor(Color.TRANSPARENT);
            gridLayout.addView(rowIcon);


            for (int j = 1; j < gridColumns; j++) {
                Button tileButton = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                tileButton.setLayoutParams(params);

                int finalI = i;
                int finalJ = j - 1;
                tileButton.setOnClickListener(view -> onTileTapped(tileButton, finalI, finalJ));

                BeatTile tile = new BeatTile(i, j - 1);


                beatTiles.add(tile);

                gridLayout.addView(tileButton);
            }
        }
        List<Integer> currentTilePositions = csvProcessor.getTilePositions(getSong(), currentLevel, getVariationForLevel(currentLevel, currentSong));

        for (int index : currentTilePositions) {
            BeatTile tile = beatTiles.get(index);
            tile.setInitiallyToggled(true);
            tile.toggle();
            int row = index / (gridColumns - 1); // Calculate row
            int col = index % (gridColumns - 1) + 1; // Calculate column
            View view = gridLayout.getChildAt(row * gridColumns + col);
            if (view instanceof Button) {
                Button tileButton = (Button) view;
                onTileTapped(tileButton, tile.getX(), tile.getY());
            }
        }


    }

    private void playBackgroundTrack() {
        int level = currentLevel;

        if (currentLevel > 14 && !(currentLevel % 6 == 0)) {
            level = 14;
        } else if (currentLevel % 6 == 0) {
            level = 1;
        }

        String trackName = currentSong + "_level_" + level + "_v" + getVariationForLevel(level, currentSong);
        Log.e("Track", "Track Name: " + trackName);
        int trackResId = getResources().getIdentifier(trackName, "raw", getPackageName());

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
        } else if (currentSong.equalsIgnoreCase("angelic_melody") && level < 14) {
            return random.nextInt((2 - 1) + 1) + 1;
        } else if (currentSong.equalsIgnoreCase("angelic_melody") ||
                currentSong.equalsIgnoreCase("string_dance") && currentLevel >= 14 && !(level % 6 == 0)) {
            return random.nextInt((7 - 1) + 1) + 1;
        } else return random.nextInt((3 - 1) + 1) + 1;

    }

    private void startGameTimer() {
        gameTimer = new CountDownTimer(timeLeftInMillis + 10000, 100) {
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
        @SuppressLint("DefaultLocale") String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private String getSong() {
        boolean result = random.nextBoolean();
        if (result) {
            currentSong = "angelic_melody";
            return "am";
        } else {
            currentSong = "string_dance";
        }
        return "sd";

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
                    tileButton.setBackgroundColor(ROW_COLORS[x]);
                } else {
                    tileButton.setBackgroundColor(Color.TRANSPARENT);
                    correctTaps++;
                    totalTaps++;
                }

            }

        }

        if (areAllTilesInactive()) {
            gameTimer.cancel();
            showGameWon();
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
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
        int accuracy = (int) (((double) correctTaps / totalTaps) * 100);
        intent.putExtra("LEVEL", currentLevel); // Pass the current level
        intent.putExtra("ACCURACY", accuracy);
        startActivity(intent);
        onDestroy();
        finish();
    }

    private void showGameWon() {
        nextLevel();

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
        //releaseMediaPlayer();

    }

}





