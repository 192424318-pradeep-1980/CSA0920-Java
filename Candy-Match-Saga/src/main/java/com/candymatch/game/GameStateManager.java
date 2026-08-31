package com.candymatch.game;

/**
 * Manages global game application state.
 */
public class GameStateManager {

    public enum State {
        MENU,
        LEVEL_SELECT,
        PLAYER_TURN,
        AI_TURN,
        PROCESSING_MOVE,
        PAUSED,
        GAME_OVER,
        LEVEL_COMPLETE,
        RESEARCH_BOOK,
        CUSTOM_CANDY_EDITOR
    }

    private State currentState = State.MENU;

    public GameStateManager() {
        this.currentState = State.MENU;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setState(State state) {
        this.currentState = state;
    }

    public boolean isGameplayActive() {
        return currentState == State.PLAYER_TURN || currentState == State.AI_TURN || currentState == State.PROCESSING_MOVE;
    }

    public boolean canPlayerInteract() {
        return currentState == State.PLAYER_TURN;
    }
}
