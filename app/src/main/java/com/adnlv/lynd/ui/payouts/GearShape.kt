package com.adnlv.lynd.ui.payouts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class GearShape(
    val teeth: Int = 8,
    val toothDepthRatio: Float = 0.16f
) : Shape {

    fun calculateVertices(size: Size): List<Offset> {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val outerRadius = min(centerX, centerY)
        if (outerRadius <= 0f) return emptyList()

        val innerRadius = outerRadius * (1f - toothDepthRatio)
        val step = (2.0 * Math.PI / teeth).toFloat()
        val vertices = ArrayList<Offset>(teeth * 5)

        for (i in 0 until teeth) {
            val baseAngle = i * step
            val a0 = baseAngle
            val a1 = baseAngle + step * 0.20f
            val a2 = baseAngle + step * 0.35f
            val a3 = baseAngle + step * 0.65f
            val a4 = baseAngle + step * 0.80f

            vertices.add(Offset(centerX + innerRadius * cos(a0), centerY + innerRadius * sin(a0)))
            vertices.add(Offset(centerX + innerRadius * cos(a1), centerY + innerRadius * sin(a1)))
            vertices.add(Offset(centerX + outerRadius * cos(a2), centerY + outerRadius * sin(a2)))
            vertices.add(Offset(centerX + outerRadius * cos(a3), centerY + outerRadius * sin(a3)))
            vertices.add(Offset(centerX + innerRadius * cos(a4), centerY + innerRadius * sin(a4)))
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
