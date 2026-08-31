package com.candymatch.candy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Represents an individual candy on the board with animation states and custom properties.
 */
public class Candy {
    private CandyType type;
    private SpecialCandyType specialType;
    private String customName;
    private String customSymbol;
    private Color customColor;
    private int scoreValue;

    // Rendering & Animation state
    private double currentX;
    private double currentY;
    private double targetX;
    private double targetY;
    private double scale = 1.0;
    private float alpha = 1.0f;

    private boolean selected = false;
    private boolean hintHighlighted = false;

    public Candy(CandyType type) {
        this(type, SpecialCandyType.NONE);
    }

    public Candy(CandyType type, SpecialCandyType specialType) {
        this.type = type;
        this.specialType = specialType;
        this.customName = type.getDisplayName();
        this.customSymbol = type.getSymbol();
        this.customColor = type.getColor();
        this.scoreValue = type.getBaseScore();
    }

    public Candy(String name, String symbol, Color color, int scoreValue) {
        this.type = CandyType.CUSTOM;
        this.specialType = SpecialCandyType.NONE;
        this.customName = name;
        this.customSymbol = symbol;
        this.customColor = color;
        this.scoreValue = scoreValue;
    }

    // Copy constructor
    public Candy createCopy() {
        Candy copy = new Candy(this.type, this.specialType);
        copy.customName = this.customName;
        copy.customSymbol = this.customSymbol;
        copy.customColor = this.customColor;
        copy.scoreValue = this.scoreValue;
        return copy;
    }

    public CandyType getType() {
        return type;
    }

    public void setType(CandyType type) {
        this.type = type;
    }

    public SpecialCandyType getSpecialType() {
        return specialType;
    }

    public void setSpecialType(SpecialCandyType specialType) {
        this.specialType = specialType;
    }

    public String getCustomName() {
        return customName;
    }

    public String getCustomSymbol() {
        return customSymbol;
    }

    public Color getColor() {
        return customColor != null ? customColor : type.getColor();
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    // Animation & Selection getters/setters
    public double getCurrentX() { return currentX; }
    public void setCurrentX(double currentX) { this.currentX = currentX; }

    public double getCurrentY() { return currentY; }
    public void setCurrentY(double currentY) { this.currentY = currentY; }

    public double getTargetX() { return targetX; }
    public void setTargetX(double targetX) { this.targetX = targetX; }

    public double getTargetY() { return targetY; }
    public void setTargetY(double targetY) { this.targetY = targetY; }

    public void setPositionImmediate(double x, double y) {
        this.currentX = x;
        this.currentY = y;
        this.targetX = x;
        this.targetY = y;
    }

    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = scale; }

    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = Math.max(0.0f, Math.min(1.0f, alpha)); }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public boolean isHintHighlighted() { return hintHighlighted; }
    public void setHintHighlighted(boolean hintHighlighted) { this.hintHighlighted = hintHighlighted; }

    public void updateAnimation(double lerpFactor) {
        this.currentX += (targetX - currentX) * lerpFactor;
        this.currentY += (targetY - currentY) * lerpFactor;
    }

    /**
     * Render the candy cell onto a 2D Graphics context.
     */
    public void draw(Graphics2D g2, int offsetX, int offsetY, int cellSize) {
        if (alpha <= 0.001f) return;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = offsetX + (int) currentX;
        int cy = offsetY + (int) currentY;

        int size = (int) (cellSize * 0.82 * scale);
        int margin = (cellSize - size) / 2;
        int drawX = cx + margin;
        int drawY = cy + margin;

        Color mainColor = getColor();

        // Selection & Hint glow effects
        if (selected) {
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(4.0f));
            g2.drawRoundRect(drawX - 4, drawY - 4, size + 8, size + 8, 20, 20);
        }

        if (hintHighlighted) {
            g2.setColor(new Color(255, 215, 0, 220));
            g2.setStroke(new BasicStroke(4.5f));
            g2.drawRoundRect(drawX - 5, drawY - 5, size + 10, size + 10, 22, 22);
        }

        // Candy Body with Gradient
        Color lighterColor = new Color(
                Math.min(255, mainColor.getRed() + 50),
                Math.min(255, mainColor.getGreen() + 50),
                Math.min(255, mainColor.getBlue() + 50)
        );
        Color darkerColor = new Color(
                Math.max(0, mainColor.getRed() - 40),
                Math.max(0, mainColor.getGreen() - 40),
                Math.max(0, mainColor.getBlue() - 40)
        );

        GradientPaint bgGradient = new GradientPaint(drawX, drawY, lighterColor, drawX + size, drawY + size, darkerColor);
        g2.setPaint(bgGradient);
        g2.fillRoundRect(drawX, drawY, size, size, 18, 18);

        // Inner Border
        g2.setColor(new Color(255, 255, 255, 100));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(drawX, drawY, size, size, 18, 18);

        // Special Candy Markings
        if (specialType == SpecialCandyType.STRIPED_HORIZONTAL) {
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRect(drawX, drawY + size / 2 - 4, size, 8);
        } else if (specialType == SpecialCandyType.STRIPED_VERTICAL) {
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRect(drawX + size / 2 - 4, drawY, 8, size);
        } else if (specialType == SpecialCandyType.WRAPPED) {
            g2.setColor(new Color(255, 215, 0, 220));
            g2.setStroke(new BasicStroke(3.5f));
            g2.drawRoundRect(drawX + 4, drawY + 4, size - 8, size - 8, 12, 12);
        } else if (specialType == SpecialCandyType.COLOR_BOMB) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillOval(drawX + 4, drawY + 4, size - 8, size - 8);
        }

        // Symbol Text Rendering
        String symbol = (specialType == SpecialCandyType.COLOR_BOMB) ? "💣" : customSymbol;
        g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, (int) (size * 0.48)));
        FontMetrics fm = g2.getFontMetrics();
        int symX = drawX + (size - fm.stringWidth(symbol)) / 2;
        int symY = drawX == cx + margin ? drawY + (size - fm.getHeight()) / 2 + fm.getAscent() : drawY + (size - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(Color.WHITE);
        g2.drawString(symbol, symX, symY);
    }
}
