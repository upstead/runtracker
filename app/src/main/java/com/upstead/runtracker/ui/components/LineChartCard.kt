package com.upstead.runtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun LineChartCard(
    title: String,
    points: List<Double>,
    dates: List<String>,
    latestLabel: String,
    valueFormatter: (Double) -> String = { "%.2f".format(it) },
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    var selectedIndex by remember(points, dates) { mutableIntStateOf(-1) }

    val minValue = min(points.minOrNull() ?: 0.0, points.maxOrNull() ?: 0.0)
    val maxValue = max(points.minOrNull() ?: 0.0, points.maxOrNull() ?: 0.0)
    val sparseDateIndices = remember(dates) { buildSparseDateIndices(dates.size) }
    val xAxisLabels = remember(dates) {
        dates.map { raw ->
            runCatching {
                LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd-MM"))
            }.getOrDefault(raw)
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                if (points.isNotEmpty()) {
                    Column {
                        Text(
                            text = "High: ${valueFormatter(maxValue)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = onSurfaceVariantColor
                        )
                        Text(
                            text = "Low: ${valueFormatter(minValue)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = onSurfaceVariantColor
                        )
                    }
                }
            }

            Text(
                text = latestLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (points.size < 2) {
                Text(
                    text = "Need at least 2 runs to draw a trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 16.dp)
                        .pointerInput(points) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    selectedIndex = nearestPointIndex(
                                        touchX = offset.x,
                                        width = size.width.toFloat(),
                                        pointCount = points.size
                                    )
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    selectedIndex = nearestPointIndex(
                                        touchX = change.position.x,
                                        width = size.width.toFloat(),
                                        pointCount = points.size
                                    )
                                }
                            )
                        }
                        .pointerInput(points) {
                            detectTapGestures { tapOffset ->
                                selectedIndex = nearestPointIndex(
                                    touchX = tapOffset.x,
                                    width = size.width.toFloat(),
                                    pointCount = points.size
                                )
                            }
                        }
                ) {
                    val minY = points.minOrNull() ?: 0.0
                    val maxY = points.maxOrNull() ?: 1.0
                    val range = max(maxY - minY, 0.001)
                    val horizontalStep = size.width / (points.size - 1)
                    val axisLabelGap = 26.dp.toPx()
                    val topGap = 10.dp.toPx()
                    val chartHeight = size.height - axisLabelGap - topGap

                    val path = Path()
                    points.forEachIndexed { index, value ->
                        val x = index * horizontalStep
                        val normalized = ((value - minY) / range).toFloat()
                        val y = chartHeight - (normalized * chartHeight) + topGap
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )

                    points.forEachIndexed { index, value ->
                        val x = index * horizontalStep
                        val normalized = ((value - minY) / range).toFloat()
                        val y = chartHeight - (normalized * chartHeight) + topGap
                        drawCircle(
                            color = primaryColor,
                            radius = 6f,
                            center = Offset(x, y)
                        )
                    }

                    val textPaint = android.graphics.Paint().apply {
                        color = onSurfaceVariantColor.copy(alpha = 0.85f).toArgb()
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    sparseDateIndices.forEach { index ->
                        val x = index * horizontalStep
                        val label = xAxisLabels.getOrNull(index).orEmpty()
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            x,
                            size.height - 4.dp.toPx(),
                            textPaint
                        )
                    }

                    if (selectedIndex in points.indices) {
                        val value = points[selectedIndex]
                        val dateLabel = xAxisLabels.getOrNull(selectedIndex).orEmpty()
                        val x = selectedIndex * horizontalStep
                        val normalized = ((value - minY) / range).toFloat()
                        val y = chartHeight - (normalized * chartHeight) + topGap

                        drawCircle(
                            color = secondaryColor,
                            radius = 9f,
                            center = Offset(x, y)
                        )

                        val tooltip = "$dateLabel  ${valueFormatter(value)}"
                        val tooltipTextPaint = android.graphics.Paint().apply {
                            color = onSurfaceColor.toArgb()
                            textSize = 12.sp.toPx()
                            isAntiAlias = true
                        }

                        val paddingX = 8.dp.toPx()
                        val paddingY = 6.dp.toPx()
                        val textWidth = tooltipTextPaint.measureText(tooltip)
                        val textHeight = tooltipTextPaint.fontMetrics.run { bottom - top }
                        val bubbleWidth = textWidth + (paddingX * 2)
                        val bubbleHeight = textHeight + (paddingY * 2)

                        val bubbleLeft = (x - bubbleWidth / 2f).coerceIn(0f, size.width - bubbleWidth)
                        val bubbleTop = (y - bubbleHeight - 14.dp.toPx()).coerceAtLeast(0f)
                        val bubbleBottom = bubbleTop + bubbleHeight

                        drawRoundRect(
                            color = surfaceColor,
                            topLeft = Offset(bubbleLeft, bubbleTop),
                            size = androidx.compose.ui.geometry.Size(bubbleWidth, bubbleHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx())
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            tooltip,
                            bubbleLeft + paddingX,
                            bubbleBottom - paddingY - tooltipTextPaint.fontMetrics.bottom,
                            tooltipTextPaint
                        )
                    }
                }
            }
        }
    }
}

private fun nearestPointIndex(touchX: Float, width: Float, pointCount: Int): Int {
    if (pointCount < 2 || width <= 0f) return -1
    val step = width / (pointCount - 1)
    return (touchX / step).roundToInt().coerceIn(0, pointCount - 1)
}

private fun buildSparseDateIndices(total: Int): Set<Int> {
    if (total <= 0) return emptySet()
    if (total <= 5) return (0 until total).toSet()

    val tickCount = 5
    return (0 until tickCount)
        .map { index ->
            ((index.toFloat() * (total - 1)) / (tickCount - 1)).roundToInt()
        }
        .toSet()
}
