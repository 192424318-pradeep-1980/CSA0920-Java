# Candy Match Saga: Candy AI Lab (Java Swing + JDBC)

An interactive desktop game in Java Swing and JDBC MySQL. Players step into the shoes of a **Candy Scientist** working in an AI Laboratory, teaching an AI Assistant how to synthesize and evolve new candies through drag-and-drop merging, custom rule creation, research tracking, and lab missions.

---

## 🔬 Core Features

1. **AI Learning Assistant (`AIAssistant.java`)**:
   - Rule-based AI engine that learns new evolution recipes whenever discovered by the player.
   - Calculates AI Learning Percentage `(learnedRules / 10) * 100%` and tracks AI Level.
   - Generates intelligent board hints suggesting valid evolution merges.

2. **Custom Candy Creator**:
   - Allows scientists to define custom evolution recipes (e.g. `Red + Red + Red = Ruby Candy`, `Ruby + Crystal = Galaxy Candy`).
   - Built-in validation prevents duplicate or empty rules.
   - Interactive rule manager allowing rule editing and deletion.

3. **Candy Research Book (`ResearchBook.java`)**:
   - Comprehensive encyclopedia displaying discovered candies vs locked candies.
   - Interactive Candy Evolution Tree showing active synthesis recipes and candy descriptions.

4. **Goal Tracker (`GoalTracker.java`)**:
   - Laboratory missions (e.g. "Discover 5 New Candies", "Unlock Galaxy Candy", "Teach AI 10 Rules", "Reach AI Level 5").
   - Real-time progress bars (`JProgressBar`) and bonus score rewards.

5. **8x8 Merging Game Board (`GameBoard.java` & `Candy.java`)**:
   - Interactive drag-and-drop candy merging.
   - Gravity physics, candy drops, refills, and particle explosion visual effects.

6. **JDBC Database Persistence (`Database.java` & `DBConnection.java`)**:
   - Saves/loads player name, score, AI Level, learned rules, discovered candies, and completed missions in table `candy_ai_lab`.
   - Connects to MySQL (`jdbc:mysql://localhost:3306/candymatch`) with automatic SQLite fallback (`candymatch.db`) if MySQL is offline.

---

## 📁 Source File Structure (12 Required Files)

```
src/main/java/com/candymatch/ailab/
├── Main.java              # Entry point: sets up system L&F and launches GameFrame
├── GameFrame.java         # Main Swing window with laboratory theme, MenuBar, dashboard & side panels
├── GameBoard.java         # 8x8 interactive canvas with drag-and-drop merging & evolution physics
├── Candy.java             # Base Candy model & custom Java2D rendering for base, evolved, & legendary candies
├── CandyRule.java         # Represents candy evolution rules (e.g. Red + Red + Red -> Ruby)
├── AIAssistant.java       # Rule-based AI engine: tracks learned rules, AI level, learning %, & hints
├── ResearchBook.java      # UI & model for discovered candies, locked candies, & evolution tree
├── GoalTracker.java       # Manages lab missions (discoveries, AI levels, rule counts) & rewards
├── Database.java          # MySQL/SQLite table operations for `candy_ai_lab` progress table
├── DBConnection.java      # JDBC connection for MySQL `candymatch` with SQLite auto-fallback
├── Player.java            # Player model holding name, score, AI level, & stats
└── Utils.java             # Helper methods for custom dialogs, exception validation, & styling
```

---

## 🚀 How to Build and Run

### Prerequisites
- **JDK 8 or higher** (`javac` and `java` in System PATH).

### Execution Steps
1. Run `compile.bat` or execute:
   ```cmd
   javac -encoding UTF-8 -cp "lib/*" -d bin src/main/java/com/candymatch/ailab/*.java
   ```
2. Run `run.bat` or execute:
   ```cmd
   java -cp "bin;lib/*" com.candymatch.ailab.Main
   ```
