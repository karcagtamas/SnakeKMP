package eu.karcags.snake.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import eu.karcags.snake.model.Direction
import eu.karcags.snake.model.GameState
import eu.karcags.snake.model.ItemEffectType
import eu.karcags.snake.model.Position
import eu.karcags.snake.model.SpecialItem

@Composable
fun GameBoard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val config = gameState.config
    val gridWidth = config.gridWidth
    val gridHeight = config.gridHeight

    Box(
        modifier = modifier
            .aspectRatio(gridWidth.toFloat() / gridHeight.toFloat())
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, SnakeColors.Border, RoundedCornerShape(12.dp))
            .background(SnakeColors.GridBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / gridWidth
            val cellHeight = size.height / gridHeight

            // Draw subtle grid lines
            drawGrid(gridWidth, gridHeight, cellWidth, cellHeight)

            // Draw food (apple)
            drawFood(gameState.food, cellWidth, cellHeight)

            // Draw special item if active on board
            gameState.specialItem?.let { item ->
                drawSpecialItem(item, cellWidth, cellHeight)
            }

            // Draw snake body & head
            drawSnake(gameState.snake, gameState.direction, cellWidth, cellHeight)
        }
    }
}

private fun DrawScope.drawGrid(
    gridWidth: Int,
    gridHeight: Int,
    cellWidth: Float,
    cellHeight: Float
) {
    for (x in 1 until gridWidth) {
        val posX = x * cellWidth
        drawLine(
            color = SnakeColors.GridLine,
            start = Offset(posX, 0f),
            end = Offset(posX, size.height),
            strokeWidth = 1f
        )
    }
    for (y in 1 until gridHeight) {
        val posY = y * cellHeight
        drawLine(
            color = SnakeColors.GridLine,
            start = Offset(0f, posY),
            end = Offset(size.width, posY),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawFood(
    food: Position,
    cellWidth: Float,
    cellHeight: Float
) {
    val centerX = food.x * cellWidth + cellWidth / 2f
    val centerY = food.y * cellHeight + cellHeight / 2f
    val radius = minOf(cellWidth, cellHeight) * 0.42f

    // Red Apple body
    drawCircle(
        color = SnakeColors.FoodApple,
        radius = radius,
        center = Offset(centerX, centerY + radius * 0.08f)
    )

    // Shine reflection
    drawCircle(
        color = SnakeColors.FoodShine,
        radius = radius * 0.28f,
        center = Offset(centerX - radius * 0.35f, centerY - radius * 0.25f)
    )

    // Green Leaf
    drawOval(
        color = SnakeColors.FoodLeaf,
        topLeft = Offset(centerX - radius * 0.1f, centerY - radius * 1.15f),
        size = Size(radius * 0.7f, radius * 0.45f)
    )
}

private fun DrawScope.drawSpecialItem(
    item: SpecialItem,
    cellWidth: Float,
    cellHeight: Float
) {
    val centerX = item.position.x * cellWidth + cellWidth / 2f
    val centerY = item.position.y * cellHeight + cellHeight / 2f
    val radius = minOf(cellWidth, cellHeight) * 0.42f

    val (mainColor, glowColor) = when (item.type) {
        ItemEffectType.SLOW -> Pair(SnakeColors.PowerupSlow, SnakeColors.PowerupSlowGlow)
        ItemEffectType.BONUS_POINTS -> Pair(SnakeColors.PowerupBonus, SnakeColors.PowerupBonusGlow)
        ItemEffectType.SPEED_UP -> Pair(SnakeColors.DowngradeSpeed, SnakeColors.DowngradeSpeedGlow)
        ItemEffectType.INSTANT_DEATH -> Pair(SnakeColors.DowngradeDeath, SnakeColors.DowngradeDeathGlow)
        ItemEffectType.POINT_MINUS -> Pair(SnakeColors.DowngradeMinus, SnakeColors.DowngradeMinusGlow)
    }

    // Outer glow
    drawCircle(
        color = glowColor,
        radius = radius * 1.3f,
        center = Offset(centerX, centerY)
    )

    // Main item background circle
    drawCircle(
        color = mainColor,
        radius = radius,
        center = Offset(centerX, centerY)
    )

    // Lifetime countdown ring
    val progress = item.remainingTicks.toFloat() / item.maxTicks.coerceAtLeast(1)
    val ringPadding = radius * 1.15f
    drawArc(
        color = Color.White.copy(alpha = 0.85f),
        startAngle = -90f,
        sweepAngle = 360f * progress,
        useCenter = false,
        topLeft = Offset(centerX - ringPadding, centerY - ringPadding),
        size = Size(ringPadding * 2, ringPadding * 2),
        style = Stroke(width = maxOf(2f, minOf(cellWidth, cellHeight) * 0.08f), cap = StrokeCap.Round)
    )

    // Draw item symbol/icon
    when (item.type) {
        ItemEffectType.SLOW -> {
            // Snowflake / clock hands
            val iconR = radius * 0.5f
            drawLine(
                color = Color.White,
                start = Offset(centerX, centerY - iconR),
                end = Offset(centerX, centerY + iconR),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(centerX - iconR, centerY),
                end = Offset(centerX + iconR, centerY),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            val diag = iconR * 0.7f
            drawLine(
                color = Color.White,
                start = Offset(centerX - diag, centerY - diag),
                end = Offset(centerX + diag, centerY + diag),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(centerX - diag, centerY + diag),
                end = Offset(centerX + diag, centerY - diag),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
        ItemEffectType.BONUS_POINTS -> {
            // Star / Diamond
            val dSize = radius * 0.6f
            val path = Path().apply {
                moveTo(centerX, centerY - dSize)
                lineTo(centerX + dSize * 0.7f, centerY)
                lineTo(centerX, centerY + dSize)
                lineTo(centerX - dSize * 0.7f, centerY)
                close()
            }
            drawPath(path, color = Color.White)
            drawCircle(
                color = mainColor,
                radius = radius * 0.2f,
                center = Offset(centerX, centerY)
            )
        }
        ItemEffectType.SPEED_UP -> {
            // Lightning Bolt
            val scale = radius * 0.6f
            val path = Path().apply {
                moveTo(centerX + scale * 0.1f, centerY - scale)
                lineTo(centerX - scale * 0.6f, centerY + scale * 0.1f)
                lineTo(centerX - scale * 0.05f, centerY + scale * 0.1f)
                lineTo(centerX - scale * 0.2f, centerY + scale)
                lineTo(centerX + scale * 0.6f, centerY - scale * 0.1f)
                lineTo(centerX + scale * 0.05f, centerY - scale * 0.1f)
                close()
            }
            drawPath(path, color = Color.White)
        }
        ItemEffectType.INSTANT_DEATH -> {
            // Skull shape (circular cranium + rectangular jaw + eye sockets)
            val skullR = radius * 0.45f
            drawCircle(
                color = Color.White,
                radius = skullR,
                center = Offset(centerX, centerY - skullR * 0.2f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(centerX - skullR * 0.55f, centerY + skullR * 0.15f),
                size = Size(skullR * 1.1f, skullR * 0.75f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Eye holes
            val eyeR = skullR * 0.25f
            drawCircle(
                color = mainColor,
                radius = eyeR,
                center = Offset(centerX - skullR * 0.4f, centerY - skullR * 0.1f)
            )
            drawCircle(
                color = mainColor,
                radius = eyeR,
                center = Offset(centerX + skullR * 0.4f, centerY - skullR * 0.1f)
            )
        }
        ItemEffectType.POINT_MINUS -> {
            // Downward triangle + minus bar
            val tSize = radius * 0.6f
            val path = Path().apply {
                moveTo(centerX, centerY + tSize * 0.9f)
                lineTo(centerX + tSize * 0.85f, centerY - tSize * 0.6f)
                lineTo(centerX - tSize * 0.85f, centerY - tSize * 0.6f)
                close()
            }
            drawPath(path, color = Color.White)
            // Minus bar in center
            drawLine(
                color = mainColor,
                start = Offset(centerX - tSize * 0.4f, centerY - tSize * 0.1f),
                end = Offset(centerX + tSize * 0.4f, centerY - tSize * 0.1f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawSnake(
    snake: List<Position>,
    direction: Direction,
    cellWidth: Float,
    cellHeight: Float
) {
    if (snake.isEmpty()) return

    val padding = minOf(cellWidth, cellHeight) * 0.06f
    val cornerRadius = CornerRadius(minOf(cellWidth, cellHeight) * 0.3f)

    // Draw body segments (from tail to neck)
    for (i in snake.indices.reversed()) {
        val segment = snake[i]
        val left = segment.x * cellWidth + padding
        val top = segment.y * cellHeight + padding
        val width = cellWidth - padding * 2
        val height = cellHeight - padding * 2

        if (i == 0) {
            // Snake Head
            drawRoundRect(
                color = SnakeColors.SnakeHead,
                topLeft = Offset(left, top),
                size = Size(width, height),
                cornerRadius = cornerRadius
            )
            // Draw eyes on head
            drawEyes(segment, direction, cellWidth, cellHeight)
        } else {
            // Snake Body
            val color = if (i % 2 == 0) SnakeColors.SnakeBody else SnakeColors.SnakeBodyAlt
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                cornerRadius = CornerRadius(minOf(cellWidth, cellHeight) * 0.22f)
            )
        }
    }
}

private fun DrawScope.drawEyes(
    head: Position,
    direction: Direction,
    cellWidth: Float,
    cellHeight: Float
) {
    val headCenterX = head.x * cellWidth + cellWidth / 2f
    val headCenterY = head.y * cellHeight + cellHeight / 2f
    val eyeRadius = minOf(cellWidth, cellHeight) * 0.12f
    val pupilRadius = eyeRadius * 0.55f
    val offsetDist = minOf(cellWidth, cellHeight) * 0.22f

    val (eye1Offset, eye2Offset) = when (direction) {
        Direction.UP -> Pair(
            Offset(headCenterX - offsetDist, headCenterY - offsetDist * 0.6f),
            Offset(headCenterX + offsetDist, headCenterY - offsetDist * 0.6f)
        )
        Direction.DOWN -> Pair(
            Offset(headCenterX - offsetDist, headCenterY + offsetDist * 0.6f),
            Offset(headCenterX + offsetDist, headCenterY + offsetDist * 0.6f)
        )
        Direction.LEFT -> Pair(
            Offset(headCenterX - offsetDist * 0.6f, headCenterY - offsetDist),
            Offset(headCenterX - offsetDist * 0.6f, headCenterY + offsetDist)
        )
        Direction.RIGHT -> Pair(
            Offset(headCenterX + offsetDist * 0.6f, headCenterY - offsetDist),
            Offset(headCenterX + offsetDist * 0.6f, headCenterY + offsetDist)
        )
    }

    // Outer white of eyes
    drawCircle(color = Color.White, radius = eyeRadius, center = eye1Offset)
    drawCircle(color = Color.White, radius = eyeRadius, center = eye2Offset)

    // Inner dark pupils
    drawCircle(color = SnakeColors.SnakeEye, radius = pupilRadius, center = eye1Offset)
    drawCircle(color = SnakeColors.SnakeEye, radius = pupilRadius, center = eye2Offset)
}
