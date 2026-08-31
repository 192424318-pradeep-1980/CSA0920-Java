package com.candymatch.ui;

import com.candymatch.game.GameManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Level Selection screen displaying all 20 levels divided by difficulty tiers.
 */
public class LevelSelectionUI extends JPanel {

    public interface LevelSelectListener {
        void onLevelSelected(int levelNumber);
        void onBackToMenu();
    }

    private final LevelSelectListener listener;

    public LevelSelectionUI(LevelSelectListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setOpaque(false);

        initUI();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 30, 15, 30));

        JLabel title = new JLabel("SELECT LEVEL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 36));
        title.setForeground(new Color(255, 215, 0));
        headerPanel.add(title, BorderLayout.CENTER);

        JButton backBtn = createStyledButton("⬅ Back", new Color(70, 70, 90));
        backBtn.setPreferredSize(new Dimension(110, 40));
        backBtn.addActionListener(e -> {
            if (listener != null) listener.onBackToMenu();
        });
        headerPanel.add(backBtn, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 20 Level Grid (5 columns x 4 rows)
        JPanel gridPanel = new JPanel(new GridLayout(4, 5, 15, 15));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(10, 40, 30, 40));

        GameManager tempManager = new GameManager();

        for (int i = 1; i <= 20; i++) {
            final int lvl = i;
            GameManager.LevelConfig cfg = tempManager.getCurrentLevelConfig();
            tempManager.startLevel(lvl);
            cfg = tempManager.getCurrentLevelConfig();

            JPanel card = createLevelCard(lvl, cfg);
            gridPanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createLevelCard(int levelNum, GameManager.LevelConfig cfg) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color topColor = new Color(45, 35, 75, 220);
                Color bottomColor = new Color(25, 20, 50, 220);

                if (levelNum <= 5) {
                    topColor = new Color(30, 80, 50, 220);
                } else if (levelNum <= 10) {
                    topColor = new Color(40, 70, 110, 220);
                } else if (levelNum <= 15) {
                    topColor = new Color(110, 60, 30, 220);
                } else {
                    topColor = new Color(110, 30, 60, 220);
                }

                GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 10, 12, 10));

        JLabel numLabel = new JLabel("LEVEL " + levelNum, SwingConstants.CENTER);
        numLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 20));
        numLabel.setForeground(Color.WHITE);
        card.add(numLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);

        JLabel tierLabel = new JLabel(cfg.difficultyTier, SwingConstants.CENTER);
        tierLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tierLabel.setForeground(new Color(255, 235, 150));

        JLabel scoreLabel = new JLabel("Target: " + cfg.targetScore, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scoreLabel.setForeground(Color.LIGHT_GRAY);

        JLabel movesLabel = new JLabel("Moves: " + cfg.moveLimit, SwingConstants.CENTER);
        movesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        movesLabel.setForeground(Color.LIGHT_GRAY);

        infoPanel.add(tierLabel);
        infoPanel.add(scoreLabel);
        infoPanel.add(movesLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        JButton playBtn = createStyledButton("PLAY", new Color(46, 139, 87));
        playBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        playBtn.addActionListener(e -> {
            if (listener != null) listener.onLevelSelected(levelNum);
        });
        card.add(playBtn, BorderLayout.SOUTH);

        return card;
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
}
