package com.candymatch.ailab;

import java.awt.*;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Candy.java
 * Candy model supporting standard candies and unlimited dynamic player-created custom candies.
 * Renders custom shapes (Circle, Square, Diamond, Hexagon, Star) and custom icons.
 */
public class Candy {
    private String id;
    private String name;
    private Color primaryColor;
    private Color secondaryColor;
    private int tier;
    private String description;
    private String shapeType = "Circle";
    private String iconSymbol = "";

    // Custom Candies Registry
    private static final Map<String, Candy> CUSTOM_CANDY_REGISTRY = new HashMap<>();

    // Board position & animation fields
    protected int row;
    protected int col;
    protected double drawX;
    protected double drawY;
    protected double targetX;
    protected double targetY;
    protected double scale = 1.0;
    protected float alpha = 1.0f;
    protected boolean selected = false;

    public Candy(String id, String name, Color primaryColor, Color secondaryColor, int tier, String description) {
        this.id = id.toUpperCase();
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.tier = tier;
        this.description = description;
    }

    public Candy(String id, String name, Color primaryColor, Color secondaryColor, int tier, String description, String shapeType, String iconSymbol) {
        this(id, name, primaryColor, secondaryColor, tier, description);
        this.shapeType = shapeType;
        this.iconSymbol = iconSymbol;
    }

    /**
     * Registers a new player-created custom candy dynamically with shape and icon.
     */
    public static boolean registerCustomCandy(String id, String name, Color primary, String shape, String icon) {
        String key = id.toUpperCase();
        if (hasCandy(name) || CUSTOM_CANDY_REGISTRY.containsKey(key)) {
            return false; // Avoid duplicate candy names
        }

        Color secondary = new Color(Math.min(255, primary.getRed() + 50), Math.min(255, primary.getGreen() + 50), Math.min(255, primary.getBlue() + 50));
        Candy customCandy = new Candy(key, name, primary, secondary, 2, "Custom scientist candy: " + name, shape, icon);
        CUSTOM_CANDY_REGISTRY.put(key, customCandy);
        return true;
    }

    public static boolean hasCandy(String nameOrId) {
        String key = nameOrId.toUpperCase().replaceAll("\\s+", "_");
        if (CUSTOM_CANDY_REGISTRY.containsKey(key)) return true;
        for (Candy c : CUSTOM_CANDY_REGISTRY.values()) {
            if (c.getName().equalsIgnoreCase(nameOrId)) return true;
        }
        return false;
    }

    public static Map<String, Candy> getCustomCandyRegistry() {
        return CUSTOM_CANDY_REGISTRY;
    }

