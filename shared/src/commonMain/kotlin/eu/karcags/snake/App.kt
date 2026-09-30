package eu.karcags.snake

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import eu.karcags.snake.game.SnakeGameEngine
import eu.karcags.snake.model.GameStatus
import eu.karcags.snake.ui.GameScreen
import eu.karcags.snake.ui.MainMenuScreen
import eu.karcags.snake.ui.SnakeTheme

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    val engine = remember { SnakeGameEngine() }
    val gameState by engine.gameState.collectAsState()

    SnakeTheme {
        when (gameState.status) {
            GameStatus.MENU -> {
                MainMenuScreen(
                    highScore = gameState.highScore,
                    initialConfig = gameState.config,
                    onStartGame = { config ->
                        engine.startNewGame(config)
                    },
                    modifier = modifier
                )
            }
            GameStatus.PLAYING,
            GameStatus.PAUSED,
            GameStatus.GAME_OVER -> {
                GameScreen(
                    engine = engine,
                    gameState = gameState,
                    onMenuClick = {
                        engine.returnToMenu()
                    },
                    modifier = modifier
                )
            }
        }
    }
}