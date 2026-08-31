package com.candymatch.ui;

import com.candymatch.ai.HintGenerator;
import com.candymatch.candy.Candy;
import com.candymatch.exceptions.InvalidMoveException;
import com.candymatch.game.Cell;
import com.candymatch.game.GameBoard;
import com.candymatch.game.GameManager;
import com.candymatch.game.Move;
import com.candymatch.game.TurnManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Interactive Java Swing GUI for playing levels of Candy Match Saga.
 * Displays level HUD, multithreaded timer countdown, moves, target score, hint banner, and handles InvalidMoveException.
 */
public class GameUI extends JPanel {

    public interface GameUIListener {
        void onOpenLevelSelect();
        void onOpenMainMenu();
    }

    private final GameManager gameManager;
    private final GameUIListener listener;

    // Swing HUD Components
    private JLabel levelLabel;
    private JLabel scoreLabel;
    private JLabel targetLabel;
    private JLabel movesLabel;
    private JLabel timerLabel;
    private JLabel turnStatusBanner;
    private JLabel hintMessageLabel;
    private JButton hintButton;

    // Game Board Canvas
    private BoardCanvas canvas;

    // Interaction Selection State
    private Candy selectedCandy = null;
    private Point selectedPoint = null;
    private int dragStartX = -1;
    private int dragStartY = -1;

    // FX Particles & Text Popups
    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    public GameUI(GameManager gameManager, GameUIListener listener) {
        this.gameManager = gameManager;
        this.listener = listener;

        setLayout(new BorderLayout());
        setOpaque(false);

        initUI();

        // 60 FPS Render & Animation Loop Timer (~16ms)
        Timer loopTimer = new Timer(16, e -> updateGameLoop());
        loopTimer.start();
    }

