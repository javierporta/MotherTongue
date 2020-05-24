package com.ipleiria.mothertongue.services

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.libraries.places.api.model.Place
import com.google.maps.GeoApiContext
import com.ipleiria.mothertongue.MainActivity
import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.databinding.ActivityMainBinding
import com.ipleiria.mothertongue.google_awareness.SnapshotApiClient
import com.ipleiria.mothertongue.google_palces.PlacesApiClient


class ContextService {

    private val interestPlaces = arrayOf("CAFE", "PARK", "SHOPPING")
    private val defaultPlaces = arrayOf("HOUSE", "BUILDING", "STREET")
    private var allPossibleActions= arrayOf("IN_VEHICLE", "ON_BICYCLE", "ON_FOOT", "STILL", "UNKNOWN", "TILTING", "UNKNOWN", "WALKING", "RUNNING")
    private val thresholdPlaces = 0.60
    private val thresholdNearbyPlaces = 0.30
    private val unsupportedPlace ="UNSUPPOTED_PLACE"

    var possibleStreetActions= arrayOf("ON_BICYCLE", "ON_FOOT", "RUNNING",  "WALKING")

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

    private var mContext: GeoApiContext? = null

    fun detectNearbyPlaces(activity: Activity){
        val apiKey: String = activity.applicationContext.getString(R.string.GOOGLE_PLACE_API_KEY)

         PlacesApiClient.instance.getNearbyPlacesAsync(activity,apiKey)
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

    fun detectPlace(activity: MainActivity, binding: ActivityMainBinding){
        val apiKey: String = activity.applicationContext.getString(R.string.GOOGLE_PLACE_API_KEY)


        activity.startLoading();
        PlacesApiClient.instance.getNearbyPlacesAsync(activity,apiKey)
            .addOnSuccessListener { response ->

                var  placeLikelihoods = response.placeLikelihoods
                var plText = ""
                for (placeLikelihood in placeLikelihoods!!) {
                    plText += """	# ${placeLikelihood.place.name}
                    likelihood: ${placeLikelihood.likelihood}
                    placeTypes: ${placeLikelihood.place.types}
                """
                }

                var nearbyPlace =placeLikelihoods[0]
                var place=""
                if(nearbyPlace.likelihood > 0.60){
                    place = "\n\n${nearbyPlace.place.name} \n\n ${nearbyPlace.place.types} \n\n ${nearbyPlace.likelihood} "
                }else{
                    place = "[HOUSE, STREET, BUILDING]"
                }

                binding.nearbyPlaces.text =  " ${place}"

                SnapshotApiClient.instance.getLocationAsync(activity).addOnSuccessListener { locationResponse ->
                    var location = "\n\n${locationResponse.location.latitude} ${locationResponse.location.longitude} ${locationResponse.location.speed}"


                    binding.nearbyPlaces.text =  " ${place}  ${location} "
                    SnapshotApiClient.instance.detectedActivityAsync(activity).addOnSuccessListener { dar ->
                        val arr = dar.activityRecognitionResult
                        val probableActivity = arr.mostProbableActivity
                        val confidence = probableActivity.confidence
                        val activityStr = probableActivity.toString()
                        val activityDetect = "\n\nActivity: " + activityStr + "\n\n Confidence: " + confidence + "/100"

                        binding.nearbyPlaces.text =  " ${place}  ${location} ${activityDetect}"
                        activity.stopLoading()
                        Log.i("TAG_PLACE", plText)

                        val detectPlace = detectPlaceProcess(nearbyPlace.likelihood, nearbyPlace.place.types, locationResponse.location.latitude, locationResponse.location.longitude, probableActivity)
                        binding.detectedPlaceNametextView.text = detectPlace
                    }
                }
            }.addOnFailureListener(OnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(activity, e.localizedMessage,
                    Toast.LENGTH_SHORT).show()
                activity.stopLoading()
                Log.e("TAG_PLACE", "Could not get Location: $e")
            })
    }

    private fun detectPlaceProcess(placeLikelihood: Double, placeTypes: List<Place.Type>?, latitude: Double, longitude: Double,probableActivity: DetectedActivity): String{


        val place = placeTypes?.filter {it ->
            interestPlaces.contains(it.name)
        }?.firstOrNull()?.name

        if(placeLikelihood >= thresholdPlaces){
            if(place != null){
                return place
            }
        }else {

            val action = allPossibleActions[probableActivity.type]

            if (placeLikelihood >= thresholdNearbyPlaces){
                when (place) {
                    "CAFE" -> {
                        var possibleActions = arrayOf("STILL", "UNKNOWN")
                        if (possibleActions.contains(action)) {
                            return "CAFE"
                        }
                    }
                    "PARK" -> {
                        var possibleActions = arrayOf("ON_FOOT", "RUNNING", "WALKING")
                        if (possibleActions.contains(action)) {
                            return "PARK"
                        }
                    }
                    "SHOPPING" -> {
                        var possibleActions = arrayOf("ON_FOOT", "STILL", "WALKING")
                        if (possibleActions.contains(action)) {
                            return "SHOPPING"
                        }
                    }
                }

                if (possibleStreetActions.contains(action)) {
                    return "STREET"
                }
            }
        }

        return unsupportedPlace
    }


}