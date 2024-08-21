package com.example.rhythmtapgame;

import android.content.Context;
import android.content.SharedPreferences;

public class PlayerProgressManager {
    private static final String PREFS_NAME = "PlayerProgress";
    private static final String KEY_XP = "currentXP";
    private static final String KEY_RANK = "rank";
    private static final String KEY_NEXT_LEVEL_XP = "nextLevelXP";
    private SharedPreferences sharedPreferences;
    private int currentXP;
    private int rank;
    private int nextLevelXP;

    public PlayerProgressManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
}
