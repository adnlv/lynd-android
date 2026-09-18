package com.adnlv.lynd

import androidx.compose.ui.geometry.Size
import com.adnlv.lynd.ui.payouts.GearShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GearShapeTest {

    @Test
    fun gearShape_calculatesCorrectNumberOfVertices() {
        val shape = GearShape(teeth = 8)
        val vertices = shape.calculateVertices(Size(40f, 40f))

        assertEquals(40, vertices.size)
    }

    @Test
    fun gearShape_verticesRemainWithinBounds() {
        val shape = GearShape(teeth = 8)
        val vertices = shape.calculateVertices(Size(40f, 40f))

        vertices.forEach { vertex ->
            assertTrue("X ${vertex.x} should be >= 0", vertex.x >= 0f)
            assertTrue("X ${vertex.x} should be <= 40", vertex.x <= 40.001f)
            assertTrue("Y ${vertex.y} should be >= 0", vertex.y >= 0f)
            assertTrue("Y ${vertex.y} should be <= 40", vertex.y <= 40.001f)
        }
    }

    @Test
    fun gearShape_returnsEmptyVerticesForZeroSize() {
        val shape = GearShape()
        val vertices = shape.calculateVertices(Size.Zero)

        assertTrue(vertices.isEmpty())
    }
}
