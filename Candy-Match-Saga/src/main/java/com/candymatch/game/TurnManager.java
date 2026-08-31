package com.candymatch.game;

/**
 * Manages game turn states, ensures strict turn-based player vs AI execution,
 * and handles input unlocking.
 */
public class TurnManager {

    public enum TurnState {
        PLAYER_TURN,
        AI_TURN,
        PROCESSING_MOVE,
        PAUSED,
        GAME_OVER,
        LEVEL_COMPLETE
    }

    private TurnState currentTurn = TurnState.PLAYER_TURN;
    private boolean playerInputEnabled = true;
    private boolean aiOpponentMode = false;
    private Runnable turnChangeCallback;

    public TurnManager() {
        this.currentTurn = TurnState.PLAYER_TURN;
        this.playerInputEnabled = true;
    }

    public void setAIOpponentMode(boolean enabled) {
        this.aiOpponentMode = enabled;
        if (!enabled) {
            startPlayerTurn();
        }
    }

    public boolean isAIOpponentMode() {
        return aiOpponentMode;
    }

    public void setTurnChangeCallback(Runnable callback) {
        this.turnChangeCallback = callback;
    }

    public TurnState getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(TurnState state) {
        this.currentTurn = state;
        updateTurnUI();
    }

    public boolean isPlayerTurn() {
        return currentTurn == TurnState.PLAYER_TURN;
    }

    public boolean isAITurn() {
        return currentTurn == TurnState.AI_TURN;
    }

    public boolean isProcessingMove() {
        return currentTurn == TurnState.PROCESSING_MOVE;
    }

    public boolean isPlayerInputEnabled() {
        return playerInputEnabled && currentTurn == TurnState.PLAYER_TURN;
    }

    public void setPlayerInputEnabled(boolean enabled) {
        this.playerInputEnabled = enabled;
    }

    public void startPlayerTurn() {
        this.currentTurn = TurnState.PLAYER_TURN;
        this.playerInputEnabled = true;
        updateTurnUI();
    }

    public void startAITurn() {
        if (!aiOpponentMode) {
            startPlayerTurn();
            return;
        }
        this.currentTurn = TurnState.AI_TURN;
        this.playerInputEnabled = false;
        updateTurnUI();
    }

    public void completePlayerTurn() {
        this.playerInputEnabled = false;
        if (aiOpponentMode) {
            startAITurn();
        } else {
            startPlayerTurn();
        }
    }

    public void completeAITurn() {
        // After AI completes turn, player turn MUST be started and input MUST be enabled
        startPlayerTurn();
    }

    public void updateTurnUI() {
        if (turnChangeCallback != null) {
            turnChangeCallback.run();
        }
    }
}
