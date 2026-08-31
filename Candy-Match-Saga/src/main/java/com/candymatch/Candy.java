package com.candymatch;

import java.awt.*;
import java.awt.geom.*;

/**
 * Candy.java
 * Represents a single candy on the 8x8 game board.
 * Supports 5 distinct candy types with 2D glossy graphics, smooth animations, and selection highlight.
 */
public class Candy {
    // 5 Candy Types (Red, Blue, Green, Yellow, Purple)
    public static final int TYPE_RED = 0;
    public static final int TYPE_BLUE = 1;
    public static final int TYPE_GREEN = 2;
    public static final int TYPE_YELLOW = 3;
    public static final int TYPE_PURPLE = 4;
    public static final int TOTAL_TYPES = 5;

    protected int row;
    protected int col;
    protected int type;

    // Smooth animation rendering coordinates
    protected double drawX;
    protected double drawY;
    protected double targetX;
    protected double targetY;

    // Visual animation states
    protected double scale = 1.0;
    protected float alpha = 1.0f;
    protected boolean selected = false;
    protected boolean isBomb = false;

    public Candy(int row, int col, int type) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.drawX = col;
        this.drawY = row;
        this.targetX = col;
        this.targetY = row;
    }

    public int getRow() { return row; }
    public void setRow(int row) { 
        this.row = row; 
        this.targetY = row;
    }

    public int getCol() { return col; }
    public void setCol(int col) { 
        this.col = col; 
        this.targetX = col;
    }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public double getDrawX() { return drawX; }
    public void setDrawX(double drawX) { this.drawX = drawX; }

    public double getDrawY() { return drawY; }
    public void setDrawY(double drawY) { this.drawY = drawY; }

    public double getTargetX() { return targetX; }
    public void setTargetX(double targetX) { this.targetX = targetX; }

    public double getTargetY() { return targetY; }
    public void setTargetY(double targetY) { this.targetY = targetY; }

    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = scale; }

    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = Math.max(0.0f, Math.min(1.0f, alpha)); }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public boolean isBomb() { return isBomb; }

    /**
     * Smoothly interpolates current draw position toward target position.
     * Returns true if position is still animating.
     */
    public boolean updateAnimation(double speed) {
        boolean moving = false;
        double dx = targetX - drawX;
        double dy = targetY - drawY;

        if (Math.abs(dx) > 0.001) {
            drawX += dx * speed;
            moving = true;
        } else {
            drawX = targetX;
        }

        if (Math.abs(dy) > 0.001) {
            drawY += dy * speed;
            moving = true;
        } else {
            drawY = targetY;
        }

        return moving;
    }

    /**
     * Renders candy shape with glossy 2D graphics.
     */
    public void draw(Graphics2D g2, int offsetX, int offsetY, int cellSize) {
        if (alpha <= 0.01f) return;

        int px = (int) (offsetX + drawX * cellSize);
        int py = (int) (offsetY + drawY * cellSize);

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (alpha < 1.0f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        // Apply scale transform around center of candy
        double centerX = px + cellSize / 2.0;
        double centerY = py + cellSize / 2.0;
        g.translate(centerX, centerY);
        g.scale(scale, scale);
        g.translate(-centerX, -centerY);

        int margin = 5;
        int size = cellSize - margin * 2;
        int x = px + margin;
        int y = py + margin;

        // Draw Selection Aura if selected
        if (selected) {
            g.setColor(new Color(255, 255, 255, 200));
            g.setStroke(new BasicStroke(3.5f));
            g.drawRoundRect(x - 2, y - 2, size + 4, size + 4, 16, 16);
            g.setColor(new Color(255, 215, 0, 160));
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(x - 4, y - 4, size + 8, size + 8, 20, 20);
        }

        drawCandyShape(g, x, y, size, type);

        g.dispose();
    }

    protected void drawCandyShape(Graphics2D g, int x, int y, int size, int type) {
        Color baseColor, topColor;

        switch (type) {
            case TYPE_RED: // Strawberry Red
                baseColor = new Color(220, 20, 60);
                topColor = new Color(255, 100, 130);
                break;
            case TYPE_BLUE: // Sapphire Blue
                baseColor = new Color(30, 144, 255);
                topColor = new Color(135, 206, 250);
                break;
            case TYPE_GREEN: // Emerald Green
                baseColor = new Color(46, 139, 87);
                topColor = new Color(124, 252, 0);
                break;
            case TYPE_YELLOW: // Sunburst Yellow
                baseColor = new Color(255, 180, 0);
                topColor = new Color(255, 240, 120);
                break;
            case TYPE_PURPLE: // Amethyst Purple
            default:
                baseColor = new Color(138, 43, 226);
                topColor = new Color(216, 191, 216);
                break;
        }

        // Draw Shadow
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval(x + 2, y + 4, size - 2, size - 2);

        // Candy Gradient Body
        GradientPaint grad = new GradientPaint(x, y, topColor, x, y + size, baseColor);
        g.setPaint(grad);

        Shape shape;
        if (type == TYPE_BLUE) {
            // Hexagon shape
            Polygon hex = new Polygon();
            hex.addPoint(x + size / 2, y);
            hex.addPoint(x + size, y + size / 4);
            hex.addPoint(x + size, y + 3 * size / 4);
            hex.addPoint(x + size / 2, y + size);
            hex.addPoint(x, y + 3 * size / 4);
            hex.addPoint(x, y + size / 4);
            shape = hex;
        } else if (type == TYPE_GREEN) {
            // Rounded Square
            shape = new RoundRectangle2D.Double(x, y, size, size, 14, 14);
        } else if (type == TYPE_YELLOW) {
            // Diamond
            Polygon diamond = new Polygon();
            diamond.addPoint(x + size / 2, y);
            diamond.addPoint(x + size, y + size / 2);
            diamond.addPoint(x + size / 2, y + size);
            diamond.addPoint(x, y + size / 2);
            shape = diamond;
        } else {
            // Circle
            shape = new Ellipse2D.Double(x, y, size, size);
        }

        g.fill(shape);

        // Border Outline
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(1.8f));
        g.draw(shape);

        // Glossy Highlight Reflection Curve
        g.setColor(new Color(255, 255, 255, 150));
        Ellipse2D gloss = new Ellipse2D.Double(x + size * 0.2, y + size * 0.12, size * 0.55, size * 0.3);
        g.fill(gloss);
    }
}
