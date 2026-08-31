package com.candymatch.ui;

import com.candymatch.custom.CustomCandyManager;
import com.candymatch.custom.CustomRuleManager;
import com.candymatch.custom.ResearchBook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Research Book interface allowing players to search, filter, preview, load, and delete custom candies & rules.
 */
public class ResearchBookUI extends JPanel {

    private final ResearchBook researchBook;
    private final Runnable backCallback;

    private JTextField searchField;
    private DefaultTableModel candyTableModel;
    private DefaultTableModel ruleTableModel;

    public ResearchBookUI(ResearchBook researchBook, Runnable backCallback) {
        this.researchBook = researchBook;
        this.backCallback = backCallback;
        setLayout(new BorderLayout());
        setOpaque(false);

        initUI();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel title = new JLabel("📖 RESEARCH BOOK", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 32));
        title.setForeground(new Color(255, 215, 0));
        headerPanel.add(title, BorderLayout.CENTER);

        JButton backBtn = createStyledButton("⬅ Back", new Color(70, 70, 90));
        backBtn.addActionListener(e -> {
            if (backCallback != null) backCallback.run();
        });
        headerPanel.add(backBtn, BorderLayout.WEST);

        // Search Bar
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBarPanel.setOpaque(false);

        JLabel searchLbl = new JLabel("🔍 Search:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLbl.setForeground(Color.WHITE);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(new Color(40, 35, 65));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.YELLOW);

        JButton searchBtn = createStyledButton("Search", new Color(138, 43, 226));
        searchBtn.addActionListener(e -> refreshTables());

        searchBarPanel.add(searchLbl);
        searchBarPanel.add(searchField);
        searchBarPanel.add(searchBtn);

        headerPanel.add(searchBarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Tabs for Candies and Rules
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("🍬 Custom Candies", buildCandyTablePanel());
        tabs.addTab("📜 Custom Rules", buildRuleTablePanel());

        add(tabs, BorderLayout.CENTER);

        refreshTables();
    }

    private JPanel buildCandyTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        String[] cols = {"Name", "Symbol", "Score Value", "Matching Rule", "Special Ability"};
        candyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(candyTableModel);
        setupTableStyle(table);

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton deleteBtn = createStyledButton("🗑️ Delete Selected Candy", new Color(178, 34, 34));
        deleteBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) {
                String name = (String) candyTableModel.getValueAt(sel, 0);
                researchBook.deleteCandy(name);
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(this, "Select a candy to delete!", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnPanel.add(deleteBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRuleTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        String[] cols = {"Rule Name", "Condition", "Effect"};
        ruleTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(ruleTableModel);
        setupTableStyle(table);

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton deleteBtn = createStyledButton("🗑️ Delete Selected Rule", new Color(178, 34, 34));
        deleteBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) {
                String name = (String) ruleTableModel.getValueAt(sel, 0);
                researchBook.deleteRule(name);
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(this, "Select a rule to delete!", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnPanel.add(deleteBtn);

        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setBackground(new Color(35, 30, 60));
        table.setForeground(Color.WHITE);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    public void refreshTables() {
        String q = searchField != null ? searchField.getText() : "";
        candyTableModel.setRowCount(0);
        ruleTableModel.setRowCount(0);

        List<CustomCandyManager.CustomCandyRecord> candies = researchBook.searchCandies(q);
        for (CustomCandyManager.CustomCandyRecord c : candies) {
            candyTableModel.addRow(new Object[]{c.getName(), c.getSymbol(), c.getScoreValue(), c.getMatchingRule(), c.getSpecialAbility()});
        }

        List<CustomRuleManager.CustomRuleRecord> rules = researchBook.searchRules(q);
        for (CustomRuleManager.CustomRuleRecord r : rules) {
            ruleTableModel.addRow(new Object[]{r.getRuleName(), r.getCondition(), r.getEffect()});
        }
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        return btn;
    }
}
