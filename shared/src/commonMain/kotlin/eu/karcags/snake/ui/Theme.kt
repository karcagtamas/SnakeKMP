package eu.karcags.snake.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object SnakeColors {
    val Background = Color(0xFF0F172A)
    val Surface = Color(0xFF1E293B)
    val SurfaceVariant = Color(0xFF334155)
    val Border = Color(0xFF475569)

    val Primary = Color(0xFF22C55E)
    val PrimaryHover = Color(0xFF16A34A)
    val PrimaryContainer = Color(0xFF14532D)

    val Secondary = Color(0xFF38BDF8)
    val AccentGold = Color(0xFFFBBF24)

    val SnakeHead = Color(0xFF4ADE80)
    val SnakeBody = Color(0xFF22C55E)
    val SnakeBodyAlt = Color(0xFF16A34A)
    val SnakeEye = Color(0xFF0F172A)

    val FoodApple = Color(0xFFEF4444)
    val FoodLeaf = Color(0xFF22C55E)
    val FoodShine = Color(0xFFFCA5A5)

    val GridLine = Color(0xFF1E293B)
    val GridBackground = Color(0xFF090D16)

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    val GameOverRed = Color(0xFFEF4444)
    val OverlayBackground = Color(0xCC090D16)

    // Powerup Colors
    val PowerupSlow = Color(0xFF38BDF8)
    val PowerupSlowGlow = Color(0x6638BDF8)
    val PowerupBonus = Color(0xFFFBBF24)
    val PowerupBonusGlow = Color(0x66FBBF24)

    // Downgrade Colors
    val DowngradeSpeed = Color(0xFFF97316)
    val DowngradeSpeedGlow = Color(0x66F97316)
    val DowngradeDeath = Color(0xFFA855F7)
    val DowngradeDeathGlow = Color(0x66A855F7)
    val DowngradeMinus = Color(0xFFEF4444)
    val DowngradeMinusGlow = Color(0x66EF4444)
}

val SnakeDarkColorScheme = darkColorScheme(
    primary = SnakeColors.Primary,
    onPrimary = Color.Black,
    primaryContainer = SnakeColors.PrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = SnakeColors.Secondary,
    onSecondary = Color.Black,
    background = SnakeColors.Background,
    onBackground = SnakeColors.TextPrimary,
    surface = SnakeColors.Surface,
    onSurface = SnakeColors.TextPrimary,
    surfaceVariant = SnakeColors.SurfaceVariant,
    onSurfaceVariant = SnakeColors.TextSecondary
)

@Composable
fun SnakeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SnakeDarkColorScheme,
        content = content
    )
}
