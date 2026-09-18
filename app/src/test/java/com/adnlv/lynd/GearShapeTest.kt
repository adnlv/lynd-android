package com.adnlv.lynd

import androidx.compose.ui.geometry.Size
import com.adnlv.lynd.ui.payouts.GearShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GearShapeTest {

    @Test
    fun gearShape_calculatesCorrectNumberOfVertices() {
        val shape = GearShape(teeth = 8, samplesPerTooth = 12)
        val vertices = shape.calculateVertices(Size(40f, 40f))

        assertEquals(96, vertices.size)
    }

    @Test
    fun gearShape_verticesRemainWithinBounds() {
        val shape = GearShape(teeth = 8, samplesPerTooth = 12)
        val vertices = shape.calculateVertices(Size(40f, 40f))

        vertices.forEach { vertex ->
            assertTrue("X ${vertex.x} should be >= 0", vertex.x >= 0f)
            assertTrue("X ${vertex.x} should be <= 40", vertex.x <= 40.001f)
            assertTrue("Y ${vertex.y} should be >= 0", vertex.y >= 0f)
            assertTrue("Y ${vertex.y} should be <= 40", vertex.y <= 40.001f)
        }
    }

    @Test
    fun gearShape_radiusTransitionsSmoothly() {
        val shape = GearShape(teeth = 8)
        val outerRadius = 20f
        val innerRadius = 20f * (1f - shape.toothDepthRatio)

        // At 12 o'clock (-PI / 2), it should be at a tooth crest
        val topCrestRadius = shape.calculateRadiusAt(-Math.PI / 2.0, outerRadius, innerRadius)
        assertEquals(outerRadius, topCrestRadius, 0.001f)

        // At tooth angle / 2 from crest, it should be at a valley root
        val toothAngle = (2.0 * Math.PI) / 8.0
        val valleyAngle = -Math.PI / 2.0 + (toothAngle / 2.0)
        val rootRadius = shape.calculateRadiusAt(valleyAngle, outerRadius, innerRadius)
        assertEquals(innerRadius, rootRadius, 0.001f)
    }

    @Test
    fun gearShape_returnsEmptyVerticesForZeroSize() {
        val shape = GearShape()
        val vertices = shape.calculateVertices(Size.Zero)

        assertTrue(vertices.isEmpty())
    }
}
