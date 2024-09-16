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
    private final List<Objective> regularObjectives;
    private final List<Objective> dailyObjectives;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private static final String OBJECTIVES_PREFS = "ObjectivesPrefs";
    private int currentTier;
    private int completedObjectiveCount;
    private int countNeededForTier;
    private long totalPlayTime; // in seconds
    private long sessionStartTime;
    private int objectivesRemainingForTierUp;
    private final Context context;
    private int initialCountNeededForTier;

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

        if (currentTime > nextResetTime) {
            nextResetTime = calculateNextResetTime();
            editor.putLong("nextResetTime", nextResetTime);
            editor.apply();
        }
    }

    private long calculateNextResetTime() {
        long currentTime = System.currentTimeMillis();
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

        long triggerAtMillis = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY;

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public void loadObjectives() {
        if (sharedPreferences.getBoolean("initialized", false)) {
            Log.d("ObjectiveManager", "Loading saved objectives.");

            loadSavedObjectives();

            initialCountNeededForTier = sharedPreferences.getInt("initialCountNeededForTier", -1);
            if (initialCountNeededForTier == -1) {
                // If not found, initialize it
                initialCountNeededForTier = regularObjectives.size() - 1; // or your desired logic
                editor.putInt("initialCountNeededForTier", initialCountNeededForTier);
                editor.apply();
            }

        } else {
            Log.d("ObjectiveManager", "Initializing default objectives.");
            initializeDefaultObjectives();
            initializeDailyObjectives();
            editor.putBoolean("initialized", true).apply();

            initialCountNeededForTier = regularObjectives.size() - 1;

            editor.putInt("initialCountNeededForTier", initialCountNeededForTier);
            editor.apply();

        }
        countNeededForTier = initialCountNeededForTier;
        objectivesRemainingForTierUp = countNeededForTier - completedObjectiveCount;

        Log.e("Tier", "Load Objectives, countNeededForTier: " + countNeededForTier + " objectivesRemainingForTierUp: " + objectivesRemainingForTierUp + " completedObjectiveCount " + completedObjectiveCount);
    }

    private void loadSavedObjectives() {
        int count = sharedPreferences.getInt("regular_count", 0);
        int countDaily = sharedPreferences.getInt("daily_count", 0);


        for (int i = 0; i < count; i++) {
            String description = sharedPreferences.getString("regular_description_" + i, "");
            int targetAmount = sharedPreferences.getInt("regular_target_" + i, 0);
            int currentProgress = sharedPreferences.getInt("regular_progress_" + i, 0);
            int rewardXP = sharedPreferences.getInt("regular_reward_" + i, 0);
            ObjectiveType type = ObjectiveType.valueOf(sharedPreferences.getString("regular_type_" + i, ObjectiveType.TAP_TILES.name()));
            boolean completed = sharedPreferences.getBoolean("regular_completed_" + i, false);
            String descriptor = sharedPreferences.getString("regular_descriptor_" + i, "");
            String classification = sharedPreferences.getString("regular_classification_" + i, "");

            Objective obj = new Objective(targetAmount, rewardXP, type, descriptor, classification);
            obj.setDescription(obj.getType(), obj.getTargetAmount());
            obj.addProgress(currentProgress);
            if (completed) {
                obj.addProgress(targetAmount);
            }
            addObjectiveIfNotExists(regularObjectives, obj);
        }

        for (int i = 0; i < countDaily; i++) {
            String description = sharedPreferences.getString("daily_description_" + i, "");
            int targetAmount = sharedPreferences.getInt("daily_target_" + i, 0);
            int currentProgress = sharedPreferences.getInt("daily_progress_" + i, 0);
            int rewardXP = sharedPreferences.getInt("daily_reward_" + i, 0);
            ObjectiveType type = ObjectiveType.valueOf(sharedPreferences.getString("daily_type_" + i, ObjectiveType.TAP_TILES.name()));
            boolean completed = sharedPreferences.getBoolean("daily_completed_" + i, false);
            String descriptor = sharedPreferences.getString("daily_descriptor_" + i, "");
            String classification = sharedPreferences.getString("daily_classification_" + i, "");

            Objective obj = new Objective(targetAmount, rewardXP, type, descriptor, classification);
            obj.setDescription(obj.getType(), obj.getTargetAmount());
            obj.addProgress(currentProgress);
            if (completed) {
                obj.addProgress(targetAmount);
            }
            addObjectiveIfNotExists(dailyObjectives, obj);
        }
    }

    public void resetObjectives() {
        currentTier = 1;

        completedObjectiveCount = 0;

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
        initializeDailyObjectives();
        saveObjectives();
    }

    private void initializeDailyObjectives() {
        for (int i = 0; i < 3; i++) {

            Random random = new Random();
            int objectiveType = random.nextInt(5);

            switch (objectiveType) {
                case 0:
                    int target0 = random.nextInt(100) + 1;
                    Objective dailyObj0 = new Objective(target0, 50, ObjectiveType.CLEAR_LEVELS, "Level", "Daily");
                    dailyObj0.setDescription(dailyObj0.getType(), dailyObj0.getTargetAmount());
                    dailyObjectives.add(dailyObj0);
                    break;
                case 1:
                    int target1 = random.nextInt(1000) + 1;
                    Objective dailyObj1 = new Objective(target1, calculateReward(ObjectiveType.TAP_TILES, target1), ObjectiveType.TAP_TILES, "Tapped", "Daily");
                    dailyObj1.setDescription(dailyObj1.getType(), dailyObj1.getTargetAmount());
                    dailyObjectives.add(dailyObj1);
                    break;
                case 2:
                    int target2 = random.nextInt(20) + 1;
                    Objective dailyObj2 = new Objective(target2, calculateReward(ObjectiveType.USE_POWERUPS, target2), ObjectiveType.USE_POWERUPS, "Used", "Daily");
                    dailyObj2.setDescription(dailyObj2.getType(), dailyObj2.getTargetAmount());
                    dailyObjectives.add(dailyObj2);
                    break;
                case 3:
                    int target3 = random.nextInt(10) + 1;
                    Objective dailyObj3 = new Objective(target3, calculateReward(ObjectiveType.NO_MISS, target3), ObjectiveType.NO_MISS, "Completed", "Daily");
                    dailyObj3.setDescription(dailyObj3.getType(), dailyObj3.getTargetAmount());
                    dailyObjectives.add(dailyObj3);
                    break;
                case 4:
                    int target4 = random.nextInt(75) + 1;
                    Objective dailyObj4 = new Objective(target4, calculateReward(ObjectiveType.ACHIEVE_COMBO, target4), ObjectiveType.ACHIEVE_COMBO, "Achieved", "Daily");
                    dailyObj4.setDescription(dailyObj4.getType(), dailyObj4.getTargetAmount());
                    dailyObjectives.add(dailyObj4);
                    break;
            }

            Collections.sort(dailyObjectives, (o1, o2) -> {
                int typeComparison = o1.getType().compareTo(o2.getType());
                if (typeComparison == 0) {
                    return Integer.compare(o1.getTargetAmount(), o2.getTargetAmount());
                }
                return typeComparison;
            });
            saveObjectives();
        }
    }

    //TODO add logic to handle duplicate objectives here
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
        Objective playTimeObjective = getObjectiveByType(ObjectiveType.PLAY_TIME, "regular");
        if (playTimeObjective != null && !playTimeObjective.isCompleted()) {
            playTimeObjective.addProgress((int) (totalPlayTime / 3600)); // Converts seconds to hours
            if (playTimeObjective.getCurrentProgress() >= playTimeObjective.getTargetAmount()) {
                completeObjective(playTimeObjective);
            }
            saveObjectives();
        }
    }

    public void initializeDefaultObjectives() {
        Objective obj1 = new Objective(250, calculateReward(ObjectiveType.TAP_TILES, 250), ObjectiveType.TAP_TILES, "Tapped", "Regular");
        obj1.setDescription(obj1.getType(), obj1.getTargetAmount());
        regularObjectives.add(obj1);

        Objective obj2 = new Objective(30, calculateReward(ObjectiveType.USE_POWERUPS, 30), ObjectiveType.USE_POWERUPS, "Used", "Regular");
        obj2.setDescription(obj2.getType(), obj2.getTargetAmount());
        regularObjectives.add(obj2);

        Objective obj3 = new Objective(20, calculateReward(ObjectiveType.CLEAR_LEVELS, 20), ObjectiveType.CLEAR_LEVELS, "Cleared", "Regular");
        obj3.setDescription(obj3.getType(), obj3.getTargetAmount());
        regularObjectives.add(obj3);

        Objective obj4 = new Objective(5, calculateReward(ObjectiveType.REACH_RANK, 5), ObjectiveType.REACH_RANK, "Reached", "Regular");
        obj4.setDescription(obj4.getType(), obj4.getTargetAmount());
        regularObjectives.add(obj4);

        Objective obj5 = new Objective(10, calculateReward(ObjectiveType.ACHIEVE_COMBO, 10), ObjectiveType.ACHIEVE_COMBO, "Achieved", "Regular");
        obj5.setDescription(obj5.getType(), obj5.getTargetAmount());
        regularObjectives.add(obj5);

        Objective obj6 = new Objective(1, calculateReward(ObjectiveType.NO_MISS, 1), ObjectiveType.NO_MISS, "Level", "Regular");
        obj6.setDescription(obj6.getType(), obj6.getTargetAmount());
        regularObjectives.add(obj6);

        Objective obj7 = new Objective(1, calculateReward(ObjectiveType.PLAY_TIME, 1), ObjectiveType.PLAY_TIME, "Hour", "Regular");
        obj7.setDescription(obj7.getType(), obj7.getTargetAmount());
        regularObjectives.add(obj7);

        Objective obj8 = new Objective(100, calculateReward(ObjectiveType.COLLECT_COINS, 100), ObjectiveType.COLLECT_COINS, "Beat Coins", "Regular");
        obj8.setDescription(obj8.getType(), obj8.getTargetAmount());
        regularObjectives.add(obj8);
    }

    public void saveObjectives() {
        editor.putInt("regular_count", regularObjectives.size());
        editor.putInt("daily_count", dailyObjectives.size());
        editor.putInt("completedObjectiveCount", completedObjectiveCount);
        editor.putInt("initialCountNeededForTier", initialCountNeededForTier);

        for (int i = 0; i < regularObjectives.size(); i++) {
            Objective obj = regularObjectives.get(i);
            editor.putString("regular_description_" + i, obj.getDescription());
            editor.putInt("regular_target_" + i, obj.getTargetAmount());
            editor.putInt("regular_progress_" + i, obj.getCurrentProgress());
            editor.putInt("regular_reward_" + i, obj.getRewardXP());
            editor.putString("regular_type_" + i, obj.getType().name());
            editor.putBoolean("regular_completed_" + i, obj.isCompleted());
            editor.putString("regular_descriptor_" + i, obj.getTargetDescriptor());
            editor.putString("regular_classification_" + i, obj.getClassification());
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
            editor.putString("daily_classification_" + i, obj.getClassification());
        }
        editor.apply();
    }

    public void completeObjective(Objective obj) {
        if (obj.isCompleted() && obj.isClaimed()) {
            completedObjectiveCount++;
            Log.d("Tier", "Completed Objective Count Increment: " + completedObjectiveCount);
            objectivesRemainingForTierUp = Math.max(0, countNeededForTier - completedObjectiveCount);
            removeObjectiveFromList(obj);
            checkAndHandleTierUpgrade();
            saveObjectives();
        }
    }

    private void removeObjectiveFromList(Objective obj) {
        if (obj.getClassification().equalsIgnoreCase("regular")) {
            regularObjectives.remove(obj);
        } else if (obj.getClassification().equalsIgnoreCase("daily")) {
            dailyObjectives.remove(obj);
        }
    }

    private void checkAndHandleTierUpgrade() {
        if (completedObjectiveCount >= countNeededForTier) {
            Log.d("Tier", "Staring tier up. CompletedObjectiveCount: " + completedObjectiveCount + "Count Needed for Tier: " + countNeededForTier);
            tierUp();
        }
    }

    public void updateObjectiveProgress(ObjectiveType type, int amount, String classification) {
        List<Objective> objectives = classification.equalsIgnoreCase("regular") ? regularObjectives : dailyObjectives;
        for (Objective obj : objectives) {
            if (obj.getType() == type && !obj.isCompleted()) {
                obj.addProgress(amount);
                if (obj.isCompleted()) {
                    saveObjectives();
                }
                break;
            }
        }
    }

    public int claimObjectiveReward(Objective obj) {
        if (obj.isCompleted() && !obj.isClaimed()) {
            int reward = obj.getRewardXP();
            obj.setClaimed(true);
            completeObjective(obj); // Ensure completeObjective is called to handle completion logic
            return reward;
        }
        return 0;
    }

    private void tierUp() {
        completedObjectiveCount = 0;
        currentTier++;
        editor.putInt("currentTier", currentTier).apply();

        Log.d("Tier", "Tiered Up. Current Tier: " + currentTier);

        addTieredObjectives();

        saveObjectives();
    }

    private void addTieredObjectives() {
        Log.d("ObjectiveManager", "Starting to add tiered objectives.");
        int multiplier = currentTier;
        int objectivesToAdd = 5 + (multiplier - 1);

        if (objectivesToAdd > 0) {
            Random random = new Random();
            for (int i = 0; i < objectivesToAdd; i++) {
                int objectiveType = random.nextInt(9);

                Objective objective = createObjectiveByType(objectiveType, multiplier);

                adjustForDuplicate(objective, multiplier);

                regularObjectives.add(objective);

                Log.d("ObjectiveManager", "Added: " + objective.getDescription());

                Collections.sort(regularObjectives, (o1, o2) -> {
                    int typeComparison = o1.getType().compareTo(o2.getType());
                    if (typeComparison == 0) {
                        return Integer.compare(o1.getTargetAmount(), o2.getTargetAmount());
                    }
                    return typeComparison;
                });
                saveObjectives();
                countNeededForTier = regularObjectives.size() - 1;
                objectivesRemainingForTierUp = countNeededForTier - completedObjectiveCount;

                initialCountNeededForTier = countNeededForTier;

                // Save initialCountNeededForTier
                editor.putInt("initialCountNeededForTier", initialCountNeededForTier);
                editor.apply();

                Log.e("Tier", "addTieredObjectives, countNeededForTier: " + countNeededForTier + " objectivesRemainingForTierUp: " + objectivesRemainingForTierUp);

            }
        }
        Log.d("ObjectiveManager", "Completed adding tiered objectives.");
    }

    private void adjustForDuplicate(Objective newObjective, int multiplier) {
        for (Objective obj : regularObjectives) {
            if (obj.equals(newObjective) && !obj.getType().equals(ObjectiveType.PLAY_TIME) && !obj.getType().equals(ObjectiveType.NO_MISS)) {
                newObjective.setTargetAmount(newObjective.getTargetAmount() + (5 * multiplier));
                newObjective.setRewardXP(calculateReward(newObjective.getType(), newObjective.getTargetAmount()));
                newObjective.setDescription(newObjective.getType(), newObjective.getTargetAmount());
                break;
            } else if (obj.equals(newObjective) && obj.getType().equals(ObjectiveType.PLAY_TIME)) {
                newObjective.setTargetAmount(newObjective.getTargetAmount() + 1);
                newObjective.setRewardXP(calculateReward(newObjective.getType(), newObjective.getTargetAmount()));
                newObjective.setDescription(newObjective.getType(), newObjective.getTargetAmount());
                break;
            }
        }
    }

    private Objective createObjectiveByType(int objectiveType, int multiplier) {
        switch (objectiveType) {
            case 0:
                Objective objective = new Objective(250 * multiplier, calculateReward(ObjectiveType.TAP_TILES, 250 * multiplier) * multiplier, ObjectiveType.TAP_TILES, "Tapped", "Regular");
                objective.setDescription(objective.getType(), objective.getTargetAmount());
                return objective;
            case 1:
                Objective objective1 = new Objective(30 * multiplier, calculateReward(ObjectiveType.USE_POWERUPS, 30 * multiplier) * multiplier, ObjectiveType.USE_POWERUPS, "Used", "Regular");
                objective1.setDescription(objective1.getType(), objective1.getTargetAmount());
                return objective1;
            case 2:
                Objective objective2 = new Objective(20 * multiplier, calculateReward(ObjectiveType.CLEAR_LEVELS, 20 * multiplier) * multiplier, ObjectiveType.CLEAR_LEVELS, "Cleared", "Regular");
                objective2.setDescription(objective2.getType(), objective2.getTargetAmount());
                return objective2;
            case 3:
                Objective objective3 = new Objective(5 * multiplier, calculateReward(ObjectiveType.REACH_RANK, 5 * multiplier) * multiplier, ObjectiveType.REACH_RANK, "Reached", "Regular");
                objective3.setDescription(objective3.getType(), objective3.getTargetAmount());
                return objective3;
            case 4:
                Objective objective4 = new Objective(10 * multiplier, calculateReward(ObjectiveType.ACHIEVE_COMBO, 10 * multiplier) * multiplier, ObjectiveType.ACHIEVE_COMBO, "Achieved", "Regular");
                objective4.setDescription(objective4.getType(), objective4.getTargetAmount());
                return objective4;
            case 5:
                Objective objective5 = new Objective(10 * multiplier, calculateReward(ObjectiveType.ACHIEVE_COMBO, 10 * multiplier) * multiplier, ObjectiveType.ACHIEVE_COMBO, "Achieved", "Regular");
                objective5.setDescription(objective5.getType(), objective5.getTargetAmount());
                return objective5;
            case 6:
                Objective objective6 = new Objective(multiplier, calculateReward(ObjectiveType.NO_MISS, multiplier) * multiplier, ObjectiveType.NO_MISS, "Level", "Regular");
                objective6.setDescription(objective6.getType(), objective6.getTargetAmount());
                return objective6;
            case 7:
                Objective objective7 = new Objective(multiplier, calculateReward(ObjectiveType.PLAY_TIME, multiplier) * multiplier, ObjectiveType.PLAY_TIME, "Hour", "Regular");
                objective7.setDescription(objective7.getType(), objective7.getTargetAmount());
                return objective7;
            case 8:
                Objective objective8 = new Objective(100 * multiplier, calculateReward(ObjectiveType.COLLECT_COINS, 100 * multiplier) * multiplier, ObjectiveType.COLLECT_COINS, "Beat Coins", "Regular");
                objective8.setDescription(objective8.getType(), objective8.getTargetAmount());
                return objective8;
        }
        return null;
    }

    public int getCompletedObjectives() {
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

    public int getObjectivesRemainingForTierUp() {
        return objectivesRemainingForTierUp;
    }

    public List<Objective> getDailyObjectives() {
        return dailyObjectives;
    }

    public int getCompletedObjectiveCount() {
        return completedObjectiveCount;
    }

    public Objective getObjectiveByType(ObjectiveType type, String classification) {
        if (classification.equalsIgnoreCase("regular")) {

            for (Objective obj : regularObjectives) {
                if (obj.getType().equals(type)) {
                    return obj;
                }
            }
        } else if (classification.equalsIgnoreCase("daily")) {
            for (Objective obj : dailyObjectives) {
                if (obj.getType().equals(type)) {
                    return obj;
                }
            }
        }
        return new Objective(0, 0, null, null, null);
    }

    private int calculateReward(ObjectiveType type, int targetAmount) {

        int baseReward;
        float multiplier;

        switch (type) {
            case CLEAR_LEVELS:
                baseReward = 50;
                multiplier = 1.5f;
                break;
            case TAP_TILES:
                baseReward = 30;
                multiplier = 1.f;
                break;
            case USE_POWERUPS:
                baseReward = 40;
                multiplier = 1.2f;
                break;
            case PLAY_TIME:
                baseReward = 20;
                multiplier = 1.3f;
                break;
            case ACHIEVE_COMBO:
                baseReward = 60;
                multiplier = 2.0f;
                break;
            case NO_MISS:
                baseReward = 100;
                multiplier = 2.5f;
                break;
            case REACH_RANK:
                baseReward = 80;
                multiplier = 2.0f;
                break;
            case COLLECT_COINS:
                baseReward = 25;
                multiplier = 1.0f;
                break;
            default:
                baseReward = 10;
                multiplier = 1.0f;
                break;
        }
        return (int) (baseReward + (targetAmount * multiplier));
    }
}
