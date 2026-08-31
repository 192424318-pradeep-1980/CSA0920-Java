package com.candymatch.match;

import com.candymatch.candy.CandyType;
import com.candymatch.candy.SpecialCandyType;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

/**
 * Data structure representing a detected group of matching candies.
 */
public class Match {
    public enum PatternShape {
        MATCH_3,
        MATCH_4_HORIZ,
        MATCH_4_VERT,
        T_OR_L_SHAPE,
        MATCH_5_COLOR_BOMB
    }

    private final Set<Point> matchedPoints = new HashSet<>();
    private CandyType candyType;
    private PatternShape shape = PatternShape.MATCH_3;
    private SpecialCandyType specialToSpawn = SpecialCandyType.NONE;
    private Point specialSpawnPoint = null;

    public Match(CandyType candyType) {
        this.candyType = candyType;
    }

    public void addPoint(int row, int col) {
        matchedPoints.add(new Point(col, row));
    }

    public Set<Point> getMatchedPoints() {
        return matchedPoints;
    }

    public CandyType getCandyType() {
        return candyType;
    }

    public PatternShape getShape() {
        return shape;
    }

    public void setShape(PatternShape shape) {
        this.shape = shape;
    }

    public SpecialCandyType getSpecialToSpawn() {
        return specialToSpawn;
    }

    public void setSpecialToSpawn(SpecialCandyType specialToSpawn) {
        this.specialToSpawn = specialToSpawn;
    }

    public Point getSpecialSpawnPoint() {
        return specialSpawnPoint;
    }

    public void setSpecialSpawnPoint(Point specialSpawnPoint) {
        this.specialSpawnPoint = specialSpawnPoint;
    }

    public int getSize() {
        return matchedPoints.size();
    }
}
