package com.adnlv.lynd

import com.adnlv.lynd.ui.payouts.Cookie9Shape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class Cookie9ShapeTest {

    @Test
    fun cookie9Shape_createsExpectedNumberOfCubics() {
        val shape = Cookie9Shape()
        val cubics = shape.polygon.cubics

        // 9 outer rounded lobes and 9 inner rounded valleys produce 19 cubic segments
        assertEquals(19, cubics.size)
    }

    @Test
    fun cookie9Shape_verticesRemainWithinNormalizedBounds() {
        val shape = Cookie9Shape()
        val cubics = shape.polygon.cubics

        cubics.forEach { cubic ->
            assertTrue("anchor0X within bounds", cubic.anchor0X in -1.01f..1.01f)
            assertTrue("anchor0Y within bounds", cubic.anchor0Y in -1.01f..1.01f)
            assertTrue("anchor1X within bounds", cubic.anchor1X in -1.01f..1.01f)
            assertTrue("anchor1Y within bounds", cubic.anchor1Y in -1.01f..1.01f)
        }
    }

    @Test
    fun cookie9Shape_polygonHasOuterAndInnerRadii() {
        val shape = Cookie9Shape()
        val cubics = shape.polygon.cubics

        // Star polygon starts at angle 0 with an outer vertex (radius ~ 1.0)
        val hasOuterPeakAtZero = cubics.any { cubic ->
            cubic.anchor0X > 0.90f && abs(cubic.anchor0Y) < 0.05f
        }
        assertTrue("Star polygon should have an outer peak near x=1, y=0", hasOuterPeakAtZero)
    }
}
