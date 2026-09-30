package eu.karcags.snake.game

import eu.karcags.snake.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class SnakeGameEngine(
    private val random: Random = Random.Default,
    initialHighScore: Int = 0,
    private val initialSnakeProvider: ((GameConfig) -> List<Position>)? = null,
    private val foodProvider: ((List<Position>, Int, Int) -> Position?)? = null
) {
    private var pendingDirections = ArrayDeque<Direction>()

    private val _gameState = MutableStateFlow(
        createInitialState(GameConfig(), initialHighScore)
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private fun createInitialState(config: GameConfig, highScore: Int): GameState {
        val initialSnake = initialSnakeProvider?.invoke(config) ?: run {
            val midX = config.gridWidth / 2
            val midY = config.gridHeight / 2
            listOf(
                Position(midX, midY),
                Position(midX - 1, midY),
                Position(midX - 2, midY)
            )
        }
        val initialFood = spawnFood(initialSnake, config.gridWidth, config.gridHeight)
            ?: Position((initialSnake.first().x + 4) % config.gridWidth, initialSnake.first().y)

        return GameState(
            snake = initialSnake,
            direction = Direction.RIGHT,
            food = initialFood,
            score = 0,
            highScore = highScore,
            status = GameStatus.MENU,
            config = config,
            isNewHighScore = false
        )
    }

    fun startNewGame(config: GameConfig = _gameState.value.config) {
        pendingDirections.clear()
        val initialSnake = initialSnakeProvider?.invoke(config) ?: run {
            val midX = config.gridWidth / 2
            val midY = config.gridHeight / 2
            listOf(
                Position(midX, midY),
                Position(midX - 1, midY),
                Position(midX - 2, midY)
            )
        }
        val initialFood = spawnFood(initialSnake, config.gridWidth, config.gridHeight)
            ?: Position((initialSnake.first().x + 3) % config.gridWidth, initialSnake.first().y)

        _gameState.value = GameState(
            snake = initialSnake,
            direction = Direction.RIGHT,
            food = initialFood,
            score = 0,
            highScore = _gameState.value.highScore,
            status = GameStatus.PLAYING,
            config = config,
            isNewHighScore = false
        )
    }

    fun changeDirection(newDirection: Direction) {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING) return

        val referenceDirection = if (pendingDirections.isNotEmpty()) {
            pendingDirections.last()
        } else {
            current.direction
        }

        // Ignore same or opposite directions, limit queue buffer to 2
        if (newDirection != referenceDirection && !newDirection.isOpposite(referenceDirection)) {
            if (pendingDirections.size < 2) {
                pendingDirections.addLast(newDirection)
            }
        }
    }

    fun tick() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING) return

        var nextDirection = current.direction
        while (pendingDirections.isNotEmpty()) {
            val candidate = pendingDirections.removeFirst()
            if (!candidate.isOpposite(current.direction)) {
                nextDirection = candidate
                break
            }
        }

        val head = current.snake.first()
        val newHead = Position(
            x = head.x + nextDirection.dx,
            y = head.y + nextDirection.dy
        )

        val config = current.config

        // 1. Check wall collision
        if (newHead.x < 0 || newHead.x >= config.gridWidth ||
            newHead.y < 0 || newHead.y >= config.gridHeight
        ) {
            handleGameOver(current)
            return
        }

        val isEatingFood = (newHead == current.food)

        // 2. Check self collision
        val bodyToCheck = if (isEatingFood) current.snake else current.snake.dropLast(1)
        if (newHead in bodyToCheck) {
            handleGameOver(current)
            return
        }

        // 3. Move snake
        val newSnake = if (isEatingFood) {
            listOf(newHead) + current.snake
        } else {
            listOf(newHead) + current.snake.dropLast(1)
        }

        val newScore = if (isEatingFood) current.score + 10 else current.score
        val isNewHigh = newScore > current.highScore
        val newHighScore = maxOf(current.highScore, newScore)

        val newFood = if (isEatingFood) {
            spawnFood(newSnake, config.gridWidth, config.gridHeight) ?: current.food
        } else {
            current.food
        }

        _gameState.value = current.copy(
            snake = newSnake,
            direction = nextDirection,
            food = newFood,
            score = newScore,
            highScore = newHighScore,
            isNewHighScore = isNewHigh
        )
    }

    private fun handleGameOver(current: GameState) {
        _gameState.value = current.copy(
            status = GameStatus.GAME_OVER
        )
        pendingDirections.clear()
    }

    fun togglePause() {
        val current = _gameState.value
        when (current.status) {
            GameStatus.PLAYING -> _gameState.value = current.copy(status = GameStatus.PAUSED)
            GameStatus.PAUSED -> _gameState.value = current.copy(status = GameStatus.PLAYING)
            else -> Unit
        }
    }

    fun pause() {
        val current = _gameState.value
        if (current.status == GameStatus.PLAYING) {
            _gameState.value = current.copy(status = GameStatus.PAUSED)
        }
    }

    fun resume() {
        val current = _gameState.value
        if (current.status == GameStatus.PAUSED) {
            _gameState.value = current.copy(status = GameStatus.PLAYING)
        }
    }

    fun returnToMenu() {
        val current = _gameState.value
        pendingDirections.clear()
        _gameState.value = current.copy(
            status = GameStatus.MENU
        )
    }

    fun getTickIntervalMs(): Long {
        val current = _gameState.value
        val diff = current.config.difficulty
        val points = current.score / 10
        val reduction = points * diff.speedIncrement
        return maxOf(diff.minTickMs, diff.initialTickMs - reduction)
    }

    private fun spawnFood(snake: List<Position>, gridWidth: Int, gridHeight: Int): Position? {
        if (foodProvider != null) {
            return foodProvider.invoke(snake, gridWidth, gridHeight)
        }
        val snakePositions = snake.toSet()
        val emptyCells = mutableListOf<Position>()
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val pos = Position(x, y)
                if (pos !in snakePositions) {
                    emptyCells.add(pos)
                }
            }
        }
        if (emptyCells.isEmpty()) return null
        val randomIndex = random.nextInt(emptyCells.size)
        return emptyCells[randomIndex]
    }
}
