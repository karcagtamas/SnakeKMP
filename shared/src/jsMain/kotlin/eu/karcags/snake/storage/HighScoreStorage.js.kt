package eu.karcags.snake.storage

import eu.karcags.snake.model.Difficulty
import kotlinx.browser.localStorage

class JsHighScoreStorage : HighScoreStorage {
    override fun getHighScore(difficulty: Difficulty): Int {
        return try {
            val key = "snake_highscore_${difficulty.name.lowercase()}"
            localStorage.getItem(key)?.toIntOrNull() ?: 0
        } catch (_: Throwable) {
            0
        }
    }

    override fun saveHighScore(difficulty: Difficulty, score: Int) {
        try {
            val key = "snake_highscore_${difficulty.name.lowercase()}"
            val current = getHighScore(difficulty)
            if (score > current) {
                localStorage.setItem(key, score.toString())
            }
        } catch (_: Throwable) {}
    }
}

actual fun createDefaultHighScoreStorage(): HighScoreStorage = JsHighScoreStorage()
