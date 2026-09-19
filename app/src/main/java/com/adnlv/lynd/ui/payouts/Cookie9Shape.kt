package com.adnlv.lynd.ui.payouts

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star

class Cookie9Shape(
    val polygon: RoundedPolygon = defaultPolygon
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cubics = polygon.cubics
        if (cubics.isEmpty()) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }

        val path = Path()
        var isFirst = true
        cubics.forEach { cubic ->
            if (isFirst) {
                path.moveTo(cubic.anchor0X, cubic.anchor0Y)
                isFirst = false
            }
            path.cubicTo(
                cubic.control0X, cubic.control0Y,
                cubic.control1X, cubic.control1Y,
                cubic.anchor1X, cubic.anchor1Y
            )
        }
        path.close()

        // Rotate -90 degrees around origin (0, 0) so the top lobe points straight up
        val rotateMatrix = Matrix().apply {
            rotateZ(-90f)
        }
        path.transform(rotateMatrix)

        val bounds = path.getBounds()
        val maxDimension = maxOf(bounds.width, bounds.height)
        if (maxDimension > 0f) {
            val scale = minOf(size.width, size.height) / maxDimension
            val scaleMatrix = Matrix().apply {
                scale(x = scale, y = scale)
            }
            path.transform(scaleMatrix)
        }

        val scaledBounds = path.getBounds()
        path.translate(size.center - scaledBounds.center)

        return Outline.Generic(path)
    }

    companion object {
        val defaultPolygon: RoundedPolygon by lazy {
            RoundedPolygon.star(
                numVerticesPerRadius = 9,
                innerRadius = 0.8f,
                rounding = CornerRounding(radius = 0.5f)
            )
        }
    }
}
