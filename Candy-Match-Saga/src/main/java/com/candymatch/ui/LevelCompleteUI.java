package com.candymatch.ui;

import com.candymatch.analytics.GameSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Victory dialog screen displaying target achievement metrics and Power BI export status.
 */
public class LevelCompleteUI extends JDialog {

    public interface LevelCompleteListener {
        void onNextLevel();
        void onLevelSelect();
        void onMainMenu();
    }

    public LevelCompleteUI(Frame parent, GameSession session, LevelCompleteListener listener) {
        super(parent, "Level Complete!", true);
        setSize(500, 460);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 25, 45));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel header = new JLabel("🎉 LEVEL COMPLETE! 🎉", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI Black", Font.BOLD, 28));
        header.setForeground(new Color(255, 215, 0));
        panel.add(header, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(7, 2, 10, 8));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(15, 10, 15, 10));

        addStatRow(stats, "Player ID:", session.getPlayerID());
        addStatRow(stats, "Level Cleared:", String.valueOf(session.getLevel()));
        addStatRow(stats, "Final Score:", session.getScore() + " pts");
        addStatRow(stats, "Total Matches:", String.valueOf(session.getTotalMatches()));
        addStatRow(stats, "Max Combo:", session.getComboCount() + "x");
        addStatRow(stats, "Specials Created:", String.valueOf(session.getSpecialCandiesCreated()));
        addStatRow(stats, "Export Telemetry:", "Saved CSV/JSON!");

        panel.add(stats, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        JButton nextBtn = createStyledButton("▶ Next Level", new Color(46, 139, 87));
        nextBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onNextLevel();
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

        btnPanel.add(nextBtn);
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
        v.setForeground(new Color(150, 255, 170));

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
