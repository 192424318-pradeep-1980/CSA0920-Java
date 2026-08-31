package com.candymatch;

import com.candymatch.custom.CustomCandyManager;
import com.candymatch.custom.CustomRuleManager;
import com.candymatch.custom.ResearchBook;
import com.candymatch.game.GameManager;

import com.candymatch.ui.CustomCandyUI;
import com.candymatch.ui.GameOverUI;
import com.candymatch.ui.GameUI;
import com.candymatch.ui.LevelCompleteUI;
import com.candymatch.ui.LevelSelectionUI;
import com.candymatch.ui.MainMenu;
import com.candymatch.ui.ResearchBookUI;

import javax.swing.*;
import java.awt.*;

/**
 * Candy Match Saga - Desktop Application Launcher.
 * Integrates CardLayout navigation between MainMenu, LevelSelect, GameUI, and
 * ResearchBook screens.
 */
public class Main extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    private final GameManager gameManager;
    private final CustomCandyManager customCandyManager;
    private final CustomRuleManager customRuleManager;
    private final ResearchBook researchBook;

    private MainMenu mainMenu;
    private LevelSelectionUI levelSelectionUI;
    private GameUI gameUI;
    private ResearchBookUI researchBookUI;

    public Main() {
        setTitle("Candy Match Saga - Rule-Based AI & Player Analytics");
        setSize(920, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        gameManager = new GameManager();
        customCandyManager = new CustomCandyManager();
        customRuleManager = new CustomRuleManager();
        researchBook = new ResearchBook(customCandyManager, customRuleManager);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        initScreens();
        add(mainContainer);

        // Register GameManager Callbacks
        gameManager.setCallbacks(
                () -> {
                    if (gameUI != null)
                        gameUI.refreshUI();
                },
                this::handleGameEnd);
    }

    private void initScreens() {
        // Main Menu
        mainMenu = new MainMenu(new MainMenu.MainMenuListener() {
            @Override
            public void onStartGame(String playerName, String mode) {
                gameManager.startGame(playerName, mode, 1);
                cardLayout.show(mainContainer, "GAME");
                if (gameUI != null)
                    gameUI.refreshUI();
            }

            @Override
            public void onOpenLevelSelect() {
                cardLayout.show(mainContainer, "LEVEL_SELECT");
            }

            @Override
            public void onOpenResearchBook() {
                if (researchBookUI != null)
                    researchBookUI.refreshTables();
                cardLayout.show(mainContainer, "RESEARCH_BOOK");
            }

            @Override
            public void onOpenCustomCandyUI() {
                openCustomCandyDialog();
            }
        });
        mainContainer.add(mainMenu, "MAIN_MENU");

        // Level Select UI
        levelSelectionUI = new LevelSelectionUI(new LevelSelectionUI.LevelSelectListener() {
            @Override
            public void onLevelSelected(int levelNumber) {
                gameManager.startLevel(levelNumber);
                cardLayout.show(mainContainer, "GAME");
                if (gameUI != null)
                    gameUI.refreshUI();
            }

            @Override
            public void onBackToMenu() {
                cardLayout.show(mainContainer, "MAIN_MENU");
            }
        });
        mainContainer.add(levelSelectionUI, "LEVEL_SELECT");

        // Game UI
        gameUI = new GameUI(gameManager, new GameUI.GameUIListener() {
            @Override
            public void onOpenLevelSelect() {
                cardLayout.show(mainContainer, "LEVEL_SELECT");
            }

            @Override
            public void onOpenMainMenu() {
                cardLayout.show(mainContainer, "MAIN_MENU");
            }
        });
        mainContainer.add(gameUI, "GAME");

        // Research Book UI
        researchBookUI = new ResearchBookUI(researchBook, () -> cardLayout.show(mainContainer, "MAIN_MENU"));
        mainContainer.add(researchBookUI, "RESEARCH_BOOK");

        cardLayout.show(mainContainer, "MAIN_MENU");
    }

    private void openCustomCandyDialog() {
        CustomCandyUI dialog = new CustomCandyUI(this, customCandyManager, customRuleManager, () -> {
            if (researchBookUI != null)
                researchBookUI.refreshTables();
        });
        dialog.setVisible(true);
    }

    private void handleGameEnd() {
        boolean won = gameManager.checkWinCondition();
        if (won) {
            LevelCompleteUI dlg = new LevelCompleteUI(this,
                    gameManager.getAnalyticsManager().getPlayerAnalytics().getSession(),
                    new LevelCompleteUI.LevelCompleteListener() {
                        @Override
                        public void onNextLevel() {
                            int nextLvl = gameManager.getCurrentLevelNumber() + 1;
                            if (nextLvl > 20)
                                nextLvl = 1;
                            gameManager.startLevel(nextLvl);
                            cardLayout.show(mainContainer, "GAME");
                            if (gameUI != null)
                                gameUI.refreshUI();
                        }

                        @Override
                        public void onLevelSelect() {
                            cardLayout.show(mainContainer, "LEVEL_SELECT");
                        }

                        @Override
                        public void onMainMenu() {
                            cardLayout.show(mainContainer, "MAIN_MENU");
                        }
                    });
            dlg.setVisible(true);
        } else {
            GameOverUI dlg = new GameOverUI(this, gameManager.getAnalyticsManager().getPlayerAnalytics().getSession(),
                    new GameOverUI.GameOverListener() {
                        @Override
                        public void onRetry() {
                            gameManager.startLevel(gameManager.getCurrentLevelNumber());
                            cardLayout.show(mainContainer, "GAME");
                            if (gameUI != null)
                                gameUI.refreshUI();
                        }

                        @Override
                        public void onLevelSelect() {
                            cardLayout.show(mainContainer, "LEVEL_SELECT");
                        }

                        @Override
                        public void onMainMenu() {
                            cardLayout.show(mainContainer, "MAIN_MENU");
                        }
                    });
            dlg.setVisible(true);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
