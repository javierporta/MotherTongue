package com.ipleiria.mothertongue.google_palces

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
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

    fun getNearbyPlaces(activity: Activity, apiKey: String): List<PlaceLikelihood>? {
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

        var placeLikelihoods: List<PlaceLikelihood>? = null
        // Call findCurrentPlace and handle the response.
        placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
            placeLikelihoods = response.placeLikelihoods
        }.addOnFailureListener(OnFailureListener { e ->
            e.printStackTrace()
            Toast.makeText(activity, e.localizedMessage,
                Toast.LENGTH_SHORT).show()
            Log.e("TAG_PLACE", "Could not get Location: $e")
        })

        return placeLikelihoods
    }

}