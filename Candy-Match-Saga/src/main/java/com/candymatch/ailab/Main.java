package com.candymatch.ailab;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main.java
 * Main Application Entry Point for Candy Match Saga: Candy AI Lab.
 * Configures system Look & Feel, initializes Player & AI Assistant models,
 * displays animated SplashScreen, and transitions into MainMenu.
 */
public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.startLoading(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        });
    }
}
