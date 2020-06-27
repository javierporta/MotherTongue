package com.ipleiria.mothertongue.ui.location

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.databinding.FragmentUserPositionBinding
import com.ipleiria.mothertongue.google_geofencing.Reminder
import com.ipleiria.mothertongue.google_geofencing.ReminderRepository
import kotlinx.android.synthetic.main.activity_location.*

class UserPosition : Fragment(), OnMapReadyCallback {


    private lateinit var binding: FragmentUserPositionBinding
    private lateinit var map: GoogleMap
    private var reminder = Reminder(latLng = null, radius = null, message = null)
    private lateinit var repository: ReminderRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentUserPositionBinding>(inflater, R.layout.fragment_user_position, container, false)


        repository = ReminderRepository(this.activity?.applicationContext!!)

        val marker = binding.root.findViewById(R.id.marker) as ImageView
        marker.visibility = View.GONE

        val mapFragment = getChildFragmentManager().findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mySpinner: Spinner = binding.root.findViewById(R.id.location) as Spinner
        val myAdapter = ArrayAdapter(
            this.activity?.applicationContext!!,
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
                    reminder.message = text;
                    addReminder(reminder)

                    var bundle = Bundle()
                    bundle.putString("SELECTED_PLACE", text)
                    this@UserPosition.findNavController().navigate(R.id.action_userPosition_to_nav_home, bundle)
                }
            }
        }

        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = false
        centerCamera()

    }

    private fun centerCamera() {
        var lat: Double = Double.MIN_VALUE
        var log: Double = Double.MIN_VALUE

        arguments?.let {
            lat = it.getDouble("latitude")
            log = it.getDouble("longitude")
        }

        val latLng = LatLng(lat,log)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20000f))

        reminder.latLng = map.cameraPosition.target
        reminder.message = ""
        reminder.radius = 5.0

        showReminderInMap(this.activity?.applicationContext!!, map, reminder)
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
                //this.activity?.finish()
            },
            failure = {
                Log.e("TAG_LOCATION", it)
            })
    }
}
