package com.ipleiria.mothertongue.services

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.google_palces.PlacesApiClient

class ContextService {
    /**
     *
     */
    private object HOLDER  {
        val INSTANCE = ContextService()
    }

    /**
     *
     */
    companion object {
        val instance: ContextService by lazy { HOLDER.INSTANCE }
    }

    fun detectNearbyPlaces(activity: Activity){
        val apiKey: String = activity.applicationContext.getString(R.string.GOOGLE_PLACE_API_KEY)

         PlacesApiClient.instance.getNearbyPlaces(activity,apiKey)
        .addOnSuccessListener { response ->

            var  placeLikelihoods = response.placeLikelihoods
            var plText = ""
            for (placeLikelihood in placeLikelihoods!!) {
                plText += """	# ${placeLikelihood.place.name}
                    likelihood: ${placeLikelihood.likelihood}
                    address: ${placeLikelihood.place.address}
                    placeTypes: ${placeLikelihood.place.types}
                    coordinates: ${placeLikelihood.place.latLng}
                """
            }

            Log.i("TAG_PLACE", plText)

        }.addOnFailureListener(OnFailureListener { e ->
            e.printStackTrace()
            Toast.makeText(activity, e.localizedMessage,
                Toast.LENGTH_SHORT).show()
            Log.e("TAG_PLACE", "Could not get Location: $e")
        })

    }

    fun detectPlace(activity: Activity){
        val apiKey: String = activity.applicationContext.getString(R.string.GOOGLE_PLACE_API_KEY)

        PlacesApiClient.instance.getNearbyPlaces(activity,apiKey)
            .addOnSuccessListener { response ->

                var  placeLikelihoods = response.placeLikelihoods
                var plText = ""
                for (placeLikelihood in placeLikelihoods!!) {
                    plText += """	# ${placeLikelihood.place.name}
                    likelihood: ${placeLikelihood.likelihood}
                    address: ${placeLikelihood.place.address}
                    placeTypes: ${placeLikelihood.place.types}
                    coordinates: ${placeLikelihood.place.latLng}
                """
                }

                Log.i("TAG_PLACE", plText)

            }.addOnFailureListener(OnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(activity, e.localizedMessage,
                    Toast.LENGTH_SHORT).show()
                Log.e("TAG_PLACE", "Could not get Location: $e")
            })

    }
}