package com.candymatch.match;

import com.candymatch.candy.Candy;
import com.candymatch.candy.CandyType;
import com.candymatch.candy.SpecialCandyType;
import com.candymatch.game.GameBoard;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes match resolution, special candy spawns, special explosions, score calculation, and cascade loops.
 */
public class MatchProcessor {

    public static class ProcessResult {
        public int scoreEarned = 0;
        public int match3Count = 0;
        public int match4Count = 0;
        public int match5Count = 0;
        public int specialCandiesCreated = 0;
        public int specialCandiesActivated = 0;
        public int cascadeCount = 0;
        public Set<Point> clearedPoints = new HashSet<>();
    }

    public static ProcessResult processSingleCascade(GameBoard board, ComboManager comboManager, Point swapOrigin) {
        ProcessResult result = new ProcessResult();
        List<Match> matches = MatchDetector.detectAllMatches(board, swapOrigin);

        if (matches.isEmpty()) {
            return result;
        }

        Set<Point> pointsToRemove = new HashSet<>();
        Set<Point> specialExplosions = new HashSet<>();

        int rawScore = 0;

        for (Match match : matches) {
            pointsToRemove.addAll(match.getMatchedPoints());

            int size = match.getSize();
            if (size >= 5) {
                result.match5Count++;
                rawScore += 100;
            } else if (size == 4) {
                result.match4Count++;
                rawScore += 60;
            } else {
                result.match3Count++;
                rawScore += 30;
            }

            // Check if special candy should be created
            if (match.getSpecialToSpawn() != SpecialCandyType.NONE && match.getSpecialSpawnPoint() != null) {
                Point spawnPt = match.getSpecialSpawnPoint();
                // Spawn special candy
                Candy specialCandy = new Candy(match.getCandyType(), match.getSpecialToSpawn());
                specialCandy.setPositionImmediate(spawnPt.x * 64, spawnPt.y * 64);
                board.setCandy(spawnPt.y, spawnPt.x, specialCandy);
                pointsToRemove.remove(spawnPt);
                result.specialCandiesCreated++;
            }
        }

        // Check for special candies being matched/detonated inside pointsToRemove
        for (Point pt : new HashSet<>(pointsToRemove)) {
            Candy c = board.getCandy(pt.y, pt.x);
            if (c != null && c.getSpecialType() != SpecialCandyType.NONE) {
                result.specialCandiesActivated++;
                triggerSpecialEffect(board, pt.y, pt.x, c.getSpecialType(), c.getType(), pointsToRemove, specialExplosions);
            }
        }

        pointsToRemove.addAll(specialExplosions);

        // Remove candies from board
        for (Point pt : pointsToRemove) {
            board.setCandy(pt.y, pt.x, null);
        }

        result.clearedPoints = pointsToRemove;
        result.scoreEarned = comboManager.calculateScore(rawScore);
        comboManager.incrementCombo();

        // Apply gravity & refill
        GravityManager.applyGravity(board);
        BoardRefillManager.refillBoard(board);

        return result;
    }

    private static void triggerSpecialEffect(GameBoard board, int r, int c, SpecialCandyType specialType, CandyType cType, Set<Point> pointsToRemove, Set<Point> extraPoints) {
        if (specialType == SpecialCandyType.STRIPED_HORIZONTAL) {
            for (int col = 0; col < GameBoard.COLS; col++) {
                extraPoints.add(new Point(col, r));
            }
        } else if (specialType == SpecialCandyType.STRIPED_VERTICAL) {
            for (int row = 0; row < GameBoard.ROWS; row++) {
                extraPoints.add(new Point(c, row));
            }
        } else if (specialType == SpecialCandyType.WRAPPED) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = r + dr;
                    int nc = c + dc;
                    if (GameBoard.isValidPosition(nr, nc)) {
                        extraPoints.add(new Point(nc, nr));
                    }
                }
            }
        } else if (specialType == SpecialCandyType.COLOR_BOMB) {
            for (int row = 0; row < GameBoard.ROWS; row++) {
                for (int col = 0; col < GameBoard.COLS; col++) {
                    Candy candy = board.getCandy(row, col);
                    if (candy != null && candy.getType() == cType) {
                        extraPoints.add(new Point(col, row));
                    }
                }
            }
        }
    }
}
