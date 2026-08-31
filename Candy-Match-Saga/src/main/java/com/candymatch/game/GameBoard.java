package com.candymatch.game;

import com.candymatch.candy.Candy;
import com.candymatch.candy.CandyType;
import com.candymatch.candy.SpecialCandyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 8x8 Board representation managing grid cells, candies, swap validation, gravity, and refilling.
 */
public class GameBoard {
    public static final int ROWS = 8;
    public static final int COLS = 8;

    private final Cell[][] grid;

    public GameBoard() {
        this.grid = new Cell[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
        generateWithoutInitialMatches();
    }

    public static boolean isValidPosition(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public static boolean areAdjacent(int r1, int c1, int r2, int c2) {
        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1);
    }

    public Cell getCell(int r, int c) {
        if (!isValidPosition(r, c)) return null;
        return grid[r][c];
    }

    public Candy getCandy(int r, int c) {
        Cell cell = getCell(r, c);
        return cell != null ? cell.getCandy() : null;
    }

    public void setCandy(int r, int c, Candy candy) {
        Cell cell = getCell(r, c);
        if (cell != null) {
            cell.setCandy(candy);
            if (candy != null) {
                candy.setPositionImmediate(c * 64, r * 64);
            }
        }
    }

    public void swap(int r1, int c1, int r2, int c2) {
        if (!isValidPosition(r1, c1) || !isValidPosition(r2, c2)) return;

        Candy c1Obj = getCandy(r1, c1);
        Candy c2Obj = getCandy(r2, c2);

        grid[r1][c1].setCandy(c2Obj);
        grid[r2][c2].setCandy(c1Obj);

        if (c1Obj != null) {
            c1Obj.setTargetX(c2 * 64);
            c1Obj.setTargetY(r2 * 64);
        }
        if (c2Obj != null) {
            c2Obj.setTargetX(c1 * 64);
            c2Obj.setTargetY(r1 * 64);
        }
    }

    public void swapSilent(int r1, int c1, int r2, int c2) {
        if (!isValidPosition(r1, c1) || !isValidPosition(r2, c2)) return;

        Candy c1Obj = getCandy(r1, c1);
        Candy c2Obj = getCandy(r2, c2);

        grid[r1][c1].setCandy(c2Obj);
        grid[r2][c2].setCandy(c1Obj);
    }

    /**
     * Initializes the grid ensuring NO 3-matches exist initially,
     * and at least ONE valid move is available.
     */
    public void generateWithoutInitialMatches() {
        boolean validBoardGenerated = false;

        while (!validBoardGenerated) {
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    CandyType type;
                    do {
                        type = CandyType.getRandomStandardType();
                    } while (createsInitialMatch(r, c, type));

                    Candy candy = new Candy(type);
                    candy.setPositionImmediate(c * 64, r * 64);
                    grid[r][c].setCandy(candy);
                }
            }

            // Check if at least one valid swap move exists
            if (hasValidMoves()) {
                validBoardGenerated = true;
            }
        }
    }

    private boolean createsInitialMatch(int r, int c, CandyType type) {
        // Horizontal check left
        if (c >= 2) {
            Candy c1 = getCandy(r, c - 1);
            Candy c2 = getCandy(r, c - 2);
            if (c1 != null && c2 != null && c1.getType() == type && c2.getType() == type) {
                return true;
            }
        }
        // Vertical check up
        if (r >= 2) {
            Candy c1 = getCandy(r - 1, c);
            Candy c2 = getCandy(r - 2, c);
            if (c1 != null && c2 != null && c1.getType() == type && c2.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public void shuffleBoard() {
        List<Candy> allCandies = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Candy candy = getCandy(r, c);
                if (candy != null) {
                    allCandies.add(candy);
                }
            }
        }

        boolean validShuffle = false;
        int attempts = 0;

        while (!validShuffle && attempts < 100) {
            attempts++;
            Collections.shuffle(allCandies);
            int idx = 0;
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    Candy candy = allCandies.get(idx++);
                    candy.setTargetX(c * 64);
                    candy.setTargetY(r * 64);
                    grid[r][c].setCandy(candy);
                }
            }

            // Verify no immediate matches and at least one move exists
            if (!hasImmediateMatch() && hasValidMoves()) {
                validShuffle = true;
            }
        }

        if (!validShuffle) {
            generateWithoutInitialMatches();
        }
    }

    public boolean hasImmediateMatch() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Candy current = getCandy(r, c);
                if (current == null) continue;
                // Horizontal
                if (c + 2 < COLS) {
                    Candy c1 = getCandy(r, c + 1);
                    Candy c2 = getCandy(r, c + 2);
                    if (c1 != null && c2 != null && current.getType() == c1.getType() && current.getType() == c2.getType()) {
                        return true;
                    }
                }
                // Vertical
                if (r + 2 < ROWS) {
                    Candy c1 = getCandy(r + 1, c);
                    Candy c2 = getCandy(r + 2, c);
                    if (c1 != null && c2 != null && current.getType() == c1.getType() && current.getType() == c2.getType()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasValidMoves() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // Check right swap
                if (c + 1 < COLS) {
                    swapSilent(r, c, r, c + 1);
                    boolean match = hasImmediateMatch();
                    swapSilent(r, c, r, c + 1);
                    if (match) return true;
                }
                // Check down swap
                if (r + 1 < ROWS) {
                    swapSilent(r, c, r + 1, c);
                    boolean match = hasImmediateMatch();
                    swapSilent(r, c, r + 1, c);
                    if (match) return true;
                }
            }
        }
        return false;
    }

    public void applyGravity() {
        for (int c = 0; c < COLS; c++) {
            for (int r = ROWS - 1; r >= 0; r--) {
                if (getCandy(r, c) == null) {
                    // Find highest non-null candy above
                    for (int aboveR = r - 1; aboveR >= 0; aboveR--) {
                        Candy above = getCandy(aboveR, c);
                        if (above != null) {
                            grid[r][c].setCandy(above);
                            grid[aboveR][c].setCandy(null);
                            above.setTargetY(r * 64);
                            above.setTargetX(c * 64);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void refillEmptyCells() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (getCandy(r, c) == null) {
                    Candy newCandy = new Candy(CandyType.getRandomStandardType());
                    // Start animation from above board
                    newCandy.setCurrentX(c * 64);
                    newCandy.setCurrentY(-64 * (ROWS - r));
                    newCandy.setTargetX(c * 64);
                    newCandy.setTargetY(r * 64);
                    grid[r][c].setCandy(newCandy);
                }
            }
        }
    }

    public void clearAllHighlights() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Candy cObj = getCandy(r, c);
                if (cObj != null) {
                    cObj.setSelected(false);
                    cObj.setHintHighlighted(false);
                }
            }
        }
    }
}
