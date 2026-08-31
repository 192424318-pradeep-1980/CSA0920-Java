package com.candymatch.analytics;

import com.candymatch.exceptions.DatabaseException;
import com.candymatch.storage.CSVManager;
import com.candymatch.storage.DatabaseManager;
import com.candymatch.storage.JSONManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinator for real-time analytics tracking, CSV/JSON file exports, and JDBC MySQL database persistence.
 */
public class AnalyticsManager {
    private final PlayerAnalytics playerAnalytics = new PlayerAnalytics();
    private final List<GameSession> sessionHistory = new ArrayList<>();

    public PlayerAnalytics getPlayerAnalytics() {
        return playerAnalytics;
    }

    public List<GameSession> getSessionHistory() {
        return sessionHistory;
    }

    public void startLevelSession(String playerID, String gameMode, int level) {
        playerAnalytics.startNewSession(playerID, gameMode, level);
    }

    public void completeSession(int finalScore, boolean won) {
        playerAnalytics.finishSession(finalScore, won);
        GameSession session = playerAnalytics.getSession();
        
        if (session != null) {
            // Collection Usage: Store completed session in ArrayList
            sessionHistory.add(session);

            // Export to CSV and JSON
            CSVManager.exportSessionData(session);
            JSONManager.exportSessionJSON(session);

            // Export to MySQL JDBC Database
            try {
                DatabaseManager.getInstance().saveGameSession(session);
            } catch (DatabaseException e) {
                System.err.println("JDBC Persistence Warning: " + e.getMessage());
            }
        }
    }
}
