package com.ipleiria.mothertongue.models

import java.io.Serializable

data class GameStatus(
    var username: String,
    var gameLevels: ArrayList<GameLevel>,
    var currentGameLevelIndex: Int = -1
) : Serializable {

    fun getScore(): Int {
        var score = 0
        if (gameLevels == null || gameLevels?.size == 0) {
            return score
        } else {
            //We suppose that each word guessed has the same weight for the score (it sums 10 points)
            for (gameLevel in gameLevels!!) {
                val guessedWordsCount = gameLevel.gamePhrases.count { it.wasGuessed }
                val levelScore = guessedWordsCount * 10;
                score += levelScore
            }
            return score
        }
    }


}