package com.ipleiria.mothertongue

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.databinding.DataBindingUtil
import com.ipleiria.mothertongue.databinding.ActivityMainBinding
import com.ipleiria.mothertongue.models.MainModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainModel: MainModel = MainModel("", "Park")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //bind mainModel ToDo: Create language spinner adapter. Get current language and current place
        binding.mainModel = mainModel

    }

    fun onClickPlayButton(view: View) {
        val intent = Intent(this, LiveCamera::class.java)
        // start your next activity
        startActivity(intent)
    }
}
