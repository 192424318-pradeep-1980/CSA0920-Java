package com.candymatch.custom;

import com.candymatch.exceptions.InvalidRuleException;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom user-defined game rules and mechanics.
 */
public class CustomRuleManager {

    public static class CustomRuleRecord {
        private String ruleName;
        private String condition;
        private String effect;

        public CustomRuleRecord(String ruleName, String condition, String effect) {
            this.ruleName = ruleName;
            this.condition = condition;
            this.effect = effect;
        }

        public String getRuleName() { return ruleName; }
        public String getCondition() { return condition; }
        public String getEffect() { return effect; }
    }

    private final List<CustomRuleRecord> customRules = new ArrayList<>();

    public CustomRuleManager() {
        try {
            addRule(new CustomRuleRecord("Double Combo Multiplier", "Cascade >= 3", "2x score for next match"));
            addRule(new CustomRuleRecord("Free Hint Bonus", "Score >= 5000", "Gain 1 extra hint"));
        } catch (InvalidRuleException ignored) {}
    }

    public boolean addRule(CustomRuleRecord rule) throws InvalidRuleException {
        if (rule == null) {
            throw new InvalidRuleException("Rule definition cannot be null.");
        }
        RuleValidator.ValidationResult val = RuleValidator.validateCustomRule(
                rule.getRuleName(), rule.getCondition(), rule.getEffect()
        );
        if (!val.isValid()) {
            throw new InvalidRuleException(val.getErrorMessage());
        }

        for (CustomRuleRecord existing : customRules) {
            if (existing.getRuleName().equalsIgnoreCase(rule.getRuleName())) {
                throw new InvalidRuleException("A custom rule named '" + rule.getRuleName() + "' already exists.");
            }
        }
        customRules.add(rule);
        return true;
    }

    public List<CustomRuleRecord> getAllRules() {
        return new ArrayList<>(customRules);
    }

    public boolean deleteRule(String ruleName) {
        return customRules.removeIf(r -> r.getRuleName().equalsIgnoreCase(ruleName));
    }
}
