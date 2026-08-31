package com.candymatch.ailab;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.regex.Pattern;

/**
 * AuthenticationManager.java
 * Handles user registration, login, password reset, and JDBC persistence
 * for the Candy AI Lab users table. Uses SHA-256 password hashing.
 */
public class AuthenticationManager {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Path REMEMBER_FILE = Paths.get("candylab_session.properties");

    public AuthenticationManager() {
        createUsersTableIfNotExists();
    }

    /**
     * Creates the users table in MySQL or SQLite depending on active JDBC connection.
     */
    public void createUsersTableIfNotExists() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String dbType = DBConnection.getDatabaseType();
        String sql;

        if (dbType.contains("MySQL")) {
            sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_name VARCHAR(100) NOT NULL, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "last_login TIMESTAMP NULL" +
                    ")";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "player_name TEXT NOT NULL, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "email TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "last_login TIMESTAMP NULL" +
                    ")";
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            ensureUserScopedColumns(conn, dbType);
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Adds user_id columns to progress tables for per-player data isolation.
     */
    private void ensureUserScopedColumns(Connection conn, String dbType) {
        try (Statement stmt = conn.createStatement()) {
            if (dbType.contains("MySQL")) {
                try { stmt.executeUpdate("ALTER TABLE candy_ai_lab ADD COLUMN user_id INT DEFAULT NULL"); } catch (SQLException ignored) {}
                try { stmt.executeUpdate("ALTER TABLE custom_candies ADD COLUMN user_id INT DEFAULT NULL"); } catch (SQLException ignored) {}
            } else {
                try { stmt.executeUpdate("ALTER TABLE custom_candies ADD COLUMN user_id INTEGER DEFAULT NULL"); } catch (SQLException ignored) {}
                try { stmt.executeUpdate("ALTER TABLE candy_ai_lab ADD COLUMN user_id INTEGER DEFAULT NULL"); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            System.err.println("Error adding user_id columns: " + e.getMessage());
        }
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new SQLException("Database connection unavailable.");

        String sql = "SELECT user_id FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Registers a new player account after validation.
     */
    public String register(String playerName, String username, String email, String password, String confirmPassword)
            throws SQLException {
        if (playerName == null || playerName.trim().isEmpty()) {
            return "Player name is required.";
        }
        if (username == null || username.trim().isEmpty()) {
            return "Username is required.";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address.";
        }
        if (!isValidPassword(password)) {
            return "Password must contain at least 8 characters.";
        }
        if (!password.equals(confirmPassword)) {
            return "Password and Confirm Password do not match.";
        }
        if (isUsernameTaken(username)) {
            return "Username is already taken. Please choose another.";
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new SQLException("Database connection unavailable.");

        String sql = "INSERT INTO users (player_name, username, email, password, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName.trim());
            pstmt.setString(2, username.trim());
            pstmt.setString(3, email.trim().toLowerCase());
            pstmt.setString(4, hashPassword(password));
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            return null;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Authenticates credentials and populates UserSession on success.
     */
    public String login(String username, String password, boolean rememberMe) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return "Please enter your username.";
        }
        if (password == null || password.isEmpty()) {
            return "Please enter your password.";
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new SQLException("Database connection unavailable.");

        String sql = "SELECT user_id, player_name, username, email, password, last_login FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                return "Invalid username or password.";
            }

            String storedHash = rs.getString("password");
            if (!storedHash.equals(hashPassword(password))) {
                return "Invalid username or password.";
            }

            int userId = rs.getInt("user_id");
            String playerName = rs.getString("player_name");
            String dbUsername = rs.getString("username");
            String email = rs.getString("email");
            Timestamp lastLogin = rs.getTimestamp("last_login");

            updateLastLogin(userId);

            UserSession.getInstance().login(
                    userId, playerName, dbUsername, email,
                    new Timestamp(System.currentTimeMillis()), rememberMe
            );
            return null;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Resets password when username and email match an existing account.
     */
    public String resetPassword(String username, String email, String newPassword, String confirmPassword)
            throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required.";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid registered email.";
        }
        if (!isValidPassword(newPassword)) {
            return "Password must contain at least 8 characters.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Password and Confirm Password do not match.";
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) throw new SQLException("Database connection unavailable.");

        String checkSql = "SELECT user_id FROM users WHERE LOWER(username) = LOWER(?) AND LOWER(email) = LOWER(?)";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, username.trim());
            check.setString(2, email.trim());
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                return "No account found with that username and email combination.";
            }

            int userId = rs.getInt("user_id");
            String updateSql = "UPDATE users SET password = ? WHERE user_id = ?";
            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setString(1, hashPassword(newPassword));
                update.setInt(2, userId);
                update.executeUpdate();
            }
            return null;
        } finally {
            closeQuietly(conn);
        }
    }

    public void updateLastLogin(int userId) throws SQLException {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } finally {
            closeQuietly(conn);
        }
    }

    public static String loadRememberedUsername() {
        if (!Files.exists(REMEMBER_FILE)) return null;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(REMEMBER_FILE)) {
            props.load(in);
            return props.getProperty("username");
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveRememberedUsername(String username) {
        Properties props = new Properties();
        props.setProperty("username", username);
        try (OutputStream out = Files.newOutputStream(REMEMBER_FILE)) {
            props.store(out, "Candy AI Lab Remember Me Session");
        } catch (IOException e) {
            System.err.println("Could not save remember-me preference: " + e.getMessage());
        }
    }

    public static void clearRememberedUsername() {
        try {
            Files.deleteIfExists(REMEMBER_FILE);
        } catch (IOException e) {
            System.err.println("Could not clear remember-me preference: " + e.getMessage());
        }
    }

    static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
