package com.candymatch.storage;

import com.candymatch.analytics.GameSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles JSON file storage for player analytics sessions.
 */
public class JSONManager {

    private static final String JSON_FILE_PATH = "player_game_data.json";

    public static synchronized void exportSessionJSON(GameSession session) {
        if (session == null) return;
        File file = new File(JSON_FILE_PATH);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"PlayerID\": \"").append(escapeJson(session.getPlayerID())).append("\",\n");
            json.append("  \"GameSessionID\": \"").append(escapeJson(session.getGameSessionID())).append("\",\n");
            json.append("  \"GameMode\": \"").append(escapeJson(session.getGameMode())).append("\",\n");
            json.append("  \"Level\": ").append(session.getLevel()).append(",\n");
            json.append("  \"Score\": ").append(session.getScore()).append(",\n");
            json.append("  \"PlayerMoves\": ").append(session.getPlayerMoves()).append(",\n");
            json.append("  \"AIMoves\": ").append(session.getAIMoves()).append(",\n");
            json.append("  \"ValidSwaps\": ").append(session.getValidSwaps()).append(",\n");
            json.append("  \"InvalidSwaps\": ").append(session.getInvalidSwaps()).append(",\n");
            json.append("  \"Matches\": ").append(session.getTotalMatches()).append(",\n");
            json.append("  \"Match3Count\": ").append(session.getMatch3Count()).append(",\n");
            json.append("  \"Match4Count\": ").append(session.getMatch4Count()).append(",\n");
            json.append("  \"Match5Count\": ").append(session.getMatch5Count()).append(",\n");
            json.append("  \"ComboCount\": ").append(session.getComboCount()).append(",\n");
            json.append("  \"SpecialCandiesCreated\": ").append(session.getSpecialCandiesCreated()).append(",\n");
            json.append("  \"SpecialCandiesActivated\": ").append(session.getSpecialCandiesActivated()).append(",\n");
            json.append("  \"HintsUsed\": ").append(session.getHintsUsed()).append(",\n");
            json.append("  \"TimeSeconds\": ").append(session.getTimeSeconds()).append(",\n");
            json.append("  \"WinLoss\": \"").append(escapeJson(session.getWinLoss())).append("\",\n");
            json.append("  \"CompletionStatus\": \"").append(escapeJson(session.getCompletionStatus())).append("\",\n");
            json.append("  \"Timestamp\": \"").append(escapeJson(session.getTimestamp())).append("\"\n");
            json.append("}\n");

            writer.println(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\"", "\\\"");
    }
}
