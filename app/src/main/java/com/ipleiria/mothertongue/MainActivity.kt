package com.ipleiria.mothertongue

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.ipleiria.mothertongue.databinding.ActivityMainBinding
import com.ipleiria.mothertongue.models.GameLevel
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.models.GameStatus
import com.ipleiria.mothertongue.models.MainModel
import com.ipleiria.mothertongue.services.ContextService
import com.ipleiria.mothertongue.translations.TranslatorService
import java.io.*


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
        getGameData()

        binding.scoreTextView.text = Game.gameStatus.getScore().toString()
    }

    override fun onStart() {
        super.onStart()

        stopLoading()


        ContextService.instance.detectPlace(this, binding);
        
        binding.scoreTextView.text = Game.gameStatus.getScore().toString()

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
        //ToDo: get the one depending on context
        var currentGamePhrases = arrayListOf<GamePhrase>()
        Game.gameStatus.gameLevels.first().gamePhrases.forEach {
            currentGamePhrases.add(it)
        }

        var newPhrases = arrayListOf<GamePhrase>()
        currentGamePhrases.forEach {
            newPhrases.add(GamePhrase(it.phrase!!, false))
        }

        //Try to create a new game level
        var newGameLevel = GameLevel(
            "Home-" + firebaseSelectedLanguageEnum, //ToDo: Replace HOme by place/category
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

    private fun firePlayButton(currentGamePhrases: ArrayList<GamePhrase>) {
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

    override fun onPause() {
        super.onPause()
        try {
            val fileOutputStream: FileOutputStream =
                openFileOutput("game.bin", Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(Game.gameStatus)
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                this@MainActivity,
                "Could not write Game to internal storage.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getGameData() {
        try {
            val fileInputStream: FileInputStream = openFileInput("game.bin")
            val objectInputStream = ObjectInputStream(fileInputStream)
            Game.gameStatus = objectInputStream.readObject() as GameStatus

            objectInputStream.close()
            fileInputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(
                this@MainActivity,
                "Could not read GameStatus from internal storage (no GameStatus yet?).",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                this@MainActivity,
                "Error reading GameStatus from internal storage.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Toast.makeText(
                this@MainActivity,
                "Error reading GameStatus from internal storage.",
                Toast.LENGTH_LONG
            ).show()
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
        val builder = AlertDialog.Builder(this)

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

    // Extension function to show toast message
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        if ( requestCode == 1  && resultCode == RESULT_OK) {
            val selectdPlace: String? = data?.getStringExtra("SELECTED_PLACE")
            binding.detectedPlaceNametextView.text = selectdPlace
            stopLoading()
        }
        super.onActivityResult(requestCode, Activity.RESULT_FIRST_USER, data)
    }
}



