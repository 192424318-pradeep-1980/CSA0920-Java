package com.candymatch.analytics;

import com.candymatch.candy.CandyType;

import java.util.HashMap;
import java.util.Map;

/**
 * Real-time analytics tracker accumulating actions during an active game level session using Java Collections (HashMap).
 */
public class PlayerAnalytics {
    private GameSession session;
    private long startTimeMillis;

    // Collection Framework Usage: HashMaps for player statistics and candy counts
    private final Map<String, Integer> playerStatistics = new HashMap<>();
    private final Map<CandyType, Integer> candyCounts = new HashMap<>();

    public PlayerAnalytics() {
        initStatsMap();
    }

    private void initStatsMap() {
        playerStatistics.put("total_swaps", 0);
        playerStatistics.put("valid_swaps", 0);
        playerStatistics.put("invalid_swaps", 0);
        playerStatistics.put("total_matches", 0);
        playerStatistics.put("hints_used", 0);
    }

    public void startNewSession(String playerID, String gameMode, int level) {
        this.session = new GameSession(playerID, gameMode, level);
        this.startTimeMillis = System.currentTimeMillis();
        candyCounts.clear();
    }

    public GameSession getSession() {
        if (session != null) {
            int elapsed = (int) ((System.currentTimeMillis() - startTimeMillis) / 1000);
            session.setTimeSeconds(elapsed);
        }
        return session;
    }

    public Map<String, Integer> getPlayerStatistics() {
        return playerStatistics;
    }

    public Map<CandyType, Integer> getCandyCounts() {
        return candyCounts;
    }

    public void recordCandyMatched(CandyType type, int count) {
        if (type != null) {
            candyCounts.put(type, candyCounts.getOrDefault(type, 0) + count);
        }
    }

    public void recordValidSwap(boolean isPlayer) {
        if (session == null) return;
        session.setValidSwaps(session.getValidSwaps() + 1);
        playerStatistics.put("valid_swaps", playerStatistics.get("valid_swaps") + 1);
        playerStatistics.put("total_swaps", playerStatistics.get("total_swaps") + 1);

        if (isPlayer) {
            session.setPlayerMoves(session.getPlayerMoves() + 1);
        } else {
            session.setAIMoves(session.getAIMoves() + 1);
        }
    }

    public void recordInvalidSwap() {
        if (session == null) return;
        session.setInvalidSwaps(session.getInvalidSwaps() + 1);
        session.setPlayerMoves(session.getPlayerMoves() + 1);

        playerStatistics.put("invalid_swaps", playerStatistics.get("invalid_swaps") + 1);
        playerStatistics.put("total_swaps", playerStatistics.get("total_swaps") + 1);
    }

    public void recordMatches(int m3, int m4, int m5, int combo) {
        if (session == null) return;
        session.setMatch3Count(session.getMatch3Count() + m3);
        session.setMatch4Count(session.getMatch4Count() + m4);
        session.setMatch5Count(session.getMatch5Count() + m5);
        session.setTotalMatches(session.getTotalMatches() + m3 + m4 + m5);

        playerStatistics.put("total_matches", playerStatistics.get("total_matches") + m3 + m4 + m5);

        if (combo > session.getComboCount()) {
            session.setComboCount(combo);
        }
    }

    public void recordSpecialCandy(int created, int activated) {
        if (session == null) return;
        session.setSpecialCandiesCreated(session.getSpecialCandiesCreated() + created);
        session.setSpecialCandiesActivated(session.getSpecialCandiesActivated() + activated);
    }

    public void recordHintUsed() {
        if (session == null) return;
        session.setHintsUsed(session.getHintsUsed() + 1);
        playerStatistics.put("hints_used", playerStatistics.get("hints_used") + 1);
    }

    public void finishSession(int finalScore, boolean won) {
        if (session == null) return;
        session.setScore(finalScore);
        session.setWinLoss(won ? "WIN" : "LOSS");
        session.setCompletionStatus(won ? "COMPLETED" : "FAILED");
        int elapsed = (int) ((System.currentTimeMillis() - startTimeMillis) / 1000);
        session.setTimeSeconds(elapsed);
    }
}
