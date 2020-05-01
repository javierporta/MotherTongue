package com.ipleiria.mothertongue

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.ipleiria.mothertongue.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


    }

    fun onClickPlayButton(view: View) {
        val intent = Intent(this, LiveCamera::class.java)
        // start your next activity
        startActivity(intent)
    }
}
