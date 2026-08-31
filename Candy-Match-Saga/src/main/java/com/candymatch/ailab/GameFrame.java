package com.candymatch.ailab;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GameFrame.java
 * Main Window GUI interface for Candy AI Lab featuring:
 * - "+ Add Candy" button in top navigation bar
 * - "Create New Candy" dialog with Name, Color, Shape (Circle, Square, Diamond, Hexagon, Star), and Icon
 * - Non-duplicate candy name validation
 * - Dynamic update of Research Book, Evolution Tree, and Custom Rule dropdowns
 * - MySQL table custom_candies JDBC persistence and auto-load on startup
 */
public class GameFrame extends JFrame {

    private Player player;
    private AIAssistant aiAssistant;
    private Database database;

    private GameBoard gameBoard;
    private ResearchBook researchBook;
    private GoalTracker goalTracker;

    // Top Panel Badges
    private JLabel scoreLabel;
    private JLabel movesLabel;
    private JLabel aiLevelLabel;
    private JLabel aiMemoryLabel;

    // AI Assistant Logs
    private JTextArea aiLogArea;

    public GameFrame(Player player, AIAssistant aiAssistant) {
        this.player = player;
        this.aiAssistant = aiAssistant;
        this.database = new Database();

        setTitle("Candy Match Saga: Candy AI Lab");
        setSize(1150, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupGUIStructure();

        // Auto-load custom candies and saved progress from database on game startup
        database.loadLatestProgress(this.player, this.aiAssistant);
        updateDashboard();
    }

    private void setupGUIStructure() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(20, 15, 38));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. TOP PANEL: Game Title, + Add Candy button, Score, Moves, AI Level
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        topPanel.setBackground(new Color(30, 22, 55));
        topPanel.setBorder(BorderFactory.createLineBorder(new Color(138, 43, 226), 2, true));

