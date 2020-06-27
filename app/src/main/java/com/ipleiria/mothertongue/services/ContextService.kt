package com.ipleiria.mothertongue.services

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.libraries.places.api.model.Place
import com.google.maps.GeoApiContext
import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.databinding.FragmentHomeBinding
import com.ipleiria.mothertongue.google_awareness.SnapshotApiClient
import com.ipleiria.mothertongue.google_geofencing.Reminder
import com.ipleiria.mothertongue.google_geofencing.ReminderRepository
import com.ipleiria.mothertongue.google_palces.PlacesApiClient
import com.ipleiria.mothertongue.ui.home.HomeFragment


class ContextService {

    private val interestPlaces = arrayOf("CAFE", "PARK", "SHOPPING")
    private val defaultPlaces = arrayOf("HOUSE", "STREET")
    private var allPossibleActions= arrayOf("IN_VEHICLE", "ON_BICYCLE", "ON_FOOT", "STILL", "UNKNOWN", "TILTING", "UNKNOWN", "WALKING", "RUNNING")
    private val thresholdPlaces = 0.60
    private val thresholdNearbyPlaces = 0.30
    private val unsupportedPlace =""
    private lateinit var repository: ReminderRepository

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

    fun detectPlace(context: HomeFragment, binding: FragmentHomeBinding){
        val apiKey: String = context.activity?.applicationContext!!.getString(R.string.GOOGLE_PLACE_API_KEY)


        context.startLoading();
        Toast.makeText(context.activity,  context.activity?.applicationContext!!.getString(R.string.location_waiting_message),
            Toast.LENGTH_LONG).show()

        PlacesApiClient.instance.getNearbyPlacesAsync(context.activity!!,apiKey)
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

                //binding.nearbyPlaces.text =  " ${place}"

                SnapshotApiClient.instance.getLocationAsync(context.activity!!).addOnSuccessListener { locationResponse ->
                    var location = "\n\n${locationResponse.location.latitude} ${locationResponse.location.longitude} ${locationResponse.location.speed}"


                    //binding.nearbyPlaces.text =  " ${place}  ${location} "
                    SnapshotApiClient.instance.detectedActivityAsync(context.activity!!).addOnSuccessListener { dar ->
                        val arr = dar.activityRecognitionResult
                        val probableActivity = arr.mostProbableActivity
                        val confidence = probableActivity.confidence
                        val activityStr = probableActivity.toString()
                        val activityDetect = "\n\nActivity: " + activityStr + "\n\n Confidence: " + confidence + "/100"

                        //binding.nearbyPlaces.text =  " ${place}  ${location} ${activityDetect}"
                        context.stopLoading()
                        Log.i("TAG_PLACE", plText)

                        val detectPlace = detectPlaceProcess(nearbyPlace.likelihood, nearbyPlace.place.types, locationResponse.location.latitude, locationResponse.location.longitude, probableActivity)
                        binding.detectedPlaceNametextView.text = detectPlace
                        binding.mainModel?.currentPlaceName =  detectPlace

                        context.stopLoading()
                        if(detectPlace == unsupportedPlace){
                            val latLng = LatLng(locationResponse.location.latitude,locationResponse.location.longitude)
                            var reminder = Reminder(latLng = latLng, radius = null, message = null)
                            val location  = exitGeofence(reminder,context)

                            if(location == null)
                            {
                               /* val intent = Intent(context.context,Location::class.java)
                                // To pass any data to next activity
                                intent.putExtra("detectPlace", unsupportedPlace)
                                intent.putExtra("latitude",  locationResponse.location.latitude)
                                intent.putExtra("longitude", locationResponse.location.longitude)
                                // start your next activity
                                context.startActivityForResult(intent,1)*/

                                var bundle = Bundle()
                                bundle.putString("detectPlace", unsupportedPlace)
                                bundle.putDouble("latitude", locationResponse.location.latitude)
                                bundle.putDouble("longitude", locationResponse.location.longitude)
                                context.findNavController().navigate(R.id.action_nav_home_to_userPosition, bundle)
                            }else{
                                binding.mainModel?.currentPlaceName =  location?.message!!
                                binding.detectedPlaceNametextView.text = location?.message!!
                                context.stopLoading()
                            }


                        }
                    }
                }
            }.addOnFailureListener(OnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(context.activity, e.localizedMessage,
                    Toast.LENGTH_SHORT).show()
                context.stopLoading()
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

    private fun exitGeofence(reminder: Reminder, context: HomeFragment): Reminder?{

        repository = ReminderRepository(context.activity!!)

        var places =repository.getAll();

        for (r in places) {

            val curDist = FloatArray(2)
            android.location.Location.distanceBetween(r.latLng?.latitude!!, r.latLng?.latitude!!, reminder.latLng?.latitude!!, reminder.latLng?.longitude!!,curDist)

            var dist =curDist[0] / 10000000
            if(r.message!= null && r.radius!= null && dist <= r.radius!!)
                return  r

        }
        return null
    }

}