    private void initUI() {
        // Top Dashboard
        JPanel topDashboard = new JPanel(new BorderLayout());
        topDashboard.setOpaque(false);
        topDashboard.setBorder(new EmptyBorder(15, 25, 5, 25));

        // Top Row: Level, Target, Moves, & Multithreaded Timer HUD
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        infoPanel.setOpaque(false);

        levelLabel = createHudBadge("Level 1 (Beginner)", new Color(70, 40, 110));
        scoreLabel = createHudBadge("Score: 0", new Color(138, 43, 226));
        targetLabel = createHudBadge("Target: 1000", new Color(46, 139, 87));
        movesLabel = createHudBadge("Moves: 20", new Color(220, 20, 60));
        timerLabel = createHudBadge("⏳ Time: 180s", new Color(255, 140, 0));

        infoPanel.add(levelLabel);
        infoPanel.add(scoreLabel);
        infoPanel.add(targetLabel);
        infoPanel.add(movesLabel);
        infoPanel.add(timerLabel);

        topDashboard.add(infoPanel, BorderLayout.WEST);

        // Top Right Navigation Buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        navPanel.setOpaque(false);

        JButton restartBtn = createStyledButton("🔄 Restart", new Color(255, 140, 0));
        restartBtn.addActionListener(e -> gameManager.startLevel(gameManager.getCurrentLevelNumber()));

        JButton lvlSelectBtn = createStyledButton("📋 Levels", new Color(70, 130, 180));
        lvlSelectBtn.addActionListener(e -> {
            gameManager.stopTimer();
            if (listener != null) listener.onOpenLevelSelect();
        });

        JButton menuBtn = createStyledButton("🏠 Menu", new Color(110, 60, 130));
        menuBtn.addActionListener(e -> {
            gameManager.stopTimer();
            if (listener != null) listener.onOpenMainMenu();
        });

        navPanel.add(restartBtn);
        navPanel.add(lvlSelectBtn);
        navPanel.add(menuBtn);

        topDashboard.add(navPanel, BorderLayout.EAST);

        // Center Turn Banner & Hint Banner
        JPanel bannerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bannerPanel.setOpaque(false);
        bannerPanel.setBorder(new EmptyBorder(5, 25, 10, 25));

        turnStatusBanner = new JLabel("YOUR TURN", SwingConstants.CENTER);
        turnStatusBanner.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        turnStatusBanner.setForeground(Color.WHITE);
        turnStatusBanner.setOpaque(true);
        turnStatusBanner.setBackground(new Color(46, 139, 87, 220));
        turnStatusBanner.setBorder(BorderFactory.createLineBorder(new Color(150, 255, 170), 2, true));
        turnStatusBanner.setPreferredSize(new Dimension(800, 38));

        hintMessageLabel = new JLabel("Click '💡 Hint' for valid move assistance", SwingConstants.CENTER);
        hintMessageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        hintMessageLabel.setForeground(new Color(255, 235, 150));
        hintMessageLabel.setOpaque(true);
        hintMessageLabel.setBackground(new Color(30, 25, 50, 180));
        hintMessageLabel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0, 120), 1, true));

        bannerPanel.add(turnStatusBanner);
        bannerPanel.add(hintMessageLabel);

        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setOpaque(false);
        northContainer.add(topDashboard, BorderLayout.NORTH);
        northContainer.add(bannerPanel, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);

        // Center Game Board Canvas
        canvas = new BoardCanvas();
        JPanel canvasCenterWrapper = new JPanel(new GridBagLayout());
        canvasCenterWrapper.setOpaque(false);
        canvasCenterWrapper.add(canvas);

        add(canvasCenterWrapper, BorderLayout.CENTER);

        // Bottom Controls (Hint Button)
        JPanel bottomControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomControlPanel.setOpaque(false);

        hintButton = createStyledButton("💡 HINT (Find Move)", new Color(255, 215, 0));
        hintButton.setFont(new Font("Segoe UI Black", Font.BOLD, 16));
        hintButton.setForeground(Color.BLACK);
        hintButton.setPreferredSize(new Dimension(240, 45));
        hintButton.addActionListener(e -> triggerHint());

        bottomControlPanel.add(hintButton);
        add(bottomControlPanel, BorderLayout.SOUTH);

        refreshUI();
    }

    public void enablePlayerInput() {
        gameManager.getTurnManager().setPlayerInputEnabled(true);
        refreshUI();
    }

    public void disablePlayerInput() {
        gameManager.getTurnManager().setPlayerInputEnabled(false);
        refreshUI();
    }

    public void updateTurnLabel() {
        TurnManager tm = gameManager.getTurnManager();
        if (tm.isAITurn()) {
            turnStatusBanner.setText("🤖 AI TURN (Thinking...)");
            turnStatusBanner.setBackground(new Color(220, 100, 20, 230));
            turnStatusBanner.setBorder(BorderFactory.createLineBorder(new Color(255, 180, 50), 2, true));
        } else if (tm.isPlayerTurn()) {
            turnStatusBanner.setText("👤 YOUR TURN");
            turnStatusBanner.setBackground(new Color(46, 139, 87, 230));
            turnStatusBanner.setBorder(BorderFactory.createLineBorder(new Color(150, 255, 170), 2, true));
        } else {
            turnStatusBanner.setText("⚡ PROCESSING...");
            turnStatusBanner.setBackground(new Color(70, 130, 180, 230));
            turnStatusBanner.setBorder(BorderFactory.createLineBorder(new Color(170, 220, 255), 2, true));
        }
    }

    public void highlightHintMove(Move move) {
        if (move == null) return;
        GameBoard board = gameManager.getBoard();
        board.clearAllHighlights();
        Candy c1 = board.getCandy(move.getRow1(), move.getCol1());
        Candy c2 = board.getCandy(move.getRow2(), move.getCol2());
        if (c1 != null) c1.setHintHighlighted(true);
        if (c2 != null) c2.setHintHighlighted(true);
        repaint();
    }

    public void clearHintHighlight() {
        gameManager.getBoard().clearAllHighlights();
        repaint();
    }

    public void refreshBoard() {
        repaint();
    }

    private JLabel createHudBadge(String text, Color bg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        return badge;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        return btn;
    }

    public void refreshUI() {
        GameManager.LevelConfig cfg = gameManager.getCurrentLevelConfig();
        levelLabel.setText(String.format("Level %d (%s)", gameManager.getCurrentLevelNumber(), cfg.difficultyTier));
        scoreLabel.setText("Score: " + gameManager.getCurrentScore());
        targetLabel.setText("Target: " + cfg.targetScore);
        movesLabel.setText("Moves: " + gameManager.getMovesRemaining());
        timerLabel.setText("⏳ Time: " + gameManager.getTimeRemainingSeconds() + "s");

        updateTurnLabel();

        TurnManager tm = gameManager.getTurnManager();
        if (tm.isAITurn()) {
            hintButton.setEnabled(false);
            hintMessageLabel.setText(gameManager.getHintGenerator().getActiveHintMessage().isEmpty() ?
                    "Please wait. AI is playing..." : gameManager.getHintGenerator().getActiveHintMessage());
        } else if (tm.isPlayerTurn()) {
            hintButton.setEnabled(true);
            if (gameManager.getHintGenerator().getActiveHintMessage().isEmpty()) {
                hintMessageLabel.setText("Swap two adjacent candies to create a match of 3 or more!");
            } else {
                hintMessageLabel.setText(gameManager.getHintGenerator().getActiveHintMessage());
            }
        } else {
            hintButton.setEnabled(false);
        }

        repaint();
    }

    private void triggerHint() {
        HintGenerator.HintInfo hint = gameManager.requestHint();
        if (hint != null) {
            hintMessageLabel.setText(hint.getMessage());
            highlightHintMove(hint.getMove());
        }
        refreshUI();
    }

    private void updateGameLoop() {
        GameBoard board = gameManager.getBoard();
        if (board != null) {
            for (int r = 0; r < GameBoard.ROWS; r++) {
                for (int c = 0; c < GameBoard.COLS; c++) {
                    Candy candy = board.getCandy(r, c);
                    if (candy != null) {
                        candy.updateAnimation(0.25);
                    }
                }
            }
        }

        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            if (!pIt.next().update()) pIt.remove();
        }

        Iterator<FloatingText> tIt = floatingTexts.iterator();
        while (tIt.hasNext()) {
            if (!tIt.next().update()) tIt.remove();
        }

        canvas.repaint();
    }

    // --------------------------------------------------------
    // Board Canvas Painting & Drag/Click Interactions with Exception Handling
    // --------------------------------------------------------
    private class BoardCanvas extends JPanel {
        private final int cellSize = 64;

        public BoardCanvas() {
            int w = GameBoard.COLS * cellSize;
            int h = GameBoard.ROWS * cellSize;
            setPreferredSize(new Dimension(w, h));
            setOpaque(false);

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    TurnManager tm = gameManager.getTurnManager();
                    if (!tm.isPlayerTurn() || !tm.isPlayerInputEnabled()) {
                        return;
                    }

                    int c = e.getX() / cellSize;
                    int r = e.getY() / cellSize;

                    if (!GameBoard.isValidPosition(r, c)) return;

                    dragStartX = e.getX();
                    dragStartY = e.getY();

                    GameBoard board = gameManager.getBoard();
                    Candy clicked = board.getCandy(r, c);

                    if (selectedCandy == null) {
                        selectedPoint = new Point(c, r);
                        selectedCandy = clicked;
                        if (selectedCandy != null) selectedCandy.setSelected(true);
                    } else {
                        if (selectedCandy == clicked) {
                            selectedCandy.setSelected(false);
                            selectedCandy = null;
                            selectedPoint = null;
                        } else if (GameBoard.areAdjacent(selectedPoint.y, selectedPoint.x, r, c)) {
                            int r1 = selectedPoint.y;
                            int c1 = selectedPoint.x;
                            int r2 = r;
                            int c2 = c;

                            selectedCandy.setSelected(false);
                            selectedCandy = null;
                            selectedPoint = null;

                            // Exception Handling: Catch InvalidMoveException
                            try {
                                gameManager.attemptPlayerSwap(r1, c1, r2, c2);
                            } catch (InvalidMoveException ex) {
                                hintMessageLabel.setText("⚠️ Move Error: " + ex.getMessage());
                            }
                        } else {
                            selectedCandy.setSelected(false);
                            selectedPoint = new Point(c, r);
                            selectedCandy = clicked;
                            if (selectedCandy != null) selectedCandy.setSelected(true);
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    TurnManager tm = gameManager.getTurnManager();
                    if (!tm.isPlayerTurn() || !tm.isPlayerInputEnabled() || selectedCandy == null || dragStartX == -1) {
                        return;
                    }

                    int dx = e.getX() - dragStartX;
                    int dy = e.getY() - dragStartY;
                    int threshold = cellSize / 3;

                    if (Math.abs(dx) > threshold || Math.abs(dy) > threshold) {
                        int tr = selectedPoint.y;
                        int tc = selectedPoint.x;

                        if (Math.abs(dx) > Math.abs(dy)) {
                            tc += (dx > 0) ? 1 : -1;
                        } else {
                            tr += (dy > 0) ? 1 : -1;
                        }

                        if (GameBoard.isValidPosition(tr, tc) && GameBoard.areAdjacent(selectedPoint.y, selectedPoint.x, tr, tc)) {
                            int r1 = selectedPoint.y;
                            int c1 = selectedPoint.x;

                            selectedCandy.setSelected(false);
                            selectedCandy = null;
                            selectedPoint = null;

                            // Exception Handling: Catch InvalidMoveException
                            try {
                                gameManager.attemptPlayerSwap(r1, c1, tr, tc);
                            } catch (InvalidMoveException ex) {
                                hintMessageLabel.setText("⚠️ Move Error: " + ex.getMessage());
                            }
                        }
                        dragStartX = -1;
                        dragStartY = -1;
                    }
                }
            };

            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = GameBoard.COLS * cellSize;
            int h = GameBoard.ROWS * cellSize;

            // Outer Frame
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 26, 26));
            g2.setColor(new Color(35, 25, 60, 220));
            g2.fill(new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 22, 22));

            // Grid Cells
            GameBoard board = gameManager.getBoard();
            for (int r = 0; r < GameBoard.ROWS; r++) {
                for (int c = 0; c < GameBoard.COLS; c++) {
                    int cx = c * cellSize;
                    int cy = r * cellSize;
                    if ((r + c) % 2 == 0) {
                        g2.setColor(new Color(255, 255, 255, 14));
                    } else {
                        g2.setColor(new Color(0, 0, 0, 30));
                    }
                    g2.fillRoundRect(cx + 2, cy + 2, cellSize - 4, cellSize - 4, 12, 12);
                }
            }

            // Draw Candies
            if (board != null) {
                for (int r = 0; r < GameBoard.ROWS; r++) {
                    for (int c = 0; c < GameBoard.COLS; c++) {
                        Candy candy = board.getCandy(r, c);
                        if (candy != null) {
                            candy.draw(g2, 0, 0, cellSize);
                        }
                    }
                }
            }

            for (Particle p : particles) p.draw(g2);
            for (FloatingText ft : floatingTexts) ft.draw(g2);
        }
    }

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
            g2.setFont(new Font("Segoe UI Black", Font.BOLD, 18));
            g2.drawString(text, (int) x + 2, (int) y + 2);

            g2.setColor(new Color(255, 215, 0, (int) (alpha * 255)));
            g2.drawString(text, (int) x, (int) y);
        }
    }
}
