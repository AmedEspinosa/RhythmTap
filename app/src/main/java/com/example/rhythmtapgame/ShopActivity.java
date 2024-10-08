package com.example.rhythmtapgame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {
    private LinearLayout storeItemsContainer;
    private CurrencyManager currencyManager;
    private InventoryManager inventoryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);  // Use your store layout

        storeItemsContainer = findViewById(R.id.storeItemsContainer);

        currencyManager = new CurrencyManager(this);

        inventoryManager = new InventoryManager(this);

        List<ShopEntry> storeItems = new ArrayList<>();
        storeItems.add(new ShopEntry(R.drawable.beat_coins_icon, "Small Beat Coin Pack", 0.99, 500, "#4C49DD", false,"beatcoins"));
        storeItems.add(new ShopEntry(R.drawable.beat_coins_mult_icon, "Medium Beat Coin Pack", 2.99, 2000, "#D93232", false,"beatcoins"));
        storeItems.add(new ShopEntry(R.drawable.ic_freeze, "Freeze Power-Up", 500, 5, "#4C49DD", true,"powerups"));
        storeItems.add(new ShopEntry(R.drawable.ic_clear, "Clear Power-Up", 500, 5, "#D93232", true,"powerups"));
        storeItems.add(new ShopEntry(R.drawable.ic_time, "Add Time Power-Up", 500, 5, "#59B937", true,"powerups"));
        storeItems.add(new ShopEntry(R.drawable.heart_icon, "Lives", 500, 5, "#6A4EB9", true,"lives"));



        for (ShopEntry item : storeItems) {
            addItemToStore(item);
        }

        FrameLayout menuButton = findViewById(R.id.playButton);
        FrameLayout leaderBoardButton = findViewById(R.id.leaderboardButton);

        ImageView inventoryButton = findViewById(R.id.inventory_button);

        TextView balance = findViewById(R.id.beatCoinBalanceText);
        TextView balance1 = findViewById(R.id.beatCoinBalanceTextShadow1);
        TextView balance2 = findViewById(R.id.beatCoinBalanceTextShadow2);

        int currentCoins = currencyManager.getBeatCoins();

        balance.setText(String.valueOf(currentCoins));
        balance1.setText(String.valueOf(currentCoins));
        balance2.setText(String.valueOf(currentCoins));

        menuButton.setOnClickListener(view -> {
            Intent intent = new Intent(ShopActivity.this, MainActivity.class);
            startActivity(intent);
        });

        leaderBoardButton.setOnClickListener(view -> {
            Intent intent = new Intent(ShopActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        inventoryButton.setOnClickListener(view -> {
            Intent intent = new Intent(ShopActivity.this, InventoryActivity.class);
            startActivity(intent);
        });
    }

    private void addItemToStore(ShopEntry item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View storeItemView = inflater.inflate(R.layout.shop_entry, storeItemsContainer, false);

        ImageView itemImage = storeItemView.findViewById(R.id.itemImage);
        TextView itemName = storeItemView.findViewById(R.id.itemName);
        TextView itemName1 = storeItemView.findViewById(R.id.itemNameShadow1);
        TextView itemName2 = storeItemView.findViewById(R.id.itemNameShadow2);

        if (item.isPurchasableByBeatCoin()) {
            TextView itemPrice = storeItemView.findViewById(R.id.itemPrice);
            TextView itemPrice1 = storeItemView.findViewById(R.id.itemPriceShadow1);
            TextView itemPrice2 = storeItemView.findViewById(R.id.itemPriceShadow2);

            itemPrice.setVisibility(View.GONE);
            itemPrice1.setVisibility(View.GONE);
            itemPrice2.setVisibility(View.GONE);

            TextView coinItemPrice = storeItemView.findViewById(R.id.itemPriceBeatCoin);
            TextView coinItemPrice1 = storeItemView.findViewById(R.id.itemPriceBeatCoinShadow1);
            TextView coinItemPrice2 = storeItemView.findViewById(R.id.itemPriceBeatCoinShadow2);

            ImageView beatCoinPriceIcon = storeItemView.findViewById(R.id.beatCoinPriceIcon);


            coinItemPrice.setVisibility(View.VISIBLE);
            coinItemPrice1.setVisibility(View.VISIBLE);
            coinItemPrice2.setVisibility(View.VISIBLE);
            beatCoinPriceIcon.setVisibility(View.VISIBLE);

            double priceDub = item.getPrice();

            String itemPriceString = String.valueOf((int) priceDub);


            coinItemPrice.setText(itemPriceString);
            coinItemPrice1.setText(itemPriceString);
            coinItemPrice2.setText(itemPriceString);




        } else {

            TextView itemPrice = storeItemView.findViewById(R.id.itemPrice);
            TextView itemPrice1 = storeItemView.findViewById(R.id.itemPriceShadow1);
            TextView itemPrice2 = storeItemView.findViewById(R.id.itemPriceShadow2);

            String itemPriceString = "$" + item.getPrice();


            itemPrice.setText(itemPriceString);
            itemPrice1.setText(itemPriceString);
            itemPrice2.setText(itemPriceString);
        }



        TextView itemQuantity = storeItemView.findViewById(R.id.itemQuantity);
        TextView itemQuantity1 = storeItemView.findViewById(R.id.itemQuantityShadow1);
        TextView itemQuantity2 = storeItemView.findViewById(R.id.itemQuantityShadow2);

        View background = storeItemView.findViewById(R.id.itemBackground);

        itemImage.setImageResource(item.getImageResourceId());
        itemName.setText(item.getName());
        itemName1.setText(item.getName());
        itemName2.setText(item.getName());

        String quantityString = "x" + item.getItemQuantity();


        itemQuantity.setText(quantityString);
        itemQuantity1.setText(quantityString);
        itemQuantity2.setText(quantityString);

        background.setBackgroundColor(Color.parseColor(item.getBackgroundColor()));

        storeItemsContainer.addView(storeItemView);

        storeItemView.setOnClickListener(v -> {
            handleItemPurchase(item);
        });
    }

    private void handleItemPurchase(ShopEntry item){

        if (item.isPurchasableByBeatCoin()) {
            if (currencyManager.getBeatCoins() > item.getPrice()) {
                currencyManager.spendBeatCoins((int) item.getPrice());

                inventoryManager.addItemToCategory(item.getCategory(),inventoryManager.getItemByCategory(item.getCategory(),item.getName(),item.getItemQuantity()));

                TextView balance = findViewById(R.id.beatCoinBalanceText);
                TextView balance1 = findViewById(R.id.beatCoinBalanceTextShadow1);
                TextView balance2 = findViewById(R.id.beatCoinBalanceTextShadow2);

                balance.setText(currencyManager.getBeatCoins());
                balance1.setText(currencyManager.getBeatCoins());
                balance2.setText(currencyManager.getBeatCoins());

            }
        }
    }


}
