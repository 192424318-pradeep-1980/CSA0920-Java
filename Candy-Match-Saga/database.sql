-- =======================================================
-- Candy Match Saga: Candy AI Lab Database Schema (MySQL)
-- =======================================================

CREATE DATABASE IF NOT EXISTS candymatch;

USE candymatch;

-- User Authentication Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Player Lab Progress Table (per-user via user_id)
CREATE TABLE IF NOT EXISTS candy_ai_lab (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT DEFAULT NULL,
    player_name VARCHAR(100) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    ai_level INT NOT NULL DEFAULT 1,
    ai_learning_pct FLOAT NOT NULL DEFAULT 0.0,
    learned_rules TEXT,
    discovered_candies TEXT,
    completed_goals TEXT,
    date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Custom Candies Registry Table (per-user via user_id)
CREATE TABLE IF NOT EXISTS custom_candies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT DEFAULT NULL,
    candy_name VARCHAR(100) NOT NULL,
    color VARCHAR(30) NOT NULL,
    shape VARCHAR(30) NOT NULL,
    icon VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Sample Initial Lab Scientist Progress Record
INSERT INTO candy_ai_lab (player_name, score, ai_level, learned_rules, discovered_candies, completed_goals) VALUES
('Scientist Alex', 850, 3, 'R1;R2;R3;R4;R5;R6', 'RED,BLUE,GREEN,YELLOW,PURPLE,RUBY,CRYSTAL', 'M1');

-- Sample Initial Custom Candy Record
INSERT IGNORE INTO custom_candies (candy_name, color, shape, icon) VALUES
('Thunder Candy', 'Orange', 'Star', '⚡');
