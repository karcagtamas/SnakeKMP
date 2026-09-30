package eu.karcags.snake

import eu.karcags.snake.game.SnakeGameEngine
import eu.karcags.snake.model.*
import eu.karcags.snake.storage.HighScoreStorage
import eu.karcags.snake.storage.InMemoryHighScoreStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SnakeGameEngineTest {

    private fun createEngine(
        initialHighScore: Int? = null,
        highScoreStorage: HighScoreStorage = InMemoryHighScoreStorage(),
        initialSnakeProvider: ((GameConfig) -> List<Position>)? = null,
        foodProvider: ((List<Position>, Int, Int) -> Position?)? = null,
        initialSpecialItemProvider: ((GameConfig) -> SpecialItem?)? = null,
        specialItemProvider: ((GameState) -> SpecialItem?)? = null,
        spawnCooldownSupplier: (() -> Int)? = null,
        itemLifetimeSupplier: (() -> Int)? = null
    ): SnakeGameEngine {
        return SnakeGameEngine(
            initialHighScore = initialHighScore,
            highScoreStorage = highScoreStorage,
            initialSnakeProvider = initialSnakeProvider,
            foodProvider = foodProvider,
            initialSpecialItemProvider = initialSpecialItemProvider,
            specialItemProvider = specialItemProvider,
            spawnCooldownSupplier = spawnCooldownSupplier,
            itemLifetimeSupplier = itemLifetimeSupplier
        )
    }

    @Test
    fun testInitialState() {
        val engine = createEngine(initialHighScore = 50)
        val state = engine.gameState.value

        assertEquals(GameStatus.MENU, state.status)
        assertEquals(50, state.highScore)
        assertEquals(0, state.score)
        assertEquals(3, state.snake.size)
        assertEquals(Direction.RIGHT, state.direction)
    }

    @Test
    fun testStartGame() {
        val engine = createEngine()
        val config = GameConfig(gridWidth = 10, gridHeight = 10, difficulty = Difficulty.HARD)
        engine.startNewGame(config)

        val state = engine.gameState.value
        assertEquals(GameStatus.PLAYING, state.status)
        assertEquals(10, state.config.gridWidth)
        assertEquals(10, state.config.gridHeight)
        assertEquals(Difficulty.HARD, state.config.difficulty)
        assertEquals(Position(5, 5), state.snake[0])
        assertEquals(Position(4, 5), state.snake[1])
        assertEquals(Position(3, 5), state.snake[2])
    }

    @Test
    fun testSnakeMovesForward() {
        val engine = createEngine()
        val config = GameConfig(gridWidth = 10, gridHeight = 10)
        engine.startNewGame(config)

        val initialHead = engine.gameState.value.snake.first()
        engine.tick()

        val newHead = engine.gameState.value.snake.first()
        assertEquals(initialHead.x + 1, newHead.x)
        assertEquals(initialHead.y, newHead.y)
        assertEquals(3, engine.gameState.value.snake.size)
    }

    @Test
    fun testChangeDirectionAndPrevent180Turn() {
        val engine = createEngine()
        engine.startNewGame(GameConfig(10, 10))

        // Moving RIGHT initially. Trying to turn LEFT should be ignored
        engine.changeDirection(Direction.LEFT)
        engine.tick()
        assertEquals(Direction.RIGHT, engine.gameState.value.direction)

        // Turn UP is valid
        engine.changeDirection(Direction.UP)
        engine.tick()
        assertEquals(Direction.UP, engine.gameState.value.direction)

        // Now moving UP. Trying to turn DOWN should be ignored
        engine.changeDirection(Direction.DOWN)
        engine.tick()
        assertEquals(Direction.UP, engine.gameState.value.direction)
    }

    @Test
    fun testWallCollisionTriggersGameOver() {
        val engine = createEngine()
        val config = GameConfig(gridWidth = 6, gridHeight = 6)
        engine.startNewGame(config)

        // Head is at (3, 3), moving RIGHT. Grid width is 6 (valid x: 0..5)
        engine.tick() // head at (4, 3)
        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)

        engine.tick() // head at (5, 3)
        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)

        engine.tick() // head attempts (6, 3) -> Wall collision
        assertEquals(GameStatus.GAME_OVER, engine.gameState.value.status)
    }

    @Test
    fun testEatingFoodIncreasesScoreAndGrowsSnake() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        var currentFoodIndex = 0
        val foodList = listOf(Position(6, 5), Position(7, 5))

        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ ->
                foodList[currentFoodIndex++]
            }
        )
        // startNewGame calls foodProvider
        val config = GameConfig(gridWidth = 10, gridHeight = 10)
        currentFoodIndex = 0
        engine.startNewGame(config)

        assertEquals(Position(6, 5), engine.gameState.value.food)
        assertEquals(0, engine.gameState.value.score)
        assertEquals(3, engine.gameState.value.snake.size)

        // Tick to move right onto (6, 5) which is food
        engine.tick()

        val stateAfter = engine.gameState.value
        assertEquals(10, stateAfter.score)
        assertEquals(4, stateAfter.snake.size)
        assertEquals(Position(6, 5), stateAfter.snake[0])
        assertEquals(Position(5, 5), stateAfter.snake[1])
        assertEquals(Position(4, 5), stateAfter.snake[2])
        assertEquals(Position(3, 5), stateAfter.snake[3])
        assertEquals(Position(7, 5), stateAfter.food)
        assertTrue(stateAfter.isNewHighScore)
    }

    @Test
    fun testSelfCollisionWithLongSnake() {
        // Snake of length 5:
        // Head at (5, 5), (4, 5), (4, 4), (5, 4), (6, 4)
        // Snake is heading RIGHT (head at (5,5), neck at (4,5)).
        // We turn UP -> new head (5, 4) which is part of the body!
        val testSnake = listOf(
            Position(5, 5),
            Position(4, 5),
            Position(4, 4),
            Position(5, 4),
            Position(6, 4)
        )
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) }
        )
        engine.startNewGame(GameConfig(10, 10))

        // Moving RIGHT: head is at (5, 5). Turn UP: new head will be (5, 4), which is in body!
        engine.changeDirection(Direction.UP)
        engine.tick()

        assertEquals(GameStatus.GAME_OVER, engine.gameState.value.status)
    }

    @Test
    fun testRapidDirectionInputBuffering() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) }
        )
        engine.startNewGame(GameConfig(10, 10))

        // Currently moving RIGHT. User presses UP then LEFT quickly before next tick
        engine.changeDirection(Direction.UP)
        engine.changeDirection(Direction.LEFT)

        // Tick 1: Consumes UP
        engine.tick()
        assertEquals(Direction.UP, engine.gameState.value.direction)
        assertEquals(Position(5, 4), engine.gameState.value.snake.first())

        // Tick 2: Consumes LEFT
        engine.tick()
        assertEquals(Direction.LEFT, engine.gameState.value.direction)
        assertEquals(Position(4, 4), engine.gameState.value.snake.first())
    }

    @Test
    fun testGetTickIntervalMsDecreasesWithScore() {
        val engine = createEngine()
        engine.startNewGame(GameConfig(difficulty = Difficulty.MEDIUM))
        val initialInterval = engine.getTickIntervalMs()
        assertEquals(Difficulty.MEDIUM.initialTickMs, initialInterval)
    }

    @Test
    fun testPauseAndResume() {
        val engine = createEngine()
        engine.startNewGame()

        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)
        engine.togglePause()
        assertEquals(GameStatus.PAUSED, engine.gameState.value.status)

        // Ticking while paused does nothing
        val headBefore = engine.gameState.value.snake.first()
        engine.tick()
        val headAfter = engine.gameState.value.snake.first()
        assertEquals(headBefore, headAfter)

        engine.resume()
        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)
    }

    @Test
    fun testReturnToMenu() {
        val engine = createEngine()
        engine.startNewGame()
        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)

        engine.returnToMenu()
        assertEquals(GameStatus.MENU, engine.gameState.value.status)
    }

    @Test
    fun testSelfCollision() {
        val engine = createEngine()
        // Snake of length 5: loop back into itself
        engine.startNewGame(GameConfig(10, 10))
        // To collide with self, we need snake of length >= 5 or turning around
        // Length 3 cannot self-collide without 180 (which is blocked).
        // Let's verify direction buffering & moves
        engine.changeDirection(Direction.UP)
        engine.tick()
        engine.changeDirection(Direction.LEFT)
        engine.tick()
        engine.changeDirection(Direction.DOWN)
        engine.tick()
        // Head moves: (3,3) -> R:(4,3) -> U:(4,2) -> L:(3,2) -> D:(3,3) (previous tail had moved away)
        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)
    }

    @Test
    fun testSlowPowerupIncreasesTickIntervalAndExpires() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) },
            initialSpecialItemProvider = {
                SpecialItem(Position(6, 5), ItemEffectType.SLOW, remainingTicks = 20, maxTicks = 20)
            }
        )
        // Use large grid to avoid hitting walls during long tick loop
        engine.startNewGame(GameConfig(gridWidth = 100, gridHeight = 100, difficulty = Difficulty.MEDIUM))

        val normalInterval = engine.getTickIntervalMs()
        assertEquals(Difficulty.MEDIUM.initialTickMs, normalInterval)

        engine.tick() // head moves to (6, 5) -> consumes SLOW powerup!
        val stateAfterEating = engine.gameState.value
        assertEquals(1, stateAfterEating.activeEffects.size)
        assertEquals(ItemEffectType.SLOW, stateAfterEating.activeEffects.first().type)
        assertEquals(null, stateAfterEating.specialItem)

        // Slower movement => tick interval should be increased
        val slowInterval = engine.getTickIntervalMs()
        assertTrue(slowInterval > normalInterval)
        assertEquals((normalInterval * 1.6f).toLong(), slowInterval)

        // Tick down until effect expires (duration was 40 ticks)
        for (i in 1..40) {
            engine.tick()
        }
        val stateAfterExpire = engine.gameState.value
        assertTrue(stateAfterExpire.activeEffects.none { it.type == ItemEffectType.SLOW })
        assertEquals(normalInterval, engine.getTickIntervalMs())
    }

    @Test
    fun testBonusPointsPowerupAddsScore() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) },
            initialSpecialItemProvider = {
                SpecialItem(Position(6, 5), ItemEffectType.BONUS_POINTS, remainingTicks = 10, maxTicks = 10)
            }
        )
        engine.startNewGame(GameConfig(gridWidth = 10, gridHeight = 10))

        assertEquals(0, engine.gameState.value.score)
        engine.tick() // Head moves to (6, 5) eating BONUS_POINTS

        assertEquals(30, engine.gameState.value.score)
        assertEquals(30, engine.gameState.value.highScore)
        assertTrue(engine.gameState.value.isNewHighScore)
        assertEquals(3, engine.gameState.value.snake.size) // Snake did not grow
        assertEquals(null, engine.gameState.value.specialItem)
    }

    @Test
    fun testSpeedUpDowngradeDecreasesTickIntervalAndExpires() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) },
            initialSpecialItemProvider = {
                SpecialItem(Position(6, 5), ItemEffectType.SPEED_UP, remainingTicks = 15, maxTicks = 15)
            }
        )
        // Use large grid to avoid hitting walls during long tick loop
        engine.startNewGame(GameConfig(gridWidth = 100, gridHeight = 100, difficulty = Difficulty.MEDIUM))

        val normalInterval = engine.getTickIntervalMs()
        engine.tick() // Head moves to (6, 5) eating SPEED_UP

        val stateAfter = engine.gameState.value
        assertEquals(1, stateAfter.activeEffects.size)
        assertEquals(ItemEffectType.SPEED_UP, stateAfter.activeEffects.first().type)

        val fastInterval = engine.getTickIntervalMs()
        assertTrue(fastInterval < normalInterval)
        assertEquals((normalInterval * 0.6f).toLong(), fastInterval)

        // Tick down until effect expires (duration was 35 ticks)
        for (i in 1..35) {
            engine.tick()
        }
        val expiredState = engine.gameState.value
        assertTrue(expiredState.activeEffects.none { it.type == ItemEffectType.SPEED_UP })
        assertEquals(normalInterval, engine.getTickIntervalMs())
    }

    @Test
    fun testInstantDeathDowngradeTriggersGameOver() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) },
            initialSpecialItemProvider = {
                SpecialItem(Position(6, 5), ItemEffectType.INSTANT_DEATH, remainingTicks = 10, maxTicks = 10)
            }
        )
        engine.startNewGame(GameConfig(gridWidth = 10, gridHeight = 10))

        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)
        engine.tick() // Head moves to (6, 5) -> Instant death!

        assertEquals(GameStatus.GAME_OVER, engine.gameState.value.status)
    }

    @Test
    fun testPointMinusDowngradeDecreasesScore() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) },
            initialSpecialItemProvider = {
                SpecialItem(Position(6, 5), ItemEffectType.POINT_MINUS, remainingTicks = 10, maxTicks = 10)
            }
        )
        engine.startNewGame(GameConfig(gridWidth = 10, gridHeight = 10))

        // Start with 0 score -> eating POINT_MINUS keeps score at 0
        engine.tick()
        assertEquals(0, engine.gameState.value.score)

        // Test with prior points (eating food first)
        var foodIndex = 0
        val foodPositions = listOf(Position(6, 5), Position(7, 5), Position(0, 0), Position(0, 1))
        val engineWithFood = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> foodPositions[foodIndex++] },
            initialSpecialItemProvider = {
                SpecialItem(Position(8, 5), ItemEffectType.POINT_MINUS, remainingTicks = 10, maxTicks = 10)
            }
        )
        foodIndex = 0
        engineWithFood.startNewGame(GameConfig(gridWidth = 20, gridHeight = 20))
        // Tick 1: Eats food at (6, 5) -> Score = 10
        engineWithFood.tick()
        assertEquals(10, engineWithFood.gameState.value.score)

        // Tick 2: Eats food at (7, 5) -> Score = 20
        engineWithFood.tick()
        assertEquals(20, engineWithFood.gameState.value.score)

        // Tick 3: Eats POINT_MINUS at (8, 5) -> Score = 20 - 15 = 5
        engineWithFood.tick()
        assertEquals(5, engineWithFood.gameState.value.score)
    }

    @Test
    fun testSpecialItemDisappearsAfterLifetime() {
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        val engine = createEngine(
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(0, 0) },
            initialSpecialItemProvider = {
                SpecialItem(Position(2, 2), ItemEffectType.SLOW, remainingTicks = 3, maxTicks = 3)
            },
            spawnCooldownSupplier = { 100 }
        )
        engine.startNewGame(GameConfig(gridWidth = 10, gridHeight = 10))

        // Snake moves away: (5,5) -> (6,5)
        // Tick 1: item remainingTicks becomes 2
        engine.tick()
        val item1 = engine.gameState.value.specialItem
        assertTrue(item1 != null)
        assertEquals(2, item1.remainingTicks)

        // Tick 2: remainingTicks becomes 1
        engine.tick()
        val item2 = engine.gameState.value.specialItem
        assertTrue(item2 != null)
        assertEquals(1, item2.remainingTicks)

        // Tick 3: item expires (remaining <= 0) and disappears
        engine.tick()
        val item3 = engine.gameState.value.specialItem
        assertEquals(null, item3)
        assertTrue(engine.gameState.value.activeEffects.isEmpty())
    }

    @Test
    fun testHighScorePersistenceWithStorage() {
        val storage = eu.karcags.snake.storage.InMemoryHighScoreStorage(
            mapOf(Difficulty.EASY to 20, Difficulty.HARD to 50)
        )

        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))
        var foodIndex = 0
        val foods = listOf(Position(6, 5), Position(7, 5), Position(8, 5), Position(9, 5))

        val engine = SnakeGameEngine(
            highScoreStorage = storage,
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> foods[foodIndex++] }
        )

        // Starting game on EASY difficulty should load high score of 20
        foodIndex = 0
        engine.startNewGame(GameConfig(gridWidth = 20, gridHeight = 20, difficulty = Difficulty.EASY))
        assertEquals(20, engine.gameState.value.highScore)
        assertEquals(false, engine.gameState.value.isNewHighScore)

        // Tick 1: Eat food -> score = 10 (not new high score)
        engine.tick()
        assertEquals(10, engine.gameState.value.score)
        assertEquals(20, engine.gameState.value.highScore)
        assertEquals(false, engine.gameState.value.isNewHighScore)

        // Tick 2: Eat food -> score = 20 (tied, not new high score)
        engine.tick()
        assertEquals(20, engine.gameState.value.score)
        assertEquals(20, engine.gameState.value.highScore)
        assertEquals(false, engine.gameState.value.isNewHighScore)

        // Tick 3: Eat food -> score = 30 (new high score!)
        engine.tick()
        assertEquals(30, engine.gameState.value.score)
        assertEquals(30, engine.gameState.value.highScore)
        assertEquals(true, engine.gameState.value.isNewHighScore)
        assertEquals(30, storage.getHighScore(Difficulty.EASY))

        // Return to menu and check that EASY high score remains 30
        engine.returnToMenu()
        assertEquals(30, engine.getHighScore(Difficulty.EASY))
        assertEquals(50, engine.getHighScore(Difficulty.HARD))
        assertEquals(50, engine.getHighScores()[Difficulty.HARD])
    }

    @Test
    fun testHighScorePerDifficultySeparation() {
        val storage = eu.karcags.snake.storage.InMemoryHighScoreStorage()
        val testSnake = listOf(Position(5, 5), Position(4, 5), Position(3, 5))

        val engine = SnakeGameEngine(
            highScoreStorage = storage,
            initialSnakeProvider = { testSnake },
            foodProvider = { _, _, _ -> Position(6, 5) }
        )

        // Play on Medium
        engine.startNewGame(GameConfig(gridWidth = 20, gridHeight = 20, difficulty = Difficulty.MEDIUM))
        assertEquals(0, engine.gameState.value.highScore)
        engine.tick() // Eat food -> score = 10
        assertEquals(10, engine.gameState.value.score)
        assertEquals(10, storage.getHighScore(Difficulty.MEDIUM))
        assertEquals(0, storage.getHighScore(Difficulty.HARD))

        // Switch to Hard
        engine.startNewGame(GameConfig(gridWidth = 20, gridHeight = 20, difficulty = Difficulty.HARD))
        assertEquals(0, engine.gameState.value.highScore)
        assertEquals(0, engine.gameState.value.score)
        engine.tick() // Eat food -> score = 10
        assertEquals(10, storage.getHighScore(Difficulty.HARD))
    }

    @Test
    fun testInMemoryHighScoreStorageOperations() {
        val storage = eu.karcags.snake.storage.InMemoryHighScoreStorage()
        assertEquals(0, storage.getHighScore(Difficulty.EASY))
        assertEquals(0, storage.getOverallHighScore())

        storage.saveHighScore(Difficulty.EASY, 40)
        storage.saveHighScore(Difficulty.MEDIUM, 80)
        storage.saveHighScore(Difficulty.HARD, 30)

        assertEquals(40, storage.getHighScore(Difficulty.EASY))
        assertEquals(80, storage.getHighScore(Difficulty.MEDIUM))
        assertEquals(30, storage.getHighScore(Difficulty.HARD))
        assertEquals(80, storage.getOverallHighScore())

        // Saving lower score should not overwrite
        storage.saveHighScore(Difficulty.MEDIUM, 50)
        assertEquals(80, storage.getHighScore(Difficulty.MEDIUM))
    }
}
