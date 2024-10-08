package com.example.rhythmtapgame;

public class ShopEntry {
    private final int imageResourceId;
    private final String name;
    private final double price;
    private int itemQuantity;
    private String backgroundColor;
    private boolean isPurchasableByBeatCoin;
    private String category;

    public ShopEntry(int imageResourceId, String name, double price, int itemQuantity, String backgroundColor, boolean isPurchasableByBeatCoin, String category) {
        this.imageResourceId = imageResourceId;
        this.name = name;
        this.price = price;
        this.itemQuantity = itemQuantity;
        this.backgroundColor = backgroundColor;
        this.isPurchasableByBeatCoin = isPurchasableByBeatCoin;
        this.category = category;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
