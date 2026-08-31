package com.candymatch.game;

import com.candymatch.candy.Candy;

/**
 * Grid cell holding a Candy reference and position metadata.
 */
public class Cell {
    private final int row;
    private final int col;
    private Candy candy;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.candy = null;
    }

    public Cell(int row, int col, Candy candy) {
        this.row = row;
        this.col = col;
        this.candy = candy;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Candy getCandy() {
        return candy;
    }

    public void setCandy(Candy candy) {
        this.candy = candy;
    }

    public boolean isEmpty() {
        return candy == null;
    }
}
