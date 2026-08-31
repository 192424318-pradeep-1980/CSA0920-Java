package com.candymatch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database.java
 * Manages database queries and persistence for table `player_score`.
 * Saves player name, score, moves, and date into MySQL (or SQLite fallback) via JDBC.
 */
public class Database {

    public Database() {
        createTableIfNotExists();
    }

    /**
     * Creates `player_score` table if it does not already exist in the database.
     */
    public void createTableIfNotExists() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String dbType = DBConnection.getDatabaseType();
        String sql;

        if (dbType.contains("MySQL")) {
            sql = "CREATE TABLE IF NOT EXISTS player_score (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_name VARCHAR(100) NOT NULL, " +
                    "score INT NOT NULL, " +
                    "moves INT NOT NULL, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
        } else {
            // SQLite syntax
            sql = "CREATE TABLE IF NOT EXISTS player_score (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "player_name TEXT NOT NULL, " +
                    "score INTEGER NOT NULL, " +
                    "moves INTEGER NOT NULL, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table player_score: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Saves player game stats into `player_score` table.
     */
    public boolean saveScore(String playerName, int score, int moves) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        String sql = "INSERT INTO player_score (player_name, score, moves, date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, (playerName != null && !playerName.trim().isEmpty()) ? playerName.trim() : "CandyCrusher");
            pstmt.setInt(2, score);
            pstmt.setInt(3, moves);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error saving player score: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Retrieves top scores sorted by score descending.
     */
    public List<PlayerRecord> getTopScores(int limit) {
        List<PlayerRecord> records = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) return records;

        String sql = "SELECT id, player_name, score, moves, date FROM player_score ORDER BY score DESC, date DESC LIMIT ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(new PlayerRecord(
                        rs.getInt("id"),
                        rs.getString("player_name"),
                        rs.getInt("score"),
                        rs.getInt("moves"),
                        rs.getTimestamp("date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching top scores: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }

        return records;
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Data Transfer Object representing a row in `player_score` table.
     */
    public static class PlayerRecord {
        public int id;
        public String playerName;
        public int score;
        public int moves;
        public Timestamp date;

        public PlayerRecord(int id, String playerName, int score, int moves, Timestamp date) {
            this.id = id;
            this.playerName = playerName;
            this.score = score;
            this.moves = moves;
            this.date = date;
        }
    }
}
