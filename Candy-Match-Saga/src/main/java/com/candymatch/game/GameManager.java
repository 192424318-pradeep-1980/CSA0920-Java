package com.candymatch.game;

import com.candymatch.ai.AIOpponent;
import com.candymatch.ai.AITurnController;
import com.candymatch.ai.HintGenerator;
import com.candymatch.analytics.AnalyticsManager;
import com.candymatch.candy.Candy;
import com.candymatch.exceptions.InvalidMoveException;
import com.candymatch.match.ComboManager;
import com.candymatch.match.MatchProcessor;

import javax.swing.Timer;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Main game coordinator overseeing levels 1-20, turn alternation, collections, multithreaded timer, and JDBC persistence.
 */
public class GameManager {

    public static class LevelConfig {
        public final int levelNumber;
        public final String difficultyTier;
        public final int targetScore;
        public final int moveLimit;
        public final int timeLimitSeconds;
        public final int requiredCombos;

        public LevelConfig(int levelNumber, String difficultyTier, int targetScore, int moveLimit, int timeLimitSeconds, int requiredCombos) {
            this.levelNumber = levelNumber;
            this.difficultyTier = difficultyTier;
            this.targetScore = targetScore;
            this.moveLimit = moveLimit;
            this.timeLimitSeconds = timeLimitSeconds;
            this.requiredCombos = requiredCombos;
        }
    }

    private String playerID = "Player1";
    private String gameMode = "PLAYER_ONLY"; // PLAYER_ONLY vs PLAYER_VS_AI
    private int currentLevelNumber = 1;
    private LevelConfig currentLevelConfig;

    private int currentScore = 0;
    private int movesRemaining = 20;
    private int movesUsed = 0;
    private int timeRemainingSeconds = 120;

    private final GameBoard board;
    private final TurnManager turnManager;
    private final GameStateManager stateManager;
    private final ComboManager comboManager;
    private final AnalyticsManager analyticsManager;
    private final AIOpponent aiOpponent;
    private final AITurnController aiTurnController;
    private final HintGenerator hintGenerator;

    // Multithreaded Independent Game Timer
    private GameTimerManager gameTimerManager;

    // Collection Framework Usage
    private final List<Move> moveHistory = new ArrayList<>();
    private final Stack<Move> undoStack = new Stack<>();
    private final Map<Integer, Integer> levelScores = new HashMap<>();
    private final Map<Integer, LevelConfig> levels = new HashMap<>();

    private Runnable uiRefreshCallback;
    private Runnable gameEndCallback;

    public GameManager() {
        this.board = new GameBoard();
        this.turnManager = new TurnManager();
        this.stateManager = new GameStateManager();
        this.comboManager = new ComboManager();
        this.analyticsManager = new AnalyticsManager();
        this.aiOpponent = new AIOpponent();
        this.aiTurnController = new AITurnController(turnManager, aiOpponent);
        this.hintGenerator = new HintGenerator();

        init20Levels();
        loadLevelConfig(1);
    }

    private void init20Levels() {
        // Levels 1-5: Beginner
        for (int i = 1; i <= 5; i++) {
            levels.put(i, new LevelConfig(i, "Beginner", 1000 + (i * 300), 25 - i, 180, 2));
        }
        // Levels 6-10: Intermediate
        for (int i = 6; i <= 10; i++) {
            levels.put(i, new LevelConfig(i, "Intermediate", 2800 + ((i - 5) * 500), 22 - (i - 5), 150, 3));
        }
        // Levels 11-15: Advanced
        for (int i = 11; i <= 15; i++) {
            levels.put(i, new LevelConfig(i, "Advanced", 5500 + ((i - 10) * 800), 20 - (i - 10), 120, 4));
        }
        // Levels 16-20: Expert
        for (int i = 16; i <= 20; i++) {
            levels.put(i, new LevelConfig(i, "Expert", 10000 + ((i - 15) * 1200), 18 - (i - 15), 90, 5));
        }
    }

    public void setCallbacks(Runnable refreshUI, Runnable gameEnd) {
        this.uiRefreshCallback = refreshUI;
        this.gameEndCallback = gameEnd;
    }

    public void startGame() {
        startGame("Player1", "PLAYER_ONLY", 1);
    }

    public void startGame(String playerID, String mode, int startLevel) {
        this.playerID = playerID;
        this.gameMode = mode;
        this.turnManager.setAIOpponentMode("PLAYER_VS_AI".equalsIgnoreCase(mode));
        startLevel(startLevel);
    }

    public void startLevel() {
        startLevel(currentLevelNumber);
    }

