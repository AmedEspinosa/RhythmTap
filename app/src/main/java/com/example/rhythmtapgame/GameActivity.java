package com.example.rhythmtapgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";


    private GridLayout gridLayout;
    private List<BeatTile> beatTiles;
    private TextView timerTextView;
    private CountDownTimer gameTimer;
    private long timeLeftInMillis = 10000;
    private Random random;
    private SoundPool soundPool;
    private int[][] rowSounds;
    private Handler soundHandler;
    private int currentColumn = 0;
    private final int tempo = 250;
    private int currentLevel = 1; // Track the current level
    private static final int BASE_TILE_COUNT = 10; // Base number of tiles to toggle
    private static final double DIFFICULTY_INCREMENT = 0.2;
    private static final int[] ROW_COLORS = {
            Color.parseColor("#800080"), // Purple
            Color.parseColor("#FF0000"), // Red
            Color.parseColor("#FFFF00"), // Yellow
            Color.parseColor("#00FFFF"), // Cyan
            Color.parseColor("#0000FF"), // Blue
            Color.parseColor("#008000")  // Green
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        soundPool = new SoundPool.Builder().setMaxStreams(30).build();
        rowSounds = new int[6][5];

        loadSounds();

        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        beatTiles = new ArrayList<>();
        random = new Random();


       startNewLevel();


    }

    private void startNewLevel() {
        stopSoundLoop(); // Stop the current sound loop
        releaseSoundPool(); // Release the current SoundPool

        // Initialize a new SoundPool
        soundPool = new SoundPool.Builder().setMaxStreams(30).build();
        loadSounds(); // Reload sounds into the new SoundPool

        beatTiles.clear(); // Clear previous tiles
        gridLayout.removeAllViews(); // Remove previous views
        setupGrid();
        startGameTimer();
        List<List<Integer>> adjacentClusters = findAdjacentTiles();
        assignSoundsToClusters(adjacentClusters);
        startSoundLoop();
    }

    ///////////////////////////SOUND LOGIC//////////////////////////////////////////////////////////

    private void startSoundLoop() {
        soundHandler = new Handler(Looper.getMainLooper());
        soundHandler.post(new Runnable() {
            @Override
            public void run() {
                playColumnSounds(currentColumn);
                currentColumn = (currentColumn + 1) % 16;
                soundHandler.postDelayed(this, tempo);
            }
        });
    }

    @SuppressLint("DiscouragedApi")
    private void loadSounds() {
        // Load sounds for single tiles
        String[] instruments = {"bass", "drum", "clap", "cymbal", "hihat", "tom_tom"};
        for (int i = 0; i < instruments.length; i++) {
            // Load the single tile sound
            Log.e(TAG, "Single Tile Sound: " + instruments[i]);
            rowSounds[i][0] = soundPool.load(this, getResources().getIdentifier(instruments[i], "raw", getPackageName()), 1);
            Log.d(TAG, "Loaded sound: " + instruments[i] + ", ID: " + rowSounds[i][0]);

            // Load sounds for adjacent tiles
            for (int j = 2; j <= 4; j++) {
                String soundName = instruments[i] + "_adjacent" + j;
                int soundResource = getResources().getIdentifier(soundName, "raw", getPackageName());
                rowSounds[i][j - 1] = soundPool.load(this, soundResource, 1);  // Note: j-1 to correctly index the array
                Log.d(TAG, "Loaded sound: " + soundName + ", ID: " + rowSounds[i][j - 1]);

            }
        }
    }

    private void playColumnSounds(int column) {
        for (int i = 0; i < gridLayout.getRowCount(); i++) {
            int index = i * (gridLayout.getColumnCount() - 1) + column;
            if (index < beatTiles.size()) {
                BeatTile tile = beatTiles.get(index);
                //Log.d(TAG, "playColumnSounds: Tile at index " + index + " is " + (tile.isActive() ? "active" : "inactive"));
                if (tile.isInitiallyToggled()) {
                    if (tile.isPartOfCluster()) {
                        if (isFirstTileInCluster(i, column)) {
                            if (tile.getSoundId() != 0) {
                                Log.d(TAG, "playColumnSounds: Cluster sound ID: " + tile.getSoundId());
                                soundPool.play(tile.getSoundId(), 1, 1, 0, 0, 1);

                            } else {
                                Log.e(TAG, "playColumnSounds: Cluster sound ID is invalid for tile at row " + i + ", column " + column);

                            }
                        }
                    } else {
                        if (tile.getSoundId() != 0) {
                            Log.d(TAG, "playColumnSounds: Single tile sound ID: " + tile.getSoundId());
                            soundPool.play(tile.getSoundId(), 1, 1, 0, 0, 1);

                        } else {
                            Log.e(TAG, "playColumnSounds: Single tile sound ID is invalid for tile at row " + i + ", column " + column);

                        }

                    }
                }
            } else {
                Log.e(TAG, "Index out of bounds: " + index);
            }
        }
    }
