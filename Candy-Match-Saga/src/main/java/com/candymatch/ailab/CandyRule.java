package com.candymatch.ailab;

import java.util.*;

/**
 * CandyRule.java
 * Represents a Candy Evolution / Combination Rule in the AI Laboratory.
 * Example:
 * - Red + Red + Red -> Ruby Candy
 * - Blue + Blue + Blue -> Crystal Candy
 * - Ruby + Crystal -> Galaxy Candy
 */
public class CandyRule {
    private String ruleId;
    private List<String> inputCandyIds;
    private String resultCandyId;
    private String description;
    private boolean isCustom; // Player created custom rule vs default lab rule

    public CandyRule(String ruleId, List<String> inputCandyIds, String resultCandyId, String description, boolean isCustom) {
        this.ruleId = ruleId;
        this.inputCandyIds = new ArrayList<>(inputCandyIds);
        Collections.sort(this.inputCandyIds); // Normalized sorted input order
        this.resultCandyId = resultCandyId.toUpperCase();
        this.description = description;
        this.isCustom = isCustom;
    }

    public String getRuleId() { return ruleId; }
    public List<String> getInputCandyIds() { return inputCandyIds; }
    public String getResultCandyId() { return resultCandyId; }
    public String getDescription() { return description; }
    public boolean isCustom() { return isCustom; }

    /**
     * Checks if a provided list of candy IDs matches this evolution rule.
     */
    public boolean matches(List<String> provided) {
        if (provided == null || provided.size() != inputCandyIds.size()) {
            return false;
        }
        List<String> sortedProvided = new ArrayList<>(provided);
        Collections.sort(sortedProvided);

        for (int i = 0; i < inputCandyIds.size(); i++) {
            if (!inputCandyIds.get(i).equalsIgnoreCase(sortedProvided.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandyRule)) return false;
        CandyRule candyRule = (CandyRule) o;
        return Objects.equals(inputCandyIds, candyRule.inputCandyIds) &&
                Objects.equals(resultCandyId, candyRule.resultCandyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputCandyIds, resultCandyId);
    }

    @Override
    public String toString() {
        return String.join(" + ", inputCandyIds) + " ➔ " + resultCandyId;
    }
}
