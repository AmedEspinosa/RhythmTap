package com.example.rhythmtapgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        TextView levelTextView = findViewById(R.id.levelTextView);
        TextView accuracyTextView = findViewById(R.id.accuracyTextView);

        // Get the level from the Intent
        int level = getIntent().getIntExtra("LEVEL", 1);
        int accuracy = getIntent().getIntExtra("ACCURACY", 0);

        String levelText = "You reached level " + level;
        String accuracyText = "Accuracy: " + accuracy + "%";

        levelTextView.setText(levelText);
        accuracyTextView.setText(accuracyText);


        Button restartButton = findViewById(R.id.restartButton);
        Button mainMenuButton = findViewById(R.id.mainMenuButton);

        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        mainMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
