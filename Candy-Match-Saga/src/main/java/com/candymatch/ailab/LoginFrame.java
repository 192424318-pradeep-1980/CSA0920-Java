package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * LoginFrame.java
 * Professional glassmorphism login screen for Candy AI Lab.
 * Supports remember-me, show/hide password, and navigation to Register / Forgot Password.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationManager authManager = new AuthenticationManager();
    private final Database database = new Database();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeBox;
    private JButton showPasswordBtn;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Candy Match Saga: Candy AI Lab - Login");
        setSize(520, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = createBackgroundPanel();
        JPanel loginCard = createLoginCard();
        loginCard.setBounds(40, 30, 440, 600);
        background.add(loginCard);
        add(background);

        loadRememberedUsername();
    }

    private JPanel createBackgroundPanel() {
        return new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint grad = new GradientPaint(
                        0, 0, new Color(25, 15, 55),
                        getWidth(), getHeight(), new Color(75, 20, 95)
                );
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 15, 40, 210));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g2.setColor(new Color(0, 255, 255, 60));
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 28, 28));
            }
        };
        card.setOpaque(false);

        JLabel iconLabel = new JLabel("🔬", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 48));
        iconLabel.setBounds(170, 20, 100, 55);

        JLabel titleLabel = new JLabel("CANDY AI LAB", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBounds(40, 75, 360, 40);

        JLabel subLabel = new JLabel("Scientist Login Portal", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subLabel.setForeground(new Color(0, 255, 255));
        subLabel.setBounds(40, 115, 360, 22);

        usernameField = createStyledField();
        usernameField.setBounds(50, 160, 340, 38);
        addFieldLabel(card, "Username", 50, 142);

        passwordField = createStyledPasswordField();
        passwordField.setBounds(50, 230, 280, 38);
        addFieldLabel(card, "Password", 50, 212);

        showPasswordBtn = createAuthButton("👁", new Color(70, 130, 180), 14);
        showPasswordBtn.setBounds(340, 230, 50, 38);
        showPasswordBtn.setToolTipText("Show / Hide Password");
        showPasswordBtn.addActionListener(e -> togglePasswordVisibility());
        card.add(showPasswordBtn);

        rememberMeBox = new JCheckBox("Remember Me");
        rememberMeBox.setBounds(50, 280, 200, 28);
        rememberMeBox.setOpaque(false);
        rememberMeBox.setForeground(new Color(220, 200, 255));
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rememberMeBox.setFocusPainted(false);
        card.add(rememberMeBox);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setBounds(30, 310, 380, 22);
        card.add(statusLabel);

        JButton loginBtn = createAuthButton("🔐 LOGIN", new Color(138, 43, 226), 16);
        loginBtn.setBounds(50, 345, 340, 45);
        loginBtn.addActionListener(e -> performLogin());
        card.add(loginBtn);

        JButton registerBtn = createAuthButton("📝 REGISTER", new Color(46, 139, 87), 14);
        registerBtn.setBounds(50, 405, 160, 40);
        registerBtn.addActionListener(e -> openRegister());
        card.add(registerBtn);

        JButton forgotBtn = createAuthButton("🔑 FORGOT PASSWORD", new Color(70, 130, 180), 13);
        forgotBtn.setBounds(220, 405, 170, 40);
        forgotBtn.addActionListener(e -> openForgotPassword());
        card.add(forgotBtn);

        JButton exitBtn = createAuthButton("🚪 EXIT", new Color(178, 34, 34), 14);
        exitBtn.setBounds(50, 460, 340, 40);
        exitBtn.addActionListener(e -> System.exit(0));
        card.add(exitBtn);

        card.add(iconLabel);
        card.add(titleLabel);
        card.add(subLabel);
        card.add(usernameField);
        card.add(passwordField);

        passwordField.addActionListener(e -> performLogin());

        return card;
    }

    private void loadRememberedUsername() {
        String remembered = AuthenticationManager.loadRememberedUsername();
        if (remembered != null && !remembered.isEmpty()) {
            usernameField.setText(remembered);
            rememberMeBox.setSelected(true);
        }
    }

    private void togglePasswordVisibility() {
        if (passwordField.getEchoChar() != 0) {
            passwordField.setEchoChar((char) 0);
            showPasswordBtn.setText("🙈");
        } else {
            passwordField.setEchoChar('•');
            showPasswordBtn.setText("👁");
        }
    }

    private void performLogin() {
        statusLabel.setText(" ");
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();
        boolean remember = rememberMeBox.isSelected();

        try {
            String error = authManager.login(username, new String(password), remember);
            if (error != null) {
                statusLabel.setText(error);
                return;
            }

            UserSession session = UserSession.getInstance();
            Player player = new Player(session.getPlayerName());
            AIAssistant aiAssistant = new AIAssistant();

            database.loadUserProgress(session.getUserId(), player, aiAssistant);

            dispose();
            MainMenu homeMenu = new MainMenu(player, aiAssistant);
            homeMenu.setVisible(true);
        } catch (SQLException ex) {
            statusLabel.setText("Database error: " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void openRegister() {
        dispose();
        RegisterFrame registerFrame = new RegisterFrame();
        registerFrame.setVisible(true);
    }

    private void openForgotPassword() {
        dispose();
        ForgotPasswordFrame forgotFrame = new ForgotPasswordFrame();
        forgotFrame.setVisible(true);
    }

    static JTextField createStyledField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(30, 25, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setEchoChar('•');
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(30, 25, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    static JButton createAuthButton(String text, Color bg, int fontSize) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        return btn;
    }

    static void addFieldLabel(JPanel parent, String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(200, 180, 255));
        label.setBounds(x, y, 200, 18);
        parent.add(label);
    }
}
