package com.example.rhythmtapgame;

public class ShopEntry {
    private int imageResourceId;
    private String name;
    private String price;
    private String itemQuantity;
    private String backgroundColor;

    public ShopEntry(int imageResourceId, String name, String price, String itemQuantity, String backgroundColor) {
        this.imageResourceId = imageResourceId;
        this.name = name;
        this.price = price;
        this.itemQuantity = itemQuantity;
        this.backgroundColor = backgroundColor;
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
}
