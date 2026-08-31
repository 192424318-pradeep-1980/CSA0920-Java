-- =======================================================
-- Candy Match Saga Database Schema Script (MySQL)
-- =======================================================

CREATE DATABASE IF NOT EXISTS candymatch;

USE candymatch;

CREATE TABLE IF NOT EXISTS game_scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(100) NOT NULL,
    score INT NOT NULL,
    moves_used INT NOT NULL,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample Initial Top Scores (Optional)
INSERT INTO game_scores (player_name, score, moves_used) VALUES 
('CandyKing', 1250, 30),
('MatchMaster', 980, 28),
('SweetPro', 720, 25);
