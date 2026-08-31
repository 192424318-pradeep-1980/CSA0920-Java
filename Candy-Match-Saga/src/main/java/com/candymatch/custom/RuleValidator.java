package com.candymatch.custom;

/**
 * Validates custom candy definitions and custom rule parameters.
 */
public class RuleValidator {

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static ValidationResult validateCustomCandy(String name, String symbol, int scoreValue, String matchingRule) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Candy name cannot be empty.");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            return new ValidationResult(false, "Candy symbol/emoji cannot be empty.");
        }
        if (scoreValue <= 0) {
            return new ValidationResult(false, "Candy score value must be a positive integer.");
        }
        if (matchingRule == null || matchingRule.trim().isEmpty()) {
            return new ValidationResult(false, "Matching rule description cannot be empty.");
        }
        return new ValidationResult(true, "Valid");
    }

    public static ValidationResult validateCustomRule(String ruleName, String condition, String effect) {
        if (ruleName == null || ruleName.trim().isEmpty()) {
            return new ValidationResult(false, "Rule name cannot be empty.");
        }
        if (condition == null || condition.trim().isEmpty()) {
            return new ValidationResult(false, "Rule condition cannot be empty.");
        }
        if (effect == null || effect.trim().isEmpty()) {
            return new ValidationResult(false, "Rule effect cannot be empty.");
        }
        return new ValidationResult(true, "Valid");
    }
}
