package com.example.rhythmtapgame;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerProgressManager {
    private static final String PREFS_NAME = "PlayerProgress";
    private static final String KEY_XP = "currentXP";
    private static final String KEY_RANK = "rank";
    private static final String KEY_NEXT_LEVEL_XP = "nextLevelXP";
    private final SharedPreferences sharedPreferences;
    private int currentXP;
    private int rank;
    private int nextLevelXP;
    private final Random random = new Random();
    private final InventoryManager inventoryManager;
    private final CurrencyManager currencyManager;


    public PlayerProgressManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        inventoryManager = new InventoryManager(context);
        currencyManager = new CurrencyManager(context);
        loadProgress();
    }

    public void loadProgress() {
        currentXP = sharedPreferences.getInt(KEY_XP, 0);
        rank = sharedPreferences.getInt(KEY_RANK, 1);
        nextLevelXP = calculateNextLevelXP(rank);
    }

    public void saveProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_XP, currentXP);
        editor.putInt(KEY_RANK, rank);
        editor.putInt(KEY_NEXT_LEVEL_XP, nextLevelXP);
        editor.apply();
    }

    public int calculateNextLevelXP(int rank) {
        int baseXP = 500;
        return (int) (baseXP * Math.pow(1.15, rank - 1));
    }

    public int getCurrentXP() {
        return currentXP;
    }

    public int getRank() {
        return rank;
    }

    public int getNextLevelXP() {
        return nextLevelXP;
    }

    public void addXP(int xpGained) {
        currentXP += xpGained;
        while (currentXP >= nextLevelXP) {
            currentXP -= nextLevelXP;
            rank++;
            nextLevelXP = calculateNextLevelXP(rank);
        }
        saveProgress();
    }

    public List<InventoryItem> giveReward(int rank){
        boolean isMilestone = (rank % 5 == 0);

        List<InventoryItem> rewards = new ArrayList<>();
        int rewardAmount = random.nextInt(3);

        if (isMilestone) {
            rewardAmount += 1;
        }

        for (int i = 0; i < rewardAmount; i++) {
            rewards.add(generateReward(rank, isMilestone));
        }

        // Apply the reward to the player
        applyReward(rewards);

        return rewards;
    }

    private InventoryItem generateReward(int playerRank, boolean isMilestone) {
        List<InventoryItem> rewardPool = new ArrayList<>();

        // Base probabilities (you can tweak these)
        int commonWeight = 60;
        int uncommonWeight = 30;
        int rareWeight = 9;
        int epicWeight = 1;

        // If it's a milestone, boost rare and epic chances
        if (isMilestone) {
            rareWeight += 5;  // Boost rare rewards
            epicWeight += 2;  // Boost epic rewards
        }

        // Fill the reward pool based on weighted RNG logic
        for (int i = 0; i < commonWeight; i++) {
            rewardPool.add(getCommonReward(playerRank));
        }
        for (int i = 0; i < uncommonWeight; i++) {
            rewardPool.add(getUncommonReward(playerRank));
        }
        for (int i = 0; i < rareWeight; i++) {
            rewardPool.add(getRareReward(playerRank));
        }
        for (int i = 0; i < epicWeight; i++) {
            rewardPool.add(getEpicReward(playerRank));
        }

        // Randomly select a reward from the pool
        return rewardPool.get(random.nextInt(rewardPool.size()));
    }

    private InventoryItem getCommonReward(int playerRank) {
        List<InventoryItem> commonRewards = new ArrayList<>();
        commonRewards.add(new InventoryItem("XP", 50 + playerRank * 10)); // XP scales with rank
        commonRewards.add(new InventoryItem("freeze", 1));
        commonRewards.add(new InventoryItem("clear", 1));
        commonRewards.add(new InventoryItem("beatCoins",15));


        return commonRewards.get(random.nextInt(commonRewards.size()));
    }

    // Example uncommon rewards (medium-tier powerups, low-tier skins)
    private InventoryItem getUncommonReward(int playerRank) {
        List<InventoryItem> uncommonRewards = new ArrayList<>();
        uncommonRewards.add(new InventoryItem("addTime", 2));
        uncommonRewards.add(new InventoryItem("beatCoins",50));

        //uncommonRewards.add(new InventoryItem("Skin", 1));
        //uncommonRewards.add(new InventoryItem("Beat Tile Pack", 2));

        return uncommonRewards.get(random.nextInt(uncommonRewards.size()));
    }

    // Example rare rewards (exclusive skins, rare songs)
    private InventoryItem getRareReward(int playerRank) {
        List<InventoryItem> rareRewards = new ArrayList<>();
        //rareRewards.add(new InventoryItem("Skin: Rare Skin", 1));
        //rareRewards.add(new InventoryItem("Song: Rare Song", 1));
        rareRewards.add(new InventoryItem("XP", 200 + playerRank * 20));
        rareRewards.add(new InventoryItem("freeze", 3));
        rareRewards.add(new InventoryItem("beatCoins",100));


        return rareRewards.get(random.nextInt(rareRewards.size()));
    }

    // Example epic rewards (exclusive skins, large XP)
    private InventoryItem getEpicReward(int playerRank) {
        List<InventoryItem> epicRewards = new ArrayList<>();
        //epicRewards.add(new InventoryItem("Exclusive Skin", 1));
        //epicRewards.add(new InventoryItem("Exclusive Song", 1));
        epicRewards.add(new InventoryItem("XP", 500 + playerRank * 50)); // Huge XP bonus
        epicRewards.add(new InventoryItem("clear", 3));
        epicRewards.add(new InventoryItem("beatCoins",200));

        return epicRewards.get(random.nextInt(epicRewards.size()));
    }

    // Apply the reward to the player's inventory
    private void applyReward(List<InventoryItem> rewards) {
        for (InventoryItem reward : rewards) {
            if (reward.getItemName().contains("XP")) {
                // Add XP directly to the player's progress
                addXP(reward.getQuantity());
            } else if (reward.getItemName().contains("beatCoins")) {
                currencyManager.addBeatCoins(reward.getQuantity());
            } else if (reward.getItemName().contains("clear")) {
                inventoryManager.updateItemQuantity("powerups", "clear", reward.getQuantity());
            } else if (reward.getItemName().contains("addTime")) {
                inventoryManager.updateItemQuantity("powerups", "addTime", reward.getQuantity());
            } else
                inventoryManager.updateItemQuantity("powerups", "freeze", reward.getQuantity());
            // Add the item to the player's inventory
        }
    }
    }

