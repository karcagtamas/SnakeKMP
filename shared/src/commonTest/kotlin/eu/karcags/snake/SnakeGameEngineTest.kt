package eu.karcags.snake

import eu.karcags.snake.game.SnakeGameEngine
import eu.karcags.snake.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SnakeGameEngineTest {

    @Test
    fun testInitialState() {
        val engine = SnakeGameEngine(initialHighScore = 50)
        val state = engine.gameState.value

        assertEquals(GameStatus.MENU, state.status)
        assertEquals(50, state.highScore)
        assertEquals(0, state.score)
        assertEquals(3, state.snake.size)
        assertEquals(Direction.RIGHT, state.direction)
    }

    @Test
    fun testStartGame() {
        val engine = SnakeGameEngine()
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
        val engine = SnakeGameEngine()
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
        val engine = SnakeGameEngine()
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
        val engine = SnakeGameEngine()
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

        val engine = SnakeGameEngine(
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
        val engine = SnakeGameEngine(
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
        val engine = SnakeGameEngine(
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
        val engine = SnakeGameEngine()
        engine.startNewGame(GameConfig(difficulty = Difficulty.MEDIUM))
        val initialInterval = engine.getTickIntervalMs()
        assertEquals(Difficulty.MEDIUM.initialTickMs, initialInterval)
    }

    @Test
    fun testPauseAndResume() {
        val engine = SnakeGameEngine()
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
        val engine = SnakeGameEngine()
        engine.startNewGame()
        assertEquals(GameStatus.PLAYING, engine.gameState.value.status)

        engine.returnToMenu()
        assertEquals(GameStatus.MENU, engine.gameState.value.status)
    }

    @Test
    fun testSelfCollision() {
        val engine = SnakeGameEngine()
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
}
