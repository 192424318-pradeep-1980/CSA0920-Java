package com.candymatch.ai;

/**
 * Statistics and profile tracker for AI Opponent decisions.
 */
public class AIOpponent {
    private int movesMade = 0;
    private int totalScore = 0;
    private int matchesCreated = 0;
    private int specialCandiesCreated = 0;

    public void reset() {
        movesMade = 0;
        totalScore = 0;
        matchesCreated = 0;
        specialCandiesCreated = 0;
    }

    public void recordMove(int scoreEarned, int matches, int specials) {
        movesMade++;
        totalScore += scoreEarned;
        matchesCreated += matches;
        specialCandiesCreated += specials;
    }

    public int getMovesMade() {
        return movesMade;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getMatchesCreated() {
        return matchesCreated;
    }

    public int getSpecialCandiesCreated() {
        return specialCandiesCreated;
    }
}
