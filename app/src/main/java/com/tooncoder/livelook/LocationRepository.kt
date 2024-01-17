package com.tooncoder.livelook

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationRepository(private val application: Application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            onLocationUpdateCallback?.invoke(locationResult)
        }
    }

    private var onLocationUpdateCallback: ((LocationResult) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(callback: (LocationResult) -> Unit) {
        onLocationUpdateCallback = callback

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopLocationUpdates() {
        onLocationUpdateCallback = null
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun startForegroundLocationService() {
        if (checkLocationPermission()) {
            val serviceIntent = Intent(application, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(application, serviceIntent)
        } else {
            // Handle the case where location permission is not granted
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("SuspiciousIndentation")
    fun callApi() {
        val apiCallTask = ApiCallTask()
          apiCallTask.execute()
    }

}
