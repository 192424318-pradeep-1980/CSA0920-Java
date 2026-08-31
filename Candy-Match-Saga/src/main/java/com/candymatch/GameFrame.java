package com.candymatch;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * GameFrame.java
 * Interactive Java Swing GUI interface for Candy Match Saga.
 * Controls game loop, mouse click/drag swapping, 2D grid rendering, score display (10 pts/candy + 50 bomb bonus),
 * moves counter (starts at 20), game over modal, and JDBC high scores leaderboard.
 */
public class GameFrame extends JFrame {

    public static final int STARTING_MOVES = 20;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private Board board;
    private Database database;

    private String playerName = "CandyCrusher";
    private int score = 0;
    private int movesRemaining = STARTING_MOVES;
    private int movesUsed = 0;

    // Swing GUI Labels
    private JLabel scoreLabel;
    private JLabel movesLabel;
    private JLabel playerLabel;
    private JLabel dbStatusLabel;
    private JTextField nameInputField;

    // Game Canvas
    private GameCanvas canvas;
    private String gameState = "IDLE"; // IDLE, SWAPPING, REVERTING, MATCHING, FALLING, GAME_OVER

    // Interaction selection
    private Candy selectedCandy = null;
    private Point selectedPoint = null;
    private int dragStartX = -1;
    private int dragStartY = -1;

    // Particles & Text popups
    private List<Particle> particles = new ArrayList<>();
    private List<FloatingText> floatingTexts = new ArrayList<>();

