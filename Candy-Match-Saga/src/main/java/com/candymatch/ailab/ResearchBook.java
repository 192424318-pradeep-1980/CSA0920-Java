package com.candymatch.ailab;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.LinkedHashSet;

import java.util.Set;

/**
 * ResearchBook.java
 * Candy Research Book GUI panel displaying:
 * - Discovered Candies (standard & dynamic custom player candies)
 * - Locked Candies
 * - Candy Evolution Tree
 * - Detailed description of each candy
 * Automatically refreshes whenever a new candy is discovered or created.
 */
public class ResearchBook extends JPanel {

    private Player player;
    private AIAssistant aiAssistant;

    private JPanel discoveredPanel;
    private JPanel evolutionTreePanel;
    private JPanel aiMemoryPanel;
    private JTextArea descriptionArea;

    public ResearchBook(Player player, AIAssistant aiAssistant) {
        this.player = player;
        this.aiAssistant = aiAssistant;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(25, 20, 45));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Title
        JLabel headerLabel = new JLabel("📖 CANDY RESEARCH BOOK 🔬", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(new Color(255, 215, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Tabbed Pane for Discoveries vs Evolution Tree vs AI Memory
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(35, 30, 60));
        tabbedPane.setForeground(Color.WHITE);

        // Tab 1: Discovered / Locked Candies
        discoveredPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        discoveredPanel.setBackground(new Color(30, 25, 55));
        JScrollPane scroll1 = new JScrollPane(discoveredPanel);
        scroll1.getViewport().setBackground(new Color(30, 25, 55));
        tabbedPane.addTab("Discovered Candies", scroll1);

        // Tab 2: Evolution Tree Graph
        evolutionTreePanel = new JPanel();
        evolutionTreePanel.setLayout(new BoxLayout(evolutionTreePanel, BoxLayout.Y_AXIS));
        evolutionTreePanel.setBackground(new Color(30, 25, 55));
        JScrollPane scroll2 = new JScrollPane(evolutionTreePanel);
        scroll2.getViewport().setBackground(new Color(30, 25, 55));
        tabbedPane.addTab("Evolution Tree", scroll2);

        // Tab 3: AI Memory System (HashMap backed)
        aiMemoryPanel = new JPanel();
        aiMemoryPanel.setLayout(new BoxLayout(aiMemoryPanel, BoxLayout.Y_AXIS));
        aiMemoryPanel.setBackground(new Color(30, 25, 55));
        JScrollPane scroll3 = new JScrollPane(aiMemoryPanel);
        scroll3.getViewport().setBackground(new Color(30, 25, 55));
        tabbedPane.addTab("🧠 AI Memory", scroll3);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Description Box
        descriptionArea = new JTextArea(4, 40);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setForeground(Color.CYAN);
        descriptionArea.setBackground(new Color(15, 10, 30));
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 1),
                "Candy Encyclopedia Description",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
        ));

        add(descriptionArea, BorderLayout.SOUTH);

        refreshBook();
    }

    /**
     * Refreshes Research Book UI.
     */
    public void refreshBook() {
        discoveredPanel.removeAll();
        evolutionTreePanel.removeAll();
        aiMemoryPanel.removeAll();

        // Collect all standard and custom candy IDs
        Set<String> allCandyIds = new LinkedHashSet<>();
        allCandyIds.add("RED");
        allCandyIds.add("BLUE");
        allCandyIds.add("GREEN");
        allCandyIds.add("YELLOW");
        allCandyIds.add("PURPLE");
        allCandyIds.add("RUBY");
        allCandyIds.add("CRYSTAL");
        allCandyIds.add("EMERALD");
        allCandyIds.add("SUNBURST");
        allCandyIds.add("AMETHYST");
        allCandyIds.add("GALAXY");

        allCandyIds.addAll(Candy.getCustomCandyRegistry().keySet());
        allCandyIds.addAll(player.getDiscoveredCandyIds());

        for (String candyId : allCandyIds) {
            boolean unlocked = player.getDiscoveredCandyIds().contains(candyId.toUpperCase());
            Candy cObj = Candy.create(candyId, 0, 0);

            JPanel card = new JPanel(new BorderLayout(5, 5));
            card.setBackground(unlocked ? new Color(45, 35, 80) : new Color(20, 15, 35));
            card.setBorder(BorderFactory.createLineBorder(unlocked ? new Color(255, 215, 0) : new Color(80, 80, 100), 2, true));
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel iconLbl = new JLabel(unlocked ? getEmojiFor(candyId) : "🔒", SwingConstants.CENTER);
            iconLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));

            JLabel nameLbl = new JLabel(unlocked ? cObj.getName() : "Locked Candy", SwingConstants.CENTER);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLbl.setForeground(unlocked ? Color.WHITE : Color.GRAY);

            card.add(iconLbl, BorderLayout.CENTER);
            card.add(nameLbl, BorderLayout.SOUTH);

            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (unlocked) {
                        descriptionArea.setText("[" + cObj.getName() + " - Tier " + cObj.getTier() + "]\n" + cObj.getDescription());
                    } else {
                        descriptionArea.setText("[Locked Candy]\nSynthesize or create custom evolution rules to unlock this candy recipe in your laboratory!");
                    }
                }
            });

            discoveredPanel.add(card);
        }

        // Evolution Tree List
        for (CandyRule rule : aiAssistant.getLearnedRulesSet()) {
            JLabel ruleLbl = new JLabel("⚡ " + rule.toString() + " (" + rule.getDescription() + ")");
            ruleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            ruleLbl.setForeground(rule.isCustom() ? new Color(255, 215, 0) : new Color(135, 206, 250));
            ruleLbl.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            evolutionTreePanel.add(ruleLbl);
        }

        // AI Memory Panel List
        JLabel memHeader = new JLabel("🧠 PERMANENT AI MEMORY SYSTEM (HashMap Storage)");
        memHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        memHeader.setForeground(new Color(255, 215, 0));
        memHeader.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        aiMemoryPanel.add(memHeader);

        AIMemory memory = aiAssistant.getAiMemory();
        JLabel statsLbl = new JLabel("Stored Custom Candies: " + memory.getCustomCandyCount() + " | Stored Custom Rules: " + memory.getEvolutionRuleCount());
        statsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLbl.setForeground(Color.CYAN);
        statsLbl.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        aiMemoryPanel.add(statsLbl);

        if (memory.getCustomCandyCount() == 0 && memory.getEvolutionRuleCount() == 0) {
            JLabel emptyLbl = new JLabel("No custom candies in AI Memory yet. Click '+ Add Candy' to create candies remembered by AI!");
            emptyLbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLbl.setForeground(Color.LIGHT_GRAY);
            emptyLbl.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            aiMemoryPanel.add(emptyLbl);
        } else {
            for (Candy customCandy : memory.getAllCustomCandies()) {
                JLabel cLbl = new JLabel("🍬 " + customCandy.getName() + " (" + customCandy.getIconSymbol() + ") - Key: " + customCandy.getId() + " [Shape: " + customCandy.getShapeType() + "]");
                cLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                cLbl.setForeground(new Color(144, 238, 144));
                cLbl.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 15));
                aiMemoryPanel.add(cLbl);
            }
            for (CandyRule customRule : memory.getAllEvolutionRules()) {
                JLabel rLbl = new JLabel("🧪 Custom Rule: " + customRule.toString() + " (" + customRule.getDescription() + ")");
                rLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                rLbl.setForeground(new Color(255, 215, 0));
                rLbl.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 15));
                aiMemoryPanel.add(rLbl);
            }
        }

        discoveredPanel.revalidate();
        discoveredPanel.repaint();
        evolutionTreePanel.revalidate();
        evolutionTreePanel.repaint();
        aiMemoryPanel.revalidate();
        aiMemoryPanel.repaint();
    }

    private String getEmojiFor(String candyId) {
        switch (candyId.toUpperCase()) {
            case "RED": return "🍓";
            case "BLUE": return "💎";
            case "GREEN": return "🍏";
            case "YELLOW": return "🍋";
            case "PURPLE": return "🍇";
            case "RUBY": return "🔻";
            case "CRYSTAL": return "🔮";
            case "EMERALD": return "🌿";
            case "SUNBURST": return "☀️";
            case "AMETHYST": return "✨";
            case "GALAXY": return "🌌";
            default: return "🧪";
        }
    }
}
