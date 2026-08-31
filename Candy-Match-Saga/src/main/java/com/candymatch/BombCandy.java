package com.candymatch;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * BombCandy.java
 * Specialized Bomb Candy power-up subclass.
 * Appears randomly on the board. When matched, destroys all 8 surrounding candies (3x3 area)
 * and awards 50 bonus score points.
 */
public class BombCandy extends Candy {

    private float sparkTime = 0.0f;

    public BombCandy(int row, int col) {
        super(row, col, -1); // Special type identifier for bomb candy
        this.isBomb = true;
    }

    @Override
    public boolean updateAnimation(double speed) {
        sparkTime += 0.15f;
        return super.updateAnimation(speed);
    }

    @Override
    protected void drawCandyShape(Graphics2D g, int x, int y, int size, int type) {
        // Dark Bomb Shadow
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(x + 2, y + 4, size - 2, size - 2);

        // Dark Metallic Charcoal Body
        GradientPaint bombGrad = new GradientPaint(
                x, y, new Color(75, 75, 90),
                x, y + size, new Color(15, 15, 25)
        );
        g.setPaint(bombGrad);
        g.fillOval(x, y, size, size);

        // Metallic Gold Border Rim
        g.setColor(new Color(255, 215, 0, 220));
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(x, y, size, size);

        // Fuse sticking out top-right
        int fuseStartX = x + size / 2 + 4;
        int fuseStartY = y + 4;
        int fuseEndX = x + size / 2 + 12;
        int fuseEndY = y - 5;

        g.setColor(new Color(139, 69, 19)); // Fuse rope
        g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(fuseStartX, fuseStartY, fuseEndX, fuseEndY);

        // Animated Flame Spark on fuse
        float sparkSize = 6.0f + (float) Math.sin(sparkTime) * 2.0f;
        g.setColor(new Color(255, 60, 0, 230));
        g.fill(new Ellipse2D.Double(fuseEndX - sparkSize / 2, fuseEndY - sparkSize / 2, sparkSize, sparkSize));

        g.setColor(new Color(255, 220, 0, 240));
        g.fill(new Ellipse2D.Double(fuseEndX - sparkSize / 4, fuseEndY - sparkSize / 4, sparkSize / 2, sparkSize / 2));

        // White Glossy Curve
        g.setColor(new Color(255, 255, 255, 150));
        Ellipse2D gloss = new Ellipse2D.Double(x + size * 0.18, y + size * 0.12, size * 0.55, size * 0.3);
        g.fill(gloss);

        // Center Bomb Icon Symbol
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI Emoji", Font.BOLD, (int) (size * 0.45)));
        FontMetrics fm = g.getFontMetrics();
        String symbol = "💣";
        int symW = fm.stringWidth(symbol);
        int symH = fm.getAscent();

        g.drawString(symbol, x + (size - symW) / 2, y + (size + symH) / 2 - 4);
    }
}
