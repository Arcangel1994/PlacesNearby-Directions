package com.example.minutos.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Features {

    fun isConnected(context: Context): Boolean {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isConnected: Boolean = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    isConnected = true
                }
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    isConnected = true
                }
            } else {
                isConnected = false
            }
        }else{
            val info = connectivityManager.activeNetworkInfo
            isConnected = info != null && info.isConnected
        }

        return isConnected
    }

    fun checkPermission(context: Context): Boolean {

        var isPermission: Boolean = false

        if ( (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            isPermission = true

        }

        return isPermission

    }

    fun distFrom(lat1: Float, lng1: Float, lat2: Float, lng2: Float): Float {
        val locStart = Location("")
        locStart.latitude = lat1.toDouble()
        locStart.longitude = lng1.toDouble()
        val locEnd = Location("")
        locEnd.latitude = lat2.toDouble()
        locEnd.longitude = lng2.toDouble()
        return locStart.distanceTo(locEnd)
    }

}