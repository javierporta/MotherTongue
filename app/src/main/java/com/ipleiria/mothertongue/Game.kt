package com.ipleiria.mothertongue

import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.ipleiria.mothertongue.models.GameLevel
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.models.GameStatus
import java.io.Serializable

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
    }
}