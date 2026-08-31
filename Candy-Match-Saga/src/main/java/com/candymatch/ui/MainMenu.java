package com.candymatch.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Glassmorphic Main Menu Screen.
 */
public class MainMenu extends JPanel {

    public interface MainMenuListener {
        void onStartGame(String playerName, String mode);
        void onOpenLevelSelect();
        void onOpenResearchBook();
        void onOpenCustomCandyUI();
    }

    private final MainMenuListener listener;
    private JTextField nameInputField;
    private JRadioButton playerOnlyRadio;
    private JRadioButton playerVsAIRadio;

    public MainMenu(MainMenuListener listener) {
        this.listener = listener;
        setLayout(null);
        setOpaque(false);

        initUI();
    }

    private void initUI() {
        JLabel logo = new JLabel("CANDY MATCH SAGA", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Black", Font.BOLD, 42));
        logo.setForeground(new Color(255, 215, 0));
        logo.setBounds(100, 45, 700, 60);
        add(logo);

        JLabel sub = new JLabel("An Intelligent Rule-Based Candy Match Game with Player Analytics", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        sub.setForeground(new Color(220, 200, 255));
        sub.setBounds(100, 105, 700, 30);
        add(sub);

        // Glassmorphic Card Container
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 15, 40, 220));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.setColor(new Color(255, 255, 255, 45));
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 28, 28));
            }
        };
        card.setOpaque(false);
        card.setBounds(220, 155, 460, 480);

        JLabel lblPrompt = new JLabel("Enter Player Name:");
        lblPrompt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrompt.setForeground(Color.WHITE);
        lblPrompt.setBounds(40, 20, 380, 25);
        card.add(lblPrompt);

        nameInputField = new JTextField("Player1");
        nameInputField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameInputField.setForeground(Color.WHITE);
        nameInputField.setBackground(new Color(40, 30, 65));
        nameInputField.setCaretColor(Color.YELLOW);
        nameInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 2, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        nameInputField.setBounds(40, 48, 380, 40);
        card.add(nameInputField);

        JLabel modePrompt = new JLabel("Select Game Mode:");
        modePrompt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modePrompt.setForeground(Color.WHITE);
        modePrompt.setBounds(40, 100, 380, 25);
        card.add(modePrompt);

        playerOnlyRadio = new JRadioButton("👤 Player Only (Normal Mode)", true);
        playerOnlyRadio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        playerOnlyRadio.setForeground(Color.WHITE);
        playerOnlyRadio.setOpaque(false);
        playerOnlyRadio.setBounds(40, 128, 380, 25);

        playerVsAIRadio = new JRadioButton("🤖 Player vs AI Opponent (Turn-Based)", false);
        playerVsAIRadio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        playerVsAIRadio.setForeground(new Color(255, 235, 150));
        playerVsAIRadio.setOpaque(false);
        playerVsAIRadio.setBounds(40, 155, 380, 25);

        ButtonGroup group = new ButtonGroup();
        group.add(playerOnlyRadio);
        group.add(playerVsAIRadio);

        card.add(playerOnlyRadio);
        card.add(playerVsAIRadio);

        JButton startBtn = createStyledButton("🎮 START PLAYING", new Color(46, 139, 87));
        startBtn.setBounds(40, 195, 380, 45);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        startBtn.addActionListener(e -> {
            String name = nameInputField.getText().trim();
            if (name.isEmpty()) name = "Player1";
            String mode = playerVsAIRadio.isSelected() ? "PLAYER_VS_AI" : "PLAYER_ONLY";
            if (listener != null) listener.onStartGame(name, mode);
        });
        card.add(startBtn);

        JButton levelBtn = createStyledButton("📋 SELECT LEVEL (1-20)", new Color(70, 130, 180));
        levelBtn.setBounds(40, 250, 380, 45);
        levelBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        levelBtn.addActionListener(e -> {
            if (listener != null) listener.onOpenLevelSelect();
        });
        card.add(levelBtn);

        JButton bookBtn = createStyledButton("📖 RESEARCH BOOK", new Color(138, 43, 226));
        bookBtn.setBounds(40, 305, 380, 45);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bookBtn.addActionListener(e -> {
            if (listener != null) listener.onOpenResearchBook();
        });
        card.add(bookBtn);

        JButton customBtn = createStyledButton("🍬 CREATE CUSTOM CANDY", new Color(220, 20, 60));
        customBtn.setBounds(40, 360, 380, 45);
        customBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        customBtn.addActionListener(e -> {
            if (listener != null) listener.onOpenCustomCandyUI();
        });
        card.add(customBtn);

        JButton exitBtn = createStyledButton("🚪 EXIT GAME", new Color(80, 80, 90));
        exitBtn.setBounds(40, 415, 380, 45);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        exitBtn.addActionListener(e -> System.exit(0));
        card.add(exitBtn);

        add(card);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bgGrad = new GradientPaint(
                0, 0, new Color(20, 15, 40),
                getWidth(), getHeight(), new Color(55, 25, 85)
        );
        g2.setPaint(bgGrad);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}
