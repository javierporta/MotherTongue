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
            var englishGamePhrases = arrayListOf(
                GamePhrase(phrase = "Computer", wasGuessed = false),
                GamePhrase(phrase = "Sunglasses", wasGuessed = false),
                GamePhrase(phrase = "Television", wasGuessed = false)
            )

            var gameLevel = GameLevel(
                "Home-" + FirebaseTranslateLanguage.EN,
                FirebaseTranslateLanguage.EN,
                englishGamePhrases,
                false
            )

            //ToDo: Get from persistence
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