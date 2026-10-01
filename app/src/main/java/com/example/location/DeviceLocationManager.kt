package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

class DeviceLocationManager(private val context: Context) {
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val address = reverseGeocode(location.latitude, location.longitude)
                        continuation.resume(LocationResult(location.latitude, location.longitude, address))
                    } else {
                        continuation.resume(LocationResult(28.6139, 77.2090, "New Delhi, India (Approximate)"))
                    }
                }
                .addOnFailureListener {
                    continuation.resume(LocationResult(28.6139, 77.2090, "New Delhi, India (Default)"))
                }
        } catch (e: SecurityException) {
            continuation.resume(null)
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lon, 1)
            val item = list?.firstOrNull()
            if (item != null) {
                "${item.locality ?: item.subAdminArea ?: "City"}, ${item.countryName ?: "India"}"
            } else {
                "Lat: %.2f, Lon: %.2f".format(lat, lon)
            }
        } catch (e: Exception) {
            "Current Coordinates (%.2f, %.2f)".format(lat, lon)
        }
    }
}
