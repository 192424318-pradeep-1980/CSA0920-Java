package com.candymatch.game;

import com.candymatch.Main;

/**
 * Game class entry wrapper for Candy Match Saga.
 */
public class Game {
    private final GameManager gameManager;

    public Game() {
        this.gameManager = new GameManager();
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
