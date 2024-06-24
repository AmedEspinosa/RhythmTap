package com.example.rhythmtapgame;

public class BeatTile {
    private int x, y;
    private boolean isActive;
    private boolean initiallyToggled;
    private int soundId;
    private boolean partOfCluster;


    public BeatTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.isActive = true;
        this.initiallyToggled = false;
        this.partOfCluster = false;
    }


    public void toggle() {
        this.isActive = !this.isActive;
    }

    public boolean isActive() {
        return this.isActive;
    }

    // Getters for x and y
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isInitiallyToggled() {
        return initiallyToggled;
    }

    public void setInitiallyToggled(boolean initiallyToggled) {
        this.initiallyToggled = initiallyToggled;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public boolean isPartOfCluster() {
        return partOfCluster;
    }

    public void setPartOfCluster(boolean partOfCluster) {
        this.partOfCluster = partOfCluster;
    }
}
