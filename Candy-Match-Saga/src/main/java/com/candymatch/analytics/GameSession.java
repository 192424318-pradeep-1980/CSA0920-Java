package com.candymatch.analytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Data container holding exact player and AI performance telemetry fields for Power BI CSV/JSON exports.
 */
public class GameSession {
    private String playerID;
    private String gameSessionID;
    private String gameMode;
    private int level;
    private int score;
    private int playerMoves;
    private int aiMoves;
    private int validSwaps;
    private int invalidSwaps;
    private int totalMatches;
    private int match3Count;
    private int match4Count;
    private int match5Count;
    private int comboCount;
    private int specialCandiesCreated;
    private int specialCandiesActivated;
    private int hintsUsed;
    private int timeSeconds;
    private String winLoss;
    private String completionStatus;
    private String timestamp;

    public GameSession(String playerID, String gameMode, int level) {
        this.playerID = playerID;
        this.gameSessionID = "SESS_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.gameMode = gameMode;
        this.level = level;
        this.score = 0;
        this.playerMoves = 0;
        this.aiMoves = 0;
        this.validSwaps = 0;
        this.invalidSwaps = 0;
        this.totalMatches = 0;
        this.match3Count = 0;
        this.match4Count = 0;
        this.match5Count = 0;
        this.comboCount = 0;
        this.specialCandiesCreated = 0;
        this.specialCandiesActivated = 0;
        this.hintsUsed = 0;
        this.timeSeconds = 0;
        this.winLoss = "IN_PROGRESS";
        this.completionStatus = "INCOMPLETE";
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Getters and Setters
    public String getPlayerID() { return playerID; }
    public void setPlayerID(String playerID) { this.playerID = playerID; }

    public String getGameSessionID() { return gameSessionID; }

    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getPlayerMoves() { return playerMoves; }
    public void setPlayerMoves(int playerMoves) { this.playerMoves = playerMoves; }

    public int getAIMoves() { return aiMoves; }
    public void setAIMoves(int aiMoves) { this.aiMoves = aiMoves; }

    public int getValidSwaps() { return validSwaps; }
    public void setValidSwaps(int validSwaps) { this.validSwaps = validSwaps; }

    public int getInvalidSwaps() { return invalidSwaps; }
    public void setInvalidSwaps(int invalidSwaps) { this.invalidSwaps = invalidSwaps; }

    public int getTotalMatches() { return totalMatches; }
    public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }

    public int getMatch3Count() { return match3Count; }
    public void setMatch3Count(int match3Count) { this.match3Count = match3Count; }

    public int getMatch4Count() { return match4Count; }
    public void setMatch4Count(int match4Count) { this.match4Count = match4Count; }

    public int getMatch5Count() { return match5Count; }
    public void setMatch5Count(int match5Count) { this.match5Count = match5Count; }

    public int getComboCount() { return comboCount; }
    public void setComboCount(int comboCount) { this.comboCount = comboCount; }

    public int getSpecialCandiesCreated() { return specialCandiesCreated; }
    public void setSpecialCandiesCreated(int specialCandiesCreated) { this.specialCandiesCreated = specialCandiesCreated; }

    public int getSpecialCandiesActivated() { return specialCandiesActivated; }
    public void setSpecialCandiesActivated(int specialCandiesActivated) { this.specialCandiesActivated = specialCandiesActivated; }

    public int getHintsUsed() { return hintsUsed; }
    public void setHintsUsed(int hintsUsed) { this.hintsUsed = hintsUsed; }

    public int getTimeSeconds() { return timeSeconds; }
    public void setTimeSeconds(int timeSeconds) { this.timeSeconds = timeSeconds; }

    public String getWinLoss() { return winLoss; }
    public void setWinLoss(String winLoss) { this.winLoss = winLoss; }

    public String getCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(String completionStatus) { this.completionStatus = completionStatus; }

    public String getTimestamp() { return timestamp; }
}
