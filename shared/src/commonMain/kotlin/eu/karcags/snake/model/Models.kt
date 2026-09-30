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
    val score: Int = 0,
    val highScore: Int = 0,
    val status: GameStatus = GameStatus.MENU,
    val config: GameConfig = GameConfig(),
    val isNewHighScore: Boolean = false
)
