package com.example.rhythmtapgame;

import android.content.Context;
import android.content.SharedPreferences;

public class CurrencyManager {
    private static final String PREFS_NAME = "currency_prefs";
    private static final String BEAT_COINS_KEY = "beat_coins";

    private final SharedPreferences sharedPreferences;

    public CurrencyManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getBeatCoins() {
        return sharedPreferences.getInt(BEAT_COINS_KEY, 0);
    }

    public void addBeatCoins(int amount) {
        int currentBalance = getBeatCoins();
        sharedPreferences.edit().putInt(BEAT_COINS_KEY, currentBalance + amount).apply();
    }

    public boolean spendBeatCoins(int amount) {
        int currentBalance = getBeatCoins();
        if (currentBalance >= amount) {
            sharedPreferences.edit().putInt(BEAT_COINS_KEY, currentBalance - amount).apply();
            return true;
        } else {
            return false;
        }
    }

    public void setBeatCoins(int amount) {
        sharedPreferences.edit().putInt(BEAT_COINS_KEY, amount).apply();
    }
}
