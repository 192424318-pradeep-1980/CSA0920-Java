package com.candymatch.ailab;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GoalTracker.java
 * Manages laboratory missions and goal tracking.
 * Missions:
 * 1. Discover 5 new candies
 * 2. Unlock Galaxy Candy
 * 3. Teach AI 10 rules
 * 4. Reach AI Level 5
 * Displays mission progress using JProgressBar and rewards bonus points upon completion.
 */
public class GoalTracker extends JPanel {

    public static class Mission {
        private String id;
        private String title;
        private String description;
        private int currentVal;
        private int targetVal;
        private int rewardPoints;
        private boolean completed;

        public Mission(String id, String title, String description, int targetVal, int rewardPoints) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.currentVal = 0;
            this.targetVal = targetVal;
            this.rewardPoints = rewardPoints;
            this.completed = false;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getCurrentVal() { return currentVal; }
        public int getTargetVal() { return targetVal; }
        public int getRewardPoints() { return rewardPoints; }
        public boolean isCompleted() { return completed; }

        public void updateVal(int val) {
            this.currentVal = Math.min(targetVal, val);
            if (this.currentVal >= targetVal) {
                this.completed = true;
            }
        }

        public void setCompleted(boolean comp) { this.completed = comp; }
    }

    private Player player;
    private AIAssistant aiAssistant;
    private List<Mission> missions;
    private JPanel missionsListPanel;

    public GoalTracker(Player player, AIAssistant aiAssistant) {
        this.player = player;
        this.aiAssistant = aiAssistant;
        this.missions = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(25, 20, 45));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel("🎯 LABORATORY MISSIONS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(255, 215, 0));
        add(titleLabel, BorderLayout.NORTH);

        missionsListPanel = new JPanel();
        missionsListPanel.setLayout(new BoxLayout(missionsListPanel, BoxLayout.Y_AXIS));
        missionsListPanel.setBackground(new Color(30, 25, 55));

        JScrollPane scroll = new JScrollPane(missionsListPanel);
        scroll.getViewport().setBackground(new Color(30, 25, 55));
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        initializeMissions();
        updateMissions();
    }

    private void initializeMissions() {
        missions.add(new Mission("M1", "Discover 5 New Candies", "Unlock at least 5 different candy species in the lab.", 5, 200));
        missions.add(new Mission("M2", "Unlock Galaxy Candy", "Synthesize the legendary cosmic Galaxy Candy.", 1, 500));
        missions.add(new Mission("M3", "Teach AI 10 Rules", "Expand AI Assistant memory to 10 learned rules.", 10, 300));
        missions.add(new Mission("M4", "Reach AI Level 5", "Train the AI Assistant to reach AI Level 5.", 5, 400));
    }

    /**
     * Checks and updates mission statuses.
     */
    public void updateMissions() {
        // Mission 1: Discover 5 new candies
        Mission m1 = missions.get(0);
        m1.updateVal(player.getDiscoveredCandyIds().size());

        // Mission 2: Unlock Galaxy Candy
        Mission m2 = missions.get(1);
        m2.updateVal(player.getDiscoveredCandyIds().contains("GALAXY") ? 1 : 0);

        // Mission 3: Teach AI 10 rules
        Mission m3 = missions.get(2);
        m3.updateVal(aiAssistant.getLearnedRulesSet().size());

        // Mission 4: Reach AI Level 5
        Mission m4 = missions.get(3);
        m4.updateVal(aiAssistant.getAiLevel());

        // Process completion rewards
        for (Mission m : missions) {
            if (m.isCompleted() && !player.getCompletedGoalIds().contains(m.getId())) {
                player.completeGoal(m.getId());
                player.addScore(m.getRewardPoints());
                JOptionPane.showMessageDialog(
                        this,
                        "🎉 MISSION COMPLETED! 🎉\n\n" + m.getTitle() + "\nReward: +" + m.getRewardPoints() + " Bonus Score Points!",
                        "Lab Mission Reward",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }

        renderMissionsUI();
    }

    private void renderMissionsUI() {
        missionsListPanel.removeAll();

        for (Mission m : missions) {
            JPanel card = new JPanel(new BorderLayout(8, 6));
            card.setBackground(new Color(40, 32, 70));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(m.isCompleted() ? new Color(50, 205, 50) : new Color(90, 80, 130), 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            JLabel title = new JLabel((m.isCompleted() ? "✅ " : "📌 ") + m.getTitle() + " (+" + m.getRewardPoints() + " pts)");
            title.setFont(new Font("Segoe UI", Font.BOLD, 13));
            title.setForeground(m.isCompleted() ? new Color(144, 238, 144) : Color.WHITE);

            JLabel desc = new JLabel(m.getDescription());
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            desc.setForeground(new Color(190, 190, 230));

            JProgressBar pb = new JProgressBar(0, m.getTargetVal());
            pb.setValue(m.getCurrentVal());
            pb.setStringPainted(true);
            pb.setString(m.getCurrentVal() + " / " + m.getTargetVal());
            pb.setForeground(m.isCompleted() ? new Color(50, 205, 50) : new Color(138, 43, 226));
            pb.setBackground(new Color(20, 15, 35));

            JPanel topBox = new JPanel(new GridLayout(2, 1));
            topBox.setOpaque(false);
            topBox.add(title);
            topBox.add(desc);

            card.add(topBox, BorderLayout.NORTH);
            card.add(pb, BorderLayout.SOUTH);

            missionsListPanel.add(card);
            missionsListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        missionsListPanel.revalidate();
        missionsListPanel.repaint();
    }
}
