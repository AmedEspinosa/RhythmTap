package com.example.rhythmtapgame;

public class BeatTile {
    private int x, y;
    private boolean isActive;
    private boolean initiallyToggled;


    public BeatTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.isActive = true;
        this.initiallyToggled = false;
    }

    public void toggle() {
        this.isActive = !this.isActive;
    }

    public boolean isActive() {
        return this.isActive;
    }

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
}

