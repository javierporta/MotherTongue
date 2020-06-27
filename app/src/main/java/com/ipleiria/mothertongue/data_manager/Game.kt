package com.ipleiria.mothertongue.data_manager

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.ipleiria.mothertongue.models.GameLevel
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.models.GameStatus
import java.io.*

class Game {
    companion object {
        lateinit var gameStatus: GameStatus

        fun initializeGame() {
            //WARNING: Similar things should not be together in this list

            var englishGamePhrases = arrayListOf(
                GamePhrase(phrase = "Computer", wasGuessed = false, contexts = arrayOf("HOUSE")),
                GamePhrase(phrase = "Fruit", wasGuessed = false, contexts = arrayOf("SHOPPING","HOUSE")),
                GamePhrase(
                    phrase = "Musical Instrument",
                    wasGuessed = false,
                    contexts = arrayOf("HOUSE")
                ),
                GamePhrase(phrase = "Toy", wasGuessed = false, contexts = arrayOf("HOUSE")),
                GamePhrase(phrase = "Television", wasGuessed = false, contexts = arrayOf("HOUSE")),
                GamePhrase(phrase = "Pillow", wasGuessed = false, contexts = arrayOf("HOUSE")),
                GamePhrase(phrase = "Desktop", wasGuessed = false, contexts = arrayOf("HOUSE")),
                GamePhrase(
                    phrase = "Shoes",
                    wasGuessed = false,
                    contexts = arrayOf("SHOPPING", "HOUSE")
                ),
                GamePhrase(phrase = "Sunglasses", wasGuessed = false, contexts = arrayOf("PARK", "SHOPPING","HOUSE")),

                GamePhrase(phrase = "Flower", wasGuessed = false, contexts = arrayOf("PARK","STREET")),
                GamePhrase(phrase = "Plant", wasGuessed = false, contexts = arrayOf("PARK", "STREET")),
                GamePhrase(phrase = "Chair", wasGuessed = false, contexts = arrayOf("CAFE", "PARK", "HOUSE")),
                GamePhrase(phrase = "Sky", wasGuessed = false, contexts = arrayOf("PARK", "STREET")),
                GamePhrase(phrase = "Bicycle", wasGuessed = false, contexts = arrayOf("PARK", "STREET")),
                GamePhrase(phrase = "Dog", wasGuessed = false, contexts = arrayOf("PARK")),
                GamePhrase(phrase = "Cat", wasGuessed = false, contexts = arrayOf("PARK")),
                GamePhrase(phrase = "Lake", wasGuessed = false, contexts = arrayOf("PARK")),

                GamePhrase(phrase = "Stairs", wasGuessed = false, contexts = arrayOf("SHOPPING")),
                GamePhrase(phrase = "Vegetable", wasGuessed = false, contexts = arrayOf("SHOPPING")),
                GamePhrase(phrase = "Food", wasGuessed = false, contexts = arrayOf("SHOPPING")),
                GamePhrase(phrase = "Car", wasGuessed = false, contexts = arrayOf("PARK", "SHOPPING","STREET")),
                GamePhrase(phrase = "Dress", wasGuessed = false, contexts = arrayOf("SHOPPING")),
                GamePhrase(phrase = "Jeans", wasGuessed = false, contexts = arrayOf("SHOPPING")),

                GamePhrase(phrase = "Coffee", wasGuessed = false, contexts = arrayOf("CAFE")),
                GamePhrase(phrase = "Cup", wasGuessed = false, contexts = arrayOf("CAFE", "HOUSE")),
                GamePhrase(phrase = "Table", wasGuessed = false, contexts = arrayOf("CAFE","HOUSE")),
                GamePhrase(phrase = "Food", wasGuessed = false, contexts = arrayOf("CAFE")),
                GamePhrase(phrase = "Wine", wasGuessed = false, contexts = arrayOf("CAFE")),

                GamePhrase(phrase = "Building", wasGuessed = false, contexts = arrayOf( "STREET")),
                GamePhrase(phrase = "Tire", wasGuessed = false, contexts = arrayOf("STREET")),
                GamePhrase(phrase = "Street", wasGuessed = false, contexts = arrayOf("STREET")),
                GamePhrase(phrase = "Mountain", wasGuessed = false, contexts = arrayOf("STREET")),
                GamePhrase(phrase = "Church", wasGuessed = false, contexts = arrayOf("STREET")),
                GamePhrase(phrase = "Beach", wasGuessed = false, contexts = arrayOf("STREET"))
            )

            var gameLevel = GameLevel(
                "INITIAL-" + FirebaseTranslateLanguage.EN,
                FirebaseTranslateLanguage.EN,
                englishGamePhrases,
                false
            )

            gameStatus = GameStatus("", arrayListOf())
            gameStatus.gameLevels.add(gameLevel)
        }

        fun addGameLevel(gameLevel: GameLevel): Int {
            //check if GameLevel already exists
            var gameLevelSearched = gameStatus.gameLevels.find { x -> x.id == gameLevel.id }

            if (gameLevelSearched == null) {
                // add new game level
                gameStatus.gameLevels.add(gameLevel)
                return gameStatus.gameLevels.lastIndex

            } else {
                //gameLevel already exists
                return gameStatus.gameLevels.indexOf(gameLevelSearched)
            }
        }

        fun resetGameLevel(gameLevel: GameLevel) {
            var gameLevel = gameStatus.gameLevels.find { x -> x.id == gameLevel.id }

            if (gameLevel != null) {

                gameLevel.gamePhrases.forEach {
                    it.wasGuessed = false
                }
                gameLevel.isComplete = false;
            }
        }

        fun saveGame(ctx: Context) {
            try {
                val fileOutputStream: FileOutputStream =
                    ctx.openFileOutput("game.bin", Context.MODE_PRIVATE)
                val objectOutputStream = ObjectOutputStream(fileOutputStream)
                objectOutputStream.writeObject(Game.gameStatus)
                objectOutputStream.close()
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("ERROR WRITING FILE", "Could not write Game to internal storage.")
            }
        }

        fun getGameData(ctx: Context) {
            try {
                val fileInputStream: FileInputStream = ctx.openFileInput("game.bin")
                val objectInputStream = ObjectInputStream(fileInputStream)
                gameStatus = objectInputStream.readObject() as GameStatus

                objectInputStream.close()
                fileInputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(
                    ctx,
                    "Could not read GameStatus from internal storage (no GameStatus yet?).",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(
                    ctx,
                    "Error reading GameStatus from internal storage.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                Toast.makeText(
                    ctx,
                    "Error reading GameStatus from internal storage.",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }
}