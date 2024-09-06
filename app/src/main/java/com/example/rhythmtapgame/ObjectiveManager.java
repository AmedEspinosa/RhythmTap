package com.example.rhythmtapgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


import android.util.Log;


public class ObjectiveManager {
    private List<Objective> regularObjectives;
    private List<Objective> dailyObjectives;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String OBJECTIVES_PREFS = "ObjectivesPrefs";
    private int currentTier;
    private int completedObjectiveCount;
    private int countNeededForTier;
    private long totalPlayTime; // in seconds
    private long sessionStartTime;

    private Context context;

    public ObjectiveManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(OBJECTIVES_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        regularObjectives = new ArrayList<>();
        dailyObjectives = new ArrayList<>();
        currentTier = sharedPreferences.getInt("currentTier", 1); // Start at Tier 1
        completedObjectiveCount = sharedPreferences.getInt("completedObjectiveCount", 0);
        totalPlayTime = sharedPreferences.getLong("totalPlayTime", 0);

        loadObjectives();
        scheduleDailyObjectiveReset();
    }

    public void startSession() {
        long currentTime = System.currentTimeMillis();
        long nextResetTime = sharedPreferences.getLong("nextResetTime", 0);

        // If nextResetTime is in the past, calculate the new reset time
        if (currentTime > nextResetTime) {
            nextResetTime = calculateNextResetTime();
            editor.putLong("nextResetTime", nextResetTime);
            editor.apply();
        }
    }

    private long calculateNextResetTime() {
        long currentTime = System.currentTimeMillis();
        // Calculate the time for the next reset (24 hours from now)
        return currentTime + 24 * 60 * 60 * 1000;
    }

    public long getMillisUntilNextReset() {
        long currentTime = System.currentTimeMillis();
        long nextResetTime = sharedPreferences.getLong("nextResetTime", 0);
        return nextResetTime - currentTime;
    }


    private void scheduleDailyObjectiveReset() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ObjectiveResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to start at approximately 24 hours from now
        long triggerAtMillis = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY;

        // Set inexact repeating alarm that triggers every 24 hours
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public void loadObjectives() {
        if (sharedPreferences.getBoolean("initialized", false)) {
            Log.d("ObjectiveManager", "Loading saved objectives.");

            loadSavedObjectives();
        } else {
            Log.d("ObjectiveManager", "Initializing default objectives.");
            initializeDefaultObjectives();
            initializeDailyObjectives();
            editor.putBoolean("initialized", true).apply();
        }
    }

    private void loadSavedObjectives() {
        int count = sharedPreferences.getInt("regular_count", 0);
        int countDaily = sharedPreferences.getInt("daily_count",0);


        for (int i = 0; i < count; i++) {
            String description = sharedPreferences.getString("regular_description_" + i, "");
            int targetAmount = sharedPreferences.getInt("regular_target_" + i, 0);
            int currentProgress = sharedPreferences.getInt("regular_progress_" + i, 0);
            int rewardXP = sharedPreferences.getInt("regular_reward_" + i, 0);
            ObjectiveType type = ObjectiveType.valueOf(sharedPreferences.getString("regular_type_" + i, ObjectiveType.TAP_TILES.name()));
            boolean completed = sharedPreferences.getBoolean("regular_completed_" + i, false);
            String descriptor = sharedPreferences.getString("regular_descriptor_" + i, "");
            String classification = sharedPreferences.getString("regular_classification_" + i,"");



            Objective obj = new Objective(description, targetAmount, rewardXP, type, descriptor,classification);
            obj.addProgress(currentProgress);
            if (completed) {
                obj.addProgress(targetAmount);
            }
            addObjectiveIfNotExists(regularObjectives, obj);

            countNeededForTier = (regularObjectives.size()) - 1;

        }

        for (int i = 0; i < countDaily; i++) {
            String description = sharedPreferences.getString("daily_description_" + i, "");
            int targetAmount = sharedPreferences.getInt("daily_target_" + i, 0);
            int currentProgress = sharedPreferences.getInt("daily_progress_" + i, 0);
            int rewardXP = sharedPreferences.getInt("daily_reward_" + i, 0);
            ObjectiveType type = ObjectiveType.valueOf(sharedPreferences.getString("daily_type_" + i, ObjectiveType.TAP_TILES.name()));
            boolean completed = sharedPreferences.getBoolean("daily_completed_" + i, false);
            String descriptor = sharedPreferences.getString("daily_descriptor_" + i, "");
            String classification = sharedPreferences.getString("daily_classification_" + i,"");



            Objective obj = new Objective(description, targetAmount, rewardXP, type, descriptor,classification);
            obj.addProgress(currentProgress);
            if (completed) {
                obj.addProgress(targetAmount);
            }
            addObjectiveIfNotExists(dailyObjectives, obj);

        }



    }

