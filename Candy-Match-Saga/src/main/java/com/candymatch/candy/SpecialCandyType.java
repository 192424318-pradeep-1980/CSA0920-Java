package com.candymatch.candy;

/**
 * Enum defining special candy abilities created from match-4, T/L match, or match-5.
 */
public enum SpecialCandyType {
    NONE("Normal", "No special effect"),
    STRIPED_HORIZONTAL("Horizontal Striped", "Clears entire row on match"),
    STRIPED_VERTICAL("Vertical Striped", "Clears entire column on match"),
    WRAPPED("Wrapped Candy", "Explodes 3x3 surrounding area on match"),
    COLOR_BOMB("Color Bomb", "Clears all candies of matching color");

    private final String title;
    private final String description;

    SpecialCandyType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
