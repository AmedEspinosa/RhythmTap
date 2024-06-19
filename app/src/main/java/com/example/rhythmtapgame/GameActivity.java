package com.example.rhythmtapgame;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.gridlayout.widget.GridLayout; // Import this class
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
    private long timeLeftInMillis = 10000; // 60 seconds
    Random random;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Log.d(TAG, "onCreate: Starting");


        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        beatTiles = new ArrayList<>();
        random = new Random();

        Log.d(TAG, "onCreate: Views initialized");


        setupGrid();
        startGameTimer();

        Log.d(TAG, "onCreate: Setup complete");

    }

    private void setupGrid() {
        Log.d(TAG, "setupGrid: Setting up grid");


        // Grid rows
        int gridRows = 6;
        gridLayout.setRowCount(gridRows);
        // Grid columns
        int gridColumns = 16;
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


        int maxTiles = gridRows * gridColumns;
        int totalTilesToToggle = Math.min((int) Math.round((timeLeftInMillis / 1000.0) * 3.2), maxTiles);


        Log.d(TAG, "setupGrid: Total tiles to toggle: " + totalTilesToToggle);


        Set<Integer> toggledTilesIndices = new HashSet<>();

        while (toggledTilesIndices.size() < totalTilesToToggle) {
            int randomIndex = random.nextInt(maxTiles);
            toggledTilesIndices.add(randomIndex);
        }

        for (int index : toggledTilesIndices) {
            BeatTile tile = beatTiles.get(index);
            tile.toggle();
            Button tileButton = (Button) gridLayout.getChildAt(index);
            onTileTapped(tileButton,tile.getX(),tile.getY());
        }

        Log.d(TAG, "setupGrid: Grid setup complete");


    }

    private void startGameTimer() {
        gameTimer = new CountDownTimer(timeLeftInMillis, 1000) {
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
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        @SuppressLint("DefaultLocale") String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }



    private void onTileTapped(Button tileButton, int x, int y) {
        // Find the tile in the list and toggle its state
        for (BeatTile tile : beatTiles) {
            if (tile.getX() == x && tile.getY() == y) {
                tile.toggle();
                tileButton.setBackgroundColor(tile.isActive() ? Color.GREEN : Color.TRANSPARENT);
                break;
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
}
