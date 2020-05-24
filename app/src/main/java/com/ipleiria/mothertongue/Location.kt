package com.ipleiria.mothertongue

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class Location : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val mySpinner = findViewById(R.id.location) as Spinner

        val myAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.places_array)
        )
        val places = arrayOf(resources.getStringArray(R.array.places_array))

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
                    setResult(Activity.RESULT_OK, resultado)
                    finish()
                }
            }
        }
    }
}
