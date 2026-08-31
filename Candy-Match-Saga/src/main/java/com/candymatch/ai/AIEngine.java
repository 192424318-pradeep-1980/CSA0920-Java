package com.candymatch.ai;

import com.candymatch.game.GameBoard;
import com.candymatch.game.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic Rule-Based AI Engine for finding, evaluating, and selecting the optimal move.
 */
public class AIEngine {

    public static List<Move> findAllValidMoves(GameBoard board) {
        List<Move> validMoves = new ArrayList<>();

        for (int r = 0; r < GameBoard.ROWS; r++) {
            for (int c = 0; c < GameBoard.COLS; c++) {
                // Horizontal right swap
                if (c + 1 < GameBoard.COLS) {
                    Move m = MoveEvaluator.evaluateAndRankMove(board, r, c, r, c + 1);
                    if (m != null && !validMoves.contains(m)) {
                        validMoves.add(m);
                    }
                }
                // Vertical down swap
                if (r + 1 < GameBoard.ROWS) {
                    Move m = MoveEvaluator.evaluateAndRankMove(board, r, c, r + 1, c);
                    if (m != null && !validMoves.contains(m)) {
                        validMoves.add(m);
                    }
                }
            }
        }

        Collections.sort(validMoves);
        return validMoves;
    }

    public static Move evaluateMove(Move move, GameBoard board) {
        return MoveEvaluator.evaluateAndRankMove(board, move.getRow1(), move.getCol1(), move.getRow2(), move.getCol2());
    }

    public static Move selectBestMove(List<Move> validMoves) {
        if (validMoves == null || validMoves.isEmpty()) {
            return null;
        }
        // Moves are pre-sorted by rank, score, and match size
        return validMoves.get(0);
    }
}
