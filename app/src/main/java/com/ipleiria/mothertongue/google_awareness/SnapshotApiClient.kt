package com.ipleiria.mothertongue.google_awareness

import android.app.Activity
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.SnapshotClient
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse
import com.google.android.gms.awareness.snapshot.HeadphoneStateResponse
import com.google.android.gms.awareness.snapshot.LocationResponse
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.tasks.Task
import com.ipleiria.mothertongue.utils.LocationPermission

/**
 *
 */
class SnapshotApiClient private constructor() {

    /**
     *
     */
    private object HOLDER  {
        val INSTANCE = SnapshotApiClient()
    }

    /**
     *
     */
    companion object {
        val instance: SnapshotApiClient by lazy { HOLDER.INSTANCE }
    }

    /**
     *
     */
    fun getLocation(activity: Activity): Location? {

        var location: Location? = null

        LocationPermission.checkFine(activity)
        Awareness.getSnapshotClient(activity).location
            .addOnSuccessListener { locationResponse ->
                location = locationResponse.location
            }
            .addOnFailureListener { e ->
                Log.e("TAG_SNAPSHOT", "Could not get Location: $e")
                Toast.makeText(activity, "Could not get Location: $e",
                    Toast.LENGTH_SHORT).show()
            }

        return location
    }

    fun getLocationAsync(activity: Activity): Task<LocationResponse> {
        LocationPermission.checkFine(activity)
        return Awareness.getSnapshotClient(activity).location
    }

    /**
     *
     */
    fun getHeadphoneState(activity: Activity): HeadphoneState? {
        var headphoneState: HeadphoneState? = null
        Awareness.getSnapshotClient(activity).headphoneState
            .addOnSuccessListener { headphoneStateResponse ->
                headphoneState = headphoneStateResponse.headphoneState
            }
            .addOnFailureListener { e ->
                Log.e("snapshot", "Could not get headphone state: $e")
                Toast.makeText(
                    activity, "Could not get headphone state: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }

        return  headphoneState
    }

    fun getHeadphoneStateAsync(activity: Activity): Task<HeadphoneStateResponse> {
        return  Awareness.getSnapshotClient(activity).headphoneState
    }

    fun detectedActivity(activity: Activity): ActivityRecognitionResult? {
        var activityRecognition: ActivityRecognitionResult?= null
        Awareness.getSnapshotClient(activity).detectedActivity
            .addOnSuccessListener { dar ->
                activityRecognition = dar.activityRecognitionResult

            }
            .addOnFailureListener { e ->
                Log.e("snapshot", "Could not get Detected Activity: $e")
                Toast.makeText(
                    activity, "Could not get Detected Activity: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }

        return activityRecognition
    }

    fun detectedActivityAsync(activity: Activity): Task<DetectedActivityResponse> {

        return Awareness.getSnapshotClient(activity).detectedActivity
    }


}