    public void resetObjectives() {
        // Clear the SharedPreferences and reinitialize the default objectives
        editor.clear();
        editor.apply();

        regularObjectives.clear();
        dailyObjectives.clear();

        Log.d("ObjectiveManager", "Resetting objectives.");
        initializeDefaultObjectives();
        initializeDailyObjectives();
    }

    public void resetDailyObjectives() {
        dailyObjectives.clear();

        // Reinitialize or add new daily objectives
        initializeDailyObjectives();

        // Save the reset objectives
        saveObjectives();
    }

    private void initializeDailyObjectives() {
        // Add new daily objectives here
        for (int i = 0; i < 3; i++) {

        Random random = new Random();
        int objectiveType = random.nextInt(5);

        switch (objectiveType) {
            case 0:
                int target0 = random.nextInt(100);
                dailyObjectives.add(new Objective("Play " + target0  + " Levels", target0, 50, ObjectiveType.CLEAR_LEVELS, "Level","Daily"));
                break;
            case 1:
                int target1 = random.nextInt(1000);
                dailyObjectives.add(new Objective("Tap "  + target1 + " Tiles", target1, calculateReward(ObjectiveType.TAP_TILES,target1), ObjectiveType.TAP_TILES, "Tapped","Daily"));
                break;
            case 2:
                int target2 = random.nextInt(20);
                dailyObjectives.add(new Objective("Use " + target2 + " Power-Ups", target2, calculateReward(ObjectiveType.USE_POWERUPS,target2), ObjectiveType.USE_POWERUPS, "Used","Daily"));
                break;
            case 3:
                int target3 = random.nextInt(10);
                dailyObjectives.add(new Objective("Complete " + target3  + "Levels", target3, calculateReward(ObjectiveType.NO_MISS,target3), ObjectiveType.NO_MISS, "Completed","Daily"));
                break;
            case 4:
                int target4 = random.nextInt(75);
                dailyObjectives.add(new Objective("Achieve a "+ target4 + "-Tap Combo", target4, calculateReward(ObjectiveType.ACHIEVE_COMBO,target4), ObjectiveType.ACHIEVE_COMBO, "Achieved","Daily"));
                break;

        }


            Collections.sort(dailyObjectives, new Comparator<Objective>() {
                @Override
                public int compare(Objective o1, Objective o2) {
                    int typeComparison = o1.getType().compareTo(o2.getType());
                    if (typeComparison == 0) {
                        return Integer.compare(o1.getTargetAmount(), o2.getTargetAmount());
                    }
                    return typeComparison;
                }
            });
        saveObjectives();


        }
    }

