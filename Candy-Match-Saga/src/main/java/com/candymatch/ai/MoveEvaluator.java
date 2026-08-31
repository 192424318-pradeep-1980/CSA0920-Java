package com.candymatch.ai;

import com.candymatch.candy.SpecialCandyType;
import com.candymatch.game.GameBoard;
import com.candymatch.game.Move;
import com.candymatch.match.Match;
import com.candymatch.match.MatchDetector;

import java.util.List;

/**
 * Helper class for scoring candidate moves based on rule-based heuristics.
 */
public class MoveEvaluator {

    public static Move evaluateAndRankMove(GameBoard board, int r1, int c1, int r2, int c2) {
        board.swapSilent(r1, c1, r2, c2);
        List<Match> matches = MatchDetector.detectAllMatches(board);
        board.swapSilent(r1, c1, r2, c2); // restore

        if (matches.isEmpty()) {
            return null;
        }

        int maxMatchSize = 3;
        int priorityRank = 2; // Default Match 3
        int predictedScore = 30;
        SpecialCandyType specialType = SpecialCandyType.NONE;

        for (Match m : matches) {
            int sz = m.getSize();
            if (sz > maxMatchSize) maxMatchSize = sz;

            if (m.getSpecialToSpawn() == SpecialCandyType.COLOR_BOMB || sz >= 5) {
                priorityRank = Math.max(priorityRank, 5);
                specialType = SpecialCandyType.COLOR_BOMB;
                predictedScore += 150;
            } else if (m.getSpecialToSpawn() == SpecialCandyType.WRAPPED || m.getShape() == Match.PatternShape.T_OR_L_SHAPE) {
                priorityRank = Math.max(priorityRank, 4);
                specialType = SpecialCandyType.WRAPPED;
                predictedScore += 100;
            } else if (m.getSpecialToSpawn() != SpecialCandyType.NONE || sz == 4) {
                priorityRank = Math.max(priorityRank, 3);
                specialType = m.getSpecialToSpawn();
                predictedScore += 60;
            } else {
                predictedScore += 30;
            }
        }

        return new Move(r1, c1, r2, c2, maxMatchSize, predictedScore, priorityRank, specialType);
    }
}