/////////////////////////////////////CLUSTER TILE LOGIC////////////////////////////////////////////////

    private List<List<Integer>> findAdjacentTiles() {
        List<List<Integer>> adjacentClusters = new ArrayList<>();

        for (int row = 0; row < gridLayout.getRowCount(); row++) {
            int consecutiveCount = 0;
            List<Integer> clusterIndexes = new ArrayList<>();

            for (int col = 1; col < gridLayout.getColumnCount(); col++) {
                int index = row * (gridLayout.getColumnCount() - 1) + (col - 1);
                BeatTile tile = beatTiles.get(index);

                if (tile.isInitiallyToggled()) {
                    consecutiveCount++;
                    clusterIndexes.add(index);
                } else {
                    if (consecutiveCount > 1) {
                        adjacentClusters.add(new ArrayList<>(clusterIndexes));
                    }
                    consecutiveCount = 0;
                    clusterIndexes.clear();
                }
            }

            if (consecutiveCount > 1) {
                adjacentClusters.add(new ArrayList<>(clusterIndexes));
            }

            for (List<Integer> cluster : adjacentClusters) {
                for (int index : cluster) {
                    beatTiles.get(index).setPartOfCluster(true);
                }
            }
        }

        return adjacentClusters;
    }

    private void assignSoundsToClusters(List<List<Integer>> adjacentClusters) {
        for (List<Integer> cluster : adjacentClusters) {
            if (!cluster.isEmpty()) {
                int row = cluster.get(0) / (gridLayout.getColumnCount() - 1);
                int clusterSize = cluster.size();


                for (int index : cluster) {
                    BeatTile tile = beatTiles.get(index);
                    if (row == 0) {
                        tile.setSoundId(clusterSize);
                    }
                    if (row == 1) {
                        tile.setSoundId(clusterSize + 4);
                    }
                    if (row == 2) {
                        tile.setSoundId(clusterSize + 8);
                    }
                    if (row == 3) {
                        tile.setSoundId(clusterSize + 12);
                    }
                    if (row == 4) {
                        tile.setSoundId(clusterSize + 16);
                    }
                    if (row == 5) {
                        tile.setSoundId(clusterSize + 20);
                    }


                    Log.e(TAG, "Tile at: " + tile.getX() + " " + tile.getY() + " Assigned: "
                            + tile.getSoundId());
                }
            }
        }
    }


    private boolean isFirstTileInCluster(int row, int column) {
        int index = row * (gridLayout.getColumnCount() - 1) + column;
        if (index > 0) {
            int previousIndex = index - 1;
            return previousIndex >= beatTiles.size() || !beatTiles.get(previousIndex).isPartOfCluster()
                    || !beatTiles.get(previousIndex).isActive();
        }
        return true;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

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


                tile.setSoundId(rowSounds[i][0]);
                Log.d(TAG, "Assigned default sound ID: " + rowSounds[i][0] + " to tile at row " + i + ", column " + (j - 1));


                beatTiles.add(tile);

                gridLayout.addView(tileButton);
            }
        }


        
         /*
    ///////////Logic to test activation of entire row of tiles///////////////////////

        int testRow = 1; // Activate the third row (index 2)

        for (int j = 1; j < gridColumns; j++) {
            int index = testRow * (gridColumns - 1) + (j - 1); // Adjust index for beatTiles
            BeatTile tile = beatTiles.get(index);
            tile.setInitiallyToggled(true);
            tile.toggle();
            int row = index / (gridColumns - 1); // Calculate row
            int col = index % (gridColumns - 1) + 1; // Calculate column
            View view = gridLayout.getChildAt(row * gridColumns + col);
            if (view instanceof Button) {
                Button tileButton = (Button) view;
                onTileTapped(tileButton, tile.getX(), tile.getY());
                Log.d(TAG, "setupGrid: Toggled tile at row " + tile.getX() + ", column " + tile.getY());
            }
        }

         */



        int maxTiles = gridRows * (gridColumns - 1);
        int baseTilesToToggle = BASE_TILE_COUNT;
        int levelTilesToToggle = (int) (baseTilesToToggle + (currentLevel - 1) * DIFFICULTY_INCREMENT * baseTilesToToggle);
        int totalTilesToToggle = Math.min(levelTilesToToggle, maxTiles);


        Set<Integer> toggledTilesIndices = new HashSet<>();

        while (toggledTilesIndices.size() < totalTilesToToggle) {
            int randomRow = random.nextInt(gridRows);
            int randomColumn = random.nextInt(gridColumns - 1) + 1;
            int randomIndex = randomRow * (gridColumns - 1) + (randomColumn - 1);
            toggledTilesIndices.add(randomIndex);

        }

        for (int index : toggledTilesIndices) {
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

    private void startGameTimer() {
        gameTimer = new CountDownTimer(timeLeftInMillis + 5000, 100) {
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


    private void onTileTapped(Button tileButton, int x, int y) {
        for (BeatTile tile : beatTiles) {
            if (tile.getX() == x && tile.getY() == y) {
                if (!tile.isInitiallyToggled()) {
                    return;
                }
                tile.toggle();
                if (tile.isActive()) {
                    tileButton.setBackgroundColor(ROW_COLORS[x]);
                } else {
                    tileButton.setBackgroundColor(Color.TRANSPARENT);
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
        intent.putExtra("LEVEL", currentLevel); // Pass the current level
        startActivity(intent);
        onDestroy();
        finish();
    }

    private void showGameWon() {
        Toast.makeText(this, "Level :" + currentLevel, Toast.LENGTH_SHORT).show();
        nextLevel();

    }

    private void nextLevel() {
        currentLevel++;
        if (currentLevel % 6 == 0) {
            currentLevel++; // Skip the next level to make it easier every 5 levels
        }
        startNewLevel();
    }


    private void stopSoundLoop() {
        if (soundHandler != null) {
            soundHandler.removeCallbacksAndMessages(null);
            soundHandler = null;
        }
    }

    private void releaseSoundPool() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}





