package com.ipleiria.mothertongue.google_palces

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.ipleiria.mothertongue.utils.LocationPermission
import java.sql.Timestamp

class PlacesApiClient {

    /**
     *
     */
    private object HOLDER  {
        val INSTANCE = PlacesApiClient()
    }

    /**
     *
     */
    companion object {
        val instance: PlacesApiClient by lazy { HOLDER.INSTANCE }
    }

    fun getNearbyPlacesAsync(activity: Activity, apiKey: String): Task<FindCurrentPlaceResponse> {
        LocationPermission.checkFine(activity)
        // Initialize Places.
        Places.initialize(activity.applicationContext, apiKey)
        // Create a new Places client instance.
        val placesClient: PlacesClient = Places.createClient(activity)
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> = mutableListOf(Place.Field.NAME,
            Place.Field.ADDRESS, Place.Field.TYPES, Place.Field.LAT_LNG)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.builder(placeFields).build()

        // Call findCurrentPlace and handle the response.
        return placesClient.findCurrentPlace(request)
    }

    fun getCurrentPlaceAsync(activity: Activity, apiKey: String): Task<FindCurrentPlaceResponse> {
        LocationPermission.checkFine(activity)
        // Initialize Places.
        Places.initialize(activity.applicationContext, apiKey)
        // Create a new Places client instance.
        val placesClient: PlacesClient = Places.createClient(activity)
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> = mutableListOf(Place.Field.NAME,
            Place.Field.ADDRESS, Place.Field.TYPES, Place.Field.LAT_LNG)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.builder(placeFields).build()

        // Call findCurrentPlace and handle the response.
        return placesClient.findCurrentPlace(request)
    }

}