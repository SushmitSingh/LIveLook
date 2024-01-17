package com.tooncoder.livelook

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository: LocationRepository = LocationRepository(application)

    private val _locationUpdates = MutableLiveData<Location>()
    val locationUpdates: LiveData<Location> get() = _locationUpdates

    fun startLocationUpdates() {
        locationRepository.startForegroundLocationService()

        locationRepository.startLocationUpdates { locationResult ->
            locationResult.lastLocation?.let {
                _locationUpdates.postValue(it)
                logLocation(it)
            }
        }
    }

    fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
    }

    private fun logLocation(location: Location) {
        Log.d("LocationUpdates", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")

        locationRepository.callApi()
    }
}
