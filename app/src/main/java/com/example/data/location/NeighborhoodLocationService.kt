package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.*

data class NeighborhoodCoordinates(
    val latitude: Double,
    val longitude: Double,
    val neighborhoodName: String,
    val city: String,
    val accuracyMeters: Float? = null,
    val isGpsLive: Boolean = true
)

data class KnownNeighborhood(
    val name: String,
    val city: String,
    val latitude: Double,
    val longitude: Double
)

class NeighborhoodLocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    companion object {
        val KNOWN_NEIGHBORHOODS = listOf(
            KnownNeighborhood("Indiranagar 4th Block, Bengaluru", "Bengaluru", 12.9719, 77.6412),
            KnownNeighborhood("Koramangala 5th Block, Bengaluru", "Bengaluru", 12.9352, 77.6245),
            KnownNeighborhood("Bandra West (14th Road), Mumbai", "Mumbai", 19.0596, 72.8295),
            KnownNeighborhood("Powai Hiranandani Gardens, Mumbai", "Mumbai", 19.1197, 72.9051),
            KnownNeighborhood("Lajpat Nagar IV, New Delhi", "New Delhi", 28.5700, 77.2400),
            KnownNeighborhood("Hauz Khas Enclave, New Delhi", "New Delhi", 28.5494, 77.2001),
            KnownNeighborhood("Jubilee Hills Road #36, Hyderabad", "Hyderabad", 17.4319, 78.4073),
            KnownNeighborhood("Gachibowli Financial District, Hyderabad", "Hyderabad", 17.4401, 78.3489)
        )

        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val raw = r * c
            return (raw * 10.0).roundToInt() / 10.0
        }

        fun findNearestKnownNeighborhood(lat: Double, lng: Double): KnownNeighborhood {
            return KNOWN_NEIGHBORHOODS.minByOrNull {
                calculateDistanceKm(lat, lng, it.latitude, it.longitude)
            } ?: KNOWN_NEIGHBORHOODS.first()
        }

        fun getCoordinatesForNeighborhood(name: String): Pair<Double, Double> {
            val match = KNOWN_NEIGHBORHOODS.find { it.name.equals(name, ignoreCase = true) }
            return if (match != null) {
                Pair(match.latitude, match.longitude)
            } else {
                Pair(12.9719, 77.6412) // Default Indiranagar
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    suspend fun fetchCurrentLocation(): Result<NeighborhoodCoordinates> {
        if (!hasLocationPermission()) {
            val defaultHub = KNOWN_NEIGHBORHOODS.first()
            return Result.success(
                NeighborhoodCoordinates(
                    latitude = defaultHub.latitude,
                    longitude = defaultHub.longitude,
                    neighborhoodName = defaultHub.name,
                    city = defaultHub.city,
                    accuracyMeters = null,
                    isGpsLive = false
                )
            )
        }

        return try {
            val location = getCurrentLocationInternal()
            if (location != null) {
                val coords = resolveNeighborhoodFromLocation(location)
                Result.success(coords)
            } else {
                val fallback = KNOWN_NEIGHBORHOODS.first()
                Result.success(
                    NeighborhoodCoordinates(
                        latitude = fallback.latitude,
                        longitude = fallback.longitude,
                        neighborhoodName = fallback.name,
                        city = fallback.city,
                        accuracyMeters = null,
                        isGpsLive = false
                    )
                )
            }
        } catch (e: Exception) {
            val fallback = KNOWN_NEIGHBORHOODS.first()
            Result.success(
                NeighborhoodCoordinates(
                    latitude = fallback.latitude,
                    longitude = fallback.longitude,
                    neighborhoodName = fallback.name,
                    city = fallback.city,
                    accuracyMeters = null,
                    isGpsLive = false
                )
            )
        }
    }

    private suspend fun getCurrentLocationInternal(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val cts = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxUpdateAgeMillis(60000)
                .setDurationMillis(5000)
                .build()

            fusedLocationClient.getCurrentLocation(request, cts.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Fallback to lastLocation if current is null
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { lastLoc: Location? ->
                                continuation.resume(lastLoc)
                            }
                            .addOnFailureListener {
                                continuation.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    // Fallback to lastLocation on error
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLoc: Location? ->
                            continuation.resume(lastLoc)
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        } catch (e: SecurityException) {
            continuation.resume(null)
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    private fun resolveNeighborhoodFromLocation(location: Location): NeighborhoodCoordinates {
        var resolvedName: String? = null
        var resolvedCity: String? = null

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val subLocality = addr.subLocality ?: addr.subAdminArea ?: addr.featureName
                    val locality = addr.locality ?: addr.adminArea
                    if (!subLocality.isNullOrBlank() && !locality.isNullOrBlank()) {
                        resolvedName = "$subLocality, $locality"
                        resolvedCity = locality
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback to nearest pre-configured neighborhood
        }

        if (resolvedName.isNullOrBlank()) {
            val nearest = findNearestKnownNeighborhood(location.latitude, location.longitude)
            resolvedName = nearest.name
            resolvedCity = nearest.city
        }

        return NeighborhoodCoordinates(
            latitude = location.latitude,
            longitude = location.longitude,
            neighborhoodName = resolvedName,
            city = resolvedCity ?: "Local Hub",
            accuracyMeters = location.accuracy,
            isGpsLive = true
        )
    }
}