    public static Candy create(String id, int row, int col) {
        String key = id.toUpperCase();
        Candy candy;

        if (CUSTOM_CANDY_REGISTRY.containsKey(key)) {
            Candy template = CUSTOM_CANDY_REGISTRY.get(key);
            candy = new Candy(template.getId(), template.getName(), template.getPrimaryColor(), template.getSecondaryColor(), template.getTier(), template.getDescription(), template.getShapeType(), template.getIconSymbol());
        } else {
            switch (key) {
                case "RED":
                    candy = new Candy("RED", "Red Berry", new Color(220, 20, 60), new Color(255, 120, 140), 1, "A basic sweet red berry candy.", "Circle", "🍓");
                    break;
                case "BLUE":
                    candy = new Candy("BLUE", "Blue Sapphire", new Color(30, 144, 255), new Color(140, 210, 255), 1, "A crisp blue ocean candy.", "Circle", "💎");
                    break;
                case "GREEN":
                    candy = new Candy("GREEN", "Green Lime", new Color(46, 139, 87), new Color(130, 250, 130), 1, "A tangy citrus lime candy.", "Circle", "🍏");
                    break;
                case "YELLOW":
                    candy = new Candy("YELLOW", "Sunburst Yellow", new Color(255, 180, 0), new Color(255, 240, 130), 1, "A bright zesty lemon candy.", "Circle", "🍋");
                    break;
                case "PURPLE":
                    candy = new Candy("PURPLE", "Purple Grape", new Color(138, 43, 226), new Color(220, 180, 255), 1, "A rich sweet grape candy.", "Circle", "🍇");
                    break;
                case "RUBY":
                    candy = new EvolvedCandy("RUBY", "Ruby Gem", new Color(180, 0, 40), new Color(255, 90, 120), 2, "Synthesized from 2 Red Berries.", "🔻");
                    break;
                case "CRYSTAL":
                    candy = new EvolvedCandy("CRYSTAL", "Crystal Quartz", new Color(0, 206, 209), new Color(180, 255, 255), 2, "Pure crystalline candy energy.", "🔮");
                    break;
                case "EMERALD":
                    candy = new EvolvedCandy("EMERALD", "Emerald Essence", new Color(0, 168, 107), new Color(120, 255, 180), 2, "Concentrated botanical emerald extract.", "🌿");
                    break;
                case "SUNBURST":
                    candy = new EvolvedCandy("SUNBURST", "Solar Flare", new Color(255, 140, 0), new Color(255, 220, 100), 2, "Radiant solar energy candy.", "☀️");
                    break;
                case "AMETHYST":
                    candy = new EvolvedCandy("AMETHYST", "Amethyst Orb", new Color(75, 0, 130), new Color(180, 120, 255), 2, "Mystical deep amethyst magic candy.", "✨");
                    break;
                case "GALAXY":
                default:
                    candy = new LegendaryCandy("GALAXY", "Galaxy Cosmic", new Color(25, 20, 60), new Color(255, 105, 180), 3, "The ultimate cosmic AI lab synthesis!", "🌌");
                    break;
            }
        }

        candy.setRow(row);
        candy.setCol(col);
        candy.drawX = col;
        candy.drawY = row;
        candy.targetX = col;
        candy.targetY = row;

        return candy;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
    public int getTier() { return tier; }
    public String getDescription() { return description; }
    public String getShapeType() { return shapeType; }
    public String getIconSymbol() { return iconSymbol; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; this.targetY = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; this.targetX = col; }

    public double getDrawX() { return drawX; }
    public double getDrawY() { return drawY; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

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

    public void draw(Graphics2D g2, int offsetX, int offsetY, int cellSize) {
        if (alpha <= 0.01f) return;

        int px = (int) (offsetX + drawX * cellSize);
        int py = (int) (offsetY + drawY * cellSize);

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = px + cellSize / 2.0;
        double cy = py + cellSize / 2.0;
        g.translate(cx, cy);
        g.scale(scale, scale);
        g.translate(-cx, -cy);

        int margin = 5;
        int size = cellSize - margin * 2;
        int x = px + margin;
        int y = py + margin;

        if (selected) {
            g.setColor(new Color(255, 255, 255, 220));
            g.setStroke(new BasicStroke(3.5f));
            g.drawRoundRect(x - 2, y - 2, size + 4, size + 4, 16, 16);
        }

        GradientPaint grad = new GradientPaint(x, y, secondaryColor, x, y + size, primaryColor);
        g.setPaint(grad);

        Shape shape = createShape(shapeType, x, y, size);
        g.fill(shape);

        g.setColor(new Color(255, 255, 255, 140));
        g.setStroke(new BasicStroke(2.0f));
        g.draw(shape);

        // Icon symbol in center
        if (iconSymbol != null && !iconSymbol.isEmpty()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI Emoji", Font.BOLD, (int) (size * 0.4)));
            FontMetrics fm = g.getFontMetrics();
            int symW = fm.stringWidth(iconSymbol);
            int symH = fm.getAscent();
            g.drawString(iconSymbol, x + (size - symW) / 2, y + (size + symH) / 2 - 3);
        }

        g.dispose();
    }

    private Shape createShape(String shape, int x, int y, int size) {
        switch (shape.toLowerCase()) {
            case "square":
                return new RoundRectangle2D.Double(x, y, size, size, 12, 12);
            case "diamond":
                Polygon diamond = new Polygon();
                diamond.addPoint(x + size / 2, y);
                diamond.addPoint(x + size, y + size / 2);
                diamond.addPoint(x + size / 2, y + size);
                diamond.addPoint(x, y + size / 2);
                return diamond;
            case "hexagon":
                Polygon hex = new Polygon();
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 3 * i - Math.PI / 6;
                    int px = (int) (x + size / 2 + (size / 2) * Math.cos(angle));
                    int py = (int) (y + size / 2 + (size / 2) * Math.sin(angle));
                    hex.addPoint(px, py);
                }
                return hex;
            case "star":
                Polygon star = new Polygon();
                double rOuter = size / 2.0;
                double rInner = size / 4.0;
                double cx = x + size / 2.0;
                double cy = y + size / 2.0;
                for (int i = 0; i < 10; i++) {
                    double r = (i % 2 == 0) ? rOuter : rInner;
                    double angle = i * Math.PI / 5 - Math.PI / 2;
                    star.addPoint((int) (cx + r * Math.cos(angle)), (int) (cy + r * Math.sin(angle)));
                }
                return star;
            case "circle":
            default:
                return new Ellipse2D.Double(x, y, size, size);
        }
    }
}

class EvolvedCandy extends Candy {
    public EvolvedCandy(String id, String name, Color primary, Color secondary, int tier, String desc, String symbol) {
        super(id, name, primary, secondary, tier, desc, "Circle", symbol);
    }
}

class LegendaryCandy extends Candy {
    public LegendaryCandy(String id, String name, Color primary, Color secondary, int tier, String desc, String symbol) {
        super(id, name, primary, secondary, tier, desc, "Circle", symbol);
    }
}
