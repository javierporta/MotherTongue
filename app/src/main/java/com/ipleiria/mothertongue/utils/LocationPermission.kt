package com.ipleiria.mothertongue.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LocationPermission {

    companion object Factory {
        fun checkFine(activity: Activity) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
            try {
                val locationMode = Settings.Secure.getInt(
                    activity.contentResolver,
                    Settings.Secure.LOCATION_MODE
                )
                if (locationMode != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
                    Toast.makeText(
                        activity,
                        "Error: high accuracy location mode must be enabled in the device.",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            } catch (e: SettingNotFoundException) {
                Toast.makeText(
                    activity, "Error: could not access location mode.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
                return
            }
        }
    }
}