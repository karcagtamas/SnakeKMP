package eu.karcags.snake.storage

import eu.karcags.snake.model.Difficulty
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import java.util.prefs.Preferences

class JvmHighScoreStorage : HighScoreStorage {
    private val prefs: Preferences? = try {
        Preferences.userRoot().node("eu/karcags/snake/highscores")
    } catch (_: Throwable) {
        null
    }

    private val fallbackFile: File? by lazy {
        try {
            val userHome = System.getProperty("user.home") ?: "."
            val dir = File(userHome, ".snake")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            File(dir, "highscores.properties")
        } catch (_: Throwable) {
            null
        }
    }

    override fun getHighScore(difficulty: Difficulty): Int {
        val key = "highscore_${difficulty.name.lowercase()}"
        if (prefs != null) {
            try {
                val score = prefs.getInt(key, -1)
                if (score >= 0) return score
            } catch (_: Throwable) {}
        }
        return readFallback(key)
    }

    override fun saveHighScore(difficulty: Difficulty, score: Int) {
        val key = "highscore_${difficulty.name.lowercase()}"
        val current = getHighScore(difficulty)
        if (score <= current) return

        if (prefs != null) {
            try {
                prefs.putInt(key, score)
                prefs.flush()
            } catch (_: Throwable) {}
        }
        writeFallback(key, score)
    }

    private fun readFallback(key: String): Int {
        val file = fallbackFile ?: return 0
        if (!file.exists()) return 0
        return try {
            val props = Properties()
            FileInputStream(file).use { props.load(it) }
            props.getProperty(key)?.toIntOrNull() ?: 0
        } catch (_: Throwable) {
            0
        }
    }

    private fun writeFallback(key: String, score: Int) {
        val file = fallbackFile ?: return
        try {
            val props = Properties()
            if (file.exists()) {
                FileInputStream(file).use { props.load(it) }
            }
            props.setProperty(key, score.toString())
            FileOutputStream(file).use { props.store(it, "Snake High Scores") }
        } catch (_: Throwable) {}
    }
}

actual fun createDefaultHighScoreStorage(): HighScoreStorage = JvmHighScoreStorage()
