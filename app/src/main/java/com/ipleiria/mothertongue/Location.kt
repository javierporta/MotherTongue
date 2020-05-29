package com.ipleiria.mothertongue

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.ipleiria.mothertongue.google_geofencing.Reminder
import com.ipleiria.mothertongue.google_geofencing.ReminderRepository
import kotlinx.android.synthetic.main.activity_location.*


class Location : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var reminder = Reminder(latLng = null, radius = null, message = null)
    private lateinit var repository: ReminderRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        marker.visibility = View.GONE
        repository = ReminderRepository(this)

        val mySpinner = findViewById(R.id.location) as Spinner
        val myAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.places_array)
        )

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0)
                {
                    val text = mySpinner.selectedItem.toString()
                    val resultado = getIntent();
                    resultado.putExtra("SELECTED_PLACE", text)
                    reminder.message = text;
                    setResult(Activity.RESULT_OK, resultado)
                    addReminder(reminder)
                }
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = false
        centerCamera()

    }

    private fun centerCamera() {
        if (intent != null) {

            var bundle :Bundle ?=intent.extras
            var lat = bundle!!.get("latitude") as Double
            var log = bundle!!.get("longitude") as Double
            val latLng = LatLng(lat,log)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 110f))

            reminder.latLng = map.cameraPosition.target
            reminder.message = "dddd"
            reminder.radius = 5.0

            showReminderInMap(this, map, reminder)
        }
    }

    fun showReminderInMap(context: Context,
                          map: GoogleMap,
                          reminder: Reminder) {
        if (reminder.latLng != null) {
            val latLng = reminder.latLng as LatLng
            val vectorToBitmap = vectorToBitmap(context.resources, R.drawable.ic_twotone_location_on_48px)
            val marker = map.addMarker(MarkerOptions().position(latLng).icon(vectorToBitmap))
            marker.tag = reminder.id
            if (reminder.radius != null) {
                val radius = reminder.radius as Double
                map.addCircle(
                    CircleOptions()
                        .center(reminder.latLng)
                        .radius(radius)
                        .strokeColor(ContextCompat.getColor(context, R.color.colorAccent))
                        .fillColor(ContextCompat.getColor(context, R.color.colorReminderFill)))
            }
        }
    }

    fun vectorToBitmap(resources: Resources, @DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun addReminder(reminder: Reminder) {
        reminder.radius = 5.0
        repository.add(reminder,
            success = {
                finish()
            },
            failure = {
                Log.e("TAG_LOCATION", it)
                //Snackbar.make(main, it, Snackbar.LENGTH_LONG).show()
            })
    }

}
