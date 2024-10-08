package com.example.rhythmtapgame;

public class InventoryItem {
    private final String itemName;
    private int quantity;
    private boolean isEquipped;
    private String category;

    public InventoryItem(String itemName,String category, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.isEquipped = false;
        this.category = category;

    }

    public void equip() {
        this.isEquipped = true;
    }

    public void unequip() {
        this.isEquipped = false;
    }

    public boolean isEquipped() {
        return isEquipped;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}


