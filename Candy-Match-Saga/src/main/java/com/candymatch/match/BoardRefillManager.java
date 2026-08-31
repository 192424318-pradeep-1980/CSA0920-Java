package com.candymatch.match;

import com.candymatch.game.GameBoard;

/**
 * Handles refilling top empty spaces after cascade drops.
 */
public class BoardRefillManager {
    public static void refillBoard(GameBoard board) {
        board.refillEmptyCells();
    }
}
