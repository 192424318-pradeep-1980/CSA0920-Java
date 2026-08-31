package com.candymatch.ai;

import com.candymatch.game.GameBoard;
import com.candymatch.game.GameManager;
import com.candymatch.game.Move;
import com.candymatch.game.TurnManager;

import javax.swing.Timer;
import java.util.List;

/**
 * Controls turn-based execution for the AI Opponent.
 * Ensures AI executes strictly ONE valid move per turn, waits for cascades,
 * updates metrics, and safely returns turn control to the Player.
 */
public class AITurnController {
    private final TurnManager turnManager;
    private final AIOpponent aiOpponent;

    public AITurnController(TurnManager turnManager, AIOpponent aiOpponent) {
        this.turnManager = turnManager;
        this.aiOpponent = aiOpponent;
    }

    public void executeAITurn(GameBoard board, GameManager gameManager, Runnable onAIMoveComplete) {
        if (!turnManager.isAITurn()) {
            return;
        }

        // Delay execution slightly (e.g. 500ms) for smooth Swing visual playback
        Timer delayTimer = new Timer(500, e -> {
            ((Timer) e.getSource()).stop();

            if (!turnManager.isAITurn()) {
                return;
            }

            List<Move> validMoves = AIEngine.findAllValidMoves(board);

            if (validMoves.isEmpty()) {
                board.shuffleBoard();
                // After reshuffle, retry finding valid move
                validMoves = AIEngine.findAllValidMoves(board);
            }

            if (!validMoves.isEmpty()) {
                Move bestMove = AIEngine.selectBestMove(validMoves);
                // Execute ONE move
                executeOneAIMove(board, bestMove, gameManager, onAIMoveComplete);
            } else {
                // Return turn to player if no move possible
                finishAITurn(gameManager, onAIMoveComplete);
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    private void executeOneAIMove(GameBoard board, Move move, GameManager gameManager, Runnable onAIMoveComplete) {
        // Swap candies on board
        board.swap(move.getRow1(), move.getCol1(), move.getRow2(), move.getCol2());

        // Trigger GameManager processing for AI move
        gameManager.processMoveAndCascades(move, false, () -> {
            // Callback when all AI cascades finish
            aiOpponent.recordMove(move.getPredictedScore(), 1, move.getSpecialTypeCreated() != null ? 1 : 0);
            finishAITurn(gameManager, onAIMoveComplete);
        });
    }

    public void finishAITurn(GameManager gameManager, Runnable onAIMoveComplete) {
        // Check game over/level complete status
        boolean continueGame = gameManager.checkGameStatus();

        if (continueGame) {
            // Mandated: Return control to PLAYER_TURN and re-enable player input
            turnManager.completeAITurn();
        }

        if (onAIMoveComplete != null) {
            onAIMoveComplete.run();
        }
    }
}
