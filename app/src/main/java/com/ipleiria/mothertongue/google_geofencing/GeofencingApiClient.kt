package com.ipleiria.mothertongue.google_geofencing

import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices

class GeofencingApiClient(context: AppCompatActivity) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    /**
     *
     */
    companion object {
        private var INSTANCE: GeofencingApiClient? = null

        fun instance(context: AppCompatActivity): GeofencingApiClient =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: GeofencingApiClient(context).also { INSTANCE = it }
            }
    }

}