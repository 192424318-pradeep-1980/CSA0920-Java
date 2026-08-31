-- =========================================================
-- Candy Match Saga Database Schema
-- MySQL 8.0+ Compatible SQL Script
-- =========================================================

CREATE DATABASE IF NOT EXISTS candy_match_saga;
USE candy_match_saga;

-- 1. Players Table
CREATE TABLE IF NOT EXISTS players (
    player_id VARCHAR(50) PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Game Sessions Table
CREATE TABLE IF NOT EXISTS game_sessions (
    session_id VARCHAR(50) PRIMARY KEY,
    player_id VARCHAR(50) NOT NULL,
    level INT NOT NULL,
    score INT NOT NULL,
    moves INT NOT NULL,
    time_seconds INT NOT NULL,
    matches INT NOT NULL,
    combo_count INT NOT NULL,
    special_candies_created INT NOT NULL,
    hints_used INT NOT NULL,
    player_moves INT NOT NULL,
    ai_moves INT NOT NULL,
    win_loss VARCHAR(20) NOT NULL,
    completion_status VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_game_sessions_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

-- 3. Player Statistics Table
CREATE TABLE IF NOT EXISTS player_statistics (
    player_id VARCHAR(50) PRIMARY KEY,
    total_games INT DEFAULT 0,
    total_wins INT DEFAULT 0,
    total_losses INT DEFAULT 0,
    highest_score INT DEFAULT 0,
    average_score DOUBLE DEFAULT 0.0,
    CONSTRAINT fk_player_stats_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);
