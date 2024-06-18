package com.example.rhythmtapgame;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private List<BeatTile> beatTiles;
    private final int gridSize = 8; // Example grid size
    private TextView timerTextView;
    private CountDownTimer gameTimer;
    private long timeLeftInMillis = 60000; // 60 seconds



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gridLayout = findViewById(R.id.gridLayout);
        timerTextView = findViewById(R.id.timerTextView);
        beatTiles = new ArrayList<>();

        setupGrid();
    }

    private void setupGrid() {
        // Example grid size
        int gridSize = 8;
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                Button tileButton = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.rowSpec = GridLayout.spec(i, 1, 1f);
                params.columnSpec = GridLayout.spec(j, 1, 1f);
                tileButton.setLayoutParams(params);
                tileButton.setBackgroundColor(Color.LTGRAY);
                int finalI = i;
                int finalJ = j;
                tileButton.setOnClickListener(view -> onTileTapped(tileButton, finalI, finalJ));

                BeatTile tile = new BeatTile(i, j);
                beatTiles.add(tile);

                gridLayout.addView(tileButton);
            }
        }
    }

    private void onTileTapped(Button tileButton, int x, int y) {
        // Find the tile in the list and toggle its state
        for (BeatTile tile : beatTiles) {
            if (tile.getX() == x && tile.getY() == y) {
                tile.toggle();
                tileButton.setBackgroundColor(tile.isActive() ? Color.GREEN : Color.GRAY);
                break;
            }
        }

        // Check if all tiles are inactive
        if (areAllTilesInactive()) {
            // Game over logic
            showGameOver();
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
}
