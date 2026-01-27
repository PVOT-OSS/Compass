package org.prauga.compass.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassDial(
    heading: Float,
    modifier: Modifier = Modifier
) {
    val cardinals = mapOf(0 to "N", 90 to "E", 180 to "S", 270 to "W")

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Rotating layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-heading)
        ) {
            val outerRadius = size.minDimension / 2
            val ringRadius = outerRadius * 0.78f
            val center = center

            // Outer ring
            drawCircle(
                color = Color.White,
                radius = ringRadius,
                style = Stroke(width = 1.dp.toPx())
            )

            // Ticks every 2°
            for (deg in 0 until 360 step 2) {
                val isMajor = deg % 30 == 0
                val length = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                val width = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                rotate(deg.toFloat(), center) {
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x, center.y - ringRadius),
                        end = Offset(center.x, center.y - ringRadius + length),
                        strokeWidth = width
                    )
                }
            }

            // Number labels outside the ring
            val outerLabelRadius = outerRadius * 0.96f
            val numberPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 13.dp.toPx()
                isAntiAlias = true
            }

            for (deg in 0 until 360 step 30) {
                val angleRad = Math.toRadians(deg.toDouble() - 90.0)
                val x = center.x + outerLabelRadius * cos(angleRad).toFloat()
                val y = center.y + outerLabelRadius * sin(angleRad).toFloat()
                val textOffset = (numberPaint.descent() + numberPaint.ascent()) / 2
                val nCanvas = drawContext.canvas.nativeCanvas
                nCanvas.save()
                nCanvas.rotate(heading, x, y)
                nCanvas.drawText(deg.toString(), x, y - textOffset, numberPaint)
                nCanvas.restore()
            }

            // Cardinal letters
            val innerLabelRadius = ringRadius - 40.dp.toPx()
            val cardinalPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 22.dp.toPx()
                isAntiAlias = true
            }

            for ((deg, label) in cardinals) {
                val angleRad = Math.toRadians(deg.toDouble() - 90.0)
                val x = center.x + innerLabelRadius * cos(angleRad).toFloat()
                val y = center.y + innerLabelRadius * sin(angleRad).toFloat()
                val textOffset = (cardinalPaint.descent() + cardinalPaint.ascent()) / 2
                val nCanvas = drawContext.canvas.nativeCanvas
                nCanvas.save()
                nCanvas.rotate(heading, x, y)
                nCanvas.drawText(label, x, y - textOffset, cardinalPaint)
                nCanvas.restore()
            }

            // Red arrow at N
            val arrowBase = center.y - ringRadius - 3.dp.toPx()
            val arrowTip = center.y - outerRadius * 0.90f
            val arrowHalfW = 6.dp.toPx()
            drawPath(
                path = Path().apply {
                    moveTo(center.x, arrowTip)
                    lineTo(center.x - arrowHalfW, arrowBase)
                    lineTo(center.x + arrowHalfW, arrowBase)
                    close()
                },
                color = Color(0xFFFF3B30)
            )
        }

        // Center cross
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - 40.dp.toPx()),
                end = Offset(center.x, center.y + 40.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(center.x - 40.dp.toPx(), center.y),
                end = Offset(center.x + 40.dp.toPx(), center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = 18.dp.toPx()
            )
        }

        // Static heading line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringR = size.minDimension / 2 * 0.78f
            drawLine(
                color = Color.White,
                start = Offset(center.x, 0f),
                end = Offset(center.x, center.y - ringR),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}