    private void addObjectiveIfNotExists(List<Objective> objectivesList, Objective newObjective) {
        boolean exists = false;
        for (Objective obj : objectivesList) {
            if (obj.equals(newObjective)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            objectivesList.add(newObjective);
        }
    }

    public void endSession() {
        long sessionDuration = (SystemClock.elapsedRealtime() - sessionStartTime) / 1000; // in seconds
        totalPlayTime += sessionDuration;
        editor.putLong("totalPlayTime", totalPlayTime).apply();

        updatePlayTimeObjective();
    }

    private void updatePlayTimeObjective() {
        Objective playTimeObjective = getObjectiveByType(ObjectiveType.PLAY_TIME,"regular");
        if (playTimeObjective != null && !playTimeObjective.isCompleted()) {
            playTimeObjective.addProgress((int) (totalPlayTime / 3600)); // Converts seconds to hours
            if (playTimeObjective.getCurrentProgress() >= playTimeObjective.getTargetAmount()) {
                completedObjectiveCount++;
                handleObjectiveCompletion(playTimeObjective);
            }
            saveObjectives();
        }
    }

    public void initializeDefaultObjectives() {
        // Initialize a few default regular objectives
        regularObjectives.add(new Objective("Tap 250 Tiles", 250, calculateReward(ObjectiveType.TAP_TILES,250), ObjectiveType.TAP_TILES, "Tapped","Regular"));
        regularObjectives.add(new Objective("Use 30 Power-Ups", 30, calculateReward(ObjectiveType.USE_POWERUPS,30), ObjectiveType.USE_POWERUPS, "Used","Regular"));
        regularObjectives.add(new Objective("Clear 20 Levels", 20, calculateReward(ObjectiveType.CLEAR_LEVELS,20), ObjectiveType.CLEAR_LEVELS, "Cleared","Regular" ));
        regularObjectives.add(new Objective("Reach Rank 5", 5, calculateReward(ObjectiveType.REACH_RANK,5), ObjectiveType.REACH_RANK, "Reached","Regular"));

        // Additional objectives from the new types
        regularObjectives.add(new Objective("Achieve a 10-Tap Combo", 10, calculateReward(ObjectiveType.ACHIEVE_COMBO,10), ObjectiveType.ACHIEVE_COMBO, "Achieved","Regular"));
        regularObjectives.add(new Objective("Complete a Level With 100% Accuracy", 1, calculateReward(ObjectiveType.NO_MISS,1), ObjectiveType.NO_MISS, "Level","Regular"));
        regularObjectives.add(new Objective("Play for 1 Hour Total", 1, calculateReward(ObjectiveType.PLAY_TIME,1), ObjectiveType.PLAY_TIME, "Hour","Regular"));
        regularObjectives.add(new Objective("Collect 100 Beat Coins", 100, calculateReward(ObjectiveType.COLLECT_COINS,100), ObjectiveType.COLLECT_COINS, "Beat Coins","Regular"));
    }

    // Save objectives to shared preferences
    public void saveObjectives() {
        editor.putInt("regular_count", regularObjectives.size());
        editor.putInt("daily_count",dailyObjectives.size());
        editor.putInt("completedObjectiveCount", completedObjectiveCount);

        for (int i = 0; i < regularObjectives.size(); i++) {
            Objective obj = regularObjectives.get(i);
            editor.putString("regular_description_" + i, obj.getDescription());
            editor.putInt("regular_target_" + i, obj.getTargetAmount());
            editor.putInt("regular_progress_" + i, obj.getCurrentProgress());
            editor.putInt("regular_reward_" + i, obj.getRewardXP());
            editor.putString("regular_type_" + i, obj.getType().name());
            editor.putBoolean("regular_completed_" + i, obj.isCompleted());
            editor.putString("regular_descriptor_" + i, obj.getTargetDescriptor());
            editor.putString("regular_classification_" +i,obj.getClassification());

        }

        for (int i = 0; i < dailyObjectives.size(); i++) {
            Objective obj = dailyObjectives.get(i);
            editor.putString("daily_description_" + i, obj.getDescription());
            editor.putInt("daily_target_" + i, obj.getTargetAmount());
            editor.putInt("daily_progress_" + i, obj.getCurrentProgress());
            editor.putInt("daily_reward_" + i, obj.getRewardXP());
            editor.putString("daily_type_" + i, obj.getType().name());
            editor.putBoolean("daily_completed_" + i, obj.isCompleted());
            editor.putString("daily_descriptor_" + i, obj.getTargetDescriptor());
            editor.putString("daily_classification_"+i,obj.getClassification());
        }
        // Save daily objectives similarly

        editor.apply();
    }

    // Update an objective's progress by type
    public void updateObjectiveProgress(ObjectiveType type, int amount, String classiffication) {
        if (classiffication.equalsIgnoreCase("regular")) {

            for (Objective obj : regularObjectives) {
                if (obj.getType() == type && !obj.isCompleted()) {
                    obj.addProgress(amount);
                    if (obj.isCompleted()) {
                        completedObjectiveCount++;
                        handleObjectiveCompletion(obj);
                    }
                    break;
                }
            }
        } else if (classiffication.equalsIgnoreCase("daily")) {
            for (Objective obj : dailyObjectives) {
                if (obj.getType() == type && !obj.isCompleted()) {
                    obj.addProgress(amount);
                    if (obj.isCompleted()) {
                        completedObjectiveCount++;
                        if (obj.isClaimed()) {
                            dailyObjectives.remove(obj);
                        }
                    }
                    break;
                }
            }
        }


        saveObjectives();
    }

    private void handleObjectiveCompletion(Objective obj) {
        // Remove completed objective
        if (obj.isClaimed()) {
            regularObjectives.remove(obj);
        }

        // Check if the tier needs to be upgraded

        if (countNeededForTier <= completedObjectiveCount || regularObjectives.isEmpty()) {
            tierUp();
        }
    }

    private void tierUp() {
        completedObjectiveCount = 0;
        currentTier++;
        editor.putInt("currentTier", currentTier).apply();

        // Add more difficult objectives
        addTieredObjectives();

        saveObjectives();
    }

    private void addTieredObjectives() {
        int multiplier = currentTier; // Increase difficulty and rewards based on tier
        int desiredObjectiveCount = 8 + (multiplier - 1) * 2; // Example: increase objectives by 2 each tier

        // Calculate how many objectives need to be added
        int objectivesToAdd = desiredObjectiveCount - regularObjectives.size();

        if (objectivesToAdd > 0) {
            for (int i = 0; i < objectivesToAdd; i++) {
                regularObjectives.add(new Objective("Tap " + (250 * multiplier) + " Tiles", 250 * multiplier, calculateReward(ObjectiveType.TAP_TILES,250 * multiplier) * multiplier, ObjectiveType.TAP_TILES, "Tapped","Regular"));
                regularObjectives.add(new Objective("Use " + (30 * multiplier) + " Power-Ups", 30 * multiplier, calculateReward(ObjectiveType.USE_POWERUPS,30 * multiplier) * multiplier, ObjectiveType.USE_POWERUPS, "Used","Regular"));
                regularObjectives.add(new Objective("Clear " + (20 * multiplier) + " Levels", 20 * multiplier, calculateReward(ObjectiveType.CLEAR_LEVELS,20 * multiplier) * multiplier, ObjectiveType.CLEAR_LEVELS, "Cleared","Regular"));
                regularObjectives.add(new Objective("Reach Rank " + (5 * multiplier), 5 * multiplier, calculateReward(ObjectiveType.REACH_RANK,5 * multiplier) * multiplier, ObjectiveType.REACH_RANK, "Reached","Regular"));
                regularObjectives.add(new Objective("Achieve a " + (10 * multiplier) + "-Tap Combo", 10 * multiplier, calculateReward(ObjectiveType.ACHIEVE_COMBO,10 * multiplier) * multiplier, ObjectiveType.ACHIEVE_COMBO, "Achieved","Regular"));
                regularObjectives.add(new Objective("Complete " + (multiplier) + "Levels With 100% Accuracy", multiplier, calculateReward(ObjectiveType.NO_MISS,multiplier) * multiplier, ObjectiveType.NO_MISS, "Level","Regular"));
                regularObjectives.add(new Objective("Play for " + multiplier + " Hours Total", multiplier, calculateReward(ObjectiveType.PLAY_TIME, multiplier) * multiplier, ObjectiveType.PLAY_TIME, "Hour","Regular"));
                regularObjectives.add(new Objective("Collect " + (100 * multiplier) + " Beat Coins", 100 * multiplier, calculateReward(ObjectiveType.COLLECT_COINS,100 * multiplier) * multiplier, ObjectiveType.COLLECT_COINS, "Beat Coins","Regular"));
            }
        }
    }

    public int claimObjectiveReward(Objective obj,String classiffication) {
        if (obj.isCompleted() && !obj.isClaimed()) {
            // Mark as claimed and remove the objective
            int reward = obj.getRewardXP();
            obj.setClaimed(true);

            if (classiffication.equalsIgnoreCase("regular")) {
                regularObjectives.remove(obj);
            } else if (classiffication.equalsIgnoreCase("daily")) {
                dailyObjectives.remove(obj);
            }

            // Update the count needed for the next tier
            completedObjectiveCount++;
            countNeededForTier--;

            // Check if a tier-up is needed
            if (countNeededForTier <= 0) {
                tierUp();
            } else {
                saveObjectives(); // Save state after changes
            }

            return reward;
        }
        return 0;
    }

    public int getCompletedObjectives(){
        int count = 0;

        for (Objective obj : regularObjectives) {
            if (obj.isCompleted()) {
                count++;
            }
        }

        for (Objective obj : dailyObjectives) {
            if (obj.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public List<Objective> getRegularObjectives() {
        return regularObjectives;
    }

    public int getCountNeededForTier() {
        return countNeededForTier;
    }

    public List<Objective> getDailyObjectives() {
        return dailyObjectives;
    }

    public int getCompletedObjectiveCount() {
        return completedObjectiveCount;
    }

    public Objective getObjectiveByType(ObjectiveType type, String classiffication) {
        if (classiffication.equalsIgnoreCase("regular")) {

            for (Objective obj : regularObjectives) {
                if (obj.getType().equals(type)) {
                    return obj;
                }
            }
        } else if (classiffication.equalsIgnoreCase("daily")) {
            for (Objective obj : dailyObjectives) {
                if (obj.getType().equals(type)) {
                    return obj;
                }
            }
        }
        return new Objective(null,0,0,null,null,null);
    }

    private int calculateReward(ObjectiveType type, int targetAmount) {

        int baseReward = 0;
        float multiplier = 1.0f;

        // Define base rewards and multipliers for each objective type
        switch (type) {
            case CLEAR_LEVELS:
                baseReward = 50;  // Base reward for clearing levels
                multiplier = 1.5f;  // Higher multiplier for harder objectives
                break;
            case TAP_TILES:
                baseReward = 30;  // Base reward for tapping tiles
                multiplier = 1.f;  // Normal difficulty
                break;
            case USE_POWERUPS:
                baseReward = 40;  // Base reward for using power-ups
                multiplier = 1.2f;  // Slightly higher difficulty
                break;
            case PLAY_TIME:
                baseReward = 20;  // Base reward for playtime-related objectives
                multiplier = 1.3f;  // Easier task, lower multiplier
                break;
            case ACHIEVE_COMBO:
                baseReward = 60;  // Base reward for achieving combos
                multiplier = 2.0f;  // Higher difficulty, higher reward
                break;
            case NO_MISS:
                baseReward = 100;  // High reward for no-miss objectives
                multiplier = 2.5f;  // Most difficult, highest multiplier
                break;
            case REACH_RANK:
                baseReward = 80;  // Base reward for rank-related objectives
                multiplier = 2.0f;  // Significant reward for rank objectives
                break;
            case COLLECT_COINS:
                baseReward = 25;  // Base reward for collecting coins
                multiplier = 1.0f;  // Normal multiplier
                break;
            default:
                baseReward = 10;  // Default reward for unknown types
                multiplier = 1.0f;
                break;
        }

        // Calculate the reward based on the target amount and the multiplier
        int rewardAmount = (int) (baseReward + (targetAmount * multiplier));

        return rewardAmount;

    }



}