    public void startLevel(int levelNumber) {
        if (levelNumber < 1) levelNumber = 1;
        if (levelNumber > 20) levelNumber = 20;
        this.currentLevelNumber = levelNumber;

        loadLevelConfig(levelNumber);

        this.currentScore = 0;
        this.movesRemaining = currentLevelConfig.moveLimit;
        this.movesUsed = 0;
        this.timeRemainingSeconds = currentLevelConfig.timeLimitSeconds;
        this.comboManager.resetCombo();
        this.aiOpponent.reset();
        this.hintGenerator.clearHint(board);

        // Reset collection histories for active level
        this.moveHistory.clear();
        this.undoStack.clear();

        board.generateWithoutInitialMatches();
        turnManager.startPlayerTurn();
        stateManager.setState(GameStateManager.State.PLAYER_TURN);

        analyticsManager.startLevelSession(playerID, gameMode, levelNumber);

        // Multithreading Implementation: Start independent countdown timer thread
        if (gameTimerManager != null) {
            gameTimerManager.stopTimer();
        }
        gameTimerManager = new GameTimerManager(currentLevelConfig.timeLimitSeconds, new GameTimerManager.TimerTickListener() {
            @Override
            public void onTimerTick(int remainingSeconds) {
                timeRemainingSeconds = remainingSeconds;
                if (uiRefreshCallback != null) {
                    uiRefreshCallback.run();
                }
            }

            @Override
            public void onTimerExpired() {
                timeRemainingSeconds = 0;
                checkGameStatus();
            }
        });
        gameTimerManager.startTimer(currentLevelConfig.timeLimitSeconds);

        if (uiRefreshCallback != null) {
            uiRefreshCallback.run();
        }
    }

    private void loadLevelConfig(int levelNumber) {
        this.currentLevelConfig = levels.getOrDefault(levelNumber, levels.get(1));
    }

    /**
     * Player initiates a swap between two adjacent cells.
     * Uses InvalidMoveException for exception handling.
     */
    public boolean attemptPlayerSwap(int r1, int c1, int r2, int c2) throws InvalidMoveException {
        if (!turnManager.isPlayerTurn() || !turnManager.isPlayerInputEnabled()) {
            throw new InvalidMoveException("Not player turn or player input is locked.");
        }

        if (!GameBoard.isValidPosition(r1, c1) || !GameBoard.isValidPosition(r2, c2)) {
            throw new InvalidMoveException("Target swap position is out of board boundaries.");
        }

        if (!GameBoard.areAdjacent(r1, c1, r2, c2)) {
            throw new InvalidMoveException("Candies must be horizontally or vertically adjacent to swap.");
        }

        // Lock player input during move processing
        turnManager.setPlayerInputEnabled(false);
        stateManager.setState(GameStateManager.State.PROCESSING_MOVE);

        // Perform swap
        board.swap(r1, c1, r2, c2);

        Move swapMove = new Move(r1, c1, r2, c2);
        
        // Collection Framework Usage: Store move in ArrayList and push onto Stack
        moveHistory.add(swapMove);
        undoStack.push(swapMove);

        processPlayerMove(swapMove);
        return true;
    }

    public void processPlayerMove() {
        // Default overload
    }

    public void processPlayerMove(Move move) {
        processMoveAndCascades(move, true, () -> {
            if (checkGameStatus()) {
                finishPlayerTurn();
            }
        });
    }

    public void processAIMove(Move move, Runnable onComplete) {
        processMoveAndCascades(move, false, () -> {
            if (onComplete != null) onComplete.run();
        });
    }

    public void processMoveAndCascades(Move move, boolean isPlayer, Runnable onCascadeFinish) {
        comboManager.resetCombo();

        MatchProcessor.ProcessResult result = MatchProcessor.processSingleCascade(board, comboManager, new Point(move.getCol2(), move.getRow2()));

        if (result.clearedPoints.isEmpty()) {
            // Invalid swap! Revert swap
            board.swap(move.getRow1(), move.getCol1(), move.getRow2(), move.getCol2());
            if (isPlayer) {
                // Remove invalid move from undo stack
                if (!undoStack.isEmpty()) undoStack.pop();
                analyticsManager.getPlayerAnalytics().recordInvalidSwap();
                turnManager.startPlayerTurn();
                stateManager.setState(GameStateManager.State.PLAYER_TURN);
            }
            if (uiRefreshCallback != null) uiRefreshCallback.run();
            return;
        }

        // Valid Swap!
        if (isPlayer) {
            movesRemaining--;
            movesUsed++;
            analyticsManager.getPlayerAnalytics().recordValidSwap(true);
        } else {
            analyticsManager.getPlayerAnalytics().recordValidSwap(false);
        }

        currentScore += result.scoreEarned;
        analyticsManager.getPlayerAnalytics().recordMatches(result.match3Count, result.match4Count, result.match5Count, comboManager.getCurrentCombo());
        analyticsManager.getPlayerAnalytics().recordSpecialCandy(result.specialCandiesCreated, result.specialCandiesActivated);

        // Collection Usage: Track level high score in HashMap
        int previousHighScore = levelScores.getOrDefault(currentLevelNumber, 0);
        if (currentScore > previousHighScore) {
            levelScores.put(currentLevelNumber, currentScore);
        }

        if (uiRefreshCallback != null) uiRefreshCallback.run();

        scheduleCascadeLoop(onCascadeFinish);
    }

