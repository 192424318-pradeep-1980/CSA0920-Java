package com.candymatch.ailab;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * AuthUI.java
 * Shared UI components for authentication screens — glassmorphism panels,
 * themed fields, and animated buttons matching the Candy AI Lab design.
 */
public final class AuthUI {

    private AuthUI() {}

    public static final Color BG_TOP = new Color(20, 12, 45);
    public static final Color BG_BOTTOM = new Color(60, 20, 85);
    public static final Color GOLD = new Color(255, 215, 0);
    public static final Color NEON_CYAN = new Color(0, 255, 255);
    public static final Color GLASS_FILL = new Color(20, 15, 40, 210);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 40);

    /** Creates a gradient background panel for auth frames. */
    public static JPanel createBackgroundPanel() {
        return new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint bgGrad = new GradientPaint(
                        0, 0, BG_TOP,
                        getWidth(), getHeight(), BG_BOTTOM
                );
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    /** Glassmorphism card panel with rounded corners. */
    public static JPanel createGlassCard(int width, int height) {
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GLASS_FILL);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.setColor(GLASS_BORDER);
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 28, 28));
            }
        };
        card.setOpaque(false);
        card.setBounds(0, 0, width, height);
        return card;
    }

    public static JLabel createLogoLabel(int y, int width) {
        JLabel icon = new JLabel("🔬", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 48));
        icon.setBounds(0, y, width, 55);

        JLabel title = new JLabel("CANDY AI LAB", SwingConstants.CENTER);
        title.setFont(new Font("Arial Black", Font.BOLD, 28));
        title.setForeground(GOLD);
        title.setBounds(0, y + 55, width, 40);

        JPanel logoPanel = new JPanel(null);
        logoPanel.setOpaque(false);
        logoPanel.setBounds(0, 0, width, y + 100);
        logoPanel.add(icon);
        logoPanel.add(title);
        return logoPanel;
    }

    public static JLabel createFieldLabel(String text, int x, int y, int width) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(220, 200, 255));
        label.setBounds(x, y, width, 22);
        return label;
    }

    public static JTextField createTextField(int x, int y, int width) {
        JTextField field = new JTextField();
        styleField(field, x, y, width, 36);
        return field;
    }

    public static JPasswordField createPasswordField(int x, int y, int width) {
        JPasswordField field = new JPasswordField();
        styleField(field, x, y, width, 36);
        return field;
    }

    private static void styleField(JTextField field, int x, int y, int width, int height) {
        field.setBounds(x, y, width, height);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(30, 22, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(NEON_CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
    }

    public static JButton createAuthButton(String text, Color bg, int x, int y, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isRollover() ? bg.brighter() : bg;
                if (getModel().isPressed()) fill = bg.darker();
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBounds(x, y, width, height);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JLabel createErrorLabel(int x, int y, int width) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(255, 100, 100));
        label.setBounds(x, y, width, 22);
        return label;
    }

    public static void showError(JLabel errorLabel, String message) {
        errorLabel.setText(message != null ? message : "");
    }
}
