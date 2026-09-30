package eu.karcags.snake.storage

import eu.karcags.snake.model.Difficulty

interface HighScoreStorage {
    fun getHighScore(difficulty: Difficulty): Int
    fun saveHighScore(difficulty: Difficulty, score: Int)

    fun getAllHighScores(): Map<Difficulty, Int> {
        return Difficulty.entries.associateWith { getHighScore(it) }
    }

    fun getOverallHighScore(): Int {
        return getAllHighScores().values.maxOrNull() ?: 0
    }
}

class InMemoryHighScoreStorage(
    initialScores: Map<Difficulty, Int> = emptyMap()
) : HighScoreStorage {
    private val scores = initialScores.toMutableMap()

    override fun getHighScore(difficulty: Difficulty): Int {
        return scores[difficulty] ?: 0
    }

    override fun saveHighScore(difficulty: Difficulty, score: Int) {
        val current = getHighScore(difficulty)
        if (score > current) {
            scores[difficulty] = score
        }
    }
}

expect fun createDefaultHighScoreStorage(): HighScoreStorage
