package com.candymatch.ui;

import com.candymatch.custom.CustomCandyManager;
import com.candymatch.custom.CustomRuleManager;
import com.candymatch.exceptions.InvalidCandyException;
import com.candymatch.exceptions.InvalidRuleException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom Candy and Custom Rule creation form dialog with exception handling.
 */
public class CustomCandyUI extends JDialog {

    private final CustomCandyManager candyManager;
    private final CustomRuleManager ruleManager;
    private final Runnable refreshCallback;

    private JTextField candyNameField;
    private JTextField symbolField;
    private JTextField scoreField;
    private JTextField matchingRuleField;
    private JTextField abilityField;

    private JTextField ruleNameField;
    private JTextField conditionField;
    private JTextField effectField;

    public CustomCandyUI(Frame parent, CustomCandyManager candyManager, CustomRuleManager ruleManager, Runnable refreshCallback) {
        super(parent, "Custom Candy & Rule Designer", true);
        this.candyManager = candyManager;
        this.ruleManager = ruleManager;
        this.refreshCallback = refreshCallback;

        setSize(550, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("🍬 Create Custom Candy", buildCandyForm());
        tabs.addTab("📜 Create Custom Rule", buildRuleForm());

        add(tabs);
    }

    private JPanel buildCandyForm() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(new Color(30, 25, 50));

        JLabel nameLbl = createLabel("Candy Name:");
        candyNameField = createTextField("Super Star");

        JLabel symLbl = createLabel("Emoji/Symbol:");
        symbolField = createTextField("⭐");

        JLabel scoreLbl = createLabel("Score Value:");
        scoreField = createTextField("100");

        JLabel ruleLbl = createLabel("Matching Rule:");
        matchingRuleField = createTextField("Match 3 Any");

        JLabel abilityLbl = createLabel("Special Ability:");
        abilityField = createTextField("Explodes surrounding cells");

        JButton saveBtn = new JButton("💾 Save Custom Candy");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(new Color(46, 139, 87));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveCandy());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setBackground(new Color(178, 34, 34));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> dispose());

        panel.add(nameLbl); panel.add(candyNameField);
        panel.add(symLbl); panel.add(symbolField);
        panel.add(scoreLbl); panel.add(scoreField);
        panel.add(ruleLbl); panel.add(matchingRuleField);
        panel.add(abilityLbl); panel.add(abilityField);
        panel.add(saveBtn); panel.add(cancelBtn);

        return panel;
    }

    private JPanel buildRuleForm() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setBackground(new Color(30, 25, 50));

        JLabel nameLbl = createLabel("Rule Name:");
        ruleNameField = createTextField("Double Combo");

        JLabel condLbl = createLabel("Condition:");
        conditionField = createTextField("Cascade >= 3");

        JLabel effLbl = createLabel("Effect:");
        effectField = createTextField("Multiply score by 2x");

        JButton saveBtn = new JButton("💾 Save Custom Rule");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(new Color(138, 43, 226));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveRule());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setBackground(new Color(178, 34, 34));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> dispose());

        panel.add(nameLbl); panel.add(ruleNameField);
        panel.add(condLbl); panel.add(conditionField);
        panel.add(effLbl); panel.add(effectField);
        panel.add(saveBtn); panel.add(cancelBtn);

        return panel;
    }

    private void saveCandy() {
        String name = candyNameField.getText().trim();
        String symbol = symbolField.getText().trim();
        String scoreStr = scoreField.getText().trim();
        String rule = matchingRuleField.getText().trim();
        String ability = abilityField.getText().trim();

        int scoreVal = 0;
        try {
            scoreVal = Integer.parseInt(scoreStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Score value must be a valid numeric integer!", "Validation Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            CustomCandyManager.CustomCandyRecord rec = new CustomCandyManager.CustomCandyRecord(
                    name, symbol, new Color(255, 105, 180), scoreVal, rule, ability
            );
            candyManager.addCustomCandy(rec);
            JOptionPane.showMessageDialog(this, "✅ Custom Candy '" + name + "' created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (refreshCallback != null) refreshCallback.run();
            dispose();
        } catch (InvalidCandyException e) {
            JOptionPane.showMessageDialog(this, "Invalid Candy Error: " + e.getMessage(), "Custom Candy Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveRule() {
        String name = ruleNameField.getText().trim();
        String cond = conditionField.getText().trim();
        String eff = effectField.getText().trim();

        try {
            CustomRuleManager.CustomRuleRecord rec = new CustomRuleManager.CustomRuleRecord(name, cond, eff);
            ruleManager.addRule(rec);
            JOptionPane.showMessageDialog(this, "✅ Custom Rule '" + name + "' created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (refreshCallback != null) refreshCallback.run();
            dispose();
        } catch (InvalidRuleException e) {
            JOptionPane.showMessageDialog(this, "Invalid Rule Error: " + e.getMessage(), "Custom Rule Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JTextField createTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(50, 45, 75));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.YELLOW);
        return tf;
    }
}
