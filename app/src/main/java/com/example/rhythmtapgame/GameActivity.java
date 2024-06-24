package com.example.rhythmtapgame;

import android.annotation.SuppressLint;
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
    private final int tempo = 250; // Tempo of 45 BPM in milliseconds (60,000 ms / 45 BPM)
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

        //Log.d(TAG, "onCreate: Starting");


        soundPool = new SoundPool.Builder().setMaxStreams(30).build();
        rowSounds = new int[6][5];

        loadSounds();


        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        beatTiles = new ArrayList<>();
        random = new Random();

        //Log.d(TAG, "onCreate: Views initialized");


        setupGrid();
        startGameTimer();
        findAdjacentTiles();
        List<List<Integer>> adjacentClusters = findAdjacentTiles(); // Call the method to find adjacent tiles
        assignSoundsToClusters(adjacentClusters);
        startSoundLoop();

        //Log.d(TAG, "onCreate: Setup complete");

    }

    @SuppressLint("DiscouragedApi")
    private void loadSounds() {
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
                    if (consecutiveCount > 1) { // Only consider clusters of size 2 or more
                        adjacentClusters.add(new ArrayList<>(clusterIndexes));
                        //Log.d(TAG, "Row " + row + ": Found " + consecutiveCount + " adjacent tiles at indexes " + clusterIndexes);
                    }
                    // Reset for next cluster
                    consecutiveCount = 0;
                    clusterIndexes.clear();
                }
            }

            // Check if there's a cluster at the end of the row
            if (consecutiveCount > 1) { // Only consider clusters of size 2 or more
                adjacentClusters.add(new ArrayList<>(clusterIndexes));
                //Log.d(TAG, "Row " + row + ": Found " + consecutiveCount + " adjacent tiles at indexes " + clusterIndexes);
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
                    } if (row == 1) {
                        tile.setSoundId(clusterSize + 4);
                    } if (row == 2) {
                        tile.setSoundId(clusterSize + 8);
                    } if (row == 3) {
                        tile.setSoundId(clusterSize + 12);
                    } if (row == 4) {
                        tile.setSoundId(clusterSize + 16);
                    } if (row == 5) {
                        tile.setSoundId(clusterSize + 20);
                    }


                    Log.e(TAG,"Tile at: " + tile.getX() + " " + tile.getY() + " Assigned: " + tile.getSoundId());
                }
            }
        }
    }


    private void startSoundLoop() {
        soundHandler = new Handler(Looper.getMainLooper());
        soundHandler.post(new Runnable() {
            @Override
            public void run() {
                playColumnSounds(currentColumn);
                currentColumn = (currentColumn + 1) % 16; // Move to the next column, loop back after 16
                soundHandler.postDelayed(this, tempo);
            }
        });
    }


    private void playColumnSounds(int column) {
        //Log.d(TAG, "playColumnSounds: Playing sounds for column " + column);

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


    private boolean isFirstTileInCluster(int row, int column) {
        int index = row * (gridLayout.getColumnCount() - 1) + column;
        if (index > 0) {
            int previousIndex = index - 1;
            return previousIndex >= beatTiles.size() || !beatTiles.get(previousIndex).isPartOfCluster() || !beatTiles.get(previousIndex).isActive();
        }
        return true;
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


                tile.setSoundId(rowSounds[i][0]);
                Log.d(TAG, "Assigned default sound ID: " + rowSounds[i][0] + " to tile at row " + i + ", column " + (j - 1));


                beatTiles.add(tile);

                gridLayout.addView(tileButton);
            }
        }


        
         /*
         //Logic to test activation of entire row of tiles

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
        int totalTilesToToggle = Math.min((int) Math.round((timeLeftInMillis / 1000.0) * 3.2), maxTiles);

        //Log.d(TAG, "setupGrid: Total tiles to toggle: " + totalTilesToToggle);


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


        //Log.d(TAG, "setupGrid: Grid setup complete");


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
        // Find the tile in the list and toggle its state
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

        // Check if all tiles are inactive
        if (areAllTilesInactive()) {
            gameTimer.cancel();
            showGameWon();
        }
    }

    private boolean areAllTilesInactive() {
        for (BeatTile tile : beatTiles) {
            if (tile.isActive()) {
                return false;
            }
        }
        return true;
    }

    private void showGameOver() {
        // Show game over screen
        Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show();
    }

    private void showGameWon() {
        Toast.makeText(this, "Congratulations! You Win!", Toast.LENGTH_SHORT).show();
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





