package com.candymatch.ailab;

import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Database.java
 * Database Persistence Manager for `candy_ai_lab` and `custom_candies` MySQL JDBC tables.
 */
public class Database {

    public Database() {
        new AuthenticationManager().createUsersTableIfNotExists();
        createTablesIfNotExists();
    }

    public void createTablesIfNotExists() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String dbType = DBConnection.getDatabaseType();
        String sqlLab;
        String sqlCandies;

        if (dbType.contains("MySQL")) {
            sqlLab = "CREATE TABLE IF NOT EXISTS candy_ai_lab (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT DEFAULT NULL, " +
                    "player_name VARCHAR(100) NOT NULL, " +
                    "score INT NOT NULL DEFAULT 0, " +
                    "ai_level INT NOT NULL DEFAULT 1, " +
                    "ai_learning_pct FLOAT NOT NULL DEFAULT 0.0, " +
                    "learned_rules TEXT, " +
                    "discovered_candies TEXT, " +
                    "completed_goals TEXT, " +
                    "date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            sqlCandies = "CREATE TABLE IF NOT EXISTS custom_candies (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT DEFAULT NULL, " +
                    "candy_name VARCHAR(100) NOT NULL, " +
                    "color VARCHAR(30) NOT NULL, " +
                    "shape VARCHAR(30) NOT NULL, " +
                    "icon VARCHAR(10) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
        } else {
            sqlLab = "CREATE TABLE IF NOT EXISTS candy_ai_lab (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER DEFAULT NULL, " +
                    "player_name TEXT NOT NULL, " +
                    "score INTEGER NOT NULL DEFAULT 0, " +
                    "ai_level INTEGER NOT NULL DEFAULT 1, " +
                    "ai_learning_pct REAL NOT NULL DEFAULT 0.0, " +
                    "learned_rules TEXT, " +
                    "discovered_candies TEXT, " +
                    "completed_goals TEXT, " +
                    "date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            sqlCandies = "CREATE TABLE IF NOT EXISTS custom_candies (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER DEFAULT NULL, " +
                    "candy_name TEXT NOT NULL, " +
                    "color TEXT NOT NULL, " +
                    "shape TEXT NOT NULL, " +
                    "icon TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlLab);
            stmt.executeUpdate(sqlCandies);
        } catch (SQLException e) {
            System.err.println("Error creating database tables: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Saves a new custom candy definition permanently in MySQL table `custom_candies`.
     */
    public boolean saveCustomCandy(String candyName, String colorName, String shape, String icon) {
        return saveCustomCandy(candyName, colorName, shape, icon, getCurrentUserId());
    }

    public boolean saveCustomCandy(String candyName, String colorName, String shape, String icon, int userId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        String sql = "INSERT INTO custom_candies (candy_name, color, shape, icon, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, candyName);
            pstmt.setString(2, colorName);
            pstmt.setString(3, shape);
            pstmt.setString(4, icon);
            if (userId > 0) {
                pstmt.setInt(5, userId);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error saving custom candy: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Auto-loads all custom candies from MySQL table `custom_candies` on startup into registry and AI Memory.
     */
    public void loadCustomCandies(Player player) {
        loadCustomCandies(player, null, getCurrentUserId());
    }

    public void loadCustomCandies(Player player, AIAssistant ai) {
        loadCustomCandies(player, ai, getCurrentUserId());
    }

    public void loadCustomCandies(Player player, AIAssistant ai, int userId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String sql;
        if (userId > 0) {
            sql = "SELECT candy_name, color, shape, icon FROM custom_candies WHERE user_id = ? ORDER BY id ASC";
        } else {
            sql = "SELECT candy_name, color, shape, icon FROM custom_candies WHERE user_id IS NULL ORDER BY id ASC";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userId > 0) {
                pstmt.setInt(1, userId);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String candyName = rs.getString("candy_name");
                String colorName = rs.getString("color");
                String shape = rs.getString("shape");
                String icon = rs.getString("icon");

                String id = candyName.toUpperCase().replaceAll("\\s+", "_");
                Color primaryColor = parseColor(colorName);

                Candy.registerCustomCandy(id, candyName, primaryColor, shape, icon);
                if (ai != null) {
                    ai.getAiMemory().storeCustomCandy(id, candyName, primaryColor, shape, icon);
                }
                if (player != null) {
                    player.discoverCandy(id);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading custom candies: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean saveProgress(Player player, AIAssistant ai) {
        return saveProgress(player, ai, getCurrentUserId());
    }

    public boolean saveProgress(Player player, AIAssistant ai, int userId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        List<String> ruleStrs = new ArrayList<>();
        for (CandyRule rule : ai.getLearnedRulesSet()) {
            String ruleSerialized = rule.getRuleId() + "|" +
                    String.join(",", rule.getInputCandyIds()) + "|" +
                    rule.getResultCandyId() + "|" +
                    rule.getDescription() + "|" +
                    rule.isCustom();
            ruleStrs.add(ruleSerialized);
        }

        String learnedRulesStr = String.join(";", ruleStrs);
        String discoveredCandiesStr = String.join(",", player.getDiscoveredCandyIds());
        String completedGoalsStr = String.join(",", player.getCompletedGoalIds());

        String sql = "INSERT INTO candy_ai_lab (player_name, score, ai_level, ai_learning_pct, learned_rules, discovered_candies, completed_goals, date_time, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getScore());
            pstmt.setInt(3, ai.getAiLevel());
            pstmt.setFloat(4, ai.getLearningPercentage());
            pstmt.setString(5, learnedRulesStr);
            pstmt.setString(6, discoveredCandiesStr);
            pstmt.setString(7, completedGoalsStr);
            pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            if (userId > 0) {
                pstmt.setInt(9, userId);
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error saving AI Lab progress: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean loadUserProgress(int userId, Player player, AIAssistant ai) {
        return loadLatestProgress(player, ai, userId);
    }

    public boolean loadLatestProgress(Player player, AIAssistant ai) {
        return loadLatestProgress(player, ai, getCurrentUserId());
    }

    /**
     * Loads the latest saved progress for a specific user, including AI memory,
     * custom candy rules, achievements, goals, and discovery data.
     */
    public boolean loadLatestProgress(Player player, AIAssistant ai, int userId) {
        loadCustomCandies(player, ai, userId);

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        String sql;
        if (userId > 0) {
            sql = "SELECT player_name, score, ai_level, ai_learning_pct, learned_rules, discovered_candies, completed_goals " +
                    "FROM candy_ai_lab WHERE user_id = ? ORDER BY date_time DESC LIMIT 1";
        } else {
            sql = "SELECT player_name, score, ai_level, ai_learning_pct, learned_rules, discovered_candies, completed_goals " +
                    "FROM candy_ai_lab ORDER BY date_time DESC LIMIT 1";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userId > 0) {
                pstmt.setInt(1, userId);
            }
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                player.setScore(rs.getInt("score"));
                player.setAiLevel(rs.getInt("ai_level"));

                String discoveredStr = rs.getString("discovered_candies");
                if (discoveredStr != null && !discoveredStr.trim().isEmpty()) {
                    player.getDiscoveredCandyIds().addAll(Arrays.asList(discoveredStr.split(",")));
                }

                String goalsStr = rs.getString("completed_goals");
                if (goalsStr != null && !goalsStr.trim().isEmpty()) {
                    player.getCompletedGoalIds().addAll(Arrays.asList(goalsStr.split(",")));
                }

                String rulesStr = rs.getString("learned_rules");
                if (rulesStr != null && !rulesStr.trim().isEmpty()) {
                    String[] rulesArray = rulesStr.split(";");
                    for (String rStr : rulesArray) {
                        String[] parts = rStr.split("\\|");
                        if (parts.length >= 5) {
                            String ruleId = parts[0];
                            List<String> inputs = Arrays.asList(parts[1].split(","));
                            String resultId = parts[2];
                            String desc = parts[3];
                            boolean isCustom = Boolean.parseBoolean(parts[4]);

                            CandyRule rule = new CandyRule(ruleId, inputs, resultId, desc, isCustom);
                            ai.learnRule(rule);
                        }
                    }
                }

                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error loading AI Lab progress: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }

        return false;
    }

    private Color parseColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red": return new Color(220, 20, 60);
            case "orange": return new Color(255, 140, 0);
            case "yellow": return new Color(255, 215, 0);
            case "green": return new Color(46, 139, 87);
            case "cyan": return new Color(0, 206, 209);
            case "blue": return new Color(30, 144, 255);
            case "purple": return new Color(138, 43, 226);
            case "pink": return new Color(255, 105, 180);
            case "gold": return new Color(255, 215, 0);
            default: return new Color(128, 0, 128);
        }
    }

    private int getCurrentUserId() {
        UserSession session = UserSession.getInstance();
        return session.isLoggedIn() ? session.getUserId() : -1;
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
