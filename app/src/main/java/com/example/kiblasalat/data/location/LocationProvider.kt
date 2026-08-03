package com.example.kiblasalat.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

interface LocationProvider {
    fun getLocationFlow(): Flow<Location?>
    suspend fun getCurrentLocation(): Location?
}

@Singleton
class LocationProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationProvider {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    override fun getLocationFlow(): Flow<Location?> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            15000L // 15 seconds update interval
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(it)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Try sending last known location immediately
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { trySend(it) }
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val priority = if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                } else if (hasFine) {
                    // Try coarse fallback
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            if (continuation.isActive) continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                } else {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }.addOnFailureListener {
                if (hasFine) {
                    // Try coarse fallback on failure
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { loc ->
                            if (continuation.isActive) continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                } else {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
    }
}
