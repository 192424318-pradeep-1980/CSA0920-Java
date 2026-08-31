package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * RegisterFrame.java
 * New player registration screen with validation for username uniqueness,
 * email format, password strength, and password confirmation.
 */
public class RegisterFrame extends JFrame {

    private final AuthenticationManager authManager = new AuthenticationManager();

    private JTextField playerNameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;

    public RegisterFrame() {
        setTitle("Candy Match Saga: Candy AI Lab - Register");
        setSize(520, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel(null) {
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

        JPanel card = createRegisterCard();
        card.setBounds(40, 25, 440, 660);
        background.add(card);
        add(background);
    }

    private JPanel createRegisterCard() {
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

        JLabel titleLabel = new JLabel("NEW SCIENTIST", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 26));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBounds(40, 20, 360, 40);

        JLabel subLabel = new JLabel("Create Your Candy AI Lab Account", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        subLabel.setForeground(new Color(0, 255, 255));
        subLabel.setBounds(40, 58, 360, 22);
        card.add(titleLabel);
        card.add(subLabel);

        int y = 95;
        playerNameField = addLabeledField(card, "Player Name", y);
        y += 68;
        usernameField = addLabeledField(card, "Username", y);
        y += 68;
        emailField = addLabeledField(card, "Email", y);
        y += 68;
        passwordField = addLabeledPasswordField(card, "Password", y);
        y += 68;
        confirmPasswordField = addLabeledPasswordField(card, "Confirm Password", y);
        y += 55;

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setBounds(30, y, 380, 22);
        card.add(statusLabel);
        y += 28;

        JButton registerBtn = LoginFrame.createAuthButton("✅ REGISTER", new Color(46, 139, 87), 15);
        registerBtn.setBounds(50, y, 340, 42);
        registerBtn.addActionListener(e -> performRegister());
        card.add(registerBtn);
        y += 55;

        JButton backBtn = LoginFrame.createAuthButton("← BACK TO LOGIN", new Color(138, 43, 226), 14);
        backBtn.setBounds(50, y, 340, 40);
        backBtn.addActionListener(e -> backToLogin());
        card.add(backBtn);

        return card;
    }

    private JTextField addLabeledField(JPanel card, String label, int y) {
        LoginFrame.addFieldLabel(card, label, 50, y);
        JTextField field = LoginFrame.createStyledField();
        field.setBounds(50, y + 20, 340, 38);
        card.add(field);
        return field;
    }

    private JPasswordField addLabeledPasswordField(JPanel card, String label, int y) {
        LoginFrame.addFieldLabel(card, label, 50, y);
        JPasswordField field = LoginFrame.createStyledPasswordField();
        field.setBounds(50, y + 20, 340, 38);
        card.add(field);
        return field;
    }

    private void performRegister() {
        statusLabel.setText(" ");
        try {
            String error = authManager.register(
                    playerNameField.getText(),
                    usernameField.getText(),
                    emailField.getText(),
                    new String(passwordField.getPassword()),
                    new String(confirmPasswordField.getPassword())
            );

            if (error != null) {
                statusLabel.setText(error);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Registration successful! Please login with your new account.",
                    "Welcome to Candy AI Lab",
                    JOptionPane.INFORMATION_MESSAGE);
            backToLogin();
        } catch (SQLException ex) {
            statusLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private void backToLogin() {
        dispose();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }
}
