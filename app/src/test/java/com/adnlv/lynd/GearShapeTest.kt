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
        val vertices = shape.calculateVertices(Size(44f, 44f))

        assertEquals(96, vertices.size)
    }

    @Test
    fun gearShape_verticesRemainWithinBounds() {
        val shape = GearShape(teeth = 8, samplesPerTooth = 12)
        val vertices = shape.calculateVertices(Size(44f, 44f))

        vertices.forEach { vertex ->
            assertTrue("X ${vertex.x} should be >= 0", vertex.x >= 0f)
            assertTrue("X ${vertex.x} should be <= 44", vertex.x <= 44.001f)
            assertTrue("Y ${vertex.y} should be >= 0", vertex.y >= 0f)
            assertTrue("Y ${vertex.y} should be <= 44", vertex.y <= 44.001f)
        }
    }

    @Test
    fun gearShape_radiusTransitionsSmoothly() {
        val shape = GearShape(teeth = 8)
        val outerRadius = 22f
        val innerRadius = 22f * (1f - shape.toothDepthRatio)

        val rootRadius = shape.calculateRadiusAt(0.0, outerRadius, innerRadius)
        assertEquals(innerRadius, rootRadius, 0.001f)

        val crestAngle = Math.PI / 8.0 // 45 / 2 = 22.5 deg (midpoint of first tooth cycle)
        val crestRadius = shape.calculateRadiusAt(crestAngle, outerRadius, innerRadius)
        assertEquals(outerRadius, crestRadius, 0.001f)
    }

    @Test
    fun gearShape_returnsEmptyVerticesForZeroSize() {
        val shape = GearShape()
        val vertices = shape.calculateVertices(Size.Zero)

        assertTrue(vertices.isEmpty())
    }
}
