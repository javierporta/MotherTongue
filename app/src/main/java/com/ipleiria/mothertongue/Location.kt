package com.ipleiria.mothertongue

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.awareness.fence.DetectedActivityFence
import com.google.android.gms.awareness.fence.FenceState
import com.google.android.gms.awareness.fence.FenceStateMap
import com.ipleiria.mothertongue.google_awareness.FenceApiClient
import com.ipleiria.mothertongue.utils.IFenceReceiver
import java.sql.Timestamp


class Location : AppCompatActivity() {

    private var myPendingIntent: PendingIntent? = null
    private var fenceReceiver: FenceReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val mySpinner = findViewById(R.id.location) as Spinner

        val myAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.places_array)
        )
        val places = arrayOf(resources.getStringArray(R.array.places_array))

        setupFences()
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        var textfence = ""
        val fenceClient= FenceApiClient.instance(myPendingIntent,fenceReceiver)
        val stateFence =
            DetectedActivityFence.during(DetectedActivityFence.STILL);
        fenceClient.addFenceSync(this,"walkingFence", stateFence)
            .addOnSuccessListener {

                fenceClient.queryFencesAsyc(this)
                    .addOnSuccessListener { fenceQueryResponse ->
                        var fenceStateMap: FenceStateMap = fenceQueryResponse.fenceStateMap

                        var fenceInfo = ""
                        for (fenceKey in fenceStateMap.fenceKeys) {
                            val state = fenceStateMap.getFenceState(fenceKey).currentState
                            fenceInfo += "$fenceKey: ${if (state == FenceState.TRUE) "TRUE" else if (state == FenceState.FALSE) "FALSE" else "UNKNOWN"}".trimIndent()
                        }
                        val timestamp = Timestamp(System.currentTimeMillis())
                        textfence = "[Fences @ $timestamp]> Fences' states: ${if (fenceInfo == "") "No registered fences." else fenceInfo}".trimIndent()

                    }
                    .addOnFailureListener { e ->
                        val timestamp = Timestamp(System.currentTimeMillis())
                        val text = "[Fences @ $timestamp] Fences could not be queried: ${e.message}".trimIndent()
                        Log.e("TAG_FENCE", text)
                    }
            }.addOnFailureListener { e ->

                val timestamp = Timestamp(System.currentTimeMillis())
                val text = "\n\n[Fences @ " + timestamp + "]\n" + "Fence " + "walkingFence" +
                        "could not be registered: " + e.message
                Log.e("TAG_FENCE", text)
            }

        mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0)
                {
                    val text = mySpinner.selectedItem.toString()
                    val resultado = getIntent();
                    resultado.putExtra("SELECTED_PLACE", text)
                    setResult(Activity.RESULT_OK, resultado)
                    finish()
                }
            }
        }
    }

    private fun setupFences()
    {
        val intent = Intent("FENCE_RECEIVER_ACTION")
        myPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        fenceReceiver = FenceReceiver()//TODO
        registerReceiver(fenceReceiver, IntentFilter("FENCE_RECEIVER_ACTION"))
    }

    private inner class FenceReceiver : BroadcastReceiver(), IFenceReceiver {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action != "FENCE_RECEIVER_ACTION") {
                Log.e("TAG_FENCES", "Received an unsupported action in FenceReceiver: action="
                        + intent.action )
                return
            }
            val fenceState = FenceState.extract(intent)
            var fenceInfo: String? = null
            when (fenceState.fenceKey) {
                "headphoneFence" -> when (fenceState.currentState) {
                    FenceState.TRUE -> fenceInfo = "TRUE | Headphones are plugged in."
                    FenceState.FALSE -> fenceInfo = "FALSE | Headphones are unplugged."
                    FenceState.UNKNOWN -> fenceInfo = "Error: unknown state."
                }
                "walkingFence" -> when (fenceState.currentState) {
                    FenceState.TRUE -> fenceInfo = "TRUE | Walking."
                    FenceState.FALSE -> fenceInfo = "FALSE | Not walking."
                    FenceState.UNKNOWN -> fenceInfo = "Error: unknown state."
                }
                "timeFence" -> when (fenceState.currentState) {
                    FenceState.TRUE -> fenceInfo = "TRUE | Within timeslot."
                    FenceState.FALSE -> fenceInfo = "FALSE | Out of timeslot."
                    FenceState.UNKNOWN -> fenceInfo = "Error: unknown state."
                }
                "walkingWithHeadphonesFenceKey" -> when (fenceState.currentState) {
                    FenceState.TRUE -> fenceInfo = "TRUE | walkingWithHead."
                    FenceState.FALSE -> fenceInfo = "FALSE | No walkingWithHead."
                }
                else -> fenceInfo = "Error: unknown fence: " + fenceState.fenceKey
            }
            val timestamp = Timestamp(System.currentTimeMillis())
            val text = ("\n\n[Fences @ " + timestamp + "]\n"  + fenceState.fenceKey + ": " + fenceInfo)
            //textView.setText(text + textView.getText())
        }
    }

}
