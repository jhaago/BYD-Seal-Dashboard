package io.github.jhaago.sealdashboard.demo

import org.junit.Assert.*
import org.junit.Test

class RoutePointTest {
    @Test fun invalidFictionalCoordinatesAreRejectedBeforeRendering() {
        for ((x,y) in listOf(Float.NaN to .2f, .2f to Float.POSITIVE_INFINITY, -1f to .5f, .5f to 2f)) {
            assertThrows(IllegalArgumentException::class.java) { RoutePoint(x,y) }
        }
    }
    @Test fun boundaryCoordinatesAreValidLocalMapPoints() {
        assertEquals(0f, RoutePoint(0f,1f).x, 0f)
        assertEquals(1f, RoutePoint(0f,1f).y, 0f)
    }
}
