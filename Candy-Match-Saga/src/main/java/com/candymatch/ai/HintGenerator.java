package com.candymatch.ai;

import com.candymatch.candy.Candy;
import com.candymatch.game.GameBoard;
import com.candymatch.game.Move;
import com.candymatch.game.TurnManager;

import java.util.List;

/**
 * Generates and formats non-intrusive hint advice for the player.
 * Highlighting valid moves on the current board without executing swaps or changing turn states.
 */
public class HintGenerator {

    public static class HintInfo {
        private final Move move;
        private final String message;

        public HintInfo(Move move, String message) {
            this.move = move;
            this.message = message;
        }

        public Move getMove() {
            return move;
        }

        public String getMessage() {
            return message;
        }
    }

    private Move activeHintMove = null;
    private String activeHintMessage = "";

    public List<Move> findAllValidMoves(GameBoard board) {
        return AIEngine.findAllValidMoves(board);
    }

    public Move findBestMove(GameBoard board) {
        List<Move> moves = findAllValidMoves(board);
        return AIEngine.selectBestMove(moves);
    }

    public HintInfo showHint(GameBoard board, TurnManager turnManager) {
        return generateHint(board, turnManager);
    }

    public HintInfo generateHint(GameBoard board, TurnManager turnManager) {
        if (turnManager.isAITurn()) {
            return new HintInfo(null, "Please wait. AI is playing...");
        }

        List<Move> validMoves = findAllValidMoves(board);

        if (validMoves.isEmpty()) {
            board.shuffleBoard();
            validMoves = findAllValidMoves(board);
        }

        if (validMoves.isEmpty()) {
            return new HintInfo(null, "No valid move found. Board reshuffled!");
        }

        Move bestMove = AIEngine.selectBestMove(validMoves);

        // Apply visual highlights to cells on board
        board.clearAllHighlights();
        Candy c1 = board.getCandy(bestMove.getRow1(), bestMove.getCol1());
        Candy c2 = board.getCandy(bestMove.getRow2(), bestMove.getCol2());

        if (c1 != null) c1.setHintHighlighted(true);
        if (c2 != null) c2.setHintHighlighted(true);

        this.activeHintMove = bestMove;
        this.activeHintMessage = String.format("Try this move! Swap (%d,%d) with (%d,%d)",
                bestMove.getRow1() + 1, bestMove.getCol1() + 1,
                bestMove.getRow2() + 1, bestMove.getCol2() + 1);

        return new HintInfo(bestMove, activeHintMessage);
    }

    public void clearHint(GameBoard board) {
        if (board != null) {
            board.clearAllHighlights();
        }
        this.activeHintMove = null;
        this.activeHintMessage = "";
    }

    public Move getActiveHintMove() {
        return activeHintMove;
    }

    public String getActiveHintMessage() {
        return activeHintMessage;
    }
}
