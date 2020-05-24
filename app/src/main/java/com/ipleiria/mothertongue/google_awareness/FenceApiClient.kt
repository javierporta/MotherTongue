package com.ipleiria.mothertongue.google_awareness

import android.app.Activity
import android.app.PendingIntent
import android.util.Log
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.*
import com.google.android.gms.tasks.Task
import com.ipleiria.mothertongue.utils.IFenceReceiver
import java.sql.Timestamp

class FenceApiClient(pendingIntent: PendingIntent?, fenceReceiver: IFenceReceiver?) {

    private var _pendingIntent: PendingIntent? = pendingIntent

    /**
     *
     */
    companion object {
        private var INSTANCE: FenceApiClient? = null

        fun instance(pendingIntent: PendingIntent?, fenceReceiver: IFenceReceiver?): FenceApiClient =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: FenceApiClient(pendingIntent, fenceReceiver).also { INSTANCE = it }
            }
    }

    /**
     *
     */
    fun addFence(activity: Activity, fenceKey: String, fence: AwarenessFence) {
        Awareness.getFenceClient(activity).updateFences(
            FenceUpdateRequest.Builder()
                .addFence(fenceKey, fence, _pendingIntent)
                .build()
        ).addOnSuccessListener {
            val timestamp = Timestamp(System.currentTimeMillis())
            val text = "\n\n[Fences @ $timestamp]\nFence $fenceKey was successfully registered."
            Log.i("TAG_FENCE", text)

        }.addOnFailureListener { e ->

            val timestamp = Timestamp(System.currentTimeMillis())
            val text = "\n\n[Fences @ " + timestamp + "]\n" + "Fence " + fenceKey +
                    "could not be registered: " + e.message
            Log.e("TAG_FENCE", text)
        }
    }

    fun addFenceSync(activity: Activity, fenceKey: String, fence: AwarenessFence): Task<Void> {
       return  Awareness.getFenceClient(activity).updateFences(
           FenceUpdateRequest.Builder()
               .addFence(fenceKey, fence, _pendingIntent)
               .build()
       )
    }

    /**
     *
     */
    fun removeFences(activity: Activity) {
        Awareness.getFenceClient(activity).updateFences(
            FenceUpdateRequest.Builder()
                .removeFence(_pendingIntent)
                .build()
        )
            .addOnSuccessListener {
                val timestamp = Timestamp(System.currentTimeMillis())
                val text = "[Fences @ $timestamp] Fences were successfully removed.".trimIndent()
                Log.i("TAG_FENCE", text)
            }
            .addOnFailureListener { e ->
                val timestamp = Timestamp(System.currentTimeMillis())
                val text = "[Fences @ $timestamp] Fences could not be removed: ${e.message}".trimIndent()
                Log.e("TAG_FENCE", text)
            }
    }

    fun removeFencesAsync(activity: Activity) : Task<Void> {
        return Awareness.getFenceClient(activity).updateFences(
            FenceUpdateRequest.Builder()
                .removeFence(_pendingIntent)
                .build()
        )
    }

    /**
     *
     */
    fun queryFences(activity: Activity): FenceStateMap? {
        var fenceStateMap: FenceStateMap? = null
        Awareness.getFenceClient(activity).queryFences(FenceQueryRequest.all())
            .addOnSuccessListener { fenceQueryResponse ->
                fenceStateMap = fenceQueryResponse.fenceStateMap
            }
            .addOnFailureListener { e ->
                val timestamp = Timestamp(System.currentTimeMillis())
                val text = "[Fences @ $timestamp] Fences could not be queried: ${e.message}".trimIndent()
                Log.e("TAG_FENCE", text)
            }

        return  fenceStateMap
    }

    fun queryFencesAsyc(activity: Activity):  Task<FenceQueryResponse> {
        return   Awareness.getFenceClient(activity).queryFences(FenceQueryRequest.all())
    }
}