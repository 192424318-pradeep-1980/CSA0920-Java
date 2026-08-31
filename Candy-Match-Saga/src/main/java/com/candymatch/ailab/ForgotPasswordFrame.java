package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * ForgotPasswordFrame.java
 * Password reset screen requiring matching username and registered email.
 */
public class ForgotPasswordFrame extends JFrame {

    private final AuthenticationManager authManager = new AuthenticationManager();

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;

    public ForgotPasswordFrame() {
        setTitle("Candy Match Saga: Candy AI Lab - Reset Password");
        setSize(520, 620);
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

        JPanel card = createResetCard();
        card.setBounds(40, 30, 440, 550);
        background.add(card);
        add(background);
    }

    private JPanel createResetCard() {
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

        JLabel titleLabel = new JLabel("RESET PASSWORD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBounds(40, 25, 360, 38);
        card.add(titleLabel);

        JLabel subLabel = new JLabel("Verify your account to set a new password", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        subLabel.setForeground(new Color(0, 255, 255));
        subLabel.setBounds(40, 62, 360, 22);
        card.add(subLabel);

        int y = 100;
        usernameField = addLabeledField(card, "Username", y);
        y += 68;
        emailField = addLabeledField(card, "Registered Email", y);
        y += 68;
        newPasswordField = addLabeledPasswordField(card, "New Password", y);
        y += 68;
        confirmPasswordField = addLabeledPasswordField(card, "Confirm Password", y);
        y += 55;

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setBounds(30, y, 380, 22);
        card.add(statusLabel);
        y += 28;

        JButton resetBtn = LoginFrame.createAuthButton("🔑 RESET PASSWORD", new Color(70, 130, 180), 15);
        resetBtn.setBounds(50, y, 340, 42);
        resetBtn.addActionListener(e -> performReset());
        card.add(resetBtn);
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

    private void performReset() {
        statusLabel.setText(" ");
        try {
            String error = authManager.resetPassword(
                    usernameField.getText(),
                    emailField.getText(),
                    new String(newPasswordField.getPassword()),
                    new String(confirmPasswordField.getPassword())
            );

            if (error != null) {
                statusLabel.setText(error);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Password reset successful! Please login with your new password.",
                    "Password Updated",
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
