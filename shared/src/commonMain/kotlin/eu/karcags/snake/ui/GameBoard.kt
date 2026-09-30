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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import eu.karcags.snake.model.Direction
import eu.karcags.snake.model.GameState
import eu.karcags.snake.model.Position

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
