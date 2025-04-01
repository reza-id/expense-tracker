package dev.cianjur.expense.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.cianjur.expense.util.CurrencyFormatter
import dev.cianjur.expense.util.DateFormatter
import kotlinx.datetime.LocalDate

@Composable
fun ExpenseChart(
    dailyExpenses: List<Pair<LocalDate, Double>>,
    dateFormatter: DateFormatter,
    currencyFormatter: CurrencyFormatter,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(8.dp)) {
        if (dailyExpenses.isEmpty()) {
            // No data to display
            return@Box
        }

        val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        val primaryColor = MaterialTheme.colorScheme.primary

        Canvas(modifier = Modifier.fillMaxSize()) {
            val chartWidth = size.width
            val chartHeight = size.height
            val padding = 16.dp.toPx()

            val maxAmount = dailyExpenses.maxOfOrNull { it.second } ?: 0.0
            val minAmount = dailyExpenses.minOfOrNull { it.second } ?: 0.0
            val range = maxAmount - minAmount

            // Draw horizontal grid lines
            val gridLineCount = 5
            val gridLineSpacing = (chartHeight - 2 * padding) / gridLineCount

            for (i in 0..gridLineCount) {
                val y = padding + i * gridLineSpacing
                drawLine(
                    color = gridLineColor,
                    start = Offset(padding, y),
                    end = Offset(chartWidth - padding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw line chart
            if (dailyExpenses.size > 1) {
                val pointSpacing = (chartWidth - 2 * padding) / (dailyExpenses.size - 1)
                val points = dailyExpenses.mapIndexed { index, (_, amount) ->
                    val x = padding + index * pointSpacing
                    val normalizedAmount = if (range > 0) (amount - minAmount) / range else 0.5
                    val y = chartHeight - padding - normalizedAmount * (chartHeight - 2 * padding)
                    Offset(x, y.toFloat())
                }

                // Draw connecting lines
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = primaryColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = point
                    )
                }

                // Draw area under the line
                val path = Path().apply {
                    val firstPoint = points.first()
                    val lastPoint = points.last()

                    moveTo(firstPoint.x, firstPoint.y)

                    // Add all points to the path
                    points.forEach { point ->
                        lineTo(point.x, point.y)
                    }

                    // Complete the path to form a closed shape
                    lineTo(lastPoint.x, chartHeight - padding)
                    lineTo(firstPoint.x, chartHeight - padding)
                    close()
                }

                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.2f)
                )
            } else if (dailyExpenses.size == 1) {
                // If there's only one data point, draw a single circle
                val x = chartWidth / 2
                val amount = dailyExpenses[0].second
                val normalizedAmount = if (range > 0) (amount - minAmount) / range else 0.5
                val y = chartHeight - padding - normalizedAmount * (chartHeight - 2 * padding)

                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y.toFloat())
                )
            }
        }
    }
}
