package com.candymatch.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository pattern providing search, filter, load, and delete functionality for saved candies and rules.
 */
public class ResearchBook {
    private final CustomCandyManager candyManager;
    private final CustomRuleManager ruleManager;

    public ResearchBook(CustomCandyManager candyManager, CustomRuleManager ruleManager) {
        this.candyManager = candyManager;
        this.ruleManager = ruleManager;
    }

    public List<CustomCandyManager.CustomCandyRecord> searchCandies(String query) {
        if (query == null || query.trim().isEmpty()) {
            return candyManager.getAllCustomCandies();
        }
        String q = query.toLowerCase().trim();
        return candyManager.getAllCustomCandies().stream()
                .filter(c -> c.getName().toLowerCase().contains(q) ||
                             c.getSymbol().contains(q) ||
                             c.getMatchingRule().toLowerCase().contains(q) ||
                             c.getSpecialAbility().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<CustomRuleManager.CustomRuleRecord> searchRules(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ruleManager.getAllRules();
        }
        String q = query.toLowerCase().trim();
        return ruleManager.getAllRules().stream()
                .filter(r -> r.getRuleName().toLowerCase().contains(q) ||
                             r.getCondition().toLowerCase().contains(q) ||
                             r.getEffect().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public boolean deleteCandy(String name) {
        return candyManager.deleteCustomCandy(name);
    }

    public boolean deleteRule(String name) {
        return ruleManager.deleteRule(name);
    }

    public CustomCandyManager getCandyManager() {
        return candyManager;
    }

    public CustomRuleManager getRuleManager() {
        return ruleManager;
    }
}
