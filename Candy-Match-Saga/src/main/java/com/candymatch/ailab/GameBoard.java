package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * GameBoard.java
 * 8x8 Board Component supporting interactive drag-and-drop merging of identical & evolved candies.
 * Evaluates Candy Rules, updates board matrix, applies gravity, triggers particle FX, and communicates with AI Assistant.
 */
public class GameBoard extends JPanel {

    public static final int ROWS = 8;
    public static final int COLS = 8;
    private int cellSize = 64;

    private Candy[][] grid;
    private Player player;
    private AIAssistant aiAssistant;
    private Runnable onMergeCallback;

    // Drag-and-drop state
    private Candy draggedCandy = null;
    private Point dragStartCell = null;
    private Point currentMousePoint = null;

    // Visual FX
    private List<Particle> particles = new ArrayList<>();
    private List<FloatingText> floatingTexts = new ArrayList<>();

    public GameBoard(Player player, AIAssistant aiAssistant, Runnable onMergeCallback) {
        this.player = player;
        this.aiAssistant = aiAssistant;
        this.onMergeCallback = onMergeCallback;
        this.grid = new Candy[ROWS][COLS];

        setLayout(null);
        setPreferredSize(new Dimension(COLS * cellSize, ROWS * cellSize));
        setOpaque(false);

        initializeBoard();
        bindMouseEvents();

        // 60 FPS Render Timer
        javax.swing.Timer timer = new javax.swing.Timer(16, e -> updateAndRepaint());
        timer.start();
    }

