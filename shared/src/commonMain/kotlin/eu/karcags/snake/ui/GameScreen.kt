package eu.karcags.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import eu.karcags.snake.game.SnakeGameEngine
import eu.karcags.snake.model.Direction
import eu.karcags.snake.model.GameState
import eu.karcags.snake.model.GameStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GameScreen(
    engine: SnakeGameEngine,
    gameState: GameState,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    // Game loop tick effect
    LaunchedEffect(gameState.status, gameState.score) {
        if (gameState.status == GameStatus.PLAYING) {
            while (isActive) {
                val interval = engine.getTickIntervalMs()
                delay(interval.milliseconds)
                engine.tick()
            }
        }
    }

    // Auto-focus keyboard handler
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnakeColors.Background)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                keyEvent.type == KeyEventType.KeyDown && when (keyEvent.key) {
                    Key.DirectionUp, Key.W -> {
                        engine.changeDirection(Direction.UP)
                        true
                    }

                    Key.DirectionDown, Key.S -> {
                        engine.changeDirection(Direction.DOWN)
                        true
                    }

                    Key.DirectionLeft, Key.A -> {
                        engine.changeDirection(Direction.LEFT)
                        true
                    }

                    Key.DirectionRight, Key.D -> {
                        engine.changeDirection(Direction.RIGHT)
                        true
                    }

                    Key.Spacebar, Key.P -> {
                        if (gameState.status == GameStatus.GAME_OVER) {
                            engine.startNewGame()
                        } else {
                            engine.togglePause()
                        }
                        true
                    }

                    Key.Escape -> {
                        engine.returnToMenu()
                        onMenuClick()
                        true
                    }

                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val availableHeight = maxHeight
            val availableWidth = maxWidth

            // Calculate responsive game board size
            val isCompactScreen = availableHeight < 650.dp
            val boardMaxSize = minOf(
                availableWidth - 24.dp,
                if (isCompactScreen) availableHeight - 220.dp else availableHeight - 280.dp,
                520.dp
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top HUD
                HudView(
                    gameState = gameState,
                    onPauseToggle = { engine.togglePause() },
                    onMenuClick = {
                        engine.returnToMenu()
                        onMenuClick()
                    }
                )

                // Game Board Canvas
                GameBoard(
                    gameState = gameState,
                    modifier = Modifier.size(boardMaxSize)
                )

                // On-screen D-Pad Controls
                DpadControls(
                    onDirection = { dir ->
                        engine.changeDirection(dir)
                        focusRequester.requestFocus()
                    }
                )
            }
        }

        // Overlay states
        when (gameState.status) {
            GameStatus.PAUSED -> {
                PauseOverlay(
                    onResume = { engine.resume() },
                    onRestart = { engine.startNewGame() },
                    onMenu = {
                        engine.returnToMenu()
                        onMenuClick()
                    }
                )
            }
            GameStatus.GAME_OVER -> {
                GameOverOverlay(
                    gameState = gameState,
                    onPlayAgain = { engine.startNewGame() },
                    onMenu = {
                        engine.returnToMenu()
                        onMenuClick()
                    }
                )
            }
            else -> Unit
        }
    }
}
