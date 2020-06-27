package com.ipleiria.mothertongue

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.ipleiria.mothertongue.data_manager.Game
import com.ipleiria.mothertongue.databinding.ActivityMainBinding
import com.ipleiria.mothertongue.databinding.FragmentHomeBinding
import com.ipleiria.mothertongue.models.GameLevel
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.models.GameStatus
import com.ipleiria.mothertongue.models.MainModel
import com.ipleiria.mothertongue.services.ContextService
import com.ipleiria.mothertongue.translations.TranslatorService
import java.io.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {


    val HOME_FRAGMENT_NAME = "Home"
    val CAMERA_FRAGMENT_NAME = "Game"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Game.initializeGame()
        Game.getGameData(this.applicationContext)

        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this,navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            try {
                if (destination.label!! == HOME_FRAGMENT_NAME) {
                    Game.saveGame(this.applicationContext)
                }
            } catch (e: Exception) {
                //nothing
            }

            //hide appbar
            val supportActionBar: ActionBar =
                this.getSupportActionBar()!!
            if (destination.label == CAMERA_FRAGMENT_NAME) {
                if (supportActionBar != null) supportActionBar.hide()
            } else {
                //show appbar
                if (supportActionBar != null) supportActionBar.show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp()
    }

}



