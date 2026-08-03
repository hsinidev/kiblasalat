package com.example.kiblasalat.domain.usecase

import com.example.kiblasalat.domain.model.QiblaDirection
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CalculateQiblaAngleUseCase @Inject constructor() {

    companion object {
        const val KAABA_LAT = 21.4225
        const val KAABA_LNG = 39.8262
        const val ALIGNMENT_THRESHOLD_DEGREES = 2.0f
    }

    operator fun invoke(
        userLat: Double,
        userLng: Double,
        userAzimuthDegrees: Float
    ): QiblaDirection {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val kaabaLngRad = Math.toRadians(KAABA_LNG)

        val deltaLng = kaabaLngRad - userLngRad

        val y = sin(deltaLng) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLng)

        val qiblaBearingRad = atan2(y, x)
        var qiblaBearingDeg = Math.toDegrees(qiblaBearingRad).toFloat()
        qiblaBearingDeg = (qiblaBearingDeg + 360) % 360

        // relativeAngle is where the Kaaba is on the screen relative to the phone's current heading
        var relativeAngle = qiblaBearingDeg - userAzimuthDegrees
        relativeAngle = (relativeAngle + 360) % 360

        val isAligned = relativeAngle < ALIGNMENT_THRESHOLD_DEGREES || relativeAngle > (360 - ALIGNMENT_THRESHOLD_DEGREES)

        return QiblaDirection(
            qiblaBearing = qiblaBearingDeg,
            userAzimuth = userAzimuthDegrees,
            relativeAngle = relativeAngle,
            isAligned = isAligned
        )
    }
}
