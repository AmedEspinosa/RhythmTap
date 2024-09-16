package com.example.rhythmtapgame;

import java.util.Objects;

public class Objective {
    private String description;
    private int targetAmount;
    private int currentProgress;
    private int rewardXP;
    private final ObjectiveType type;
    private boolean completed;
    private boolean claimed;
    private String targetDescriptor;
    private String classification;

    public Objective(int targetAmount, int rewardXP, ObjectiveType type, String targetDescriptor, String classification) {
        this.targetAmount = targetAmount;
        this.rewardXP = rewardXP;
        this.type = type;
        this.currentProgress = 0;
        this.completed = false;
        this.claimed = false;
        this.targetDescriptor = targetDescriptor;
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(ObjectiveType objectiveType, int targetAmount) {
        this.description = buildDescription(objectiveType,targetAmount);
    }

    public String buildDescription(ObjectiveType objectiveType, int targetAmount) {
        if (targetAmount > 1) {
            switch (objectiveType) {
                case TAP_TILES:
                    return  "Tap " + targetAmount + " Tiles";
                case USE_POWERUPS:
                   return  "Use " + targetAmount + " Power-Ups";
                case CLEAR_LEVELS:
                    return  "Clear " + targetAmount + " Levels";
                case REACH_RANK:
                    return  "Reach Rank " + targetAmount;
                case ACHIEVE_COMBO:
                    return  "Achieve a " + targetAmount + "-Tap Combo";
                case NO_MISS:
                    return  "Complete " + targetAmount + " Levels With 100% Accuracy";
                case PLAY_TIME:
                    return  "Play for " + targetAmount + " Hours Total";
                case COLLECT_COINS:
                    return  "Collect " + targetAmount + " Beat Coins";
                default:
                    return  "";
            }
        } else if (objectiveType.equals(ObjectiveType.NO_MISS)) {
            return  "Complete " + targetAmount + " Level With 100% Accuracy";
        } else if (objectiveType.equals(ObjectiveType.PLAY_TIME)) {
           return  "Play for " + targetAmount + " Hour Total";

        }
        return "";
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setRewardXP(int rewardXP) {
        this.rewardXP = rewardXP;
    }

    public int getRewardXP() {
        return rewardXP;
    }

    public ObjectiveType getType() {
        return type;
    }

    public String getTargetDescriptor() {
        return targetDescriptor;
    }

    public void setTargetDescriptor(String targetDescriptor) {
        this.targetDescriptor = targetDescriptor;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public void addProgress(int amount) {
        if (!completed) {
            currentProgress += amount;
            if (currentProgress >= targetAmount) {
                currentProgress = targetAmount;
                completed = true;
            }
        }
    }

    public void resetProgress() {
        currentProgress = 0;
        completed = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Objective objective = (Objective) obj;
        return targetAmount == objective.targetAmount &&
                description.equals(objective.description) &&
                type == objective.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, targetAmount, type);
    }
}


