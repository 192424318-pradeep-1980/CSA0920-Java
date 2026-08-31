package com.candymatch.storage;

import com.candymatch.analytics.GameSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles saving telemetry sessions to Power BI compatible CSV data files.
 */
public class CSVManager {

    private static final String PLAYER_DATA_CSV = "player_game_data.csv";
    private static final String LEVEL_PERFORMANCE_CSV = "level_performance.csv";
    private static final String AI_PERFORMANCE_CSV = "ai_performance.csv";

    public static synchronized void exportSessionData(GameSession session) {
        if (session == null) return;
        writePlayerDataCSV(session);
        writeLevelPerformanceCSV(session);
        if ("PLAYER_VS_AI".equalsIgnoreCase(session.getGameMode())) {
            writeAIPerformanceCSV(session);
        }
    }

    private static void writePlayerDataCSV(GameSession session) {
        File file = new File(PLAYER_DATA_CSV);
        boolean exists = file.exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (!exists) {
                writer.println("PlayerID,GameSessionID,GameMode,Level,Score,PlayerMoves,AIMoves,ValidSwaps,InvalidSwaps,Matches,Match3Count,Match4Count,Match5Count,ComboCount,SpecialCandiesCreated,SpecialCandiesActivated,HintsUsed,TimeSeconds,WinLoss,CompletionStatus,Timestamp");
            }
            writer.printf("%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s,%s,%s%n",
                    session.getPlayerID(),
                    session.getGameSessionID(),
                    session.getGameMode(),
                    session.getLevel(),
                    session.getScore(),
                    session.getPlayerMoves(),
                    session.getAIMoves(),
                    session.getValidSwaps(),
                    session.getInvalidSwaps(),
                    session.getTotalMatches(),
                    session.getMatch3Count(),
                    session.getMatch4Count(),
                    session.getMatch5Count(),
                    session.getComboCount(),
                    session.getSpecialCandiesCreated(),
                    session.getSpecialCandiesActivated(),
                    session.getHintsUsed(),
                    session.getTimeSeconds(),
                    session.getWinLoss(),
                    session.getCompletionStatus(),
                    session.getTimestamp()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLevelPerformanceCSV(GameSession session) {
        File file = new File(LEVEL_PERFORMANCE_CSV);
        boolean exists = file.exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (!exists) {
                writer.println("Level,GameMode,Score,TotalMatches,ComboMax,TimeSeconds,WinLoss,Timestamp");
            }
            writer.printf("%d,%s,%d,%d,%d,%d,%s,%s%n",
                    session.getLevel(),
                    session.getGameMode(),
                    session.getScore(),
                    session.getTotalMatches(),
                    session.getComboCount(),
                    session.getTimeSeconds(),
                    session.getWinLoss(),
                    session.getTimestamp()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeAIPerformanceCSV(GameSession session) {
        File file = new File(AI_PERFORMANCE_CSV);
        boolean exists = file.exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (!exists) {
                writer.println("GameSessionID,Level,AIMoves,PlayerMoves,TotalMatches,WinLoss,Timestamp");
            }
            writer.printf("%s,%d,%d,%d,%d,%s,%s%n",
                    session.getGameSessionID(),
                    session.getLevel(),
                    session.getAIMoves(),
                    session.getPlayerMoves(),
                    session.getTotalMatches(),
                    session.getWinLoss(),
                    session.getTimestamp()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
