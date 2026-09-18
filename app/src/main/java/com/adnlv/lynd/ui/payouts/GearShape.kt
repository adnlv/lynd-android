package com.adnlv.lynd.ui.payouts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class GearShape(
    val teeth: Int = 8,
    val toothDepthRatio: Float = 0.09f,
    val samplesPerTooth: Int = 12
) : Shape {

    fun calculateRadiusAt(
        angle: Double,
        outerRadius: Float,
        innerRadius: Float
    ): Float {
        val toothAngle = (2.0 * PI) / teeth
        val shiftedAngle = angle + (PI / 2.0) + (toothAngle / 2.0)
        val normalizedAngle = ((shiftedAngle % (2.0 * PI)) + (2.0 * PI)) % (2.0 * PI)
        val phase = (normalizedAngle % toothAngle) / toothAngle

        return when {
            phase < 0.10 -> innerRadius
            phase < 0.40 -> {
                val t = ((phase - 0.10) / 0.30).toFloat()
                innerRadius + (outerRadius - innerRadius) * smoothstep(t)
            }
            phase < 0.60 -> outerRadius
            phase < 0.90 -> {
                val t = ((phase - 0.60) / 0.30).toFloat()
                outerRadius - (outerRadius - innerRadius) * smoothstep(t)
            }
            else -> innerRadius
        }
    }

    private fun smoothstep(t: Float): Float = t * t * (3f - 2f * t)

    fun calculateVertices(size: Size): List<Offset> {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val outerRadius = min(centerX, centerY)
        if (outerRadius <= 0f) return emptyList()

        val innerRadius = outerRadius * (1f - toothDepthRatio)
        val totalSamples = teeth * samplesPerTooth
        val angleStep = (2.0 * PI) / totalSamples
        val vertices = ArrayList<Offset>(totalSamples)

        for (i in 0 until totalSamples) {
            val angle = i * angleStep
            val radius = calculateRadiusAt(angle, outerRadius, innerRadius)
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()
            vertices.add(Offset(x, y))
        }
        return vertices
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val vertices = calculateVertices(size)
        if (vertices.isEmpty()) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }

        val path = Path()
        vertices.forEachIndexed { index, vertex ->
            if (index == 0) {
                path.moveTo(vertex.x, vertex.y)
            } else {
                path.lineTo(vertex.x, vertex.y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}
