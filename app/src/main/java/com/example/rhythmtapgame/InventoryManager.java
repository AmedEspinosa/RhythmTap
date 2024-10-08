package com.example.rhythmtapgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {
    private final List<InventoryCategory> categories;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private static final String INVENTORY_PREFS = "InventoryPrefs";

    public InventoryManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(INVENTORY_PREFS, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.categories = new ArrayList<>();
        initializeDefaultCategories();
        loadInventory();
    }

    private void initializeDefaultCategories() {
        InventoryCategory powerups = new InventoryCategory("powerups");
        powerups.addItem(new InventoryItem("freeze", "powerups", 0));
        powerups.addItem(new InventoryItem("clear", "powerups", 0));
        powerups.addItem(new InventoryItem("addTime", "powerups", 0));

        categories.add(powerups);

        categories.add(new InventoryCategory("Lives"));
        categories.add(new InventoryCategory("Songs"));
        categories.add(new InventoryCategory("Skins"));
    }

    public void saveInventory() {
        for (InventoryCategory category : categories) {
            List<InventoryItem> items = category.getItems();
            for (int i = 0; i < items.size(); i++) {
                InventoryItem item = items.get(i);
                String key = category.getCategoryName() + "_" + item.getItemName();
                editor.putInt(key + "_quantity", item.getQuantity());
                editor.putBoolean(key + "_equipped", item.isEquipped());
                Log.d("InventorySave", "Saving: " + key + "_quantity = " + item.getQuantity());
            }
        }
        editor.apply();
    }

    public void loadInventory() {
        Log.d("InventoryLoad", "Loading inventory");
        for (InventoryCategory category : categories) {
            Log.d("InventoryLoad", "Category: " + category.getCategoryName());
            List<InventoryItem> items = category.getItems();
            if (items.isEmpty()) {
                Log.d("InventoryLoad", "No items found in category: " + category.getCategoryName());

            }
            for (InventoryItem item : items) {
                String key = category.getCategoryName() + "_" + item.getItemName();
                int savedQuantity = sharedPreferences.getInt(key + "_quantity", 0);
                Log.d("InventoryLoad", "Loaded: " + key + "_quantity = " + savedQuantity);
                boolean isEquipped = sharedPreferences.getBoolean(key + "_equipped", false);

                item.setQuantity(savedQuantity);
                if (isEquipped) {
                    item.equip();
                }
            }
        }
    }

    public List<InventoryCategory> getCategories() {
        return categories;
    }

    public void addItemToCategory(String categoryName, InventoryItem item) {
        for (InventoryCategory category : categories) {
            if (category.getCategoryName().equalsIgnoreCase(categoryName)) {
                category.addItem(item);
                saveInventory();
                return;
            }
        }
    }

    public InventoryCategory getCategoryByName(String categoryName) {
        for (InventoryCategory category : categories) {
            if (category.getCategoryName().equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        return null;
    }

    public int getCategorySize(String categoryName) {
        for (InventoryCategory category : categories) {
            if (category.getCategoryName().equalsIgnoreCase(categoryName)) {
                return category.getItems().size();
            }
        }
        Log.e("Inventory", "Category could not be found!");
        return 0;
    }

    public void updateItemQuantity(String categoryName, String itemName, int newQuantity) {
        InventoryCategory category = getCategoryByName(categoryName);
        if (category != null) {
            for (InventoryItem item : category.getItems()) {
                if (item.getItemName().equalsIgnoreCase(itemName)) {
                    item.setQuantity(newQuantity);
                    saveInventory();
                    return;
                }
            }
        }
    }

    public int getItemQuantity(String categoryName, String itemName) {
        InventoryCategory category = getCategoryByName(categoryName);
        if (category != null) {
            for (InventoryItem item : category.getItems()) {
                if (item.getItemName().equalsIgnoreCase(itemName)) {
                    return item.getQuantity();
                }
            }
        }
        Log.e("Inventory", "Category could not be found!");
        return 0;

    }

    public InventoryItem getItemByCategory(String category, String itemName, int quantity) {
        if (category.equalsIgnoreCase("powerups")) {
            if (itemName.toLowerCase().contains("freeze")) {
                return new InventoryItem("freeze", "powerups", quantity);
            } else if (itemName.toLowerCase().contains("clear")) {
                return new InventoryItem("clear", "powerups", quantity);
            } else if (itemName.toLowerCase().contains("add time")) {
                return new InventoryItem("addTime", "powerups", quantity);
            }
        } else if (category.equalsIgnoreCase("lives")) {
            return new InventoryItem("lives", "lives", quantity);
        }
            return null;
    }


    public void equipItem(String categoryName, String itemName) {
        InventoryCategory category = getCategoryByName(categoryName);
        if (category != null) {
            for (InventoryItem item : category.getItems()) {
                if (item.getItemName().equalsIgnoreCase(itemName)) {
                    item.equip();
                    saveInventory();
                    return;
                }
            }
        }
    }
}
