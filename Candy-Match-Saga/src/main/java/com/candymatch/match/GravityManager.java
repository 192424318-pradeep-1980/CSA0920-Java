package com.candymatch.match;

import com.candymatch.game.GameBoard;

/**
 * Handles tile drop physics/gravity operations.
 */
public class GravityManager {
    public static void applyGravity(GameBoard board) {
        board.applyGravity();
    }
}