        JLabel titleLabel = new JLabel("🔬 CANDY AI LAB 🔬");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 215, 0));

        // "+ Add Candy" Manager Button
        JButton addCandyBtn = Utils.createStyledButton("+ Add Candy", new Color(138, 43, 226));
        addCandyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addCandyBtn.addActionListener(e -> openCreateNewCandyDialog());

        scoreLabel = createBadge("Score: " + player.getScore(), new Color(138, 43, 226));
        movesLabel = createBadge("Moves: " + player.getMoves(), new Color(220, 20, 60));
        aiLevelLabel = createBadge("AI Level: " + aiAssistant.getAiLevel(), new Color(46, 139, 87));
        aiMemoryLabel = createBadge("🧠 Memory: " + aiAssistant.getAiMemory().getCustomCandyCount() + " Candies", new Color(147, 112, 219));

        topPanel.add(titleLabel);
        topPanel.add(addCandyBtn);
        topPanel.add(scoreLabel);
        topPanel.add(movesLabel);
        topPanel.add(aiLevelLabel);
        topPanel.add(aiMemoryLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. CENTER PANEL: 8×8 Candy Board
        gameBoard = new GameBoard(player, aiAssistant, () -> {
            updateDashboard();
            if (goalTracker != null) goalTracker.updateMissions();
            if (researchBook != null) researchBook.refreshBook();
        });

        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setOpaque(false);
        boardWrapper.add(gameBoard);

        mainPanel.add(boardWrapper, BorderLayout.CENTER);

        // 3. RIGHT PANEL: AI Assistant, Goal Tracker, AI Hint button, Research Book Button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(340, 600));

        // AI Assistant Box
        JPanel aiBox = new JPanel(new BorderLayout(5, 5));
        aiBox.setBackground(new Color(25, 20, 45));
        aiBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(30, 144, 255), 2),
                "🤖 AI LEARNING ASSISTANT",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), Color.CYAN
        ));

        aiLogArea = new JTextArea(6, 20);
        aiLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        aiLogArea.setBackground(new Color(15, 10, 30));
        aiLogArea.setForeground(new Color(144, 238, 144));
        aiLogArea.setEditable(false);
        JScrollPane aiScroll = new JScrollPane(aiLogArea);

        JButton hintBtn = Utils.createStyledButton("💡 AI Hint", new Color(138, 43, 226));
        hintBtn.addActionListener(e -> {
            String hint = aiAssistant.generateHint(gameBoard);
            aiAssistant.logAiMessage(hint);
            updateDashboard();
        });

        aiBox.add(aiScroll, BorderLayout.CENTER);
        aiBox.add(hintBtn, BorderLayout.SOUTH);

        // Goal Tracker Box
        goalTracker = new GoalTracker(player, aiAssistant);

        // Research Book Button
        JButton researchBtn = Utils.createStyledButton("📖 Research Book", new Color(70, 130, 180));
        researchBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        researchBtn.setMaximumSize(new Dimension(340, 42));
        researchBtn.addActionListener(e -> {
            JDialog dlg = new JDialog(this, "Candy Research Book", true);
            dlg.setSize(750, 550);
            dlg.setLocationRelativeTo(this);
            dlg.add(new ResearchBook(player, aiAssistant));
            dlg.setVisible(true);
        });

        rightPanel.add(aiBox);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(goalTracker);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(researchBtn);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        // 4. BOTTOM PANEL: Create Rule, Save, Load, Reset Game
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(30, 22, 55));
        bottomPanel.setBorder(BorderFactory.createLineBorder(new Color(138, 43, 226), 2, true));

        JButton createRuleBtn = Utils.createStyledButton("➕ Create Rule", new Color(138, 43, 226));
        createRuleBtn.addActionListener(e -> openCustomRuleCreatorDialog());

        JButton saveBtn = Utils.createStyledButton("💾 Save Progress", new Color(46, 139, 87));
        saveBtn.addActionListener(e -> saveProgress());

        JButton loadBtn = Utils.createStyledButton("📂 Load Progress", new Color(70, 130, 180));
        loadBtn.addActionListener(e -> loadProgress());

        JButton resetBtn = Utils.createStyledButton("🔄 Reset Game", new Color(255, 140, 0));
        resetBtn.addActionListener(e -> resetGame());

        bottomPanel.add(createRuleBtn);
        bottomPanel.add(saveBtn);
        bottomPanel.add(loadBtn);
        bottomPanel.add(resetBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel createBadge(String text, Color bg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return lbl;
    }

    public void updateDashboard() {
        scoreLabel.setText("Score: " + player.getScore());
        movesLabel.setText("Moves: " + player.getMoves());
        aiLevelLabel.setText("AI Level: " + aiAssistant.getAiLevel());
        if (aiMemoryLabel != null && aiAssistant != null && aiAssistant.getAiMemory() != null) {
            aiMemoryLabel.setText("🧠 Memory: " + aiAssistant.getAiMemory().getCustomCandyCount() + " Candies");
        }

        StringBuilder sb = new StringBuilder();
        for (String log : aiAssistant.getAiDialogLogs()) {
            sb.append(log).append("\n");
        }
        aiLogArea.setText(sb.toString());
        aiLogArea.setCaretPosition(aiLogArea.getDocument().getLength());
    }

    public void saveProgress() {
        boolean saved = database.saveProgress(player, aiAssistant);
        if (saved) {
            JOptionPane.showMessageDialog(this, "✅ Progress & Custom Rules saved to MySQL database!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Utils.showErrorDialog(this, "Save Error", "Failed to save progress to database!");
        }
    }

    public void loadProgress() {
        boolean loaded = database.loadLatestProgress(player, aiAssistant);
        if (loaded) {
            updateDashboard();
            if (goalTracker != null) goalTracker.updateMissions();
            if (researchBook != null) researchBook.refreshBook();
            JOptionPane.showMessageDialog(this, "✅ Saved progress & rules loaded from database!", "Load Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Utils.showErrorDialog(this, "Load Error", "No saved progress found!");
        }
    }

    public void resetGame() {
        player = new Player("Scientist Alex");
        aiAssistant = new AIAssistant();
        gameBoard.initializeBoard();
        updateDashboard();
        if (goalTracker != null) goalTracker.updateMissions();
        if (researchBook != null) researchBook.refreshBook();
        JOptionPane.showMessageDialog(this, "🔄 Game Reset Complete!", "Reset", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Dialog: "Create New Candy"
     * Fields:
     * - Candy Name (placeholder / text field)
     * - Candy Color (dropdown)
     * - Candy Shape (dropdown: Circle, Square, Diamond, Hexagon, Star)
     * - Candy Icon (emoji picker / dropdown)
     * Buttons: Create Candy, Cancel
     */
    private void openCreateNewCandyDialog() {
        JDialog dialog = new JDialog(this, "Create New Candy", true);
        dialog.setSize(460, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField("Thunder Candy");
        nameField.setToolTipText("Enter new candy name (e.g. Thunder Candy)");

        JComboBox<String> colorCombo = new JComboBox<>(new String[]{
                "Orange", "Red", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Gold"
        });

        JComboBox<String> shapeCombo = new JComboBox<>(new String[]{
                "Star", "Circle", "Square", "Diamond", "Hexagon"
        });

        JComboBox<String> iconCombo = new JComboBox<>(new String[]{
                "⚡", "🔥", "💎", "🔮", "🌿", "☀️", "✨", "🌌", "🧪", "💥", "🌟"
        });

        formPanel.add(new JLabel("Candy Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Candy Color:"));
        formPanel.add(colorCombo);
        formPanel.add(new JLabel("Candy Shape:"));
        formPanel.add(shapeCombo);
        formPanel.add(new JLabel("Candy Icon:"));
        formPanel.add(iconCombo);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton createBtn = Utils.createStyledButton("Create Candy", new Color(46, 139, 87));
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createBtn.addActionListener(e -> {
            String candyName = nameField.getText().trim();
            if (candyName.isEmpty()) {
                Utils.showErrorDialog(dialog, "Validation Error", "Candy Name cannot be empty!");
                return;
            }

            if (Candy.hasCandy(candyName)) {
                Utils.showErrorDialog(dialog, "Duplicate Error", "A candy with name '" + candyName + "' already exists!");
                return;
            }

            String colorStr = (String) colorCombo.getSelectedItem();
            String shapeStr = (String) shapeCombo.getSelectedItem();
            String iconStr = (String) iconCombo.getSelectedItem();

            String id = candyName.toUpperCase().replaceAll("\\s+", "_");
            Color primaryColor = parseColor(colorStr);

            // 1. Create the new candy automatically
            boolean added = Candy.registerCustomCandy(id, candyName, primaryColor, shapeStr, iconStr);

            if (added) {
                // 2. Store in AI Memory System (HashMap backed)
                aiAssistant.rememberCustomCandy(id, candyName, primaryColor, shapeStr, iconStr);

                // 3. Save using JDBC in MySQL custom_candies table
                database.saveCustomCandy(candyName, colorStr, shapeStr, iconStr);

                // 4. Add new candy to Research Book
                player.discoverCandy(id);
                if (researchBook != null) researchBook.refreshBook();

                updateDashboard();

                dialog.dispose();

                JOptionPane.showMessageDialog(
                        this,
                        "AI learned a new custom candy!\nAdded '" + candyName + "' to laboratory registry.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        JButton cancelBtn = Utils.createStyledButton("Cancel", new Color(178, 34, 34));
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(createBtn);
        btnPanel.add(cancelBtn);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Dialog: "Create Custom Evolution Rule"
     * Allows selecting from all registered standard and custom candies!
     */
    private void openCustomRuleCreatorDialog() {
        JDialog dialog = new JDialog(this, "Create Custom Evolution Rule", true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField ruleIdField = new JTextField("CR_" + (System.currentTimeMillis() % 1000));

        List<String> candyOptions = new ArrayList<>(Arrays.asList("Red Berry", "Blue Sapphire", "Green Lime", "Sunburst Yellow", "Purple Grape", "Ruby Gem", "Crystal Quartz", "Emerald Essence", "Solar Flare", "Amethyst Orb", "Galaxy Cosmic"));
        for (Candy c : Candy.getCustomCandyRegistry().values()) {
            if (!candyOptions.contains(c.getName())) candyOptions.add(c.getName());
        }

        JComboBox<String> in1Combo = new JComboBox<>(candyOptions.toArray(new String[0]));
        JComboBox<String> in2Combo = new JComboBox<>(candyOptions.toArray(new String[0]));
        JTextField resultNameField = new JTextField("Fire Candy");

        formPanel.add(new JLabel("Rule ID:"));
        formPanel.add(ruleIdField);
        formPanel.add(new JLabel("Input Candy 1:"));
        formPanel.add(in1Combo);
        formPanel.add(new JLabel("Input Candy 2:"));
        formPanel.add(in2Combo);
        formPanel.add(new JLabel("New Result Candy Name:"));
        formPanel.add(resultNameField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton saveRuleBtn = Utils.createStyledButton("Save Rule", new Color(46, 139, 87));
        saveRuleBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveRuleBtn.addActionListener(e -> {
            try {
                String ruleId = ruleIdField.getText().trim();
                String in1Name = (String) in1Combo.getSelectedItem();
                String in2Name = (String) in2Combo.getSelectedItem();
                String resultName = resultNameField.getText().trim();

                String in1Id = in1Name.toUpperCase().replaceAll("\\s+", "_");
                String in2Id = in2Name.toUpperCase().replaceAll("\\s+", "_");
                String resultId = resultName.toUpperCase().replaceAll("\\s+", "_");

                List<String> inputs = Arrays.asList(in1Id, in2Id);

                Utils.validateCustomRule(ruleId, inputs, resultName, aiAssistant);

                Candy.registerCustomCandy(resultId, resultName, new Color(255, 140, 0), "Star", "🔥");
                aiAssistant.rememberCustomCandy(resultId, resultName, new Color(255, 140, 0), "Star", "🔥");

                CandyRule customRule = new CandyRule(ruleId, inputs, resultId, "Scientist Creation: " + resultName, true);

                aiAssistant.learnRule(customRule);
                player.discoverCandy(resultId);
                if (researchBook != null) researchBook.refreshBook();

                database.saveProgress(player, aiAssistant);

                updateDashboard();
                if (goalTracker != null) goalTracker.updateMissions();

                dialog.dispose();

                JOptionPane.showMessageDialog(
                        this,
                        "AI learned a new custom candy evolution!",
                        "AI Learning Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Utils.CustomRuleException ex) {
                Utils.showErrorDialog(this, "Rule Validation Error", ex.getMessage());
            }
        });

        JButton cancelBtn = Utils.createStyledButton("Cancel", new Color(178, 34, 34));
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveRuleBtn);
        btnPanel.add(cancelBtn);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private Color parseColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red": return new Color(220, 20, 60);
            case "orange": return new Color(255, 140, 0);
            case "yellow": return new Color(255, 215, 0);
            case "green": return new Color(46, 139, 87);
            case "cyan": return new Color(0, 206, 209);
            case "blue": return new Color(30, 144, 255);
            case "purple": return new Color(138, 43, 226);
            case "pink": return new Color(255, 105, 180);
            case "gold": return new Color(255, 215, 0);
            default: return new Color(128, 0, 128);
        }
    }
}
