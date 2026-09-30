package eu.karcags.snake.model

data class Position(
    val x: Int,
    val y: Int
)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean {
        return (this == UP && other == DOWN) ||
                (this == DOWN && other == UP) ||
                (this == LEFT && other == RIGHT) ||
                (this == RIGHT && other == LEFT)
    }
}

enum class Difficulty(
    val label: String,
    val initialTickMs: Long,
    val minTickMs: Long,
    val speedIncrement: Long
) {
    EASY("Easy", 180L, 100L, 2L),
    MEDIUM("Medium", 130L, 60L, 2L),
    HARD("Hard", 85L, 40L, 1L)
}

enum class ItemCategory {
    POWERUP,
    DOWNGRADE
}

enum class ItemEffectType(
    val category: ItemCategory,
    val label: String,
    val symbol: String
) {
    // Powerups
    SLOW(ItemCategory.POWERUP, "Slow", "❄️"),
    BONUS_POINTS(ItemCategory.POWERUP, "+30 Pts", "⭐"),

    // Downgrades
    SPEED_UP(ItemCategory.DOWNGRADE, "Speed Up", "⚡"),
    INSTANT_DEATH(ItemCategory.DOWNGRADE, "Death", "💀"),
    POINT_MINUS(ItemCategory.DOWNGRADE, "-15 Pts", "🔻")
}

data class SpecialItem(
    val position: Position,
    val type: ItemEffectType,
    val remainingTicks: Int,
    val maxTicks: Int
)

data class ActiveEffect(
    val type: ItemEffectType,
    val remainingTicks: Int,
    val maxTicks: Int
)

enum class GameStatus {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class GameConfig(
    val gridWidth: Int = 20,
    val gridHeight: Int = 20,
    val difficulty: Difficulty = Difficulty.MEDIUM
)

data class GameState(
    val snake: List<Position>,
    val direction: Direction,
    val food: Position,
    val specialItem: SpecialItem? = null,
    val activeEffects: List<ActiveEffect> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val status: GameStatus = GameStatus.MENU,
    val config: GameConfig = GameConfig(),
    val isNewHighScore: Boolean = false
)