    public void initializeBoard() {
        Random rand = new Random();
        String[] baseTypes = {"RED", "BLUE", "GREEN", "YELLOW", "PURPLE"};

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                String type = baseTypes[rand.nextInt(baseTypes.length)];
                grid[r][c] = Candy.create(type, r, c);
            }
        }
    }

    private void bindMouseEvents() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int c = e.getX() / cellSize;
                int r = e.getY() / cellSize;

                if (isValidCell(r, c)) {
                    draggedCandy = grid[r][c];
                    if (draggedCandy != null) {
                        dragStartCell = new Point(c, r);
                        currentMousePoint = e.getPoint();
                        draggedCandy.setSelected(true);
                        repaint();
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedCandy != null) {
                    currentMousePoint = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedCandy != null && dragStartCell != null) {
                    int targetC = e.getX() / cellSize;
                    int targetR = e.getY() / cellSize;

                    if (isValidCell(targetR, targetC) && (targetR != dragStartCell.y || targetC != dragStartCell.x)) {
                        attemptMerge(dragStartCell, new Point(targetC, targetR));
                    }

                    draggedCandy.setSelected(false);
                    draggedCandy = null;
                    dragStartCell = null;
                    currentMousePoint = null;
                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    /**
     * Attempts to merge candies between start position and target cell.
     */
    private void attemptMerge(Point from, Point to) {
        Candy c1 = grid[from.y][from.x];
        Candy c2 = grid[to.y][to.x];

        if (c1 == null || c2 == null) return;

        List<String> inputs = Arrays.asList(c1.getId(), c2.getId());
        CandyRule matchedRule = aiAssistant.findMatchingRule(inputs);

        // Also check if 3 identical candies exist in adjacent line
        if (matchedRule == null && c1.getId().equalsIgnoreCase(c2.getId())) {
            // Merge 2 identical candies into next tier
            String resultId = getEvolvedResultId(c1.getId());
            if (resultId != null) {
                matchedRule = new CandyRule("AUTO_" + System.currentTimeMillis(), inputs, resultId, "Direct Candy Fusion Synthesis", true);
            }
        }

        if (matchedRule != null) {
            // SUCCESSFUL MERGE & EVOLUTION!
            player.decrementMoves();
            player.addScore(50);

            // AI learns the rule if unknown
            aiAssistant.learnRule(matchedRule);

            // Record discovery
            boolean isNewDiscovery = player.discoverCandy(matchedRule.getResultCandyId());
            if (isNewDiscovery) {
                player.addScore(100);
                spawnFloatingText("🌟 NEW CANDY DISCOVERED! +100", to.x, to.y);
            } else {
                spawnFloatingText("✨ MERGED +50", to.x, to.y);
            }

            // Replace target candy with evolved candy
            grid[to.y][to.x] = Candy.create(matchedRule.getResultCandyId(), to.y, to.x);
            grid[from.y][from.x] = null;

            spawnMergeParticles(to.x, to.y);
            applyGravity();
            refillBoard();

            if (onMergeCallback != null) {
                onMergeCallback.run();
            }
        } else {
            // INVALID DRAG & DROP MERGE
            spawnFloatingText("❌ Invalid Fusion Recipe!", to.x, to.y);
        }
    }

    private String getEvolvedResultId(String baseId) {
        switch (baseId.toUpperCase()) {
            case "RED": return "RUBY";
            case "BLUE": return "CRYSTAL";
            case "GREEN": return "EMERALD";
            case "YELLOW": return "SUNBURST";
            case "PURPLE": return "AMETHYST";
            case "RUBY": case "CRYSTAL": return "GALAXY";
            default: return null;
        }
    }

    public void applyGravity() {
        for (int c = 0; c < COLS; c++) {
            int emptySpot = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                if (grid[r][c] != null) {
                    if (r != emptySpot) {
                        grid[emptySpot][c] = grid[r][c];
                        grid[r][c] = null;
                        grid[emptySpot][c].setRow(emptySpot);
                    }
                    emptySpot--;
                }
            }
        }
    }

    public void refillBoard() {
        Random rand = new Random();
        String[] baseTypes = {"RED", "BLUE", "GREEN", "YELLOW", "PURPLE"};

        for (int c = 0; c < COLS; c++) {
            for (int r = ROWS - 1; r >= 0; r--) {
                if (grid[r][c] == null) {
                    String type = baseTypes[rand.nextInt(baseTypes.length)];
                    grid[r][c] = Candy.create(type, r, c);
                }
            }
        }
    }

    private boolean isValidCell(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    private void spawnMergeParticles(int gridX, int gridY) {
        int px = gridX * cellSize + cellSize / 2;
        int py = gridY * cellSize + cellSize / 2;
        Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double speed = 2.0 + rand.nextDouble() * 6.0;
            Color pColor = rand.nextBoolean() ? new Color(255, 215, 0) : new Color(138, 43, 226);
            particles.add(new Particle(px, py, Math.cos(angle) * speed, Math.sin(angle) * speed, pColor, 25));
        }
    }

    private void spawnFloatingText(String text, int gridX, int gridY) {
        int px = gridX * cellSize + cellSize / 4;
        int py = gridY * cellSize + cellSize / 2;
        floatingTexts.add(new FloatingText(text, px, py));
    }

    private void updateAndRepaint() {
        double speed = 0.22;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != null) {
                    grid[r][c].updateAnimation(speed);
                }
            }
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            if (!particles.get(i).update()) particles.remove(i);
        }

        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            if (!floatingTexts.get(i).update()) floatingTexts.remove(i);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = COLS * cellSize;
        int h = ROWS * cellSize;

        // Outer Glass Panel
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 24, 24));
        g2.setColor(new Color(40, 30, 70, 220));
        g2.fill(new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 20, 20));

        // Checkerboard Grid Cells
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int cx = c * cellSize;
                int cy = r * cellSize;
                if ((r + c) % 2 == 0) {
                    g2.setColor(new Color(255, 255, 255, 14));
                } else {
                    g2.setColor(new Color(0, 0, 0, 25));
                }
                g2.fillRoundRect(cx + 2, cy + 2, cellSize - 4, cellSize - 4, 12, 12);
            }
        }

        // Draw Candies
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Candy cObj = grid[r][c];
                if (cObj != null) {
                    cObj.draw(g2, 0, 0, cellSize);
                }
            }
        }

        // Draw Dragging feedback line
        if (draggedCandy != null && dragStartCell != null && currentMousePoint != null) {
            int sx = dragStartCell.x * cellSize + cellSize / 2;
            int sy = dragStartCell.y * cellSize + cellSize / 2;
            g2.setColor(new Color(255, 215, 0, 200));
            g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 8}, 0));
            g2.drawLine(sx, sy, currentMousePoint.x, currentMousePoint.y);
        }

        // Draw Particles
        for (Particle p : particles) p.draw(g2);

        // Draw Floating Text
        for (FloatingText ft : floatingTexts) ft.draw(g2);
    }

    public Candy[][] getGrid() { return grid; }
    public void setGrid(Candy[][] newGrid) { this.grid = newGrid; }

    private static class Particle {
        double x, y, vx, vy;
        Color color;
        int life, maxLife;

        Particle(double x, double y, double vx, double vy, Color color, int maxLife) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = color; this.life = maxLife; this.maxLife = maxLife;
        }

        boolean update() {
            x += vx; y += vy; vy += 0.2;
            life--;
            return life > 0;
        }

        void draw(Graphics2D g2) {
            float alpha = (float) life / maxLife;
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g2.fillOval((int) x - 4, (int) y - 4, 8, 8);
        }
    }

    private static class FloatingText {
        String text;
        double x, y;
        int life = 40;

        FloatingText(String text, double x, double y) {
            this.text = text; this.x = x; this.y = y;
        }

        boolean update() {
            y -= 1.2;
            life--;
            return life > 0;
        }

        void draw(Graphics2D g2) {
            float alpha = Math.min(1.0f, (float) life / 30.0f);
            g2.setColor(new Color(0, 0, 0, (int) (alpha * 180)));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString(text, (int) x + 2, (int) y + 2);

            g2.setColor(new Color(255, 215, 0, (int) (alpha * 255)));
            g2.drawString(text, (int) x, (int) y);
        }
    }
}
