package com.candymatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection.java
 * Handles JDBC connection establishment for MySQL database `candymatch`.
 * Includes automatic SQLite fallback for zero-configuration local execution if MySQL is unavailable.
 */
public class DBConnection {

    private static String mysqlHost = "localhost";
    private static int mysqlPort = 3306;
    private static String mysqlDb = "candymatch";
    private static String mysqlUser = "root";
    private static String mysqlPass = "";

    private static String currentDbType = "Disconnected";

    /**
     * Gets active JDBC database connection.
     * Attempts MySQL connection first; falls back seamlessly to SQLite file database if MySQL fails.
     */
    public static Connection getConnection() {
        Connection conn = null;

        // 1. Try MySQL Connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Server connection to create database if not existing
            String serverUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection tempConn = DriverManager.getConnection(serverUrl, mysqlUser, mysqlPass);
                 Statement stmt = tempConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + mysqlDb);
            }

            // Target DB connection
            String dbUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDb + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            conn = DriverManager.getConnection(dbUrl, mysqlUser, mysqlPass);
            currentDbType = "MySQL (" + mysqlDb + ")";
            return conn;
        } catch (Exception e) {
            // MySQL unavailable
        }

        // 2. Fallback to SQLite Embedded Database
        try {
            Class.forName("org.sqlite.JDBC");
            String sqliteUrl = "jdbc:sqlite:candymatch.db";
            conn = DriverManager.getConnection(sqliteUrl);
            currentDbType = "SQLite (Local Fallback)";
            return conn;
        } catch (Exception e) {
            System.err.println("JDBC Connection Failed: " + e.getMessage());
            currentDbType = "Offline Mode";
        }

        return null;
    }

    public static String getDatabaseType() {
        return currentDbType;
    }
}
