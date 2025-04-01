package dev.cianjur.expense.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.cianjur.expense.presentation.dashboard.CategoryStat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CategoryPieChart(
    categoryStats: List<CategoryStat>,
    modifier: Modifier = Modifier
) {
    if (categoryStats.isEmpty()) return

    val totalPercentage = categoryStats.sumOf { it.percentage }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = (canvasWidth.coerceAtMost(canvasHeight) / 2) * 0.8f
            val center = Offset(canvasWidth / 2, canvasHeight / 2)

            var startAngle = -90f  // Start from top (12 o'clock position)

            categoryStats.forEach { stat ->
                val sweepAngle = (stat.percentage / totalPercentage * 360).toFloat()
                val color = Color(android.graphics.Color.parseColor(stat.category.color))

                // Draw sector
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Draw border
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    style = Stroke(width = 2.dp.toPx()),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                startAngle += sweepAngle
            }

            // Draw center circle (optional - for donut chart effect)
            drawCircle(
                color = Color.White,
                radius = radius * 0.6f,
                center = center
            )
        }
    }
}
