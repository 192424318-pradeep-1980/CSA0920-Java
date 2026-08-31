package com.candymatch.ailab;

import java.awt.Color;
import java.util.*;

/**
 * AIMemory.java
 * Permanent AI Memory System backed by HashMap data structures.
 * - Stores player-created custom candies (customCandiesMap: HashMap<String, Candy>).
 * - Stores custom candy evolution rules (evolutionRulesMap: HashMap<String, CandyRule>).
 * - Enforces duplicate prevention (avoidDuplicates: true).
 * - Enables constant-time O(1) lookup and management (useHashMap: true).
 */
public class AIMemory {

    // HashMap for storing custom candies: Candy Key/ID -> Candy model object
    private final Map<String, Candy> customCandiesMap;

    // HashMap for storing custom evolution rules: Rule ID/Signature -> CandyRule object
    private final Map<String, CandyRule> evolutionRulesMap;

    public AIMemory() {
        this.customCandiesMap = new HashMap<>();
        this.evolutionRulesMap = new HashMap<>();
    }

    /**
     * Stores a custom candy into AI HashMap memory.
     * Prevents duplicates by Candy ID or Name.
     */
    public boolean storeCustomCandy(Candy candy) {
        if (candy == null) return false;
        String key = candy.getId().toUpperCase();

        if (hasCustomCandy(key) || hasCustomCandyByName(candy.getName())) {
            return false; // Avoid duplicates
        }

        customCandiesMap.put(key, candy);
        return true;
    }

    /**
     * Convenience helper to create and store custom candy in AI Memory.
     */
    public boolean storeCustomCandy(String id, String name, Color primaryColor, String shape, String icon) {
        String key = id.toUpperCase();
        if (hasCustomCandy(key) || hasCustomCandyByName(name)) {
            return false; // Avoid duplicates
        }

        Color secondaryColor = new Color(
                Math.min(255, primaryColor.getRed() + 50),
                Math.min(255, primaryColor.getGreen() + 50),
                Math.min(255, primaryColor.getBlue() + 50)
        );

        Candy candy = new Candy(key, name, primaryColor, secondaryColor, 2, "Custom scientist candy: " + name, shape, icon);
        customCandiesMap.put(key, candy);
        return true;
    }

    /**
     * Stores a custom evolution rule into AI HashMap memory.
     * Prevents duplicates by rule structure or ID.
     */
    public boolean storeEvolutionRule(CandyRule rule) {
        if (rule == null) return false;
        String key = getRuleSignature(rule);

        if (evolutionRulesMap.containsKey(key) || evolutionRulesMap.containsKey(rule.getRuleId())) {
            return false; // Avoid duplicates
        }

        evolutionRulesMap.put(key, rule);
        return true;
    }

    /**
     * Generates a unique signature for duplicate checking.
     */
    private String getRuleSignature(CandyRule rule) {
        List<String> inputs = new ArrayList<>(rule.getInputCandyIds());
        Collections.sort(inputs);
        return String.join("+", inputs) + "->" + rule.getResultCandyId().toUpperCase();
    }

    public boolean hasCustomCandy(String keyOrId) {
        if (keyOrId == null) return false;
        return customCandiesMap.containsKey(keyOrId.toUpperCase());
    }

    public boolean hasCustomCandyByName(String candyName) {
        if (candyName == null) return false;
        for (Candy candy : customCandiesMap.values()) {
            if (candy.getName().equalsIgnoreCase(candyName.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEvolutionRule(CandyRule rule) {
        if (rule == null) return false;
        return evolutionRulesMap.containsKey(getRuleSignature(rule));
    }

    public Candy getCustomCandy(String keyOrId) {
        if (keyOrId == null) return null;
        return customCandiesMap.get(keyOrId.toUpperCase());
    }

    public CandyRule getEvolutionRule(String keyOrSignature) {
        if (keyOrSignature == null) return null;
        return evolutionRulesMap.get(keyOrSignature);
    }

    public Map<String, Candy> getCustomCandiesMap() {
        return Collections.unmodifiableMap(customCandiesMap);
    }

    public Map<String, CandyRule> getEvolutionRulesMap() {
        return Collections.unmodifiableMap(evolutionRulesMap);
    }

    public Collection<Candy> getAllCustomCandies() {
        return customCandiesMap.values();
    }

    public Collection<CandyRule> getAllEvolutionRules() {
        return evolutionRulesMap.values();
    }

    public int getCustomCandyCount() {
        return customCandiesMap.size();
    }

    public int getEvolutionRuleCount() {
        return evolutionRulesMap.size();
    }

    public void clearMemory() {
        customCandiesMap.clear();
        evolutionRulesMap.clear();
    }
}
