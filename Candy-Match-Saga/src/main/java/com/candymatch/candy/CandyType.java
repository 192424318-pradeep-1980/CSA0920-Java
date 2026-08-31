package com.candymatch.candy;

import java.awt.Color;

/**
 * Enum representing standard candy types with distinct visual properties.
 */
public enum CandyType {
    RED("Strawberry Red", new Color(235, 45, 85), "🍓", 30),
    BLUE("Blueberry Blue", new Color(45, 140, 240), "🫐", 30),
    GREEN("Apple Green", new Color(45, 200, 110), "🍏", 30),
    YELLOW("Lemon Yellow", new Color(250, 200, 30), "🍋", 30),
    PURPLE("Grape Purple", new Color(160, 60, 230), "🍇", 30),
    ORANGE("Orange Citrus", new Color(255, 130, 30), "🍊", 30),
    CUSTOM("Custom Candy", new Color(255, 105, 180), "⭐", 50);

    private final String displayName;
    private final Color color;
    private final String symbol;
    private final int baseScore;

    CandyType(String displayName, Color color, String symbol, int baseScore) {
        this.displayName = displayName;
        this.color = color;
        this.symbol = symbol;
        this.baseScore = baseScore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getBaseScore() {
        return baseScore;
    }

    public static CandyType getRandomStandardType() {
        CandyType[] standard = {RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE};
        return standard[(int) (Math.random() * standard.length)];
    }
}
