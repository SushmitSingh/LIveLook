package com.tooncoder.livelook

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale

class AddressUtil(private val context: Context) {

    fun getAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
        return if (addresses.isNotEmpty()) {
            val address = addresses[0]
            val addressStringBuilder = StringBuilder()
            for (i in 0..address.maxAddressLineIndex) {
                addressStringBuilder.append(address.getAddressLine(i)).append("\n")
            }
            addressStringBuilder.toString()
        } else {
            "Address not found"
        }
    }
}
