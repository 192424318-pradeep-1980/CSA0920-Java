package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MainMenu.java
 * Main Menu / Home Screen panel with Start Game, Research Book, Settings, and Exit buttons.
 */
public class MainMenu extends JFrame {

    private Player player;
    private AIAssistant aiAssistant;
    private AuthenticationManager authManager;

    public MainMenu(Player player, AIAssistant aiAssistant, AuthenticationManager authManager) {
        this.player = player;
        this.aiAssistant = aiAssistant;
        this.authManager = authManager;

        setTitle("Candy Match Saga: Candy AI Lab - Home");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint bgGrad = new GradientPaint(
                        0, 0, new Color(25, 15, 55),
                        getWidth(), getHeight(), new Color(75, 20, 95)
                );
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JLabel logoLabel = new JLabel("CANDY AI LAB", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial Black", Font.BOLD, 42));
        logoLabel.setForeground(new Color(255, 215, 0));
        logoLabel.setBounds(100, 60, 600, 60);

        JLabel subLabel = new JLabel("Welcome, " + UserSession.getInstance().getPlayerName() + "!", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        subLabel.setForeground(new Color(230, 200, 255));
        subLabel.setBounds(100, 120, 600, 30);

        JPanel cardBox = new JPanel(null) {
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
        cardBox.setOpaque(false);
        cardBox.setBounds(200, 180, 400, 400);

        JButton startBtn = createMenuBtn("🎮 START GAME", new Color(46, 139, 87));
        startBtn.setBounds(40, 30, 320, 55);
        startBtn.addActionListener(e -> {
            dispose();
            GameFrame gameFrame = new GameFrame(player, aiAssistant);
            gameFrame.setVisible(true);
        });
        cardBox.add(startBtn);

        JButton researchBtn = createMenuBtn("📖 RESEARCH BOOK", new Color(138, 43, 226));
        researchBtn.setBounds(40, 100, 320, 55);
        researchBtn.addActionListener(e -> {
            JDialog dlg = new JDialog(this, "Candy Research Book", true);
            dlg.setSize(750, 550);
            dlg.setLocationRelativeTo(this);
            dlg.add(new ResearchBook(player, aiAssistant));
            dlg.setVisible(true);
        });
        cardBox.add(researchBtn);

        JButton settingsBtn = createMenuBtn("⚙️ SETTINGS", new Color(70, 130, 180));
        settingsBtn.setBounds(40, 170, 320, 55);
        settingsBtn.addActionListener(e -> showSettingsDialog());
        cardBox.add(settingsBtn);

        JButton logoutBtn = createMenuBtn("🔓 LOGOUT", new Color(255, 140, 0));
        logoutBtn.setBounds(40, 240, 320, 55);
        logoutBtn.addActionListener(e -> handleLogout());
        cardBox.add(logoutBtn);

        JButton exitBtn = createMenuBtn("🚪 EXIT GAME", new Color(178, 34, 34));
        exitBtn.setBounds(40, 300, 320, 55);
        exitBtn.addActionListener(e -> System.exit(0));
        cardBox.add(exitBtn);

        panel.add(logoLabel);
        panel.add(subLabel);
        panel.add(cardBox);

        add(panel);
    }

    private JButton createMenuBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        return btn;
    }

    private void showSettingsDialog() {
        UserSession session = UserSession.getInstance();
        JOptionPane.showMessageDialog(
                this,
                "⚙️ CANDY AI LAB SETTINGS ⚙️\n\n" +
                        "Database Connection: " + DBConnection.getDatabaseType() + "\n" +
                        "Logged-in User: " + session.getUsername() + "\n" +
                        "Scientist Name: " + session.getPlayerName() + "\n" +
                        "AI Assistant Version: v2.4 (Rule Learning Active)\n",
                "Settings",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthenticationManager.logout();
            dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        }
    }
}
