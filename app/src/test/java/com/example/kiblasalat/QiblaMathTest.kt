package com.example.kiblasalat

import com.example.kiblasalat.domain.usecase.CalculateQiblaAngleUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaMathTest {

    private val calculateQiblaAngleUseCase = CalculateQiblaAngleUseCase()

    @Test
    fun testQiblaBearingFromLondon() {
        val direction = calculateQiblaAngleUseCase(51.5074, -0.1278, 0f)
        assertEquals(119.0f, direction.qiblaBearing, 2.0f)
        assertEquals(direction.qiblaBearing, direction.relativeAngle, 0.01f)
    }

    @Test
    fun testAlignmentThreshold() {
        val directionAligned = calculateQiblaAngleUseCase(51.5074, -0.1278, 119.0f)
        assertTrue(directionAligned.isAligned)

        val directionAlignedSlightDev = calculateQiblaAngleUseCase(51.5074, -0.1278, 118.0f)
        assertTrue(directionAlignedSlightDev.isAligned)

        val directionNotAligned = calculateQiblaAngleUseCase(51.5074, -0.1278, 100.0f)
        assertTrue(!directionNotAligned.isAligned)
    }

    @Test
    fun testLowPassFilterBoundaryWrapAround() {
        val angle1 = Math.toRadians(359.0)
        val angle2 = Math.toRadians(1.0)

        val sin1 = sin(angle1).toFloat()
        val cos1 = cos(angle1).toFloat()
        val sin2 = sin(angle2).toFloat()
        val cos2 = cos(angle2).toFloat()

        val avgSin = (sin1 + sin2) / 2f
        val avgCos = (cos1 + cos2) / 2f

        val filteredAngleRad = atan2(avgSin, avgCos)
        var filteredAngleDeg = Math.toDegrees(filteredAngleRad.toDouble()).toFloat()
        filteredAngleDeg = (filteredAngleDeg + 360) % 360

        assertEquals(0.0f, filteredAngleDeg, 0.5f)
    }
}
