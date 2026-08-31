package com.candymatch;

import java.awt.Point;
import java.util.*;

/**
 * Board.java
 * Manages the 8x8 grid matrix of candies.
 * Implements:
 * 1. Grid Generator: Random 8x8 board without initial matches.
 * 2. Swap Engine: Adjacent swaps & swap validation.
 * 3. Match Detection: Horizontal/vertical match scanning.
 * 4. Bomb Expansion: 3x3 surrounding candy removal & 50 bonus score calculation.
 * 5. Gravity & Refill: Shifts remaining candies down & fills top empty cells with random candies (~8% bomb spawn).
 */
public class Board {
    public static final int ROWS = 8;
    public static final int COLS = 8;

    private Candy[][] grid;
    private Random random;
    private static final double BOMB_SPAWN_PROB = 0.08;

    public Board() {
        grid = new Candy[ROWS][COLS];
        random = new Random();
        generateWithoutInitialMatches();
    }

    /**
     * Fills 8x8 grid with random candies guaranteeing NO initial matches.
     */
    public void generateWithoutInitialMatches() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int type;
                do {
                    type = random.nextInt(Candy.TOTAL_TYPES);
                } while (createsMatchAt(r, c, type));

                grid[r][c] = new Candy(r, c, type);
            }
        }
    }

    private boolean createsMatchAt(int r, int c, int type) {
        // Horizontal left check
        if (c >= 2) {
            Candy left1 = grid[r][c - 1];
            Candy left2 = grid[r][c - 2];
            if (left1 != null && left2 != null && left1.getType() == type && left2.getType() == type) {
                return true;
            }
        }
        // Vertical up check
        if (r >= 2) {
            Candy up1 = grid[r - 1][c];
            Candy up2 = grid[r - 2][c];
            if (up1 != null && up2 != null && up1.getType() == type && up2.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public Candy getCandy(int r, int c) {
        if (isValidPosition(r, c)) {
            return grid[r][c];
        }
        return null;
    }

    public void setCandy(int r, int c, Candy candy) {
        if (isValidPosition(r, c)) {
            grid[r][c] = candy;
            if (candy != null) {
                candy.setRow(r);
                candy.setCol(c);
            }
        }
    }

    public boolean isValidPosition(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    public boolean areAdjacent(int r1, int c1, int r2, int c2) {
        if (!isValidPosition(r1, c1) || !isValidPosition(r2, c2)) return false;
        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1);
    }

    public void swap(int r1, int c1, int r2, int c2) {
        if (!isValidPosition(r1, c1) || !isValidPosition(r2, c2)) return;

        Candy c1Obj = grid[r1][c1];
        Candy c2Obj = grid[r2][c2];

        grid[r1][c1] = c2Obj;
        grid[r2][c2] = c1Obj;

        if (c1Obj != null) { c1Obj.setRow(r2); c1Obj.setCol(c2); }
        if (c2Obj != null) { c2Obj.setRow(r1); c2Obj.setCol(c1); }
    }

    /**
     * Match Result container holding matched cells, bomb centers, and score gained.
     */
    public static class MatchResult {
        public Set<Point> matchedPoints = new HashSet<>();
        public Set<Point> explodedPoints = new HashSet<>();
        public List<Point> bombCenters = new ArrayList<>();
        public int scoreGained = 0;

        public boolean hasMatches() {
            return !matchedPoints.isEmpty() || !explodedPoints.isEmpty();
        }

        public Set<Point> getAllPointsToRemove() {
            Set<Point> all = new HashSet<>(matchedPoints);
            all.addAll(explodedPoints);
            return all;
        }
    }

    /**
     * Detects horizontal and vertical matches >= 3 candies.
     * Expands 3x3 surrounding region if a Bomb Candy is matched.
     * Score rule: 10 pts per candy removed + 50 bonus pts per bomb explosion.
     */
    public MatchResult findMatches(int cascadeLevel) {
        MatchResult res = new MatchResult();

        // 1. Horizontal Scan
        for (int r = 0; r < ROWS; r++) {
            int matchLen = 1;
            for (int c = 0; c < COLS; c++) {
                Candy curr = getCandy(r, c);
                Candy next = (c < COLS - 1) ? getCandy(r, c + 1) : null;

                if (curr != null && next != null && curr.getType() != -1 && curr.getType() == next.getType()) {
                    matchLen++;
                } else {
                    if (matchLen >= 3) {
                        for (int k = c - matchLen + 1; k <= c; k++) {
                            res.matchedPoints.add(new Point(k, r)); // x=col, y=row
                        }
                    }
                    matchLen = 1;
                }
            }
        }

        // 2. Vertical Scan
        for (int c = 0; c < COLS; c++) {
            int matchLen = 1;
            for (int r = 0; r < ROWS; r++) {
                Candy curr = getCandy(r, c);
                Candy next = (r < ROWS - 1) ? getCandy(r + 1, c) : null;

                if (curr != null && next != null && curr.getType() != -1 && curr.getType() == next.getType()) {
                    matchLen++;
                } else {
                    if (matchLen >= 3) {
                        for (int k = r - matchLen + 1; k <= r; k++) {
                            res.matchedPoints.add(new Point(c, k)); // x=col, y=row
                        }
                    }
                    matchLen = 1;
                }
            }
        }

        // 3. Bomb Candy 3x3 Explosion Expansion (Recursive chain reaction)
        Queue<Point> bombQueue = new LinkedList<>();
        Set<Point> processedBombs = new HashSet<>();

        for (Point pt : res.matchedPoints) {
            Candy candy = getCandy(pt.y, pt.x);
            if (candy != null && candy.isBomb()) {
                bombQueue.add(pt);
                res.bombCenters.add(pt);
                processedBombs.add(pt);
            }
        }

        while (!bombQueue.isEmpty()) {
            Point bombPt = bombQueue.poll();
            int br = bombPt.y;
            int bc = bombPt.x;

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = br + dr;
                    int nc = bc + dc;
                    if (isValidPosition(nr, nc)) {
                        Point neighbor = new Point(nc, nr);
                        res.explodedPoints.add(neighbor);

                        Candy neighborCandy = getCandy(nr, nc);
                        if (neighborCandy != null && neighborCandy.isBomb() && !processedBombs.contains(neighbor)) {
                            processedBombs.add(neighbor);
                            bombQueue.add(neighbor);
                            res.bombCenters.add(neighbor);
                        }
                    }
                }
            }
        }

        // 4. Score Calculation: 10 pts per candy + 50 bonus pts per bomb explosion
        Set<Point> allRemoved = res.getAllPointsToRemove();
        int baseScore = allRemoved.size() * 10;
        int bombBonus = res.bombCenters.size() * 50; // 50 Bonus score per bomb
        res.scoreGained = baseScore + bombBonus + (cascadeLevel * 15);

        return res;
    }

    /**
     * Removes matched candies from grid.
     */
    public void removeCandies(Set<Point> points) {
        for (Point pt : points) {
            setCandy(pt.y, pt.x, null);
        }
    }

    /**
     * Applies gravity physics: shifts non-null candies downward to fill empty bottom cells.
     */
    public boolean applyGravity() {
        boolean shifted = false;
        for (int c = 0; c < COLS; c++) {
            int emptySpot = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                Candy candy = getCandy(r, c);
                if (candy != null) {
                    if (r != emptySpot) {
                        setCandy(emptySpot, c, candy);
                        setCandy(r, c, null);
                        candy.setTargetY(emptySpot);
                        shifted = true;
                    }
                    emptySpot--;
                }
            }
        }
        return shifted;
    }

    /**
     * Refills empty top spaces with new random candies (spawning Bomb Candy with ~8% probability).
     */
    public List<Candy> refillEmptyCells() {
        List<Candy> newCandies = new ArrayList<>();
        for (int c = 0; c < COLS; c++) {
            int spawnIndex = 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                if (getCandy(r, c) == null) {
                    Candy newCandy;
                    if (random.nextDouble() < BOMB_SPAWN_PROB) {
                        newCandy = new BombCandy(r, c);
                    } else {
                        int type = random.nextInt(Candy.TOTAL_TYPES);
                        newCandy = new Candy(r, c, type);
                    }

                    newCandy.setDrawY(-spawnIndex);
                    newCandy.setTargetY(r);
                    setCandy(r, c, newCandy);
                    newCandies.add(newCandy);
                    spawnIndex++;
                }
            }
        }
        return newCandies;
    }

    public Candy[][] getGrid() { return grid; }
}
