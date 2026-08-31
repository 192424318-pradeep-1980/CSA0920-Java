package com.candymatch.storage;

import com.candymatch.analytics.GameSession;
import com.candymatch.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager.java
 * Handles MySQL JDBC database integration for `candy_match_saga`.
 * Uses PreparedStatement queries for inserting players, game sessions, and player statistics.
 * Includes graceful connection fallback if MySQL service is inactive.
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DB_NAME = "candy_match_saga";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private String currentDbType = "Disconnected";

    private DatabaseManager() {
        try {
            initDatabase();
        } catch (DatabaseException e) {
            System.err.println("DatabaseManager Initialization Notice: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        String host = System.getenv().getOrDefault("MYSQL_HOST", DEFAULT_HOST);
        String portStr = System.getenv().getOrDefault("MYSQL_PORT", String.valueOf(DEFAULT_PORT));
        int port = Integer.parseInt(portStr);
        String user = System.getenv().getOrDefault("MYSQL_USER", DEFAULT_USER);
        String pass = System.getenv().getOrDefault("MYSQL_PASSWORD", DEFAULT_PASS);

        // 1. Try MySQL Connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to MySQL server to ensure candy_match_saga database exists
            String serverUrl = "jdbc:mysql://" + host + ":" + port + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection tempConn = DriverManager.getConnection(serverUrl, user, pass);
                 Statement stmt = tempConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            Connection conn = DriverManager.getConnection(dbUrl, user, pass);
            currentDbType = "MySQL (" + DB_NAME + ")";
            return conn;
        } catch (Exception e) {
            // MySQL unavailable; fall back to SQLite embedded DB
        }

        // 2. Try SQLite Local Fallback
        try {
            Class.forName("org.sqlite.JDBC");
            String sqliteUrl = "jdbc:sqlite:candy_match_saga.db";
            Connection conn = DriverManager.getConnection(sqliteUrl);
            currentDbType = "SQLite (Local Fallback)";
            return conn;
        } catch (Exception e) {
            throw new SQLException("Unable to establish MySQL or SQLite JDBC connection.", e);
        }
    }

    public void initDatabase() throws DatabaseException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            if (currentDbType.contains("MySQL")) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                        "player_id VARCHAR(50) PRIMARY KEY, " +
                        "player_name VARCHAR(100) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS game_sessions (" +
                        "session_id VARCHAR(50) PRIMARY KEY, " +
                        "player_id VARCHAR(50) NOT NULL, " +
                        "level INT NOT NULL, " +
                        "score INT NOT NULL, " +
                        "moves INT NOT NULL, " +
                        "time_seconds INT NOT NULL, " +
                        "matches INT NOT NULL, " +
                        "combo_count INT NOT NULL, " +
                        "special_candies_created INT NOT NULL, " +
                        "hints_used INT NOT NULL, " +
                        "player_moves INT NOT NULL, " +
                        "ai_moves INT NOT NULL, " +
                        "win_loss VARCHAR(20) NOT NULL, " +
                        "completion_status VARCHAR(20) NOT NULL, " +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_statistics (" +
                        "player_id VARCHAR(50) PRIMARY KEY, " +
                        "total_games INT DEFAULT 0, " +
                        "total_wins INT DEFAULT 0, " +
                        "total_losses INT DEFAULT 0, " +
                        "highest_score INT DEFAULT 0, " +
                        "average_score DOUBLE DEFAULT 0.0, " +
                        "FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE)");
            } else {
                // SQLite syntax
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                        "player_id TEXT PRIMARY KEY, " +
                        "player_name TEXT NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS game_sessions (" +
                        "session_id TEXT PRIMARY KEY, " +
                        "player_id TEXT NOT NULL, " +
                        "level INTEGER NOT NULL, " +
                        "score INTEGER NOT NULL, " +
                        "moves INTEGER NOT NULL, " +
                        "time_seconds INTEGER NOT NULL, " +
                        "matches INTEGER NOT NULL, " +
                        "combo_count INTEGER NOT NULL, " +
                        "special_candies_created INTEGER NOT NULL, " +
                        "hints_used INTEGER NOT NULL, " +
                        "player_moves INTEGER NOT NULL, " +
                        "ai_moves INTEGER NOT NULL, " +
                        "win_loss TEXT NOT NULL, " +
                        "completion_status TEXT NOT NULL, " +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_statistics (" +
                        "player_id TEXT PRIMARY KEY, " +
                        "total_games INTEGER DEFAULT 0, " +
                        "total_wins INTEGER DEFAULT 0, " +
                        "total_losses INTEGER DEFAULT 0, " +
                        "highest_score INTEGER DEFAULT 0, " +
                        "average_score REAL DEFAULT 0.0)");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database tables: " + e.getMessage(), e);
        }
    }

    public void savePlayer(String playerId, String playerName) throws DatabaseException {
        String sql;
        if (currentDbType.contains("MySQL")) {
            sql = "INSERT INTO players (player_id, player_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE player_name = ?";
        } else {
            sql = "INSERT OR REPLACE INTO players (player_id, player_name) VALUES (?, ?)";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerId);
            pstmt.setString(2, playerName);
            if (currentDbType.contains("MySQL")) {
                pstmt.setString(3, playerName);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error saving player record: " + e.getMessage(), e);
        }
    }

    public void saveGameSession(GameSession session) throws DatabaseException {
        if (session == null) return;
        savePlayer(session.getPlayerID(), session.getPlayerID());

        String sql = "INSERT INTO game_sessions (session_id, player_id, level, score, moves, time_seconds, matches, " +
                "combo_count, special_candies_created, hints_used, player_moves, ai_moves, win_loss, completion_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, session.getGameSessionID());
            pstmt.setString(2, session.getPlayerID());
            pstmt.setInt(3, session.getLevel());
            pstmt.setInt(4, session.getScore());
            pstmt.setInt(5, session.getPlayerMoves() + session.getAIMoves());
            pstmt.setInt(6, session.getTimeSeconds());
            pstmt.setInt(7, session.getTotalMatches());
            pstmt.setInt(8, session.getComboCount());
            pstmt.setInt(9, session.getSpecialCandiesCreated());
            pstmt.setInt(10, session.getHintsUsed());
            pstmt.setInt(11, session.getPlayerMoves());
            pstmt.setInt(12, session.getAIMoves());
            pstmt.setString(13, session.getWinLoss());
            pstmt.setString(14, session.getCompletionStatus());

            pstmt.executeUpdate();

            updatePlayerStatistics(session.getPlayerID(), session.getScore(), "WIN".equalsIgnoreCase(session.getWinLoss()));
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting game session JDBC record: " + e.getMessage(), e);
        }
    }

    public void updatePlayerStatistics(String playerId, int finalScore, boolean won) throws DatabaseException {
        try (Connection conn = getConnection()) {
            int totalGames = 0, totalWins = 0, totalLosses = 0, highestScore = 0;
            double avgScore = 0.0;

            String selectSql = "SELECT total_games, total_wins, total_losses, highest_score, average_score FROM player_statistics WHERE player_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, playerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        totalGames = rs.getInt("total_games");
                        totalWins = rs.getInt("total_wins");
                        totalLosses = rs.getInt("total_losses");
                        highestScore = rs.getInt("highest_score");
                        avgScore = rs.getDouble("average_score");
                    }
                }
            }

            int newTotalGames = totalGames + 1;
            int newWins = won ? totalWins + 1 : totalWins;
            int newLosses = won ? totalLosses : totalLosses + 1;
            int newHighestScore = Math.max(highestScore, finalScore);
            double newAvgScore = ((avgScore * totalGames) + finalScore) / newTotalGames;

            String upsertSql;
            if (currentDbType.contains("MySQL")) {
                upsertSql = "INSERT INTO player_statistics (player_id, total_games, total_wins, total_losses, highest_score, average_score) " +
                        "VALUES (?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE total_games = ?, total_wins = ?, total_losses = ?, highest_score = ?, average_score = ?";
            } else {
                upsertSql = "INSERT OR REPLACE INTO player_statistics (player_id, total_games, total_wins, total_losses, highest_score, average_score) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
                pstmt.setString(1, playerId);
                pstmt.setInt(2, newTotalGames);
                pstmt.setInt(3, newWins);
                pstmt.setInt(4, newLosses);
                pstmt.setInt(5, newHighestScore);
                pstmt.setDouble(6, newAvgScore);

                if (currentDbType.contains("MySQL")) {
                    pstmt.setInt(7, newTotalGames);
                    pstmt.setInt(8, newWins);
                    pstmt.setInt(9, newLosses);
                    pstmt.setInt(10, newHighestScore);
                    pstmt.setDouble(11, newAvgScore);
                }

                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error updating player statistics JDBC record: " + e.getMessage(), e);
        }
    }

    public List<GameSession> getGameSessions(String playerId) throws DatabaseException {
        List<GameSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM game_sessions WHERE player_id = ? ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GameSession session = new GameSession(rs.getString("player_id"), "PLAYER_ONLY", rs.getInt("level"));
                    session.setScore(rs.getInt("score"));
                    session.setPlayerMoves(rs.getInt("player_moves"));
                    session.setAIMoves(rs.getInt("ai_moves"));
                    session.setTimeSeconds(rs.getInt("time_seconds"));
                    session.setTotalMatches(rs.getInt("matches"));
                    session.setComboCount(rs.getInt("combo_count"));
                    session.setSpecialCandiesCreated(rs.getInt("special_candies_created"));
                    session.setHintsUsed(rs.getInt("hints_used"));
                    session.setWinLoss(rs.getString("win_loss"));
                    session.setCompletionStatus(rs.getString("completion_status"));
                    sessions.add(session);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error reading game sessions from database: " + e.getMessage(), e);
        }
        return sessions;
    }

    public String getCurrentDbType() {
        return currentDbType;
    }
}
