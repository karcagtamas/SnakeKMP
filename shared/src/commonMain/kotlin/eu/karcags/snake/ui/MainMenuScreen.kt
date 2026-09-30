package eu.karcags.snake.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.karcags.snake.model.Difficulty
import eu.karcags.snake.model.GameConfig

@Composable
fun MainMenuScreen(
    highScore: Int,
    initialConfig: GameConfig,
    onStartGame: (GameConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDifficulty by remember { mutableStateOf(initialConfig.difficulty) }
    var selectedGridSize by remember { mutableStateOf(initialConfig.gridWidth) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnakeColors.Background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, SnakeColors.Border, RoundedCornerShape(20.dp)),
            color = SnakeColors.Surface
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Logo & Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SnakeColors.PrimaryContainer)
                            .border(2.dp, SnakeColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐍", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "SNAKE",
                        color = SnakeColors.Primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Classic Arcade Game",
                        color = SnakeColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }

                // High score badge
                if (highScore > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SnakeColors.SurfaceVariant)
                            .border(1.dp, SnakeColors.AccentGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🏆", fontSize = 16.sp)
                            Text(
                                text = "High Score: $highScore",
                                color = SnakeColors.AccentGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Difficulty Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DIFFICULTY",
                        color = SnakeColors.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Difficulty.entries.forEach { diff ->
                            val isSelected = (diff == selectedDifficulty)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) SnakeColors.Primary else SnakeColors.SurfaceVariant
                                    )
                                    .clickable { selectedDifficulty = diff }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diff.label,
                                    color = if (isSelected) Color.Black else SnakeColors.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Grid Size Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "GRID SIZE",
                        color = SnakeColors.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    val gridOptions = listOf(15 to "15×15", 20 to "20×20", 25 to "25×25")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridOptions.forEach { (size, label) ->
                            val isSelected = (size == selectedGridSize)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) SnakeColors.Secondary else SnakeColors.SurfaceVariant
                                    )
                                    .clickable { selectedGridSize = size }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else SnakeColors.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Play Button
                Button(
                    onClick = {
                        val config = GameConfig(
                            gridWidth = selectedGridSize,
                            gridHeight = selectedGridSize,
                            difficulty = selectedDifficulty
                        )
                        onStartGame(config)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnakeColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "▶  PLAY GAME",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                // Controls Instructions & Items Legend
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SnakeColors.GridBackground)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "HOW TO PLAY",
                        color = SnakeColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Move: Arrow Keys or W / A / S / D",
                        color = SnakeColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Touch / Mouse: On-screen D-Pad",
                        color = SnakeColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Pause / Restart: Spacebar or Enter",
                        color = SnakeColors.TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "SPECIAL ITEMS (Disappear after a few seconds)",
                        color = SnakeColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ❄️ Slow: Slows down snake temporarily",
                        color = SnakeColors.PowerupSlow,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• ⭐ +30 Pts: Gives bonus score",
                        color = SnakeColors.PowerupBonus,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• ⚡ Speed Up: Temporarily speeds up snake",
                        color = SnakeColors.DowngradeSpeed,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 💀 Death: Instant game over hazard!",
                        color = SnakeColors.DowngradeDeath,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• 🔻 -15 Pts: Decreases current score",
                        color = SnakeColors.DowngradeMinus,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
