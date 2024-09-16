package com.example.rhythmtapgame;

public class LeaderboardEntry {
    private final int rank;
    private final String playerName;
    private final long score;

    public LeaderboardEntry(int rank, String playerName, long score) {
        this.rank = rank;
        this.playerName = playerName;
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getScore() {
        return score;
    }
}
