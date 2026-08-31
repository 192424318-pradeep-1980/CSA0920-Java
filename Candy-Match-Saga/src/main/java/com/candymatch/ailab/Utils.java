package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;

/**
 * Utils.java
 * Utility helper methods for exception handling, custom dialogs, UI theme styling,
 * and rule input validation in the Candy AI Laboratory.
 */
public class Utils {

    public static class CustomRuleException extends Exception {
        public CustomRuleException(String message) {
            super(message);
        }
    }

    /**
     * Validates player created custom evolution rules.
     * Prevents duplicate inputs, empty names, or invalid outputs.
     */
    public static void validateCustomRule(String ruleId, java.util.List<String> inputs, String resultId, AIAssistant ai) throws CustomRuleException {
        if (resultId == null || resultId.trim().isEmpty()) {
            throw new CustomRuleException("Result Candy name/ID cannot be empty!");
        }

        if (inputs == null || inputs.size() < 2) {
            throw new CustomRuleException("A valid evolution rule requires at least 2 input candies!");
        }

        for (String in : inputs) {
            if (in == null || in.trim().isEmpty()) {
                throw new CustomRuleException("Input candy type cannot be empty!");
            }
        }

        // Prevent Duplicate Rule Check
        CandyRule candidate = new CandyRule(ruleId, inputs, resultId, "Custom Rule", true);
        if (ai.getLearnedRulesSet().contains(candidate)) {
            throw new CustomRuleException("Duplicate Rule Error: An identical candy evolution rule already exists in AI memory!");
        }
    }

    public static JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        return btn;
    }

    public static void showErrorDialog(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
