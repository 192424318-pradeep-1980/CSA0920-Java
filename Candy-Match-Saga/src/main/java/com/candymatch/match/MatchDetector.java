package com.candymatch.match;

import com.candymatch.candy.Candy;
import com.candymatch.candy.CandyType;
import com.candymatch.candy.SpecialCandyType;
import com.candymatch.game.GameBoard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects all horizontal, vertical, and overlapping (T/L/Match4/Match5) match patterns on the board.
 */
public class MatchDetector {

    public static List<Match> detectAllMatches(GameBoard board) {
        return detectAllMatches(board, null);
    }

    public static List<Match> detectAllMatches(GameBoard board, Point preferredSpawn) {
        List<Set<Point>> horizontalGroups = new ArrayList<>();
        List<Set<Point>> verticalGroups = new ArrayList<>();

        // 1. Scan horizontal matches
        for (int r = 0; r < GameBoard.ROWS; r++) {
            int count = 1;
            for (int c = 0; c < GameBoard.COLS; c++) {
                Candy current = board.getCandy(r, c);
                Candy next = (c + 1 < GameBoard.COLS) ? board.getCandy(r, c + 1) : null;

                if (current != null && next != null && current.getType() == next.getType()) {
                    count++;
                } else {
                    if (count >= 3 && current != null) {
                        Set<Point> group = new HashSet<>();
                        for (int k = c - count + 1; k <= c; k++) {
                            group.add(new Point(k, r));
                        }
                        horizontalGroups.add(group);
                    }
                    count = 1;
                }
            }
        }

        // 2. Scan vertical matches
        for (int c = 0; c < GameBoard.COLS; c++) {
            int count = 1;
            for (int r = 0; r < GameBoard.ROWS; r++) {
                Candy current = board.getCandy(r, c);
                Candy next = (r + 1 < GameBoard.ROWS) ? board.getCandy(r + 1, c) : null;

                if (current != null && next != null && current.getType() == next.getType()) {
                    count++;
                } else {
                    if (count >= 3 && current != null) {
                        Set<Point> group = new HashSet<>();
                        for (int k = r - count + 1; k <= r; k++) {
                            group.add(new Point(c, k));
                        }
                        verticalGroups.add(group);
                    }
                    count = 1;
                }
            }
        }

        // 3. Combine & classify matches
        List<Match> finalMatches = new ArrayList<>();
        Set<Set<Point>> processedHoriz = new HashSet<>();
        Set<Set<Point>> processedVert = new HashSet<>();

        // Check for T or L shapes (intersection of horizontal & vertical)
        for (Set<Point> hGroup : horizontalGroups) {
            for (Set<Point> vGroup : verticalGroups) {
                Set<Point> intersection = new HashSet<>(hGroup);
                intersection.retainAll(vGroup);

                if (!intersection.isEmpty()) {
                    // Intersection found! Form T/L shape
                    Point intersectPoint = intersection.iterator().next();
                    Point candyPt = hGroup.iterator().next();
                    Candy cObj = board.getCandy(candyPt.y, candyPt.x);
                    CandyType type = cObj != null ? cObj.getType() : CandyType.RED;

                    Match match = new Match(type);
                    hGroup.forEach(pt -> match.addPoint(pt.y, pt.x));
                    vGroup.forEach(pt -> match.addPoint(pt.y, pt.x));

                    match.setShape(Match.PatternShape.T_OR_L_SHAPE);
                    match.setSpecialToSpawn(SpecialCandyType.WRAPPED);
                    match.setSpecialSpawnPoint(preferredSpawn != null && match.getMatchedPoints().contains(preferredSpawn) ? preferredSpawn : intersectPoint);

                    finalMatches.add(match);
                    processedHoriz.add(hGroup);
                    processedVert.add(vGroup);
                }
            }
        }

        // Process remaining horizontal groups
        for (Set<Point> hGroup : horizontalGroups) {
            if (processedHoriz.contains(hGroup)) continue;

            Point candyPt = hGroup.iterator().next();
            Candy cObj = board.getCandy(candyPt.y, candyPt.x);
            CandyType type = cObj != null ? cObj.getType() : CandyType.RED;

            Match match = new Match(type);
            hGroup.forEach(pt -> match.addPoint(pt.y, pt.x));

            if (hGroup.size() >= 5) {
                match.setShape(Match.PatternShape.MATCH_5_COLOR_BOMB);
                match.setSpecialToSpawn(SpecialCandyType.COLOR_BOMB);
                match.setSpecialSpawnPoint(selectSpawnPoint(hGroup, preferredSpawn));
            } else if (hGroup.size() == 4) {
                match.setShape(Match.PatternShape.MATCH_4_HORIZ);
                match.setSpecialToSpawn(SpecialCandyType.STRIPED_HORIZONTAL);
                match.setSpecialSpawnPoint(selectSpawnPoint(hGroup, preferredSpawn));
            } else {
                match.setShape(Match.PatternShape.MATCH_3);
            }
            finalMatches.add(match);
        }

        // Process remaining vertical groups
        for (Set<Point> vGroup : verticalGroups) {
            if (processedVert.contains(vGroup)) continue;

            Point candyPt = vGroup.iterator().next();
            Candy cObj = board.getCandy(candyPt.y, candyPt.x);
            CandyType type = cObj != null ? cObj.getType() : CandyType.RED;

            Match match = new Match(type);
            vGroup.forEach(pt -> match.addPoint(pt.y, pt.x));

            if (vGroup.size() >= 5) {
                match.setShape(Match.PatternShape.MATCH_5_COLOR_BOMB);
                match.setSpecialToSpawn(SpecialCandyType.COLOR_BOMB);
                match.setSpecialSpawnPoint(selectSpawnPoint(vGroup, preferredSpawn));
            } else if (vGroup.size() == 4) {
                match.setShape(Match.PatternShape.MATCH_4_VERT);
                match.setSpecialToSpawn(SpecialCandyType.STRIPED_VERTICAL);
                match.setSpecialSpawnPoint(selectSpawnPoint(vGroup, preferredSpawn));
            } else {
                match.setShape(Match.PatternShape.MATCH_3);
            }
            finalMatches.add(match);
        }

        return finalMatches;
    }

    private static Point selectSpawnPoint(Set<Point> group, Point preferred) {
        if (preferred != null && group.contains(preferred)) {
            return preferred;
        }
        return group.iterator().next();
    }
}
