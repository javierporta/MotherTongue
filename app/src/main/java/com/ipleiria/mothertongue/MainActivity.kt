package com.ipleiria.mothertongue

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.ipleiria.mothertongue.databinding.ActivityMainBinding
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.models.MainModel
import com.ipleiria.mothertongue.services.ContextService
import com.ipleiria.mothertongue.translations.TranslatorService
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private val mainModel: MainModel = MainModel("", "Park","")
    private var firebaseSelectedLanguageEnum: Int = 0

    public  var nearbyPlaces ="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initializeLanguageSpinner()

        // ToDo:Get current language and current place

        //bind mainModel
        binding.mainModel = mainModel

        Game.initializeGame()
        binding.scoreTextView.text = Game.gameStatus.getScore().toString()
    }

    override fun onStart() {
        super.onStart()

        stopLoading()

        ContextService.instance.detectPlace(this, binding);
    }

    private fun initializeLanguageSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.languages_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.languageSpinner.adapter = adapter
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
                        applicationContext,
                        "Language is ready to use",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        applicationContext,
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

        //Todo: Pass list depending on context. Now using #HOME category#
        //Translate phrases
        //WARNING: Similar things should not be together in this list
        var currentGamePhrases = Game.gameStatus.gameLevels.first().gamePhrases

        if (firebaseSelectedLanguageEnum != FirebaseTranslateLanguage.EN) { //Only for languages to be translated (not english)
            translateGamePhrases(currentGamePhrases)
        } else {
            goToLiveCamera(currentGamePhrases)
        }
    }

    private fun goToLiveCamera(translatedGamePhrases: ArrayList<GamePhrase>) {
        val intent = Intent(this, LiveCamera::class.java)

        //Todo: add hardcoded text as companion object
        intent.putExtra("firebaseSelectedLanguageEnum", firebaseSelectedLanguageEnum)
        intent.putParcelableArrayListExtra("gamePhrases", translatedGamePhrases)
        // start your next activity
        startActivity(intent)
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
                                wasGuessed = gamePhrase.value.wasGuessed
                            )
                        )
                    }
                    if (gamePhrase.index == gamePhrases.count() - 1) {
                        isLastPhrase = true
                    }
                }.continueWith { if (isLastPhrase) goToLiveCamera(translatedPhrases) }
        }
    }

    public fun startLoading() {
        binding.pBar.visibility = View.VISIBLE
        binding.playButton.isClickable = false
        binding.playButton.isEnabled = false

    }

    public fun stopLoading() {
        binding.playButton.isClickable = true
        binding.playButton.isEnabled = true
        binding.pBar.visibility = View.GONE
    }


}
