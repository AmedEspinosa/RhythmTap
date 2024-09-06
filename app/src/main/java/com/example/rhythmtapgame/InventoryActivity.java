package com.example.rhythmtapgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);  // Use your store layout

        FrameLayout menuButton = findViewById(R.id.playButton);
        FrameLayout leaderBoardButton = findViewById(R.id.leaderboardButton);
        FrameLayout shopButton = findViewById(R.id.shopButton);

        ImageView exitButton = findViewById(R.id.exit_button_inventory);

        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(InventoryActivity.this, MainActivity.class);
            startActivity(intent);
        });

        leaderBoardButton.setOnClickListener(view -> {
            Intent intent = new Intent(InventoryActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        shopButton.setOnClickListener(view -> {
            Intent intent = new Intent(InventoryActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        exitButton.setOnClickListener(view -> {
            Intent intent = new Intent(InventoryActivity.this, ShopActivity.class);
            startActivity(intent);
        });

    }
}
