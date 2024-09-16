package com.example.rhythmtapgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class InventoryActivity extends AppCompatActivity {

    InventoryManager inventoryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);  // Use your store layout

        FrameLayout menuButton = findViewById(R.id.playButton);
        FrameLayout leaderBoardButton = findViewById(R.id.leaderboardButton);
        FrameLayout shopButton = findViewById(R.id.shopButton);

        ImageView exitButton = findViewById(R.id.exit_button_inventory);

        inventoryManager = new InventoryManager(this);

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

        setUpInventory();
    }

    private void setUpInventory(){
        TextView livesCount = findViewById(R.id.livesCount);

        TextView powerUpCount = findViewById(R.id.powerupCount);

        TextView songCount = findViewById(R.id.songCount);

        TextView skinCount = findViewById(R.id.skinCount);

        int freezeCount = inventoryManager.getItemQuantity("powerups","freeze");
        int clearCount = inventoryManager.getItemQuantity("powerups","clear");
        int addTimeCount = inventoryManager.getItemQuantity("powerups","addTime");

        livesCount.setText(String.valueOf(inventoryManager.getCategorySize("lives")));

        powerUpCount.setText(String.valueOf(freezeCount + clearCount + addTimeCount));

        songCount.setText(String.valueOf(inventoryManager.getCategorySize("songs")));

        skinCount.setText(String.valueOf(inventoryManager.getCategorySize("skins")));
    }
}
