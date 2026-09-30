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
    private val foodProvider: ((List<Position>, Int, Int) -> Position?)? = null,
    private val initialSpecialItemProvider: ((GameConfig) -> SpecialItem?)? = null,
    private val specialItemProvider: ((GameState) -> SpecialItem?)? = null,
    private val spawnCooldownSupplier: (() -> Int)? = null,
    private val itemLifetimeSupplier: (() -> Int)? = null
) {
    private var pendingDirections = ArrayDeque<Direction>()
    private var spawnCooldownTicks = initialSpawnCooldown()

    private val _gameState = MutableStateFlow(
        createInitialState(GameConfig(), initialHighScore)
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private fun initialSpawnCooldown(): Int {
        return spawnCooldownSupplier?.invoke() ?: (15 + random.nextInt(15))
    }

    private fun defaultItemLifetime(): Int {
        return itemLifetimeSupplier?.invoke() ?: 35
    }

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
        val initialSpecial = initialSpecialItemProvider?.invoke(config)

        return GameState(
            snake = initialSnake,
            direction = Direction.RIGHT,
            food = initialFood,
            specialItem = initialSpecial,
            activeEffects = emptyList(),
            score = 0,
            highScore = highScore,
            status = GameStatus.MENU,
            config = config,
            isNewHighScore = false
        )
    }

    fun startNewGame(config: GameConfig = _gameState.value.config) {
        pendingDirections.clear()
        spawnCooldownTicks = initialSpawnCooldown()
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
        val initialSpecial = initialSpecialItemProvider?.invoke(config)

        _gameState.value = GameState(
            snake = initialSnake,
            direction = Direction.RIGHT,
            food = initialFood,
            specialItem = initialSpecial,
            activeEffects = emptyList(),
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

        var newScore = if (isEatingFood) current.score + 10 else current.score

        // 4. Check Special Item collision
        val currentSpecial = current.specialItem
        val isEatingSpecial = (currentSpecial != null && newHead == currentSpecial.position)

        val updatedEffects = current.activeEffects
            .map { it.copy(remainingTicks = it.remainingTicks - 1) }
            .filter { it.remainingTicks > 0 }
            .toMutableList()

        var nextSpecialItem: SpecialItem? = currentSpecial

        if (isEatingSpecial) {
            when (currentSpecial.type) {
                ItemEffectType.INSTANT_DEATH -> {
                    handleGameOver(current.copy(snake = newSnake, direction = nextDirection))
                    return
                }
                ItemEffectType.SLOW -> {
                    val duration = 40
                    updatedEffects.removeAll { it.type == ItemEffectType.SLOW }
                    updatedEffects.add(ActiveEffect(ItemEffectType.SLOW, duration, duration))
                }
                ItemEffectType.SPEED_UP -> {
                    val duration = 35
                    updatedEffects.removeAll { it.type == ItemEffectType.SPEED_UP }
                    updatedEffects.add(ActiveEffect(ItemEffectType.SPEED_UP, duration, duration))
                }
                ItemEffectType.BONUS_POINTS -> {
                    newScore += 30
                }
                ItemEffectType.POINT_MINUS -> {
                    newScore = maxOf(0, newScore - 15)
                }
            }
            nextSpecialItem = null
            spawnCooldownTicks = initialSpawnCooldown()
        } else if (nextSpecialItem != null) {
            // Count down item lifetime on board
            val remaining = nextSpecialItem.remainingTicks - 1
            if (remaining <= 0) {
                nextSpecialItem = null
                spawnCooldownTicks = initialSpawnCooldown()
            } else {
                nextSpecialItem = nextSpecialItem.copy(remainingTicks = remaining)
            }
        } else {
            // Special item is not on board; count down spawn cooldown
            spawnCooldownTicks--
            if (spawnCooldownTicks <= 0) {
                nextSpecialItem = if (specialItemProvider != null) {
                    specialItemProvider.invoke(current)
                } else {
                    spawnRandomSpecialItem(newSnake, current.food, config.gridWidth, config.gridHeight)
                }
                spawnCooldownTicks = initialSpawnCooldown()
            }
        }

        val isNewHigh = newScore > current.highScore
        val newHighScore = maxOf(current.highScore, newScore)

        val newFood = if (isEatingFood) {
            spawnFood(newSnake, config.gridWidth, config.gridHeight, nextSpecialItem?.position) ?: current.food
        } else {
            current.food
        }

        _gameState.value = current.copy(
            snake = newSnake,
            direction = nextDirection,
            food = newFood,
            specialItem = nextSpecialItem,
            activeEffects = updatedEffects,
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
            status = GameStatus.MENU,
            specialItem = null,
            activeEffects = emptyList()
        )
    }

    fun getTickIntervalMs(): Long {
        val current = _gameState.value
        val diff = current.config.difficulty
        val points = current.score / 10
        val reduction = points * diff.speedIncrement
        val baseInterval = maxOf(diff.minTickMs, diff.initialTickMs - reduction)

        var speedMultiplier = 1.0f
        if (current.activeEffects.any { it.type == ItemEffectType.SLOW }) {
            speedMultiplier *= 1.6f
        }
        if (current.activeEffects.any { it.type == ItemEffectType.SPEED_UP }) {
            speedMultiplier *= 0.6f
        }

        val calculated = (baseInterval * speedMultiplier).toLong()
        return maxOf(30L, calculated)
    }

    private fun spawnFood(
        snake: List<Position>,
        gridWidth: Int,
        gridHeight: Int,
        blockedPosition: Position? = null
    ): Position? {
        if (foodProvider != null) {
            return foodProvider.invoke(snake, gridWidth, gridHeight)
        }
        val blocked = snake.toMutableSet()
        if (blockedPosition != null) {
            blocked.add(blockedPosition)
        }
        val emptyCells = mutableListOf<Position>()
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val pos = Position(x, y)
                if (pos !in blocked) {
                    emptyCells.add(pos)
                }
            }
        }
        if (emptyCells.isEmpty()) return null
        val randomIndex = random.nextInt(emptyCells.size)
        return emptyCells[randomIndex]
    }

    private fun spawnRandomSpecialItem(
        snake: List<Position>,
        food: Position,
        gridWidth: Int,
        gridHeight: Int
    ): SpecialItem? {
        val blocked = (snake + food).toSet()
        val emptyCells = mutableListOf<Position>()
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val pos = Position(x, y)
                if (pos !in blocked) {
                    emptyCells.add(pos)
                }
            }
        }
        if (emptyCells.isEmpty()) return null
        val randomPos = emptyCells[random.nextInt(emptyCells.size)]
        val types = ItemEffectType.entries
        val randomType = types[random.nextInt(types.size)]
        val lifetime = defaultItemLifetime()
        return SpecialItem(
            position = randomPos,
            type = randomType,
            remainingTicks = lifetime,
            maxTicks = lifetime
        )
    }
}
