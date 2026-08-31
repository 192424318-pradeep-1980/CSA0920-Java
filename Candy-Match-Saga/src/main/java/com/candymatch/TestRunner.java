package com.candymatch;

import com.candymatch.analytics.GameSession;
import com.candymatch.candy.CandyType;
import com.candymatch.custom.CustomCandyManager;
import com.candymatch.custom.CustomRuleManager;
import com.candymatch.exceptions.DatabaseException;
import com.candymatch.exceptions.InvalidCandyException;
import com.candymatch.exceptions.InvalidMoveException;
import com.candymatch.exceptions.InvalidRuleException;
import com.candymatch.game.GameManager;
import com.candymatch.game.GameTimerManager;
import com.candymatch.game.Move;
import com.candymatch.storage.DatabaseManager;

import java.util.List;

/**
 * Automated Verification Test Suite for Candy Match Saga features:
 * 1. Collection Framework Usage
 * 2. Multithreading Implementation
 * 3. Exception Handling Implementation
 * 4. JDBC / Database Integration
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("CANDY MATCH SAGA - AUTOMATED INTEGRATION TEST SUITE");
        System.out.println("==================================================");

        boolean allPassed = true;

        // 1. TEST MULTITHREADING
        System.out.println("\n[TEST 1] Multithreading Implementation (GameTimerManager)");
        try {
            final int[] tickCount = {0};
            GameTimerManager timer = new GameTimerManager(10, new GameTimerManager.TimerTickListener() {
                @Override
                public void onTimerTick(int remainingSeconds) {
                    tickCount[0]++;
                    System.out.println("   -> [TimerThread] Tick: " + remainingSeconds + "s remaining");
                }

                @Override
                public void onTimerExpired() {
                    System.out.println("   -> [TimerThread] Expired!");
                }
            });

            timer.startTimer(10);
            Thread.sleep(2500);
            timer.stopTimer();

            if (tickCount[0] >= 2) {
                System.out.println("✅ [PASS] Multithreaded timer executed independently on background thread.");
            } else {
                System.err.println("❌ [FAIL] Timer thread did not tick expected number of times.");
                allPassed = false;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] Multithreading error: " + e.getMessage());
            allPassed = false;
        }

        // 2. TEST COLLECTIONS
        System.out.println("\n[TEST 2] Collection Framework Usage");
        try {
            GameManager gm = new GameManager();
            gm.startLevel(1);

            // ArrayList Move History
            gm.getMoveHistory().add(new Move(0, 0, 0, 1));
            // Stack Undo History
            gm.getUndoStack().push(new Move(0, 0, 0, 1));
            // HashMap Level Scores
            gm.getLevelScores().put(1, 1500);

            // HashMap Analytics
            gm.getAnalyticsManager().getPlayerAnalytics().recordCandyMatched(CandyType.RED, 3);

            boolean listOk = gm.getMoveHistory().size() == 1;
            boolean stackOk = !gm.getUndoStack().isEmpty();
            boolean mapOk = gm.getLevelScores().get(1) == 1500;
            boolean candyMapOk = gm.getAnalyticsManager().getPlayerAnalytics().getCandyCounts().get(CandyType.RED) == 3;

            if (listOk && stackOk && mapOk && candyMapOk) {
                System.out.println("✅ [PASS] ArrayList (moves), Stack (undo), and HashMap (levelScores & candyCounts) verified.");
            } else {
                System.err.println("❌ [FAIL] Collection validation check failed.");
                allPassed = false;
            }
        } catch (Exception e) {
            System.err.println("❌ [FAIL] Collection error: " + e.getMessage());
            allPassed = false;
        }

        // 3. TEST EXCEPTION HANDLING
        System.out.println("\n[TEST 3] Exception Handling Implementation");
        int exceptionsCaught = 0;

        // InvalidMoveException
        try {
            GameManager gm = new GameManager();
            gm.startLevel(1);
            gm.attemptPlayerSwap(-1, 0, 0, 0); // Out of bounds
        } catch (InvalidMoveException e) {
            exceptionsCaught++;
            System.out.println("   -> Caught expected InvalidMoveException: " + e.getMessage());
        }

        // InvalidCandyException
        try {
            CustomCandyManager ccm = new CustomCandyManager();
            ccm.addCustomCandy(new CustomCandyManager.CustomCandyRecord("", "⭐", null, -50, "", ""));
        } catch (InvalidCandyException e) {
            exceptionsCaught++;
            System.out.println("   -> Caught expected InvalidCandyException: " + e.getMessage());
        }

        // InvalidRuleException
        try {
            CustomRuleManager crm = new CustomRuleManager();
            crm.addRule(new CustomRuleManager.CustomRuleRecord("", "", ""));
        } catch (InvalidRuleException e) {
            exceptionsCaught++;
            System.out.println("   -> Caught expected InvalidRuleException: " + e.getMessage());
        }

        if (exceptionsCaught == 3) {
            System.out.println("✅ [PASS] All custom exception scenarios handled gracefully.");
        } else {
            System.err.println("❌ [FAIL] Expected exceptions were not caught properly.");
            allPassed = false;
        }

        // 4. TEST JDBC / DATABASE INTEGRATION
        System.out.println("\n[TEST 4] JDBC / Database Integration");
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            System.out.println("   -> Active Database Engine: " + dbManager.getCurrentDbType());

            // Insert Player Record
            String testPlayerId = "TEST_PLAYER_001";
            dbManager.savePlayer(testPlayerId, "TestPlayerOne");
            System.out.println("   -> Inserted Player Record: " + testPlayerId);

            // Insert Game Session Record
            GameSession testSession = new GameSession(testPlayerId, "PLAYER_ONLY", 1);
            testSession.setScore(3200);
            testSession.setPlayerMoves(15);
            testSession.setAIMoves(0);
            testSession.setValidSwaps(15);
            testSession.setInvalidSwaps(1);
            testSession.setTotalMatches(12);
            testSession.setMatch3Count(10);
            testSession.setMatch4Count(2);
            testSession.setTimeSeconds(45);
            testSession.setWinLoss("WIN");
            testSession.setCompletionStatus("COMPLETED");

            dbManager.saveGameSession(testSession);
            System.out.println("   -> Inserted Game Session Record: " + testSession.getGameSessionID());

            // Retrieve Record
            List<GameSession> retrieved = dbManager.getGameSessions(testPlayerId);
            if (!retrieved.isEmpty()) {
                GameSession fetched = retrieved.get(0);
                System.out.println("   -> Retrieved Record from DB! SessionID: " + fetched.getGameSessionID() +
                        ", PlayerID: " + fetched.getPlayerID() +
                        ", Score: " + fetched.getScore() +
                        ", Status: " + fetched.getWinLoss());
                System.out.println("✅ [PASS] JDBC Database connection, PreparedStatement insertion, and query retrieval verified.");
            } else {
                System.err.println("❌ [FAIL] Retrieved session list is empty.");
                allPassed = false;
            }
        } catch (DatabaseException e) {
            System.err.println("❌ [FAIL] Database JDBC error: " + e.getMessage());
            allPassed = false;
        }

        System.out.println("\n==================================================");
        if (allPassed) {
            System.out.println("🎉 ALL 4 REQUIREMENTS SUCCESSFULLY VERIFIED & PASSED!");
        } else {
            System.out.println("⚠️ SOME VERIFICATION TESTS FAILED.");
        }
        System.out.println("==================================================");
    }
}
