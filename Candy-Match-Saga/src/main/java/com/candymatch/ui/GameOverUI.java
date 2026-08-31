package com.candymatch.ui;

import com.candymatch.analytics.GameSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Defeat dialog screen displaying end of level statistics and options.
 */
public class GameOverUI extends JDialog {

    public interface GameOverListener {
        void onRetry();
        void onLevelSelect();
        void onMainMenu();
    }

    public GameOverUI(Frame parent, GameSession session, GameOverListener listener) {
        super(parent, "Level Failed", true);
        setSize(480, 420);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 20, 40));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel header = new JLabel("💥 LEVEL FAILED 💥", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI Black", Font.BOLD, 28));
        header.setForeground(new Color(220, 50, 50));
        panel.add(header, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(6, 2, 10, 10));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(20, 10, 20, 10));

        addStatRow(stats, "Player ID:", session.getPlayerID());
        addStatRow(stats, "Level:", String.valueOf(session.getLevel()));
        addStatRow(stats, "Final Score:", String.valueOf(session.getScore()));
        addStatRow(stats, "Player Moves:", String.valueOf(session.getPlayerMoves()));
        addStatRow(stats, "AI Moves:", String.valueOf(session.getAIMoves()));
        addStatRow(stats, "Time Elapsed:", session.getTimeSeconds() + " sec");

        panel.add(stats, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        JButton retryBtn = createStyledButton("🔄 Retry", new Color(255, 140, 0));
        retryBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onRetry();
        });

        JButton lvlBtn = createStyledButton("📋 Levels", new Color(70, 130, 180));
        lvlBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onLevelSelect();
        });

        JButton menuBtn = createStyledButton("🏠 Main Menu", new Color(138, 43, 226));
        menuBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onMainMenu();
        });

        btnPanel.add(retryBtn);
        btnPanel.add(lvlBtn);
        btnPanel.add(menuBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);
        add(panel);
    }

    private void addStatRow(JPanel panel, String label, String val) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(Color.LIGHT_GRAY);

        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 14));
        v.setForeground(Color.WHITE);

        panel.add(l);
        panel.add(v);
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
