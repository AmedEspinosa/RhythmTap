package com.example.rhythmtapgame;

import java.util.Objects;

public class Objective {
    private String description;
    private int targetAmount;
    private int currentProgress;
    private int rewardXP;
    private ObjectiveType type;
    private boolean completed;
    private boolean claimed;
    private String targetDescriptor;
    private String classification;

    public Objective(String description, int targetAmount, int rewardXP, ObjectiveType type, String targetDescriptor, String classification) {
        this.description = description;
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

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getCurrentProgress() {
        return currentProgress;
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


