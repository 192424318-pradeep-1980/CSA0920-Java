package com.candymatch.ailab;

import java.awt.Point;
import java.util.*;

/**
 * AIAssistant.java
 * Rule-based AI Learning Assistant for the Candy AI Laboratory.
 * Demonstrates Collections Framework (HashMap, HashSet, ArrayList).
 * - Learns rules discovered by player.
 * - Displays AI Learning Percentage & Level.
 * - Provides intelligent hints & suggestions.
 */
public class AIAssistant {
    private Map<String, CandyRule> knownRulesMap;  // ruleId -> CandyRule
    private Set<CandyRule> learnedRulesSet;       // HashSet of learned rules
    private List<String> aiDialogLogs;            // AI assistant conversation logs
    private AIMemory aiMemory;                    // Permanent AI Memory System (HashMap backed)
    private int aiLevel;
    private float learningPercentage;

    private static final int TOTAL_GOAL_RULES = 10;

    public AIAssistant() {
        this.knownRulesMap = new HashMap<>();
        this.learnedRulesSet = new HashSet<>();
        this.aiDialogLogs = new ArrayList<>();
        this.aiMemory = new AIMemory();
        this.aiLevel = 1;
        this.learningPercentage = 0.0f;

        initializeDefaultRules();
    }

    /**
     * Initializes default lab rules.
     */
    private void initializeDefaultRules() {
        // Base Tier 1 to Tier 2 Evolutions
        registerRule(new CandyRule("R1", Arrays.asList("RED", "RED", "RED"), "RUBY", "Synthesize 3 Red Berries into Ruby Gem", false));
        registerRule(new CandyRule("R2", Arrays.asList("BLUE", "BLUE", "BLUE"), "CRYSTAL", "Synthesize 3 Blue Sapphires into Crystal Quartz", false));
        registerRule(new CandyRule("R3", Arrays.asList("GREEN", "GREEN", "GREEN"), "EMERALD", "Synthesize 3 Lime Candies into Emerald Essence", false));
        registerRule(new CandyRule("R4", Arrays.asList("YELLOW", "YELLOW", "YELLOW"), "SUNBURST", "Synthesize 3 Sunbursts into Solar Flare", false));
        registerRule(new CandyRule("R5", Arrays.asList("PURPLE", "PURPLE", "PURPLE"), "AMETHYST", "Synthesize 3 Purple Grapes into Amethyst Orb", false));

        // Advanced Tier 2 to Tier 3 Evolutions
        registerRule(new CandyRule("R6", Arrays.asList("RUBY", "CRYSTAL"), "GALAXY", "Combine Ruby Gem & Crystal Quartz into Cosmic Galaxy Candy", false));

        logAiMessage("🤖 AI Assistant initialized! Ready to learn new candy combinations!");
    }

    public void registerRule(CandyRule rule) {
        knownRulesMap.put(rule.getRuleId(), rule);
        learnedRulesSet.add(rule);
        recalculateProgress();
    }

    /**
     * Called when player discovers or creates a new rule.
     */
    public boolean learnRule(CandyRule rule) {
        if (!learnedRulesSet.contains(rule)) {
            learnedRulesSet.add(rule);
            knownRulesMap.put(rule.getRuleId(), rule);
            if (rule.isCustom()) {
                aiMemory.storeEvolutionRule(rule);
            }
            recalculateProgress();
            logAiMessage("💡 AI Learned New Rule! (" + rule + ")");
            return true;
        }
        return false;
    }

    /**
     * Permanently remembers a custom candy created by the player in AI HashMap memory.
     */
    public boolean rememberCustomCandy(Candy candy) {
        boolean stored = aiMemory.storeCustomCandy(candy);
        if (stored) {
            logAiMessage("🧠 [AI Memory] Permanently remembered custom candy: " + candy.getName() + " (" + candy.getIconSymbol() + ")!");
        }
        return stored;
    }

    public boolean rememberCustomCandy(String id, String name, java.awt.Color primaryColor, String shape, String icon) {
        boolean stored = aiMemory.storeCustomCandy(id, name, primaryColor, shape, icon);
        if (stored) {
            logAiMessage("🧠 [AI Memory] Permanently remembered custom candy: " + name + " (" + icon + ")!");
        }
        return stored;
    }

    /**
     * Evaluates a candy combination against known rules.
     */
    public CandyRule findMatchingRule(List<String> inputCandyIds) {
        for (CandyRule rule : learnedRulesSet) {
            if (rule.matches(inputCandyIds)) {
                return rule;
            }
        }
        return null;
    }

    private void recalculateProgress() {
        this.learningPercentage = Math.min(100.0f, (learnedRulesSet.size() / (float) TOTAL_GOAL_RULES) * 100.0f);
        this.aiLevel = 1 + (learnedRulesSet.size() / 2);
    }

    public void logAiMessage(String message) {
        aiDialogLogs.add("[" + new java.util.Date().toString().substring(11, 19) + "] " + message);
        if (aiDialogLogs.size() > 50) {
            aiDialogLogs.remove(0);
        }
    }

    /**
     * AI Board Hint Generator.
     * Scans board candies to suggest a potential valid evolution combination.
     */
    public String generateHint(GameBoard board) {
        Candy[][] grid = board.getGrid();
        Map<String, List<Point>> candyPositions = new HashMap<>();

        for (int r = 0; r < GameBoard.ROWS; r++) {
            for (int c = 0; c < GameBoard.COLS; c++) {
                Candy cObj = grid[r][c];
                if (cObj != null) {
                    candyPositions.computeIfAbsent(cObj.getId(), k -> new ArrayList<>()).add(new Point(c, r));
                }
            }
        }

        for (CandyRule rule : learnedRulesSet) {
            List<String> inputs = rule.getInputCandyIds();
            boolean possible = true;

            Map<String, Integer> reqCounts = new HashMap<>();
            for (String inId : inputs) {
                reqCounts.put(inId, reqCounts.getOrDefault(inId, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : reqCounts.entrySet()) {
                List<Point> pts = candyPositions.get(entry.getKey());
                if (pts == null || pts.size() < entry.getValue()) {
                    possible = false;
                    break;
                }
            }

            if (possible) {
                return "💡 AI Hint: Drag and merge " + String.join(" + ", inputs) + " to synthesize " + rule.getResultCandyId() + "!";
            }
        }

        return "💡 AI Hint: Merge identical adjacent candies to discover new evolution recipes!";
    }

    public Map<String, CandyRule> getKnownRulesMap() { return knownRulesMap; }
    public Set<CandyRule> getLearnedRulesSet() { return learnedRulesSet; }
    public List<String> getAiDialogLogs() { return aiDialogLogs; }
    public AIMemory getAiMemory() { return aiMemory; }
    public int getAiLevel() { return aiLevel; }
    public float getLearningPercentage() { return learningPercentage; }
}
