package com.candymatch.custom;

import com.candymatch.candy.Candy;
import com.candymatch.exceptions.InvalidCandyException;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages creation, collection, and verification of custom user-created candies.
 */
public class CustomCandyManager {

    public static class CustomCandyRecord {
        private String name;
        private String symbol;
        private Color color;
        private int scoreValue;
        private String matchingRule;
        private String specialAbility;

        public CustomCandyRecord(String name, String symbol, Color color, int scoreValue, String matchingRule, String specialAbility) {
            this.name = name;
            this.symbol = symbol;
            this.color = color;
            this.scoreValue = scoreValue;
            this.matchingRule = matchingRule;
            this.specialAbility = specialAbility;
        }

        public String getName() { return name; }
        public String getSymbol() { return symbol; }
        public Color getColor() { return color; }
        public int getScoreValue() { return scoreValue; }
        public String getMatchingRule() { return matchingRule; }
        public String getSpecialAbility() { return specialAbility; }

        public Candy toCandy() {
            return new Candy(name, symbol, color, scoreValue);
        }
    }

    private final List<CustomCandyRecord> customCandies = new ArrayList<>();

    public CustomCandyManager() {
        // Pre-register default custom candy sample
        try {
            addCustomCandy(new CustomCandyRecord("Rainbow Star", "⭐", new Color(255, 105, 180), 100, "Match 3 Any", "Explodes surrounding candies"));
        } catch (InvalidCandyException ignored) {}
    }

    public boolean addCustomCandy(CustomCandyRecord candyRecord) throws InvalidCandyException {
        if (candyRecord == null) {
            throw new InvalidCandyException("Custom candy definition cannot be null.");
        }
        RuleValidator.ValidationResult val = RuleValidator.validateCustomCandy(
                candyRecord.getName(), candyRecord.getSymbol(), candyRecord.getScoreValue(), candyRecord.getMatchingRule()
        );
        if (!val.isValid()) {
            throw new InvalidCandyException(val.getErrorMessage());
        }

        // Unique name check
        for (CustomCandyRecord existing : customCandies) {
            if (existing.getName().equalsIgnoreCase(candyRecord.getName())) {
                throw new InvalidCandyException("A custom candy named '" + candyRecord.getName() + "' already exists.");
            }
        }
        customCandies.add(candyRecord);
        return true;
    }

    public List<CustomCandyRecord> getAllCustomCandies() {
        return new ArrayList<>(customCandies);
    }

    public boolean deleteCustomCandy(String name) {
        return customCandies.removeIf(c -> c.getName().equalsIgnoreCase(name));
    }
}
