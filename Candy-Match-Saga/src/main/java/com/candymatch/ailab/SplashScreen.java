package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * SplashScreen.java
 * Animated Laboratory Splash Screen — displays Candy AI Lab logo for 3 seconds,
 * then automatically navigates to the Login Page.
 */
public class SplashScreen extends JWindow {

    private JProgressBar progressBar;

    public SplashScreen() {
        setSize(600, 360);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Midnight Purple Background
                GradientPaint bgGrad = new GradientPaint(
                        0, 0, new Color(20, 12, 45),
                        getWidth(), getHeight(), new Color(60, 20, 85)
                );
                g2.setPaint(bgGrad);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Glass Rim
                g2.setColor(new Color(255, 215, 0, 180));
                g2.setStroke(new BasicStroke(3.0f));
                g2.draw(new RoundRectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4, 20, 20));
            }
        };

        JLabel iconLabel = new JLabel("🔬", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 64));
        iconLabel.setBounds(100, 40, 400, 70);

        JLabel titleLabel = new JLabel("CANDY AI LAB", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Black", Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBounds(50, 115, 500, 50);

        JLabel subLabel = new JLabel("Initializing Candy Science Engine & Neural Assistant...", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subLabel.setForeground(new Color(220, 200, 255));
        subLabel.setBounds(50, 165, 500, 25);

        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(60, 230, 480, 24);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressBar.setForeground(new Color(138, 43, 226));
        progressBar.setBackground(new Color(20, 15, 35));
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 1, true));

        JLabel statusLabel = new JLabel("Loading AI Modules...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.CYAN);
        statusLabel.setBounds(50, 260, 500, 25);

        panel.add(iconLabel);
        panel.add(titleLabel);
        panel.add(subLabel);
        panel.add(progressBar);
        panel.add(statusLabel);

        add(panel);
    }

    public void startLoading(Runnable onComplete) {
        setVisible(true);
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Display animated logo for 3 seconds
                for (int i = 0; i <= 100; i += 2) {
                    Thread.sleep(60);
                    publish(i);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int val = chunks.get(chunks.size() - 1);
                progressBar.setValue(val);
            }

            @Override
            protected void done() {
                dispose();
                onComplete.run();
            }
        };
        worker.execute();
    }
}
