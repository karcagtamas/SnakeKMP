package eu.karcags.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.karcags.snake.model.Direction
import eu.karcags.snake.model.GameState
import eu.karcags.snake.model.GameStatus

@Composable
fun HudView(
    gameState: GameState,
    onPauseToggle: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, SnakeColors.Border, RoundedCornerShape(12.dp)),
        color = SnakeColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score and High Score
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "SCORE",
                        color = SnakeColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${gameState.score}",
                        color = SnakeColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column {
                    Text(
                        text = "BEST 🏆",
                        color = SnakeColors.AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${gameState.highScore}",
                        color = SnakeColors.AccentGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Difficulty badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SnakeColors.SurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = gameState.config.difficulty.label.uppercase(),
                        color = SnakeColors.Secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controls buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPauseToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnakeColors.SurfaceVariant,
                        contentColor = SnakeColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (gameState.status == GameStatus.PAUSED) "▶ Resume" else "⏸ Pause",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onMenuClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Menu", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DpadControls(
    onDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // UP
        DpadButton(
            label = "▲",
            onClick = { onDirection(Direction.UP) }
        )

        // LEFT, CENTER, RIGHT
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DpadButton(
                label = "◀",
                onClick = { onDirection(Direction.LEFT) }
            )
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SnakeColors.SurfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎮", fontSize = 18.sp)
            }
            DpadButton(
                label = "▶",
                onClick = { onDirection(Direction.RIGHT) }
            )
        }

        // DOWN
        DpadButton(
            label = "▼",
            onClick = { onDirection(Direction.DOWN) }
        )
    }
}

@Composable
private fun DpadButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SnakeColors.Surface)
            .border(1.5.dp, SnakeColors.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = SnakeColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnakeColors.OverlayBackground),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, SnakeColors.Border, RoundedCornerShape(16.dp)),
            color = SnakeColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = SnakeColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SnakeColors.Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Resume Game", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SnakeColors.SurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Restart", color = SnakeColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Main Menu", color = SnakeColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun GameOverOverlay(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnakeColors.OverlayBackground),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, SnakeColors.GameOverRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            color = SnakeColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GAME OVER",
                    color = SnakeColors.GameOverRed,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                if (gameState.isNewHighScore) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SnakeColors.AccentGold.copy(alpha = 0.2f))
                            .border(1.dp, SnakeColors.AccentGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🎉 NEW HIGH SCORE! 🎉",
                            color = SnakeColors.AccentGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Final Score breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SnakeColors.GridBackground)
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "FINAL SCORE",
                        color = SnakeColors.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${gameState.score}",
                        color = SnakeColors.TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Best: ${gameState.highScore}",
                        color = SnakeColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SnakeColors.Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Main Menu", color = SnakeColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
