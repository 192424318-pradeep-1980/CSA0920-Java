package com.candymatch.ailab;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Player.java
 * Model representing player state, lab scientist credentials, score, AI level,
 * discovered candies, and completed lab missions.
 * Demonstrates OOP Encapsulation.
 */
public class Player {
    private String name;
    private int score;
    private int moves;
    private int aiLevel;
    private float aiLearningPercentage;
    private Set<String> discoveredCandyIds;
    private Set<String> completedGoalIds;
    private Timestamp lastSavedAt;

    public Player(String name) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : "Scientist Alex";
        this.score = 0;
        this.moves = 30;
        this.aiLevel = 1;
        this.aiLearningPercentage = 0.0f;
        this.discoveredCandyIds = new HashSet<>();
        this.completedGoalIds = new HashSet<>();
        this.lastSavedAt = new Timestamp(System.currentTimeMillis());

        // Default base discoveries
        discoveredCandyIds.add("RED");
        discoveredCandyIds.add("BLUE");
        discoveredCandyIds.add("GREEN");
        discoveredCandyIds.add("YELLOW");
        discoveredCandyIds.add("PURPLE");
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public void setScore(int score) { this.score = score; }

    public int getMoves() { return moves; }
    public void decrementMoves() { if (moves > 0) moves--; }
    public void setMoves(int moves) { this.moves = moves; }

    public int getAiLevel() { return aiLevel; }
    public void setAiLevel(int level) { this.aiLevel = level; }

    public float getAiLearningPercentage() { return aiLearningPercentage; }
    public void setAiLearningPercentage(float percentage) { this.aiLearningPercentage = percentage; }

    public Set<String> getDiscoveredCandyIds() { return discoveredCandyIds; }
    public boolean discoverCandy(String candyId) {
        return discoveredCandyIds.add(candyId);
    }

    public Set<String> getCompletedGoalIds() { return completedGoalIds; }
    public boolean completeGoal(String goalId) {
        return completedGoalIds.add(goalId);
    }

    public Timestamp getLastSavedAt() { return lastSavedAt; }
    public void setLastSavedAt(Timestamp timestamp) { this.lastSavedAt = timestamp; }
}
