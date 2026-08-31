/* =======================================================
   Candy Match Saga - Web Application Engine
   Turn-Based AI Opponent with Adaptive Player Learning Engine,
   Real Hint Generator, 20 Levels, Split Player & AI Scores,
   Diverse Unique Candy Icons Palette (No Duplicate Icons!),
   Working Candy Synthesis Engine (Spawns Synthesized Candies on Grid!),
   Research Book Tab Switching (Candies vs Combinations),
   Real-Time Match Counters Breakdown (Match 2, 3, 4, 5),
   Power BI Analytics Dashboard, & AI Worth Analyzer (ACCEPT / REJECT)
   ======================================================= */

(function () {
    const GRID_SIZE = 8;
    const CELL_PIXELS = 64;

    // Game Mode State
    let gameMode = 'PLAYER_ONLY'; // 'PLAYER_ONLY' vs 'PLAYER_VS_AI'
    let currentTurn = 'PLAYER_TURN'; // 'PLAYER_TURN', 'AI_TURN', 'PROCESSING_MOVE', 'GAME_OVER', 'LEVEL_COMPLETE'
    let playerInputEnabled = true;

    // Player & AI Score Split State
    let playerName = 'Player1';
    let currentLevel = 1;
    let score = 0;
    let playerScore = 0;
    let aiScore = 0;
    let movesRemaining = 20;
    let movesUsed = 0;
    let targetScore = 1000;
    let comboMultiplier = 1;
    let maxComboInSession = 1;
    let hintsUsed = 0;

    // Adaptive AI Learning Model
    let playerFavCandyCounts = {};
    let playerMatchSizeCounts = { 2: 0, 3: 0, 4: 0, 5: 0 };
    let playerLearnedStrategy = 'Observing Player...';
    let playerFavCandyName = 'None';
    let playerFavCandyKey = 'RED';

    // Telemetry Statistics (INCLUDES MATCH 2)
    let validSwaps = 0;
    let invalidSwaps = 0;
    let playerMovesCount = 0;
    let aiMovesCount = 0;
    let match2Count = 0;
    let match3Count = 0;
    let match4Count = 0;
    let match5Count = 0;
    let totalMatchesCount = 0;
    let specialCandiesCreated = 0;
    let specialCandiesActivated = 0;
    let startTimeSeconds = Date.now();

    // Selection & Active Item state for AI Analyzer
    let grid = []; // 8x8 array of Candy objects
    let selectedCell = null;
    let activeHintMove = null;
    let selectedCandyKeyForAnalysis = 'RUBY';

    // Sound FX
    let soundEnabled = true;
    let audioCtx = null;

    // 20 Level Definitions
    const LEVELS_CONFIG = {};
    for (let i = 1; i <= 20; i++) {
        let tier = i <= 5 ? "Beginner" : i <= 10 ? "Intermediate" : i <= 15 ? "Advanced" : "Expert";
        let target = i <= 5 ? 1000 + i * 300 : i <= 10 ? 2800 + (i - 5) * 500 : i <= 15 ? 5500 + (i - 10) * 800 : 10000 + (i - 15) * 1200;
        let moves = i <= 5 ? 25 - i : i <= 10 ? 22 - (i - 5) : i <= 15 ? 20 - (i - 10) : 18 - (i - 15);
        LEVELS_CONFIG[i] = { level: i, tier: tier, target: target, moves: moves };
    }

    // Dynamic Candy Definitions Catalogue (ALL UNIQUE ICONS & COLORS!)
    const CANDY_TYPES = {
        RED: { name: 'Red Berry', color: '#eb2d55', topColor: '#ff6482', icon: '🍓', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        BLUE: { name: 'Blueberry Blue', color: '#2d8cf0', topColor: '#87cefa', icon: '🫐', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        GREEN: { name: 'Apple Green', color: '#2dc86e', topColor: '#7cfc00', icon: '🍏', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        YELLOW: { name: 'Lemon Yellow', color: '#fac81e', topColor: '#fff078', icon: '🍋', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        PURPLE: { name: 'Grape Purple', color: '#a03ce6', topColor: '#d8bfd8', icon: '🍇', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        ORANGE: { name: 'Orange Citrus', color: '#ff821e', topColor: '#ffdc64', icon: '🍊', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        RUBY_BASIC: { name: 'Ruby Crystal', color: '#dc143c', topColor: '#ff69b4', icon: '🔻', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        AMETHYST_BASIC: { name: 'Amethyst Crystal', color: '#8a2be2', topColor: '#da70d6', icon: '🔮', baseScore: 30, formula: 'Basic Candy', matchSize: 3 },
        RUBY: { name: 'Ruby Candy', color: '#b40028', topColor: '#ff5a78', icon: '🔻', baseScore: 100, formula: 'Match 2: 🍓 Red + 🍓 Red = 🔻 Ruby', matchSize: 2 },
        AMETHYST: { name: 'Amethyst Orb', color: '#4b0082', topColor: '#b478ff', icon: '🔮', baseScore: 120, formula: 'Match 2: 🍇 Grape + 🫐 Blueberry = 🔮 Amethyst', matchSize: 2 },
        STAR: { name: 'Super Star', color: '#ff69b4', topColor: '#ffc0cb', icon: '⭐', baseScore: 150, formula: 'Match 2: 🍋 Lemon + 🍓 Red = ⭐ Super Star', matchSize: 2 }
    };

    // Color map for dynamically created synthesized candy icons
    const ICON_COLOR_MAP = {
        '🔻': '#b40028', '🔮': '#4b0082', '💎': '#00a86b', '🧪': '#00f2fe',
        '✨': '#ba55d3', '⭐': '#ffd700', '🌟': '#ffeb3b', '☀️': '#ff7f00',
        '🌙': '#4a90e2', '⚡': '#ffeb3b', '💥': '#ff3300', '🌌': '#191970',
        '🔥': '#ff4500', '🍬': '#ff69b4', '🍭': '#ff1493', '🍫': '#8b4513',
        '🍩': '#d2691e', '🧁': '#ffb6c1', '🍰': '#ffc0cb', '🍪': '#cd853f',
        '🍯': '#ffa500', '🍍': '#ffd700', '🍑': '#ffabe7', '🍒': '#dc143c',
        '🥝': '#8fe388', '🍉': '#ff4081', '🍌': '#ffee58', '🍎': '#e53935', '🍐': '#aed581'
    };

    const SYNTHESIS_RULES = [
        { in1: 'RED', in2: 'RED', matchSize: 2, outName: 'Ruby Candy', outKey: 'RUBY', icon: '🔻', scoreVal: 100, formula: 'Match 2: 🍓 Red Berry + 🍓 Red Berry => 🔻 Ruby Candy' },
        { in1: 'PURPLE', in2: 'BLUE', matchSize: 2, outName: 'Amethyst Orb', outKey: 'AMETHYST', icon: '🔮', scoreVal: 120, formula: 'Match 2: 🍇 Grape + 🫐 Blueberry => 🔮 Amethyst Orb' },
        { in1: 'YELLOW', in2: 'RED', matchSize: 2, outName: 'Super Star', outKey: 'STAR', icon: '⭐', scoreVal: 150, formula: 'Match 2: 🍋 Lemon + 🍓 Red => ⭐ Super Star' }
    ];

    const CUSTOM_RULES = [
        { name: 'Ruby Combination Rule', condition: 'Match 2: Red Berry + Red Berry', effect: 'Synthesizes Ruby Candy (+100 pts)' },
        { name: 'Double Combo Multiplier', condition: 'Cascade >= 3', effect: '2x score bonus' }
    ];

    // Audio helper
    function playSound(type) {
        if (!soundEnabled) return;
        try {
            if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
            if (audioCtx.state === 'suspended') audioCtx.resume();
            const now = audioCtx.currentTime;
            const osc = audioCtx.createOscillator();
            const gain = audioCtx.createGain();
            osc.connect(gain);
            gain.connect(audioCtx.destination);

            if (type === 'pop') {
                osc.type = 'triangle';
                osc.frequency.setValueAtTime(440, now);
                osc.frequency.exponentialRampToValueAtTime(880, now + 0.15);
                gain.gain.setValueAtTime(0.3, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + 0.15);
                osc.start(now); osc.stop(now + 0.15);
            } else if (type === 'swap') {
                osc.type = 'sine';
                osc.frequency.setValueAtTime(300, now);
                osc.frequency.linearRampToValueAtTime(500, now + 0.1);
                gain.gain.setValueAtTime(0.2, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
                osc.start(now); osc.stop(now + 0.1);
            }
        } catch (e) {}
    }

    function createCandy(typeKey, specialType = 'NONE') {
        const def = CANDY_TYPES[typeKey] || CANDY_TYPES.RED;
        return {
            type: typeKey,
            specialType: specialType,
            name: def.name,
            color: def.color,
            topColor: def.topColor || '#ffffff',
            icon: def.icon,
            baseScore: def.baseScore,
            formula: def.formula || 'Standard Candy',
            matchSize: def.matchSize || 3,
            highlighted: false
        };
    }

    // Active Candy Variety Pool Count (Controls match frequency; default 7 for challenging, less automatic cascades)
    let candyPoolCount = 7;

    function getRandomTypeKey() {
        const ALL_BASIC_KEYS = ['RED', 'BLUE', 'GREEN', 'YELLOW', 'PURPLE', 'ORANGE', 'RUBY_BASIC', 'AMETHYST_BASIC'];
        const activeKeys = ALL_BASIC_KEYS.slice(0, Math.min(ALL_BASIC_KEYS.length, candyPoolCount));
        return activeKeys[Math.floor(Math.random() * activeKeys.length)];
    }



    function initBoard() {
        let attempts = 0;
        let valid = false;
        while (!valid && attempts < 30) {
            attempts++;
            grid = [];
            for (let r = 0; r < GRID_SIZE; r++) {
                grid[r] = [];
                for (let c = 0; c < GRID_SIZE; c++) {
                    let typeKey;
                    let innerTries = 0;
                    do {
                        typeKey = getRandomTypeKey();
                        innerTries++;
                    } while (createsInitialMatch(r, c, typeKey) && innerTries < 15);
                    grid[r][c] = createCandy(typeKey);
                }
            }
            if (findAllValidMoves().length > 0) {
                valid = true;
            }
        }
    }


    function createsInitialMatch(r, c, typeKey) {
        if (c >= 2 && grid[r][c - 1] && grid[r][c - 2] && grid[r][c - 1].type === typeKey && grid[r][c - 2].type === typeKey) {
            return true;
        }
        if (r >= 2 && grid[r - 1] && grid[r - 2] && grid[r - 1][c].type === typeKey && grid[r - 2][c].type === typeKey) {
            return true;
        }
        return false;
    }

    function swap(r1, c1, r2, c2) {
        let temp = grid[r1][c1];
        grid[r1][c1] = grid[r2][c2];
        grid[r2][c2] = temp;
    }

    function areAdjacent(r1, c1, r2, c2) {
        let dr = Math.abs(r1 - r2);
        let dc = Math.abs(c1 - c2);
        return (dr === 1 && dc === 0) || (dr === 0 && dc === 1);
    }

    // =========================================================
    // MATCH DETECTOR & CANDY SYNTHESIS ENGINE
    // =========================================================
    function detectAllMatches() {
        let matches = [];

        // Horizontal matches (3+)
        for (let r = 0; r < GRID_SIZE; r++) {
            let count = 1;
            for (let c = 0; c < GRID_SIZE; c++) {
                let curr = grid[r][c];
                let next = (c + 1 < GRID_SIZE) ? grid[r][c + 1] : null;
                if (curr && next && curr.type === next.type) {
                    count++;
                } else {
                    if (count >= 3 && curr) {
                        let group = [];
                        for (let k = c - count + 1; k <= c; k++) {
                            group.push({ r: r, c: k });
                        }
                        matches.push({ type: curr.type, coords: group });
                    }
                    count = 1;
                }
            }
        }

        // Vertical matches (3+)
        for (let c = 0; c < GRID_SIZE; c++) {
            let count = 1;
            for (let r = 0; r < GRID_SIZE; r++) {
                let curr = grid[r][c];
                let next = (r + 1 < GRID_SIZE) ? grid[r + 1][c] : null;
                if (curr && next && curr.type === next.type) {
                    count++;
                } else {
                    if (count >= 3 && curr) {
                        let group = [];
                        for (let k = r - count + 1; k <= r; k++) {
                            group.push({ r: k, c: c });
                        }
                        matches.push({ type: curr.type, coords: group });
                    }
                    count = 1;
                }
            }
        }

        // CANDY SYNTHESIS MATCHES (Match-2 Pair & Custom Multi-Ingredients)
        for (let r = 0; r < GRID_SIZE; r++) {
            for (let c = 0; c < GRID_SIZE; c++) {
                let curr = grid[r][c];
                if (!curr) continue;

                // Check horizontal adjacent synthesis
                if (c + 1 < GRID_SIZE && grid[r][c + 1]) {
                    let right = grid[r][c + 1];
                    for (let s of SYNTHESIS_RULES) {
                        if ((curr.type === s.in1 && right.type === s.in2) || (curr.type === s.in2 && right.type === s.in1)) {
                            matches.push({
                                type: s.outKey,
                                outKey: s.outKey,
                                coords: [{ r: r, c: c }, { r: r, c: c + 1 }],
                                isSynthesis: true,
                                isMatch2: (s.matchSize === 2)
                            });
                        }
                    }
                }

                // Check vertical adjacent synthesis
                if (r + 1 < GRID_SIZE && grid[r + 1][c]) {
                    let down = grid[r + 1][c];
                    for (let s of SYNTHESIS_RULES) {
                        if ((curr.type === s.in1 && down.type === s.in2) || (curr.type === s.in2 && down.type === s.in1)) {
                            matches.push({
                                type: s.outKey,
                                outKey: s.outKey,
                                coords: [{ r: r, c: c }, { r: r + 1, c: c }],
                                isSynthesis: true,
                                isMatch2: (s.matchSize === 2)
                            });
                        }
                    }
                }
            }
        }

        return matches;
    }

    function applyGravityAndRefill() {
        // Gravity
        for (let c = 0; c < GRID_SIZE; c++) {
            for (let r = GRID_SIZE - 1; r >= 0; r--) {
                if (!grid[r][c]) {
                    for (let aboveR = r - 1; aboveR >= 0; aboveR--) {
                        if (grid[aboveR][c]) {
                            grid[r][c] = grid[aboveR][c];
                            grid[aboveR][c] = null;
                            break;
                        }
                    }
                }
            }
        }
        // Refill empty slots
        for (let r = 0; r < GRID_SIZE; r++) {
            for (let c = 0; c < GRID_SIZE; c++) {
                if (!grid[r][c]) {
                    grid[r][c] = createCandy(getRandomTypeKey());
                }
            }
        }
    }

    // =========================================================
    // RULE-BASED & ADAPTIVE AI ENGINE
    // =========================================================
    function learnFromPlayerMove(c1Obj, matches) {
        if (!c1Obj) return;

        // Record player candy type preference
        let type = c1Obj.type;
        playerFavCandyCounts[type] = (playerFavCandyCounts[type] || 0) + 1;

        // Find most frequent player candy type
        let topKey = type;
        let topCount = 0;
        for (let k in playerFavCandyCounts) {
            if (playerFavCandyCounts[k] > topCount) {
                topCount = playerFavCandyCounts[k];
                topKey = k;
            }
        }
        playerFavCandyKey = topKey;
        let candDef = CANDY_TYPES[topKey];
        playerFavCandyName = candDef ? `${candDef.icon} ${candDef.name}` : topKey;

        // Record match sizes
        for (let m of matches) {
            let sz = m.isMatch2 ? 2 : Math.min(5, m.coords.length);
            playerMatchSizeCounts[sz] = (playerMatchSizeCounts[sz] || 0) + 1;
        }

        // Determine player strategy pattern
        if (playerMatchSizeCounts[5] > 0 || playerMatchSizeCounts[4] > playerMatchSizeCounts[3]) {
            playerLearnedStrategy = 'Aggressive Special Matching';
        } else if (playerMatchSizeCounts[2] > playerMatchSizeCounts[3]) {
            playerLearnedStrategy = 'Synthesis Pair Combination';
        } else {
            playerLearnedStrategy = 'Balanced Cascade Strategy';
        }
    }

    function findAllValidMoves() {
        let validMoves = [];
        for (let r = 0; r < GRID_SIZE; r++) {
            for (let c = 0; c < GRID_SIZE; c++) {
                // Check right swap
                if (c + 1 < GRID_SIZE) {
                    swap(r, c, r, c + 1);
                    let matches = detectAllMatches();
                    swap(r, c, r, c + 1); // restore
                    if (matches.length > 0) {
                        let score = evaluateMoveMatches(matches);
                        let rank = evaluateMoveRank(matches);
                        validMoves.push({ r1: r, c1: c, r2: r, c2: c + 1, rank: rank, score: score, matches: matches });
                    }
                }
                // Check down swap
                if (r + 1 < GRID_SIZE) {
                    swap(r, c, r + 1, c);
                    let matches = detectAllMatches();
                    swap(r, c, r + 1, c); // restore
                    if (matches.length > 0) {
                        let score = evaluateMoveMatches(matches);
                        let rank = evaluateMoveRank(matches);
                        validMoves.push({ r1: r, c1: c, r2: r + 1, c: c, rank: rank, score: score, matches: matches });
                    }
                }
            }
        }

        // ADAPTIVE LEARNING BIAS: Boost candidate AI moves matching player's learned favorite candy!
        for (let vm of validMoves) {
            for (let m of vm.matches) {
                if (m.type === playerFavCandyKey) {
                    vm.score += 50; // Learned strategic preference bonus!
                    vm.rank += 1;
                }
            }
        }

        // Sort by rank descending, then score descending
        validMoves.sort((a, b) => b.rank !== a.rank ? b.rank - a.rank : b.score - a.score);
        return validMoves;
    }

    function evaluateMoveRank(matches) {
        let maxRank = 2; // Match 3
        for (let m of matches) {
            let sz = m.coords.length;
            if (sz >= 5) maxRank = Math.max(maxRank, 5); // Match 5
            else if (sz === 4) maxRank = Math.max(maxRank, 3); // Match 4
            else if (m.isMatch2 || m.isSynthesis) maxRank = Math.max(maxRank, 4); // Synthesis Match
        }
        return maxRank;
    }

    function evaluateMoveMatches(matches) {
        let total = 0;
        for (let m of matches) {
            let sz = m.coords.length;
            let base = CANDY_TYPES[m.type] ? CANDY_TYPES[m.type].baseScore : 30;
            if (m.isMatch2 || m.isSynthesis) total += base * 2.5;
            else total += (sz >= 5 ? base * 3.5 : sz === 4 ? base * 2 : base);
        }
        return total;
    }

    // =========================================================
    // TURN MANAGER & MOVE EXECUTION
    // =========================================================
    function setPlayerInputEnabled(enabled) {
        playerInputEnabled = enabled;
    }

    function updateTurnUI() {
        const turnBanner = document.getElementById('turn-banner');
        const turnText = document.getElementById('turn-banner-text');
        const hintBtn = document.getElementById('btn-ai-hint');

        if (currentTurn === 'AI_TURN') {
            turnBanner.className = 'turn-banner ai-turn-banner';
            turnText.innerText = '🤖 AI TURN (Thinking...)';
            if (hintBtn) hintBtn.disabled = true;
            setPlayerInputEnabled(false);
        } else if (currentTurn === 'PLAYER_TURN') {
            turnBanner.className = 'turn-banner player-turn-banner';
            turnText.innerText = '👤 YOUR TURN';
            if (hintBtn) hintBtn.disabled = false;
            setPlayerInputEnabled(true);
        } else if (currentTurn === 'GAME_OVER') {
            turnBanner.className = 'turn-banner game-over-banner';
            turnText.innerText = '💥 GAME OVER - OUT OF MOVES!';
            if (hintBtn) hintBtn.disabled = true;
            setPlayerInputEnabled(false);
        } else if (currentTurn === 'LEVEL_COMPLETE') {
            turnBanner.className = 'turn-banner level-complete-banner';
            turnText.innerText = '🎉 LEVEL COMPLETE - WINNER!';
            if (hintBtn) hintBtn.disabled = true;
            setPlayerInputEnabled(false);
        } else {
            turnBanner.className = 'turn-banner processing-turn-banner';
            turnText.innerText = '⚡ PROCESSING MATCHES...';
            if (hintBtn) hintBtn.disabled = true;
            setPlayerInputEnabled(false);
        }
        updateDashboard();
    }


    function startPlayerTurn() {
        currentTurn = 'PLAYER_TURN';
        updateTurnUI();
    }

    function startAITurn() {
        if (gameMode !== 'PLAYER_VS_AI') {
            startPlayerTurn();
            return;
        }
        currentTurn = 'AI_TURN';
        updateTurnUI();
        logAIMessage(`🤖 AI Opponent is calculating move using learned player strategy (${playerLearnedStrategy})...`);

        setTimeout(() => {
            executeOneAIMove();
        }, 600);
    }

    function executeOneAIMove() {
        if (currentTurn !== 'AI_TURN') return;

        let validMoves = findAllValidMoves();
        if (validMoves.length === 0) {
            logAIMessage('🤖 No valid moves! Reshuffling board...');
            initBoard();
            validMoves = findAllValidMoves();
        }

        if (validMoves.length > 0) {
            let bestMove = validMoves[0];
            logAIMessage(`🤖 AI executes move: (${bestMove.r1 + 1},${bestMove.c1 + 1}) <-> (${bestMove.r2 + 1},${bestMove.c2 + 1}) [Learned Target: ${playerFavCandyName}]`);
            aiMovesCount++;

            swap(bestMove.r1, bestMove.c1, bestMove.r2, bestMove.c2);
            processMatchesAndCascades(false, () => {
                // AI cascade complete
                if (checkGameStatus()) {
                    // MANDATED: Control MUST automatically return to PLAYER_TURN!
                    startPlayerTurn();
                }
            });
        } else {
            startPlayerTurn();
        }
    }

    function handlePlayerSwapAttempt(r1, c1, r2, c2) {
        if (currentTurn !== 'PLAYER_TURN' || !playerInputEnabled) return;
        if (!areAdjacent(r1, c1, r2, c2)) return;

        // Clear active hint
        clearHintHighlights();

        currentTurn = 'PROCESSING_MOVE';
        updateTurnUI();

        let swappedCandy = grid[r1][c1];

        swap(r1, c1, r2, c2);
        let matches = detectAllMatches();

        if (matches.length === 0) {
            // Invalid swap! Revert swap
            swap(r1, c1, r2, c2);
            invalidSwaps++;
            playerMovesCount++;
            playSound('error');
            startPlayerTurn();
            return;
        }

        // ADAPTIVE AI LEARNING: AI learns from player's successful move!
        learnFromPlayerMove(swappedCandy, matches);

        validSwaps++;
        playerMovesCount++;
        movesRemaining--;
        movesUsed++;
        playSound('swap');

        processMatchesAndCascades(true, () => {
            // Player cascade complete
            if (checkGameStatus()) {
                if (gameMode === 'PLAYER_VS_AI') {
                    startAITurn();
                } else {
                    startPlayerTurn();
                }
            }
        }, r1, c1);
    }

    function processMatchesAndCascades(isPlayer, onComplete, spawnR = null, spawnC = null) {
        let matches = detectAllMatches();
        if (matches.length === 0) {
            if (onComplete) onComplete();
            return;
        }

        playSound('pop');

        // Mark points to clear & calculate score
        let pointsToClear = new Set();
        let cascadeScore = 0;
        let synthesisToSpawn = null;

        for (let m of matches) {
            totalMatchesCount++;
            let sz = m.coords.length;
            let baseVal = CANDY_TYPES[m.type] ? CANDY_TYPES[m.type].baseScore : 30;

            if (m.isSynthesis) {
                match2Count++;
                cascadeScore += baseVal * 2.5;
                synthesisToSpawn = { outKey: m.outKey, r: m.coords[0].r, c: m.coords[0].c };
                let outDef = CANDY_TYPES[m.outKey];
                logAIMessage(`🧪 CANDY SYNTHESIS EXECUTED! Yielded ${outDef ? outDef.icon + ' ' + outDef.name : m.outKey} (+${baseVal} pts)!`);
            } else if (sz === 2) {
                match2Count++;
                cascadeScore += baseVal * 2;
            } else if (sz >= 5) {
                match5Count++;
                cascadeScore += baseVal * 3.5;
            } else if (sz === 4) {
                match4Count++;
                cascadeScore += baseVal * 2;
            } else {
                match3Count++;
                cascadeScore += baseVal;
            }

            for (let pt of m.coords) {
                pointsToClear.add(`${pt.r},${pt.c}`);
            }
        }

        // Remove matched candies
        for (let key of pointsToClear) {
            let [r, c] = key.split(',').map(Number);
            grid[r][c] = null;
        }

        // SPAWN SYNTHESIZED CANDY ON GRID!
        if (synthesisToSpawn) {
            let targetR = (spawnR !== null && grid[spawnR]) ? spawnR : synthesisToSpawn.r;
            let targetC = (spawnC !== null) ? spawnC : synthesisToSpawn.c;
            grid[targetR][targetC] = createCandy(synthesisToSpawn.outKey);
        }

        let earned = Math.round(cascadeScore * comboMultiplier);
        if (isPlayer) {
            playerScore += earned;
        } else {
            aiScore += earned;
        }
        score = playerScore + aiScore;

        comboMultiplier += 0.5;
        if (comboMultiplier > maxComboInSession) {
            maxComboInSession = comboMultiplier;
        }

        applyGravityAndRefill();
        drawBoard();

        setTimeout(() => {
            processMatchesAndCascades(isPlayer, onComplete);
        }, 300);
    }

    function checkGameStatus() {
        if (score >= targetScore) {
            currentTurn = 'LEVEL_COMPLETE';
            updateTurnUI();
            let winner = gameMode === 'PLAYER_VS_AI' ? (playerScore >= aiScore ? "👤 Player Wins!" : "🤖 AI Wins!") : "🎉 Level Complete!";
            logAIMessage(`🎉 LEVEL COMPLETE! Target of ${targetScore} reached! ${winner}`);
            alert(`🎉 LEVEL COMPLETE!\n• Player Score: ${playerScore} pts\n• AI Score: ${aiScore} pts\n• ${winner}`);
            exportAllTelemetryData();
            return false;
        }
        if (movesRemaining <= 0) {
            currentTurn = 'GAME_OVER';
            updateTurnUI();
            logAIMessage(`💥 LEVEL FAILED! Out of moves.`);
            alert(`💥 GAME OVER! Out of moves.\n• Player Score: ${playerScore} pts\n• AI Score: ${aiScore} pts.`);
            exportAllTelemetryData();
            return false;
        }
        comboMultiplier = 1;
        return true;
    }

    // =========================================================
    // HINT SYSTEM
    // =========================================================
    function triggerHint() {
        if (currentTurn !== 'PLAYER_TURN') {
            alert("Hint unavailable during AI turn!");
            return;
        }

        let validMoves = findAllValidMoves();
        if (validMoves.length === 0) {
            initBoard();
            validMoves = findAllValidMoves();
        }

        if (validMoves.length === 0) {
            alert("No valid move found!");
            return;
        }

        let best = validMoves[0];
        hintsUsed++;
        activeHintMove = best;

        grid[best.r1][best.c1].highlighted = true;
        grid[best.r2][best.c2].highlighted = true;

        const hintBannerText = document.getElementById('hint-banner-text');
        if (hintBannerText) {
            hintBannerText.innerText = `💡 Try this move! Swap (${best.r1 + 1},${best.c1 + 1}) with (${best.r2 + 1},${best.c2 + 1})`;
        }

        drawBoard();
    }

    function clearHintHighlights() {
        activeHintMove = null;
        for (let r = 0; r < GRID_SIZE; r++) {
            for (let c = 0; c < GRID_SIZE; c++) {
                if (grid[r] && grid[r][c]) {
                    grid[r][c].highlighted = false;
                }
            }
        }
    }

    // =========================================================
    // AI CANDY POINT & SYNTHESIS WORTH ANALYZER (ACCEPT / REJECT)
    // =========================================================
    function runAICandyWorthCheck(name, scoreVal, icon, formula, matchReq = 2) {
        scoreVal = parseInt(scoreVal) || 30;
        let baseYieldRatio = (scoreVal / 30).toFixed(1);
        let rating = 9.1;
        let isAccepted = true;
        let moveSavings = (scoreVal / 100 * 2.5).toFixed(1);

        // AI ACCEPT vs REJECT Logic
        if (scoreVal < 15) {
            isAccepted = false;
            rating = 4.2;
        } else if (scoreVal > 250) {
            isAccepted = false;
            rating = 5.0;
        } else {
            isAccepted = true;
            rating = Math.min(9.8, (8.0 + (scoreVal / 100)).toFixed(1));
        }

        const decisionBadge = document.getElementById('ai-decision-badge');
        const decisionText = document.getElementById('ai-decision-text');

        if (isAccepted) {
            decisionBadge.className = 'ai-decision-badge badge-accept';
            decisionText.innerText = 'AI DECISION: ✅ ACCEPTED (WORTH IT)';
        } else {
            decisionBadge.className = 'ai-decision-badge badge-reject';
            decisionText.innerText = 'AI DECISION: ❌ REJECTED (UNBALANCED)';
        }

        let comboFormula = formula || `Match ${matchReq}: 🍓 Red Berry + 🍓 Red Berry = ${icon} ${name}`;

        document.getElementById('analyzer-icon').innerText = icon || '🔻';
        document.getElementById('analyzer-target-name').innerText = name;
        document.getElementById('analyzer-target-detail').innerText = `Formula: ${comboFormula} | Base: ${scoreVal} pts`;

        const resRating = document.getElementById('res-rating');
        resRating.innerText = `${rating} / 10 (${isAccepted ? 'Worth It' : 'Rejected'})`;
        resRating.className = `res-value ${isAccepted ? 'val-green' : 'val-red'}`;

        const resMatchReq = document.getElementById('res-match-req');
        if (resMatchReq) {
            resMatchReq.innerText = `Match ${matchReq} Required`;
        }

        document.getElementById('res-efficiency').innerText = `${baseYieldRatio}x Standard Match Score Yield`;
        document.getElementById('res-move-savings').innerText = `Saves ~${moveSavings} moves to reach level target`;

        const resBal = document.getElementById('res-balance');
        if (isAccepted) {
            resBal.innerText = '✅ PERFECTLY BALANCED & WORTH IT';
            resBal.className = 'res-value val-green';
        } else if (scoreVal < 15) {
            resBal.innerText = '❌ REJECTED: UNDERPOWERED (< 15 pts yield)';
            resBal.className = 'res-value val-red';
        } else {
            resBal.innerText = '❌ REJECTED: OVERPOWERED (> 250 pts yield)';
            resBal.className = 'res-value val-red';
        }

        let verdict = `AI Algorithmic Evaluation Report for '${name}' (${icon}):\n` +
                      `• Formula: ${comboFormula}\n` +
                      `• Match Size Required: Match ${matchReq}\n` +
                      `• Score Value: ${scoreVal} pts (${baseYieldRatio}x base efficiency)\n` +
                      `• Verdict: ${isAccepted ? 'ACCEPTED! This candy synthesis provides optimal reward balance and strategy worthiness.' : 'REJECTED! Score points are unbalanced for level targets.'}`;

        document.getElementById('ai-verdict-text').innerText = verdict;
        document.getElementById('ai-analyzer-modal').classList.add('active');
    }

    // =========================================================
    // POWER BI DASHBOARD RENDERER
    // =========================================================
    function openPowerBIDashboard() {
        let totalSwaps = validSwaps + invalidSwaps;
        let accuracy = totalSwaps > 0 ? Math.round((validSwaps / totalSwaps) * 100) : 100;
        let sessID = "SESS_" + Math.random().toString(36).substring(2, 8).toUpperCase();

        document.getElementById('pbi-val-scores').innerText = `${playerScore} / ${aiScore}`;
        document.getElementById('pbi-val-moves').innerText = `${playerMovesCount} / ${aiMovesCount}`;
        document.getElementById('pbi-val-accuracy').innerText = `${accuracy}%`;
        document.getElementById('pbi-val-matches').innerText = `${match2Count} / ${match3Count} / ${match4Count} / ${match5Count}`;
        document.getElementById('pbi-val-combo').innerText = `${maxComboInSession}x`;

        // Update Bar Charts
        let totalM = totalMatchesCount || 1;
        document.getElementById('bar-m2').style.width = `${Math.min(100, Math.round((match2Count / totalM) * 100))}%`;
        document.getElementById('cnt-m2').innerText = match2Count;

        document.getElementById('bar-m3').style.width = `${Math.min(100, Math.round((match3Count / totalM) * 100))}%`;
        document.getElementById('cnt-m3').innerText = match3Count;

        document.getElementById('bar-m4').style.width = `${Math.min(100, Math.round((match4Count / totalM) * 100))}%`;
        document.getElementById('cnt-m4').innerText = match4Count;

        document.getElementById('bar-m5').style.width = `${Math.min(100, Math.round((match5Count / totalM) * 100))}%`;
        document.getElementById('cnt-m5').innerText = match5Count;

        // Accuracy Gauge
        document.getElementById('gauge-fill').style.width = `${accuracy}%`;
        document.getElementById('gauge-desc').innerText = `Swap Precision: ${accuracy}% (${validSwaps} valid, ${invalidSwaps} invalid).`;

        // Data Table
        const tbody = document.getElementById('pbi-table-body');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td>${playerName}</td>
                    <td>${sessID}</td>
                    <td>${gameMode}</td>
                    <td>${currentLevel}</td>
                    <td>${playerScore}</td>
                    <td>${aiScore}</td>
                    <td>${validSwaps}</td>
                    <td>${totalMatchesCount}</td>
                    <td>${maxComboInSession}x</td>
                    <td>${score >= targetScore ? 'COMPLETED' : 'IN_PROGRESS'}</td>
                </tr>
            `;
        }

        document.getElementById('power-bi-modal').classList.add('active');
    }

    // =========================================================
    // RENDER CANVAS
    // =========================================================
    function drawBoard() {
        const canvas = document.getElementById('game-canvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Draw grid checkerboard
        for (let r = 0; r < GRID_SIZE; r++) {
            for (let c = 0; c < GRID_SIZE; c++) {
                let x = c * CELL_PIXELS;
                let y = r * CELL_PIXELS;

                ctx.fillStyle = (r + c) % 2 === 0 ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.2)';
                ctx.fillRect(x, y, CELL_PIXELS, CELL_PIXELS);

                let candy = grid[r][c];
                if (candy) {
                    let cx = x + CELL_PIXELS / 2;
                    let cy = y + CELL_PIXELS / 2;
                    let radius = CELL_PIXELS * 0.38;

                    // Selection / Hint Highlight Glow
                    if (selectedCell && selectedCell.r === r && selectedCell.c === c) {
                        ctx.strokeStyle = '#ffffff';
                        ctx.lineWidth = 4;
                        ctx.strokeRect(x + 4, y + 4, CELL_PIXELS - 8, CELL_PIXELS - 8);
                    } else if (candy.highlighted) {
                        ctx.strokeStyle = '#ffd700';
                        ctx.lineWidth = 4;
                        ctx.strokeRect(x + 2, y + 2, CELL_PIXELS - 4, CELL_PIXELS - 4);
                    }

                    // Draw Candy Circle Body
                    ctx.beginPath();
                    ctx.arc(cx, cy, radius, 0, 2 * Math.PI);
                    ctx.fillStyle = candy.color;
                    ctx.fill();
                    ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
                    ctx.lineWidth = 2;
                    ctx.stroke();

                    // Draw Symbol Icon
                    ctx.font = '24px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillStyle = '#ffffff';
                    ctx.fillText(candy.icon, cx, cy);
                }
            }
        }
    }

    // =========================================================
    // POWER BI CSV & JSON EXPORTS
    // =========================================================
    function exportCSV(filename, csvContent) {
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
    }

    function exportJSON(filename, jsonContent) {
        const blob = new Blob([jsonContent], { type: 'application/json' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
    }

    function exportAllTelemetryData() {
        let elapsedSeconds = Math.floor((Date.now() - startTimeSeconds) / 1000);
        let winLoss = score >= targetScore ? "WIN" : "LOSS";
        let status = score >= targetScore ? "COMPLETED" : "FAILED";
        let sessID = "SESS_" + Math.random().toString(36).substring(2, 10).toUpperCase();

        // 1. player_game_data.csv
        let playerCSV = "PlayerID,GameSessionID,GameMode,Level,TotalScore,PlayerScore,AIScore,PlayerMoves,AIMoves,ValidSwaps,InvalidSwaps,Matches,Match2Count,Match3Count,Match4Count,Match5Count,ComboCount,SpecialCandiesCreated,SpecialCandiesActivated,HintsUsed,TimeSeconds,WinLoss,CompletionStatus,Timestamp\n";
        playerCSV += `${playerName},${sessID},${gameMode},${currentLevel},${score},${playerScore},${aiScore},${playerMovesCount},${aiMovesCount},${validSwaps},${invalidSwaps},${totalMatchesCount},${match2Count},${match3Count},${match4Count},${match5Count},${maxComboInSession},${specialCandiesCreated},${specialCandiesActivated},${hintsUsed},${elapsedSeconds},${winLoss},${status},${new Date().toISOString()}\n`;

        // 2. level_performance.csv
        let levelCSV = "Level,GameMode,TotalScore,PlayerScore,AIScore,TotalMatches,ComboMax,TimeSeconds,WinLoss,Timestamp\n";
        levelCSV += `${currentLevel},${gameMode},${score},${playerScore},${aiScore},${totalMatchesCount},${maxComboInSession},${elapsedSeconds},${winLoss},${new Date().toISOString()}\n`;

        // 3. ai_performance.csv
        let aiCSV = "GameSessionID,Level,AIScore,PlayerScore,AIMoves,PlayerMoves,TotalMatches,WinLoss,Timestamp\n";
        aiCSV += `${sessID},${currentLevel},${aiScore},${playerScore},${aiMovesCount},${playerMovesCount},${totalMatchesCount},${winLoss},${new Date().toISOString()}\n`;

        // 4. player_game_data.json
        let telemetryObj = {
            PlayerID: playerName,
            GameSessionID: sessID,
            GameMode: gameMode,
            Level: currentLevel,
            TotalScore: score,
            PlayerScore: playerScore,
            AIScore: aiScore,
            PlayerMoves: playerMovesCount,
            AIMoves: aiMovesCount,
            ValidSwaps: validSwaps,
            InvalidSwaps: invalidSwaps,
            Matches: totalMatchesCount,
            Match2Count: match2Count,
            Match3Count: match3Count,
            Match4Count: match4Count,
            Match5Count: match5Count,
            ComboCount: maxComboInSession,
            SpecialCandiesCreated: specialCandiesCreated,
            SpecialCandiesActivated: specialCandiesActivated,
            HintsUsed: hintsUsed,
            TimeSeconds: elapsedSeconds,
            WinLoss: winLoss,
            CompletionStatus: status,
            Timestamp: new Date().toISOString()
        };

        exportCSV('player_game_data.csv', playerCSV);
        exportCSV('level_performance.csv', levelCSV);
        if (gameMode === 'PLAYER_VS_AI') exportCSV('ai_performance.csv', aiCSV);
        exportJSON('player_game_data.json', JSON.stringify(telemetryObj, null, 2));
    }

    // =========================================================
    // UI HELPERS & LISTENERS
    // =========================================================
    function updateDashboard() {
        document.getElementById('display-player-name').innerText = playerName;
        document.getElementById('display-level-num').innerText = currentLevel;
        
        // SEPARATE PLAYER & AI SCORE DISPLAYS
        document.getElementById('display-player-score').innerText = playerScore;
        document.getElementById('display-ai-score').innerText = aiScore;
        
        const aiScoreDashPill = document.getElementById('ai-score-dash-pill');
        if (aiScoreDashPill) {
            aiScoreDashPill.style.display = gameMode === 'PLAYER_VS_AI' ? 'flex' : 'none';
        }

        const panelPScore = document.getElementById('ai-panel-p-score');
        if (panelPScore) panelPScore.innerText = playerScore;

        const panelAScore = document.getElementById('ai-panel-a-score');
        if (panelAScore) panelAScore.innerText = aiScore;

        // ADAPTIVE AI LEARNING PANEL UPDATES
        const patEl = document.getElementById('ai-learned-pattern');
        if (patEl) patEl.innerText = playerLearnedStrategy;

        const canEl = document.getElementById('ai-learned-candy');
        if (canEl) canEl.innerText = playerFavCandyName;

        const levEl = document.getElementById('ai-adaptation-level');
        if (levEl) levEl.innerText = `Level ${Math.min(10, playerMovesCount + 1)} (${playerMovesCount} Swaps Learned)`;

        document.getElementById('display-target').innerText = targetScore;
        document.getElementById('display-moves').innerText = movesRemaining;

        // REAL-TIME MATCH COUNTER UPDATES
        const totMatchesEl = document.getElementById('display-total-matches');
        if (totMatchesEl) totMatchesEl.innerText = totalMatchesCount;

        const statTotEl = document.getElementById('stat-total-matches');
        if (statTotEl) statTotEl.innerText = totalMatchesCount;

        const statM2El = document.getElementById('stat-m2');
        if (statM2El) statM2El.innerText = match2Count;

        const statM3El = document.getElementById('stat-m3');
        if (statM3El) statM3El.innerText = match3Count;

        const statM4El = document.getElementById('stat-m4');
        if (statM4El) statM4El.innerText = match4Count;

        const statM5El = document.getElementById('stat-m5');
        if (statM5El) statM5El.innerText = match5Count;
    }

    function logAIMessage(msg) {
        const box = document.getElementById('ai-log-box');
        if (box) {
            let div = document.createElement('div');
            div.className = 'ai-log-entry';
            div.innerText = msg;
            box.appendChild(div);
            box.scrollTop = box.scrollHeight;
        }
    }

    function renderResearchBook(query = '') {
        const grid = document.getElementById('candy-grid-discoveries');
        if (!grid) return;
        grid.innerHTML = '';

        let q = query.toLowerCase().trim();

        for (let key in CANDY_TYPES) {
            let c = CANDY_TYPES[key];
            if (q && !c.name.toLowerCase().includes(q) && !c.formula.toLowerCase().includes(q)) {
                continue;
            }
            let card = document.createElement('div');
            card.className = 'candy-item-card';
            card.innerHTML = `<div style="font-size:22px;">${c.icon}</div><div>${c.name}</div><div style="color:#ffd700;">${c.baseScore} pts</div>`;
            card.addEventListener('click', () => {
                selectedCandyKeyForAnalysis = key;
                runAICandyWorthCheck(c.name, c.baseScore, c.icon, c.formula, c.matchSize || 3);
            });
            grid.appendChild(card);
        }

        const rulesBox = document.getElementById('custom-rules-list');
        if (rulesBox) {
            rulesBox.innerHTML = '';
            for (let r of SYNTHESIS_RULES) {
                if (q && !r.formula.toLowerCase().includes(q) && !r.outName.toLowerCase().includes(q)) {
                    continue;
                }
                let card = document.createElement('div');
                card.className = 'rule-item-card';
                card.innerHTML = `<strong>${r.formula}</strong><br><small>Yields: ${r.outName} (+${r.scoreVal} pts)</small>`;
                card.addEventListener('click', () => {
                    runAICandyWorthCheck(r.outName, r.scoreVal, r.icon, r.formula, r.matchSize || 2);
                });
                rulesBox.appendChild(card);
            }
        }
    }

    function render20LevelsModal() {
        const grid = document.getElementById('level-cards-grid');
        if (!grid) return;
        grid.innerHTML = '';

        for (let i = 1; i <= 20; i++) {
            let cfg = LEVELS_CONFIG[i];
            let card = document.createElement('div');
            card.className = 'lvl-card';
            card.innerHTML = `<h4>Level ${i}</h4><p>${cfg.tier}</p><p>Target: ${cfg.target}</p><p>Moves: ${cfg.moves}</p>`;
            card.addEventListener('click', () => {
                startLevel(i);
                document.getElementById('level-select-modal').classList.remove('active');
            });
            grid.appendChild(card);
        }
    }

    function startLevel(lvlNum) {
        currentLevel = lvlNum;
        let cfg = LEVELS_CONFIG[lvlNum] || LEVELS_CONFIG[1];
        targetScore = cfg.target;
        movesRemaining = cfg.moves;
        score = 0;
        playerScore = 0;
        aiScore = 0;
        movesUsed = 0;
        comboMultiplier = 1;
        maxComboInSession = 1;
        match2Count = 0;
        match3Count = 0;
        match4Count = 0;
        match5Count = 0;
        totalMatchesCount = 0;
        validSwaps = 0;
        invalidSwaps = 0;
        playerMovesCount = 0;
        aiMovesCount = 0;
        playerFavCandyCounts = {};
        playerMatchSizeCounts = { 2: 0, 3: 0, 4: 0, 5: 0 };
        playerLearnedStrategy = 'Observing Player...';
        playerFavCandyName = 'None';
        startTimeSeconds = Date.now();

        initBoard();
        startPlayerTurn();

        // Switch to Game Screen
        document.getElementById('welcome-screen').classList.remove('active');
        document.getElementById('game-screen').classList.add('active');

        drawBoard();
    }

    // Canvas Mouse & Touch Click Handling with accurate viewport scaling
    function setupCanvasListeners() {
        const canvas = document.getElementById('game-canvas');
        if (!canvas) return;

        function handleCanvasPointer(e) {
            if (currentTurn !== 'PLAYER_TURN' || !playerInputEnabled) return;

            const rect = canvas.getBoundingClientRect();
            const scaleX = canvas.width / rect.width;
            const scaleY = canvas.height / rect.height;

            const clientX = e.clientX || (e.touches && e.touches[0] ? e.touches[0].clientX : 0);
            const clientY = e.clientY || (e.touches && e.touches[0] ? e.touches[0].clientY : 0);

            const clickX = (clientX - rect.left) * scaleX;
            const clickY = (clientY - rect.top) * scaleY;

            const c = Math.floor(clickX / CELL_PIXELS);
            const r = Math.floor(clickY / CELL_PIXELS);

            if (r < 0 || r >= GRID_SIZE || c < 0 || c >= GRID_SIZE) return;

            if (!selectedCell) {
                selectedCell = { r: r, c: c };
            } else {
                if (selectedCell.r === r && selectedCell.c === c) {
                    selectedCell = null;
                } else if (areAdjacent(selectedCell.r, selectedCell.c, r, c)) {
                    let r1 = selectedCell.r;
                    let c1 = selectedCell.c;
                    selectedCell = null;
                    handlePlayerSwapAttempt(r1, c1, r, c);
                } else {
                    selectedCell = { r: r, c: c };
                }
            }
            drawBoard();
        }

        canvas.addEventListener('click', handleCanvasPointer);
    }

    // EXPOSE GLOBAL UI HANDLERS (Guarantees buttons work immediately!)
    window.startGameFromUI = function() {
        let nameInput = document.getElementById('player-name-input');
        playerName = (nameInput && nameInput.value.trim()) ? nameInput.value.trim() : 'Player1';

        let radios = document.getElementsByName('game-mode-radio');
        for (let r of radios) {
            if (r.checked) gameMode = r.value;
        }

        let poolSelect = document.getElementById('candy-pool-select');
        if (poolSelect) {
            candyPoolCount = parseInt(poolSelect.value) || 7;
        }

        startLevel(1);
    };

    window.openLevelSelectFromUI = function() {
        render20LevelsModal();
        const modal = document.getElementById('level-select-modal');
        if (modal) modal.classList.add('active');
    };


    window.toggleSoundFromUI = function() {
        soundEnabled = !soundEnabled;
        const soundBtn = document.getElementById('btn-toggle-sound');
        if (soundBtn) {
            soundBtn.innerText = soundEnabled ? '🔊 SOUND: ON' : '🔇 SOUND: OFF';
        }
    };

    // Safe Event Listener Helper
    function safeAddListener(id, event, handler) {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener(event, handler);
        }
    }

    // Global Initialization
    function initApp() {

        setupCanvasListeners();
        renderResearchBook();
        render20LevelsModal();

        // RESEARCH BOOK TAB BUTTON SWITCHING (Candies vs Combinations)
        const tabCandies = document.getElementById('tab-btn-discoveries');
        const tabCombinations = document.getElementById('tab-btn-rules');
        const contentCandies = document.getElementById('tab-discoveries');
        const contentCombinations = document.getElementById('tab-rules');

        if (tabCandies && tabCombinations) {
            tabCandies.addEventListener('click', () => {
                tabCandies.classList.add('active');
                tabCombinations.classList.remove('active');
                if (contentCandies) contentCandies.classList.add('active');
                if (contentCombinations) contentCombinations.classList.remove('active');
            });

            tabCombinations.addEventListener('click', () => {
                tabCombinations.classList.add('active');
                tabCandies.classList.remove('active');
                if (contentCombinations) contentCombinations.classList.add('active');
                if (contentCandies) contentCandies.classList.remove('active');
            });
        }

        // RESEARCH BOOK LIVE SEARCH FILTER
        safeAddListener('search-research-input', 'input', (e) => {
            renderResearchBook(e.target.value);
        });

        // Match Size Selector Listener
        safeAddListener('synth-match-size', 'change', () => {
            const synthMatchSizeSelect = document.getElementById('synth-match-size');
            if (!synthMatchSizeSelect) return;
            let count = parseInt(synthMatchSizeSelect.value) || 2;
            const in3 = document.getElementById('synth-in3');
            const p3 = document.getElementById('plus-sign-3');
            const in4 = document.getElementById('synth-in4');
            const p4 = document.getElementById('plus-sign-4');
            const in5 = document.getElementById('synth-in5');
            const p5 = document.getElementById('plus-sign-5');

            if (in3) in3.style.display = count >= 3 ? 'inline-block' : 'none';
            if (p3) p3.style.display = count >= 3 ? 'inline-block' : 'none';
            if (in4) in4.style.display = count >= 4 ? 'inline-block' : 'none';
            if (p4) p4.style.display = count >= 4 ? 'inline-block' : 'none';
            if (in5) in5.style.display = count >= 5 ? 'inline-block' : 'none';
            if (p5) p5.style.display = count >= 5 ? 'inline-block' : 'none';
        });

        // Start Game Button
        safeAddListener('btn-start-game', 'click', window.startGameFromUI);

        // Sound Toggle Button
        safeAddListener('btn-toggle-sound', 'click', window.toggleSoundFromUI);


        // Add Candy Modal Buttons
        safeAddListener('btn-add-candy', 'click', () => {
            const modal = document.getElementById('create-candy-modal');
            if (modal) modal.classList.add('active');
        });

        // Add Custom Rule Modal Buttons
        safeAddListener('btn-custom-rule', 'click', () => {
            const modal = document.getElementById('custom-rule-modal');
            if (modal) modal.classList.add('active');
        });

        // Save New Synthesized Candy
        safeAddListener('btn-save-new-candy', 'click', () => {
            let matchSize = parseInt(document.getElementById('synth-match-size').value) || 2;
            let in1 = document.getElementById('synth-in1').value;
            let in2 = document.getElementById('synth-in2').value;
            let in3 = document.getElementById('synth-in3').value;
            let in4 = document.getElementById('synth-in4').value;
            let in5 = document.getElementById('synth-in5').value;

            let name = document.getElementById('candy-name-input').value.trim();
            let icon = document.getElementById('candy-icon-select').value;
            let scoreVal = parseInt(document.getElementById('candy-score-input').value) || 100;

            if (!name) {
                alert("Candy name cannot be empty!");
                return;
            }

            let in1Obj = CANDY_TYPES[in1] || CANDY_TYPES.RED;
            let in2Obj = CANDY_TYPES[in2] || CANDY_TYPES.RED;
            let formulaStr = `Match ${matchSize}: ${in1Obj.icon} ${in1Obj.name} + ${in2Obj.icon} ${in2Obj.name}`;

            if (matchSize >= 3) {
                let in3Obj = CANDY_TYPES[in3] || CANDY_TYPES.BLUE;
                formulaStr += ` + ${in3Obj.icon} ${in3Obj.name}`;
            }
            if (matchSize >= 4) {
                let in4Obj = CANDY_TYPES[in4] || CANDY_TYPES.GREEN;
                formulaStr += ` + ${in4Obj.icon} ${in4Obj.name}`;
            }
            if (matchSize >= 5) {
                let in5Obj = CANDY_TYPES[in5] || CANDY_TYPES.YELLOW;
                formulaStr += ` + ${in5Obj.icon} ${in5Obj.name}`;
            }

            formulaStr += ` => ${icon} ${name}`;

            let key = 'SYNTH_' + Date.now();
            let customColor = ICON_COLOR_MAP[icon] || '#ba55d3';

            CANDY_TYPES[key] = {
                name: name,
                color: customColor,
                topColor: '#ffffff',
                icon: icon,
                baseScore: scoreVal,
                formula: formulaStr,
                matchSize: matchSize
            };

            SYNTHESIS_RULES.push({
                in1: in1, in2: in2, matchSize: matchSize, outName: name, outKey: key, icon: icon, scoreVal: scoreVal, formula: formulaStr
            });

            let r = Math.floor(Math.random() * GRID_SIZE);
            let c = Math.floor(Math.random() * GRID_SIZE);
            grid[r][c] = createCandy(key);

            renderResearchBook();
            drawBoard();
            const modal = document.getElementById('create-candy-modal');
            if (modal) modal.classList.remove('active');
            logAIMessage(`✨ Synthesized '${name}' (${icon} ${formulaStr}) valued at ${scoreVal} pts!`);
            alert(`✅ Synthesis Complete: ${formulaStr} added to game board!`);
        });

        // Save Custom Rule Button
        safeAddListener('btn-save-custom-rule', 'click', () => {
            let name = document.getElementById('rule-name-input').value.trim();
            let cond = document.getElementById('rule-cond-input').value.trim();
            let eff = document.getElementById('rule-effect-input').value.trim();

            if (!name) {
                alert("Rule name cannot be empty!");
                return;
            }

            CUSTOM_RULES.push({ name: name, condition: cond, effect: eff });
            renderResearchBook();
            const modal = document.getElementById('custom-rule-modal');
            if (modal) modal.classList.remove('active');
            logAIMessage(`📜 Created custom rule '${name}'!`);
            alert(`✅ Custom Rule '${name}' saved successfully!`);
        });

        // AI Analyzer Check Buttons
        safeAddListener('btn-analyze-selected', 'click', () => {
            let c = CANDY_TYPES[selectedCandyKeyForAnalysis] || CANDY_TYPES.RUBY;
            runAICandyWorthCheck(c.name, c.baseScore, c.icon, c.formula, c.matchSize || 2);
        });

        safeAddListener('btn-check-candy-worth', 'click', () => {
            let matchSize = parseInt(document.getElementById('synth-match-size').value) || 2;
            let in1 = document.getElementById('synth-in1').value;
            let in2 = document.getElementById('synth-in2').value;
            let in3 = document.getElementById('synth-in3').value;
            let in4 = document.getElementById('synth-in4').value;
            let in5 = document.getElementById('synth-in5').value;

            let in1Obj = CANDY_TYPES[in1] || CANDY_TYPES.RED;
            let in2Obj = CANDY_TYPES[in2] || CANDY_TYPES.RED;

            let name = document.getElementById('candy-name-input').value.trim() || "Ruby Candy";
            let icon = document.getElementById('candy-icon-select').value;
            let scoreVal = parseInt(document.getElementById('candy-score-input').value) || 100;
            let formulaStr = `Match ${matchSize}: ${in1Obj.icon} ${in1Obj.name} + ${in2Obj.icon} ${in2Obj.name}`;

            if (matchSize >= 3) {
                let in3Obj = CANDY_TYPES[in3] || CANDY_TYPES.BLUE;
                formulaStr += ` + ${in3Obj.icon} ${in3Obj.name}`;
            }
            if (matchSize >= 4) {
                let in4Obj = CANDY_TYPES[in4] || CANDY_TYPES.GREEN;
                formulaStr += ` + ${in4Obj.icon} ${in4Obj.name}`;
            }
            if (matchSize >= 5) {
                let in5Obj = CANDY_TYPES[in5] || CANDY_TYPES.YELLOW;
                formulaStr += ` + ${in5Obj.icon} ${in5Obj.name}`;
            }
            formulaStr += ` => ${icon} ${name}`;

            runAICandyWorthCheck(name, scoreVal, icon, formulaStr, matchSize);
        });

        safeAddListener('btn-check-rule-worth', 'click', () => {
            let name = document.getElementById('rule-name-input').value.trim() || "Custom Rule";
            let cond = document.getElementById('rule-cond-input').value.trim();
            let eff = document.getElementById('rule-effect-input').value.trim();
            let formulaStr = `${cond} => ${eff}`;

            runAICandyWorthCheck(name, 100, '📜', formulaStr, 2);
        });

        // Power BI Dashboard Button
        safeAddListener('btn-power-bi-dash', 'click', openPowerBIDashboard);
        safeAddListener('pbi-btn-csv', 'click', exportAllTelemetryData);
        safeAddListener('pbi-btn-json', 'click', exportAllTelemetryData);

        // Level Select Button
        safeAddListener('btn-open-level-select', 'click', () => {
            const modal = document.getElementById('level-select-modal');
            if (modal) modal.classList.add('active');
        });

        // Close Modals
        document.querySelectorAll('.close-modal').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.modal').forEach(m => m.classList.remove('active'));
            });
        });

        // Hint Button
        safeAddListener('btn-ai-hint', 'click', triggerHint);

        // Export Buttons
        safeAddListener('btn-export-csv', 'click', exportAllTelemetryData);
        safeAddListener('btn-export-json', 'click', exportAllTelemetryData);

        // Reset & Menu
        safeAddListener('btn-reset', 'click', () => startLevel(currentLevel));
    }

    if (document.readyState === 'complete' || document.readyState === 'interactive') {
        setTimeout(initApp, 1);
    } else {
        window.addEventListener('DOMContentLoaded', initApp);
    }

})();