    private void scheduleCascadeLoop(Runnable onFinish) {
        Timer cascadeTimer = new Timer(250, e -> {
            ((Timer) e.getSource()).stop();

            MatchProcessor.ProcessResult cascadeResult = MatchProcessor.processSingleCascade(board, comboManager, null);

            if (!cascadeResult.clearedPoints.isEmpty()) {
                currentScore += cascadeResult.scoreEarned;
                analyticsManager.getPlayerAnalytics().recordMatches(cascadeResult.match3Count, cascadeResult.match4Count, cascadeResult.match5Count, comboManager.getCurrentCombo());
                analyticsManager.getPlayerAnalytics().recordSpecialCandy(cascadeResult.specialCandiesCreated, cascadeResult.specialCandiesActivated);

                if (uiRefreshCallback != null) uiRefreshCallback.run();

                scheduleCascadeLoop(onFinish);
            } else {
                if (uiRefreshCallback != null) uiRefreshCallback.run();
                if (onFinish != null) onFinish.run();
            }
        });
        cascadeTimer.setRepeats(false);
        cascadeTimer.start();
    }

    public void finishPlayerTurn() {
        hintGenerator.clearHint(board);
        turnManager.completePlayerTurn();

        if (turnManager.isAITurn()) {
            stateManager.setState(GameStateManager.State.AI_TURN);
            if (uiRefreshCallback != null) uiRefreshCallback.run();
            aiTurnController.executeAITurn(board, this, () -> {
                if (uiRefreshCallback != null) uiRefreshCallback.run();
            });
        } else {
            stateManager.setState(GameStateManager.State.PLAYER_TURN);
            if (uiRefreshCallback != null) uiRefreshCallback.run();
        }
    }

    public void finishAITurn() {
        turnManager.completeAITurn();
        stateManager.setState(GameStateManager.State.PLAYER_TURN);
        if (uiRefreshCallback != null) uiRefreshCallback.run();
    }

    public boolean checkGameStatus() {
        if (checkWinCondition()) {
            stateManager.setState(GameStateManager.State.LEVEL_COMPLETE);
            stopTimer();
            analyticsManager.completeSession(currentScore, true);
            if (gameEndCallback != null) gameEndCallback.run();
            return false;
        }

        if (checkLossCondition()) {
            stateManager.setState(GameStateManager.State.GAME_OVER);
            stopTimer();
            analyticsManager.completeSession(currentScore, false);
            if (gameEndCallback != null) gameEndCallback.run();
            return false;
        }

        return true;
    }

    public void stopTimer() {
        if (gameTimerManager != null) {
            gameTimerManager.stopTimer();
        }
    }

    public boolean checkWinCondition() {
        return currentScore >= currentLevelConfig.targetScore;
    }

    public boolean checkLossCondition() {
        return (movesRemaining <= 0 || timeRemainingSeconds <= 0) && currentScore < currentLevelConfig.targetScore;
    }

    public HintGenerator.HintInfo requestHint() {
        if (!turnManager.isPlayerTurn()) {
            return new HintGenerator.HintInfo(null, "Hint unavailable during AI turn!");
        }
        analyticsManager.getPlayerAnalytics().recordHintUsed();
        return hintGenerator.generateHint(board, turnManager);
    }

    // Getters & Collection Accessors
    public GameBoard getBoard() { return board; }
    public TurnManager getTurnManager() { return turnManager; }
    public GameStateManager getGameStateManager() { return stateManager; }
    public AnalyticsManager getAnalyticsManager() { return analyticsManager; }
    public HintGenerator getHintGenerator() { return hintGenerator; }
    public AIOpponent getAIOpponent() { return aiOpponent; }
    public LevelConfig getCurrentLevelConfig() { return currentLevelConfig; }
    public GameTimerManager getGameTimerManager() { return gameTimerManager; }

    public List<Move> getMoveHistory() { return moveHistory; }
    public Stack<Move> getUndoStack() { return undoStack; }
    public Map<Integer, Integer> getLevelScores() { return levelScores; }

    public String getPlayerID() { return playerID; }
    public String getGameMode() { return gameMode; }
    public int getCurrentLevelNumber() { return currentLevelNumber; }
    public int getCurrentScore() { return currentScore; }
    public int getMovesRemaining() { return movesRemaining; }
    public int getMovesUsed() { return movesUsed; }
    public int getTimeRemainingSeconds() { return timeRemainingSeconds; }
}
