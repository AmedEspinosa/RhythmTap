package com.example.rhythmtapgame;

public class BeatTile {
    private int x, y;
    private boolean isActive;

    public BeatTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.isActive = true;
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





}
