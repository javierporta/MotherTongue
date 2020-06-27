package com.ipleiria.mothertongue.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.ipleiria.mothertongue.LiveCamera
import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.data_manager.Game
import com.ipleiria.mothertongue.databinding.FragmentHomeBinding
import com.ipleiria.mothertongue.models.GameLevel
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.models.MainModel
import com.ipleiria.mothertongue.services.ContextService
import com.ipleiria.mothertongue.translations.TranslatorService
import java.lang.Exception

class HomeFragment : Fragment(),  AdapterView.OnItemSelectedListener  {


    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainModel: MainModel
    private var firebaseSelectedLanguageEnum: Int = 0

    private var PAGE = "inti"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainModel = MainModel("", "","")
        binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container, false)
        initializeLanguageSpinner()

        //bind mainModel
        binding.mainModel = mainModel

        binding.scoreTextView.text = Game.gameStatus.getScore().toString()

        val button: Button = binding.root.findViewById(R.id.gameStatusButton) as Button
        button.setOnClickListener{
            goToGameStatus(it)
        }


        val button2: Button = binding.root.findViewById(R.id.playButton) as Button
        button2.setOnClickListener{
            onClickPlayButton(it)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        stopLoading()
        binding.scoreTextView.text = Game.gameStatus.getScore().toString()

        arguments?.let {
            mainModel.currentPlaceName = it.getString("SELECTED_PLACE", "")
            binding.detectedPlaceNametextView.text = it.getString("SELECTED_PLACE", "")
        }

        if(PAGE != "Location" && mainModel.currentPlaceName == "") {

            if (!alreadyGrantedPermission()) {
                checkAndAskAccessFinePermission()
            } else {
                ContextService.instance.detectPlace(this, binding);
            }
        }
    }

    private fun checkAndAskAccessFinePermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    private fun alreadyGrantedPermission(): Boolean {
        return (checkSelfPermission(
            this.activity!!.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
                )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        ContextService.instance.detectPlace(this, binding)
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
    }



    private fun initializeLanguageSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.applicationContext?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.languages_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                binding.languageSpinner.adapter = adapter
            }
        }
        binding.languageSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        val selectedlanguage = parent.getItemAtPosition(pos)

        when (selectedlanguage) {
            "English" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.EN
            }
            "Spanish" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.ES
            }
            "Portuguese" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.PT
            }
            "French" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.FR
            }
            "German" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.DE
            }
            "Japanese" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.JA
            }
            "Chinese" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.ZH
            }
            "Arabic" -> {
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.AR
            }
            else -> { // Note the block
                //No language selected, proceed with English as default
                firebaseSelectedLanguageEnum = FirebaseTranslateLanguage.EN
            }
        }

        if (firebaseSelectedLanguageEnum != FirebaseTranslateLanguage.EN) { //Only for languages to be translated (not english)

            this.startLoading()

            TranslatorService(firebaseSelectedLanguageEnum).downloadModelIfNeeded()
                .addOnSuccessListener {

                    stopLoading()

                    Toast.makeText(
                        activity,
                        "Language is ready to use",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        activity,
                        "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    fun onClickPlayButton(view: View) {

        //WARNING: Similar things should not be together in this list
        //ToDo: get the one depending on context
        var currentGamePhrases = arrayListOf<GamePhrase>()
        Game.gameStatus.gameLevels.first().gamePhrases.forEach {
            if(it.contexts != null && it.contexts?.contains(mainModel.currentPlaceName)!!)
                currentGamePhrases.add(it)
        }

        var newPhrases = arrayListOf<GamePhrase>()
        currentGamePhrases.forEach {
            newPhrases.add(GamePhrase(it.phrase!!, false, it.contexts))
        }

        //Try to create a new game level
        var newGameLevel = GameLevel(
            mainModel.currentPlaceName + "-" + firebaseSelectedLanguageEnum,
            firebaseSelectedLanguageEnum,
            newPhrases,
            false
        )


        //Update current game to be played
        Game.gameStatus.currentGameLevelIndex =
            Game.addGameLevel(newGameLevel) //return new index or current indez

        var currentLevel = Game.gameStatus.gameLevels[Game.gameStatus.currentGameLevelIndex]
        if (currentLevel.isComplete) {
            showYesNoDialogReset(currentLevel, currentLevel.gamePhrases)
            return
        }

        firePlayButton(currentLevel.gamePhrases)
    }

    fun goToGameStatus(view: View) {
        this.findNavController().navigate(R.id.action_nav_home_to_gameProfile)
    }

    public fun startLoading() {
        binding.pBar.visibility = View.VISIBLE
        binding.playButton.isClickable = false
        binding.playButton.isEnabled = false
        binding.playButton.visibility = View.INVISIBLE

        binding.gameStatusButton.isClickable = false
        binding.gameStatusButton.isEnabled = false

    }

    public fun stopLoading() {
        binding.playButton.isClickable = true
        binding.playButton.isEnabled = true

        binding.pBar.visibility = View.INVISIBLE

        binding.gameStatusButton.isClickable = true
        binding.gameStatusButton.isEnabled = true

        if(mainModel.currentPlaceName != ""){
            binding.playButton.visibility = View.VISIBLE
        }
    }

    // Method to show an alert dialog with yes, no and cancel button
    private fun showYesNoDialogReset(
        currentLevel: GameLevel,
        currentGamePhrases: ArrayList<GamePhrase>
    ) {
        // Late initialize an alert dialog object
        lateinit var dialog: AlertDialog


        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(activity)

        // Set a title for alert dialog
        builder.setTitle("You already completed this level")

        // Set a message for alert dialog
        builder.setMessage("Would you to like to reset it and play again?")


        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    Game.resetGameLevel(currentLevel)
                    toast("Reset game level")
                    firePlayButton(currentGamePhrases)
                }
                //DialogInterface.BUTTON_NEGATIVE -> toast("Negative/No button clicked.")
                //DialogInterface.BUTTON_NEUTRAL -> toast("Neutral/Cancel button clicked.")
            }
        }


        // Set the alert dialog positive/yes button
        builder.setPositiveButton("YES", dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO", dialogClickListener)

        // Set the alert dialog neutral/cancel button
        builder.setNeutralButton("CANCEL", dialogClickListener)


        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

    private fun firePlayButton(currentGamePhrases: ArrayList<GamePhrase>) {
        if (firebaseSelectedLanguageEnum != FirebaseTranslateLanguage.EN) { //Only for languages to be translated (not english)
            translateGamePhrases(currentGamePhrases)
        } else {
            goToLiveCamera(currentGamePhrases)
        }
    }

    private fun goToLiveCamera(translatedGamePhrases: ArrayList<GamePhrase>) {
        var bundle = Bundle()
        bundle.putInt("firebaseSelectedLanguageEnum", firebaseSelectedLanguageEnum)
        bundle.putParcelableArrayList("gamePhrases", translatedGamePhrases)
        this.findNavController().navigate(R.id.action_nav_home_to_liveCamera, bundle)
    }

    private fun translateGamePhrases(gamePhrases: ArrayList<GamePhrase>) {
        startLoading()

        val translatorService = TranslatorService(firebaseSelectedLanguageEnum)
        var translatedPhrases =
            arrayListOf<GamePhrase>() //Create a copy of game phrases but it will have the translated phrases
        var isLastPhrase = false
        for (gamePhrase in gamePhrases.withIndex()) {
            translatorService.translate(gamePhrase.value.phrase!!)
                .addOnSuccessListener { translationResult: String? ->
                    if (translationResult != null) {
                        translatedPhrases.add(
                            GamePhrase(
                                phrase = translationResult!!,
                                wasGuessed = gamePhrase.value.wasGuessed,
                                contexts = gamePhrase.value.contexts
                            )
                        )
                        //update singleton
                        Game.gameStatus.gameLevels[Game.gameStatus.currentGameLevelIndex].gamePhrases[gamePhrase.index].phrase =
                            translationResult!!
                    }
                    if (gamePhrase.index == gamePhrases.count() - 1) {
                        isLastPhrase = true
                    }
                }.continueWith { if (isLastPhrase) goToLiveCamera(translatedPhrases) }
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.applicationContext?.let { Game.saveGame(it) }
    }

    // Extension function to show toast message
    fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
