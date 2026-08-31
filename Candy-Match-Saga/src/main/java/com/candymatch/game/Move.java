package com.candymatch.game;

import com.candymatch.candy.SpecialCandyType;

/**
 * Value object representing a swap move between two cells.
 */
public class Move implements Comparable<Move> {
    private final int row1;
    private final int col1;
    private final int row2;
    private final int col2;

    private int matchSize = 3;
    private int predictedScore = 30;
    private int priorityRank = 1; // 5=Match 5/Color Bomb, 4=T/L Wrapped, 3=Match 4 Striped, 2=Match 3, 1=Basic
    private SpecialCandyType specialTypeCreated = SpecialCandyType.NONE;

    public Move(int row1, int col1, int row2, int col2) {
        this.row1 = row1;
        this.col1 = col1;
        this.row2 = row2;
        this.col2 = col2;
    }

    public Move(int row1, int col1, int row2, int col2, int matchSize, int predictedScore, int priorityRank, SpecialCandyType specialTypeCreated) {
        this.row1 = row1;
        this.col1 = col1;
        this.row2 = row2;
        this.col2 = col2;
        this.matchSize = matchSize;
        this.predictedScore = predictedScore;
        this.priorityRank = priorityRank;
        this.specialTypeCreated = specialTypeCreated;
    }

    public int getRow1() { return row1; }
    public int getCol1() { return col1; }
    public int getRow2() { return row2; }
    public int getCol2() { return col2; }

    public int getMatchSize() { return matchSize; }
    public int getPredictedScore() { return predictedScore; }
    public int getPriorityRank() { return priorityRank; }
    public SpecialCandyType getSpecialTypeCreated() { return specialTypeCreated; }

    @Override
    public int compareTo(Move o) {
        if (this.priorityRank != o.priorityRank) {
            return Integer.compare(o.priorityRank, this.priorityRank); // Higher rank first
        }
        if (this.predictedScore != o.predictedScore) {
            return Integer.compare(o.predictedScore, this.predictedScore);
        }
        return Integer.compare(o.matchSize, this.matchSize);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Move)) return false;
        Move m = (Move) obj;
        return (row1 == m.row1 && col1 == m.col1 && row2 == m.row2 && col2 == m.col2) ||
               (row1 == m.row2 && col1 == m.col2 && row2 == m.row1 && col2 == m.col1);
    }

    @Override
    public int hashCode() {
        return row1 + col1 + row2 + col2;
    }

    @Override
    public String toString() {
        return String.format("Move [(%d,%d) <-> (%d,%d) | Rank:%d | Score:%d]", row1, col1, row2, col2, priorityRank, predictedScore);
    }
}
