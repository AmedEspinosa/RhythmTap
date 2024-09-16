package com.example.rhythmtapgame;

public class ShopEntry {
    private final int imageResourceId;
    private final String name;
    private final String price;
    private String itemQuantity;
    private String backgroundColor;
    private boolean isPurchasableByBeatCoin;

    public ShopEntry(int imageResourceId, String name, String price, String itemQuantity, String backgroundColor, boolean isPurchasableByBeatCoin) {
        this.imageResourceId = imageResourceId;
        this.name = name;
        this.price = price;
        this.itemQuantity = itemQuantity;
        this.backgroundColor = backgroundColor;
        this.isPurchasableByBeatCoin = isPurchasableByBeatCoin;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isPurchasableByBeatCoin() {
        return isPurchasableByBeatCoin;
    }

    public void setIsPurchasableByBeatCoin(boolean isPurchasableByBeatCoin) {
        this.isPurchasableByBeatCoin = isPurchasableByBeatCoin;
    }
}