    public GameFrame() {
        setTitle("Candy Match Saga - Java Swing");
        setSize(900, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        board = new Board();
        database = new Database();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Screens
        mainContainer.add(buildWelcomeScreen(), "WELCOME");
        
        canvas = new GameCanvas();
        mainContainer.add(buildGameScreen(), "GAME");

        add(mainContainer);

        // 60 FPS Render Timer (~16ms)
        javax.swing.Timer timer = new javax.swing.Timer(16, e -> gameLoopStep());
        timer.start();
    }

    private JPanel buildWelcomeScreen() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint bgGrad = new GradientPaint(
                        0, 0, new Color(25, 15, 50),
                        getWidth(), getHeight(), new Color(70, 20, 90)
                );
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JLabel logo = new JLabel("CANDY MATCH SAGA", SwingConstants.CENTER);
        logo.setFont(new Font("Arial Black", Font.BOLD, 40));
        logo.setForeground(new Color(255, 215, 0));
        logo.setBounds(100, 70, 700, 60);

        JLabel sub = new JLabel("Match 3+ Candies & Blast Bomb Power-Ups!", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        sub.setForeground(new Color(230, 200, 255));
        sub.setBounds(100, 130, 700, 30);

        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 15, 40, 210));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 28, 28));
            }
        };
        card.setOpaque(false);
        card.setBounds(230, 200, 440, 380);

        JLabel lblPrompt = new JLabel("Enter Player Name:");
        lblPrompt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPrompt.setForeground(Color.WHITE);
        lblPrompt.setBounds(50, 30, 340, 25);
        card.add(lblPrompt);

        nameInputField = new JTextField("CandyCrusher");
        nameInputField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameInputField.setForeground(Color.WHITE);
        nameInputField.setBackground(new Color(40, 30, 65));
        nameInputField.setCaretColor(Color.YELLOW);
        nameInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 2, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        nameInputField.setBounds(50, 60, 340, 45);
        card.add(nameInputField);

        JButton startBtn = createStyledButton("🎮 START GAME", new Color(46, 139, 87));
        startBtn.setBounds(50, 125, 340, 50);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startBtn.addActionListener(e -> {
            String input = nameInputField.getText();
            if (input != null && !input.trim().isEmpty()) {
                playerName = input.trim();
            }
            restartGame();
            cardLayout.show(mainContainer, "GAME");
        });
        card.add(startBtn);

        JButton leaderBtn = createStyledButton("🏆 HIGH SCORES", new Color(138, 43, 226));
        leaderBtn.setBounds(50, 195, 340, 50);
        leaderBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        leaderBtn.addActionListener(e -> showLeaderboardDialog());
        card.add(leaderBtn);

        JButton exitBtn = createStyledButton("🚪 EXIT GAME", new Color(178, 34, 34));
        exitBtn.setBounds(50, 265, 340, 50);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        exitBtn.addActionListener(e -> System.exit(0));
        card.add(exitBtn);

        panel.add(logo);
        panel.add(sub);
        panel.add(card);

        return panel;
    }

    private JPanel buildGameScreen() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint bgGrad = new GradientPaint(
                        0, 0, new Color(20, 15, 38),
                        getWidth(), getHeight(), new Color(45, 25, 75)
                );
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Top Dashboard
        JPanel dash = new JPanel(null);
        dash.setBounds(30, 15, 840, 70);
        dash.setOpaque(false);

        playerLabel = new JLabel("👤 Player: " + playerName);
        playerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        playerLabel.setForeground(new Color(255, 235, 150));
        playerLabel.setBounds(10, 8, 220, 25);
        dash.add(playerLabel);

        dbStatusLabel = new JLabel("⚡ " + DBConnection.getDatabaseType());
        dbStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbStatusLabel.setForeground(new Color(170, 220, 255));
        dbStatusLabel.setBounds(10, 35, 240, 25);
        dash.add(dbStatusLabel);

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(new Color(138, 43, 226, 180));
        scoreLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 160, 255), 2, true));
        scoreLabel.setBounds(270, 15, 170, 40);
        dash.add(scoreLabel);

        movesLabel = new JLabel("Moves: " + STARTING_MOVES, SwingConstants.CENTER);
        movesLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        movesLabel.setForeground(Color.WHITE);
        movesLabel.setOpaque(true);
        movesLabel.setBackground(new Color(220, 20, 60, 180));
        movesLabel.setBorder(BorderFactory.createLineBorder(new Color(255, 120, 150), 2, true));
        movesLabel.setBounds(455, 15, 150, 40);
        dash.add(movesLabel);

        JButton restartBtn = createStyledButton("Restart", new Color(255, 140, 0));
        restartBtn.setBounds(620, 15, 95, 40);
        restartBtn.addActionListener(e -> restartGame());
        dash.add(restartBtn);

        JButton menuBtn = createStyledButton("Menu", new Color(70, 130, 180));
        menuBtn.setBounds(725, 15, 95, 40);
        menuBtn.addActionListener(e -> cardLayout.show(mainContainer, "WELCOME"));
        dash.add(menuBtn);

        panel.add(dash);

        // Center Board Canvas
        canvas.setBounds(190, 100, 520, 520);
        panel.add(canvas);

        // Bottom Footer Leaderboard Button
        JButton leaderboardBtn = createStyledButton("🏆 High Scores", new Color(46, 139, 87));
        leaderboardBtn.setBounds(360, 632, 180, 40);
        leaderboardBtn.addActionListener(e -> showLeaderboardDialog());
        panel.add(leaderboardBtn);

        return panel;
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

    public void restartGame() {
        score = 0;
        movesRemaining = STARTING_MOVES;
        movesUsed = 0;
        gameState = "IDLE";
        selectedCandy = null;
        selectedPoint = null;
        particles.clear();
        floatingTexts.clear();
        board.generateWithoutInitialMatches();
        updateDashboard();
        canvas.repaint();
    }

    public void updateDashboard() {
        playerLabel.setText("👤 Player: " + playerName);
        scoreLabel.setText("Score: " + score);
        movesLabel.setText("Moves: " + movesRemaining);
        dbStatusLabel.setText("⚡ " + DBConnection.getDatabaseType());
    }

    // ----------------------------------------------------
    // Mouse Interaction Handling (Click & Drag Swap)
    // ----------------------------------------------------
    private class GameCanvas extends JPanel {
        private int cellSize = 64;

        public GameCanvas() {
            setOpaque(false);

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!"IDLE".equals(gameState) || movesRemaining <= 0) return;

                    int c = e.getX() / cellSize;
                    int r = e.getY() / cellSize;

                    if (!board.isValidPosition(r, c)) return;

                    dragStartX = e.getX();
                    dragStartY = e.getY();

                    if (selectedCandy == null) {
                        selectedPoint = new Point(c, r);
                        selectedCandy = board.getCandy(r, c);
                        if (selectedCandy != null) selectedCandy.setSelected(true);
                    } else {
                        Candy clicked = board.getCandy(r, c);
                        if (selectedCandy == clicked) {
                            selectedCandy.setSelected(false);
                            selectedCandy = null;
                            selectedPoint = null;
                        } else if (board.areAdjacent(selectedPoint.y, selectedPoint.x, r, c)) {
                            initiateSwap(selectedPoint, new Point(c, r));
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
                    if (!"IDLE".equals(gameState) || selectedCandy == null || dragStartX == -1) return;

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

                        if (board.isValidPosition(tr, tc)) {
                            initiateSwap(selectedPoint, new Point(tc, tr));
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

            int w = Board.COLS * cellSize;
            int h = Board.ROWS * cellSize;

            // Glass Container Outer Background
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 24, 24));
            g2.setColor(new Color(40, 30, 70, 220));
            g2.fill(new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 20, 20));

            // Checkerboard Grid Cells
            for (int r = 0; r < Board.ROWS; r++) {
                for (int c = 0; c < Board.COLS; c++) {
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
            for (int r = 0; r < Board.ROWS; r++) {
                for (int c = 0; c < Board.COLS; c++) {
                    Candy candy = board.getCandy(r, c);
                    if (candy != null) {
                        candy.draw(g2, 0, 0, cellSize);
                    }
                }
            }

            // Draw Particles
            for (Particle p : particles) {
                p.draw(g2);
            }

            // Draw Floating Score Text Popups
            for (FloatingText ft : floatingTexts) {
                ft.draw(g2);
            }
        }
    }

    private void initiateSwap(Point p1, Point p2) {
        if (selectedCandy != null) {
            selectedCandy.setSelected(false);
            selectedCandy = null;
            selectedPoint = null;
        }

        board.swap(p1.y, p1.x, p2.y, p2.x);
        gameState = "SWAPPING";

        javax.swing.Timer timer = new javax.swing.Timer(250, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            Board.MatchResult result = board.findMatches(0);

            if (result.hasMatches()) {
                // Valid swap!
                movesRemaining--;
                movesUsed++;
                updateDashboard();
                processMatches(result, 0);
            } else {
                // Revert invalid swap
                board.swap(p1.y, p1.x, p2.y, p2.x);
                gameState = "REVERTING";
                javax.swing.Timer revertTimer = new javax.swing.Timer(250, e2 -> {
                    ((javax.swing.Timer) e2.getSource()).stop();
                    gameState = "IDLE";
                });
                revertTimer.start();
            }
        });
        timer.start();
    }

    private void processMatches(Board.MatchResult result, int cascadeLevel) {
        gameState = "MATCHING";
        score += result.scoreGained;
        updateDashboard();

        // Spawn particles & text popups for Bomb Candy explosions (+50 bonus)
        for (Point bombCenter : result.bombCenters) {
            spawnBombExplosion(bombCenter);
            spawnFloatingText("💣 BOMB +50!", bombCenter.x, bombCenter.y);
        }

        if (result.scoreGained > 0 && result.bombCenters.isEmpty()) {
            Set<Point> pts = result.getAllPointsToRemove();
            if (!pts.isEmpty()) {
                Point firstPt = pts.iterator().next();
                spawnFloatingText("+" + result.scoreGained, firstPt.x, firstPt.y);
            }
        }

        // Animate pop scale down
        Set<Point> removePoints = result.getAllPointsToRemove();
        for (Point pt : removePoints) {
            Candy c = board.getCandy(pt.y, pt.x);
            if (c != null) {
                c.setScale(0.0);
                c.setAlpha(0.0f);
            }
        }

        javax.swing.Timer matchTimer = new javax.swing.Timer(300, e -> {
            ((javax.swing.Timer) e.getSource()).stop();

            board.removeCandies(removePoints);
            board.applyGravity();
            board.refillEmptyCells();

            gameState = "FALLING";

            javax.swing.Timer fallTimer = new javax.swing.Timer(300, e2 -> {
                ((javax.swing.Timer) e2.getSource()).stop();

                Board.MatchResult cascadeResult = board.findMatches(cascadeLevel + 1);
                if (cascadeResult.hasMatches()) {
                    processMatches(cascadeResult, cascadeLevel + 1);
                } else {
                    gameState = "IDLE";
                    if (movesRemaining <= 0) {
                        handleGameOver();
                    }
                }
            });
            fallTimer.start();
        });
        matchTimer.start();
    }

    private void spawnBombExplosion(Point center) {
        int px = center.x * 64 + 32;
        int py = center.y * 64 + 32;
        Random rand = new Random();
        for (int i = 0; i < 35; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = 2.0 + rand.nextDouble() * 7.0;
            Color pColor = rand.nextBoolean() ? new Color(255, 215, 0) : new Color(255, 60, 0);
            particles.add(new Particle(px, py, Math.cos(angle) * speed, Math.sin(angle) * speed, pColor, 25));
        }
    }

    private void spawnFloatingText(String text, int gridX, int gridY) {
        int px = gridX * 64 + 16;
        int py = gridY * 64 + 32;
        floatingTexts.add(new FloatingText(text, px, py));
    }

    private void gameLoopStep() {
        // Update position interpolations
        double speed = 0.22;
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Candy candy = board.getCandy(r, c);
                if (candy != null) candy.updateAnimation(speed);
            }
        }

        // Update particles
        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            if (!pIt.next().update()) pIt.remove();
        }

        // Update floating texts
        Iterator<FloatingText> tIt = floatingTexts.iterator();
        while (tIt.hasNext()) {
            if (!tIt.next().update()) tIt.remove();
        }

        canvas.repaint();
    }

    private void handleGameOver() {
        gameState = "GAME_OVER";
        boolean saved = database.saveScore(playerName, score, movesUsed);

        String saveMsg = saved ?
                "✅ Score successfully saved to database table player_score!" :
                "⚠️ Score recorded locally!";

        Object[] options = {"Play Again", "View High Scores", "Main Menu"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "🎉 GAME OVER! 🎉\n\n" +
                        "Player: " + playerName + "\n" +
                        "Final Score: " + score + " pts\n" +
                        "Moves Used: " + movesUsed + "\n\n" +
                        saveMsg,
                "Candy Match Saga - Game Over",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else if (choice == JOptionPane.NO_OPTION) {
            showLeaderboardDialog();
        } else {
            cardLayout.show(mainContainer, "WELCOME");
        }
    }

    private void showLeaderboardDialog() {
        JDialog dlg = new JDialog(this, "Leaderboard - player_score", true);
        dlg.setSize(640, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 20, 45));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("🏆 HIGH SCORES LEADERBOARD 🏆", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(255, 215, 0));
        header.add(title, BorderLayout.CENTER);

        dlg.add(header, BorderLayout.NORTH);

        String[] cols = {"Rank", "Player Name", "Score", "Moves Used", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setBackground(new Color(35, 30, 60));
        table.setForeground(Color.WHITE);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        List<Database.PlayerRecord> records = database.getTopScores(20);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int rank = 1;
        for (Database.PlayerRecord r : records) {
            model.addRow(new Object[]{
                    "#" + rank,
                    r.playerName,
                    r.score + " pts",
                    r.moves,
                    (r.date != null) ? sdf.format(r.date) : "N/A"
            });
            rank++;
        }

        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(25, 20, 45));
        JButton closeBtn = createStyledButton("Close", new Color(178, 34, 34));
        closeBtn.addActionListener(e -> dlg.dispose());
        btnPanel.add(closeBtn);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // Particle & Floating Text helpers
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
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.drawString(text, (int) x + 2, (int) y + 2);

            g2.setColor(new Color(255, 215, 0, (int) (alpha * 255)));
            g2.drawString(text, (int) x, (int) y);
        }
    }
}
