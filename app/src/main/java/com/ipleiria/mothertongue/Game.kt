package com.ipleiria.mothertongue

import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.ipleiria.mothertongue.models.GameLevel
import com.ipleiria.mothertongue.models.GamePhrase
import com.ipleiria.mothertongue.models.GameStatus

class Game {
    companion object {
        lateinit var gameStatus: GameStatus

        fun initializeGame() {
            var englishGamePhrases = arrayListOf(
                GamePhrase(phrase = "Computer", wasGuessed = false),
                GamePhrase(phrase = "Sunglasses", wasGuessed = false),
                GamePhrase(phrase = "Television", wasGuessed = false)
            )
            var englishGamePhrases2 = arrayListOf(
                GamePhrase(phrase = "Computer", wasGuessed = true),
                GamePhrase(phrase = "Sunglasses", wasGuessed = true),
                GamePhrase(phrase = "Television", wasGuessed = false)
            )

            var gameLevel = GameLevel(FirebaseTranslateLanguage.EN, englishGamePhrases)
            var gameLevel2 = GameLevel(FirebaseTranslateLanguage.EN, englishGamePhrases2)

            //ToDo: Get from persistence
            gameStatus = GameStatus("", arrayListOf())
            gameStatus.gameLevels.add(gameLevel)
            gameStatus.gameLevels.add(gameLevel2)
        }
    }
}