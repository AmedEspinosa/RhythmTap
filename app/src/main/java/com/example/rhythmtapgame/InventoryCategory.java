package com.example.rhythmtapgame;

import java.util.ArrayList;
import java.util.List;

public class InventoryCategory {
    private final String categoryName;
    private final List<InventoryItem> items;

    public InventoryCategory(String categoryName) {
        this.categoryName = categoryName;
        this.items = new ArrayList<>();
    }

    public String getCategoryName() {
        return categoryName;
    }

    public List<InventoryItem> getItems() {
        return items;
    }

    public void addItem(InventoryItem item) {
        items.add(item);
    }
}
