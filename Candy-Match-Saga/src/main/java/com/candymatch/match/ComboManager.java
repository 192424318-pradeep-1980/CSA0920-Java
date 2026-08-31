package com.candymatch.match;

/**
 * Manages cascade multiplier and combo counters.
 */
public class ComboManager {
    private int currentCombo = 1;
    private int maxComboInSession = 1;

    public void resetCombo() {
        this.currentCombo = 1;
    }

    public void incrementCombo() {
        this.currentCombo++;
        if (currentCombo > maxComboInSession) {
            this.maxComboInSession = currentCombo;
        }
    }

    public int getCurrentCombo() {
        return currentCombo;
    }

    public int getMaxComboInSession() {
        return maxComboInSession;
    }

    public double getMultiplier() {
        return 1.0 + (currentCombo - 1) * 0.5;
    }

    public int calculateScore(int baseScore) {
        return (int) (baseScore * getMultiplier());
    }
}
