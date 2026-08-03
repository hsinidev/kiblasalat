package com.example.kiblasalat.presentation.viewmodel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiblasalat.domain.model.QiblaDirection
import com.example.kiblasalat.domain.repository.SettingsRepository
import com.example.kiblasalat.domain.usecase.CalculateQiblaAngleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class QiblaState(
    val qiblaDirection: QiblaDirection? = null,
    val hasSensors: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class QiblaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val calculateQiblaAngleUseCase: CalculateQiblaAngleUseCase
) : ViewModel(), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val _qiblaState = MutableStateFlow(QiblaState())
    val qiblaState: StateFlow<QiblaState> = _qiblaState.asStateFlow()

    private var userLat: Double? = null
    private var userLng: Double? = null

    private var avgSin = 0f
    private var avgCos = 0f
    private val alpha = 0.12f // Jitter smoothing factor
    private var isFirstSensorValue = true

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAccelerometer = FloatArray(3)
    private val lastGeomagnetic = FloatArray(3)
    private var isAccelerometerSet = false
    private var isGeomagneticSet = false

    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.getLatitude(),
                settingsRepository.getLongitude()
            ) { lat, lng ->
                Pair(lat, lng)
            }.collect { (lat, lng) ->
                userLat = lat ?: 21.4225 // Makkah default
                userLng = lng ?: 39.8262
            }
        }
    }

    fun startListening() {
        isFirstSensorValue = true
        isAccelerometerSet = false
        isGeomagneticSet = false
        
        val rotationRegistered = rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: false

        if (!rotationRegistered) {
            val accRegistered = accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            } ?: false
            val magRegistered = magneticSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            } ?: false

            if (!accRegistered || !magRegistered) {
                _qiblaState.value = QiblaState(
                    hasSensors = false,
                    errorMessage = "Compass sensors (Rotation Vector / Magnetometer) are not available on this device."
                )
            }
        } else {
            _qiblaState.value = QiblaState(hasSensors = true)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val lat = userLat ?: return
        val lng = userLng ?: return

        var azimuthRad = 0f
        var gotOrientation = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuthRad = orientationAngles[0]
            gotOrientation = true
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                isAccelerometerSet = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, lastGeomagnetic, 0, event.values.size)
                isGeomagneticSet = true
            }

            if (isAccelerometerSet && isGeomagneticSet) {
                SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastGeomagnetic)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                azimuthRad = orientationAngles[0]
                gotOrientation = true
            }
        }

        if (gotOrientation) {
            val currentSin = sin(azimuthRad)
            val currentCos = cos(azimuthRad)

            if (isFirstSensorValue) {
                avgSin = currentSin
                avgCos = currentCos
                isFirstSensorValue = false
            } else {
                avgSin = avgSin + alpha * (currentSin - avgSin)
                avgCos = avgCos + alpha * (currentCos - avgCos)
            }

            val filteredAzimuthRad = atan2(avgSin, avgCos)
            var azimuthDeg = Math.toDegrees(filteredAzimuthRad.toDouble()).toFloat()
            azimuthDeg = (azimuthDeg + 360) % 360

            val qiblaDir = calculateQiblaAngleUseCase(lat, lng, azimuthDeg)
            _qiblaState.value = QiblaState(
                qiblaDirection = qiblaDir,
                hasSensors = true
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
